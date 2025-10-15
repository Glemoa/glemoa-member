package hyunsub.glemoamember.bookmark.repository;

import hyunsub.glemoamember.bookmark.domain.BookMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    List<BookMark> findBookMarkByMemberId(Long memberId);
    // 특정 memberId가 저장한 모든 북마크를 찾고, 그 결과를 createdTime을 기준으로 내림차순 정렬하여 List로 반환합니다.
    List<BookMark> findByMemberIdOrderByCreatedTimeDesc(Long memberId);
    Optional<BookMark> findByPostIdAndMemberId(Long postId, Long memberId);

    @Query(value = "SELECT * FROM book_mark WHERE member_id = :memberId ORDER BY created_time DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<BookMark> findByMemberIdWithPagination(@Param("memberId") Long memberId, @Param("offset") Long offset, @Param("limit") Long limit);

    long countByMemberId(Long memberId);
}

