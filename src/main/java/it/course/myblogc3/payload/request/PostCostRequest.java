package it.course.myblogc3.payload.request;

import java.util.Date;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;

@Getter
public class PostCostRequest {
	
	private long postId;
	
	@NotNull
	private int shiftCost;
	
	
	@NotNull
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date startDate;
	
	@Future
	@NotNull
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date endDate;
	

}
