package com.zbkj.common.response;

import com.zbkj.common.model.coze.CozeBot;
import com.zbkj.common.model.coze.CozeBotConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MiniCozeBotReponse extends CozeBot {
    @ApiModelProperty(value = "智能体配置")
    private CozeBotConfig cozeBotConfig;
}
