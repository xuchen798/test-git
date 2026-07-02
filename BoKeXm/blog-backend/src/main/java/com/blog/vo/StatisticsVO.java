package com.blog.vo;

import lombok.Data;

@Data
public class StatisticsVO {

    private Long userCount;

    private Long articleCount;

    private Long commentCount;

    private Long todayViewCount;

}
