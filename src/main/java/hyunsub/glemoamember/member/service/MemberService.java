package hyunsub.glemoamember.member.service;

import hyunsub.glemoamember.member.domain.Member;
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

    public Long saveMember(MemberSaveReqDto dto) {
        Optional<Member> optionalMember = findByEmail(dto.getEmail());
        if(optionalMember.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        String encodePassword = passwordEncoder.encode(dto.getPassword());
        Member member = memberRepository.save(dto.toEntity(encodePassword));

        return member.getId();
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