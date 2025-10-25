package hyunsub.glemoamember.member.service;

import hyunsub.glemoamember.member.domain.Member;
import hyunsub.glemoamember.member.dto.GoogleProfileDto;
import hyunsub.glemoamember.member.dto.KakaoProfileDto;
import hyunsub.glemoamember.member.dto.MemberLoginReqDto;
import hyunsub.glemoamember.member.dto.MemberSaveReqDto;
import hyunsub.glemoamember.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Member> findByEmail(String memberEmail) {
        return memberRepository.findByEmail(memberEmail);
    }

    public Optional<Member> findBySocialId(String socialId) {
        return memberRepository.findBySocialId(socialId);
    }

    public Long saveMember(MemberSaveReqDto dto) {
        Optional<Member> optionalMember = findByEmail(dto.getEmail());
        if(optionalMember.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        String encodePassword = passwordEncoder.encode(dto.getPassword());
        Member member = memberRepository.save(dto.toEntity(encodePassword));

        return member.getId();
    }

    public Member loginMemberWithGoogle(GoogleProfileDto dto) {
        Optional<Member> optionalMember = findBySocialId(dto.getSub());

        if(optionalMember.isEmpty()) {
            // 회원가입이 되어 있지 않다면 회원가입을 시켜준다.
            Member member = memberRepository.save(dto.toEntity(dto.getSub()));
            return member;
        }

        return optionalMember.get();
    }

    public Member loginMemberWithKakao(KakaoProfileDto dto) {
        Optional<Member> optionalMember = findBySocialId(dto.getId());

        if(optionalMember.isEmpty()) {
            // 회원가입이 되어 있지 않다면 회원가입을 시켜준다.
            Member member = memberRepository.save(dto.toEntity(dto.getId()));
            return member;
        }

        return optionalMember.get();
    }
    
    public Member loginMember(MemberLoginReqDto dto) {
        // 존재하는 이메일인지 체크
        Optional<Member> optionalMember = findByEmail(dto.getEmail());
        if(optionalMember.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 이메일입니다.");
        }
        // 비밀번호 일치하는 지 체크
        if(passwordEncoder.matches(dto.getPassword(),optionalMember.get().getPassword())) {
            return optionalMember.get();
        }
        // 일치하지 않으면
        throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
}