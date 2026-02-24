package com.example.integration;

import com.example.subscription.domain.entity.Transaction;
import com.example.subscription.domain.repository.TransactionRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TransactionRepository 통합 테스트.
 *
 * <p>JPA 연동, 쿼리 메서드, 페이징 등을 검증한다.
 */
@DataJpaTest
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
@DisplayName("TransactionRepository 통합 테스트")
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository repository;

    private Transaction createTransaction(Long subscriptionId, String txId, Transaction.Type type, double amount, Instant purchasedAt) {
        Money money = new Money(BigDecimal.valueOf(amount), Currency.getInstance("USD"));
        return Transaction.create(subscriptionId, txId, type, money, purchasedAt);
    }

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    @DisplayName("기본 CRUD")
    class BasicCrud {

        @Test
        @DisplayName("트랜잭션 저장 및 ID로 조회")
        void saveAndFindById() {
            // given
            Instant purchasedAt = Instant.now();
            Transaction transaction = createTransaction(1L, "tx_001", Transaction.Type.PURCHASE, 9.99, purchasedAt);

            // when
            Transaction saved = entityManager.persist(transaction);
            entityManager.flush();

            Optional<Transaction> found = repository.findById(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getTransactionId()).isEqualTo("tx_001");
            assertThat(found.get().getType()).isEqualTo(Transaction.Type.PURCHASE);
        }

        @Test
        @DisplayName("트랜잭션 삭제")
        void delete() {
            // given
            Instant purchasedAt = Instant.now();
            Transaction transaction = createTransaction(1L, "tx_002", Transaction.Type.PURCHASE, 9.99, purchasedAt);
            Transaction saved = entityManager.persist(transaction);
            entityManager.flush();

            // when
            repository.delete(saved);
            entityManager.flush();

            // then
            Optional<Transaction> found = repository.findById(saved.getId());
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("쿼리 메서드")
    class QueryMethods {

        @Test
        @DisplayName("트랜잭션 ID로 조회")
        void findByTransactionId() {
            // given
            Instant purchasedAt = Instant.now();
            Transaction transaction = createTransaction(1L, "tx_010", Transaction.Type.PURCHASE, 9.99, purchasedAt);
            entityManager.persist(transaction);
            entityManager.flush();

            // when
            Optional<Transaction> found = repository.findByTransactionId("tx_010");

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getSubscriptionId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("구독 ID로 모든 트랜잭션 조회")
        void findBySubscriptionId() {
            // given
            Instant purchasedAt = Instant.now();

            Transaction tx1 = createTransaction(1L, "tx_020", Transaction.Type.PURCHASE, 9.99, purchasedAt);
            Transaction tx2 = createTransaction(1L, "tx_021", Transaction.Type.RENEWAL, 9.99, purchasedAt);
            Transaction tx3 = createTransaction(2L, "tx_022", Transaction.Type.PURCHASE, 9.99, purchasedAt);

            entityManager.persist(tx1);
            entityManager.persist(tx2);
            entityManager.persist(tx3);
            entityManager.flush();

            // when
            List<Transaction> sub1Txs = repository.findBySubscriptionId(1L);

            // then
            assertThat(sub1Txs).hasSize(2);
            assertThat(sub1Txs).extracting("subscriptionId").containsOnly(1L);
        }

        @Test
        @DisplayName("트랜잭션 ID 존재 여부 확인")
        void existsByTransactionId() {
            // given
            Instant purchasedAt = Instant.now();
            Transaction transaction = createTransaction(1L, "tx_030", Transaction.Type.PURCHASE, 9.99, purchasedAt);
            entityManager.persist(transaction);
            entityManager.flush();

            // when & then
            assertThat(repository.existsByTransactionId("tx_030")).isTrue();
            assertThat(repository.existsByTransactionId("tx_999")).isFalse();
        }
    }

    @Nested
    @DisplayName("페이징 및 정렬")
    class PagingAndSorting {

        @Test
        @DisplayName("구독별 트랜잭션 시간순 조회")
        void findBySubscriptionIdOrderByPurchasedAtDesc() {
            // given
            Instant now = Instant.now();

            Transaction tx1 = createTransaction(1L, "tx_040", Transaction.Type.PURCHASE, 9.99, now.minus(10, ChronoUnit.DAYS));
            Transaction tx2 = createTransaction(1L, "tx_041", Transaction.Type.RENEWAL, 9.99, now.minus(5, ChronoUnit.DAYS));
            Transaction tx3 = createTransaction(1L, "tx_042", Transaction.Type.RENEWAL, 9.99, now);

            entityManager.persist(tx1);
            entityManager.persist(tx2);
            entityManager.persist(tx3);
            entityManager.flush();

            // when
            Page<Transaction> page = repository.findBySubscriptionIdOrderByPurchasedAtDesc(1L, PageRequest.of(0, 10));

            // then
            assertThat(page.getContent()).hasSize(3);
            // 최신순 정렬 확인
            assertThat(page.getContent().get(0).getTransactionId()).isEqualTo("tx_042");
            assertThat(page.getContent().get(2).getTransactionId()).isEqualTo("tx_040");
        }

        @Test
        @DisplayName("특정 기간 트랜잭션 조회")
        void findByPurchasedAtBetween() {
            // given
            Instant now = Instant.now();

            Transaction tx1 = createTransaction(1L, "tx_050", Transaction.Type.PURCHASE, 9.99, now.minus(20, ChronoUnit.DAYS));
            Transaction tx2 = createTransaction(1L, "tx_051", Transaction.Type.RENEWAL, 9.99, now.minus(5, ChronoUnit.DAYS));
            Transaction tx3 = createTransaction(1L, "tx_052", Transaction.Type.RENEWAL, 9.99, now.plus(5, ChronoUnit.DAYS));

            entityManager.persist(tx1);
            entityManager.persist(tx2);
            entityManager.persist(tx3);
            entityManager.flush();

            // when
            Instant from = now.minus(10, ChronoUnit.DAYS);
            Instant to = now.plus(10, ChronoUnit.DAYS);
            Page<Transaction> page = repository.findByPurchasedAtBetween(from, to, PageRequest.of(0, 10));

            // then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getContent()).extracting("transactionId")
                    .containsExactlyInAnyOrder("tx_051", "tx_052");
        }

        @Test
        @DisplayName("유형별 트랜잭션 페이징 조회")
        void findByType() {
            // given
            Instant now = Instant.now();

            Transaction tx1 = createTransaction(1L, "tx_060", Transaction.Type.PURCHASE, 9.99, now);
            Transaction tx2 = createTransaction(2L, "tx_061", Transaction.Type.PURCHASE, 9.99, now);
            Transaction tx3 = createTransaction(3L, "tx_062", Transaction.Type.RENEWAL, 9.99, now);

            entityManager.persist(tx1);
            entityManager.persist(tx2);
            entityManager.persist(tx3);
            entityManager.flush();

            // when
            Page<Transaction> page = repository.findByType(Transaction.Type.PURCHASE, PageRequest.of(0, 10));

            // then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getContent()).extracting("type").containsOnly(Transaction.Type.PURCHASE);
        }
    }

    @Nested
    @DisplayName("복잡한 쿼리")
    class ComplexQueries {

        @Test
        @DisplayName("구독의 마지막 갱신 트랜잭션 조회")
        void findLastRenewalBySubscriptionId() {
            // given
            Instant now = Instant.now();

            Transaction purchase = createTransaction(1L, "tx_070", Transaction.Type.PURCHASE, 9.99, now.minus(30, ChronoUnit.DAYS));
            Transaction renewal1 = createTransaction(1L, "tx_071", Transaction.Type.RENEWAL, 9.99, now.minus(15, ChronoUnit.DAYS));
            Transaction renewal2 = createTransaction(1L, "tx_072", Transaction.Type.RENEWAL, 9.99, now);

            entityManager.persist(purchase);
            entityManager.persist(renewal1);
            entityManager.persist(renewal2);
            entityManager.flush();

            // when
            Optional<Transaction> lastRenewal = repository.findLastRenewalBySubscriptionId(1L);

            // then
            assertThat(lastRenewal).isPresent();
            assertThat(lastRenewal.get().getTransactionId()).isEqualTo("tx_072");
        }

        @Test
        @DisplayName("갱신 트랜잭션이 없으면 빈 Optional 반환")
        void findLastRenewalWhenNone() {
            // given
            Instant now = Instant.now();

            Transaction purchase = createTransaction(1L, "tx_080", Transaction.Type.PURCHASE, 9.99, now);
            entityManager.persist(purchase);
            entityManager.flush();

            // when
            Optional<Transaction> lastRenewal = repository.findLastRenewalBySubscriptionId(1L);

            // then
            assertThat(lastRenewal).isEmpty();
        }
    }

    @Nested
    @DisplayName("비즈니스 시나리오")
    class BusinessScenarios {

        @Test
        @DisplayName("멱등성: 동일 트랜잭션 ID 중복 저장 방지")
        void idempotencyCheck() {
            // given
            Instant purchasedAt = Instant.now();
            Transaction transaction = createTransaction(1L, "tx_090", Transaction.Type.PURCHASE, 9.99, purchasedAt);
            entityManager.persist(transaction);
            entityManager.flush();

            // when
            boolean exists = repository.existsByTransactionId("tx_090");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("환불 트랜잭션 식별")
        void identifyRefundTransaction() {
            // given
            Instant purchasedAt = Instant.now();
            Transaction refund = createTransaction(1L, "tx_100", Transaction.Type.REFUND, 9.99, purchasedAt);
            Transaction saved = entityManager.persist(refund);
            entityManager.flush();

            // when
            Transaction found = repository.findById(saved.getId()).orElseThrow();

            // then
            assertThat(found.isRefund()).isTrue();
        }
    }
}
