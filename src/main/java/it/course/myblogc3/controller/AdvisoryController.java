package it.course.myblogc3.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.course.myblogc3.entity.Advisory;
import it.course.myblogc3.entity.AdvisoryId;
import it.course.myblogc3.entity.AdvisoryReason;
import it.course.myblogc3.entity.AdvisoryStatus;
import it.course.myblogc3.entity.Comment;
import it.course.myblogc3.entity.User;
import it.course.myblogc3.payload.request.AdvisoryRequest;
import it.course.myblogc3.payload.request.AdvisoryStatusUpdateRequest;
import it.course.myblogc3.payload.response.AdvisoryResponse;
import it.course.myblogc3.payload.response.ApiResponseCustom;
import it.course.myblogc3.repository.AdvisoryReasonDetailRepository;
import it.course.myblogc3.repository.AdvisoryReasonRepository;
import it.course.myblogc3.repository.AdvisoryRepository;
import it.course.myblogc3.repository.CommentRepository;
import it.course.myblogc3.repository.UserRepository;
import it.course.myblogc3.service.AdvisoryService;
import it.course.myblogc3.service.BanService;
import it.course.myblogc3.service.UserService;

@RestController
public class AdvisoryController {
	
	@Autowired CommentRepository commentRepository;
	@Autowired UserService userService;
	@Autowired AdvisoryReasonRepository advisoryReasonRepository;
	@Autowired AdvisoryRepository advisoryRepository;
	@Autowired UserRepository userRepository;
	@Autowired AdvisoryReasonDetailRepository advisoryReasonDetailRepository;
	@Autowired AdvisoryService advisoryService;
	@Autowired BanService banService;
	
	
	@PostMapping("private/create-advisory")
	@PreAuthorize("hasRole('EDITOR') or hasRole('READER')")
	public ResponseEntity<ApiResponseCustom> createAdvisory(@RequestBody AdvisoryRequest advisoryRequest, HttpServletRequest request){
				
		Optional<Comment> c = commentRepository.findByIdAndVisibleTrue(advisoryRequest.getCommentId());
		if(!c.isPresent()) 				
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(),404, "NOT FOUND","Comment not found",request.getRequestURI()
					), HttpStatus.NOT_FOUND);
		
		Optional<AdvisoryReason> reason = advisoryReasonRepository.findByAdvisoryReasonName(advisoryRequest.getReason());
		if(!reason.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(
							Instant.now(), 404, "NOT FOUND", "Advisory reason not found" ,request.getRequestURI() ),HttpStatus.NOT_FOUND);
		
		User u = userService.getAuthenticatedUser();
	
		if(u.getId().equals(c.get().getCommentAuthor().getId())) 
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(),401,"Forbidden","Why are you reporting yourself?",request.getRequestURI()
					), HttpStatus.FORBIDDEN);
		
		boolean exists = advisoryRepository.existsByAdvisoryIdCommentAndAdvisoryIdAdvisoryReason(c.get(), reason.get());
		if(exists)
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(),401,"Forbidden","This comment has been reported already for the same reason",request.getRequestURI()
					), HttpStatus.FORBIDDEN);
		
		AdvisoryId aId = new AdvisoryId(c.get(), u, reason.get());
		Advisory a = new Advisory(aId, AdvisoryStatus.OPEN, advisoryRequest.getDescription());
		advisoryRepository.save(a);
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK", "new advisory added", request.getRequestURI()),
				HttpStatus.OK);
	}
	
	@PutMapping("private/change-status-advisory")
	@PreAuthorize("hasRole('ADMIN')")
	@Transactional
	public ResponseEntity<ApiResponseCustom> changeStatusPost(@Valid @RequestBody AdvisoryStatusUpdateRequest advisoryStatusUpdateRequest, HttpServletRequest request){
				
		Advisory a = advisoryRepository.getAdvisoryById(
				advisoryStatusUpdateRequest.getUserId(),
				advisoryStatusUpdateRequest.getCommentId(),
				advisoryStatusUpdateRequest.getReasonId()
				);
		
		if(a == null)
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(),404,"NOT FOUND","Advisory not found",request.getRequestURI()
					), HttpStatus.NOT_FOUND);
		
		if(a.getStatus().ordinal() == 2 || a.getStatus().ordinal() == 3)
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(),404,"NOT FOUND","Advisory alredy closed",request.getRequestURI()
					), HttpStatus.NOT_FOUND);
		
		a.setStatus(AdvisoryStatus.valueOf(advisoryStatusUpdateRequest.getStatus()));
		
		if(advisoryStatusUpdateRequest.getStatus().equals(AdvisoryStatus.valueOf("CLOSED_WITH_CONSEQUENCE").toString())) {
			commentRepository.updateVisibilityComment(advisoryStatusUpdateRequest.getCommentId());
			
			// recuperare la gravità
			int gravity = advisoryReasonDetailRepository.getSeverityValue(advisoryStatusUpdateRequest.getReasonId());
			
			// aggiornare bannedUntil e disbilitare user
			Optional<User> u = userRepository.getCommentAuthorByCommentId(advisoryStatusUpdateRequest.getCommentId());
			banService.setBanUntil(u.get(), gravity);
					
		}
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK", "Advisory status has been updated", request.getRequestURI()),
				HttpStatus.OK);
	}
	
	@GetMapping("private/get-open-advisories")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> getOpenAdvisories(HttpServletRequest request){
		
		List<AdvisoryResponse> list = advisoryRepository.getOpenAdvisories();
		if(list.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 404, "NOT_FOUND", "No open advisories found", request.getRequestURI()),
					HttpStatus.NOT_FOUND);
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK", list, request.getRequestURI()),
				HttpStatus.OK);
	}

}
