package hyunsub.glemoamember.member.service;

import hyunsub.glemoamember.bookmark.dto.PostDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "glemoa-reader")
public interface ReaderFeign {

    @GetMapping("/post/{postId}")
    PostDto findByPostId (@PathVariable("postId") Long postId);

    @PostMapping("/post/viewBookMarkedPost")
    List<PostDto> viewBookMarkedPostByPostIdList(List<Long> postIdList);
}
