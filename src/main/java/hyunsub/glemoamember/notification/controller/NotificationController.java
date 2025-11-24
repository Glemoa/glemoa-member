package hyunsub.glemoamember.notification.controller;

import hyunsub.glemoamember.bookmark.dto.PostDto;
import hyunsub.glemoamember.notification.dto.NotificationReqDto;
import hyunsub.glemoamember.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notification")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // 프런트가 호출하는 SSE 구독 엔드포인트
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@RequestHeader("X-User-Id") String xUserId) {
        // Gateway에서 검증된 사용자 ID를 헤더로 넘겨줌
        SseEmitter emitter = notificationService.subscribe(Long.parseLong(xUserId));
        return ResponseEntity.ok(emitter);
    }

    // collector 에서 호출하는 알림 생성 api
    @PostMapping("/create")
    public void createNotification(@RequestBody List<NotificationReqDto> notificationReqDtos) {
        notificationService.createNotifiaction(notificationReqDtos);
        log.info("notificationReqDtos: {}", notificationReqDtos);
    }

    // 읽지 않은 알림의 개수 조회 api
    @GetMapping("/count-unread")
    public ResponseEntity<?> countUnreadNotifications(@RequestHeader("X-User-Id") String xUserId) {
        if(xUserId == null) {
            return ResponseEntity.badRequest().body("사용자 ID가 누락되었습니다.");
        }

        try {
            Long count = notificationService.countUnreadNotifications(Long.parseLong(xUserId));
            return ResponseEntity.ok(count);
        } catch(Exception e) {
            log.error("읽지 않은 알림 개수 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("읽지 않은 알림 개수 중 오류가 발생했습니다.");
        }
    }

    // 읽지 않은 알림 조회 api
    @GetMapping("/search-unread")
    public ResponseEntity<?> getUnreadNotifications(@RequestHeader("X-User-Id") String xUserId) {
        if(xUserId == null) {
            return ResponseEntity.badRequest().body("사용자 ID가 누락되었습니다.");
        }
        
        try {
            List<PostDto> unreadPosts = notificationService.getUnreadNotifications(Long.parseLong(xUserId));
            return ResponseEntity.ok(unreadPosts);
        } catch(Exception e) {
            log.error("읽지 않은 알림 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("알림 조회 중 오류가 발생했습니다.");
        }
    }

    // 특정 사용자의 알림을 읽음 처리
    @PatchMapping("/read/{postId}")
    public ResponseEntity<?> updateReadNotification(@RequestHeader("X-User-Id") String xUserId,
                                              @PathVariable("postId") Long postId) {
        if(xUserId == null) {
            return ResponseEntity.badRequest().body("사용자 ID가 누락되었습니다.");
        }

        if(postId == null) {
            return ResponseEntity.badRequest().body("게시글 ID가 누락되었습니다.");
        }

        notificationService.updateReadNotification(Long.parseLong(xUserId), postId);

        return ResponseEntity.ok("알림을 읽음 처리하였습니다.");
    }

    // 특정 사용자의 알림을 전부 읽음 처리
    @PatchMapping("/read-all")
    public ResponseEntity<?> updateReadAllNotification(@RequestHeader("X-User-Id") String xUserId) {
        if(xUserId == null) {
            return ResponseEntity.badRequest().body("사용자 ID가 누락되었습니다.");
        }

        notificationService.updateReadAllNotification(Long.parseLong(xUserId));

        return ResponseEntity.ok("알림을 전부 읽음 처리하였습니다.");
    }

    // 읽은 알림 조회 api
    @GetMapping("/search-read")
    public ResponseEntity<?> getReadNotifications(@RequestHeader("X-User-Id") String xUserId) {
        if(xUserId == null) {
            return ResponseEntity.badRequest().body("사용자 ID가 누락되었습니다.");
        }

        try {
            List<PostDto> unreadPosts = notificationService.getReadNotifications(Long.parseLong(xUserId));
            return ResponseEntity.ok(unreadPosts);
        } catch(Exception e) {
            log.error("읽지 않은 알림 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("알림 조회 중 오류가 발생했습니다.");
        }
    }

    // 특정 사용자가 읽은 특정 알림을 삭제 처리
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<?> deleteNotification(@RequestHeader("X-User-Id") String xUserId,
                                                    @PathVariable("postId") Long postId) {
        if(xUserId == null) {
            return ResponseEntity.badRequest().body("사용자 ID가 누락되었습니다.");
        }

        if(postId == null) {
            return ResponseEntity.badRequest().body("게시글 ID가 누락되었습니다.");
        }

        notificationService.deleteNotification(Long.parseLong(xUserId), postId);

        return ResponseEntity.ok("알림을 삭제하였습니다.");
    }

    // 특정 사용자가 읽은 알림을 전부 삭제 처리
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllNotification(@RequestHeader("X-User-Id") String xUserId) {
        if(xUserId == null) {
            return ResponseEntity.badRequest().body("사용자 ID가 누락되었습니다.");
        }

        notificationService.deleteAllNotification(Long.parseLong(xUserId));

        return ResponseEntity.ok("알림을 전부 삭제하였습니다.");
    }
}