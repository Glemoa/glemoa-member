package hyunsub.glemoamember.member.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // 비밀번호는 일반 로그인에만 필요하며, SNS 사용자는 비밀번호가 없다. 따라서 nullable로 설계되었음. 이메일은 필수.
    private String password;

    // Enum을 String 타입으로 지정 안할시 숫자로 DB에 저장된다. 문자열로 저장하기 위해 EnumType.STRING 지정.
    // 빌더패턴에서 default 값을 사용하려면 Builder.Default륾 명시해줘야 함.
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private String socialId;

    @CreationTimestamp
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updatedTime;
}


