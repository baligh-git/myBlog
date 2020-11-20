package it.course.myblogc3.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.course.myblogc3.entity.Advisory;
import it.course.myblogc3.entity.AdvisoryId;
import it.course.myblogc3.entity.AdvisoryReason;
import it.course.myblogc3.entity.Comment;
import it.course.myblogc3.payload.response.AdvisoryResponse;

@Repository
public interface AdvisoryRepository extends JpaRepository<Advisory, AdvisoryId>{
	
	@Query(value="SELECT * FROM advisory WHERE reporter=:reporterId "
			+ "AND comment_id=:commentId AND advisory_reason=:reasonId", nativeQuery=true)
	Advisory getAdvisoryById(@Param("reporterId") long reporterId,@Param("commentId") long commentId, @Param("reasonId") long reasonId);

	
	@Query("SELECT NEW it.course.myblogc3.payload.response.AdvisoryResponse("
		+ "a.createdAt, "
		+ "a.updatedAt, "
		+ "ard.advisorySeverity.severityDescription,"
		+ "ard.advisorySeverity.severityValue, "
		+ "a.advisoryId.advisoryReason.id, "
		+ "a.advisoryId.advisoryReason.advisoryReasonName, "
		+ "a.status, "
		+ "a.advisoryId.reporter.username, "
		+ "a.advisoryId.comment.commentAuthor.username, "
		+ "a.advisoryId.comment.id"
		+ ")"
		+ "FROM Advisory a "
		+ "LEFT JOIN AdvisoryReasonDetail ard "
		+ "ON a.advisoryId.advisoryReason.id = ard.advisoryReasonDetailId.advisoryReason.id "
		+ "WHERE a.status IN (0,1) "
		+ "AND a.createdAt BETWEEN ard.advisoryReasonDetailId.startDate AND ard.endDate "
		+ "ORDER BY a.status ASC")
	List<AdvisoryResponse> getOpenAdvisories();
	
	boolean existsByAdvisoryIdCommentAndAdvisoryIdAdvisoryReason(Comment c, AdvisoryReason ar);
	
	
}
