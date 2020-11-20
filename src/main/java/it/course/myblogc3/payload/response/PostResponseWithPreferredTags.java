package it.course.myblogc3.payload.response;

import java.util.Date;
import java.util.List;

import it.course.myblogc3.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class PostResponseWithPreferredTags {

	private Long id;
	private String title;
	private Long authorId;
	private String authorName;
	private Date updatedAt;
	private String[] tagNames; // tags del post
	
	public static PostResponseWithPreferredTags createFromEntity(Post p) {
		return new PostResponseWithPreferredTags(
			p.getId(),
			p.getTitle(),
			p.getAuthor().getId(),
			p.getAuthor().getUsername(),
			p.getUpdatedAt(),
			p.getTag().stream().map(t->t.getTagName()).toArray(String[]::new)
			);
		}

	
}