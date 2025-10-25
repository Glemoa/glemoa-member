package hyunsub.glemoamember.member.service;

import hyunsub.glemoamember.member.dto.SocialLoginAccessTokenDto;
import hyunsub.glemoamember.member.dto.GoogleProfileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class GoogleService {
    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    public SocialLoginAccessTokenDto getAccessToken(String code) {
        // 인가 코드, clientId, clinet_secret, redirect_uri, grant_type 값이 필요하다.
        // 이 값들 중 인가코드는 파라미터로 받도록 설정 / 나머지는 properties나 yml에서 설정

        // 여기서 이제 api 서버에서 서버로 api 요청을 보내야 하는데 server to server http통신 에서 가장 많이 사용하는 라이브러리가 REST 템플릿이라는 거다.
        // Spring6부터 RestTemplate는 비추천상태이기 때문에, 대신 RestClinet를 사용한다.
        RestClient restClient = RestClient.create();

        // MultiValueMap을 통해 자동으로 form-data 형식으로 body를 조립 가능함.
        // 여기 code, client_id 와 같은 값들은 구글에서 지정되어 있는 이름이기 때문에 형식을 맞춰줘야 한다.
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<SocialLoginAccessTokenDto> response = restClient.post()
                // 토큰 uri는 전에 다운받았던 json 파일에 명시되어 있다.
                .uri("https://oauth2.googleapis.com/token")
                // 우리가 보내고자 하는 데이터의 타입이 application/x-www-form-urlencoded라고 하는거임.
                // 이거는 뭐냐면 formdata 형식이다. json 형식이 아니라 폼 데이터 형식으로 데이터를 줘야하기 때문에 이렇게 설정.
                .header("Content-Type", "application/x-www-form-urlencoded")
                // ?code=xxxx&client_id=xxxx&client_secret=xxxx&redirect_uri=xxxx&grant_type=xxxx
                .body(params)
                // retriveve는 응답 body값만을 추출한다.
                .retrieve()
                .toEntity(SocialLoginAccessTokenDto.class);

        System.out.println("GoogleAccessTokenDto 전체: " + response);
        System.out.println("GoogleAccessTokenDto Body : " + response.getBody().toString());

        return response.getBody();
    }

    public GoogleProfileDto getGoogleProfile(String token) {
        System.out.println("Google Access Token : " + token);

        RestClient restClient = RestClient.create();

        ResponseEntity<GoogleProfileDto> response = restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toEntity(GoogleProfileDto.class);

        System.out.println("GoogleProfileDto 전체: " + response);
        System.out.println("GoogleProfileDto Body : " + response.getBody().toString());

        return response.getBody();
    }
}
