package tn.esprit.spring.smartcity.sla;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.smartcity.category.Category;
import tn.esprit.spring.smartcity.category.CategoryRepository;
import tn.esprit.spring.smartcity.citizen.Citizen;
import tn.esprit.spring.smartcity.citizen.CitizenRepository;
import tn.esprit.spring.smartcity.notification.NotificationService;
import tn.esprit.spring.smartcity.report.Report;
import tn.esprit.spring.smartcity.report.ReportRepository;
import tn.esprit.spring.smartcity.report.ReportStatus;
import tn.esprit.spring.smartcity.report.TreatmentStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlaService {

    private final ReportRepository reportRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;
    private final CitizenRepository citizenRepository;

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void checkSlaDeadlines() {
        log.info("Running SLA deadline check...");
        List<Report> activeReports = reportRepository.findBySlaDeadlineBeforeAndSlaBreachedAndTreatmentStatusNotIn(
                LocalDateTime.now(), false,
                Arrays.asList(TreatmentStatus.RESOLVED, TreatmentStatus.VALIDATED));

        for (Report report : activeReports) {
            report.setSlaBreached(true);
            reportRepository.save(report);

            if (report.getCitizen() != null) {
                Citizen citizen = report.getCitizen();
                long hoursLate = Duration.between(report.getSlaDeadline(), LocalDateTime.now()).toHours();
                notificationService.notifySlaBreach(citizen, report, hoursLate);
            }

            long hoursLate = Duration.between(report.getSlaDeadline(), LocalDateTime.now()).toHours();
            String breachMsg = "Alerte SLA : le signalement \"" + report.getTitle() + "\" a dépassé le délai de " + hoursLate + "h";
            notificationService.notifyManagerEvent(report, breachMsg);
            notificationService.notifyAssignedAgentEvent(report, breachMsg);
            notificationService.notifyAdminsAndMunicipality(report, breachMsg);

            log.warn("SLA BREACHED for report #{}: {} ({} hours late)",
                    report.getId(), report.getTitle(),
                    Duration.between(report.getSlaDeadline(), LocalDateTime.now()).toHours());
        }
        log.info("SLA check complete. {} breaches found.", activeReports.size());
    }

    public void checkSlaWarning(Report report) {
        if (report.getSlaDeadline() == null || report.getSlaBreached()) return;

        long hoursRemaining = Duration.between(LocalDateTime.now(), report.getSlaDeadline()).toHours();
        Category cat = report.getCategory();
        if (cat != null && cat.getEscalationHours() != null && hoursRemaining <= cat.getEscalationHours() && hoursRemaining > 0) {
            if (report.getCitizen() != null) {
                Citizen citizen = report.getCitizen();
                notificationService.notifySlaWarning(citizen, report, hoursRemaining);
            }
            String warningMsg = "Attention : il ne reste que " + hoursRemaining + "h pour résoudre \"" + report.getTitle() + "\" avant la deadline SLA";
            notificationService.notifyManagerEvent(report, warningMsg);
            notificationService.notifyAssignedAgentEvent(report, warningMsg);
        }
    }

    public Map<String, Object> getSlaStats() {
        List<Report> allReports = reportRepository.findAll();
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalActive = allReports.stream()
                .filter(r -> r.getSlaDeadline() != null)
                .filter(r -> r.getTreatmentStatus() != TreatmentStatus.RESOLVED && r.getTreatmentStatus() != TreatmentStatus.VALIDATED)
                .count();
        long breached = allReports.stream()
                .filter(r -> Boolean.TRUE.equals(r.getSlaBreached()))
                .count();
        long onTime = totalActive - breached;

        stats.put("totalActive", totalActive);
        stats.put("breached", breached);
        stats.put("onTime", onTime);
        stats.put("complianceRate", totalActive > 0 ? Math.round(onTime * 100.0 / totalActive) : 100);

        List<Map<String, Object>> byCategory = new ArrayList<>();
        Map<Category, List<Report>> reportsByCategory = allReports.stream()
                .filter(r -> r.getSlaDeadline() != null && r.getCategory() != null)
                .collect(Collectors.groupingBy(Report::getCategory));

        for (Map.Entry<Category, List<Report>> entry : reportsByCategory.entrySet()) {
            Category cat = entry.getKey();
            List<Report> reports = entry.getValue();
            long catTotal = reports.size();
            long catBreached = reports.stream().filter(r -> Boolean.TRUE.equals(r.getSlaBreached())).count();
            long catResolved = reports.stream()
                    .filter(r -> r.getTreatmentStatus() == TreatmentStatus.RESOLVED || r.getTreatmentStatus() == TreatmentStatus.VALIDATED)
                    .count();

            double avgResolutionHours = reports.stream()
                    .filter(r -> r.getResolvedAt() != null && r.getCreatedAt() != null)
                    .mapToLong(r -> Duration.between(r.getCreatedAt(), r.getResolvedAt()).toHours())
                    .average().orElse(0.0);

            Map<String, Object> catStats = new LinkedHashMap<>();
            catStats.put("categoryId", cat.getId());
            catStats.put("categoryName", cat.getName());
            catStats.put("slaHours", cat.getSlaHours());
            catStats.put("total", catTotal);
            catStats.put("breached", catBreached);
            catStats.put("resolved", catResolved);
            catStats.put("complianceRate", catTotal > 0 ? Math.round((catTotal - catBreached) * 100.0 / catTotal) : 100);
            catStats.put("avgResolutionHours", Math.round(avgResolutionHours));
            byCategory.add(catStats);
        }

        stats.put("byCategory", byCategory);
        return stats;
    }

    public List<Map<String, Object>> getSlaConfig() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(cat -> {
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("categoryId", cat.getId());
            config.put("categoryName", cat.getName());
            config.put("slaHours", cat.getSlaHours());
            config.put("escalationHours", cat.getEscalationHours());
            return config;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Category updateSlaConfig(Long categoryId, Integer slaHours, Integer escalationHours) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (slaHours != null) category.setSlaHours(slaHours);
        if (escalationHours != null) category.setEscalationHours(escalationHours);
        return categoryRepository.save(category);
    }
}
