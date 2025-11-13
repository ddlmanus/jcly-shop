package com.zbkj.common.enums;

/**
 * 人工客服相关枚举
 * @author AI Assistant
 * @since 2025-01-09
 */
public class HumanServiceEnum {

    /**
     * 客服在线状态枚举
     */
    public enum OnlineStatus {
        ONLINE("ONLINE", "在线"),
        BUSY("BUSY", "忙碌"),
        AWAY("AWAY", "离开"),
        OFFLINE("OFFLINE", "离线");

        private final String code;
        private final String desc;

        OnlineStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 服务等级枚举
     */
    public enum ServiceLevel {
        JUNIOR("JUNIOR", "初级"),
        STANDARD("STANDARD", "标准"),
        SENIOR("SENIOR", "高级"),
        EXPERT("EXPERT", "专家");

        private final String code;
        private final String desc;

        ServiceLevel(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 会话状态枚举
     */
    public enum SessionStatus {
        WAITING("WAITING", "等待中"),
        ACTIVE("ACTIVE", "进行中"),
        ENDED("ENDED", "已结束"),
        CLOSED("CLOSED", "已关闭");

        private final String code;
        private final String desc;

        SessionStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 优先级枚举
     */
    public enum Priority {
        LOW("LOW", "低"),
        NORMAL("NORMAL", "普通"),
        HIGH("HIGH", "高"),
        URGENT("URGENT", "紧急");

        private final String code;
        private final String desc;

        Priority(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 用户类型枚举
     */
    public enum UserType {
        USER("USER", "用户"),
        MERCHANT("MERCHANT", "商户"),
        PLATFORM("PLATFORM", "平台");

        private final String code;
        private final String desc;

        UserType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 联系人类型枚举
     */
    public enum ContactType {
        USER("USER", "用户"),
        MERCHANT("MERCHANT", "商户"),
        PLATFORM("PLATFORM", "平台");

        private final String code;
        private final String desc;

        ContactType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        NEW_MESSAGE("NEW_MESSAGE", "新消息"),
        SESSION_TRANSFER("SESSION_TRANSFER", "会话转接"),
        SYSTEM_NOTICE("SYSTEM_NOTICE", "系统通知"),
        USER_ONLINE("USER_ONLINE", "用户上线"),
        USER_OFFLINE("USER_OFFLINE", "用户离线");

        private final String code;
        private final String desc;

        NotificationType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
