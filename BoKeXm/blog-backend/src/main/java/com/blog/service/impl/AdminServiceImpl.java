package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.common.RedisKeyConstants;
import com.blog.common.RedisUtil;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.AdminService;
import com.blog.vo.StatisticsVO;
import com.blog.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private RedisUtil redisUtil;

    private static final String VIEW_COUNT_KEY_PREFIX = RedisKeyConstants.VIEW_COUNT;

    @Override
    public PageResult<UserVO> getUserList(Long page, Long size, String username, Integer status) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        if (username != null && !username.isEmpty()) {
            queryWrapper.like(User::getUsername, username);
        }

        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }

        queryWrapper.orderByDesc(User::getCreateTime);

        Page<User> pageParam = new Page<>(page, size);
        Page<User> resultPage = userMapper.selectPage(pageParam, queryWrapper);

        List<UserVO> userVOList = Collections.emptyList();
        if (resultPage.getRecords() != null && !resultPage.getRecords().isEmpty()) {
            userVOList = resultPage.getRecords().stream()
                    .map(user -> {
                        UserVO userVO = new UserVO();
                        BeanUtils.copyProperties(user, userVO);
                        return userVO;
                    })
                    .collect(Collectors.toList());
        }

        return PageResult.of(
                resultPage.getTotal(),
                userVOList,
                page.intValue(),
                size.intValue()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        if (userId == null || status == null) {
            throw new BusinessException("参数错误");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setStatus(status);
        userMapper.updateById(updateUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("参数错误");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        userMapper.deleteById(userId);
    }

    @Override
    public StatisticsVO getStatistics() {
        StatisticsVO statisticsVO = new StatisticsVO();

        Long userCount = userMapper.selectCount(null);
        statisticsVO.setUserCount(userCount);

        Long articleCount = articleMapper.selectCount(null);
        statisticsVO.setArticleCount(articleCount);

        Long commentCount = commentMapper.selectCount(null);
        statisticsVO.setCommentCount(commentCount);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String viewCountKey = VIEW_COUNT_KEY_PREFIX + today;
        Object viewCountObj = redisUtil.get(viewCountKey);
        Long todayViewCount = 0L;
        if (viewCountObj != null) {
            todayViewCount = Long.valueOf(viewCountObj.toString());
        }
        statisticsVO.setTodayViewCount(todayViewCount);

        return statisticsVO;
    }

}
