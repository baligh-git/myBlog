package it.course.myblogc3.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.course.myblogc3.entity.Voting;
import it.course.myblogc3.entity.VotingId;


@Repository
public interface VotingRepository extends JpaRepository<Voting, VotingId>{
	
	
	@Query(value="SELECT AVG(vote) FROM voting WHERE post_id=:id", nativeQuery=true)
	Double postAvg(@Param("id") long id);
	
	@Transactional
	@Modifying
	@Query(value="INSERT INTO voting "
			+ "(created_at, vote, voter, post_id) "
			+ "VALUES (CURRENT_TIMESTAMP, :vote, :voter, :postId)", nativeQuery=true
			)
	void insertVote(@Param("vote") int vote, @Param("voter") long voter, @Param("postId") long postId);
	
	@Transactional
	@Modifying
	@Query(value="UPDATE voting "
			+ "SET vote=:vote "
			+ "WHERE voter=:voter AND post_id=:postId ", nativeQuery=true
			)
	void updateVote(@Param("vote") int vote, @Param("voter") long voter, @Param("postId") long postId);
	
	/*
	 SELECT p.id, p.title, COUNT(v.vote) AS countVote, COALESCE(AVG(v.vote), 0.00) AS avgVote
	FROM voting v
	RIGHT OUTER JOIN  post p ON v.post_id=p.id
	WHERE p.is_visible=1 
	GROUP BY p.title 
	ORDER BY avgVote DESC;
	
	@Query(value="select new it.course.myblog.payload.response.PostResponseWithVoting"
			+ "("
			+ "p.id, "
			+ "p.title, "
			+ "p.content, "
			+ "p.language.langCode, "
			+ "p.author.username, "
			+ "p.dbfile.fileName, "
			+ "p.updatedAt, "
			+ "COUNT(v.vote), "
			+ "COALESCE(AVG(v.vote), 0.00) AS average"
			+ ")"
			+ "FROM Voting v "
			+ "RIGHT OUTER JOIN "
			+ "Post p "
			+ "ON p.id = v.votingId.post.id "
			+ "WHERE p.visible=true "
			+ "GROUP BY p.title "
			+ "ORDER BY average desc "
		)
	List<PostResponseWithVoting> findPostCountAndAvg();
	
	@Query(value="select new it.course.myblog.payload.response.PostResponseWithVoting"
			+ "("
			+ "p.id, "
			+ "p.title, "
			+ "p.content, "
			+ "p.language.langCode, "
			+ "p.author.username, "
			+ "p.dbfile.fileName, "
			+ "p.updatedAt, "
			+ "COUNT(v.vote), "
			+ "COALESCE(AVG(v.vote), 0.00) AS average"
			+ ")"
			+ "FROM Voting v "
			+ "RIGHT OUTER JOIN "
			+ "Post p "
			+ "ON p.id = v.votingId.post.id "
			+ "WHERE p.visible=true "
			+ "AND p.author.id=:authorId "
			+ "GROUP BY p.title "
			+ "ORDER BY average desc "
		)
	List<PostResponseWithVoting> findPostCountAndAvgByAuthor(@Param("authorId") Long authorId);
	
	@Query(value="select new it.course.myblog.payload.response.AuthorResponse"
		+ "("
		+ "p.author.id, "
		+ "p.author.username, "
		+ "p.author.email, "
		+ "COUNT(v.vote), "
		+ "COALESCE(AVG(v.vote), 0.00) AS average"
		+ ")"
		+ "FROM Voting v "
		+ "RIGHT OUTER JOIN "
		+ "Post p "
		+ "ON p.id = v.votingId.post.id "
		+ "WHERE p.visible=true AND p.author.id=:authorId"
		)
	AuthorResponse findAuthorCountAndAvg(@Param("authorId") Long authorId);
	*/
	
	

}
