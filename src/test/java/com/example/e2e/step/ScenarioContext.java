package com.example.e2e.step;

import io.restassured.response.Response;
import org.springframework.stereotype.Component;

/**
 * Cucumber 시나리오 간 상태 공유를 위한 컨텍스트.
 *
 * <p>여러 Step Definition 클래스 간에 응답, 인증 토큰 등을 공유한다.
 * Spring의 request scope 대신 singleton을 사용하며, 각 시나리오마다 초기화된다.
 */
@Component
public class ScenarioContext {

    private Response response;
    private String authToken;
    private Long currentUserId;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    /**
     * 시나리오 시작 시 상태를 초기화한다.
     */
    public void clear() {
        this.response = null;
        this.authToken = null;
        this.currentUserId = null;
    }
}
