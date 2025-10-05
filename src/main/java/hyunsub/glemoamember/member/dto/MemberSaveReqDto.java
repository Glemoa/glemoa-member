package hyunsub.glemoamember.member.dto;

import hyunsub.glemoamember.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberSaveReqDto {
    private String name;
    private String email;
    private String password;

    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .password(encodedPassword)
                .build();
    }
}
