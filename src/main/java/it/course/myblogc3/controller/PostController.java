package it.course.myblogc3.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.course.myblogc3.entity.Language;
import it.course.myblogc3.entity.Post;
import it.course.myblogc3.entity.PostCost;
import it.course.myblogc3.entity.PostCostId;
import it.course.myblogc3.entity.Tag;
import it.course.myblogc3.entity.User;
import it.course.myblogc3.payload.request.PostCostRequest;
import it.course.myblogc3.payload.request.PostRequest;
import it.course.myblogc3.payload.request.TagNamesRequest;
import it.course.myblogc3.payload.response.ApiResponseCustom;
import it.course.myblogc3.payload.response.CommentResponseForPostDetail;
import it.course.myblogc3.payload.response.PostDetailResponse;
import it.course.myblogc3.payload.response.PostResponse;
import it.course.myblogc3.payload.response.PostResponseForSearch;
import it.course.myblogc3.payload.response.PostResponseWithPreferredTags;
import it.course.myblogc3.repository.CommentRepository;
import it.course.myblogc3.repository.LanguageRepository;
import it.course.myblogc3.repository.PostCostRepository;
import it.course.myblogc3.repository.PostRepository;
import it.course.myblogc3.repository.TagRepository;
import it.course.myblogc3.repository.UserRepository;
import it.course.myblogc3.service.PostService;
import it.course.myblogc3.service.UserService;

@RestController
@Validated
public class PostController {
	
	@Autowired PostRepository postRepository;
	@Autowired TagRepository tagRepository;
	@Autowired CommentRepository commentRepository;
	@Autowired LanguageRepository languageRepository;
	@Autowired UserService userService;
	@Autowired UserRepository userRepository;
	@Autowired PostService postService;
	@Autowired PostCostRepository postCostRepository;
	
