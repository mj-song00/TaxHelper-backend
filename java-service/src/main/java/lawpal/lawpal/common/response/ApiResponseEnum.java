package lawpal.lawpal.common.response;

import lombok.Getter;

@Getter
public enum ApiResponseEnum {
    SIGNUP_SUCCESS("회원가입 완료");

    private final String message;

    ApiResponseEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
