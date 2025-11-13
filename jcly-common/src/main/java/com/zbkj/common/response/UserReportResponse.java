package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户报表响应类
 * +----------------------------------------------------------------------
 * | JCLY [ JCLY赋能开发者，助力企业发展 ]
 * +----------------------------------------------------------------------
 * | Copyright (c) 2016~2025 https://www.ddlmanus.xyz All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed JCLY并不是自由软件，未经许可不能去掉JCLY相关版权
 * +----------------------------------------------------------------------
 * | Author: dudl
 * +----------------------------------------------------------------------
 */
@Data
@ApiModel(value = "UserReportResponse", description = "用户报表响应")
public class UserReportResponse {

    @ApiModelProperty(value = "用户总数")
    private Integer userTotal;

    @ApiModelProperty(value = "小程序端用户数")
    private Integer miniProgramUsers;

    @ApiModelProperty(value = "H5端用户数")
    private Integer h5Users;

    @ApiModelProperty(value = "PC端用户数")
    private Integer pcUsers;

    @ApiModelProperty(value = "App端用户数")
    private Integer appUsers;

    @ApiModelProperty(value = "今日活跃用户数")
    private Integer todayActiveUsers;

    @ApiModelProperty(value = "昨日活跃用户数")
    private Integer yesterdayActiveUsers;

    @ApiModelProperty(value = "本周活跃用户数")
    private Integer weekActiveUsers;

    @ApiModelProperty(value = "本月活跃用户数")
    private Integer monthActiveUsers;

    @ApiModelProperty(value = "访客数(页面浏览量)")
    private Integer pageviews;

    @ApiModelProperty(value = "下单用户数")
    private Integer orderNum;

    @ApiModelProperty(value = "成交用户数")
    private Integer paidUserCount;

    @ApiModelProperty(value = "充值用户数")
    private Integer rechargeUserCount;

    @ApiModelProperty(value = "客单价")
    private BigDecimal avgOrderAmount;

    @ApiModelProperty(value = "注册用户增长率")
    private String registerGrowthRate;

    @ApiModelProperty(value = "活跃用户增长率")
    private String activeGrowthRate;

    @ApiModelProperty(value = "充值用户增长率")
    private String rechargeGrowthRate;

    @ApiModelProperty(value = "新增用户趋势")
    private List<UserTrendData> userTrend;

    @ApiModelProperty(value = "会员统计")
    private MemberStatistics memberStats;

    @ApiModelProperty(value = "用户消费排行")
    private List<UserConsumptionRanking> consumptionRanking;

    @Data
    @ApiModel(value = "UserTrendData", description = "用户趋势数据")
    public static class UserTrendData {
        @ApiModelProperty(value = "日期")
        private String date;

        @ApiModelProperty(value = "App端新增")
        private Integer appNewUsers;

        @ApiModelProperty(value = "小程序端新增")
        private Integer miniProgramNewUsers;

        @ApiModelProperty(value = "H5端新增")
        private Integer h5NewUsers;

        @ApiModelProperty(value = "PC端新增")
        private Integer pcNewUsers;

        @ApiModelProperty(value = "活跃用户数")
        private Integer activeUsers;
    }

    @Data
    @ApiModel(value = "MemberStatistics", description = "会员统计")
    public static class MemberStatistics {
        @ApiModelProperty(value = "开通会员数")
        private Integer openMembers;

        @ApiModelProperty(value = "过期会员数")
        private Integer expiredMembers;

        @ApiModelProperty(value = "续费会员数")
        private Integer renewedMembers;

        @ApiModelProperty(value = "月度会员趋势")
        private List<MonthlyMemberData> monthlyTrend;
    }

    @Data
    @ApiModel(value = "MonthlyMemberData", description = "月度会员数据")
    public static class MonthlyMemberData {
        @ApiModelProperty(value = "月份")
        private String month;

        @ApiModelProperty(value = "开通数量")
        private Integer openCount;

        @ApiModelProperty(value = "过期数量")
        private Integer expiredCount;

        @ApiModelProperty(value = "续费数量")
        private Integer renewedCount;
    }

    @Data
    @ApiModel(value = "UserConsumptionRanking", description = "用户消费排行")
    public static class UserConsumptionRanking {
        @ApiModelProperty(value = "排名")
        private Integer rank;

        @ApiModelProperty(value = "用户ID")
        private Integer userId;

        @ApiModelProperty(value = "用户昵称")
        private String nickname;

        @ApiModelProperty(value = "用户头像")
        private String avatar;

        @ApiModelProperty(value = "消费金额")
        private BigDecimal consumptionAmount;

        @ApiModelProperty(value = "订单数量")
        private Integer orderCount;

        @ApiModelProperty(value = "注册渠道")
        private String registerType;
    }
} 