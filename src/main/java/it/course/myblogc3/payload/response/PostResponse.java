package it.course.myblogc3.payload.response;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class PostResponse {
	
	private Long id;
	private String title;
	private Long authorId;
	private String authorName;
	private Date updatedAt;
	private int commentsNr;

}
