package hyunsub.glemoamember.notification.repository;

import hyunsub.glemoamember.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 특정 사용자의 알림 목록을 최신순으로 조회
    List<Notification> findByMemberIdOrderByCreatedTimeDesc(Long memberId);

    // 특정 사용자의 읽지 않은 알림 *개수* 조회
    Long countByMemberIdAndIsReadFalse(Long memberId);

    // 특정 사용자의 특정 알림을 읽음 처리
    @Query(
            value = "update notification " +
                    "set is_read = true " +
                    "where post_id = :postId and member_id = :memberId",
            nativeQuery = true
    )
    @Modifying
    int updateReadNotification(
            @Param("memberId") Long memberId,
            @Param("postId") Long postId
    );

    // 특정 사용자의 모든 알림을 읽음 처리
    @Query(
            value = "update notification " +
                    "set is_read = true " +
                    "where member_id = :memberId",
            nativeQuery = true
    )
    @Modifying
    int updateReadAllNotification(
            @Param("memberId") Long memberId
    );

    // 특정 사용자가 읽은 특정 알림을 삭제
    @Query(
            value = "delete " +
                    "from notification " +
                    "where post_id = :postId and member_id = :memberId and is_read = true ",
            nativeQuery = true
    )
    @Modifying
    int deleteNotification(
            @Param("memberId") Long memberId,
            @Param("postId") Long postId
    );

    // 특정 사용자가 읽은 모든 알림을 삭제
    @Query(
            value = "delete " +
                    "from notification " +
                    "where member_id = :memberId and is_read = true ",
            nativeQuery = true
    )
    @Modifying
    int deleteAllNotification(
            @Param("memberId") Long memberId
    );

    // 특정 사용자의 읽은 알림 최신순으로 조회
    List<Notification> findByMemberIdAndIsReadTrueOrderByCreatedTimeDesc(Long memberId);

    // 특정 사용자의 읽지 않은 알림 최신순으로 조회
    List<Notification> findByMemberIdAndIsReadFalseOrderByCreatedTimeDesc(Long memberId);
}
