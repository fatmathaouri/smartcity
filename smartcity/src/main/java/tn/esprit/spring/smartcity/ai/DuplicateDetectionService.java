package tn.esprit.spring.smartcity.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.smartcity.report.Report;
import tn.esprit.spring.smartcity.report.ReportRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DuplicateDetectionService {

    private final ReportRepository reportRepository;
    private static final double SIMILARITY_THRESHOLD = 0.5;

    public List<DuplicateResult> findDuplicates(Long reportId) {
        Report currentReport = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        List<Report> allReports = reportRepository.findAll().stream()
                .filter(r -> !r.getId().equals(reportId))
                .collect(Collectors.toList());

        String currentText = normalize(currentReport.getTitle() + " " + currentReport.getDescription());

        List<DuplicateResult> results = new ArrayList<>();
        for (Report other : allReports) {
            String otherText = normalize(other.getTitle() + " " + other.getDescription());
            double similarity = jaccardSimilarity(currentText, otherText);

            if (similarity >= SIMILARITY_THRESHOLD) {
                results.add(new DuplicateResult(other.getId(), other.getTitle(), similarity));
            }
        }

        results.sort((a, b) -> Double.compare(b.similarity, a.similarity));
        return results;
    }

    private double jaccardSimilarity(String s1, String s2) {
        Set<String> set1 = tokenize(s1);
        Set<String> set2 = tokenize(s2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) return 0;
        return (double) intersection.size() / union.size();
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("[\\s,.;!?]+"))
                .filter(w -> w.length() > 2)
                .collect(Collectors.toSet());
    }

    private String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("[éèêë]", "e")
                .replaceAll("[àâä]", "a")
                .replaceAll("[îï]", "i")
                .replaceAll("[ôö]", "o")
                .replaceAll("[ùûü]", "u")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public record DuplicateResult(Long reportId, String title, double similarity) {}
}
