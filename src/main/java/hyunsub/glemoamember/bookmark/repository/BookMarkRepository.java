package hyunsub.glemoamember.bookmark.repository;

import hyunsub.glemoamember.bookmark.domain.BookMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    List<BookMark> findBookMarkByMemberId(Long memberId);
}
