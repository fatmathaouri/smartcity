package tn.esprit.spring.smartcity.report;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportCommentRepository extends JpaRepository<ReportComment, Long> {
    List<ReportComment> findByReportIdOrderByCreatedAtDesc(Long reportId);
    long countByReportId(Long reportId);
}
