package it.course.myblogc3.payload.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;


@Getter
public class AdvisoryRequest {
	
	@NotNull
	private long commentId;
	
	@NotBlank @NotEmpty
	@Size(max=15)
	private String reason;
	
	@Size(max=100)
	private String description;

}
