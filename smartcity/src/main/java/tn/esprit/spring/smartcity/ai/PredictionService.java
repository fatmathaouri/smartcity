package tn.esprit.spring.smartcity.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.smartcity.dashboard.DashboardStatsDto;
import tn.esprit.spring.smartcity.report.Report;
import tn.esprit.spring.smartcity.report.ReportRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final ReportRepository reportRepository;

    public List<DashboardStatsDto.ZonePrediction> predictProblemZones() {
        List<Report> allReports = reportRepository.findAll();

        Map<String, List<Report>> byZone = allReports.stream()
                .filter(r -> r.getAddress() != null && !r.getAddress().isBlank())
                .collect(Collectors.groupingBy(this::extractZone));

        List<DashboardStatsDto.ZonePrediction> predictions = new ArrayList<>();

        for (var entry : byZone.entrySet()) {
            String zone = entry.getKey();
            List<Report> reports = entry.getValue();

            if (reports.size() < 3) continue;

            LocalDateTime now = LocalDateTime.now();
            long winterCount = reports.stream()
                    .filter(r -> {
                        int m = r.getCreatedAt().getMonthValue();
                        return m == 12 || m == 1 || m == 2;
                    })
                    .count();
            long otherCount = reports.size() - winterCount;

            double winterMonths = 3.0;
            double otherMonths = 9.0;
            double winterRate = winterCount / winterMonths;
            double otherRate = otherCount / otherMonths;

            double increase = 0;
            String recommendation;

            if (otherRate > 0) {
                increase = ((winterRate - otherRate) / otherRate) * 100;
            }

            if (increase > 50) {
                recommendation = "Maintenance préventive recommandée avant l'hiver pour " + zone;
            } else if (increase > 20) {
                recommendation = "Surveillance renforcée recommandée pour " + zone;
            } else {
                continue;
            }

            predictions.add(new DashboardStatsDto.ZonePrediction(zone, Math.round(increase * 10.0) / 10.0, recommendation));
        }

        predictions.sort((a, b) -> Double.compare(b.getIncidentIncrease(), a.getIncidentIncrease()));
        return predictions;
    }

    private String extractZone(Report report) {
        if (report.getAddress() == null) return "Inconnu";
        String addr = report.getAddress().toLowerCase();
        String[] parts = addr.split(",");
        if (parts.length >= 2) return parts[parts.length - 2].trim();
        if (parts.length == 1) return parts[0].trim();
        return addr.trim();
    }
}
