package com.zbkj.common.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 用户实名认证表
 * </p>
 *
 * @author System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_user_real_name_auth")
@ApiModel(value="UserRealNameAuth对象", description="用户实名认证表")
public class UserRealNameAuth implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "认证记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "认证类型 1=身份证OCR 2=人脸识别 3=OCR+人脸")
    private Integer authType;

    @ApiModelProperty(value = "真实姓名")
    private String realName;

    @ApiModelProperty(value = "身份证号(加密存储)")
    private String idCard;

    @ApiModelProperty(value = "身份证正面照片")
    private String idCardFrontImage;

    @ApiModelProperty(value = "身份证反面照片")
    private String idCardBackImage;

    @ApiModelProperty(value = "手持身份证照片")
    private String handHeldIdCardImage;

    @ApiModelProperty(value = "人脸识别照片")
    private String faceImage;

    @ApiModelProperty(value = "人脸识别视频")
    private String faceVideo;

    @ApiModelProperty(value = "性别 1=男 2=女")
    private Integer gender;

    @ApiModelProperty(value = "出生日期")
    private String birthday;

    @ApiModelProperty(value = "民族")
    private String nation;

    @ApiModelProperty(value = "地址")
    private String address;

    @ApiModelProperty(value = "发证机关")
    private String issuingAuthority;

    @ApiModelProperty(value = "身份证有效期")
    private String validPeriod;

    @ApiModelProperty(value = "OCR识别结果")
    private String ocrResult;

    @ApiModelProperty(value = "人脸识别结果")
    private String faceResult;

    @ApiModelProperty(value = "活体检测结果")
    private String livenessResult;

    @ApiModelProperty(value = "认证状态 0=待认证 1=认证中 2=认证成功 3=认证失败")
    private Integer authStatus;

    @ApiModelProperty(value = "认证时间")
    private Date authTime;

    @ApiModelProperty(value = "失败原因")
    private String failReason;

    @ApiModelProperty(value = "置信度分数")
    private java.math.BigDecimal confidenceScore;

    @ApiModelProperty(value = "审核人ID")
    private Integer auditorId;

    @ApiModelProperty(value = "审核时间")
    private Date auditTime;

    @ApiModelProperty(value = "审核备注")
    private String auditRemark;

    @ApiModelProperty(value = "IP地址")
    private String ipAddress;

    @ApiModelProperty(value = "用户代理")
    private String userAgent;

    @ApiModelProperty(value = "认证来源 1=APP 2=H5 3=小程序")
    private Integer authSource;

    @ApiModelProperty(value = "第三方认证订单号")
    private String thirdPartyOrderNo;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "是否删除 0=否 1=是")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
} 