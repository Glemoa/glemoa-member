package hyunsub.glemoamember.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private Long id;
    private String title;
    private String link;
    private String author;
    private Integer commentCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private Integer recommendationCount;
    private String source;
}
