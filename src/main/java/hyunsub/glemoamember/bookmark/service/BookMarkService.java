package hyunsub.glemoamember.bookmark.service;

import hyunsub.glemoamember.bookmark.domain.BookMark;
import hyunsub.glemoamember.bookmark.dto.BookmarkPageResDto;
import hyunsub.glemoamember.bookmark.dto.PostDto;
import hyunsub.glemoamember.bookmark.dto.PostIdDto;
import hyunsub.glemoamember.bookmark.repository.BookMarkRepository;
import hyunsub.glemoamember.member.domain.Member;
import hyunsub.glemoamember.member.repository.MemberRepository;
import hyunsub.glemoamember.member.service.ReaderFeign;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookMarkService {
    private final BookMarkRepository bookMarkRepository;
    private final MemberRepository memberRepository;
    private final ReaderFeign readerFeign;

    public BookMarkService(BookMarkRepository bookMarkRepository,
                           MemberRepository memberRepository, ReaderFeign readerFeign) {
        this.bookMarkRepository = bookMarkRepository;
        this.memberRepository = memberRepository;
        this.readerFeign = readerFeign;
    }

    public void doBookMark(PostIdDto dto, String xUserId) {
        Optional<Member> optionalMember = memberRepository.findById(Long.parseLong(xUserId));

        if (optionalMember.isEmpty()) {
            throw new RuntimeException("Member not found");
        }

        Member member = optionalMember.get();

        Optional<BookMark> optionalBookMark = bookMarkRepository.findByPostIdAndMemberId(dto.getPostId(), member.getId());

        if (optionalBookMark.isPresent()) {
            // 즐겨찾기가 이미 존재 한다면 삭제
            bookMarkRepository.delete(optionalBookMark.get());
        } else {
            // 즐겨찾기가 없다면 추가
            PostDto PostDto = readerFeign.findByPostId(dto.getPostId());
            if (PostDto == null) {
                throw new RuntimeException("Post not found");
            }
            BookMark bookMark = BookMark.builder()
                    .postId(dto.getPostId())
                    .member(member)
                    .build();
            bookMarkRepository.save(bookMark);
        }
    }

    public List<Long> viewBookMarkedPostId(String xUserId) {
        List<BookMark> bookMarks = bookMarkRepository.findBookMarkByMemberId(Long.parseLong(xUserId));

        List<Long> postIdList = new ArrayList<>();

        for(BookMark bookMark : bookMarks) {
            postIdList.add(bookMark.getPostId());
        }

        return postIdList;
    }

    public BookmarkPageResDto viewBookMarkedPost(String xUserId, Long page, Long pageSize, Long movablePageCount) {
        Long memberId = Long.parseLong(xUserId);
        // 1. DB에서 페이지에 해당하는 북마크 목록만 조회합니다.
        List<BookMark> bookMarks = bookMarkRepository.findByMemberIdWithPagination(memberId, (page - 1) * pageSize, pageSize);

        List<Long> postIdList = new ArrayList<>();
        for(BookMark bookMark : bookMarks) {
            postIdList.add(bookMark.getPostId());
        }

        // 2. reader 서비스에 페이지에 해당하는 postId 목록만 보내서 게시물 상세 정보를 받아옵니다.
        List<PostDto> postDtos = new ArrayList<>();
        if (!postIdList.isEmpty()) {
            postDtos = readerFeign.viewBookMarkedPostByPostIdList(postIdList);
        }

        // 3. 페이지 계산을 위해 전체 북마크 개수를 조회합니다.
        Long totalBookMarks = bookMarkRepository.countByMemberId(memberId);

        // 4. 최종 결과를 DTO에 담아 반환합니다.
        return BookmarkPageResDto.builder()
                .postDtoList(postDtos)
                .postCount(totalBookMarks)
                .build();
    }
}
