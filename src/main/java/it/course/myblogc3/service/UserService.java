package it.course.myblogc3.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.course.myblogc3.entity.Authority;
import it.course.myblogc3.entity.AuthorityName;
import it.course.myblogc3.entity.LoginAttempt;
import it.course.myblogc3.entity.LoginAttemptId;
import it.course.myblogc3.entity.LoginTrace;
import it.course.myblogc3.entity.LoginTraceId;
import it.course.myblogc3.entity.User;
import it.course.myblogc3.repository.LoginAttemptRepository;
import it.course.myblogc3.repository.LoginTraceRepository;
import it.course.myblogc3.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired UserRepository userRepository;
	@Autowired PasswordEncoder passwordEncoder;
	@Autowired LoginAttemptRepository loginAttemptRepository;
	@Autowired LoginTraceRepository loginTraceRepository;
	
	public boolean isEligibleForCredit(User user) {
		return loginTraceRepository.existsById(new LoginTraceId(user, LocalDate.now()));
	}
	
	public void addCredit(User user) {
		
		LoginTrace lt = new LoginTrace(new LoginTraceId(user, LocalDate.now()));
		loginTraceRepository.save(lt);
		user.setCredits(user.getCredits()+1);
		userRepository.save(user);
	
	}
		
	public boolean isReader(User user) {
		
		return user.getAuthorities().stream().anyMatch(a->a.getName().equals(AuthorityName.ROLE_READER));
		
	}
		
	
	public boolean isBanned(User user) {
		if(user.getBannedUntil()!=null)
		   if(user.getBannedUntil().isAfter(LocalDateTime.now()))
			   return true;
		   else {
			user.setEnabled(true);
			user.setBannedUntil(null);
			userRepository.save(user);
		}
		return false;
	}

	
	public boolean isValidPassword(User user, String password) {
		
		return passwordEncoder.matches(password, user.getPassword());
	}
    
	
	public int getAttempt(User user,Optional<LoginAttempt> la,HttpServletRequest request) {
		int counter=1;
		if(!la.isPresent()) {
			loginAttemptRepository.save(new LoginAttempt(new LoginAttemptId(user),request.getRemoteAddr(),1));
			return 1;
		}
		else if(LocalDateTime.now().isAfter(la.get().getLoginFailAt().plusDays(1L))) 
			     la.get().setCounter(1);
		else {
			 counter=la.get().getCounter()+1;
			 la.get().setCounter(counter);
		 }
		
		loginAttemptRepository.save(la.get());
		
		
		if(counter==3) {
			user.setBannedUntil(LocalDateTime.now().plusMinutes(15L));
			user.setEnabled(false);
			userRepository.save(user);
		    loginAttemptRepository.delete(la.get());
		}
		return counter;
			
	}
	
	public User getAuthenticatedUser() {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User u = userRepository.findByUsername(authentication.getName()).get();
		return u;
		
	}
	
	
	
	public boolean isPasswordCorrect (String requestPassword, String userPassword) {
		
		return passwordEncoder.matches(requestPassword, userPassword);
		
	}
	
	
	
	public byte[] getSHA(String input) throws NoSuchAlgorithmException {	
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return md.digest(input.getBytes(StandardCharsets.UTF_8));
	} 
	
	
	public String toHexString(byte[] hash) {
		
		BigInteger number = new BigInteger(1, hash);
		StringBuilder hexString = new StringBuilder(number.toString(16));
		
		while (hexString.length() < 32) {
			hexString.insert(0, '0');
		}
		
		return hexString.toString().toUpperCase();
		
	}
	
	private static final String[] IP_HEADER_CANDIDATES = {
	        "X-Forwarded-For",
	        "Proxy-Client-IP",
	        "WL-Proxy-Client-IP",
	        "HTTP_X_FORWARDED_FOR",
	        "HTTP_X_FORWARDED",
	        "HTTP_X_CLUSTER_CLIENT_IP",
	        "HTTP_CLIENT_IP",
	        "HTTP_FORWARDED_FOR",
	        "HTTP_FORWARDED",
	        "HTTP_VIA",
	        "REMOTE_ADDR"};
	
	public String getClientIpAddress(HttpServletRequest request) {
	    for (String header : IP_HEADER_CANDIDATES) {
	        String ip = request.getHeader(header);
	        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
	            return ip;
	        }
	    }
	    return request.getRemoteAddr();
	}

}
