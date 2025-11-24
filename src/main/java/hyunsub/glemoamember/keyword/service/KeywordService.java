package hyunsub.glemoamember.keyword.service;

import hyunsub.glemoamember.keyword.domain.Keyword;
import hyunsub.glemoamember.keyword.dto.KeywordReqDto;
import hyunsub.glemoamember.keyword.dto.KeywordResDto;
import hyunsub.glemoamember.keyword.repository.KeywordRepository;
import hyunsub.glemoamember.member.domain.Member;
import hyunsub.glemoamember.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class KeywordService {
    private final KeywordRepository keywordRepository;
    private final MemberRepository memberRepository;

    public KeywordService(KeywordRepository keywordRepository, MemberRepository memberRepository) {
        this.keywordRepository = keywordRepository;
        this.memberRepository = memberRepository;
    }

    public void save(Long xUserId, KeywordReqDto keywordReqDto) {
        // 1. 멤버 id로 멤버 조회
        Member member = memberRepository.findById(xUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        boolean isExist = keywordRepository.existsByMemberIdAndKeywordName(xUserId, keywordReqDto.getKeywordName());

        if (isExist) {
            // 중복일 경우 예외 발생 (메시지는 프론트엔드에 보여줄 내용)
            throw new IllegalStateException("이미 등록된 키워드입니다.");
        }

        // 2. 키워드 엔티티 생성
        Keyword keyword = Keyword.builder()
                .member(member)
                .keywordName(keywordReqDto.getKeywordName())
                .build();

        keywordRepository.save(keyword);
    }

    public List<KeywordResDto> getAllKeywordsByMember(Long xUserId) {
        List<Keyword> keywords = keywordRepository.findByMemberId(xUserId);
        List<KeywordResDto> keywordResDtos = new ArrayList<>();

        // keyword 테이블에 아무것도 저장되어 있지 않다면 빈 키워드 리스트 반환
        if(keywords.isEmpty()) {
            return keywordResDtos;
        }

        for(Keyword keyword : keywords) {
            keywordResDtos.add(
                    KeywordResDto.builder()
                            .keywordId(keyword.getId())
                            .memberId(keyword.getMember().getId())
                            .keywordName(keyword.getKeywordName())
                            .build()
            );
        }
        return keywordResDtos;
    }

    public void delete(String keywordName, Long xUserId) {
        Keyword keyword = keywordRepository.findByMemberIdAndKeywordName(xUserId, keywordName)
                .orElseThrow(() -> new IllegalArgumentException("자신이 등록한 키워드만 삭제할 수 있습니다."));

        keywordRepository.delete(keyword);
    }

    public List<KeywordResDto> getAllKeywords() {
        List<Keyword> keywords = keywordRepository.findAllByOrderByIdDesc();
        List<KeywordResDto> keywordResDtos = new ArrayList<>();

        // keyword 테이블에 아무것도 저장되어 있지 않다면 빈 키워드 리스트 반환
        if(keywords.isEmpty()) {
            return keywordResDtos;
        }

        for(Keyword keyword : keywords) {
            keywordResDtos.add(
                    KeywordResDto.builder()
                            .keywordId(keyword.getId())
                            .memberId(keyword.getMember().getId())
                            .keywordName(keyword.getKeywordName())
                            .build()
            );
        }
        return keywordResDtos;
    }
}