	@PostMapping("private/create-post")
	@PreAuthorize("hasRole('EDITOR')")
	public ResponseEntity<ApiResponseCustom> createPost(@RequestBody PostRequest postRequest, HttpServletRequest request){
		
		if(postRepository.existsByTitle(postRequest.getTitle()))
			return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(),200,"OK", "This post title already exists" ,
						request.getRequestURI() ),HttpStatus.OK);
		
		Optional<Language> l = languageRepository.findByLangCodeAndVisibleTrue(postRequest.getLanguage());
		if(!l.isPresent())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(
							Instant.now(),200,"OK", "Language not found" ,request.getRequestURI() ),HttpStatus.OK);
		
		User u = userService.getAuthenticatedUser();
		
		Post p = new Post(postRequest.getTitle(), postRequest.getContent(), u ,l.get());
		
		if(!postRequest.getTagNames().isEmpty()) {
			Set<Tag> tags = tagRepository.findByTagNameInAndVisibleTrue(postRequest.getTagNames());
			if(!tags.isEmpty())
				p.setTag(tags);
		}	
		
		postRepository.save(p);		
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK", "new post added", request.getRequestURI()),
				HttpStatus.OK);
	}
	
	@PutMapping("private/update-post-status/{postId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> updatePostStatus(@PathVariable long postId, HttpServletRequest request){
		
		boolean exists = postRepository.existsById(postId);
		
		if(!exists)
			return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 404, "OK", "Post not found", request.getRequestURI()),
					HttpStatus.NOT_FOUND);
		
		postRepository.updateStatusPostJpql(postId);
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, "OK", "Post status updated", request.getRequestURI()),
				HttpStatus.OK);		
		
	}
	
	@GetMapping("public/get-visibile-posts")
	public ResponseEntity<ApiResponseCustom> getVisiblePosts(HttpServletRequest request) {
		
		List<PostResponse> ps = postRepository.getPostsVisible();
		
		if(ps.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 404, "OK", "No posts found", request.getRequestURI()),
				HttpStatus.NOT_FOUND);
		
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 200, "OK", ps, request.getRequestURI()),
			HttpStatus.OK);
		
	}
	

	@GetMapping("public/get-visibile-post-public/{postId}")
	public ResponseEntity<ApiResponseCustom> getVisiblePostPublic(@PathVariable long postId, HttpServletRequest request) {
		
		PostDetailResponse p = postRepository.getPostDetailPublic(postId);		
		
		if(p == null)
			return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 404, "OK", "No post found/purchased whith id "+postId, request.getRequestURI()),
				HttpStatus.NOT_FOUND);
		
		
		Set<Tag> tags = tagRepository.getTagsInPost(postId);
		List<CommentResponseForPostDetail> cs = commentRepository.commentFromPost(postId);
		p.setTags(tags.stream().map(Tag::getTagName).collect(Collectors.toSet()));
		p.setComments(cs);
		
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 200, "OK", p, request.getRequestURI()),
			HttpStatus.OK);
		
	}
	
	@GetMapping("private/get-visibile-post-private/{postId}")
	public ResponseEntity<ApiResponseCustom> getVisiblePostPrivate(@PathVariable long postId, HttpServletRequest request) {
		
		User u = userService.getAuthenticatedUser();
		
		PostDetailResponse p = postRepository.getPostDetailPrivate(postId, u.getId());		
		
		if(p == null)
			return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 404, "OK", "No post found/purchased whith id "+postId, request.getRequestURI()),
				HttpStatus.NOT_FOUND);
		
		
		Set<Tag> tags = tagRepository.getTagsInPost(postId);
		List<CommentResponseForPostDetail> cs = commentRepository.commentFromPost(postId);
		p.setTags(tags.stream().map(Tag::getTagName).collect(Collectors.toSet()));
		p.setComments(cs);
		
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 200, "OK", p, request.getRequestURI()),
			HttpStatus.OK);
		
	}
	
	@DeleteMapping("public/delete-post/{postId}")
	public ResponseEntity<ApiResponseCustom> deletePost(@PathVariable Long postId, HttpServletRequest request){
		
		postRepository.deletePost(postId);
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200,"OK",
				"The post with id "+postId+" not exists more", request.getRequestURI()),HttpStatus.OK);
	}
	
	@GetMapping("public/get-visibile-posts-by-tags")
	public ResponseEntity<ApiResponseCustom> getVisiblePostsByTags(@RequestBody TagNamesRequest tagNamesRequest, HttpServletRequest request) {
		
		List<PostResponse> ps = postRepository.getPostsVisibleByTagNames(tagNamesRequest.getTagNames());
		if(ps.isEmpty())
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200,"OK",
				"No posts found with tags "+tagNamesRequest.getTagNames(), request.getRequestURI()),HttpStatus.OK);
	
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, 
				"OK", ps, request.getRequestURI()), HttpStatus.OK);
	}
	
	@GetMapping("public/get-visibile-posts-by-lang/{langCode}")
	public ResponseEntity<ApiResponseCustom> getVisiblePostsByLang(@PathVariable @Size(min=2,max=2) @NotBlank String langCode, HttpServletRequest request){
		
		List<PostResponse> ps = postRepository.getPostsByLanguage(langCode);
		if(ps.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, 
					"OK", "No posts found written in "+langCode, request.getRequestURI()), HttpStatus.OK);
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, 
			"OK", ps, request.getRequestURI()), HttpStatus.OK);
	}
	
	@GetMapping("public/get-visibile-posts-by-keyword-sql")
	public ResponseEntity<ApiResponseCustom> getPostsByKeywordsSql(@RequestParam boolean caseSensitive, 
			@RequestParam boolean exactMatch,  @RequestParam String wordToFind, HttpServletRequest request){
		
		String wordToFindExact = "\\b".concat(wordToFind.concat("\\b"));
		List<Post> ps = new ArrayList<Post>();
		
		if(caseSensitive)
			if(exactMatch)
				ps = postRepository.getPostsVisibleBySearchCaseSensitiveTrue(wordToFindExact);
			else
				ps = postRepository.getPostsVisibleBySearchCaseSensitiveTrue(wordToFind);
		else
			if(exactMatch)
				ps = postRepository.getPostsVisibleBySearchCaseSensitiveFalse(wordToFindExact);
			else
				ps = postRepository.getPostsVisibleBySearchCaseSensitiveFalse(wordToFind);
		
		if(ps.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 404, 
					"404", "No posts found for keyword: "+wordToFind, request.getRequestURI()), HttpStatus.NOT_FOUND);
		
		List<PostResponseForSearch> prs = ps.stream()
				.map(post-> new PostResponseForSearch(
						post.getId(),
						post.getTitle(),
						post.getContent(),
						userRepository.getAuthorUsernameByPostId(post.getId()),
						post.getUpdatedAt()
						))
				.collect(Collectors.toList());
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, 
				"OK", prs, request.getRequestURI()), HttpStatus.OK);
	}
	
	@GetMapping("public/get-visibile-posts-by-keyword-java")
	public ResponseEntity<ApiResponseCustom> getPostsByKeywordsJava(@RequestParam boolean caseSensitive, 
			@RequestParam boolean exactMatch,  @RequestParam String wordToFind, HttpServletRequest request){
		
		List<PostResponseForSearch> ps = postRepository.getPostsVisibleForSearch();
		List<PostResponseForSearch> ps2 = new ArrayList<PostResponseForSearch>();
		
		if(exactMatch)
			if(!caseSensitive)
				ps2 = ps.stream().filter(p ->
					(postService.isExactMatch(wordToFind.toLowerCase(), p.getTitle().toLowerCase())) ||
					(postService.isExactMatch(wordToFind.toLowerCase(), p.getContent().toLowerCase()))	
				).collect(Collectors.toList());
			else
				ps2 = ps.stream().filter(p ->
					(postService.isExactMatch(wordToFind, p.getTitle())) ||
					(postService.isExactMatch(wordToFind, p.getContent()))	
				).collect(Collectors.toList());
		else
			if(!caseSensitive)
				ps2 = ps.stream().filter(p ->
					p.getTitle().toLowerCase().contains(wordToFind.toLowerCase()) ||
					p.getContent().toLowerCase().contains(wordToFind.toLowerCase())
				).collect(Collectors.toList());
			else
				ps2 = ps.stream().filter(p ->
					p.getTitle().contains(wordToFind) ||
					p.getContent().contains(wordToFind)
				).collect(Collectors.toList());
		
			
		if(ps2.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(	Instant.now(),
					200,"OK","no post found" ,
					request.getRequestURI()),HttpStatus.NOT_FOUND);
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(Instant.now(), 200, 
				"OK", ps2, request.getRequestURI()), HttpStatus.OK);
	}
	
	@GetMapping("private/get-visibile-posts-by-preferred-tags")
	@PreAuthorize("hasRole('READER') or hasRole('EDITOR')")
	public ResponseEntity<ApiResponseCustom> getVisiblePostsByPreferredTags(HttpServletRequest request){
		
		User loggeduser=userService.getAuthenticatedUser();
		
		Set<Post> ps = postRepository.getPostsVisibleByPreferredTags(loggeduser.getId());
		
		if(ps.isEmpty())
			return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom( Instant.now(), 200,"OK","no post found ",
					request.getRequestURI()),HttpStatus.OK);
		
		List<PostResponseWithPreferredTags> prwp = ps.stream().map(PostResponseWithPreferredTags::createFromEntity)
		.collect(Collectors.toList());
		
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", prwp, request.getRequestURI()),
				HttpStatus.OK);
	}
	
	
	@PutMapping("private/update-post-cost/{postId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> updatePostCost(@PathVariable long postId, @RequestParam @NotNull int cost, HttpServletRequest request) {
		
		Optional<Post> p = postRepository.findById(postId);
		if(!p.isPresent())
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 404, "Not Found", "Post "+postId+" not found", request.getRequestURI()),
				HttpStatus.NOT_FOUND);
		
		if(p.get().getCost() == 0) {
			p.get().setCost(cost);
			postRepository.save(p.get());
		} else {
			return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", "A initial cost has alrady set for post "+postId, request.getRequestURI()), HttpStatus.OK);
		}
		
		return new ResponseEntity<ApiResponseCustom>(
				new ApiResponseCustom(Instant.now(), 200, "OK", "Initial cost for post "+postId+" has been set" , request.getRequestURI()), HttpStatus.OK);
	}
	
	@PostMapping("private/update-shift-cost")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponseCustom> updateShiftCost(@Valid @RequestBody PostCostRequest postCostRequest, HttpServletRequest request) {

	Optional<Post> p = postRepository.findById(postCostRequest.getPostId());
		if(!p.isPresent())
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 404, "Not Found", "Post "+postCostRequest.getPostId()+" not found", request.getRequestURI()),
			HttpStatus.NOT_FOUND);
	
	if(p.get().getCost() == 0)
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 200, "OK", "You need to set an initial cost for post "+postCostRequest.getPostId(), request.getRequestURI()),
				HttpStatus.OK);
	
	Date startDate = postCostRequest.getStartDate();
	Date endDate = postCostRequest.getEndDate();
	
	if(endDate.before(startDate))
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 200, "OK", "Are you sure you want to set the shift after its end?", request.getRequestURI()),
			HttpStatus.OK);

	PostCostId pcId = new PostCostId(p.get(), startDate, endDate);

	if(postCostRepository.existsById(pcId))
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 200, "OK", "There is already a shiftCost with these dates", request.getRequestURI()),
			HttpStatus.OK);

	int realCost = postService.getRealCost(postCostRequest.getPostId(), p.get().getCost(), startDate, endDate);

	if(realCost + postCostRequest.getShiftCost() < 0 )
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 200, "OK", "Are you sure you want to pay users to show them a post?", request.getRequestURI()),
			HttpStatus.OK);
	
	// minimum cost = 1
	if(realCost + postCostRequest.getShiftCost() == 0 )
		return new ResponseEntity<ApiResponseCustom>(
			new ApiResponseCustom(Instant.now(), 200, "OK", "The final cost can't be zero!", request.getRequestURI()),
			HttpStatus.OK);

	PostCost newPostCost = new PostCost(pcId, postCostRequest.getShiftCost());

	postCostRepository.save(newPostCost);

	return new ResponseEntity<ApiResponseCustom>(
		new ApiResponseCustom(Instant.now(), 200, "OK", "Now post is purchasable for "+ (realCost + postCostRequest.getShiftCost()) +" credits", request.getRequestURI()),
		HttpStatus.OK);
	}
	
	
	
}
