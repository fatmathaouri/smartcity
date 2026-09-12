package tn.esprit.spring.smartcity.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;
import tn.esprit.spring.smartcity.citizen.Citizen;
import tn.esprit.spring.smartcity.report.Report;
import tn.esprit.spring.smartcity.department.Department;
import tn.esprit.spring.smartcity.department.DepartmentRepository;
import tn.esprit.spring.smartcity.agent.DepartmentManager;
import tn.esprit.spring.smartcity.agent.DepartmentManagerRepository;
import tn.esprit.spring.smartcity.agent.MunicipalAgent;
import tn.esprit.spring.smartcity.agent.MunicipalAgentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentManagerRepository managerRepository;
    private final MunicipalAgentRepository agentRepository;
    private final UserRepository userRepository;

    public List<Notification> getNotificationsByCitizenId(Long citizenId) {
        return notificationRepository.findByCitizenIdOrderByCreatedAtDesc(citizenId);
    }

    public List<Notification> getUnreadNotifications(Long citizenId) {
        return notificationRepository.findByCitizenIdAndReadFalse(citizenId);
    }

    public long getUnreadCount(Long citizenId) {
        return notificationRepository.countByCitizenIdAndReadFalse(citizenId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long citizenId) {
        List<Notification> unread = notificationRepository.findByCitizenIdAndReadFalse(citizenId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void markAllAsReadForUser(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public Notification createNotification(Citizen citizen, Report report, String message) {
        Notification notification = Notification.builder()
                .citizen(citizen)
                .report(report)
                .message(message)
                .type(NotificationType.INFO)
                .build();
        return notificationRepository.save(notification);
    }

    public Notification createTypedNotification(Citizen citizen, Report report, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .citizen(citizen)
                .report(report)
                .message(message)
                .type(type)
                .build();
        return notificationRepository.save(notification);
    }

    public Notification createUserNotification(User user, Report report, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .report(report)
                .message(message)
                .type(NotificationType.INFO)
                .build();
        return notificationRepository.save(notification);
    }

    public Notification createUserTypedNotification(User user, Report report, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .report(report)
                .message(message)
                .type(type)
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId);
    }

    public long getUnreadCountByUserId(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    // ========== CITIZEN NOTIFICATIONS ==========

    public void notifyReportCreated(Citizen citizen, Report report) {
        createTypedNotification(citizen, report,
                "Votre signalement \"" + report.getTitle() + "\" a été créé avec succès",
                NotificationType.REPORT_CREATED);
    }

    public void notifyStatusChanged(Citizen citizen, Report report, String newStatus) {
        createTypedNotification(citizen, report,
                "Le statut de votre signalement \"" + report.getTitle() + "\" a changé : " + newStatus,
                NotificationType.STATUS_CHANGED);
    }

    public void notifyVoteReceived(Citizen citizen, Report report, String voterName) {
        createTypedNotification(citizen, report,
                voterName + " a liké votre signalement \"" + report.getTitle() + "\"",
                NotificationType.VOTE_RECEIVED);
    }

    public void notifyCommentAdded(Citizen citizen, Report report, String commenterName) {
        createTypedNotification(citizen, report,
                commenterName + " a commenté votre signalement \"" + report.getTitle() + "\"",
                NotificationType.COMMENT_ADDED);
    }

    public void notifyBadgeEarned(Citizen citizen, String badgeName) {
        createTypedNotification(citizen, null,
                "Félicitations ! Vous avez débloqué le badge \"" + badgeName + "\"",
                NotificationType.BADGE_EARNED);
    }

    public void notifyPlanning(Citizen citizen, Report report, String message) {
        createTypedNotification(citizen, report, message, NotificationType.PLANNING);
    }

    public void notifyAgentAssigned(Citizen citizen, Report report, String agentName) {
        createTypedNotification(citizen, report,
                "L'agent " + agentName + " a été assigné à votre signalement \"" + report.getTitle() + "\"",
                NotificationType.STATUS_CHANGED);
    }

    public void notifySlaWarning(Citizen citizen, Report report, long hoursRemaining) {
        createTypedNotification(citizen, report,
                "Attention : il ne reste que " + hoursRemaining + "h pour résoudre \"" + report.getTitle() + "\" avant la deadline SLA",
                NotificationType.SLA_WARNING);
    }

    public void notifySlaBreach(Citizen citizen, Report report, long hoursLate) {
        createTypedNotification(citizen, report,
                "Alerte SLA : le signalement \"" + report.getTitle() + "\" a dépassé le délai de " + hoursLate + "h",
                NotificationType.SLA_BREACH);
    }

    // ========== DEPARTMENT NOTIFICATIONS (Manager + Agents) ==========

    @Transactional
    public void notifyReportToDepartment(Report report) {
        if (report.getCategory() == null) return;
        List<Department> departments = departmentRepository.findByCategory_Id(report.getCategory().getId());
        if (departments.isEmpty()) return;
        Department dept = departments.get(0);

        String message = "Nouveau signalement dans votre département : \"" + report.getTitle() + "\"";

        managerRepository.findByDepartmentId(dept.getId()).ifPresent(manager ->
                createUserTypedNotification(manager.getUser(), report, message, NotificationType.REPORT_CREATED));

        List<MunicipalAgent> agents = agentRepository.findByDepartmentId(dept.getId());
        for (MunicipalAgent agent : agents) {
            createUserTypedNotification(agent.getUser(), report, message, NotificationType.REPORT_CREATED);
        }
    }

    @Transactional
    public void notifyDepartmentStatusChanged(Report report, String status) {
        if (report.getCategory() == null) return;
        List<Department> departments = departmentRepository.findByCategory_Id(report.getCategory().getId());
        if (departments.isEmpty()) return;
        Department dept = departments.get(0);

        String message = "Le signalement \"" + report.getTitle() + "\" a changé de statut : " + status;

        managerRepository.findByDepartmentId(dept.getId()).ifPresent(manager ->
                createUserTypedNotification(manager.getUser(), report, message, NotificationType.STATUS_CHANGED));

        List<MunicipalAgent> agents = agentRepository.findByDepartmentId(dept.getId());
        for (MunicipalAgent agent : agents) {
            createUserTypedNotification(agent.getUser(), report, message, NotificationType.STATUS_CHANGED);
        }
    }

    @Transactional
    public void notifyAssignedAgent(User agent, Report report, String message) {
        createUserTypedNotification(agent, report, message, NotificationType.STATUS_CHANGED);
    }

    @Transactional
    public void notifyManagerEvent(Report report, String eventMessage) {
        if (report.getCategory() == null) return;
        List<Department> departments = departmentRepository.findByCategory_Id(report.getCategory().getId());
        if (departments.isEmpty()) return;
        Department dept = departments.get(0);

        managerRepository.findByDepartmentId(dept.getId()).ifPresent(manager ->
                createUserTypedNotification(manager.getUser(), report, eventMessage, NotificationType.INFO));
    }

    @Transactional
    public void notifyAssignedAgentEvent(Report report, String eventMessage) {
        if (report.getAssignedTo() == null) return;
        createUserTypedNotification(report.getAssignedTo(), report, eventMessage, NotificationType.INFO);
    }

    // ========== ADMIN / MUNICIPALITY NOTIFICATIONS ==========

    @Transactional
    public void notifyAdmins(Report report) {
        List<User> admins = userRepository.findByRoleName("ROLE_ADMIN");
        String message = "Nouveau signalement signalé : \"" + report.getTitle() + "\"";
        for (User admin : admins) {
            createUserTypedNotification(admin, report, message, NotificationType.REPORT_CREATED);
        }
    }

    @Transactional
    public void notifyMunicipality(Report report) {
        List<User> municipalityUsers = userRepository.findByRoleName("ROLE_MUNICIPALITY");
        String message = "Nouveau signalement dans la ville : \"" + report.getTitle() + "\"";
        for (User muni : municipalityUsers) {
            createUserTypedNotification(muni, report, message, NotificationType.REPORT_CREATED);
        }
    }

    @Transactional
    public void notifyAdminsAndMunicipality(Report report, String eventMessage) {
        List<User> admins = userRepository.findByRoleName("ROLE_ADMIN");
        for (User admin : admins) {
            createUserTypedNotification(admin, report, eventMessage, NotificationType.STATUS_CHANGED);
        }
        List<User> municipalityUsers = userRepository.findByRoleName("ROLE_MUNICIPALITY");
        for (User muni : municipalityUsers) {
            createUserTypedNotification(muni, report, eventMessage, NotificationType.STATUS_CHANGED);
        }
    }
}
