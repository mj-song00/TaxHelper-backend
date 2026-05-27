package lawpal.lawpal.domain.ministry.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lawpal.lawpal.domain.law.entity.Law;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "law_join_ministry")
public class LawJointMinistry extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 공동부령 부처명
     */
    @Column(nullable = false)
    private String ministryName;

    /**
     * 공동부령 부처코드
     */
    @Column(nullable = false)
    private String ministryCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "law_id", nullable = false)
    private Law law;
}