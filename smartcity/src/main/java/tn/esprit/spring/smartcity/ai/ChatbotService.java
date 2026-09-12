package tn.esprit.spring.smartcity.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;
import tn.esprit.spring.smartcity.citizen.Citizen;
import tn.esprit.spring.smartcity.citizen.CitizenRepository;
import tn.esprit.spring.smartcity.category.Category;
import tn.esprit.spring.smartcity.category.CategoryRepository;
import tn.esprit.spring.smartcity.report.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final UserRepository userRepository;
    private final CitizenRepository citizenRepository;
    private final ReportRepository reportRepository;
    private final CategoryRepository categoryRepository;

    private static final Map<String, List<String>> KEYWORDS = Map.of(
            "statut", List.of("statut", "état", "avancement", "situé", "situation"),
            "signaler", List.of("signaler", "problème", "incident", "signalement", "plainte"),
            "catégorie", List.of("catégorie", "categorie", "type", "domaine"),
            "aide", List.of("aide", "help", "comment", "pourquoi", "besoin"),
            "merci", List.of("merci", "remercie", "super", "bravo", "génial"),
            "bonjour", List.of("bonjour", "salut", "hello", "coucou", "bonsoir"),
            "stats", List.of("statistique", "stats", "nombre", "total", "chiffre"),
            "catégories", List.of("catégories", "catégorie", "voirie", "propreté", "éclairage")
    );

    public Map<String, Object> chat(String message, Long userId) {
        String lower = message.toLowerCase().trim();
        String intent = detectIntent(lower);

        String reply;
        switch (intent) {
            case "bonjour" -> reply = "Bonjour ! Je suis l'assistant SmartCity. Comment puis-je vous aider ?";
            case "statut" -> reply = handleStatutQuery(userId);
            case "signaler" -> reply = "Pour signaler un problème, allez dans Signalements puis cliquez Nouveau signalement.";
            case "catégorie" -> reply = handleCategoryQuery(lower);
            case "catégories" -> reply = handleCategoryList();
            case "aide" -> reply = "Je peux vérifier vos signalements, lister les catégories, ou donner des statistiques.";
            case "merci" -> reply = "Avec plaisir ! N'hésitez pas si vous avez d'autres questions.";
            case "stats" -> reply = handleStatsQuery();
            default -> reply = "Je ne comprends pas. Demandez le statut d'un signalement, les catégories, ou des statistiques.";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("reply", reply);
        response.put("intent", intent);
        return response;
    }

    private String detectIntent(String message) {
        for (Map.Entry<String, List<String>> entry : KEYWORDS.entrySet()) {
            for (String kw : entry.getValue()) {
                if (message.contains(kw)) return entry.getKey();
            }
        }
        return "unknown";
    }

    private String handleStatutQuery(Long userId) {
        if (userId == null) return "Connectez-vous pour vérifier vos signalements.";
        Citizen citizen = citizenRepository.findByUserId(userId).orElse(null);
        if (citizen == null) return "Profil citoyen introuvable.";
        List<Report> reports = reportRepository.findByCitizenId(citizen.getId());
        if (reports.isEmpty()) return "Vous n'avez aucun signalement.";
        StringBuilder sb = new StringBuilder("Vos signalements :\n");
        for (Report r : reports) {
            sb.append("- ").append(r.getTitle()).append(" — Statut: ")
              .append(r.getStatus()).append(", Traitement: ").append(r.getTreatmentStatus()).append("\n");
        }
        return sb.toString();
    }

    private String handleCategoryQuery(String message) {
        if (message.contains("voirie")) return "Voirie : routes, trottoirs, nids-de-poule. Priorité : Haute.";
        if (message.contains("propreté") || message.contains("déchet")) return "Propreté : déchets, poubelles, nettoyage. Priorité : Moyenne.";
        if (message.contains("éclairage")) return "Éclairage : lampadaires, éclairage public. Priorité : Moyenne.";
        if (message.contains("eau") || message.contains("assainissement")) return "Eau et Assainissement : fuites, égouts, inondations. Priorité : Haute.";
        if (message.contains("espace") || message.contains("vert")) return "Espaces Verts : parcs, jardins, arbres. Priorité : Basse.";
        if (message.contains("stationnement") || message.contains("parking")) return "Stationnement : parking. Priorité : Basse.";
        return "Catégories : Voirie, Propreté, Éclairage, Espaces Verts, Eau et Assainissement, Stationnement.";
    }

    private String handleCategoryList() {
        List<Category> cats = categoryRepository.findAll();
        StringBuilder sb = new StringBuilder("Catégories :\n");
        for (Category c : cats) {
            sb.append("- ").append(c.getName()).append(" : ").append(c.getDescription()).append("\n");
        }
        return sb.toString();
    }

    private String handleStatsQuery() {
        long total = reportRepository.count();
        long pending = reportRepository.countByStatus(ReportStatus.PENDING);
        long resolved = reportRepository.countByStatus(ReportStatus.RESOLVED);
        return String.format("Stats : Total=%d, En attente=%d, Résolus=%d, Taux=%d%%",
                total, pending, resolved, total > 0 ? (resolved * 100 / total) : 0);
    }
}
