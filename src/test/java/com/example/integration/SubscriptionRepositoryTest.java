package com.example.integration;

import com.example.subscription.domain.entity.Subscription;
import com.example.subscription.domain.repository.SubscriptionRepository;
import com.example.subscription.domain.vo.Period;
import com.example.subscription.domain.vo.ProductId;
import com.example.subscription.domain.vo.SubscriptionStatus;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SubscriptionRepository 통합 테스트.
 *
 * <p>JPA 연동, 쿼리 메서드, 페이징 등을 검증한다.
 */
@DataJpaTest
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
@DisplayName("SubscriptionRepository 통합 테스트")
class SubscriptionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SubscriptionRepository repository;

    private Subscription createSubscription(String originalTxId, Long userId, String productId, Instant expiresAt) {
        Period period = new Period(Instant.now(), expiresAt);
        ProductId product = new ProductId(productId);
        return Subscription.create(originalTxId, userId, product, period);
    }

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    @DisplayName("기본 CRUD")
    class BasicCrud {

        @Test
        @DisplayName("구독 저장 및 ID로 조회")
        void saveAndFindById() {
            // given
            Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
            Subscription subscription = createSubscription("tx_001", 1L, "basic_001", expiresAt);

            // when
            Subscription saved = entityManager.persist(subscription);
            entityManager.flush();

            Optional<Subscription> found = repository.findById(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getOriginalTransactionId()).isEqualTo("tx_001");
            assertThat(found.get().getUserId()).isEqualTo(1L);
            assertThat(found.get().getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("구독 삭제")
        void delete() {
            // given
            Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
            Subscription subscription = createSubscription("tx_002", 1L, "basic_001", expiresAt);
            Subscription saved = entityManager.persist(subscription);
            entityManager.flush();

            // when
            repository.delete(saved);
            entityManager.flush();

            // then
            Optional<Subscription> found = repository.findById(saved.getId());
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("쿼리 메서드")
    class QueryMethods {

        @Test
        @DisplayName("원본 트랜잭션 ID로 조회")
        void findByOriginalTransactionId() {
            // given
            Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
            Subscription subscription = createSubscription("tx_003", 1L, "basic_001", expiresAt);
            entityManager.persist(subscription);
            entityManager.flush();

            // when
            Optional<Subscription> found = repository.findByOriginalTransactionId("tx_003");

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("사용자 ID로 활성 구독 조회")
        void findActiveByUserId() {
            // given
            Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

            // 활성 구독
            Subscription active1 = createSubscription("tx_010", 1L, "basic_001", expiresAt);
            Subscription active2 = createSubscription("tx_011", 1L, "pro_001", expiresAt);

            // 만료 구독
            Subscription expired = createSubscription("tx_012", 1L, "basic_001", expiresAt);
            expired.expire();

            entityManager.persist(active1);
            entityManager.persist(active2);
            entityManager.persist(expired);
            entityManager.flush();

            // when
            List<Subscription> actives = repository.findActiveByUserId(1L, SubscriptionStatus.ACTIVE);

            // then
            assertThat(actives).hasSize(2);
            assertThat(actives).extracting("originalTransactionId")
                    .containsExactlyInAnyOrder("tx_010", "tx_011");
        }

        @Test
        @DisplayName("사용자 ID로 모든 구독 조회")
        void findByUserId() {
            // given
            Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

            Subscription sub1 = createSubscription("tx_020", 1L, "basic_001", expiresAt);
            Subscription sub2 = createSubscription("tx_021", 1L, "pro_001", expiresAt);
            Subscription sub3 = createSubscription("tx_022", 2L, "basic_001", expiresAt);

            entityManager.persist(sub1);
            entityManager.persist(sub2);
            entityManager.persist(sub3);
            entityManager.flush();

            // when
            List<Subscription> user1Subs = repository.findByUserId(1L);

            // then
            assertThat(user1Subs).hasSize(2);
            assertThat(user1Subs).extracting("userId").containsOnly(1L);
        }

        @Test
        @DisplayName("원본 트랜잭션 ID 존재 여부 확인")
        void existsByOriginalTransactionId() {
            // given
            Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
            Subscription subscription = createSubscription("tx_030", 1L, "basic_001", expiresAt);
            entityManager.persist(subscription);
            entityManager.flush();

            // when & then
            assertThat(repository.existsByOriginalTransactionId("tx_030")).isTrue();
            assertThat(repository.existsByOriginalTransactionId("tx_999")).isFalse();
        }
    }

    @Nested
    @DisplayName("페이징 및 정렬")
    class PagingAndSorting {

        @Test
        @DisplayName("만료 예정 구독 페이징 조회")
        void findExpiringBetween() {
            // given
            Instant now = Instant.now();
            Instant expiresAt1 = now.plus(5, ChronoUnit.DAYS);
            Instant expiresAt2 = now.plus(10, ChronoUnit.DAYS);
            Instant expiresAt3 = now.plus(20, ChronoUnit.DAYS); // 범위 밖

            Subscription sub1 = createSubscription("tx_040", 1L, "basic_001", expiresAt1);
            Subscription sub2 = createSubscription("tx_041", 2L, "basic_001", expiresAt2);
            Subscription sub3 = createSubscription("tx_042", 3L, "basic_001", expiresAt3);

            entityManager.persist(sub1);
            entityManager.persist(sub2);
            entityManager.persist(sub3);
            entityManager.flush();

            // when
            Instant from = now.plus(1, ChronoUnit.DAYS);
            Instant to = now.plus(15, ChronoUnit.DAYS);
            Page<Subscription> page = repository.findExpiringBetween(from, to, PageRequest.of(0, 10));

            // then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.getTotalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("상태별 구독 페이징 조회")
        void findByStatus() {
            // given
            Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

            Subscription active1 = createSubscription("tx_050", 1L, "basic_001", expiresAt);
            Subscription active2 = createSubscription("tx_051", 2L, "basic_001", expiresAt);

            Subscription expired = createSubscription("tx_052", 3L, "basic_001", expiresAt);
            expired.expire();

            entityManager.persist(active1);
            entityManager.persist(active2);
            entityManager.persist(expired);
            entityManager.flush();

            // when
            Page<Subscription> activePage = repository.findByStatus(SubscriptionStatus.ACTIVE, PageRequest.of(0, 10));

            // then
            assertThat(activePage.getContent()).hasSize(2);
            assertThat(activePage.getContent()).extracting("status").containsOnly(SubscriptionStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("낙관적 락")
    class OptimisticLocking {

        @Test
        @DisplayName("버전 필드 자동 증가")
        void versionAutoIncrement() {
            // given
            Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
            Subscription subscription = createSubscription("tx_060", 1L, "basic_001", expiresAt);
            Subscription saved = entityManager.persist(subscription);
            entityManager.flush();

            Long initialVersion = saved.getVersion();

            // when
            saved.renew(expiresAt.plus(30, ChronoUnit.DAYS), Instant.now(), expiresAt.plus(30, ChronoUnit.DAYS));
            entityManager.flush();
            entityManager.clear();

            Subscription found = repository.findById(saved.getId()).orElseThrow();

            // then
            assertThat(found.getVersion()).isGreaterThan(initialVersion);
        }
    }

    @Nested
    @DisplayName("복잡한 시나리오")
    class ComplexScenarios {

        @Test
        @DisplayName("구독 상태 변경 이력 추적")
        void subscriptionStateChanges() {
            // given
            Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
            Subscription subscription = createSubscription("tx_070", 1L, "basic_001", expiresAt);
            Subscription saved = entityManager.persist(subscription);
            entityManager.flush();

            // when - 상태 변경
            saved.changeStatus(SubscriptionStatus.GRACE_PERIOD);
            entityManager.flush();
            entityManager.clear();

            // then
            Subscription found = repository.findById(saved.getId()).orElseThrow();
            assertThat(found.getStatus()).isEqualTo(SubscriptionStatus.GRACE_PERIOD);
        }

        @Test
        @DisplayName("업그레이드 후 제품 ID 변경 확인")
        void upgradeChangesProductId() {
            // given
            Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
            Subscription subscription = createSubscription("tx_080", 1L, "basic_001", expiresAt);
            Subscription saved = entityManager.persist(subscription);
            entityManager.flush();

            // when
            ProductId newProductId = new ProductId("pro_001");
            saved.upgrade(newProductId);
            entityManager.flush();
            entityManager.clear();

            // then
            Subscription found = repository.findById(saved.getId()).orElseThrow();
            assertThat(found.getProductId().getValue()).isEqualTo("pro_001");
        }
    }
}
