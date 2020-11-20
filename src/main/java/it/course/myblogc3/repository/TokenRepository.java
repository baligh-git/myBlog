package it.course.myblogc3.repository;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.course.myblogc3.entity.Token;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long>{
	
	boolean existsByToken(String token);
	
	void deleteAllByExpiryDateLessThan(Date expiryDate);

}
