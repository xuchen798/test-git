package com.blog.controller;

import com.blog.common.JwtUtil;
import com.blog.common.Result;
import com.blog.dto.ForgotPasswordCodeDTO;
import com.blog.dto.LoginDTO;
import com.blog.dto.RegisterDTO;
import com.blog.dto.ResetPasswordDTO;
import com.blog.dto.UpdatePasswordDTO;
import com.blog.dto.UpdateUserInfoDTO;
import com.blog.service.UserService;
import com.blog.vo.LoginVO;
import com.blog.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@Tag(name = "用户模块")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private JwtUtil jwtUtil;

    @Value("${file.upload.path:D:/blog/upload/}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/upload/}")
    private String uploadUrlPrefix;

    @GetMapping("/captcha")
    @Operation(summary = "获取验证码")
    public Result<Map<String, String>> getCaptcha() {
        Map<String, String> captcha = userService.getCaptcha();
        return Result.success(captcha);
    }

    @PostMapping("/forgot/send-code")
    @Operation(summary = "找回密码：校验用户名邮箱并发送验证码")
    public Result<Map<String, String>> sendForgotPasswordCode(@Valid @RequestBody ForgotPasswordCodeDTO dto) {
        Map<String, String> captcha = userService.sendForgotPasswordCode(dto);
        return Result.success(captcha);
    }

    @PostMapping("/forgot/reset")
    @Operation(summary = "找回密码：验证验证码并重置密码")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto);
        return Result.success();
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success(loginVO);
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<UserVO> getUserInfo(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith(jwtUtil.getPrefix())) {
            token = token.substring(jwtUtil.getPrefix().length());
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        UserVO userInfo = userService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    @PutMapping("/info")
    @Operation(summary = "更新用户信息")
    public Result<Void> updateUserInfo(@RequestHeader("Authorization") String token,
                                       @RequestBody UpdateUserInfoDTO updateUserInfoDTO) {
        if (token != null && token.startsWith(jwtUtil.getPrefix())) {
            token = token.substring(jwtUtil.getPrefix().length());
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        userService.updateUserInfo(userId, updateUserInfoDTO);
        return Result.success();
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> updatePassword(@RequestHeader("Authorization") String token,
                                       @Valid @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        if (token != null && token.startsWith(jwtUtil.getPrefix())) {
            token = token.substring(jwtUtil.getPrefix().length());
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        userService.updatePassword(userId, updatePasswordDTO);
        return Result.success();
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传用户头像")
    public Result<String> uploadAvatar(@RequestHeader("Authorization") String token,
                                       @RequestParam("file") MultipartFile file) {
        if (token != null && token.startsWith(jwtUtil.getPrefix())) {
            token = token.substring(jwtUtil.getPrefix().length());
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (file == null || file.isEmpty()) {
            return Result.error("头像文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + suffix;

        File destDir = new File(uploadPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File destFile = new File(destDir, filename);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            return Result.error("头像上传失败：" + e.getMessage());
        }

        String avatarUrl = uploadUrlPrefix + filename;
        UpdateUserInfoDTO updateUserInfoDTO = new UpdateUserInfoDTO();
        updateUserInfoDTO.setAvatar(avatarUrl);
        userService.updateUserInfo(userId, updateUserInfoDTO);

        return Result.success(avatarUrl);
    }

}
