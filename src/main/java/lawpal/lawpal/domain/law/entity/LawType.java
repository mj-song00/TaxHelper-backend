package lawpal.lawpal.domain.law.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 법종 구분에 대한 table 입니다.
 * 법종 구분 코드와 content로 구성되어 있습니다.
 */

@Entity
@Getter
@Table(name = "law_type")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LawType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ex. A0009
     */
    @Column
    private String typeCode;

    /**
     * ex. 총리령
     */
    @Column
    private String typeName;
}


