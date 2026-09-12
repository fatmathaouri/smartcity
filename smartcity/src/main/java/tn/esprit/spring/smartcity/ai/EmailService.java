package tn.esprit.spring.smartcity.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendAccountCreationEmail(String to, String firstName, String tempPassword, String email) {
        log.info("Sending account creation email to {} (temp password: {})", to, tempPassword);
        log.info("Subject: Bienvenue dans SmartCity - Vos identifiants");
        log.info("Body: Bonjour {}, votre compte a été créé. Email: {}, Mot de passe temporaire: {}. Changez-le à la première connexion.", firstName, email, tempPassword);
    }

    public void sendPasswordResetEmail(String to, String firstName, String tempPassword) {
        log.info("Sending password reset email to {} (temp password: {})", to, tempPassword);
        log.info("Subject: SmartCity - Réinitialisation de mot de passe");
        log.info("Body: Bonjour {}, votre mot de passe a été réinitialisé. Nouveau mot de passe temporaire: {}. Changez-le à la prochaine connexion.", firstName, tempPassword);
    }

    public void sendSlaBreachEmail(String to, String reportTitle) {
        log.info("Sending SLA breach email to {} for report: {}", to, reportTitle);
        log.info("Subject: SmartCity - Dépassement SLA");
        log.info("Body: Le signalement '{}' a dépassé le délai SLA. Veuillez intervenir en priorité.", reportTitle);
    }
}
