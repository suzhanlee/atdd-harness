package com.example.subscription.domain.repository;

import com.example.subscription.domain.entity.Subscription;
import com.example.subscription.domain.vo.SubscriptionStatus;
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
 * 구독 Repository.
 *
 * <p>구독 Aggregate Root의 영속성을 관리한다. 기본 CRUD는 JpaRepository를 상속받아 제공하며, 도메인 특화 조회 메서드를
 * 추가로 제공한다.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Apple 원본 트랜잭션 ID로 구독을 조회한다.
     *
     * <p>비즈니스 키 기반 조회로, Webhook 처리 시 사용된다.
     *
     * @param originalTransactionId Apple 원본 트랜잭션 ID
     * @return 조회된 구독 (없으면 empty)
     */
    Optional<Subscription> findByOriginalTransactionId(String originalTransactionId);

    /**
     * 사용자 ID로 활성 구독을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 활성 구독 목록
     */
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.status = :status")
    List<Subscription> findActiveByUserId(@Param("userId") Long userId, @Param("status") SubscriptionStatus status);

    /**
     * 만료 예정인 구독을 조회한다.
     *
     * <p>만료 알림 발송 등에 활용된다.
     *
     * @param from 조회 시작 일시
     * @param to 조회 종료 일시
     * @param pageable 페이징 정보
     * @return 만료 예정 구독 목록
     */
    @Query("SELECT s FROM Subscription s WHERE s.expiresAt BETWEEN :from AND :to AND s.status = 'ACTIVE'")
    Page<Subscription> findExpiringBetween(
            @Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    /**
     * 특정 상태의 구독을 조회한다.
     *
     * @param status 구독 상태
     * @param pageable 페이징 정보
     * @return 해당 상태의 구독 목록
     */
    Page<Subscription> findByStatus(SubscriptionStatus status, Pageable pageable);

    /**
     * 사용자 ID로 모든 구독을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 모든 구독
     */
    List<Subscription> findByUserId(Long userId);

    /**
     * Apple 원본 트랜잭션 ID 존재 여부를 확인한다.
     *
     * @param originalTransactionId Apple 원본 트랜잭션 ID
     * @return 존재하면 true
     */
    boolean existsByOriginalTransactionId(String originalTransactionId);

    /**
     * 사용자 ID와 상태로 구독을 조회한다.
     *
     * @param userId 사용자 ID
     * @param status 구독 상태
     * @return 해당 상태의 구독 목록
     */
    List<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
}
