package lawpal.lawpal.domain.user.dto;

import lawpal.lawpal.domain.user.enums.UserRole;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AuthUser {
    private final UUID id;
    private final String nickname;
    private final UserRole role;

    public AuthUser(UUID id, String nickname, UserRole role){
        this.id = id;
        this.nickname = nickname;
        this.role = role;
    }
}
