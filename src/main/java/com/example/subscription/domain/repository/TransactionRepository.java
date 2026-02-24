package com.example.subscription.domain.repository;

import com.example.subscription.domain.entity.Transaction;
import com.example.subscription.domain.entity.Transaction.Type;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 트랜잭션 Repository.
 *
 * <p>결제/갱신 트랜잭션의 영속성을 관리한다. 이력 추적 및 정산 등에 활용된다.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Apple 트랜잭션 ID로 조회한다.
     *
     * <p>멱등성 처리 시 중복 트랜잭션 확인에 사용된다.
     *
     * @param transactionId Apple 트랜잭션 ID
     * @return 조회된 트랜잭션 (없으면 empty)
     */
    Optional<Transaction> findByTransactionId(String transactionId);

    /**
     * 구독 ID로 모든 트랜잭션을 조회한다.
     *
     * @param subscriptionId 구독 ID
     * @return 해당 구독의 모든 트랜잭션
     */
    List<Transaction> findBySubscriptionId(Long subscriptionId);

    /**
     * 구독 ID로 트랜잭션을 시간순으로 조회한다.
     *
     * @param subscriptionId 구독 ID
     * @param pageable 페이징 정보
     * @return 트랜잭션 목록 (최신순)
     */
    @Query("SELECT t FROM Transaction t WHERE t.subscriptionId = :subscriptionId ORDER BY t.purchasedAt DESC")
    Page<Transaction> findBySubscriptionIdOrderByPurchasedAtDesc(
            @Param("subscriptionId") Long subscriptionId, Pageable pageable);

    /**
     * 특정 기간 내 트랜잭션을 조회한다.
     *
     * <p>정산 및 통계 집계에 활용된다.
     *
     * @param from 시작 일시
     * @param to 종료 일시
     * @param pageable 페이징 정보
     * @return 해당 기간의 트랜잭션
     */
    @Query("SELECT t FROM Transaction t WHERE t.purchasedAt BETWEEN :from AND :to")
    Page<Transaction> findByPurchasedAtBetween(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    /**
     * 특정 유형의 트랜잭션을 조회한다.
     *
     * @param type 트랜잭션 유형
     * @param pageable 페이징 정보
     * @return 해당 유형의 트랜잭션
     */
    Page<Transaction> findByType(Type type, Pageable pageable);

    /**
     * Apple 트랜잭션 ID 존재 여부를 확인한다.
     *
     * @param transactionId Apple 트랜잭션 ID
     * @return 존재하면 true
     */
    boolean existsByTransactionId(String transactionId);

    /**
     * 구독의 마지막 갱신 트랜잭션을 조회한다.
     *
     * @param subscriptionId 구독 ID
     * @return 마지막 갱신 트랜잭션
     */
    @Query("SELECT t FROM Transaction t WHERE t.subscriptionId = :subscriptionId AND t.type = 'RENEWAL' ORDER BY t.purchasedAt DESC LIMIT 1")
    Optional<Transaction> findLastRenewalBySubscriptionId(@Param("subscriptionId") Long subscriptionId);
}
