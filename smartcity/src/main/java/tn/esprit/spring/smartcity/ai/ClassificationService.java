package tn.esprit.spring.smartcity.ai;

import org.springframework.stereotype.Service;
import tn.esprit.spring.smartcity.report.Priority;
import tn.esprit.spring.smartcity.report.Report;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

@Service
public class ClassificationService {

    private static final List<Map.Entry<String, ClassificationRule>> RULES = List.of(
        new AbstractMap.SimpleEntry<>("Voirie", new ClassificationRule(
            List.of("trou", "nid de poule", "nid-de-poule", "route cassée", "chaussée", "route abîmée",
                    "dos d'âne", "ralentisseur cassé", "trottoir", "bitume", "crevasse"),
            Priority.HIGH)),
        new AbstractMap.SimpleEntry<>("Propreté", new ClassificationRule(
            List.of("poubelle", "déchet", "ordure", "sale", "décharge", "immondice", "carte grasse",
                    "dépotoir", "décharge sauvage", "sac poubelle", "encombrant"),
            Priority.MEDIUM)),
        new AbstractMap.SimpleEntry<>("Éclairage", new ClassificationRule(
            List.of("lampadaire", "éclairage", "lumière", "réverbère", "ampoule grillée", "obscurité",
                    "sombre", "panne éclairage", "lampe"),
            Priority.MEDIUM)),
        new AbstractMap.SimpleEntry<>("Espaces Verts", new ClassificationRule(
            List.of("arbre", "parc", "jardin", "vert", "pelouse", "branche", "feuille morte",
                    "racine", "espace vert", "plantation"),
            Priority.LOW)),
        new AbstractMap.SimpleEntry<>("Eau et Assainissement", new ClassificationRule(
            List.of("fuite", "eau", "inondation", "égout", "cassé tuyau", "égoût", "canalisation",
                    "plomberie", "ruissellement", "marre d'eau"),
            Priority.HIGH)),
        new AbstractMap.SimpleEntry<>("Stationnement", new ClassificationRule(
            List.of("stationnement", "parking", "garé", "stationner", "voiture garée", "station interdit",
                    "place parking"),
            Priority.LOW)),
        new AbstractMap.SimpleEntry<>("Bruit", new ClassificationRule(
            List.of("bruit", "nuisance sonore", "tapage", "vacarme", "bruit fort", "musique forte"),
            Priority.MEDIUM)),
        new AbstractMap.SimpleEntry<>("Sécurité", new ClassificationRule(
            List.of("danger", "sécurité", "accident", "agression", "dangereux", "insécurité",
                    "cambriolage", "vol", "incivilité"),
            Priority.HIGH))
    );

    public ClassificationResult classify(String text) {
        String lowerText = text.toLowerCase();
        int bestScore = 0;
        String bestCategory = null;
        Priority bestPriority = Priority.MEDIUM;

        for (var entry : RULES) {
            ClassificationRule rule = entry.getValue();
            int score = 0;
            for (String keyword : rule.keywords) {
                if (lowerText.contains(keyword)) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestCategory = entry.getKey();
                bestPriority = rule.priority;
            }
        }

        return new ClassificationResult(bestCategory, bestPriority, bestScore);
    }

    public void classify(Report report) {
        String text = (report.getTitle() != null ? report.getTitle() : "")
                + " " + (report.getDescription() != null ? report.getDescription() : "");
        ClassificationResult result = classify(text);

        if (report.getCategory() == null && result.category != null) {
            report.setCategory(new tn.esprit.spring.smartcity.category.Category());
            report.getCategory().setName(result.category);
        }

        if (report.getPriority() == null) {
            report.setPriority(result.priority);
        }
    }

    public record ClassificationResult(String category, Priority priority, int score) {}
    private record ClassificationRule(List<String> keywords, Priority priority) {}
}
