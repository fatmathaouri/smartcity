package tn.esprit.spring.smartcity.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.smartcity.ai.PredictionService;
import tn.esprit.spring.smartcity.report.ReportRepository;
import tn.esprit.spring.smartcity.report.ReportStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ReportRepository reportRepository;
    private final PredictionService predictionService;

    public DashboardStatsDto getDashboardStats() {
        long total = reportRepository.count();
        long pending = reportRepository.countByStatus(ReportStatus.PENDING);
        long inProgress = reportRepository.countByStatus(ReportStatus.IN_PROGRESS);
        long resolved = reportRepository.countByStatus(ReportStatus.RESOLVED);

        Map<String, Long> byCategory = new HashMap<>();
        List<Object[]> categoryData = reportRepository.countByCategory();
        for (Object[] row : categoryData) {
            byCategory.put("category_" + row[0], (Long) row[1]);
        }

        Map<Integer, Long> byMonth = new HashMap<>();
        List<Object[]> monthData = reportRepository.countByMonth();
        for (Object[] row : monthData) {
            byMonth.put((Integer) row[0], (Long) row[1]);
        }

        List<DashboardStatsDto.ZonePrediction> predictions = predictionService.predictProblemZones();

        return DashboardStatsDto.builder()
                .totalReports(total)
                .pendingReports(pending)
                .inProgressReports(inProgress)
                .resolvedReports(resolved)
                .reportsByCategory(byCategory)
                .reportsByMonth(byMonth)
                .predictions(predictions)
                .build();
    }
}
