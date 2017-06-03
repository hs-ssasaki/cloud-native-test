package com.metflix;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.catalina.filters.RequestDumperFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class MembershipApplication {

	public static void main(String[] args) {
		SpringApplication.run(MembershipApplication.class, args);
	}
	
	// RequestDumperFilterをBean登録することで、Webリクエストをコンソールに表示できる
	@Bean
	RequestDumperFilter requestDumperFilter() {
		return new RequestDumperFilter();
	}
}

class Member {
	public String user;
	public Integer age;
	
	public Member() {}

	public Member(String name, Integer age) {
		this.user = name;
		this.age = age;
	}
}

@RestController
@RequestMapping("/api/members")
class MembershipController {
	// ConcurrentHashMapは、スレッドセーフなHashMap
	@SuppressWarnings("serial")
	final Map<String, Member> memberStore = new ConcurrentHashMap<String, Member>() {
		{
			put("yamada", new Member("yamada", 10));
			put("sato", new Member("sato", 30));
		}
	};
	
	/* 
	 * Postエンドポイントでのリクエストボディの取得方法。
	 * １．リクエストに一致するPOJOを定義する。
	 * ２．コントロールメソッドの引数に @RequestBody を指定
	 */
	@PostMapping
	public Member register(@RequestBody Member member) {
		memberStore.put(member.user, member);
		return member;
	}

	/*
	 * 動作確認
	 * curl -XPOST http://localhost:4444/api/members -H 'Content-Type: application/json' -d '{"user":"tanaka", "age":"20"}'
	 */
	@GetMapping("/{user}")
	Member get(@PathVariable String user) {
		return memberStore.get(user);
	}
}