package hyunsub.glemoamember.member.controller;

import hyunsub.glemoamember.common.JwtTokenProvider;
import hyunsub.glemoamember.member.domain.Member;
import hyunsub.glemoamember.member.domain.Role;
import hyunsub.glemoamember.member.dto.*;
import hyunsub.glemoamember.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

    @Qualifier("rtdb")
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

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
        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), Role.USER.toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), Role.USER.toString());
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken);

        return new ResponseEntity<>(new MemberLoginResDto(accessToken, refreshToken), HttpStatus.OK);
    }

    @PostMapping("/member/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenReqDto dto) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(dto.getRefreshToken())
                .getBody();

        Object refreshToken = redisTemplate.opsForValue().get(claims.getSubject());

        // 리프레시 토큰이 일치하지 않는다면 jwt 재발급 X
        if(refreshToken == null || !refreshToken.toString().equals(dto.getRefreshToken())){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtTokenProvider.createAccessToken(claims.getSubject(), Role.USER.toString());

        return new ResponseEntity<>(
                new AccessTokenReissueResDto(accessToken), HttpStatus.OK);
    }
}