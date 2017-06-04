package com.metflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/*
 * Eureka-server構築
 * 1. pom.xml編集
 * 2. @EnableEurekaServer付与
 * 3. コンフィグサーバのレポジトリのapplication.propertiesを変更
 *  - EurekaServer導入前
 *     member.api=http://localhost:4444
 *     recommendation.api=http://localhost:3333
 *  - EurekaServer導入後(bootstrap.propertiesのspring.application.nameで指定した名前でエンドポイントを引けるようになる)
 *     member.api=http://membership
 *     recommendation.api=http://recommendations
 * 動作確認
 *  http://localhost:8761 にアクセス
 *  指定した名前で、各サービスのエンドポイントが登録され、かつ、インスタンス（複数でも可）の稼働を監視しているのがわかる。
 *  
 * 以下は、演習のため。ConfigServerのレポジトリを切り替えるために設定
 * 3. bootstrap.properties編集
 *  ConfigServerのコンフィグレーションレポジトリのパスは、以下のため
 *  　「http://localhost:8888/{name}/{env}/{label}」
 *     envのデフォルトは'default'
 *  Githubの場合、
 *   label は Githubのブランチに対応。
 *   name と env は、プロパティファイル名に対応
 *  {name=membership}, {env=development}, {label=eureka-server}だと、
 *  eureka-serverブランチの、membership-development.properties に設定する。
 *  
 *  
 *  Eureka-clientの設定
 * 1. pom.xml編集
 *  	<dependency>
 *  		<groupId>org.springframework.cloud</groupId>
 *  		<artifactId>spring-cloud-starter-eureka</artifactId>
 *  	</dependency> 
 * 2. Eurekaサーバに登録したいアプリに、@EnableDiscoveryClient を付与
 * 3. JavaConfigのRestTemplateのBean登録時に、@LoadBarancerをつける。
 * 　　これにより以下の動作をするようになる。
 *     - httpアクセス時に、Eurekaサーバで名前解決してからアクセスする
 *     - Ribonライブラリを有効化して、ロードバランスでインスタンスを取得するようにする
 *
 * 以下は演習のため、ConfigServerのレポジトリを切り替える。
 * 4. bootstrap.properties追加
 *   spring.cloud.config.label=service-registry
 */
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
	}
}
