package tn.esprit.spring.smartcity.planning;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;
import tn.esprit.spring.smartcity.report.Report;
import tn.esprit.spring.smartcity.report.ReportRepository;
import tn.esprit.spring.smartcity.report.TreatmentStatus;
import tn.esprit.spring.smartcity.notification.NotificationService;
import tn.esprit.spring.smartcity.agent.DepartmentManager;
import tn.esprit.spring.smartcity.agent.DepartmentManagerRepository;
import tn.esprit.spring.smartcity.agent.MunicipalAgent;
import tn.esprit.spring.smartcity.agent.MunicipalAgentRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final InterventionPlanRepository planRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final NotificationService notificationService;
    private final DepartmentManagerRepository managerRepository;
    private final MunicipalAgentRepository agentRepository;

    @Transactional
    public InterventionPlan createPlan(Long reportId, Long agentId, LocalDateTime plannedDate,
                                        String timeSlot, String notes) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        List<InterventionPlan> conflicts = planRepository.findConflicts(agentId, plannedDate, timeSlot);
        if (!conflicts.isEmpty()) {
            InterventionPlan conflict = conflicts.get(0);
            throw new RuntimeException("CONFLICT:" + agent.getFirstName() + " " + agent.getLastName()
                    + " est déjà planifié le " + conflict.getPlannedDate().toLocalDate()
                    + " de " + conflict.getTimeSlot()
                    + " pour " + conflict.getReport().getTitle());
        }

        InterventionPlan plan = InterventionPlan.builder()
                .report(report)
                .agent(agent)
                .plannedDate(plannedDate)
                .timeSlot(timeSlot)
                .notes(notes)
                .build();
        InterventionPlan saved = planRepository.save(plan);

        notificationService.createUserTypedNotification(agent, report,
                "Vous avez été planifié pour \"" + report.getTitle() + "\" le "
                        + plannedDate.toLocalDate() + " (" + timeSlot + ")",
                tn.esprit.spring.smartcity.notification.NotificationType.PLANNING);

        if (report.getCategory() != null) {
            notifyManagerOfPlan(agent, report, "Nouveau plan : " + agent.getFirstName() + " " + agent.getLastName()
                    + " interviendra pour \"" + report.getTitle() + "\" le " + plannedDate.toLocalDate());
        }

        return saved;
    }

    public List<InterventionPlan> getPlansForAgent(Long agentId) {
        return planRepository.findByAgentIdOrderByPlannedDateAsc(agentId);
    }

    public List<InterventionPlan> getPlansForDateRange(LocalDateTime start, LocalDateTime end) {
        return planRepository.findByPlannedDateBetween(start, end);
    }

    public List<InterventionPlan> getPlansForAgentAndDateRange(Long agentId, LocalDateTime start, LocalDateTime end) {
        return planRepository.findByAgentIdAndPlannedDateBetween(agentId, start, end);
    }

    @Transactional
    public InterventionPlan updateStatus(Long planId, PlanStatus status) {
        InterventionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        plan.setStatus(status);

        Report report = plan.getReport();
        User agent = plan.getAgent();

        switch (status) {
            case IN_PROGRESS:
                report.setTreatmentStatus(TreatmentStatus.IN_PROGRESS);
                report.setUpdatedAt(LocalDateTime.now());
                reportRepository.save(report);
                if (report.getCitizen() != null) {
                    notificationService.notifyStatusChanged(report.getCitizen(), report, "En cours");
                }
                notificationService.notifyManagerEvent(report,
                        "L'agent " + agent.getFirstName() + " " + agent.getLastName()
                                + " a commencé l'intervention pour \"" + report.getTitle() + "\"");
                break;

            case COMPLETED:
                plan.setCompletedAt(LocalDateTime.now());
                report.setTreatmentStatus(TreatmentStatus.RESOLVED);
                report.setStatus(tn.esprit.spring.smartcity.report.ReportStatus.RESOLVED);
                report.setResolvedAt(LocalDateTime.now());
                report.setUpdatedAt(LocalDateTime.now());
                reportRepository.save(report);
                if (report.getCitizen() != null) {
                    notificationService.notifyStatusChanged(report.getCitizen(), report, "Résolu");
                }
                notifyManagerOfPlan(agent, report,
                        "\"" + report.getTitle() + "\" terminé par " + agent.getFirstName() + " " + agent.getLastName()
                                + " — en attente de validation");
                break;

            case VALIDATED:
                plan.setValidatedAt(LocalDateTime.now());
                report.setTreatmentStatus(TreatmentStatus.VALIDATED);
                report.setUpdatedAt(LocalDateTime.now());
                reportRepository.save(report);
                if (report.getCitizen() != null) {
                    notificationService.notifyStatusChanged(report.getCitizen(), report, "Validé");
                }
                notificationService.createUserTypedNotification(agent, report,
                        "Votre intervention pour \"" + report.getTitle() + "\" a été validée",
                        tn.esprit.spring.smartcity.notification.NotificationType.PLANNING);
                notificationService.notifyAdminsAndMunicipality(report,
                        "Le signalement \"" + report.getTitle() + "\" a été validé");
                break;

            case CANCELLED:
                if (report.getCitizen() != null) {
                    notificationService.notifyStatusChanged(report.getCitizen(), report, "Plan annulé");
                }
                notificationService.createUserTypedNotification(agent, report,
                        "Le plan d'intervention pour \"" + report.getTitle() + "\" a été annulé",
                        tn.esprit.spring.smartcity.notification.NotificationType.PLANNING);
                break;

            default:
                break;
        }

        return planRepository.save(plan);
    }

    @Transactional
    public InterventionPlan validatePlan(Long planId, Long managerUserId) {
        InterventionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        DepartmentManager manager = managerRepository.findByUserId(managerUserId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        plan.setStatus(PlanStatus.VALIDATED);
        plan.setValidatedAt(LocalDateTime.now());

        Report report = plan.getReport();
        report.setTreatmentStatus(TreatmentStatus.VALIDATED);
        report.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report);

        if (report.getCitizen() != null) {
            notificationService.notifyStatusChanged(report.getCitizen(), report, "Validé");
        }
        notificationService.createUserTypedNotification(plan.getAgent(), report,
                "Votre intervention pour \"" + report.getTitle() + "\" a été validée par le manager",
                tn.esprit.spring.smartcity.notification.NotificationType.PLANNING);
        notificationService.notifyAdminsAndMunicipality(report,
                "Le signalement \"" + report.getTitle() + "\" a été validé");

        return planRepository.save(plan);
    }

    @Transactional
    public InterventionPlan rejectPlan(Long planId, Long managerUserId, String reason) {
        InterventionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        plan.setStatus(PlanStatus.CANCELLED);
        plan.setRejectionReason(reason);
        plan.setCompletedAt(null);

        Report report = plan.getReport();
        report.setTreatmentStatus(TreatmentStatus.IN_PROGRESS);
        report.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report);

        notificationService.createUserTypedNotification(plan.getAgent(), report,
                "Le plan pour \"" + report.getTitle() + "\" a été rejeté" +
                        (reason != null ? " : " + reason : ""),
                tn.esprit.spring.smartcity.notification.NotificationType.PLANNING);

        return planRepository.save(plan);
    }

    @Transactional
    public InterventionPlan updatePlan(Long planId, LocalDateTime plannedDate, String timeSlot, String notes) {
        InterventionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        if (plannedDate != null) plan.setPlannedDate(plannedDate);
        if (timeSlot != null) plan.setTimeSlot(timeSlot);
        if (notes != null) plan.setNotes(notes);
        return planRepository.save(plan);
    }

    @Transactional
    public void deletePlan(Long planId) {
        planRepository.deleteById(planId);
    }

    public Map<String, Object> getWeekOverview(Long agentId, LocalDate weekStart) {
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekStart.plusDays(7).atTime(LocalTime.MAX);

        List<InterventionPlan> plans;
        if (agentId != null) {
            plans = planRepository.findByAgentIdAndPlannedDateBetween(agentId, start, end);
        } else {
            plans = planRepository.findInRange(start, end);
        }

        long planned = plans.stream().filter(p -> p.getStatus() == PlanStatus.PLANNED).count();
        long inProgress = plans.stream().filter(p -> p.getStatus() == PlanStatus.IN_PROGRESS).count();
        long completed = plans.stream().filter(p -> p.getStatus() == PlanStatus.COMPLETED).count();
        long validated = plans.stream().filter(p -> p.getStatus() == PlanStatus.VALIDATED).count();
        long cancelled = plans.stream().filter(p -> p.getStatus() == PlanStatus.CANCELLED).count();

        Map<String, Object> wm = new java.util.LinkedHashMap<>();
        wm.put("weekStart", weekStart.toString());
        wm.put("totalPlans", plans.size());
        wm.put("planned", planned);
        wm.put("inProgress", inProgress);
        wm.put("completed", completed);
        wm.put("validated", validated);
        wm.put("cancelled", cancelled);
        wm.put("plans", plans.stream().map(this::toDto).collect(Collectors.toList()));
        return wm;
    }

    public List<Map<String, Object>> getCompletedPlans() {
        return planRepository.findCompletedPlans().stream()
                .map(this::toDtoWithDetails)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getPlanningStats(LocalDate weekStart) {
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekStart.plusDays(7).atTime(LocalTime.MAX);

        List<InterventionPlan> allPlans = planRepository.findActiveInRange(start, end);

        Map<Long, String> agentNames = new LinkedHashMap<>();
        Map<Long, long[]> agentStats = new LinkedHashMap<>();

        for (InterventionPlan plan : allPlans) {
            Long agentUserId = plan.getAgent().getId();
            agentNames.putIfAbsent(agentUserId, plan.getAgent().getFirstName() + " " + plan.getAgent().getLastName());
            agentStats.putIfAbsent(agentUserId, new long[]{0, 0, 0, 0, 0});

            long[] stats = agentStats.get(agentUserId);
            stats[0]++;
            switch (plan.getStatus()) {
                case PLANNED: stats[1]++; break;
                case IN_PROGRESS: stats[2]++; break;
                case COMPLETED: stats[3]++; break;
                case VALIDATED: stats[4]++; break;
                default: break;
            }
        }

        List<Map<String, Object>> perAgent = new ArrayList<>();
        for (Map.Entry<Long, long[]> entry : agentStats.entrySet()) {
            long[] s = entry.getValue();
            Map<String, Object> am = new LinkedHashMap<>();
            am.put("agentUserId", entry.getKey());
            am.put("agentName", agentNames.get(entry.getKey()));
            am.put("total", s[0]);
            am.put("planned", s[1]);
            am.put("inProgress", s[2]);
            am.put("completed", s[3]);
            am.put("validated", s[4]);
            perAgent.add(am);
        }

        long totalValidated = allPlans.stream().filter(p -> p.getStatus() == PlanStatus.VALIDATED).count();
        long totalCompletedOrValidated = allPlans.stream()
                .filter(p -> p.getStatus() == PlanStatus.COMPLETED || p.getStatus() == PlanStatus.VALIDATED)
                .count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("weekStart", weekStart.toString());
        stats.put("totalPlans", allPlans.size());
        stats.put("perAgent", perAgent);
        stats.put("completionRate", allPlans.isEmpty() ? 0 : Math.round((totalCompletedOrValidated * 100.0) / allPlans.size()));
        return stats;
    }

    private void notifyManagerOfPlan(User agent, Report report, String message) {
        if (report.getCategory() == null) return;
        List<tn.esprit.spring.smartcity.department.Department> departments =
                new ArrayList<>();
        try {
            var deptRepo = report.getCategory();
            var found = managerRepository.findByUserId(agent.getId());
            if (found.isPresent() && found.get().getDepartment() != null) {
                Long deptId = found.get().getDepartment().getId();
                managerRepository.findByDepartmentId(deptId).ifPresent(manager ->
                        notificationService.createUserTypedNotification(manager.getUser(), report, message,
                                tn.esprit.spring.smartcity.notification.NotificationType.PLANNING));
            }
        } catch (Exception ignored) {}
    }

    public Map<String, Object> toDto(InterventionPlan plan) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", plan.getId());
        dto.put("reportId", plan.getReport().getId());
        dto.put("reportTitle", plan.getReport().getTitle());
        dto.put("agentId", plan.getAgent().getId());
        dto.put("agentName", plan.getAgent().getFirstName() + " " + plan.getAgent().getLastName());
        dto.put("plannedDate", plan.getPlannedDate().toString());
        dto.put("timeSlot", plan.getTimeSlot());
        dto.put("status", plan.getStatus().name());
        dto.put("notes", plan.getNotes() != null ? plan.getNotes() : "");
        dto.put("createdAt", plan.getCreatedAt().toString());
        return dto;
    }

    private Map<String, Object> toDtoWithDetails(InterventionPlan plan) {
        Map<String, Object> dto = toDto(plan);
        Report report = plan.getReport();
        dto.put("reportAddress", report.getAddress() != null ? report.getAddress() : "");
        dto.put("reportCategory", report.getCategory() != null ? report.getCategory().getName() : "");
        dto.put("reportPriority", report.getPriority() != null ? report.getPriority().name() : "");
        dto.put("reportSlaDeadline", report.getSlaDeadline() != null ? report.getSlaDeadline().toString() : null);
        dto.put("reportSlaBreached", report.getSlaBreached());
        dto.put("completedAt", plan.getCompletedAt() != null ? plan.getCompletedAt().toString() : null);
        dto.put("validatedAt", plan.getValidatedAt() != null ? plan.getValidatedAt().toString() : null);
        dto.put("rejectionReason", plan.getRejectionReason() != null ? plan.getRejectionReason() : null);

        if (plan.getCompletedAt() != null && plan.getCreatedAt() != null) {
            long hours = Duration.between(plan.getCreatedAt(), plan.getCompletedAt()).toHours();
            long minutes = Duration.between(plan.getCreatedAt(), plan.getCompletedAt()).toMinutes() % 60;
            dto.put("duration", hours + "h" + (minutes > 0 ? String.format("%02d", minutes) : "00"));
        }

        if (report.getSlaDeadline() != null) {
            long hoursRemaining = Duration.between(LocalDateTime.now(), report.getSlaDeadline()).toHours();
            dto.put("slaHoursRemaining", hoursRemaining);
            dto.put("slaRespected", hoursRemaining > 0);
        }

        return dto;
    }
}
