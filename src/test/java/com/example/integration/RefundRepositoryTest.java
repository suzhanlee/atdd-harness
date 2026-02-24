package com.example.integration;

import com.example.subscription.domain.entity.Refund;
import com.example.subscription.domain.repository.RefundRepository;
import com.example.subscription.domain.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RefundRepository 통합 테스트.
 *
 * <p>JPA 연동, 쿼리 메서드, 페이징 등을 검증한다.
 */
@DataJpaTest
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
@DisplayName("RefundRepository 통합 테스트")
class RefundRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RefundRepository repository;

    private Refund createRefund(Long subscriptionId, String originalTxId, double amount, Refund.Reason reason, Instant refundedAt) {
        Money money = new Money(BigDecimal.valueOf(amount), Currency.getInstance("USD"));
        return Refund.create(subscriptionId, originalTxId, money, reason, "APPLE_CODE_001", refundedAt);
    }

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    @DisplayName("기본 CRUD")
    class BasicCrud {

        @Test
        @DisplayName("환불 저장 및 ID로 조회")
        void saveAndFindById() {
            // given
            Instant refundedAt = Instant.now();
            Refund refund = createRefund(1L, "tx_001", 9.99, Refund.Reason.USER_REQUEST, refundedAt);

            // when
            Refund saved = entityManager.persist(refund);
            entityManager.flush();

            Optional<Refund> found = repository.findById(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getOriginalTransactionId()).isEqualTo("tx_001");
            assertThat(found.get().getReason()).isEqualTo(Refund.Reason.USER_REQUEST);
        }

        @Test
        @DisplayName("환불 삭제")
        void delete() {
            // given
            Instant refundedAt = Instant.now();
            Refund refund = createRefund(1L, "tx_002", 9.99, Refund.Reason.USER_REQUEST, refundedAt);
            Refund saved = entityManager.persist(refund);
            entityManager.flush();

            // when
            repository.delete(saved);
            entityManager.flush();

            // then
            Optional<Refund> found = repository.findById(saved.getId());
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("쿼리 메서드")
    class QueryMethods {

        @Test
        @DisplayName("구독 ID로 환불 정보 조회")
        void findBySubscriptionId() {
            // given
            Instant refundedAt = Instant.now();
            Refund refund = createRefund(1L, "tx_010", 9.99, Refund.Reason.USER_REQUEST, refundedAt);
            entityManager.persist(refund);
            entityManager.flush();

            // when
            Optional<Refund> found = repository.findBySubscriptionId(1L);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getOriginalTransactionId()).isEqualTo("tx_010");
        }

        @Test
        @DisplayName("원본 트랜잭션 ID로 환불 정보 조회")
        void findByOriginalTransactionId() {
            // given
            Instant refundedAt = Instant.now();
            Refund refund = createRefund(1L, "tx_020", 9.99, Refund.Reason.APPLE_APPROVED, refundedAt);
            entityManager.persist(refund);
            entityManager.flush();

            // when
            Optional<Refund> found = repository.findByOriginalTransactionId("tx_020");

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getSubscriptionId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("구독 ID로 환불 여부 확인")
        void existsBySubscriptionId() {
            // given
            Instant refundedAt = Instant.now();
            Refund refund = createRefund(1L, "tx_030", 9.99, Refund.Reason.USER_REQUEST, refundedAt);
            entityManager.persist(refund);
            entityManager.flush();

            // when & then
            assertThat(repository.existsBySubscriptionId(1L)).isTrue();
            assertThat(repository.existsBySubscriptionId(999L)).isFalse();
        }
    }

    @Nested
    @DisplayName("페이징 및 정렬")
    class PagingAndSorting {

        @Test
        @DisplayName("특정 기간 환불 조회")
        void findByRefundedAtBetween() {
            // given
            Instant now = Instant.now();

            Refund r1 = createRefund(1L, "tx_040", 9.99, Refund.Reason.USER_REQUEST, now.minus(20, ChronoUnit.DAYS));
            Refund r2 = createRefund(2L, "tx_041", 9.99, Refund.Reason.APPLE_APPROVED, now.minus(5, ChronoUnit.DAYS));
            Refund r3 = createRefund(3L, "tx_042", 9.99, Refund.Reason.PAYMENT_ISSUE, now.plus(5, ChronoUnit.DAYS));

            entityManager.persist(r1);
            entityManager.persist(r2);
            entityManager.persist(r3);
            entityManager.flush();

            // when
            Instant from = now.minus(10, ChronoUnit.DAYS);
            Instant to = now.plus(10, ChronoUnit.DAYS);
            Page<Refund> page = repository.findByRefundedAtBetween(from, to, PageRequest.of(0, 10));

            // then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getContent()).extracting("originalTransactionId")
                    .containsExactlyInAnyOrder("tx_041", "tx_042");
        }

        @Test
        @DisplayName("사유별 환불 페이징 조회")
        void findByReason() {
            // given
            Instant now = Instant.now();

            Refund r1 = createRefund(1L, "tx_050", 9.99, Refund.Reason.USER_REQUEST, now);
            Refund r2 = createRefund(2L, "tx_051", 9.99, Refund.Reason.USER_REQUEST, now);
            Refund r3 = createRefund(3L, "tx_052", 9.99, Refund.Reason.APPLE_APPROVED, now);

            entityManager.persist(r1);
            entityManager.persist(r2);
            entityManager.persist(r3);
            entityManager.flush();

            // when
            Page<Refund> page = repository.findByReason(Refund.Reason.USER_REQUEST, PageRequest.of(0, 10));

            // then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getContent()).extracting("reason").containsOnly(Refund.Reason.USER_REQUEST);
        }
    }

    @Nested
    @DisplayName("비즈니스 시나리오")
    class BusinessScenarios {

        @Test
        @DisplayName("Apple 환불 사유 코드 저장")
        void saveWithAppleReasonCode() {
            // given
            Instant refundedAt = Instant.now();
            Money amount = new Money(BigDecimal.valueOf(9.99), Currency.getInstance("USD"));

            Refund refund = Refund.create(
                    1L,
                    "tx_060",
                    amount,
                    Refund.Reason.APPLE_APPROVED,
                    "APPLE_REASON_CODE_123",
                    refundedAt
            );

            // when
            Refund saved = entityManager.persist(refund);
            entityManager.flush();

            // then
            assertThat(saved.getAppleReasonCode()).isEqualTo("APPLE_REASON_CODE_123");
        }

        @Test
        @DisplayName("환불 금액 검증")
        void refundAmountValidation() {
            // given
            Instant refundedAt = Instant.now();
            Refund refund = createRefund(1L, "tx_070", 19.99, Refund.Reason.USER_REQUEST, refundedAt);
            Refund saved = entityManager.persist(refund);
            entityManager.flush();

            // when
            Refund found = repository.findById(saved.getId()).orElseThrow();

            // then
            assertThat(found.getRefundAmount().getAmount()).isEqualByComparingTo("19.99");
            assertThat(found.getRefundAmount().getCurrency().getCurrencyCode()).isEqualTo("USD");
        }
    }
}
