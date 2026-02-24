package com.example.subscription.domain.vo;

import java.time.Instant;
import java.util.Objects;

/**
 * 기간을 표현하는 불변 값 객체.
 *
 * <p>시작 시간과 종료 시간을 캡슐화하여 구독 기간을 표현한다.
 *
 * <p>불변식:
 *
 * <ul>
 *   <li>startAt은 null이 아니어야 함
 *   <li>endAt은 null이 아니어야 함
 *   <li>startAt은 endAt보다 이전이어야 함
 * </ul>
 */
public class Period {

    private final Instant startAt;
    private final Instant endAt;

    /**
     * Period 객체를 생성한다.
     *
     * @param startAt 시작 시간 (null 불가)
     * @param endAt 종료 시간 (null 불가)
     * @throws IllegalArgumentException startAt이 endAt 이후인 경우
     * @throws NullPointerException startAt 또는 endAt이 null인 경우
     */
    public Period(Instant startAt, Instant endAt) {
        Objects.requireNonNull(startAt, "startAt must not be null");
        Objects.requireNonNull(endAt, "endAt must not be null");
        if (!startAt.isBefore(endAt)) {
            throw new IllegalArgumentException("startAt must be before endAt");
        }
        this.startAt = startAt;
        this.endAt = endAt;
    }

    /**
     * 시작 시간을 반환한다.
     *
     * @return 시작 시간
     */
    public Instant getStartAt() {
        return startAt;
    }

    /**
     * 종료 시간을 반환한다.
     *
     * @return 종료 시간
     */
    public Instant getEndAt() {
        return endAt;
    }

    /**
     * 주어진 시간이 이 기간에 포함되는지 확인한다.
     *
     * <p>시작 시간은 포함하고, 종료 시간은 포함하지 않는다 [start, end)
     *
     * @param instant 확인할 시간
     * @return 포함되면 true
     */
    public boolean contains(Instant instant) {
        return !instant.isBefore(startAt) && instant.isBefore(endAt);
    }

    /**
     * 이 기간이 다른 기간과 겹치는지 확인한다.
     *
     * <p>두 기간이 교집합이 있으면 true를 반환한다.
     *
     * @param other 다른 기간
     * @return 겹치면 true
     */
    public boolean overlaps(Period other) {
        return this.startAt.isBefore(other.endAt) && other.startAt.isBefore(this.endAt);
    }

    /**
     * 기간의 길이를 초 단위로 반환한다.
     *
     * @return 기간 길이 (초)
     */
    public long getDurationInSeconds() {
        return endAt.getEpochSecond() - startAt.getEpochSecond();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Period period = (Period) o;
        return Objects.equals(startAt, period.startAt) && Objects.equals(endAt, period.endAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startAt, endAt);
    }

    @Override
    public String toString() {
        return String.format("Period[%s ~ %s]", startAt, endAt);
    }
}
