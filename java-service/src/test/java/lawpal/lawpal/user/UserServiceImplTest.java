package lawpal.lawpal.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lawpal.lawpal.common.config.PasswordEncoder;
import lawpal.lawpal.common.exception.BaseException;
import lawpal.lawpal.common.exception.ExceptionEnum;
import lawpal.lawpal.domain.user.entity.User;
import lawpal.lawpal.domain.user.repository.UserRepository;
import lawpal.lawpal.domain.user.dto.request.SignupRequest;
import lawpal.lawpal.domain.user.service.UserServiceImpl;
import lawpal.lawpal.domain.user.validation.UserValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserValidation userValidation;

    @Nested
    @DisplayName("회원가입")
    class CreateUser {
        private static ValidatorFactory factory;
        private static Validator validator;

        @BeforeAll
        public static void init() {
            factory = Validation.buildDefaultValidatorFactory();
            validator = factory.getValidator();
        }

        @Test
        @DisplayName("회원가입 - 성공")
        void createUserSuccess() {

            //given
            SignupRequest signupRequest = SignupRequest.builder()
                    .email("test@test.com")
                    .password("Asdf1234!")
                    .nickName("tester")
                    .build();

            when(userRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.empty()); // 가입된 이메일 없음
            when(userRepository.findByNickName(signupRequest.getNickName())).thenReturn(Optional.empty());// 가입된 닉네임 없음
            when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword"); // 비밀번호 암호화

            //when
            userService.createUser(signupRequest);

            //than
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 중복된 email")
        void createUserFailureEmailAlreadyExists() {
            //given
            SignupRequest signupRequest = SignupRequest.builder()
                    .email("test@example.com")
                    .password("password123!A")
                    .nickName("TestUser")
                    .build();

            User existingUser = mock(User.class);

            when(userRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.of(existingUser));

            // When & Then
            BaseException exception = assertThrows(BaseException.class, () -> {
                userService.createUser(signupRequest);
            });

            assertEquals(ExceptionEnum.USER_ALREADY_EXISTS, exception.getExceptionEnum());
        }

        @Test
        @DisplayName("회원가입 실패 - 비밀번호 규칙 불일치: 특수문자없음")
        void createUserFailDidNotMeetThePasswordRuleNoSpecialCharacters() {
            //given
            SignupRequest signupRequest = SignupRequest.builder()
                    .email("test@test.com")
                    .password("Asdf1234")
                    .nickName("tester")
                    .build();

            //when
            Set<ConstraintViolation<SignupRequest>> violations = validator.validate(signupRequest);
            //than
            for (ConstraintViolation<SignupRequest> violation : violations) {
                System.out.println(violation.getMessage());
            }
        }

        @Test
        @DisplayName("회원가입 실패 - 비밀번호 규칙 불일치: 대문자 없음")
        void createUserFailDidNotMeetThePasswordRuleWithNoCapitalLetters() {
            //given
            SignupRequest signupRequest = SignupRequest.builder()
                    .email("test@test.com")
                    .password("sdf1234!")
                    .nickName("tester")
                    .build();

            //when
            Set<ConstraintViolation<SignupRequest>> violations = validator.validate(signupRequest);

            //than
            for (ConstraintViolation<SignupRequest> violation : violations) {
                System.out.println(violation.getMessage());
            }
        }

        @Test
        @DisplayName("회원가입 실패 - 비밀번호 규칙 불일치: 8글자 미만")
        void createUserFailDidNotMeetThePasswordRuleLessThan8Characters() {
            //given
            SignupRequest signupRequest = SignupRequest.builder()
                    .email("test@test.com")
                    .password("Asd234!")
                    .nickName("tester")
                    .build();

            //when
            Set<ConstraintViolation<SignupRequest>> violations = validator.validate(signupRequest);

            //than
            for (ConstraintViolation<SignupRequest> violation : violations) {
                System.out.println(violation.getMessage());
            }
        }

        @Test
        @DisplayName("회원가입 실패 - 닉네임 중복 ")
        void createUserFailDuplicateNickname() {
            //given
            SignupRequest signupRequest = SignupRequest.builder()
                    .email("test@test.com")
                    .password("Asd234!")
                    .nickName("tester")
                    .build();

            //when
            User existingUser = mock(User.class);

            when(userRepository.findByNickName(signupRequest.getNickName())).thenReturn(Optional.of(existingUser));
            BaseException exception = assertThrows(BaseException.class, () -> {
                userService.createUser(signupRequest);
            });

            //than
            assertEquals(ExceptionEnum.USER_ALREADY_EXISTS, exception.getExceptionEnum());
        }
    }
}
