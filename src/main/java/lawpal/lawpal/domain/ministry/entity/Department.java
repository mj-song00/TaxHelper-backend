package lawpal.lawpal.domain.ministry.entity;

import jakarta.persistence.*;
import lawpal.lawpal.domain.law.entity.Law;
import lombok.*;

/**
 *
 */
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     *  부서키
     *  ex.211795
     */
    @Column(unique = true)
    private String departmentKey;

    /**
     * 부서명
     * ex.은행과
     */
    private String departmentName;

    /**
     * 부서연락처
     * ex.02-0000-0000
     */
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "law_id", nullable = false)
    private Law law;

}
