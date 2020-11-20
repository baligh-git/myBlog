package it.course.myblogc3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.course.myblogc3.entity.AdvisoryReason;
import it.course.myblogc3.payload.response.AdvisoryReasonResponse;

@Repository
public interface AdvisoryReasonRepository extends JpaRepository<AdvisoryReason, Long>{
	
	Optional<AdvisoryReason> findByAdvisoryReasonName(String advisoryReasonName);
	/*
	boolean existsByAdvisoryReasonName(String advisoryReasonName);
	
	List<AdvisoryReason> findByAdvisoryReasonNameOrIdAndAdvisoryReasonNameNot(String advisoryReasonNameOld, long id, String advisoryReasonNameNew);

	
	
	*/
}
