package com.metflix;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.catalina.filters.RequestDumperFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

/*
 * CircuitBreakerの導入
 * APIをコールするクライアント側に導入する。
 * 
 * 1. pom.xml編集
 * 2. @EnableCircuitBreakerを付与
 * 3. 外部のRest apiをコールするメソッドに、@HystrixCommanを付与
 *  - fallbackMethod エラー時に分岐させるフォールバックメソッドを指定
 *  - ignoreException エラー判定を無視する例外を指定
 *  - commandProperties
 * 4. フォールバックメソッドを定義
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
public class RecommendationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecommendationsApplication.class, args);
	}
	
	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@Bean
	RequestDumperFilter requestDumperFilter() {
		return new RequestDumperFilter();
	}
}


@RestController
@RequestMapping("/api/recommendations")
class RecommendationsController {
	List<Movie> kidsRecommendations = Arrays.asList(new Movie("Lion king"), new Movie("Totoro"));
	List<Movie> adultRecommendations = Arrays.asList(new Movie("shown"), new Movie("spring"));
	List<Movie> familyRecommendations = Arrays.asList(new Movie("hook"), new Movie("the sandlot"));

	@Autowired
	RestTemplate restTemplate;

	@Value("${member.api:http://localhost:4444}")
	URI memberApi;


	@HystrixCommand(fallbackMethod="recommendationFallback",
			ignoreExceptions=UserNotFoundException.class,
			commandProperties=@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value="5000"))
	@GetMapping("/{user}")
	public List<Movie> findRecommendationsForUser(@PathVariable String user) throws UserNotFoundException {
		/*
		 * RestTemplateクライアントから、membershipのREST APIをたたいて、memberオブジェクトを取得する。
		 * RestTemplete#exchange()で、Rest apiをたたく
		 * UriComponentBuilder()で、URIを作成
		 * RequestEntity.get()で、リクエストエンティティを作成 
		 */
		Member member = restTemplate.exchange(
				RequestEntity.get(UriComponentsBuilder.fromUri(memberApi).pathSegment("api", "members", user).build().toUri()).build(), 
				Member.class).getBody();
		
		if(member == null) 
			throw new UserNotFoundException();
		
		return member.age < 17 ? kidsRecommendations : adultRecommendations ;
	}
	
    /**
     * Should be safe for all audiences
     */
    List<Movie> recommendationFallback(String user) {
        return familyRecommendations;
    }
}

class Movie {
	public String title;

	public Movie() {}
	public Movie(String title) {
		super();
		this.title = title;
	}
}

class Member {
	public String user;
	public Integer age;
}

@SuppressWarnings("serial")
class UserNotFoundException extends Exception {
}