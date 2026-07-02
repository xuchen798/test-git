package com.blog.service;

import com.blog.common.PageResult;
import com.blog.entity.SysLog;

public interface SysLogService {

    PageResult<SysLog> getLogList(Long page, Long size, String operation, String startTime, String endTime);

    void addLog(SysLog sysLog);

}
