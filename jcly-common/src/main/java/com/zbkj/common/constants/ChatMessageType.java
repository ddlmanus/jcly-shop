package com.zbkj.common.constants;

/**
 * 聊天消息类型常量
 * 企业级聊天系统支持的所有消息类型
 * 
 * @author AI Assistant
 * @since 2025-01-09
 */
public class ChatMessageType {
    
    // 基础消息类型
    public static final String TEXT = "text";
    public static final String IMAGE = "image";
    public static final String FILE = "file";
    public static final String VOICE = "voice";
    public static final String VIDEO = "video";
    
    // 业务消息类型
    public static final String PRODUCT_CARD = "product_card";
    public static final String ORDER_CARD = "order_card";
    public static final String COUPON_CARD = "coupon_card";
    public static final String ACTIVITY_CARD = "activity_card";
    
    // 系统消息类型
    public static final String SYSTEM_NOTICE = "system_notice";
    public static final String QUICK_REPLY = "quick_reply";
    public static final String TYPING_STATUS = "typing_status";
    public static final String READ_STATUS = "read_status";
    
    // AI消息类型
    public static final String AI_REPLY = "ai_reply";
    public static final String AI_THINKING = "ai_thinking";
    public static final String AI_SUGGESTION = "ai_suggestion";
    
    // 特殊消息类型
    public static final String LOCATION = "location";
    public static final String CONTACT = "contact";
    public static final String LINK = "link";
    public static final String RICH_TEXT = "rich_text";
}
