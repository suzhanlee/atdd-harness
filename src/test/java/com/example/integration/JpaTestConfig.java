package com.example.integration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 테스트 설정.
 *
 * <p>@DataJpaTest에서 엔티티와 리포지토리를 스캔하기 위한 설정.
 * com.example.subscription 하위 패키지를 스캔한다.
 */
@Configuration
@EntityScan(basePackages = "com.example")
@EnableJpaRepositories(basePackages = "com.example")
class JpaTestConfig {
}
