package it.course.myblogc3.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.course.myblogc3.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, String>{
	
	@Query(value="SELECT p.tag FROM Post p WHERE p.id=:postId")
	Set<Tag> getTagsInPost(@Param("postId") long postId);
	
	Set<Tag> findByTagNameInAndVisibleTrue(Set<String> tagsNames);
	
	Optional<Tag> findByTagNameAndVisibleTrue(String tagname);
	
	List<Tag> findByVisibleTrue();
	/*
	@Query(value="SELECT upt.tag_id FROM user_preferred_tags upt "
			+ "INNER JOIN tag t ON t.tag_name=upt.tag_id "
			+ "WHERE t.is_visible=true "
			+ "AND upt.user_id= :userId", nativeQuery=true)
	Set<Tag> getTagNamesByUser(@Param("userId") long userId);
	
	@Query(value="SELECT u.preferredTags FROM User u "
			+ "JOIN FETCH Tag t ON t.visible=true "
			+ "WHERE u.id=:userId")
	Set<Tag> getTagNamesByUser(@Param("userId") long userId);
	*/
}
