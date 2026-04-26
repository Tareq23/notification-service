# Notification Service

## Functional Requirements

### Notification Types & Triggers

| ID    | Notification Type                 | Trigger Condition                                                                 |
|-------|-----------------------------------|-----------------------------------------------------------------------------------|
| 1.1   | Order placed                      | Confirmation sent immediately after successful checkout                          |
| 1.2   | Order confirmed by seller         | Trigger when seller accepts or auto-confirms                                     |
| 1.3   | Order shipped                     | Trigger on courier pickup with tracking number                                   |
| 1.4   | Out for delivery                  | Trigger on same-day last-mile scan                                               |
| 1.5   | Order delivered                   | Trigger on successful delivery scan or OTP confirmation                          |
| 1.6   | Delivery failed / returned        | Trigger with reason and rescheduling options                                     |
| 1.7   | Refund initiated / completed      | Trigger on each refund status change                                             |
| 1.8   | Return / exchange approved/rejected | Trigger on approval or rejection                                                |
| 1.9   | Payment status                    | Payment success, failure, or pending                                             |
| 1.10  | Price drop alert                  | On wishlisted or cart items                                                      |
| 1.11  | Back-in-stock alert               | For out-of-stock saved items                                                     |
| 1.12  | Flash sale / deal reminder        | Sale starting or ending soon                                                     |
| 1.13  | Cart abandonment reminder         | Configurable delay (e.g., 1hr / 24hr)                                            |
| 1.14  | Review request                    | After confirmed delivery                                                         |
| 1.15  | Voucher / coupon expiry reminder  | Before expiration                                                                |
| 1.16  | Account security events           | Password change, new device login, suspicious activity                           |
| 1.17  | Seller messages / support reply   | When reply received                                                              |

---

### Content & Personalization

| ID    | Feature                          | Description                                                                      |
|-------|----------------------------------|----------------------------------------------------------------------------------|
| 4.1   | Dynamic template engine          | Inject order ID, item name, price, ETA, etc.                                     |
| 4.2   | Rich push notifications          | Image, action buttons (Track / Cancel / Review)                                  |
| 4.3   | Deep link support                | Open exact order/product screen in app                                           |
| 4.4   | Personalized content             | Use user name and purchase history in subject lines and body                     |
| 4.5   | A/B testing                      | Send variant messages to user segments, track CTR                                |
| 4.6   | Multi-language templates         | One template per language per event                                              |
| 4.7   | Brand/seller-specific templates  | For marketplace multi-seller scenarios                                           |

---

### In-App Notification Center

| ID    | Feature                          | Description                                                                      |
|-------|----------------------------------|----------------------------------------------------------------------------------|
| 6.1   | Paginated feed                   | Newest first, infinite scroll or load more                                       |
| 6.2   | Unread badge count               | Displayed on bell icon, synced across devices                                    |
| 6.3   | Mark as read                     | Individual or mark all as read                                                   |
| 6.4   | Delete notification              | Remove from feed                                                                 |
| 6.7   | Category tabs                    | Orders, Promotions, Account, etc.                                                |
| 6.8   | Notification expiry              | Auto-remove stale promotions after N days                                        |
| 6.9   | Real-time update                 | New notifications appear without page refresh (WebSocket/SSE)                    |

---

## Notification Channels

| ID    | Channel                          | Implementation                                                                   |
|-------|----------------------------------|----------------------------------------------------------------------------------|
| 2.1   | Push notification (mobile)       | iOS APNs, Android FCM                                                            |
| 2.2   | Email                            | Transactional and marketing via SMTP / SES / SendGrid                            |
| 2.3   | SMS                              | OTP, order updates via Twilio / local SMS gateway                                |
| 2.4   | In-app notification              | Notification center (web + mobile)                                               |
| 2.5   | WhatsApp Business API            | Order & delivery updates                                                         |
| 2.6   | Browser push notification (web)  | Service worker based                                                             |
| 2.7   | Multi-channel fallback           | If push fails, fall back to SMS or email                                         |
| 2.8   | Channel priority order           | Configurable per notification type                                               |

---

## User Preference & Control

| ID    | Feature                          | Description                                                                      |
|-------|----------------------------------|----------------------------------------------------------------------------------|
| 3.1   | Per-category opt-in/out          | Order updates, promotions, security, etc.                                        |
| 3.2   | Per-channel opt-in/out           | Turn off emails but keep push, etc.                                              |
| 3.3   | Global mute                      | Silence all non-critical notifications                                           |
| 3.4   | Quiet hours / DND                | Define time windows per channel                                                  |
| 3.5   | Frequency cap                    | Max N marketing notifications per day/week                                       |
| 3.6   | Language preference              | Notifications sent in user's selected language                                   |
| 3.7   | Unsubscribe link                 | In every marketing email (legal requirement)                                     |
| 3.8   | Preference center                | Single UI to manage all notification settings                                    |

---

## Delivery & Reliability

