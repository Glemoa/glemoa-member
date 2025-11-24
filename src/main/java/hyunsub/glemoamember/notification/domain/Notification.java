package hyunsub.glemoamember.notification.domain;

import hyunsub.glemoamember.keyword.domain.Keyword;
import hyunsub.glemoamember.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id")
    private Keyword keyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private Long postId;

    @Builder.Default
    private boolean isRead = false;

    @CreationTimestamp
    private LocalDateTime createdTime;

    public void checkRead() {
        isRead = true;
    }
}
