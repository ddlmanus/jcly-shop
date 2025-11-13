package com.zbkj.common.request.groupbuy;

import com.zbkj.common.request.PageParamRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 拼团商品分类查询请求参数
 *
 * @author dazongzi
 * @version 1.0.0
 * @Date 2024-12-5
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "GroupBuyProductCategorySearchRequest", description = "拼团商品分类查询请求参数")
public class GroupBuyProductCategorySearchRequest extends PageParamRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品平台分类ID", required = true)
    @NotNull(message = "商品平台分类ID不能为空")
    private Integer categoryId;

    @ApiModelProperty(value = "排序字段：price-价格，sales-销量，rating-评价数，stock-库存数量")
    private String sortField;

    @ApiModelProperty(value = "排序方式：asc-升序，desc-降序，默认desc")
    private String sortOrder = "desc";
}