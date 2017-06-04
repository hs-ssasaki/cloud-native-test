package com.metflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/* 
 * ConfigServer構築
 * 1. pom.xmlに追加
 * 		<dependency>
 * 			<groupId>org.springframework.cloud</groupId>
 * 			<artifactId>spring-cloud-config-server</artifactId>
 * 		</dependency>
 * 2. @EnableConfigServer 付与
 * 3. applications.properties設定(spring.cloud.config.server.git.uri)
 * 
 * 他マイクロサービス設定
 * 1. pom.xmlに追加
 *	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>Brixton.SR1</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-config</artifactId>
	</dependency>
 * 2. ConfigServerのレポジトリに、アプリ名.propetiesを追加。ローカルのapplication.propertiesを削除
 * 3. bootstrap.propertiesを追加 
 *    spring.cloud.config.url=ConfigServerのURL
 *    spring.application.name=アプリ名
 * 
 * ※以下必要に応じて
 * 4. /env**, /refresh** エンドポイントの認証を解除するように、Spring SecurityのJavaConfigを変更
 *    ※actuatorを使っている場合は、management.security.enabled=falseしておかないと、いけない模様
 * 5. Controllerに、@RefreshScopeをつける
 * 
 * 動作確認
 * ・コンフィグレポジトリの値を変更して、以下のエンドポイントアクセスで、値が反映される
 * 　curl -XPOST http://localhost:8080/refresh
 * ・以下のコマンドで一時的なプロパティの変更も可能
 * 　curl -XPOST http://localhost:8080/env -d message="Message is Updated"
 *　 curl -XPOST http://localhost:8080/refresh
 *	 
 */
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}
}
