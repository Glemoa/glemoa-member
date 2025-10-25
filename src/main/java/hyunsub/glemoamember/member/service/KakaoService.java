package hyunsub.glemoamember.member.service;

import hyunsub.glemoamember.member.dto.KakaoProfileDto;
import hyunsub.glemoamember.member.dto.SocialLoginAccessTokenDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class KakaoService {

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    public SocialLoginAccessTokenDto getAccessToken(String code) {

        RestClient restClient = RestClient.create();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<SocialLoginAccessTokenDto> response = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(SocialLoginAccessTokenDto.class);

        System.out.println("AccessTokenDto 전체: " + response);
        System.out.println("AccessTokenDto Body : " + response.getBody().toString());

        return response.getBody();
    }

    public KakaoProfileDto getKakaoProfile(String token) {
        System.out.println("Kakao Access Token : " + token);

        RestClient restClient = RestClient.create();

        ResponseEntity<KakaoProfileDto> response = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toEntity(KakaoProfileDto.class);

        System.out.println("KakaoProfileDto 전체: " + response);
        System.out.println("KakaoProfileDto Body : " + response.getBody().toString());

        return response.getBody();
    }
}
