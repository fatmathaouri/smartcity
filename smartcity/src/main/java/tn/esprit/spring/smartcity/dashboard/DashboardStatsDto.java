package tn.esprit.spring.smartcity.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalReports;
    private long pendingReports;
    private long inProgressReports;
    private long resolvedReports;
    private Map<String, Long> reportsByCategory;
    private Map<Integer, Long> reportsByMonth;
    private List<ZonePrediction> predictions;

    @Data
    @AllArgsConstructor
    public static class ZonePrediction {
        private String zoneName;
        private double incidentIncrease;
        private String recommendation;
    }
}
