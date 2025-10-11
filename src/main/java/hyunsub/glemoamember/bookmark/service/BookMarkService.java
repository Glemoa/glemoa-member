package hyunsub.glemoamember.bookmark.service;

import hyunsub.glemoamember.bookmark.domain.BookMark;
import hyunsub.glemoamember.bookmark.dto.PostIdDto;
import hyunsub.glemoamember.bookmark.dto.PostDto;
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
    private final BookMarkRepository bokBookMarkRepository;
    private final MemberRepository memberRepository;
    private final ReaderFeign readerFeign;

    public BookMarkService(BookMarkRepository bokBookMarkRepository,
                           MemberRepository memberRepository, ReaderFeign readerFeign) {
        this.bokBookMarkRepository = bokBookMarkRepository;
        this.memberRepository = memberRepository;
        this.readerFeign = readerFeign;
    }

    public void doBookMark(PostIdDto dto, String xUserId) {
        Optional<Member> optionalMember = memberRepository.findById(Long.parseLong(xUserId));

        if (optionalMember.isEmpty()) {
            throw new RuntimeException("Member not found");
        }

        Member member = optionalMember.get();

        Optional<BookMark> optionalBookMark = bokBookMarkRepository.findByPostIdAndMemberId(dto.getPostId(), member.getId());

        if (optionalBookMark.isPresent()) {
            // 즐겨찾기가 이미 존재 한다면 삭제
            bokBookMarkRepository.delete(optionalBookMark.get());
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
            bokBookMarkRepository.save(bookMark);
        }
    }

    public List<Long> viewBookMarkedPostId(String xUserId) {
        List<BookMark> bookMarks = bokBookMarkRepository.findBookMarkByMemberId(Long.parseLong(xUserId));

        List<Long> postIdList = new ArrayList<>();

        for(BookMark bookMark : bookMarks) {
            postIdList.add(bookMark.getPostId());
        }

        return postIdList;
    }

    public List<PostDto> viewBookMarkedPost(String xUserId) {
        List<BookMark> bookMarks = bokBookMarkRepository.findByMemberIdOrderByCreatedTimeDesc(Long.parseLong(xUserId));

        List<Long> postIdList = new ArrayList<>();

        for(BookMark bookMark : bookMarks) {
            postIdList.add(bookMark.getPostId());
        }

        List<PostDto> postDtos = readerFeign.viewBookMarkedPostByPostIdList(postIdList);

        return postDtos;
    }
}
