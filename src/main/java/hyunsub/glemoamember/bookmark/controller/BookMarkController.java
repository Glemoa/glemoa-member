package hyunsub.glemoamember.bookmark.controller;

import hyunsub.glemoamember.bookmark.dto.BookmarkPageResDto;
import hyunsub.glemoamember.bookmark.dto.PostIdDto;
import hyunsub.glemoamember.bookmark.service.BookMarkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookMark")
public class BookMarkController {
    private final BookMarkService bookMarkService;

    public BookMarkController(BookMarkService bookMarkService) {
        this.bookMarkService = bookMarkService;
    }

    @PostMapping("/doBookMark")
    public ResponseEntity<?> doBookMark(@RequestBody PostIdDto dto,
                                          @RequestHeader("X-User-Id") String xUserId) {
        bookMarkService.doBookMark(dto, xUserId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/viewBookMaredPostId")
    public ResponseEntity<?> viewBookMarkedPostId(@RequestHeader("X-User-Id") String xUserId) {
        List<Long> bookmaredPostId = bookMarkService.viewBookMarkedPostId(xUserId);

        return new ResponseEntity<>(bookmaredPostId, HttpStatus.OK);
    }

    @GetMapping("/viewBookMarkPost")
    public ResponseEntity<?> viewBookMarkPost(@RequestHeader("X-User-Id") String xUserId,
                                              @RequestParam("page") Long page,
                                              @RequestParam("pageSize") Long pageSize,
                                              @RequestParam("movablePageCount") Long movablePageCount) {
        BookmarkPageResDto bookmaredPost = bookMarkService.viewBookMarkedPost(xUserId, page, pageSize, movablePageCount);

        return new ResponseEntity<>(bookmaredPost, HttpStatus.OK);
    }
}
