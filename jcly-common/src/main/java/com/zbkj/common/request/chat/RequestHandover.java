package com.zbkj.common.request.chat;

import lombok.Data;

@Data
public class RequestHandover {
    private String sessionId;
    private String reason;
    private String urgency;
}
