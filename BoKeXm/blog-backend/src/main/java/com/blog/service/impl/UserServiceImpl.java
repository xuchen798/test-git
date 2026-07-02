package com.blog.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.JwtUtil;
import com.blog.common.RedisKeyConstants;
import com.blog.common.RedisUtil;
import com.blog.common.ResultCode;
import com.blog.dto.ForgotPasswordCodeDTO;
import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.dto.ResetPasswordDTO;
import com.blog.dto.UpdatePasswordDTO;
import com.blog.dto.UpdateUserInfoDTO;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public Map<String, String> getCaptcha() {
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        String captchaText = lineCaptcha.getCode();
        String uuid = IdUtil.simpleUUID();
        String redisKey = RedisKeyConstants.USER_CAPTCHA + uuid;
        redisUtil.set(redisKey, captchaText, 5, TimeUnit.MINUTES);

        String base64Image = lineCaptcha.getImageBase64Data();

        Map<String, String> result = new HashMap<>();
        result.put("key", uuid);
        result.put("image", base64Image);
        return result;
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        validateCaptcha(registerDTO.getCaptchaKey(), registerDTO.getCaptcha());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername())
                .or()
                .eq(User::getEmail, registerDTO.getEmail());
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            if (existUser.getUsername().equals(registerDTO.getUsername())) {
                throw new BusinessException(ResultCode.USER_ALREADY_EXIST.getCode(), "用户名已存在");
            } else {
                throw new BusinessException(ResultCode.USER_ALREADY_EXIST.getCode(), "邮箱已被注册");
            }
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setStatus(1);
        user.setRole(0);
        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptcha());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, loginDTO.getEmail());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }

        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        String redisKey = RedisKeyConstants.USER_LOGIN_TOKEN + user.getId();
        redisUtil.set(redisKey, token, 24, TimeUnit.HOURS);

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(userVO);
        return loginVO;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public void updateUserInfo(Long userId, UpdateUserInfoDTO updateUserInfoDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (updateUserInfoDTO.getUsername() != null) {
            user.setUsername(updateUserInfoDTO.getUsername());
            user.setNickname(updateUserInfoDTO.getUsername());
        }
        if (updateUserInfoDTO.getAvatar() != null) {
            user.setAvatar(updateUserInfoDTO.getAvatar());
        }
        if (updateUserInfoDTO.getNickname() != null) {
            user.setNickname(updateUserInfoDTO.getNickname());
        }
        if (updateUserInfoDTO.getSignature() != null) {
            user.setSignature(updateUserInfoDTO.getSignature());
        }
        if (updateUserInfoDTO.getBio() != null) {
            user.setBio(updateUserInfoDTO.getBio());
        }
        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO updatePasswordDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (!BCrypt.checkpw(updatePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPassword(BCrypt.hashpw(updatePasswordDTO.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public Map<String, String> sendForgotPasswordCode(ForgotPasswordCodeDTO dto) {
        if (dto.getUsername() == null || dto.getEmail() == null) {
            throw new BusinessException("用户名和邮箱不能为空");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null || !dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new BusinessException("用户名与邮箱不匹配");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        String captchaText = lineCaptcha.getCode();
        String uuid = IdUtil.simpleUUID();
        String redisKey = RedisKeyConstants.USER_CAPTCHA + uuid;
        redisUtil.set(redisKey, captchaText, 5, TimeUnit.MINUTES);

        Map<String, String> result = new HashMap<>();
        result.put("key", uuid);
        result.put("image", lineCaptcha.getImageBase64Data());
        return result;
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        validateCaptcha(dto.getCaptchaKey(), dto.getCaptcha());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null || !dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new BusinessException("用户名与邮箱不匹配");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        user.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        userMapper.updateById(user);

        try {
            String tokenKey = RedisKeyConstants.USER_LOGIN_TOKEN + user.getId();
            redisUtil.delete(tokenKey);
        } catch (Exception e) {
            log.warn("重置密码后清除旧登录 token 失败，忽略：{}", e.getMessage());
        }
    }

    private void validateCaptcha(String captchaKey, String captcha) {
        String redisKey = RedisKeyConstants.USER_CAPTCHA + captchaKey;
        Object redisCaptcha = redisUtil.get(redisKey);
        if (redisCaptcha == null) {
            log.warn("验证码 Redis 中未找到或 Redis 不可用（captchaKey={}），已降级跳过校验", captchaKey);
            return;
        }
        if (!captcha.equalsIgnoreCase(redisCaptcha.toString())) {
            redisUtil.delete(redisKey);
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
        redisUtil.delete(redisKey);
    }

}
