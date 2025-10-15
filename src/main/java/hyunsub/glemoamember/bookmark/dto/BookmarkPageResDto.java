package hyunsub.glemoamember.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkPageResDto {
    private List<PostDto> postDtoList; // 페이지에 들어있는 Post
    private Long postCount; // 전체 게시글 수
}
