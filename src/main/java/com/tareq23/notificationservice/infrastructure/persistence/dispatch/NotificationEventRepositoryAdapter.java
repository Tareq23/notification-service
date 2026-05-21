package com.tareq23.notificationservice.infrastructure.persistence.dispatch;

import com.tareq23.notificationservice.domain.dispatch.aggregate.DeadLetterEntry;
import com.tareq23.notificationservice.domain.dispatch.aggregate.IdempotencyKey;
import com.tareq23.notificationservice.domain.dispatch.aggregate.NotificationEvent;
import com.tareq23.notificationservice.domain.dispatch.aggregate.NotificationLog;
import com.tareq23.notificationservice.domain.dispatch.port.DeadLetterRepository;
import com.tareq23.notificationservice.domain.dispatch.port.IdempotencyRepository;
import com.tareq23.notificationservice.domain.dispatch.port.NotificationEventRepository;
import com.tareq23.notificationservice.domain.dispatch.port.NotificationLogRepository;
import com.tareq23.notificationservice.domain.dispatch.valueobject.EventPayload;
import com.tareq23.notificationservice.domain.shared.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class NotificationEventRepositoryAdapter
        implements NotificationEventRepository,
        NotificationLogRepository,
        IdempotencyRepository,
        DeadLetterRepository {

    private final NotificationEventMongoRepository mongoRepo;
    private final MongoTemplate mongoTemplate;

    @Override
    public void save(NotificationEvent event) {
        mongoRepo.save(toDocument(event));
    }

    @Override
    public Optional<NotificationEvent> findById(String notificationId) {
        return mongoRepo.findById(notificationId).map(this::toDomain);
    }

    @Override
    public Optional<NotificationEvent> findByIdempotencyKey(String idempotencyKey) {
        return mongoRepo.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    @Override
    public List<NotificationEvent> findFailedByUserId(String userId) {
        Query q = Query.query(Criteria.where("userId").is(userId).and("status").is("FAILED"));
        return mongoTemplate.find(q, NotificationEventDocument.class)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void update(NotificationEvent event) {
        mongoRepo.save(toDocument(event));
    }


    @Override
    public void save(NotificationLog log) {
        // Logs are embedded inside NotificationEventDocument.
        // A dedicated collection is only needed for large-scale query use cases.
        // For now, logs are persisted via update(NotificationEvent).
    }

    @Override
    public void saveAll(List<NotificationLog> logs) {
        // See save(NotificationLog). Logs are embedded in the parent document.
    }

    @Override
    public List<NotificationLog> findLogsByNotificationId(String notificationId) {
        return mongoRepo.findById(notificationId)
                .map(doc -> doc.getLogs().stream()
                        .map(this::toLogDomain)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IdempotencyRepository
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public boolean exists(IdempotencyKey key) {
        Query q = Query.query(Criteria.where("_id").is(key.value()));
        return mongoTemplate.exists(q, IdempotencyKeyDocument.class);
    }

    @Override
    public void register(IdempotencyKey key, String notificationId) {
        IdempotencyKeyDocument doc = new IdempotencyKeyDocument();
        doc.setKey(key.value());
        doc.setNotificationId(notificationId);
        doc.setCreatedAt(Instant.now());
        mongoTemplate.save(doc);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DeadLetterRepository
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void save(DeadLetterEntry entry) {
        mongoTemplate.save(toDlqDocument(entry));
    }

    @Override
    public Optional<DeadLetterEntry> findDlqByNotificationId(String notificationId) {
        Query q = Query.query(Criteria.where("notificationId").is(notificationId));
        DeadLetterDocument doc = mongoTemplate.findOne(q, DeadLetterDocument.class);
        return Optional.ofNullable(doc).map(this::toDlqDomain);
    }

    @Override
    public List<DeadLetterEntry> findAll(int page, int size) {
        Query q = new Query().with(PageRequest.of(page, size));
        return mongoTemplate.find(q, DeadLetterDocument.class)
                .stream().map(this::toDlqDomain).collect(Collectors.toList());
    }

    @Override
    public void delete(String dlqId) {
        mongoTemplate.remove(
                Query.query(Criteria.where("_id").is(dlqId)),
                DeadLetterDocument.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mapping helpers: domain ↔ document
    // ─────────────────────────────────────────────────────────────────────────

    private NotificationEventDocument toDocument(NotificationEvent e) {
        NotificationEventDocument doc = new NotificationEventDocument();
        doc.setId(e.notificationId());
        doc.setUserId(e.userId().value());
        doc.setIdempotencyKey(e.idempotencyKey().value());
        doc.setChannel(e.channel());
        doc.setPriority(e.priority());
        doc.setTemplateKey(e.templateKey());
        doc.setPayload(e.payload().data());
        doc.setStatus(e.status());
        doc.setProviderMessageId(e.providerMessageId());
        doc.setFailureReason(e.failureReason());
        doc.setAttemptNumber(e.attemptNumber());
        doc.setScheduledAt(e.scheduledAt());
        doc.setDispatchedAt(e.dispatchedAt());
        doc.setDeliveredAt(e.deliveredAt());
        doc.setCreatedAt(e.createdAt());
        doc.setLogs(e.logs().stream().map(this::toLogDocument).collect(Collectors.toList()));
        return doc;
    }

    private NotificationEvent toDomain(NotificationEventDocument doc) {
        // Reconstruct aggregate from document via the builder
        // Note: domain events are NOT re-raised on reconstitution — only on
        // state-changing method calls (markDispatched, markFailed, etc.)
        return NotificationEvent.builder()
                .notificationId(doc.getId())
                .userId(UserId.of(doc.getUserId()))
                .idempotencyKey(com.tareq23.notificationservice.domain.dispatch.aggregate.IdempotencyKey.of(doc.getIdempotencyKey()))
                .channel(doc.getChannel())
                .priority(doc.getPriority())
                .templateKey(doc.getTemplateKey())
                .payload(EventPayload.of(doc.getPayload()))
                .scheduledAt(doc.getScheduledAt())
                .build();
        // Status, logs, attempt numbers etc. would be restored via a
        // reconstitution factory method in a fully event-sourced design.
        // For this document-sourced approach, extend the builder or use
        // a package-private setter pattern as needed.
    }

    private NotificationLogDocument toLogDocument(NotificationLog l) {
        NotificationLogDocument doc = new NotificationLogDocument();
        doc.setLogId(l.logId());
        doc.setNotificationId(l.notificationId());
        doc.setChannel(l.channel());
        doc.setStatus(l.status());
        doc.setProviderMessageId(l.providerMessageId());
        doc.setFailureReason(l.failureReason());
        doc.setAttemptNumber(l.attemptNumber());
        doc.setLoggedAt(l.loggedAt());
        return doc;
    }

    private NotificationLog toLogDomain(NotificationLogDocument doc) {
        return NotificationLog.builder()
                .logId(doc.getLogId())
                .notificationId(doc.getNotificationId())
                .channel(doc.getChannel())
                .status(doc.getStatus())
                .providerMessageId(doc.getProviderMessageId())
                .failureReason(doc.getFailureReason())
                .attemptNumber(doc.getAttemptNumber())
                .loggedAt(doc.getLoggedAt())
                .build();
    }

    private DeadLetterDocument toDlqDocument(DeadLetterEntry e) {
        DeadLetterDocument doc = new DeadLetterDocument();
        doc.setDlqId(e.dlqId() != null ? e.dlqId() : UUID.randomUUID().toString());
        doc.setNotificationId(e.notificationId());
        doc.setUserId(e.userId().value());
        doc.setChannel(e.channel());
        doc.setTemplateKey(e.templateKey());
        doc.setLastFailureReason(e.lastFailureReason());
        doc.setTotalAttempts(e.totalAttempts());
        doc.setFirstAttemptAt(e.firstAttemptAt());
        doc.setMovedToDlqAt(e.movedToDlqAt());
        return doc;
    }

    private DeadLetterEntry toDlqDomain(DeadLetterDocument doc) {
        return DeadLetterEntry.builder()
                .dlqId(doc.getDlqId())
                .notificationId(doc.getNotificationId())
                .userId(UserId.of(doc.getUserId()))
                .channel(doc.getChannel())
                .templateKey(doc.getTemplateKey())
                .lastFailureReason(doc.getLastFailureReason())
                .totalAttempts(doc.getTotalAttempts())
                .firstAttemptAt(doc.getFirstAttemptAt())
                .movedToDlqAt(doc.getMovedToDlqAt())
                .build();
    }
}
