package com.example.subscription.domain.repository;

import com.example.subscription.domain.entity.Refund;
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
 * 환불 Repository.
 *
 * <p>환불 정보의 영속성을 관리한다. 환불 이력 조회 및 정산 등에 활용된다.
 */
@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    /**
     * 구독 ID로 환불 정보를 조회한다.
     *
     * @param subscriptionId 구독 ID
     * @return 해당 구독의 환불 정보 (없으면 empty)
     */
    Optional<Refund> findBySubscriptionId(Long subscriptionId);

    /**
     * Apple 원본 트랜잭션 ID로 환불 정보를 조회한다.
     *
     * @param originalTransactionId Apple 원본 트랜잭션 ID
     * @return 환불 정보 (없으면 empty)
     */
    Optional<Refund> findByOriginalTransactionId(String originalTransactionId);

    /**
     * 특정 기간 내 환불을 조회한다.
     *
     * <p>정산 및 통계 집계에 활용된다.
     *
     * @param from 시작 일시
     * @param to 종료 일시
     * @param pageable 페이징 정보
     * @return 해당 기간의 환불 목록
     */
    @Query("SELECT r FROM Refund r WHERE r.refundedAt BETWEEN :from AND :to")
    Page<Refund> findByRefundedAtBetween(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    /**
     * 구독 ID로 환불 여부를 확인한다.
     *
     * @param subscriptionId 구독 ID
     * @return 환불되었으면 true
     */
    boolean existsBySubscriptionId(Long subscriptionId);

    /**
     * 특정 사유의 환불을 조회한다.
     *
     * @param reason 환불 사유
     * @param pageable 페이징 정보
     * @return 해당 사유의 환불 목록
     */
    Page<Refund> findByReason(Refund.Reason reason, Pageable pageable);
}
