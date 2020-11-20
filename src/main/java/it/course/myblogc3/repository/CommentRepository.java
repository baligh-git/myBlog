package it.course.myblogc3.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.course.myblogc3.entity.Comment;
import it.course.myblogc3.payload.response.CommentResponse;
import it.course.myblogc3.payload.response.CommentResponseForPostDetail;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>{
	
	Optional<Comment> findByIdAndVisibleTrue(long id);
	
	@Query(value="SELECT new it.course.myblogc3.payload.response.CommentResponse "
			+ "("
			+ "c.id, "
			+ "c.comment, "
			+ "c.createdAt, "
			+ "c.visible, "
			+ "c.commentAuthor.username, "
			+ "c.post.title) "
			+ "FROM Comment c "
			+ "WHERE c.visible=true AND c.id=:commentId")
	CommentResponse commentDetails(@Param("commentId") long commentId);
	
	@Query(value="SELECT new it.course.myblogc3.payload.response.CommentResponseForPostDetail "
			+ "(c.id, "
			+ "c.comment, "
			+ "c.createdAt, "
			+ "c.commentAuthor.username "
			+ ") "
			+ "FROM Comment c "
			+ "WHERE c.visible=true AND c.post.id=:postId")
	List<CommentResponseForPostDetail> commentFromPost(@Param("postId") long postId);
	
	@Transactional
	@Modifying
	@Query(value="DELETE FROM Comment c "
			+ "WHERE c.id=:commentId")
	void deleteComment(@Param("commentId") long commentId);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE Comment c SET c.visible=false "
			+ "WHERE c.id=:commentId")
	void updateVisibilityComment(@Param("commentId") long commentId);

}
