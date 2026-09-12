package tn.esprit.spring.smartcity.report;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReportVoteRepository extends JpaRepository<ReportVote, Long> {
    Optional<ReportVote> findByReportIdAndUserIdAndVoteType(Long reportId, Long userId, VoteType voteType);
    long countByReportIdAndVoteType(Long reportId, VoteType voteType);
    void deleteByReportIdAndUserIdAndVoteType(Long reportId, Long userId, VoteType voteType);
}
