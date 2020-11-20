package it.course.myblogc3.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import it.course.myblogc3.entity.User;

@Service
public class BanService {
	
	
	public void setBanUntil(User u, int gravity) {
		if(u.getBannedUntil() == null) {
			u.setBannedUntil(LocalDateTime.now().plusDays(gravity));
		}else {
			u.setBannedUntil(u.getBannedUntil().plusDays(gravity));
		}
		u.setEnabled(false);
	}

}
