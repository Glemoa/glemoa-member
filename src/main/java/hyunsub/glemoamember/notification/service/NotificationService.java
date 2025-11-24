package hyunsub.glemoamember.notification.service;

import hyunsub.glemoamember.bookmark.dto.PostDto;
import hyunsub.glemoamember.bookmark.dto.PostIdDto;
import hyunsub.glemoamember.keyword.domain.Keyword;
import hyunsub.glemoamember.keyword.repository.KeywordRepository;
import hyunsub.glemoamember.member.domain.Member;
import hyunsub.glemoamember.member.repository.MemberRepository;
import hyunsub.glemoamember.member.service.ReaderFeign;
import hyunsub.glemoamember.notification.domain.Notification;
import hyunsub.glemoamember.notification.dto.NotificationReqDto;
import hyunsub.glemoamember.notification.repository.NotificationRepository;
import hyunsub.glemoamember.notification.repository.SseEmitterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class NotificationService {
    private final SseEmitterRepository sseEmitterRepository;
    private final MemberRepository memberRepository;
    private final KeywordRepository keywordRepository;
    private final NotificationRepository notificationRepository;
    private final ReaderFeign readerFeign;

    public NotificationService(SseEmitterRepository sseEmitterRepository, MemberRepository memberRepository, KeywordRepository keywordRepository, NotificationRepository notificationRepository, ReaderFeign readerFeign) {
        this.sseEmitterRepository = sseEmitterRepository;
        this.memberRepository = memberRepository;
        this.keywordRepository = keywordRepository;
        this.notificationRepository = notificationRepository;
        this.readerFeign = readerFeign;
    }

    public SseEmitter subscribe(Long userId) {
        // 1. 현재 클라이언트를 위한 Emitter 생성 (타임아웃 설정: 60분)
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);

        try {
            // 2. 연결 시 "연결되었습니다" 같은 더미 데이터를 하나 보내줘야 함
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected!"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 3. 저장소에 저장
        sseEmitterRepository.save(userId, emitter);

        // 4. 연결 종료/타임아웃 시 저장소에서 삭제
        emitter.onCompletion(() -> sseEmitterRepository.deleteById(userId));
        emitter.onTimeout(() -> sseEmitterRepository.deleteById(userId));

        return emitter;
    }

    public void sendRealTimeNotifications(Long userId, List<Long> newPostIds) {
        SseEmitter emitter = sseEmitterRepository.get(userId);
        if (emitter != null) {
            // 1. 방금 생긴 게시글들의 상세 정보만 Reader 서비스에서 가져옴 (부하 최소화)
            List<PostDto> newPostDtos = new ArrayList<>();
            try {
                if (!newPostIds.isEmpty()) {
                    newPostDtos = readerFeign.viewBookMarkedPostByPostIdList(newPostIds);
                }
            } catch (Exception e) {
                log.error("Feign Error", e);
                return;
            }

            // 2. 전송
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(newPostDtos)); // 방금 생긴 N개만 전송
            } catch (IOException e) {
                sseEmitterRepository.deleteById(userId);
            }
        }
    }

    public void createNotifiaction(List<NotificationReqDto> notificationReqDtos) {
        List<Notification> notifications = new ArrayList<>();

        for (NotificationReqDto notificationReqDto : notificationReqDtos) {
            // 외래 키만 설정해서 저장하는 경우 실제 db에서 조회하는 것 보다 프록시를 통한 getReference로 찾는 것이 성능에 좋다.
            Member member = memberRepository.getReferenceById(notificationReqDto.getMemberId());
            Keyword keyword = keywordRepository.getReferenceById(notificationReqDto.getKeywordId());

            notifications.add(notificationReqDto.toEntity(keyword, member));
        }

        notificationRepository.saveAll(notifications);

        // 사용자별로 "새로 생긴 게시글 ID 목록"을 그룹화하여 전송
        // Map<UserId, List<PostId>> 형태가 된다.
        Map<Long, List<Long>> userNewPostsMap = notificationReqDtos.stream()
                .collect(Collectors.groupingBy(
                        NotificationReqDto::getMemberId,
                        Collectors.mapping(NotificationReqDto::getPostId, Collectors.toList())
                ));

        // 그룹화된대로 각 유저에게 "네가 받아야 할 새 글 목록은 이거야"라고 전송
        userNewPostsMap.forEach((userId, postIds) -> {
            this.sendRealTimeNotifications(userId, postIds);
        });
    }

    public Long countUnreadNotifications(Long userId) {
        Long count = notificationRepository.countByMemberIdAndIsReadFalse(userId);
        return count;
    }

    public List<PostDto> getUnreadNotifications(Long xUserId) {
        // 1. 읽지 않은 알림 목록에서 postId들을 가져옵니다.
        List<Long> postIds = notificationRepository.findByMemberIdAndIsReadFalseOrderByCreatedTimeDesc(xUserId)
                .stream() // 리스트를 스트림으로 변환
                .map(Notification::getPostId) // 각 알림에서 postId만 추출
                .collect(Collectors.toList()); // 새로운 리스트로 수집

        // 2. reader-service에 postId 목록을 보내서 게시물 상세 정보를 가져옵니다.
        if (postIds.isEmpty()) {
            return new ArrayList<>(); // 읽지 않은 알림이 없으면 빈 리스트 반환
        }

        return readerFeign.viewBookMarkedPostByPostIdList(postIds);
    }

    public void updateReadNotification(Long userId, Long postId) {
        notificationRepository.updateReadNotification(userId, postId);
    }

    public void updateReadAllNotification(Long userId) {
        notificationRepository.updateReadAllNotification(userId);
    }

    public List<PostDto> getReadNotifications(Long xUserId) {
        // 1. 읽지 않은 알림 목록에서 postId들을 가져옵니다.
        List<Long> postIds = notificationRepository.findByMemberIdAndIsReadTrueOrderByCreatedTimeDesc(xUserId)
                .stream() // 리스트를 스트림으로 변환
                .map(Notification::getPostId) // 각 알림에서 postId만 추출
                .collect(Collectors.toList()); // 새로운 리스트로 수집

        // 2. reader-service에 postId 목록을 보내서 게시물 상세 정보를 가져옵니다.
        if (postIds.isEmpty()) {
            return new ArrayList<>(); // 읽지 않은 알림이 없으면 빈 리스트 반환
        }

        return readerFeign.viewBookMarkedPostByPostIdList(postIds);
    }

    public void deleteNotification(Long userId, Long postId) {
        notificationRepository.deleteNotification(userId, postId);
    }

    public void deleteAllNotification(Long userId) {
        notificationRepository.deleteAllNotification(userId);
    }
}
