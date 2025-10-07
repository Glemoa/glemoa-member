package hyunsub.glemoamember.bookmark.controller;

import hyunsub.glemoamember.bookmark.dto.PostDto;
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

    @PostMapping("/doSave")
    public ResponseEntity<?> saveBookMark(@RequestBody PostIdDto dto,
                                          @RequestHeader("X-User-Id") String xUserId) {
        bookMarkService.saveBookMark(dto, xUserId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/viewBookMark")
    public ResponseEntity<?> viewBookMark(@RequestHeader("X-User-Id") String xUserId) {
        List<PostDto> bookmaredPost = bookMarkService.viewBookMarkedPost(xUserId);

        return new ResponseEntity<>(bookmaredPost, HttpStatus.OK);
    }
}
