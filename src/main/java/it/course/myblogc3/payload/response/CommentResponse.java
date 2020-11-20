package it.course.myblogc3.payload.response;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class CommentResponse {
	
	private Long id;
	private String comment;
	private Date createdAt;
	private Boolean visible;
	private String commentAuthor;
	private String postTitle;
	
}
