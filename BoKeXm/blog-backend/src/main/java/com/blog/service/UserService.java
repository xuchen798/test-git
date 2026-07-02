package com.blog.service;

import com.blog.dto.ForgotPasswordCodeDTO;
import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.dto.ResetPasswordDTO;
import com.blog.dto.UpdatePasswordDTO;
import com.blog.dto.UpdateUserInfoDTO;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;

import java.util.Map;

public interface UserService {

    Map<String, String> getCaptcha();

    void register(RegisterDTO registerDTO);

    LoginVO login(LoginDTO loginDTO);

    UserVO getUserInfo(Long userId);

    void updateUserInfo(Long userId, UpdateUserInfoDTO updateUserInfoDTO);

    void updatePassword(Long userId, UpdatePasswordDTO updatePasswordDTO);

    Map<String, String> sendForgotPasswordCode(ForgotPasswordCodeDTO dto);

    void resetPassword(ResetPasswordDTO dto);

}
