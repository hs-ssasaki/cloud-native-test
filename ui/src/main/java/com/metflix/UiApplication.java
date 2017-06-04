package com.metflix;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import org.apache.catalina.filters.RequestDumperFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/*
 * pom.xmlに、spring-boot-starter-security を追加した時点で、
 * デフォルトでアプリにBasic認証がかけられる。
 * application.properties に以下を追加するとbasic認証を無効化
 *  security.basic.enabled=false
 *  
 *  https://www.slideshare.net/navekazu/spring-bootweb-55470364
 *  
 * Spring Securityで認証する場合。
 * 1. @EnableWebSecurityを付与し、WebSecurityConfigureAdapterを継承したJavaConfigクラスで設定を行う。
 *    configure(WebSecurity) のオーバーライドで、全体に関わる設定（特定のリクエストに対してセキュリティ設定を無視するなど）
 *    configure(HttpSecurity) のオーバーライドで、認可の設定やログイン・ログアウトの設定
 *    configure(AuthenticationManagerBuilder auth) のオーバーライドで、認証処理の設定
 * 2. ユーザ情報の操作を行う、UserDetailsインターフェースの実装クラスを作成する。またはデフォルト実装を利用する。
 * 　　デフォルト実装のUserは、username, password, authorities, accountNonExpired, accountNonLocked, credentialisNotExpiredなどのプロパティを実装。
 * 3. 1のインスタンスを取得する、UserDetailsServiceインターフェースの実装クラスをする。またはデフォルト実装を利用する。
 * 　　UserDetailsServiceインターフェースは、loadUserByUsername(String username) を定義
 * 4. 認証ユーザの情報を画面に表示するなど、Thymeleafとの連携機能は、 thymeleaf-extras-springsecurity4 をpom.xmlに追加することで利用できる。
 */

// Spring SecurityのJavaConfigを兼ねるため、WebSecurityConfigurerAdapterを継承させる
@SpringBootApplication
@EnableDiscoveryClient
public class UiApplication extends WebSecurityConfigurerAdapter {

	public static void main(String[] args) {
		SpringApplication.run(UiApplication.class, args);
	}

	@LoadBalanced
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@Autowired
	UserDetailsService userDetailsService;
	
	// Spring SecurityのJavaConfig設定を行う。
	// configure(HttpSecurity http)のオーバーライドで、認証情報の受取方式(ベーシック認証、フォーム認証、など)の設定を行う。
	//
	// ベーシック認証の例。
	// httpBasic() : ベーシック認証を使用する
	//  .authorizeRequests() : 認可の設定。HttpSecurityからアクセス範囲を決めるオブジェクトを取得する。
	//  .antMatchers("**").authenticated() : この階層・及びこの階層以下のすべてについて認証を有効にする
	//  .antMatchers("/admin/**").hasRole("ADMIN").anyRequest().authenticated() : admin/ 以下のパスはADMIN権限をもっている認証ユーザのみ許可
	//
	// フォーム認証の例。
	// formLogin() : フォーム認証を使用する
	//  .loginPage("xx") : ログインページの指定
	//  .usernameParameter("xx") : ユーザ名を格納するフォームコントローラの名前
	//  .passwordParameter("yy") : パスワードを格納するフォームコントローラの名前
	//  .permitAll() : ログインページは全員のアクセスを許可する
	//
	// ConfigServer の追加に伴い、/env, /refresh以下を認証なしでアクセスできるように
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.httpBasic().and()
			.csrf().ignoringAntMatchers("/env**", "/refresh**").and()
			.authorizeRequests().antMatchers("/env**", "/refresh**").permitAll()
			.antMatchers("**").authenticated().and()
			.addFilterBefore(new RequestDumperFilter(), ChannelProcessingFilter.class);
	}
	
	// ・認証処理の方式の設定
	// configure(AuthenticationManagerBuilder)のオーバーライドで、認証処理の方式を設定する。
	//
	//・インメモリでの認証処理の場合
	// .inMemoryAuthentication() : インメモリ（プログラム内に固定）を使う
	//   .withUser("xx").password("yy").role("USER");
	//   .withUser("yy").password("zz").role("ADMIN");
	//
	//・dbを使った認証処理の場合
	// .jdbcuthentication() : dbを使う
	//   .dataSource(dataSource) : 接続先データ・ソースを指定
	// 
	//・ldapを使った認証処理の場合
	// .ldapAuthentication() : ldapを使う
	//
	//・時前のUserDetailsServiceを使って認証する場合
	// .userDetailsService(作成したuserDetailsServiceインスタンス）
	//
	//・パスワードエンコードの処理を挟んでから自前のUserDetailsServiceを使う場合
	// .authenticationProvider(createAuthProvider())
	//  ※createAuthProvider() 内で、作成した自前のuserDetailsServiceを渡した、authProviderインスタンスを作って返す
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService);
	}
}

class Movie {
	public String title;
}

@Controller
@RefreshScope
class HomeController {
	@Autowired
	RestTemplate restTemplate;
	@Value("${recommendation.api:http://localhost:3333}")
	URI recommendationApi;
	@Value("${message:Welcome to metflix}")
	String message;
	
	@GetMapping("/")
	public String home(Principal principal, Model model) {
        List<Movie> recommendations = restTemplate.exchange(RequestEntity.get(UriComponentsBuilder.fromUri(recommendationApi)
                .pathSegment("api", "recommendations", principal.getName())
                .build().toUri()).build(), new ParameterizedTypeReference<List<Movie>>() {
        }).getBody();
        model.addAttribute("message", message);
        model.addAttribute("username", principal.getName());
        model.addAttribute("recommendations", recommendations);
        return "index";		
	}
}

// membershipサービスに登録されているかどうかを認証処理とするため、自前のUserDetailsServiceインターフェースの実装クラスを作成する
// インターフェースで宣言されている、loadUserByUsername()メソッドをオーバーライドする。
@Component
class MemberUserDetailsService implements UserDetailsService {
    @Autowired
    RestTemplate restTemplate;
    @Value("${member.api:http://localhost:4444}")
    URI memberApi;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String member = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(memberApi)
                        .pathSegment("api", "members", username)
                        .build().toUri()).build(),
                String.class).getBody();
        if (member == null) {
            throw new UsernameNotFoundException(username + " is not found");
        }
        // UserはSpring Securityで用意された、UserDetailsインターフェースの実装済みクラス。詳細は定義の実装参照。
        // コンストラクタは、User(ユーザ名, パスワード, 権限)
        return new User(username, "metflix", AuthorityUtils.createAuthorityList("MEMBER"));
    }
}