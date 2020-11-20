package it.course.myblogc3.controller;

import java.time.Instant;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import it.course.myblogc3.entity.Post;
import it.course.myblogc3.entity.PurchasedPost;
import it.course.myblogc3.entity.PurchasedPostId;
import it.course.myblogc3.entity.User;
import it.course.myblogc3.payload.response.ApiResponseCustom;
import it.course.myblogc3.repository.PostRepository;
import it.course.myblogc3.repository.PurchasedPostRepository;
import it.course.myblogc3.repository.UserRepository;
import it.course.myblogc3.service.PostService;
import it.course.myblogc3.service.UserService;

@RestController
public class PurchasedPostController {
	
	@Autowired PurchasedPostRepository purchasedPostRepository;
	@Autowired UserService userService;
	@Autowired UserRepository userRepository;
	@Autowired PostRepository postRepository;
	@Autowired PostService postService;
	
	@PostMapping("private/purchase-post/{postId}")
	@PreAuthorize("hasRole('READER')")
	@Transactional
	public ResponseEntity<ApiResponseCustom> purchasePost(@PathVariable long postId, HttpServletRequest request){
		
		Optional<Post> p = postRepository.findByIdAndVisibleTrue(postId);
		if(!p.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 404, "Not Found", "Post "+postId+" not found", request.getRequestURI()),
					HttpStatus.NOT_FOUND);
		
		if(p.get().getCost()==0) {
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 200, "OK", "Post is free", request.getRequestURI()),
					HttpStatus.OK);	
		}
		
		User u = userService.getAuthenticatedUser();
		
		if(purchasedPostRepository.existsById(new PurchasedPostId(p.get(), u))) {
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 200, "OK", "You have already buy the post "+postId, request.getRequestURI()),
					HttpStatus.OK);	
		}
		
		int actualCost = postService.getActualRealCost(postId, p.get().getCost());
		
		if(!(u.getCredits() >= actualCost)) 
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 200, "OK", "Post "+postId+" is too much expensive for you", request.getRequestURI()),
						HttpStatus.OK);
		
		u.setCredits(u.getCredits() - actualCost);
		
		purchasedPostRepository.save(new PurchasedPost(new PurchasedPostId(p.get(), u)));
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "ok", "Post "+postId+" purchased", request.getRequestURI()),
				HttpStatus.OK);
	}
		

}
