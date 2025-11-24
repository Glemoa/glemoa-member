package hyunsub.glemoamember.keyword.repository;

import hyunsub.glemoamember.keyword.domain.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    // 키워드 내림차순으로 전부 조회
    List<Keyword> findAllByOrderByIdDesc();
    // 특정 사용자가 설정한 키워드 전부 조회
    List<Keyword> findByMemberId(Long memberId);
    // 특정 사용자의 키워드들 조회
    Optional<Keyword> findByMemberIdAndKeywordName(Long memberId, String keywordName);
    // 특정 사용자가 해당 키워드를 저장 해놓았는지 체크
    boolean existsByMemberIdAndKeywordName(Long memberId, String keywordName);
}
