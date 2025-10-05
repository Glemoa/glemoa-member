package hyunsub.glemoamember.common;

import hyunsub.glemoamember.member.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.expiration}")
    private int expiration;

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expirationRt}")
    private int expirationRt;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private Key ENCRYPT_SECRET_KEY = null;
    private Key ENCRYPT_RT_SECRET_KEY = null;

    // 생성자가 호출되고, 스프링빈이 만들어진 직후에 아래 메서드 바로 실행하는 어노테이션
    // java.util.Base64.getDecoder().decode()를 사용하여 이 Base64 인코딩된 문자열을 바이트 배열로 디코딩 함.
    // 디코딩된 바이트 배열과 HS512 서명 알고리즘 정보를 이용하여 시크릿 키를 생성.
    @PostConstruct
    public void init() {
        ENCRYPT_SECRET_KEY = new SecretKeySpec(
                Base64.getDecoder().decode(secretKey), "HmacSHA512");
        ENCRYPT_RT_SECRET_KEY = new SecretKeySpec(
                Base64.getDecoder().decode(secretKey), "HmacSHA512");
    }

    public String createAccessToken(Long id, String role) {
        // claims는 사용자정보(페이로드 정보)
        Claims claims = Jwts.claims();
        claims.put("id", id);
        claims.put("role", role);
        Date now = new Date();

        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 60 * 60)) // 1000ms * 3600 = 3600초
                .signWith(ENCRYPT_SECRET_KEY)
                .compact();

        return accessToken;
    }

    public String createRefreshToken(String email, String role) {
        Claims claims = Jwts.claims();
        claims.put("email", email);
        claims.put("role", role);
        Date now = new Date();

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 60 * 60))
                .signWith(ENCRYPT_RT_SECRET_KEY)
                .compact();

        return refreshToken;
    }
}
