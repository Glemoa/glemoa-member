package hyunsub.glemoamember.notification.dto;

import hyunsub.glemoamember.keyword.domain.Keyword;
import hyunsub.glemoamember.member.domain.Member;
import hyunsub.glemoamember.notification.domain.Notification;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationReqDto {
    private Long keywordId;
    private Long memberId;
    private Long postId;

    public Notification toEntity(Keyword keyword, Member member) {
        return Notification.builder()
                .keyword(keyword)
                .member(member)
                .postId(this.postId)
                .isRead(false)
                .build();
    }
}
