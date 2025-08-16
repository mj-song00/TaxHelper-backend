package lawpal.lawpal.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lawpal.lawpal.common.annotation.Auth;
import lawpal.lawpal.common.response.ApiResponse;
import lawpal.lawpal.common.response.ApiResponseEnum;
import lawpal.lawpal.domain.user.dto.AuthUser;
import lawpal.lawpal.domain.user.dto.request.ChangePasswordRequest;
import lawpal.lawpal.domain.user.dto.request.SignupRequest;
import lawpal.lawpal.domain.user.dto.response.UserProfileResponse;
import lawpal.lawpal.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "인증 API")
@RestController
@RequestMapping("/api/v1/users/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원가입", description = "회원가입을 진행합니다.")
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<Void>> signup(
            @Valid @RequestBody SignupRequest signupRequest) {
        userService.createUser(signupRequest);
        ApiResponse<Void> response =
                ApiResponse.successWithOutData(ApiResponseEnum.SIGNUP_SUCCESS);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "비밀번호 변경", description = "본인의 비밀번호를 변경합니다.")
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Auth AuthUser authUser,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(
                authUser,
                changePasswordRequest.getOldPassword(),
                changePasswordRequest.getNewPassword());
        ApiResponse<Void> response =
                ApiResponse.successWithOutData(ApiResponseEnum.PASSWORD_CHANGED_SUCCESS);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 프로필 조회", description = "본인의 프로필을 조회합니다.")
    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @Auth AuthUser authUser) {
        UserProfileResponse profile = userService.getUserProfile(authUser);
        ApiResponse<UserProfileResponse> response =
                ApiResponse.successWithData(profile, ApiResponseEnum.PROFILE_RETRIEVED_SUCCESS);
        return ResponseEntity.ok(response);
    }

}
