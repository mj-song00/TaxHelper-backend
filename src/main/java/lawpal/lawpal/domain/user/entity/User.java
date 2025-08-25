package lawpal.lawpal.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lawpal.lawpal.common.entity.Timestamped;
import lawpal.lawpal.domain.user.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "Users")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED) // 외부 직접 호출을 막기 위해 protected 설정
public class User extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id; // UUID BINARY(16)으로 저장

    @Column(nullable = false, unique = true)
    private String email; // 이메일 (불변 필드로 유지)

    @Column(nullable = false)
    private String nickName; // 닉네임

    @Column
    private String password; // 일반 로그인 사용자의 비밀번호 (소셜 로그인 사용자는 null 가능)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole; // 사용자 역할

    @Column
    private LocalDateTime deletedAt;

    public User(
            @NotBlank @Email String email,
            @NotBlank String nickName,
            String encodedPassword,
            UserRole userRole
    ){
        this.email = email;
        this.nickName = nickName;
        this.password = encodedPassword;
        this.userRole = userRole;
    }

    // 비밀번호 변경
    public void updatePassword(String password) {
        this.password = password;
    }

    // 닉네임 변경
    public void updateNickName(String nickName) {
        this.nickName = nickName;
    }

    // 회원 탈퇴
    public void updateDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }
}