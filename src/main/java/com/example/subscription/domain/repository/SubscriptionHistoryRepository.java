package com.example.subscription.domain.repository;

import com.example.subscription.domain.entity.SubscriptionHistory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 구독 이력 Repository.
 *
 * <p>구독 상태 변경 이력의 영속성을 관리한다.
 */
@Repository
public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {

    /**
     * 사용자 ID로 구독 이력을 조회한다.
     *
     * @param subscriptionId 구독 ID
     * @return 구독 이력 목록
     */
    List<SubscriptionHistory> findBySubscriptionIdOrderByChangedAtDesc(Long subscriptionId);

    /**
     * 사용자 ID로 구독 이력을 페이징하여 조회한다.
     *
     * @param subscriptionId 구독 ID
     * @param pageable 페이징 정보
     * @return 구독 이력 페이지
     */
    Page<SubscriptionHistory> findBySubscriptionIdOrderByChangedAtDesc(Long subscriptionId, Pageable pageable);
}
