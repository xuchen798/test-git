package com.blog.service;

import com.blog.common.PageResult;
import com.blog.vo.StatisticsVO;
import com.blog.vo.UserVO;

public interface AdminService {

    PageResult<UserVO> getUserList(Long page, Long size, String username, Integer status);

    void updateUserStatus(Long userId, Integer status);

    void deleteUser(Long userId);

    StatisticsVO getStatistics();

}
