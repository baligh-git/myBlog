package it.course.myblogc3.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Tuple;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.course.myblogc3.entity.Post;
import it.course.myblogc3.entity.Tag;
import it.course.myblogc3.payload.response.PostDetailResponse;
import it.course.myblogc3.payload.response.PostResponse;
import it.course.myblogc3.payload.response.PostResponseForSearch;
import it.course.myblogc3.payload.response.PostResponseWithPreferredTags;

public interface PostRepository extends JpaRepository<Post, Long>{
	
	boolean existsByIdAndVisibleTrue(long id);
	boolean existsByTitle(String title);
	boolean existsByTitleStartsWith(String title);
	boolean existsByTitleLike(String title);
	
	Optional<Post> findByIdAndVisibleTrue(long id);
	
	/***** *****/
	@Transactional
	@Modifying
	@Query(value="UPDATE post SET is_visible = NOT is_visible "
			+ "WHERE id=:postId ", nativeQuery=true)
	void updateStatusPost(@Param("postId") long postId);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE Post p SET p.visible = CASE  WHEN p.visible = 1 THEN  0 ELSE  1  END	WHERE  p.id =:postId" ) 
	void updateStatusPostJpql(@Param("postId")Long postId);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE post p SET p.is_visible = IF(p.is_visible = 0, 1, 0) WHERE id = :postId ", nativeQuery = true)
	void changeStatusPost(@Param("postId") long postId);
	/***** *****/
	
	@Query(value="SELECT new it.course.myblogc3.payload.response.PostResponse("
		+ "p.id, "
		+ "p.title, "
		+ "p.author.id, "
		+ "p.author.username, "
		+ "p.updatedAt, size(p.comments)"
		+ ") "
		+ "FROM Post p "
		+ "WHERE p.visible=true ORDER BY p.updatedAt DESC"
		)
	List<PostResponse> getPostsVisible();
	
	@Query(value="SELECT new it.course.myblogc3.payload.response.PostResponse("
		+ "p.id, "
		+ "p.title, "
		+ "p.author.id, "
		+ "p.author.username, "
		+ "p.updatedAt, "
		+ "size(p.comments)) "
		+ "FROM Post p "
		+ "WHERE p.visible=true AND p.language.langCode=:langCode AND p.language.visible=true GROUP BY p.id ORDER BY p.updatedAt DESC"
		)
	List<PostResponse> getPostsByLanguage(@Param("langCode") String langCode);
/*
	@Query(value="SELECT new it.course.myblogc3.payload.response.PostDetailResponse("
		+ "p.id, "
		+ "p.title, "
		+ "p.content, "
		+ "p.author.id, "
		+ "p.author.username, "
		+ "p.updatedAt  "
		+ ") "
		+ "FROM Post p "
		+ "WHERE p.visible=true AND p.id=:postId"
		)
	PostDetailResponse getPostDetail(@Param("postId") long postId);
*/	
	@Query(value="SELECT new it.course.myblogc3.payload.response.PostResponse("
		+ "p.id, "
		+ "p.title, "
		+ "p.author.id, "
		+ "p.author.username, "
		+ "p.updatedAt, size(p.comments) "		
		+ ") "
		+ "FROM Post p "
		+ "LEFT JOIN p.tag ts "
		+ "WHERE p.visible = true AND ts.visible = true "
		+ "AND ts.tagName IN (:tags) "
		+ "GROUP BY p.id ORDER BY p.title")
	List<PostResponse> getPostsVisibleByTagNames(@Param("tags") Set<String> tags);
	
	@Transactional
	@Modifying
	@Query(value="DELETE FROM Post p "
			+ "WHERE p.id=:postId")
	void deletePost(@Param("postId") long postId);
	
	@Transactional
	@Modifying
	@Query(value="DELETE FROM Post p "
			+ "WHERE p.title LIKE CONCAT('%',:title,'%') ")
	void deletePostByTitle(@Param("title") String title);
	
