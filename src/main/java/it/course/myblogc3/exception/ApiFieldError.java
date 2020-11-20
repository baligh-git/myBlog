package it.course.myblogc3.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class ApiFieldError {
	
	private String field;
	private String error;
	private String defaultMessage;
	private Object rejectedValue;
	
	public ApiFieldError(String field, String error, String defaultMessage) {
		super();
		this.field = field;
		this.error = error;
		this.defaultMessage = defaultMessage;
	}
	
}
