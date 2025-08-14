package lawpal.lawpal.domain.user.service;

import lawpal.lawpal.domain.user.dto.request.SignupRequest;

public interface UserService {
    void createUser(SignupRequest signupRequest);
}
