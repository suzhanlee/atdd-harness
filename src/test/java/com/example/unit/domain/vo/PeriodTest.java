package com.example.unit.domain.vo;

import static org.assertj.core.api.Assertions.*;

import com.example.subscription.domain.vo.Period;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Period Value Object 테스트")
class PeriodTest {

    @Nested
    @DisplayName("생성자는")
    class Constructor {

        @Test
        @DisplayName("유효한 시작과 종료 시간으로 생성한다")
        void constructor_withValidPeriod() {
            // given
            Instant start = Instant.now();
            Instant end = start.plus(30, ChronoUnit.DAYS);

            // when
            Period period = new Period(start, end);

            // then
            assertThat(period.getStartAt()).isEqualTo(start);
            assertThat(period.getEndAt()).isEqualTo(end);
        }

        @Test
        @DisplayName("null startAt으로 생성 시 예외를 던진다")
        void constructor_withNullStartAt_throwsException() {
            // given
            Instant end = Instant.now().plus(30, ChronoUnit.DAYS);

            // when & then
            assertThatThrownBy(() -> new Period(null, end))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("startAt must not be null");
        }

        @Test
        @DisplayName("null endAt으로 생성 시 예외를 던진다")
        void constructor_withNullEndAt_throwsException() {
            // given
            Instant start = Instant.now();

            // when & then
            assertThatThrownBy(() -> new Period(start, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("endAt must not be null");
        }

        @Test
        @DisplayName("startAt이 endAt 이후인 경우 예외를 던진다")
        void constructor_withStartAfterEnd_throwsException() {
            // given
            Instant start = Instant.now().plus(30, ChronoUnit.DAYS);
            Instant end = Instant.now();

            // when & then
            assertThatThrownBy(() -> new Period(start, end))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("startAt must be before endAt");
        }

        @Test
        @DisplayName("startAt과 endAt이 같은 경우 예외를 던진다")
        void constructor_withSameStartAndEnd_throwsException() {
            // given
            Instant same = Instant.now();

            // when & then
            assertThatThrownBy(() -> new Period(same, same))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("startAt must be before endAt");
        }
    }

    @Nested
    @DisplayName("contains 메서드는")
    class Contains {

        @Test
        @DisplayName("기간 내의 시간이면 true를 반환한다")
        void contains_timeWithinPeriod_returnsTrue() {
            // given
            Instant start = Instant.now();
            Instant middle = start.plus(15, ChronoUnit.DAYS);
            Instant end = start.plus(30, ChronoUnit.DAYS);
            Period period = new Period(start, end);

            // when & then
            assertThat(period.contains(middle)).isTrue();
        }

        @Test
        @DisplayName("시작 시간이면 true를 반환한다")
        void contains_startTime_returnsTrue() {
            // given
            Instant start = Instant.now();
            Instant end = start.plus(30, ChronoUnit.DAYS);
            Period period = new Period(start, end);

            // when & then
            assertThat(period.contains(start)).isTrue();
        }

        @Test
        @DisplayName("종료 시간 직전이면 true를 반환한다")
        void contains_endTimeMinusOne_returnsTrue() {
            // given
            Instant start = Instant.now();
            Instant end = start.plus(30, ChronoUnit.DAYS);
            Period period = new Period(start, end);

            // when & then
            assertThat(period.contains(end.minusMillis(1))).isTrue();
        }

        @Test
        @DisplayName("종료 시간이면 false를 반환한다")
        void contains_endTime_returnsFalse() {
            // given
            Instant start = Instant.now();
            Instant end = start.plus(30, ChronoUnit.DAYS);
            Period period = new Period(start, end);

            // when & then
            assertThat(period.contains(end)).isFalse();
        }

        @Test
        @DisplayName("기간 이전의 시간이면 false를 반환한다")
        void contains_timeBeforePeriod_returnsFalse() {
            // given
            Instant start = Instant.now();
            Instant end = start.plus(30, ChronoUnit.DAYS);
            Period period = new Period(start, end);
            Instant before = start.minus(1, ChronoUnit.DAYS);

            // when & then
            assertThat(period.contains(before)).isFalse();
        }

        @Test
        @DisplayName("기간 이후의 시간이면 false를 반환한다")
        void contains_timeAfterPeriod_returnsFalse() {
            // given
            Instant start = Instant.now();
            Instant end = start.plus(30, ChronoUnit.DAYS);
            Period period = new Period(start, end);
            Instant after = end.plus(1, ChronoUnit.DAYS);

            // when & then
            assertThat(period.contains(after)).isFalse();
        }
    }

    @Nested
    @DisplayName("overlaps 메서드는")
    class Overlaps {

        @Test
        @DisplayName("겹치는 기간이면 true를 반환한다")
        void overlaps_overlappingPeriods_returnsTrue() {
            // given
            Instant now = Instant.now();
            Period period1 = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Period period2 = new Period(now.plus(15, ChronoUnit.DAYS), now.plus(45, ChronoUnit.DAYS));

            // when & then
            assertThat(period1.overlaps(period2)).isTrue();
        }

        @Test
        @DisplayName("완전히 포함되는 기간이면 true를 반환한다")
        void overlaps_containedPeriod_returnsTrue() {
            // given
            Instant now = Instant.now();
            Period period1 = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Period period2 = new Period(now.plus(5, ChronoUnit.DAYS), now.plus(10, ChronoUnit.DAYS));

            // when & then
            assertThat(period1.overlaps(period2)).isTrue();
        }

        @Test
        @DisplayName("같은 기간이면 true를 반환한다")
        void overlaps_samePeriod_returnsTrue() {
            // given
            Instant now = Instant.now();
            Period period1 = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Period period2 = new Period(now, now.plus(30, ChronoUnit.DAYS));

            // when & then
            assertThat(period1.overlaps(period2)).isTrue();
        }

        @Test
        @DisplayName("인접한 기간(종료=시작)이면 false를 반환한다")
        void overlaps_adjacentPeriod_returnsFalse() {
            // given
            Instant now = Instant.now();
            Period period1 = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Period period2 = new Period(now.plus(30, ChronoUnit.DAYS), now.plus(60, ChronoUnit.DAYS));

            // when & then
            assertThat(period1.overlaps(period2)).isFalse();
        }

        @Test
        @DisplayName("분리된 기간이면 false를 반환한다")
        void overlaps_separatePeriods_returnsFalse() {
            // given
            Instant now = Instant.now();
            Period period1 = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Period period2 = new Period(now.plus(60, ChronoUnit.DAYS), now.plus(90, ChronoUnit.DAYS));

            // when & then
            assertThat(period1.overlaps(period2)).isFalse();
        }
    }

    @Nested
    @DisplayName("getDurationInSeconds 메서드는")
    class GetDurationInSeconds {

        @Test
        @DisplayName("기간의 길이를 초 단위로 반환한다")
        void getDurationInSeconds() {
            // given
            Instant start = Instant.now();
            Instant end = start.plus(30, ChronoUnit.DAYS);
            Period period = new Period(start, end);
            long expectedSeconds = 30 * 24 * 60 * 60L;

            // when
            long duration = period.getDurationInSeconds();

            // then
            assertThat(duration).isEqualTo(expectedSeconds);
        }

        @Test
        @DisplayName("1시간 기간의 길이는 3600초이다")
        void getDurationInSeconds_oneHour() {
            // given
            Instant start = Instant.now();
            Instant end = start.plus(1, ChronoUnit.HOURS);
            Period period = new Period(start, end);

            // when
            long duration = period.getDurationInSeconds();

            // then
            assertThat(duration).isEqualTo(3600L);
        }
    }

    @Nested
    @DisplayName("동등성 비교")
    class Equality {

        @Test
        @DisplayName("같은 값의 Period는 동등하다")
        void equality_sameValue() {
            // given
            Instant start = Instant.now();
            Instant end = start.plus(30, ChronoUnit.DAYS);
            Period period1 = new Period(start, end);
            Period period2 = new Period(start, end);

            // then
            assertThat(period1).isEqualTo(period2);
            assertThat(period1.hashCode()).isEqualTo(period2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 Period는 동등하지 않다")
        void equality_differentValue() {
            // given
            Instant start = Instant.now();
            Period period1 = new Period(start, start.plus(30, ChronoUnit.DAYS));
            Period period2 = new Period(start, start.plus(60, ChronoUnit.DAYS));

            // then
            assertThat(period1).isNotEqualTo(period2);
        }
    }
}
