package com.uade.tpo.service.interfaces;

import com.uade.tpo.entity.User;
import com.uade.tpo.entity.dto.RegisterRequest;
import com.uade.tpo.entity.dto.TdRequest;
import com.uade.tpo.entity.dto.UserRequest;
import com.uade.tpo.entity.enumerations.Role;
import com.uade.tpo.security.AuthenticationResponse;

public interface UserServiceInterface {
    public AuthenticationResponse registerUser(RegisterRequest registerRequest);
    public AuthenticationResponse logIn(UserRequest userRequest);
    public User getUser(String code);
    public void userAuthority(User user, Role role);
    public void deleteUser(String code);
    public void editUser(String code, UserRequest userRequest);
    public void uploadInsurance(String code, UserRequest userRequest);
    public UserRequest getInsurance(String code);
    public void requestChangePassword(String email);
    public void validateCode(TdRequest tdRequest);
    public void changePassword(UserRequest userRequest);
}