	@Query(value="SELECT post FROM Post post "
			+ "LEFT JOIN FETCH post.author "
			+ "LEFT JOIN FETCH post.language "
			+ "LEFT JOIN FETCH post.tag "
			+ "LEFT JOIN FETCH post.comments "
			+ "WHERE post.id=:postId GROUP BY post.id")
	Optional<Post> findPostById(@Param("postId") long postId);
	
	@Query(value="SELECT "
			+ "p.id "
			+ "FROM Post p "
			+ "WHERE p.visible=true AND p.id=:postId")
	Long getPostId(@Param("postId")long postId);
	
	@Query(value="SELECT new it.course.myblogc3.payload.response.PostDetailResponse("
			+ "p.id, "
			+ "p.title, "
			+ "p.content, "
			+ "p.author.id, "
			+ "p.author.username, "
			+ "p.updatedAt, "
			+ "COALESCE(AVG(v.vote), 0.00) "//AVERAGE
			+ ") "
			+ "FROM Post p "
			+ "LEFT JOIN Voting v "
			+ "ON p.id = v.votingId.post.id "
			+ "WHERE p.visible=true AND p.id=:postId AND p.cost=0"
			)
	PostDetailResponse getPostDetailPublic(@Param("postId") long postId);
	
	@Query(value="SELECT new it.course.myblogc3.payload.response.PostDetailResponse("
			+ "p.id, "
			+ "p.title, "
			+ "p.content, "
			+ "p.author.id, "
			+ "p.author.username, "
			+ "p.updatedAt, "
			+ "COALESCE(AVG(v.vote), 0.00) "//AVERAGE
			+ ") "
			+ "FROM Post p "
			+ "LEFT JOIN Voting v ON p.id = v.votingId.post.id "
			+ "LEFT JOIN PurchasedPost pp ON pp.purchasedPostId.post.id = :postId AND pp.purchasedPostId.user.id = :userId "
			+ "WHERE p.visible=true AND p.id=:postId"
			)
	PostDetailResponse getPostDetailPrivate(@Param("postId") long postId, @Param("userId") long userId);


	
	@Query(value="SELECT p.* FROM post p "
			+ " WHERE p.is_visible=true "
			+ " AND   REGEXP_LIKE(p.title, :wordToFind ) OR REGEXP_LIKE(p.content,:wordToFind )",nativeQuery = true)
	List<Post> getPostsVisibleBySearchCaseSensitiveFalse(String wordToFind);
	
	
	@Query(value="SELECT p.* FROM post p "
			+ " WHERE p.is_visible=true "
			+ " AND   REGEXP_LIKE(p.title, BINARY :wordToFind ) OR REGEXP_LIKE(p.content, BINARY :wordToFind )",nativeQuery = true)
	List<Post> getPostsVisibleBySearchCaseSensitiveTrue(String wordToFind);
	
	@Query(value="SELECT new it.course.myblogc3.payload.response.PostResponseForSearch("
			+ "p.id, "
			+ "p.title, "
			+ "p.content, "
			+ "p.author.username, "
			+ "p.updatedAt"		
			+ ") "
			+ "FROM Post p "
			+ "WHERE p.visible = true"
			)
	List<PostResponseForSearch> getPostsVisibleForSearch();
	
	@Query(value="SELECT p1 FROM Post p1 "
			+ "JOIN FETCH p1.tag "
			+ "JOIN FETCH p1.author u "
			+ "WHERE p1 IN (SELECT p from Post p "
			+ "JOIN p.tag pt "
			+ "JOIN User u on u.id= :userId "
			+ "JOIN u.preferredTags upt ON upt.tagName=pt.tagName "
			+ "WHERE p.visible = true "
			+ "AND pt.visible=true)"
			)
	Set<Post> getPostsVisibleByPreferredTags(@Param("userId") long userId);

	
}
