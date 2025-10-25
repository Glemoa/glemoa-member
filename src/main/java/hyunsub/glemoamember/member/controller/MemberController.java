package hyunsub.glemoamember.member.controller;

import hyunsub.glemoamember.common.JwtTokenProvider;
import hyunsub.glemoamember.member.domain.Member;
import hyunsub.glemoamember.member.domain.Role;
import hyunsub.glemoamember.member.dto.*;
import hyunsub.glemoamember.member.service.GoogleService;
import hyunsub.glemoamember.member.service.KakaoService;
import hyunsub.glemoamember.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final GoogleService googleService;
    private final KakaoService kakaoService;
    private final JwtTokenProvider jwtTokenProvider;

    @Qualifier("rtdb")
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    public MemberController(MemberService memberService, GoogleService googleService, KakaoService kakaoService,
                            JwtTokenProvider jwtTokenProvider, RedisTemplate<String, Object> redisTemplate) {
        this.memberService = memberService;
        this.googleService = googleService;
        this.kakaoService = kakaoService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/doSave")
    public ResponseEntity<?> doSaveMember(@RequestBody MemberSaveReqDto dto) {
        Long id = memberService.saveMember(dto);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginReqDto dto) {
        Member member = memberService.loginMember(dto);

        String accessToken = jwtTokenProvider.createAccessToken(
                member.getId().toString(), member.getEmail(), Role.USER.toString());

        String refreshToken = jwtTokenProvider.createRefreshToken(
                member.getId().toString(), member.getEmail(), Role.USER.toString());

        redisTemplate.opsForValue().set(member.getEmail(), refreshToken);

        return new ResponseEntity<>(new MemberLoginResDto(accessToken, refreshToken), HttpStatus.OK);
    }

    @PostMapping("/google/doLogin")
    public ResponseEntity<?> doLoginWithGoogle(@RequestBody AuthorizationCodeDto autoAuthorizationCodeDto) {
        log.info("Google 인가 코드: {}", autoAuthorizationCodeDto.getCode());

        // 1. accessToken 구글로부터 발급
        // getAccessToken을 할 때 사실 구글에서 토큰의 값 뿐만 아니라 여러가지 정보들을 준다.
        // 토큰의 만료 시간등이라던가 scope, 토큰의 타입 등... 추후에 활용을 할 수 있도록 String 말고 DTO로 만들어두기
        SocialLoginAccessTokenDto googleAccessTokenDto = googleService.getAccessToken(autoAuthorizationCodeDto.getCode());

        // 2. 사용자 프로필 얻기
        GoogleProfileDto googleProfileDto = googleService.getGoogleProfile(googleAccessTokenDto.getAccess_token());

        // 3. 로그인 -> 만약 회원가입이 되어 있지 않다면 회원가입
        Member member = memberService.loginMemberWithGoogle(googleProfileDto);
        log.info("로그인 성공");

        String accessToken = jwtTokenProvider.createAccessToken(
                member.getId().toString(), member.getEmail(), Role.USER.toString());

        String refreshToken = jwtTokenProvider.createRefreshToken(
                member.getId().toString(), member.getEmail(), Role.USER.toString());

        redisTemplate.opsForValue().set(member.getEmail(), refreshToken);

        return new ResponseEntity<>(new MemberLoginResDto(accessToken, refreshToken), HttpStatus.OK);
    }

    @PostMapping("/kakao/doLogin")
    public ResponseEntity<?> doLoginWithKakao(@RequestBody AuthorizationCodeDto autoAuthorizationCodeDto) {
        log.info(autoAuthorizationCodeDto.getCode());

        // 1. accessToken 얻기
        SocialLoginAccessTokenDto accessTokenDto = kakaoService.getAccessToken(autoAuthorizationCodeDto.getCode());

        // 2. 사용자 프로필 얻기
        KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());

        // 3. 로그인 -> 만약 회원가입이 되어 있지 않다면 회원가입
        Member member = memberService.loginMemberWithKakao(kakaoProfileDto);
        log.info("로그인 성공");

        String accessToken = jwtTokenProvider.createAccessToken(
                member.getId().toString(), member.getEmail(), Role.USER.toString());

        String refreshToken = jwtTokenProvider.createRefreshToken(
                member.getId().toString(), member.getEmail(), Role.USER.toString());

        redisTemplate.opsForValue().set(member.getEmail(), refreshToken);

        return new ResponseEntity<>(new MemberLoginResDto(accessToken, refreshToken), HttpStatus.OK);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenReqDto dto) {
        /*
            1. 리프레시 토큰이 유효한지 확인
            2. 리프레시 토큰의 claims 추출
         */
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(dto.getRefreshToken())
                .getBody();

        Object refreshToken = redisTemplate.opsForValue().get(claims.get("email", String.class));

        // 리프레시 토큰이 일치하지 않는다면 jwt 재발급 X
        if(refreshToken == null || !refreshToken.toString().equals(dto.getRefreshToken())){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                claims.getSubject(), claims.get("email", String.class), claims.get("role", String.class));

        return new ResponseEntity<>(
                new AccessTokenReissueResDto(accessToken), HttpStatus.OK);
    }
}