package lawpal.lawpal.domain.user.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lawpal.lawpal.domain.user.dto.AuthUser;
import lawpal.lawpal.domain.user.dto.request.SignupRequest;

public interface UserService {
    void createUser(SignupRequest signupRequest);

    void changePassword(AuthUser authUser,String oldPassword, String newPassword);}
