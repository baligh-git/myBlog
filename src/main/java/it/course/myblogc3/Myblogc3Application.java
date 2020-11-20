package it.course.myblogc3;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import it.course.myblogc3.entity.Comment;
import it.course.myblogc3.entity.Language;
import it.course.myblogc3.entity.Post;
import it.course.myblogc3.entity.Tag;
import it.course.myblogc3.entity.User;
import it.course.myblogc3.entity.Voting;
import it.course.myblogc3.entity.VotingId;
import it.course.myblogc3.repository.LanguageRepository;
import it.course.myblogc3.repository.PostRepository;
import it.course.myblogc3.repository.TagRepository;
import it.course.myblogc3.repository.TokenRepository;
import it.course.myblogc3.repository.UserRepository;
import it.course.myblogc3.repository.VotingRepository;

@EnableScheduling
@SpringBootApplication
public class Myblogc3Application {

	public static void main(String[] args) {
		SpringApplication.run(Myblogc3Application.class, args);
	}
	
	@Autowired PostRepository postRepository;
	@Autowired UserRepository userRepository;
	@Autowired LanguageRepository languageRepository;
	@Autowired TagRepository tagRepository;
	@Autowired VotingRepository votingRepository;
	@Autowired TokenRepository tokenRepository;
	
	@Scheduled(cron = "0 0 10 * * * ") // seconds, minutes, hours, day of month (1-31) , month, day of week (0-6)
	@Transactional
	public void deleteOldTokens() {
		tokenRepository.deleteAllByExpiryDateLessThan(new Date());
	}

	@Bean
	@Profile("dev")
	@Transactional
	public void createPostAutomatically() {
		
		String title = "TEST-TITLE_";
		String comment = "TEST-COMMENT_";
		
		//postRepository.deletePostByTitle(title);
		if(!postRepository.existsByTitleStartsWith(title)) {
	
			List<User> author = userRepository.getUserEditor();
			List<User> authorComment = userRepository.getUserReader();
			List<Tag> tags = tagRepository.findByVisibleTrue();
			
			
			
			List<Language> language = languageRepository.findByVisibleTrue();
			
			String content = "Lorem ipsum dolor sit amet, consectetur adipisci elit, sed do eiusmod tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrum exercitationem ullamco laboriosam, nisi ut aliquid ex ea commodi consequatur. Duis aute irure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum";
		    Random rand = new Random();
					
		    List<Post> ps =  new ArrayList<Post>();
		    List<Voting> vs = new ArrayList<Voting>();
		    
			for(int i=0; i<20; i++) {
				Post p = new Post(
					title+i,
					content,
					author.get(rand.nextInt(author.size())),
					language.get(rand.nextInt(language.size())),
					rand.nextBoolean() // IS_VISIBLE
					);
				if(p.getVisible()) {
					// comments
					List<Comment> cs = new ArrayList<Comment>();
					for(int j=0; j<10; j++) {
						Comment c = new Comment(
							comment+j,
							p,
							authorComment.get(rand.nextInt(authorComment.size())),
							rand.nextBoolean()
							);
						cs.add(c);
					}
					// tags
					Set<Tag> tg =  new HashSet<>();
					int[] arry= {1,2};
					int limit=arry[rand.nextInt(arry.length)];
					for(int j=0; j<limit; j++) {
						tg.add(tags.get(rand.nextInt(tags.size())));
						
					}
					// Voting
					for(int k=0; k<authorComment.size(); k++) {
						vs.add(new Voting(
								new VotingId(p, authorComment.get(k)),
								(int)(Math.random() * 5) + 1) //int rand = (int)(Math.random() * range) + min; // range max-min+1
								);
					}
					
					p.setComments(cs);
					p.setTag(tg);
				}
				ps.add(p);
			}
			postRepository.saveAll(ps);
			votingRepository.saveAll(vs);
		}
	}	

}
