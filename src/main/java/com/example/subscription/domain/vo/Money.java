package com.example.subscription.domain.vo;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * 금액을 표현하는 불변 값 객체.
 *
 * <p>통화와 금액을 함께 캡슐화하여 금액 계산의 정확성을 보장한다. BigDecimal을 사용하여 부동소수점 오차를 방지한다.
 *
 * <p>불변식:
 *
 * <ul>
 *   <li>amount는 null이 아니어야 함
 *   <li>currency는 null이 아니어야 함
 *   <li>amount는 0 이상이어야 함 (환불 계산 시 음수 허용 고려 필요)
 * </ul>
 */
public class Money {

    private final BigDecimal amount;
    private final Currency currency;

    /**
     * Money 객체를 생성한다.
     *
     * @param amount 금액 (null 불가)
     * @param currency 통화 (null 불가)
     * @throws IllegalArgumentException amount가 음수인 경우
     * @throws NullPointerException amount 또는 currency가 null인 경우
     */
    public Money(BigDecimal amount, Currency currency) {
        // TODO: TDD에서 불변식 검증 구현
        // Objects.requireNonNull(amount, "amount must not be null");
        // Objects.requireNonNull(currency, "currency must not be null");
        // if (amount.compareTo(BigDecimal.ZERO) < 0) {
        //     throw new IllegalArgumentException("amount must be non-negative");
        // }
        this.amount = amount;
        this.currency = currency;
    }

    /**
     * 금액을 반환한다.
     *
     * @return 금액
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * 통화를 반환한다.
     *
     * @return 통화
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * 두 금액을 더한다.
     *
     * @param other 더할 금액
     * @return 더한 결과
     * @throws IllegalArgumentException 통화가 다른 경우
     */
    public Money add(Money other) {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * 금액을 뺀다.
     *
     * @param other 뺄 금액
     * @return 뺀 결과
     * @throws IllegalArgumentException 통화가 다른 경우
     */
    public Money subtract(Money other) {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * 금액에 비율을 곱한다.
     *
     * @param multiplier 곱할 비율
     * @return 곱한 결과
     */
    public Money multiply(BigDecimal multiplier) {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * 다른 금액보다 큰지 확인한다.
     *
     * @param other 비교할 금액
     * @return 크면 true
     */
    public boolean isGreaterThan(Money other) {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * 0원인지 확인한다.
     *
     * @return 0원이면 true
     */
    public boolean isZero() {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount)
                && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return String.format("%s %s", currency.getCurrencyCode(), amount);
    }
}
