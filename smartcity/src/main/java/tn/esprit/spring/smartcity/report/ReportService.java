package tn.esprit.spring.smartcity.report;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.smartcity.ai.ClassificationService;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;
import tn.esprit.spring.smartcity.category.Category;
import tn.esprit.spring.smartcity.category.CategoryRepository;
import tn.esprit.spring.smartcity.citizen.Citizen;
import tn.esprit.spring.smartcity.citizen.CitizenRepository;

import tn.esprit.spring.smartcity.gamification.GamificationService;
import tn.esprit.spring.smartcity.notification.NotificationService;
import tn.esprit.spring.smartcity.sla.SlaService;

import tn.esprit.spring.smartcity.department.Department;
import tn.esprit.spring.smartcity.department.DepartmentRepository;
import tn.esprit.spring.smartcity.agent.DepartmentManager;
import tn.esprit.spring.smartcity.agent.DepartmentManagerRepository;
import tn.esprit.spring.smartcity.agent.MunicipalAgent;
import tn.esprit.spring.smartcity.agent.MunicipalAgentRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final CitizenRepository citizenRepository;
    private final CategoryRepository categoryRepository;
    private final ClassificationService classificationService;
    private final ReportVoteRepository reportVoteRepository;
    private final ReportCommentRepository reportCommentRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;
    private final SlaService slaService;
    private final NotificationService notificationService;
    private final DepartmentRepository departmentRepository;
    private final DepartmentManagerRepository managerRepository;
    private final MunicipalAgentRepository agentRepository;

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
    }

    public List<Report> getReportsByCitizenId(Long citizenId) {
        return reportRepository.findByCitizenId(citizenId);
    }

    public List<Report> getReportsByCitizenUserId(Long userId) {
        return reportRepository.findByCitizenUserId(userId);
    }

    public List<Report> getReportsByAssignedTo(Long userId) {
        return reportRepository.findByAssignedToId(userId);
    }

    public List<Report> getReportsByAssignedToAndTreatmentStatus(Long userId, TreatmentStatus ts) {
        return reportRepository.findByAssignedToIdAndTreatmentStatus(userId, ts);
    }

    public List<Report> getReportsByTreatmentStatus(TreatmentStatus ts) {
        return reportRepository.findByTreatmentStatus(ts);
    }

    public List<Report> getReportsByCategoryId(Long categoryId) {
        return reportRepository.findByCategoryId(categoryId);
    }

    public List<Report> getUnassignedReportsByCategoryId(Long categoryId) {
        return reportRepository.findByCategoryIdAndAssignedToIsNullAndTreatmentStatus(categoryId, TreatmentStatus.NEW);
    }

    @Transactional
    public Report createReport(ReportDto dto, Long userId) {
        Citizen citizen = citizenRepository.findFirstByUserIdOrderByIdDesc(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow();
                    Citizen newCitizen = new Citizen();
                    newCitizen.setUser(user);
                    return citizenRepository.save(newCitizen);
                });

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
        }

        Report report = Report.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .address(dto.getAddress())
                .photoUrl(dto.getPhotoUrl())
                .citizen(citizen)
                .category(category)
                .build();

        classificationService.classify(report);

        if (category != null && category.getSlaHours() != null) {
            report.setSlaDeadline(LocalDateTime.now().plusHours(category.getSlaHours()));
        }

        Report saved = reportRepository.save(report);
        gamificationService.addPointsForReport(userId);
        notificationService.notifyReportCreated(citizen, saved);
        notificationService.notifyReportToDepartment(saved);
        notificationService.notifyAdmins(saved);
        notificationService.notifyMunicipality(saved);
        return saved;
    }

    @Transactional
    public Report updateReport(Long id, ReportDto dto) {
        Report report = getReportById(id);

        if (dto.getTitle() != null) report.setTitle(dto.getTitle());
        if (dto.getDescription() != null) report.setDescription(dto.getDescription());
        if (dto.getLatitude() != null) report.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) report.setLongitude(dto.getLongitude());
        if (dto.getAddress() != null) report.setAddress(dto.getAddress());
        if (dto.getPhotoUrl() != null) report.setPhotoUrl(dto.getPhotoUrl());
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId()).orElse(null);
            report.setCategory(category);
        }
        if (dto.getPriority() != null) {
            report.setPriority(Priority.valueOf(dto.getPriority()));
        }

        report.setUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Transactional
    public Report updateStatus(Long id, String status) {
        Report report = getReportById(id);
        report.setStatus(ReportStatus.valueOf(status));
        report.setUpdatedAt(LocalDateTime.now());
        Report saved = reportRepository.save(report);
        notificationService.notifyStatusChanged(report.getCitizen(), saved, status);
        notificationService.notifyDepartmentStatusChanged(saved, status);
        notificationService.notifyAdminsAndMunicipality(saved, "Statut du signalement \"" + saved.getTitle() + "\" changé : " + status);
        return saved;
    }

    @Transactional
    public Report updateTreatmentStatus(Long id, TreatmentStatus ts) {
        Report report = getReportById(id);
        report.setTreatmentStatus(ts);
        report.setUpdatedAt(LocalDateTime.now());
        Report saved = reportRepository.save(report);
        notificationService.notifyStatusChanged(report.getCitizen(), saved, ts.name());
        notificationService.notifyDepartmentStatusChanged(saved, ts.name());
        notificationService.notifyAdminsAndMunicipality(saved, "Traitement du signalement \"" + saved.getTitle() + "\" changé : " + ts.name());
        return saved;
    }

    @Transactional
    public Report assignAgent(Long reportId, Long agentUserId) {
        Report report = getReportById(reportId);
        User agent = userRepository.findById(agentUserId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));
        report.setAssignedTo(agent);
        report.setTreatmentStatus(TreatmentStatus.ACCEPTED);
        report.setUpdatedAt(LocalDateTime.now());
        Report saved = reportRepository.save(report);
        String agentName = agent.getFirstName() + " " + agent.getLastName();
        notificationService.notifyAgentAssigned(report.getCitizen(), saved, agentName);
        notificationService.notifyAssignedAgent(agent, saved,
                "Vous avez été assigné au signalement \"" + saved.getTitle() + "\"");
        notificationService.notifyManagerEvent(saved,
                "L'agent " + agentName + " a été assigné au signalement \"" + saved.getTitle() + "\"");
        return saved;
    }

    @Transactional
    public Report agentIntervention(Long reportId, Long agentUserId, String comment,
                                     String photoBefore, String photoAfter) {
        Report report = getReportById(reportId);
        if (report.getAssignedTo() == null || !report.getAssignedTo().getId().equals(agentUserId)) {
            throw new RuntimeException("You are not assigned to this report");
        }
        if (comment != null) report.setAgentComment(comment);
        if (photoBefore != null) report.setPhotoBefore(photoBefore);
        if (photoAfter != null) report.setPhotoAfter(photoAfter);
        report.setInterventionDate(LocalDateTime.now());
        report.setTreatmentStatus(TreatmentStatus.IN_PROGRESS);
        report.setUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Transactional
    public Report resolveReport(Long reportId) {
        Report report = getReportById(reportId);
        report.setTreatmentStatus(TreatmentStatus.RESOLVED);
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        Report saved = reportRepository.save(report);
        notificationService.notifyStatusChanged(report.getCitizen(), saved, "Résolu");
        notificationService.notifyDepartmentStatusChanged(saved, "Résolu");
        notificationService.notifyAssignedAgentEvent(saved, "Le signalement \"" + saved.getTitle() + "\" a été résolu");
        notificationService.notifyAdminsAndMunicipality(saved, "Le signalement \"" + saved.getTitle() + "\" a été résolu");
        return saved;
    }

    @Transactional
    public Report validateReport(Long reportId) {
        Report report = getReportById(reportId);
        report.setTreatmentStatus(TreatmentStatus.VALIDATED);
        report.setUpdatedAt(LocalDateTime.now());
        Report saved = reportRepository.save(report);
        notificationService.notifyStatusChanged(report.getCitizen(), saved, "Validé");
        notificationService.notifyDepartmentStatusChanged(saved, "Validé");
        notificationService.notifyAdminsAndMunicipality(saved, "Le signalement \"" + saved.getTitle() + "\" a été validé");
        return saved;
    }

    @Transactional
    public ReportDto.CommentDto addComment(Long reportId, Long userId, String content) {
        Report report = getReportById(reportId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ReportComment comment = ReportComment.builder()
                .report(report)
                .user(user)
                .content(content)
                .build();
        reportCommentRepository.save(comment);

        report.setCommentCount(report.getCommentCount() + 1);
        reportRepository.save(report);
        gamificationService.addPointsForComment(userId);

        if (report.getCitizen() != null && !report.getCitizen().getUser().getId().equals(userId)) {
            String commenterName = user.getFirstName() + " " + user.getLastName();
            notificationService.notifyCommentAdded(report.getCitizen(), report, commenterName);
        }

        ReportDto.CommentDto dto = new ReportDto.CommentDto();
        dto.setId(comment.getId());
        dto.setUserId(user.getId());
        dto.setUserName(user.getFirstName() + " " + user.getLastName());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }

    @Transactional
    public boolean toggleVote(Long reportId, Long userId, VoteType voteType) {
        Report report = getReportById(reportId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var existing = reportVoteRepository.findByReportIdAndUserIdAndVoteType(reportId, userId, voteType);
        if (existing.isPresent()) {
            reportVoteRepository.delete(existing.get());
            if (voteType == VoteType.LIKE) report.setLikeCount(Math.max(0, report.getLikeCount() - 1));
            else report.setConfirmCount(Math.max(0, report.getConfirmCount() - 1));
            recalculatePriority(report);
            reportRepository.save(report);
            return false;
        } else {
            ReportVote vote = ReportVote.builder()
                    .report(report)
                    .user(user)
                    .voteType(voteType)
                    .build();
            reportVoteRepository.save(vote);
            if (voteType == VoteType.LIKE) report.setLikeCount(report.getLikeCount() + 1);
            else {
                report.setConfirmCount(report.getConfirmCount() + 1);
                gamificationService.addPointsForConfirmation(userId);
            }
            recalculatePriority(report);
            reportRepository.save(report);

            if (report.getCitizen() != null && !report.getCitizen().getUser().getId().equals(userId)) {
                String voterName = user.getFirstName() + " " + user.getLastName();
                notificationService.notifyVoteReceived(report.getCitizen(), report, voterName);
            }
            return true;
        }
    }

    private void recalculatePriority(Report report) {
        long total = report.getConfirmCount();
        if (total >= 20) {
            report.setPriority(Priority.CRITICAL);
        } else if (total >= 10) {
            report.setPriority(Priority.HIGH);
        } else if (total >= 5) {
            report.setPriority(Priority.MEDIUM);
        } else {
            report.setPriority(Priority.LOW);
        }
    }

    public List<ReportComment> getComments(Long reportId) {
        return reportCommentRepository.findByReportIdOrderByCreatedAtDesc(reportId);
    }

    public long getLikeCount(Long reportId) {
        return reportVoteRepository.countByReportIdAndVoteType(reportId, VoteType.LIKE);
    }

    public long getConfirmCount(Long reportId) {
        return reportVoteRepository.countByReportIdAndVoteType(reportId, VoteType.CONFIRM);
    }

    @Transactional
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }

    @Transactional
    public Report addPhoto(Long reportId, String photoUrl) {
        Report report = getReportById(reportId);
        if (report.getPhotoUrls() == null) report.setPhotoUrls(new ArrayList<>());
        report.getPhotoUrls().add(photoUrl);
        report.setUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Transactional
    public Report setProofPhoto(Long reportId, String proofPhoto) {
        Report report = getReportById(reportId);
        report.setProofPhoto(proofPhoto);
        report.setUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Transactional
    public Report addVideo(Long reportId, String videoUrl) {
        Report report = getReportById(reportId);
        report.setVideoUrl(videoUrl);
        report.setUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    public List<Report> getNearbyReports(Double lat, Double lng, Double radiusKm) {
        double latDelta = radiusKm / 111.0;
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));
        return reportRepository.findByLocationBounds(
                lat - latDelta, lat + latDelta,
                lng - lngDelta, lng + lngDelta);
    }

    public ReportDto toDto(Report report) {
        ReportDto dto = new ReportDto();
        dto.setId(report.getId());
        dto.setTitle(report.getTitle());
        dto.setDescription(report.getDescription());
        dto.setStatus(report.getStatus().name());
        dto.setPriority(report.getPriority() != null ? report.getPriority().name() : null);
        dto.setTreatmentStatus(report.getTreatmentStatus() != null ? report.getTreatmentStatus().name() : null);
        dto.setLatitude(report.getLatitude());
        dto.setLongitude(report.getLongitude());
        dto.setPhotoUrl(report.getPhotoUrl());
        dto.setAddress(report.getAddress());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());
        dto.setCitizenId(report.getCitizen().getId());
        dto.setCitizenName(report.getCitizen().getUser().getFirstName() + " " + report.getCitizen().getUser().getLastName());
        if (report.getCategory() != null) {
            dto.setCategoryId(report.getCategory().getId());
            dto.setCategoryName(report.getCategory().getName());
        }
        if (report.getAssignedTo() != null) {
            dto.setAssignedToId(report.getAssignedTo().getId());
            dto.setAssignedToName(report.getAssignedTo().getFirstName() + " " + report.getAssignedTo().getLastName());
        }
        dto.setPhotoBefore(report.getPhotoBefore());
        dto.setPhotoAfter(report.getPhotoAfter());
        dto.setAgentComment(report.getAgentComment());
        dto.setInterventionDate(report.getInterventionDate());
        dto.setLikeCount(report.getLikeCount());
        dto.setConfirmCount(report.getConfirmCount());
        dto.setCommentCount(report.getCommentCount());
        dto.setSlaDeadline(report.getSlaDeadline());
        dto.setResolvedAt(report.getResolvedAt());
        dto.setSlaBreached(report.getSlaBreached());
        dto.setPhotoUrls(report.getPhotoUrls());
        dto.setVideoUrl(report.getVideoUrl());
        dto.setProofPhoto(report.getProofPhoto());

        if (report.getSlaDeadline() != null) {
            long hoursRemaining = Duration.between(LocalDateTime.now(), report.getSlaDeadline()).toHours();
            dto.setHoursRemaining((double) hoursRemaining);
        }

        return dto;
    }

    public ReportDto toDtoWithSocial(Report report, Long currentUserId) {
        ReportDto dto = toDto(report);
        if (currentUserId != null) {
            dto.setLikedByMe(reportVoteRepository
                    .findByReportIdAndUserIdAndVoteType(report.getId(), currentUserId, VoteType.LIKE).isPresent());
            dto.setConfirmedByMe(reportVoteRepository
                    .findByReportIdAndUserIdAndVoteType(report.getId(), currentUserId, VoteType.CONFIRM).isPresent());
        }
        return dto;
    }

    public List<ReportDto> toDtoList(List<Report> reports) {
        return reports.stream().map(this::toDto).toList();
    }
}
