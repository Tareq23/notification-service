package com.tareq23.notificationservice.interfaces.rest.dispatch;

import com.tareq23.notificationservice.application.dispatch.DispatchCommand;
import com.tareq23.notificationservice.application.dispatch.DispatchNotificationUseCase;
import com.tareq23.notificationservice.application.dispatch.DispatchResult;
import com.tareq23.notificationservice.application.dispatch.ScheduleNotificationUseCase;
import com.tareq23.notificationservice.domain.dispatch.valueobject.NotificationPriority;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;



@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationHandler.class);

    private final DispatchNotificationUseCase dispatchUseCase;
    private final ScheduleNotificationUseCase scheduleUseCase;

    public NotificationHandler(DispatchNotificationUseCase dispatchUseCase,
                               ScheduleNotificationUseCase scheduleUseCase) {
        this.dispatchUseCase = Objects.requireNonNull(dispatchUseCase);
        this.scheduleUseCase = Objects.requireNonNull(scheduleUseCase);
    }



    @PostMapping("/dispatch")
    public ResponseEntity<DispatchResponse> dispatch(
            @Valid @RequestBody DispatchRequest request) {

        log.info("Dispatch request received. userId={} channel={} template={}",
                request.userId(), request.channel(), request.templateKey());

        DispatchCommand command = DispatchCommand.builder()
                .userId(request.userId())
                .recipient(request.recipient())
                .channel(request.channel())
                .priority(request.priority() != null ? request.priority() : NotificationPriority.NORMAL)
                .templateKey(request.templateKey())
                .subject(request.subject() != null ? request.subject() : "")
                .renderedBody(resolveRenderedBody(request))
                .payload(request.payload() != null ? request.payload() : Map.of())
                .correlationId(request.correlationId())
                .build();

        DispatchResult result = dispatchUseCase.dispatch(command);
        DispatchResponse response = DispatchResponse.from(result);

        HttpStatus status = result.isFailed()
                ? HttpStatus.ACCEPTED   // 202: accepted for retry
                : HttpStatus.OK;        // 200: dispatched, duplicate, or suppressed

        return ResponseEntity.status(status).body(response);
    }



    @PostMapping("/schedule")
    public ResponseEntity<Map<String, String>> schedule(
            @Valid @RequestBody ScheduleRequest request) {

        log.info("Schedule request received. userId={} channel={} at={}",
                request.userId(), request.channel(), request.scheduledAt());

        String notificationId = scheduleUseCase.schedule(
                request.userId(),
                request.channel(),
                request.priority() != null ? request.priority() : NotificationPriority.NORMAL,
                request.templateKey(),
                request.payload() != null ? request.payload() : Map.of(),
                request.correlationId(),
                request.scheduledAt()
        );

        if (notificationId == null) {
            return ResponseEntity.ok(Map.of("status", "DUPLICATE",
                    "message", "Notification already scheduled with this correlationId"));
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("notificationId", notificationId, "status", "SCHEDULED"));
    }



    private String resolveRenderedBody(DispatchRequest request) {
        return request.payload() != null
                ? request.payload().getOrDefault("renderedBody", "")
                : "";
    }
}
