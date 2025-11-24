package hyunsub.glemoamember.keyword.controller;

import hyunsub.glemoamember.keyword.dto.KeywordReqDto;
import hyunsub.glemoamember.keyword.dto.KeywordResDto;
import hyunsub.glemoamember.keyword.service.KeywordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/keyword")
public class KeywordController {
    private final KeywordService keywordService;

    public KeywordController(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveKeyword(@RequestHeader("X-User-Id") String xUserId,
                                         @RequestBody KeywordReqDto keywordReqDto) {
        if(keywordReqDto.getKeywordName() == null) {
            return ResponseEntity.badRequest().body("키워드가 누락되었습니다.");
        }

        if(xUserId == null) {
            return ResponseEntity.badRequest().body("사용자 ID가 누락되었습니다.");
        }

        try {
            keywordService.save(Long.parseLong(xUserId), keywordReqDto);

            // 성공 시 200 OK
            return ResponseEntity.ok(String.format("%s 저장에 성공하였습니다!", keywordReqDto.getKeywordName()));

        } catch (IllegalStateException e) {
            // 중복 발생 시 409 Conflict 반환
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // "이미 등록된 키워드입니다."

        }
    }

    @GetMapping("/memberAll")
    public ResponseEntity<?> getAllKeyword(@RequestHeader("X-User-Id") String xUserId) {
        if(xUserId == null) {
            return ResponseEntity.badRequest().body("사용자 ID가 누락되었습니다.");
        }

        List<KeywordResDto> keywordResDtos = keywordService.getAllKeywordsByMember(Long.parseLong(xUserId));

        return ResponseEntity.ok(keywordResDtos);
    }

    @DeleteMapping("/{keywordName}")
    public ResponseEntity<?> deleteKeyword(@RequestHeader("X-User-Id") String xUserId,
                                           @PathVariable("keywordName") String keywordName) {
        if(xUserId == null) {
            return ResponseEntity.badRequest().body("사용자 ID가 누락되었습니다.");
        }

        try {
            keywordService.delete(keywordName, Long.parseLong(xUserId));
            return ResponseEntity.ok(String.format("키워드 %s 삭제에 성공하였습니다.", keywordName));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // collector 서비스에서 사용하는 회원이 설정한 키워드 정보를 반환하는 api
    @GetMapping("/all")
    public List<KeywordResDto> getAllKeywords() {
        return keywordService.getAllKeywords();
    }
}
