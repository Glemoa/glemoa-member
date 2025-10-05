package hyunsub.glemoamember.member.controller;

import hyunsub.glemoamember.common.JwtTokenProvider;
import hyunsub.glemoamember.member.domain.Member;
import hyunsub.glemoamember.member.domain.Role;
import hyunsub.glemoamember.member.dto.MemberLoginReqDto;
import hyunsub.glemoamember.member.dto.MemberLoginResDto;
import hyunsub.glemoamember.member.dto.MemberSaveReqDto;
import hyunsub.glemoamember.member.service.MemberService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider, RedisTemplate<String, Object> redisTemplate) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/member/doSave")
    public ResponseEntity<?> doSaveMember(@RequestBody MemberSaveReqDto dto) {
        Long id = memberService.saveMember(dto);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    @PostMapping("/member/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginReqDto dto) {
        Member member = memberService.loginMember(dto);
        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), Role.USER.toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), Role.USER.toString());
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken);

        return new ResponseEntity<>(new MemberLoginResDto(accessToken, refreshToken), HttpStatus.OK);
    }
}