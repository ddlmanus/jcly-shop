package com.zbkj.common.model.product;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_Product_template")
@ApiModel(value="商品模版对象", description="商品模版表")
public class ProductTemplate {
    @ApiModelProperty(value = "模版ID")
    private Integer id;
    @ApiModelProperty(value = "模版名称")
    private String name;
    @ApiModelProperty(value = "模版内容")
    private String content;
    @ApiModelProperty(value = "模版状态")
    private Boolean status;
    @ApiModelProperty(value = "模版排序")
    private Integer sort;
    @ApiModelProperty(value = "模版创建时间")
    private String createTime;
    @ApiModelProperty(value = "模版更新时间")
    private String updateTime;
}
