package tn.esprit.spring.smartcity.report;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportDto {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String treatmentStatus;
    private Double latitude;
    private Double longitude;
    private String photoUrl;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long citizenId;
    private String citizenName;
    private Long categoryId;
    private String categoryName;
    private Long assignedToId;
    private String assignedToName;
    private String photoBefore;
    private String photoAfter;
    private String agentComment;
    private LocalDateTime interventionDate;
    private Long likeCount;
    private Long confirmCount;
    private Long commentCount;
    private List<CommentDto> comments;
    private Boolean likedByMe;
    private Boolean confirmedByMe;
    private LocalDateTime slaDeadline;
    private LocalDateTime resolvedAt;
    private Boolean slaBreached;
    private Double hoursRemaining;
    private List<String> photoUrls;
    private String videoUrl;
    private String proofPhoto;

    @Data
    public static class CommentDto {
        private Long id;
        private Long userId;
        private String userName;
        private String content;
        private LocalDateTime createdAt;
    }
}
