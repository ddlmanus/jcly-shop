package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 商品批量提审请求对象
 *
 * @author Han
 * @version 1.0.0
 * @Date 2025/3/12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ProductBatchAuditRequest", description = "商品批量提审请求对象")
public class ProductBatchAuditRequest extends CommonBatchRequest {

    private static final long serialVersionUID = -1115963599616978060L;

    @ApiModelProperty(value = "是否自动上架")
    private Boolean isAutoUp = false;
}
