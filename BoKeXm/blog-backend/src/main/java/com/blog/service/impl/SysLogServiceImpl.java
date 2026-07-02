package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.entity.SysLog;
import com.blog.exception.BusinessException;
import com.blog.mapper.SysLogMapper;
import com.blog.service.SysLogService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Service
public class SysLogServiceImpl implements SysLogService {

    @Resource
    private SysLogMapper sysLogMapper;

    @Override
    public PageResult<SysLog> getLogList(Long page, Long size, String operation, String startTime, String endTime) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        LambdaQueryWrapper<SysLog> queryWrapper = new LambdaQueryWrapper<>();

        if (operation != null && !operation.isEmpty()) {
            queryWrapper.like(SysLog::getOperation, operation);
        }

        if (startTime != null && !startTime.isEmpty()) {
            LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.ge(SysLog::getCreateTime, start);
        }

        if (endTime != null && !endTime.isEmpty()) {
            LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.le(SysLog::getCreateTime, end);
        }

        queryWrapper.orderByDesc(SysLog::getCreateTime);

        Page<SysLog> pageParam = new Page<>(page, size);
        Page<SysLog> resultPage = sysLogMapper.selectPage(pageParam, queryWrapper);

        return PageResult.of(
                resultPage.getTotal(),
                resultPage.getRecords() != null ? resultPage.getRecords() : Collections.emptyList(),
                page.intValue(),
                size.intValue()
        );
    }

    @Override
    public void addLog(SysLog sysLog) {
        if (sysLog == null) {
            throw new BusinessException("参数错误");
        }
        if (sysLog.getCreateTime() == null) {
            sysLog.setCreateTime(LocalDateTime.now());
        }
        sysLogMapper.insert(sysLog);
    }

}
