package hyunsub.glemoamember.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hyunsub.glemoamember.member.domain.Member;
import hyunsub.glemoamember.member.domain.SocialType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 지금 이 dto에 없는 필드값이 들어올 경우 그 필드는 자동으로 무시하겠다라는 어노테이션이다.
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoProfileDto {
    // 카카오에서는 open_id값이 sub가 아니라 id로 되어있었음.
    private String id;
    private KakaoAccount kakao_account;

    // KakaoAccount, Profile 클래스 모두 json과 매핑이 되는 것들이기 때문에 JsonIgnoreProperties와 나머지 어노테이션들을 추가해줌.
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        private String email;
        private Profile profile;
    }

    // KakaoAccount, Profile 클래스 모두 json과 매핑이 되는 것들이기 때문에 JsonIgnoreProperties와 나머지 어노테이션들을 추가해줌.
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile{
        private String nickname;
    }

    public Member toEntity(String id) {
        return Member.builder()
                .email(this.kakao_account.email)
                .name(this.kakao_account.profile.nickname)
                .socialType(SocialType.KAKAO)
                .socialId(id)
                .build();
    }
}
