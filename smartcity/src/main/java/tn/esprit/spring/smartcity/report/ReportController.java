package tn.esprit.spring.smartcity.report;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.spring.smartcity.auth.User;
import tn.esprit.spring.smartcity.auth.UserRepository;
import tn.esprit.spring.smartcity.agent.DepartmentManager;
import tn.esprit.spring.smartcity.agent.DepartmentManagerRepository;
import tn.esprit.spring.smartcity.department.Department;
import tn.esprit.spring.smartcity.department.DepartmentRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;
    private final PhotoUploadService photoUploadService;
    private final DepartmentManagerRepository managerRepository;
    private final DepartmentRepository departmentRepository;

    private User getCurrentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<ReportDto>> getAllReports() {
        return ResponseEntity.ok(reportService.toDtoList(reportService.getAllReports()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportDto> getReport(@PathVariable Long id, Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(reportService.toDtoWithSocial(reportService.getReportById(id), user.getId()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReportDto>> getMyReports(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(reportService.toDtoList(reportService.getReportsByCitizenUserId(user.getId())));
    }

    @PostMapping
    public ResponseEntity<ReportDto> createReport(@RequestBody ReportDto dto, Authentication auth) {
        User user = getCurrentUser(auth);
        Report report = reportService.createReport(dto, user.getId());
        return ResponseEntity.ok(reportService.toDto(report));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReportDto> updateReport(@PathVariable Long id, @RequestBody ReportDto dto) {
        return ResponseEntity.ok(reportService.toDto(reportService.updateReport(id, dto)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReportDto> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(reportService.toDto(reportService.updateStatus(id, status)));
    }

    @PutMapping("/{id}/treatment-status")
    public ResponseEntity<ReportDto> updateTreatmentStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(reportService.toDto(reportService.updateTreatmentStatus(id, TreatmentStatus.valueOf(status))));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ReportDto> assignAgent(@PathVariable Long id, @RequestParam Long agentId) {
        return ResponseEntity.ok(reportService.toDto(reportService.assignAgent(id, agentId)));
    }

    @PutMapping("/{id}/intervene")
    public ResponseEntity<ReportDto> agentIntervention(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        User agent = getCurrentUser(auth);
        String comment = (String) body.get("comment");
        String photoBefore = (String) body.get("photoBefore");
        String photoAfter = (String) body.get("photoAfter");
        return ResponseEntity.ok(reportService.toDto(
                reportService.agentIntervention(id, agent.getId(), comment, photoBefore, photoAfter)));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ReportDto> resolveReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.toDto(reportService.resolveReport(id)));
    }

    @PutMapping("/{id}/validate")
    public ResponseEntity<ReportDto> validateReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.toDto(reportService.validateReport(id)));
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<Map<String, Object>> toggleVote(
            @PathVariable Long id,
            @RequestParam VoteType type,
            Authentication auth) {
        User user = getCurrentUser(auth);
        boolean added = reportService.toggleVote(id, user.getId(), type);
        Report report = reportService.getReportById(id);
        java.util.LinkedHashMap<String, Object> voteResult = new java.util.LinkedHashMap<>();
        voteResult.put("added", added);
        voteResult.put("likeCount", report.getLikeCount());
        voteResult.put("confirmCount", report.getConfirmCount());
        return ResponseEntity.ok(voteResult);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ReportDto.CommentDto> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(reportService.addComment(id, user.getId(), body.get("content")));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<ReportComment>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getComments(id));
    }

    @GetMapping("/by-agent")
    public ResponseEntity<List<ReportDto>> getByAgent(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(reportService.toDtoList(reportService.getReportsByAssignedTo(user.getId())));
    }

    @GetMapping("/by-department")
    public ResponseEntity<List<ReportDto>> getByDepartment(Authentication auth) {
        User user = getCurrentUser(auth);
        DepartmentManager manager = managerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Non manager"));
        Long categoryId = manager.getDepartment().getCategory().getId();
        return ResponseEntity.ok(reportService.toDtoList(reportService.getReportsByCategoryId(categoryId)));
    }

    @GetMapping("/by-treatment-status")
    public ResponseEntity<List<ReportDto>> getByTreatmentStatus(@RequestParam String status) {
        return ResponseEntity.ok(reportService.toDtoList(reportService.getReportsByTreatmentStatus(TreatmentStatus.valueOf(status))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<ReportDto> addPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String filename = photoUploadService.uploadPhoto(file);
        return ResponseEntity.ok(reportService.toDto(reportService.addPhoto(id, filename)));
    }

    @PutMapping("/{id}/proof")
    public ResponseEntity<ReportDto> setProofPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String filename = photoUploadService.uploadPhoto(file);
        return ResponseEntity.ok(reportService.toDto(reportService.setProofPhoto(id, filename)));
    }

    @PutMapping("/{id}/video")
    public ResponseEntity<ReportDto> addVideo(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String filename = photoUploadService.uploadVideo(file);
        return ResponseEntity.ok(reportService.toDto(reportService.addVideo(id, filename)));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<ReportDto>> getNearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "5") Double radius) {
        return ResponseEntity.ok(reportService.toDtoList(reportService.getNearbyReports(lat, lng, radius)));
    }
}
