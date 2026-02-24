package com.example.subscription.domain.vo;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Apple Product ID를 표현하는 불변 값 객체.
 *
 * <p>Apple App Store에서 제공하는 제품 식별자를 캡슐화한다. Product ID는 Apple Developer Console에서 등록한
 * 고유한 식별자이다.
 *
 * <p>불변식:
 *
 * <ul>
 *   <li>value는 null이 아니어야 함
 *   <li>value는 비어있지 않아야 함
 * </ul>
 */
@Embeddable
public class ProductId {

    private String value;

    /** JPA 기본 생성자 */
    protected ProductId() {}

    /**
     * ProductId 객체를 생성한다.
     *
     * @param value Apple Product ID (null 불가, 비어있지 않음)
     * @throws NullPointerException value가 null인 경우
     * @throws IllegalArgumentException value가 비어있는 경우
     */
    public ProductId(String value) {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
        this.value = value;
    }

    /**
     * Product ID 값을 반환한다.
     *
     * @return Product ID 값
     */
    public String getValue() {
        return value;
    }

    /**
     * 이 Product ID가 다른 Product ID보다 상위 등급인지 확인한다.
     *
     * <p>업그레이드 가능 여부 판단에 사용된다. 등급 비교는 Product ID의 접두사로 판단한다.
     *
     * <p>등급 순서: basic < pro < ultra
     *
     * @param other 비교할 Product ID
     * @return 상위 등급이면 true
     */
    public boolean isHigherTierThan(ProductId other) {
        int thisTier = getTierLevel(this.value);
        int otherTier = getTierLevel(other.value);
        return thisTier > otherTier;
    }

    /**
     * Product ID의 등급 레벨을 반환한다.
     *
     * @param productIdValue Product ID 값
     * @return 등급 레벨 (높을수록 상위 등급)
     */
    private int getTierLevel(String productIdValue) {
        String lowerValue = productIdValue.toLowerCase();
        if (lowerValue.startsWith("ultra")) {
            return 3;
        } else if (lowerValue.startsWith("pro")) {
            return 2;
        } else if (lowerValue.startsWith("basic")) {
            return 1;
        }
        // 알 수 없는 등급은 기본적으로 BASIC으로 처리
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductId productId = (ProductId) o;
        return Objects.equals(value, productId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
