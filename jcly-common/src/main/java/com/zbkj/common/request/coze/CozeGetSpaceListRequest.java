package com.zbkj.common.request.coze;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CozeGetSpaceListRequest对象", description = "查看空间列表请求参数")
public class CozeGetSpaceListRequest {
    
    @ApiModelProperty(value = "分页查询时的页码，默认为 1")
    @JsonProperty("page_num")
    private Integer pageNum;
    
    @ApiModelProperty(value = "分页大小，即每页返回多少个工作空间，默认为 20，最大为 50")
    @JsonProperty("page_size")
    private Integer pageSize;
    
    @ApiModelProperty(value = "企业 ID，用于查询指定企业中的工作空间")
    @JsonProperty("enterprise_id")
    private String enterpriseId;
    
    @ApiModelProperty(value = "扣子用户 ID，用于查询特定用户的工作空间信息")
    @JsonProperty("user_id")
    private String userId;
    
    @ApiModelProperty(value = "扣子的组织 ID，用于查询特定组织的工作空间信息")
    @JsonProperty("coze_account_id")
    private String cozeAccountId;
}
