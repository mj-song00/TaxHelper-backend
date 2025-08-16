package lawpal.lawpal.domain.user.service;

import lawpal.lawpal.common.config.PasswordEncoder;
import lawpal.lawpal.common.exception.BaseException;
import lawpal.lawpal.common.exception.ExceptionEnum;
import lawpal.lawpal.domain.user.dto.AuthUser;
import lawpal.lawpal.domain.user.dto.response.UserProfileResponse;
import lawpal.lawpal.domain.user.entity.User;
import lawpal.lawpal.domain.user.enums.UserRole;
import lawpal.lawpal.domain.user.repository.UserRepository;
import lawpal.lawpal.domain.user.dto.request.SignupRequest;
import lawpal.lawpal.domain.user.validation.UserValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService  {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidation userValidation;

    @Transactional
    @Override
    public void createUser(SignupRequest signupRequest){
        Optional<User> userByEmail = userRepository.findByEmail(signupRequest.getEmail());
        Optional<User> userByNickname = userRepository. findByNickName(signupRequest.getNickName());

        if (userByEmail.isPresent()) {
            throw new BaseException(ExceptionEnum.USER_ALREADY_EXISTS);
        }

        if (userByNickname.isPresent()) {
            throw new BaseException(ExceptionEnum.USER_ALREADY_EXISTS);
        }


        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        UserRole userRole = UserRole.USER;

        User user = new User(
                signupRequest.getEmail(),
                signupRequest.getNickName(),
                encodedPassword,
                userRole
        );

        userRepository.save(user);
    }

    // 비밀번호 변경
    @Transactional
    @Override
    public void changePassword(AuthUser authUser, String oldPassword, String newPassword) {

        // 인증된 사용자 확인
        userValidation.validateAuthenticatedUser(authUser);

        // 인증된 사용자의 ID로 사용자 조회
        User user = userValidation.findUserById(authUser.getId());

        // 사용자 탈퇴 여부 확인
        userValidation.validateUserNotDeleted(user);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BaseException(ExceptionEnum.PASSWORD_MISMATCH);
        }

        // 새 비밀번호가 기존 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BaseException(ExceptionEnum.PASSWORD_SAME_AS_OLD);
        }

        // 새 비밀번호로 업데이트
        user.updatePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(AuthUser authUser) {
        // 인증된 사용자 확인
        userValidation.validateAuthenticatedUser(authUser);

        // 인증된 사용자의 ID로 사용자 조회
        User user = userValidation.findUserById(authUser.getId());
        // 사용자 탈퇴 여부 확인
        userValidation.validateUserNotDeleted(user);

        // 사용자 정보 반환
        return UserProfileResponse.of(user);
    }

}
