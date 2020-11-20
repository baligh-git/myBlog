package it.course.myblogc3.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="LOGIN_TRACE")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class LoginTrace {
	
	@EmbeddedId
	private LoginTraceId loginTraceId;

}
