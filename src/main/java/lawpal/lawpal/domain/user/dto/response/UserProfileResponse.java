package lawpal.lawpal.domain.user.dto.response;

import lawpal.lawpal.domain.user.entity.User;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UserProfileResponse {
    private UUID id;
    private String email;
    private String nickName;

    public UserProfileResponse(UUID id, String email, String nickName) {
        this.id = id;
        this.email = email;
        this.nickName = nickName;
    }

    public static UserProfileResponse of (User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickName()
        );
    }
}