package lawpal.lawpal.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lawpal.lawpal.common.entity.Timestamped;
import lawpal.lawpal.domain.chat.enums.ChatJobStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_jobs")
public class ChatJob extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID jobId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false)
    private Integer topK;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatJobStatus status;

    @Column(columnDefinition = "TEXT")
    private String context;

    @Column(columnDefinition = "TEXT")
    private String sources;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(length = 1000)
    private String errorMessage;

    @Column
    private LocalDateTime processingStartedAt;

    @Column
    private LocalDateTime completedAt;

    public void updatePreparedData(String context, String sources) {
        this.context = context;
        this.sources = sources;
    }

    public void markWaiting() {
        this.status = ChatJobStatus.WAITING;
    }

    public void markProcessing() {
        this.status = ChatJobStatus.PROCESSING;
        this.processingStartedAt = LocalDateTime.now();
    }

    public void markCompleted(String answer) {
        this.answer = answer;
        this.status = ChatJobStatus.COMPLETED;
        this.errorMessage = null;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = ChatJobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
}
