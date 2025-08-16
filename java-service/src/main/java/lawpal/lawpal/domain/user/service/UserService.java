package lawpal.lawpal.domain.user.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lawpal.lawpal.domain.user.dto.AuthUser;
import lawpal.lawpal.domain.user.dto.request.SignupRequest;
import lawpal.lawpal.domain.user.dto.response.UserProfileResponse;

public interface UserService {
    void createUser(SignupRequest signupRequest);

    void changePassword(AuthUser authUser,String oldPassword, String newPassword);

    UserProfileResponse getUserProfile(AuthUser authUser);

    void changeNickName(AuthUser authUser,  String newNickName);

    void deleteUser(AuthUser authenticatedUser, String refreshToken, HttpServletResponse response);
}
