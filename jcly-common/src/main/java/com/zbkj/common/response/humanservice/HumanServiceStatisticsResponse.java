package com.zbkj.common.response.humanservice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 人工客服数据统计响应类
 * @author AI Assistant
 * @since 2025-01-09
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "HumanServiceStatisticsResponse", description = "人工客服数据统计响应")
public class HumanServiceStatisticsResponse {

    @ApiModelProperty(value = "总体统计数据")
    private OverallStatistics overall;

    @ApiModelProperty(value = "客服回复统计列表")
    private List<StaffReplyStatistics> staffReplyStats;

    @ApiModelProperty(value = "每日消息统计")
    private List<DailyMessageStatistics> dailyMessageStats;

    @ApiModelProperty(value = "消息类型分布")
    private List<MessageTypeStatistics> messageTypeStats;

    @ApiModelProperty(value = "客服评价统计")
    private List<StaffRatingStatistics> staffRatingStats;

    @ApiModelProperty(value = "热门问题统计")
    private List<PopularQuestionStatistics> popularQuestions;

    /**
     * 总体统计数据
     */
    @Data
    @Accessors(chain = true)
    @ApiModel(value = "OverallStatistics", description = "总体统计数据")
    public static class OverallStatistics {
        @ApiModelProperty(value = "总消息数")
        private Long totalMessages;

        @ApiModelProperty(value = "今日消息数")
        private Long todayMessages;

        @ApiModelProperty(value = "活跃客服数")
        private Long activeStaffCount;

        @ApiModelProperty(value = "平均响应时间（秒）")
        private BigDecimal avgResponseTime;

        @ApiModelProperty(value = "客户满意度评分")
        private BigDecimal avgSatisfactionScore;

        @ApiModelProperty(value = "解决率")
        private BigDecimal resolutionRate;
    }

    /**
     * 客服回复统计
     */
    @Data
    @Accessors(chain = true)
    @ApiModel(value = "StaffReplyStatistics", description = "客服回复统计")
    public static class StaffReplyStatistics {
        @ApiModelProperty(value = "客服ID")
        private Long staffId;

        @ApiModelProperty(value = "客服姓名")
        private String staffName;

        @ApiModelProperty(value = "客服头像")
        private String staffAvatar;

        @ApiModelProperty(value = "今日回复数")
        private Long todayReplies;

        @ApiModelProperty(value = "本周回复数")
        private Long weekReplies;

        @ApiModelProperty(value = "本月回复数")
        private Long monthReplies;

        @ApiModelProperty(value = "平均响应时间（秒）")
        private BigDecimal avgResponseTime;

        @ApiModelProperty(value = "处理会话数")
        private Long handledSessions;

        @ApiModelProperty(value = "客户满意度评分")
        private BigDecimal satisfactionScore;

        @ApiModelProperty(value = "在线时长（分钟）")
        private Long onlineMinutes;
    }

    /**
     * 每日消息统计
     */
    @Data
    @Accessors(chain = true)
    @ApiModel(value = "DailyMessageStatistics", description = "每日消息统计")
    public static class DailyMessageStatistics {
        @ApiModelProperty(value = "日期")
        private String date;

        @ApiModelProperty(value = "用户消息数")
        private Long userMessages;

        @ApiModelProperty(value = "客服回复数")
        private Long staffReplies;

        @ApiModelProperty(value = "AI回复数")
        private Long aiReplies;

        @ApiModelProperty(value = "总消息数")
        private Long totalMessages;
    }

    /**
     * 消息类型统计
     */
    @Data
    @Accessors(chain = true)
    @ApiModel(value = "MessageTypeStatistics", description = "消息类型统计")
    public static class MessageTypeStatistics {
        @ApiModelProperty(value = "消息类型")
        private String messageType;

        @ApiModelProperty(value = "消息类型名称")
        private String messageTypeName;

        @ApiModelProperty(value = "消息数量")
        private Long count;

        @ApiModelProperty(value = "占比")
        private BigDecimal percentage;
    }

    /**
     * 客服评价统计
     */
    @Data
    @Accessors(chain = true)
    @ApiModel(value = "StaffRatingStatistics", description = "客服评价统计")
    public static class StaffRatingStatistics {
        @ApiModelProperty(value = "客服ID")
        private Long staffId;

        @ApiModelProperty(value = "客服姓名")
        private String staffName;

        @ApiModelProperty(value = "客服头像")
        private String staffAvatar;

        @ApiModelProperty(value = "总评价数")
        private Long totalRatings;

        @ApiModelProperty(value = "好评数")
        private Long goodRatings;

        @ApiModelProperty(value = "中评数")
        private Long averageRatings;

        @ApiModelProperty(value = "差评数")
        private Long badRatings;

        @ApiModelProperty(value = "好评率")
        private BigDecimal goodRatingRate;

        @ApiModelProperty(value = "平均评分")
        private BigDecimal avgScore;

        @ApiModelProperty(value = "排名")
        private Integer rank;
    }

    /**
     * 热门问题统计
     */
    @Data
    @Accessors(chain = true)
    @ApiModel(value = "PopularQuestionStatistics", description = "热门问题统计")
    public static class PopularQuestionStatistics {
        @ApiModelProperty(value = "问题关键词")
        private String keyword;

        @ApiModelProperty(value = "问题数量")
        private Long count;

        @ApiModelProperty(value = "占比")
        private BigDecimal percentage;

        @ApiModelProperty(value = "平均解决时间（分钟）")
        private BigDecimal avgSolutionTime;
    }
}