| ID    | Feature                          | Description                                                                      |
|-------|----------------------------------|----------------------------------------------------------------------------------|
| 5.1   | At-least-once delivery           | With idempotency (no duplicate sends)                                            |
| 5.2   | Retry mechanism                  | Exponential backoff on provider failures                                         |
| 5.3   | Dead-letter queue                | Failed notifications stored for investigation                                    |
| 5.4   | Delivery tracking                | Sent / delivered / opened / failed per notification                              |
| 5.5   | Rate limiting                    | Respect provider quotas (FCM, APNs, SMTP limits)                                 |
| 5.6   | Priority queues                  | Transactional (OTP, order updates) over marketing                                |
| 5.7   | Scheduled notifications          | Send at a future time (sale launch, reminders)                                   |
| 5.8   | Batch / bulk send                | For campaigns to millions of users efficiently                                   |

---

## Admin, Analytics & Compliance

| ID    | Feature                          | Description                                                                      |
|-------|----------------------------------|----------------------------------------------------------------------------------|
| 7.1   | Admin dashboard                  | Create/edit/delete templates, preview per channel                                |
| 7.2   | Campaign management              | Audience segmentation, schedule, send, and track                                 |
| 7.3   | Delivery analytics               | Delivery rate, open rate, click-through rate per campaign                        |
| 7.4   | Audit log                        | Who sent what notification to whom and when                                      |
| 7.5   | Suppression list management      | Respect unsubscribes, bounces, and spam reports                                  |
| 7.6   | GDPR / PDPA compliance           | User data erasure includes notification history                                  |
| 7.7   | Alerting                         | Internal alert if delivery rate drops below threshold                            |
| 7.8   | Multi-tenant support             | Separate config per seller / region / brand                                      |

---

# Notification Service Database Schema

This document outlines the MongoDB collection schema for the **Notification Service**, organized by functional layer.

---

## 🏗️ Layer 1: Core (Users & Preferences)
Managed user accounts, communication channels, and delivery settings.

| Collection | Description | Fields |
| :--- | :--- | :--- |
| **`users`** | Platform user accounts | `_id`, `userId`, `email`, `phone`, `locale`, `timezone` |
| **`notification_preferences`** | Per-user channel & category settings | `_id`, `userId`, `channels`, `categories`, `quietHours`, `globalMute`, `dailyCap`, `updatedAt` |
| **`device_tokens`** | Push tokens per device | `_id`, `userId`, `token`, `platform`, `appVersion`, `isActive`, `lastSeenAt`, `createdAt` |

---

## 📝 Layer 2: Templates & Events
Defines the content of notifications and the triggers that initiate them.

| Collection | Description | Fields |
| :--- | :--- | :--- |
| **`notification_templates`** | Channel-specific message templates | `_id`, `templateKey`, `eventType`, `channel`, `locale`, `subject`, `body`, `imageUrl`, `actionButtons`, `version` |
| **`notification_events`** | Incoming trigger events queue | `_id`, `eventType`, `sourceService`, `userId`, `payload`, `priority`, `status`, `scheduledAt`, `createdAt` |

---

## 🚀 Layer 3: Dispatch & Delivery
Handles the technical execution of sending notifications and tracking reliability.

| Collection | Description | Fields |
| :--- | :--- | :--- |
| **`notification_logs`** | One record per send attempt | `_id`, `eventId`, `userId`, `channel`, `templateKey`, `renderedBody`, `status`, `providerMsgId`, `retryCount`, `errorReason`, `openedAt`, `clickedAt`, `sentAt` |
| **`dead_letter_queue`** | Permanently failed notifications | `_id`, `originalEventId`, `logId`, `failureReason`, `totalAttempts`, `resolvedAt`, `createdAt` |
| **`idempotency_keys`** | Prevents duplicate sends | `_id`, `key`, `logId`, `processedAt`, `expiresAt` |

---

## 🔔 Layer 4: In-App Notification Center
Manages the "Bell" icon feed within the application.

| Collection | Description | Fields |
| :--- | :--- | :--- |
| **`inapp_notifications`** | Bell feed per user | `_id`, `userId`, `title`, `body`, `imageUrl`, `deepLink`, `category`, `isRead`, `readAt`, `expiresAt`, `createdAt` |

---

## 📊 Layer 5: Campaigns & Suppression
Handles bulk marketing, unsubscribes, and volume control.

| Collection | Description | Fields |
| :--- | :--- | :--- |
| **`campaigns`** | Bulk marketing sends | `_id`, `name`, `templateKey`, `channels`, `audience`, `scheduledAt`, `status`, `stats`, `abTest`, `createdBy`, `createdAt` |
| **`suppression_list`** | Unsubscribes, bounces, and spam | `_id`, `identifier`, `identifierType`, `channel`, `reason`, `source`, `createdAt` |
| **`rate_limit_counters`** | Daily cap tracking per user | `_id`, `userId`, `channel`, `category`, `count`, `expiresAt` |

---

## ⚖️ Layer 6: Audit & Compliance
Tracking for administrative actions and data privacy (GDPR).

| Collection | Description | Fields |
| :--- | :--- | :--- |
| **`audit_logs`** | Admin actions & system events | `_id`, `actorId`, `action`, `targetType`, `targetId`, `diff`, `createdAt` |
| **`gdpr_deletion_requests`** | Right-to-erasure tracking | `_id`, `userId`, `status`, `collectionsErased`, `requestedAt`, `completedAt`, `handledBy` |

---

### 🔑 Key Schema Indicators
* **PK**: Primary Key (`_id`)
* **IDX**: Indexed field (Optimized for queries)
* **REQ**: Required field
* **TTL**: Time-To-Live (Data auto-expires after a set duration)