package it.course.myblogc3.entity;

import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="LOGIN_ATTEMPT")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class LoginAttempt {
	
	@EmbeddedId
	private LoginAttemptId loginAttemptId;
	
	@Column(name="LOGIN_FAIL_AT",
			updatable=false, insertable=false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private LocalDateTime loginFailAt;
	
	@Column(name="IP", nullable= false, length=40)
	private String ip;
	
	@Column(name="COUNTER", nullable= false, columnDefinition="TINYINT(1)")
	private int counter;
	
	public LoginAttempt(LoginAttemptId loginAttemptId, String ip, int counter) {
		super();
		this.loginAttemptId = loginAttemptId;
		this.ip = ip;
		this.counter = counter;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((loginAttemptId == null) ? 0 : loginAttemptId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof LoginAttempt))
			return false;
		LoginAttempt other = (LoginAttempt) obj;
		if (loginAttemptId == null) {
			if (other.loginAttemptId != null)
				return false;
		} else if (!loginAttemptId.equals(other.loginAttemptId))
			return false;
		return true;
	}

	
}
