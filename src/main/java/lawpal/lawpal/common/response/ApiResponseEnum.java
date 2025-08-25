package lawpal.lawpal.common.response;

import lombok.Getter;

@Getter
public enum ApiResponseEnum {
    SIGNUP_SUCCESS("회원가입 완료"),
    PASSWORD_CHANGED_SUCCESS("비밀번호 변경이 완료되었습니다."),
    PROFILE_RETRIEVED_SUCCESS("프로필 조회가 완료되었습니다."),
    NICKNAME_CHANGED_SUCCESS("닉네임 변경이 완료되었습니다."),
    USER_DELETED_SUCCESS("회원 탈퇴가 완료되었습니다.");

    private final String message;

    ApiResponseEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
