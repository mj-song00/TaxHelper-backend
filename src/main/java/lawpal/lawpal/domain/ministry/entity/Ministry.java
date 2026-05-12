package lawpal.lawpal.domain.ministry.entity;

import jakarta.persistence.*;
import lawpal.lawpal.common.entity.Timestamped;
import lombok.Getter;

/**
 * 소관 부처에 대한 table 입니다.
 * 현재 id, 부처명, 부처 코드에 대해 작성되어 있습니다.
 */
@Entity
@Getter
@Table(name = "ministries")
public class Ministry extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String ministryName;

    @Column(nullable = false, unique = true)
    private String ministryCode;

}
