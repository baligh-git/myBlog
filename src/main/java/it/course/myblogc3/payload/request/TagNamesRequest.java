package it.course.myblogc3.payload.request;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter //@Setter
public class TagNamesRequest {
	
	Set<String> tagNames;

}
