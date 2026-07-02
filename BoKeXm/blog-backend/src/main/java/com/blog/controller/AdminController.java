package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.common.ResultCode;
import com.blog.entity.SysLog;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.service.AdminService;
import com.blog.service.SysLogService;
import com.blog.vo.StatisticsVO;
import com.blog.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin")
@Tag(name = "管理模块")
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private SysLogService sysLogService;

    @Resource
    private UserMapper userMapper;

    @GetMapping("/user/list")
    @Operation(summary = "用户列表")
    public Result<PageResult<UserVO>> getUserList(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        if (!checkAdmin(request)) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        PageResult<UserVO> result = adminService.getUserList(page, size, username, status);
        return Result.success(result);
    }

    @PutMapping("/user/{id}/status")
    @Operation(summary = "更新用户状态")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                         @RequestParam Integer status,
                                         HttpServletRequest request) {
        if (!checkAdmin(request)) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        adminService.updateUserStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/user/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        if (!checkAdmin(request)) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        adminService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/log/list")
    @Operation(summary = "系统日志列表")
    public Result<PageResult<SysLog>> getLogList(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {
        if (!checkAdmin(request)) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        PageResult<SysLog> result = sysLogService.getLogList(page, size, operation, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/statistics")
    @Operation(summary = "统计信息")
    public Result<StatisticsVO> getStatistics(HttpServletRequest request) {
        if (!checkAdmin(request)) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        StatisticsVO statistics = adminService.getStatistics();
        return Result.success(statistics);
    }

    private boolean checkAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return false;
        }
        User user = userMapper.selectById(userId);
        return user != null && user.getRole() != null && user.getRole() == 1;
    }

}
