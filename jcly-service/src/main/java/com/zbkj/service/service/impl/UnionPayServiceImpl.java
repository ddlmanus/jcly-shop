package com.zbkj.service.service.impl;


import com.alibaba.fastjson.JSON;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.model.DemoBase;
import com.zbkj.common.model.order.Order;
import com.zbkj.common.model.user.UserBankCard;
import com.zbkj.common.unionpay.AcpService;
import com.zbkj.common.unionpay.SDKConfig;
import com.zbkj.common.utils.CrmebDateUtil;
import com.zbkj.common.utils.RestTemplateUtil;
import com.zbkj.common.vo.BankCardVerifyRequestVo;
import com.zbkj.common.vo.BankCardVerifyResponseVo;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.UnionPayService;
import org.springframework.util.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class UnionPayServiceImpl implements UnionPayService {
	private static final Logger logger = LoggerFactory.getLogger(UnionPayServiceImpl.class);
	@Autowired
	private SystemConfigService systemConfigService;
	
	@Autowired
	private RestTemplateUtil restTemplateUtil;


	/**
	 * 银联支付（简单版本，用于测试）
	 * @param order
	 * @return
	 */
	@Override
	public String unionPay(Order order) {
		return unionPay(order, null, "111111");
	}

	/**
	 * 银联支付（完整版本，支持银行卡信息）
	 * @param order 订单信息
	 * @param bankCard 银行卡信息
	 * @param smsCode 短信验证码
	 * @return
	 */
	@Override
	public String unionPay(Order order, UserBankCard bankCard, String smsCode) {
		try {
			// 参数校验
			if (order == null) {
				logger.error("银联支付发起失败：订单对象为空");
				throw new RuntimeException("订单对象不能为空");
			}
			if (order.getOrderNo() == null || order.getOrderNo().trim().isEmpty()) {
				logger.error("银联支付发起失败：订单号为空");
				throw new RuntimeException("订单号不能为空");
			}
			if (order.getPayPrice() == null || order.getPayPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
				logger.error("银联支付发起失败：订单金额无效，订单号：{}, 金额：{}", order.getOrderNo(), order.getPayPrice());
				throw new RuntimeException("订单金额无效");
			}

			Map<String, String> requestData = new HashMap<String, String>();
			
			/***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
			requestData.put("version", DemoBase.version);   			  //版本号，全渠道默认值
			requestData.put("encoding", DemoBase.encoding); 	  //字符集编码，可以使用UTF-8,GBK两种方式
			requestData.put("signMethod", "01");            			  //签名方法，只支持 01：RSA方式证书加密
			requestData.put("txnType", "01");               			  //交易类型 ，01：消费
			requestData.put("txnSubType", "01");            			  //交易子类型， 01：自助消费
			
			// 使用基础的B2C网关支付（与官方demo一致，避免权限问题）
			requestData.put("bizType", "000201");           		  //业务类型，B2C网关支付，手机wap支付
			
			//渠道类型，这个字段区分B2C网关支付和手机wap支付；07：PC,平板  08：手机
			if(order.getPayType() != null && order.getPayType().equals("unionpay_mobile")){//手机支付
				requestData.put("channelType", "08");     
			}else{//PC支付
				requestData.put("channelType", "07");
			}
			
			// 前台回调地址(自定义)
			String frontUrl;
			String notifyUrl;
			String unionMerId;
			
			try {
				frontUrl = systemConfigService.getValueByKeyException("union_front_url");
				notifyUrl = systemConfigService.getValueByKeyException("union_notify_url");
				unionMerId = systemConfigService.getValueByKeyException("union_merId");
			} catch (Exception e) {
				logger.error("银联支付配置获取失败：{}", e.getMessage());
				throw new RuntimeException("银联支付配置不完整，请检查系统配置");
			}
			
			requestData.put("frontUrl", frontUrl);
			
			/***商户接入参数***/
			requestData.put("merId", unionMerId);    	          //商户号码，请改成自己申请的正式商户号或者open上注册得来的777测试商户号
			requestData.put("accessType", "0");             			  //接入类型，0：直连商户 
			requestData.put("orderId", order.getOrderNo());          //商户订单号，8-40位数字字母，不能含"-"或"_"，可以自行定制规则
			requestData.put("txnTime", DemoBase.getCurrentTime());     //订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
			requestData.put("currencyCode", "156");         			  //交易币种（境内商户一般是156 人民币）
			
			// 交易金额，单位分，不要带小数点 - 修正金额处理
			Long amountInCents = order.getPayPrice().multiply(new java.math.BigDecimal(100)).longValue();
			if (amountInCents <= 0) {
				logger.error("银联支付发起失败：计算后的金额无效，订单号：{}, 原金额：{}, 计算后金额：{}分", 
					order.getOrderNo(), order.getPayPrice(), amountInCents);
				throw new RuntimeException("支付金额计算错误");
			}
			requestData.put("txnAmt", amountInCents.toString());
			
			// 订单描述信息
			requestData.put("orderDesc", "商城订单-" + order.getOrderNo());
			
			//请求方保留域，透传字段（可以实现商户自定义参数的追踪）
			requestData.put("reqReserved", order.getOrderNo());
			
			//后台通知地址
			requestData.put("backUrl", notifyUrl);

			// 暂时移除无跳转支付功能，避免权限问题
			// 使用标准的跳转支付，与官方demo保持一致
			if (bankCard != null) {
				logger.info("注意：当前使用跳转支付模式，银行卡信息将在银联页面输入");
			}
			
			logger.info("银联支付请求参数：订单号={}, 金额={}分, 商户号={}", order.getOrderNo(), amountInCents, unionMerId);
			
			/**请求参数设置完毕，以下对请求参数进行签名并生成html表单，将表单写入浏览器跳转打开银联页面**/
			Map<String, String> submitFromData = AcpService.sign(requestData,DemoBase.encoding);  //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
			
			if (submitFromData == null || submitFromData.isEmpty()) {
				logger.error("银联支付签名失败，订单号：{}", order.getOrderNo());
				throw new RuntimeException("银联支付签名失败，请检查证书配置");
			}
			
			String requestFrontUrl = SDKConfig.getConfig().getFrontRequestUrl();  //获取请求银联的前台地址：对应属性文件acp_sdk.properties文件中的acpsdk.frontTransUrl
			if (requestFrontUrl == null || requestFrontUrl.trim().isEmpty()) {
				logger.error("银联支付前台请求地址未配置，订单号：{}", order.getOrderNo());
				throw new RuntimeException("银联支付前台请求地址未配置");
			}
			
			String form = AcpService.createAutoFormHtml(requestFrontUrl, submitFromData,DemoBase.encoding);   //生成自动跳转的Html表单
			
			if (form == null || form.trim().isEmpty()) {
				logger.error("银联支付HTML表单生成失败，订单号：{}", order.getOrderNo());
				throw new RuntimeException("银联支付HTML表单生成失败");
			}
			
			logger.info("银联支付请求HTML生成成功，订单号：{}", order.getOrderNo());
			//将生成的html写到浏览器中完成自动跳转打开银联支付页面；这里调用signData之后，将html写到浏览器跳转到银联页面之前均不能对html中的表单项的名称和值进行修改，如果修改会导致验签不通过
			return form;
		} catch (Exception e) {
			logger.error("银联支付发起异常，订单号：{}", order != null ? order.getOrderNo() : "未知", e);
			throw new RuntimeException("银联支付发起失败：" + e.getMessage(), e);
		}
	}

	@Override
	public String validate(Map<String, String> valideData, String encoding) {
		String message = Constants.SUCCESS;
		if (!AcpService.validate(valideData, encoding)) {
			message = Constants.FAIL;
		}
		return message;
	}
	/**
	 * 查询银联支付订单状态
	 * @param orderId 商户订单号
	 * @param txnTime 原交易时间
	 * @return 查询结果
	 */
	@Override
	public Map<String, String> queryOrder(String orderId, String txnTime) {
		Map<String, String> requestData = new HashMap<String, String>();
		
		/***银联全渠道系统，产品参数***/
		requestData.put("version", DemoBase.version);
		requestData.put("encoding", DemoBase.encoding);
		requestData.put("signMethod", "01");
		requestData.put("txnType", "00");               // 交易类型，00：查询交易
		requestData.put("txnSubType", "00");            // 交易子类型，00：默认
		requestData.put("bizType", "000000");           // 业务类型，000000：查询
		
		/***商户接入参数***/
		String unionMerId = systemConfigService.getValueByKeyException("union_merId");
		requestData.put("merId", unionMerId);
		requestData.put("accessType", "0");             // 接入类型，0：直连商户
		requestData.put("orderId", orderId);            // 商户订单号
		requestData.put("txnTime", txnTime);            // 原交易时间
		
		logger.info("银联支付订单查询请求参数：订单号={}, 交易时间={}, 商户号={}", orderId, txnTime, unionMerId);
		
		// 签名
		Map<String, String> submitData = AcpService.sign(requestData, DemoBase.encoding);
		
		// 发送查询请求
		String queryUrl = SDKConfig.getConfig().getSingleQueryUrl();
		Map<String, String> responseData = AcpService.post(submitData, queryUrl, DemoBase.encoding);
		
		if (responseData != null && !responseData.isEmpty()) {
			// 验证签名
			if (AcpService.validate(responseData, DemoBase.encoding)) {
				logger.info("银联支付订单查询成功，订单号：{}，应答码：{}", orderId, responseData.get("respCode"));
				return responseData;
			} else {
				logger.error("银联支付订单查询验签失败，订单号：{}", orderId);
			}
		} else {
			logger.error("银联支付订单查询无响应，订单号：{}", orderId);
		}
		
		return null;
	}

	/**
	 * 银联支付退款（消费撤销）
	 * @param orderId 原订单号
	 * @param origTxnTime 原交易时间
	 * @param refundAmount 退款金额
	 * @return 退款结果
	 */
	@Override
	public Map<String, String> refundOrder(String orderId, String origTxnTime, BigDecimal refundAmount) {
		logger.info("银联退款请求开始，订单号: {}, 原交易时间: {}, 退款金额: {}", orderId, origTxnTime, refundAmount);
		
		Map<String, String> requestData = new HashMap<String, String>();
		
		try {
			/***银联全渠道系统，产品参数***/
			requestData.put("version", DemoBase.version);
			requestData.put("encoding", DemoBase.encoding);
			requestData.put("signMethod", "01");
			requestData.put("txnType", "31");               // 交易类型，31：消费撤销
			requestData.put("txnSubType", "00");            // 交易子类型，00：默认
			requestData.put("bizType", "000201");           // 业务类型
			requestData.put("channelType", "07");           // 渠道类型
			
			/***商户接入参数***/
			String unionMerId = systemConfigService.getValueByKeyException("union_merId");
			requestData.put("merId", unionMerId);
			requestData.put("accessType", "0");             // 接入类型，0：直连商户
			requestData.put("orderId", orderId + "_R" + System.currentTimeMillis()); // 退款订单号，加后缀避免重复
			requestData.put("txnTime", CrmebDateUtil.nowDateTimeStr());    // 退款交易时间
			requestData.put("txnAmt", refundAmount.multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_HALF_UP).toString()); // 退款金额（分）
			requestData.put("currencyCode", "156");         // 交易币种，156：人民币
			
			/***原交易信息***/
			requestData.put("origQryId", "");               // 原交易查询流水号，如果有的话
			requestData.put("origOrderId", orderId);        // 原订单号
			requestData.put("origTxnTime", origTxnTime);    // 原交易时间
			
			/***后台通知地址***/
			String backUrl = systemConfigService.getValueByKeyException("union_backUrl");
			if (backUrl.contains("callback")) {
				backUrl = backUrl.replace("callback", "refund-callback");
			} else {
				backUrl = backUrl + "/refund";
			}
			requestData.put("backUrl", backUrl);
			
			// 请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文
			Map<String, String> submitFromData = AcpService.sign(requestData, DemoBase.encoding);
			String requestUrl = SDKConfig.getConfig().getBackRequestUrl();
			
			logger.info("银联退款请求URL: {}", requestUrl);
			logger.info("银联退款请求参数: {}", submitFromData);
			
			Map<String, String> responseData = AcpService.post(submitFromData, requestUrl, DemoBase.encoding);
			
			if (responseData != null) {
				logger.info("银联退款响应: {}", responseData);
				
				// 验证签名
				if (AcpService.validate(responseData, DemoBase.encoding)) {
					String respCode = responseData.get("respCode");
					String respMsg = responseData.get("respMsg");
					
					if ("00".equals(respCode)) {
						logger.info("银联退款成功，订单号: {}, 退款金额: {}", orderId, refundAmount);
						responseData.put("refundStatus", "SUCCESS");
						responseData.put("refundMessage", "退款成功");
					} else {
						logger.warn("银联退款失败，订单号: {}, 错误码: {}, 错误信息: {}", orderId, respCode, respMsg);
						responseData.put("refundStatus", "FAILED");
						responseData.put("refundMessage", respMsg);
					}
				} else {
					logger.error("银联退款响应签名验证失败，订单号: {}", orderId);
					responseData.put("refundStatus", "FAILED");
					responseData.put("refundMessage", "响应签名验证失败");
				}
			} else {
				logger.error("银联退款响应为空，订单号: {}", orderId);
				Map<String, String> errorResponse = new HashMap<>();
				errorResponse.put("refundStatus", "FAILED");
				errorResponse.put("refundMessage", "网络异常，响应为空");
				return errorResponse;
			}
			
			return responseData;
			
		} catch (Exception e) {
			logger.error("银联退款异常，订单号: " + orderId, e);
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("refundStatus", "FAILED");
			errorResponse.put("refundMessage", "退款异常：" + e.getMessage());
			return errorResponse;
		}
	}

	/**
	 * 银联支付退货
	 * @param origQryId 原交易查询流水号
	 * @param returnAmount 退货金额
	 * @return 退货结果
	 */
	@Override
	public Map<String, String> returnOrder(String origQryId, BigDecimal returnAmount) {
		logger.info("银联退货请求开始，原交易查询流水号: {}, 退货金额: {}", origQryId, returnAmount);
		
		Map<String, String> requestData = new HashMap<String, String>();
		
		try {
			/***银联全渠道系统，产品参数***/
			requestData.put("version", DemoBase.version);
			requestData.put("encoding", DemoBase.encoding);
			requestData.put("signMethod", "01");
			requestData.put("txnType", "04");               // 交易类型，04：退货
			requestData.put("txnSubType", "00");            // 交易子类型，00：默认
			requestData.put("bizType", "000000");           // 业务类型，000000：默认
			requestData.put("channelType", "07");           // 渠道类型，07：PC，08：手机
			
			/***商户接入参数***/
			String unionMerId = systemConfigService.getValueByKeyException("union_merId");
			requestData.put("merId", unionMerId);
			requestData.put("accessType", "0");             // 接入类型，0：直连商户
			requestData.put("orderId", "RT" + System.currentTimeMillis()); // 退货订单号，新的订单号
			requestData.put("txnTime", CrmebDateUtil.nowDateTimeStr());    // 退货交易时间
			requestData.put("txnAmt", returnAmount.multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_HALF_UP).toString()); // 退货金额（分）
			requestData.put("currencyCode", "156");         // 交易币种，156：人民币
			
			/***原交易信息***/
			requestData.put("origQryId", origQryId);        // 原交易查询流水号（必填）
			
			/***后台通知地址***/
			String backUrl = systemConfigService.getValueByKeyException("union_backUrl");
			if (backUrl.contains("callback")) {
				backUrl = backUrl.replace("callback", "return-callback");
			} else {
				backUrl = backUrl + "/return";
			}
			requestData.put("backUrl", backUrl);
			
			// 请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文
			Map<String, String> submitFromData = AcpService.sign(requestData, DemoBase.encoding);
			String requestUrl = SDKConfig.getConfig().getBackRequestUrl();
			
			logger.info("银联退货请求URL: {}", requestUrl);
			logger.info("银联退货请求参数: {}", submitFromData);
			
			Map<String, String> responseData = AcpService.post(submitFromData, requestUrl, DemoBase.encoding);
			
			if (responseData != null) {
				logger.info("银联退货响应: {}", responseData);
				
				// 验证签名
				if (AcpService.validate(responseData, DemoBase.encoding)) {
					String respCode = responseData.get("respCode");
					String respMsg = responseData.get("respMsg");
					
					if ("00".equals(respCode)) {
						logger.info("银联退货成功，原查询流水号: {}, 退货金额: {}", origQryId, returnAmount);
						responseData.put("returnStatus", "SUCCESS");
						responseData.put("returnMessage", "退货成功");
					} else if ("03".equals(respCode) || "04".equals(respCode) || "05".equals(respCode)) {
						logger.info("银联退货处理中，原查询流水号: {}, 状态码: {}", origQryId, respCode);
						responseData.put("returnStatus", "PROCESSING");
						responseData.put("returnMessage", "退货处理中，请稍后查询结果");
					} else {
						logger.warn("银联退货失败，原查询流水号: {}, 错误码: {}, 错误信息: {}", origQryId, respCode, respMsg);
						responseData.put("returnStatus", "FAILED");
						responseData.put("returnMessage", respMsg);
					}
				} else {
					logger.error("银联退货响应签名验证失败，原查询流水号: {}", origQryId);
					responseData.put("returnStatus", "FAILED");
					responseData.put("returnMessage", "响应签名验证失败");
				}
			} else {
				logger.error("银联退货响应为空，原查询流水号: {}", origQryId);
				Map<String, String> errorResponse = new HashMap<>();
				errorResponse.put("returnStatus", "FAILED");
				errorResponse.put("returnMessage", "网络异常，响应为空");
				return errorResponse;
			}
			
			return responseData;
			
		} catch (Exception e) {
			logger.error("银联退货异常，原查询流水号: " + origQryId, e);
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("returnStatus", "FAILED");
			errorResponse.put("returnMessage", "退货异常：" + e.getMessage());
			return errorResponse;
		}
	}

	/**
	 * 银行卡信息验证
	 * @param request 验证请求
	 * @return 验证结果
	 */
	@Override
	public BankCardVerifyResponseVo verifyBankCard(BankCardVerifyRequestVo request) {
		logger.info("银行卡验证请求开始，验证类型: {}, 卡号: {}", request.getVerifyType(), 
				maskCardNo(request.getCardNo()));
		
		BankCardVerifyResponseVo response = new BankCardVerifyResponseVo();
		
		try {
			// 获取系统配置
			String appId = systemConfigService.getValueByKeyException("union_merId");
			String appPwd = systemConfigService.getValueByKeyException("union_appPwd");
			
			// 构建请求参数
			Map<String, Object> requestData = new HashMap<>();
			requestData.put("appId", appId);
			requestData.put("appPwd", DigestUtils.md5DigestAsHex(appPwd.getBytes())); // MD5加密
			
			// 构建data参数
			Map<String, Object> data = new HashMap<>();
			data.put("cardNo", request.getCardNo());
			data.put("cardholderName", request.getCardholderName());
			data.put("idCard", request.getIdCard());
			
			// 根据验证类型添加不同的参数
			if ("4".equals(request.getVerifyType()) || "6".equals(request.getVerifyType())) {
				if (StringUtils.isEmpty(request.getMobile())) {
					response.setResponseCode("99");
					response.setMessage("4要素或6要素验证时手机号不能为空");
					response.setVerifyResultByCode();
					return response;
				}
				data.put("mobile", request.getMobile());
			}
			
			if ("6".equals(request.getVerifyType())) {
				if (StringUtils.isEmpty(request.getCvn2()) || StringUtils.isEmpty(request.getExpired())) {
					response.setResponseCode("99");
					response.setMessage("6要素验证时CVN2码和有效期不能为空");
					response.setVerifyResultByCode();
					return response;
				}
				data.put("cvn2", request.getCvn2());
				data.put("expired", request.getExpired());
			}
			
			data.put("verifyType", request.getVerifyType());
			requestData.put("data", data);
			
			// 发送请求
			String requestUrl = "https://auth.95516.com/authonl/onlineAuth/rest/verify/bankcard";
			logger.info("银行卡验证请求URL: {}", requestUrl);
			logger.info("银行卡验证请求参数: {}", JSON.toJSONString(requestData));
			
			String responseJson = restTemplateUtil.postMapData(requestUrl, requestData);
			logger.info("银行卡验证响应: {}", responseJson);
			
			if (StringUtils.isEmpty(responseJson)) {
				response.setResponseCode("99");
				response.setMessage("网络异常，响应为空");
				response.setVerifyResultByCode();
				return response;
			}
			
			// 解析响应
			@SuppressWarnings("unchecked")
			Map<String, Object> responseMap = JSON.parseObject(responseJson, Map.class);
			String responseCode = (String) responseMap.get("responseCode");
			String message = (String) responseMap.get("message");
			
			response.setResponseCode(responseCode);
			response.setMessage(message);
			response.setVerifyResultByCode();
			
			// 如果验证成功，尝试解析更多信息
			if ("00".equals(responseCode)) {
				@SuppressWarnings("unchecked")
				Map<String, Object> responseData = (Map<String, Object>) responseMap.get("data");
				if (responseData != null) {
					response.setBankName((String) responseData.get("bankName"));
					response.setCardType((String) responseData.get("cardType"));
					response.setDetail((String) responseData.get("detail"));
				}
			}
			
			logger.info("银行卡验证完成，验证结果: {}, 响应码: {}", response.getVerifyResult(), responseCode);
			return response;
			
		} catch (Exception e) {
			logger.error("银行卡验证异常", e);
			response.setResponseCode("99");
			response.setMessage("验证异常：" + e.getMessage());
			response.setVerifyResultByCode();
			return response;
		}
	}
	
	/**
	 * 脱敏银行卡号
	 */
	private String maskCardNo(String cardNo) {
		if (StringUtils.isEmpty(cardNo) || cardNo.length() < 8) {
			return cardNo;
		}
		return cardNo.substring(0, 4) + "****" + cardNo.substring(cardNo.length() - 4);
	}

	/**
	 * 构建customerInfo字段
	 * @param bankCard 银行卡信息
	 * @param smsCode 短信验证码
	 * @return customerInfo字符串
	 */
	private String buildCustomerInfo(UserBankCard bankCard, String smsCode) {
		try {
			Map<String, String> customerInfoMap = new HashMap<>();
			
			// 短信验证码（测试环境固定为111111）
			customerInfoMap.put("smsCode", StringUtils.isEmpty(smsCode) ? "111111" : smsCode);
			
			// 如果有真实的银行卡信息，添加相关字段
			if (bankCard != null) {
				// 证件类型（01：身份证）
				customerInfoMap.put("certifTp", "01");
				
				// 证件号码（解密身份证号）
				if (!StringUtils.isEmpty(bankCard.getIdCard())) {
					// 这里需要解密身份证号，暂时使用原值
					customerInfoMap.put("certifId", bankCard.getIdCard());
				}
				
				// 姓名（解密姓名）
				if (!StringUtils.isEmpty(bankCard.getCardholderName())) {
					// 这里需要解密姓名，暂时使用原值
					customerInfoMap.put("customerNm", bankCard.getCardholderName());
				}
				
				// 如果有手机号、CVN2、有效期，构建加密信息域
				if (!StringUtils.isEmpty(bankCard.getMobile())) {
					Map<String, String> encryptInfoMap = new HashMap<>();
					encryptInfoMap.put("phoneNo", bankCard.getMobile());
					
					// 如果是6要素验证，添加CVN2和有效期
					if ("6".equals(bankCard.getVerifyType()) && 
						!StringUtils.isEmpty(bankCard.getCvn2()) && 
						!StringUtils.isEmpty(bankCard.getExpired())) {
						encryptInfoMap.put("cvn2", bankCard.getCvn2());
						encryptInfoMap.put("expired", bankCard.getExpired());
					}
					
					// 构建加密信息域字符串
					StringBuilder encryptInfo = new StringBuilder();
					for (Map.Entry<String, String> entry : encryptInfoMap.entrySet()) {
						if (encryptInfo.length() > 0) {
							encryptInfo.append("&");
						}
						encryptInfo.append(entry.getKey()).append("=").append(entry.getValue());
					}
					
					// 加密并添加到customerInfo
					if (encryptInfo.length() > 0) {
						String encryptedInfo = AcpService.encryptData(encryptInfo.toString(), DemoBase.encoding);
						customerInfoMap.put("encryptedInfo", encryptedInfo);
					}
				}
			} else {
				// 使用测试数据
				customerInfoMap.put("certifTp", "01");
				customerInfoMap.put("certifId", "341126197709218366"); // 测试身份证号
				customerInfoMap.put("customerNm", "全渠道"); // 测试姓名
			}
			
			// 使用银联SDK构建customerInfo字符串
			String testCardNo = (bankCard != null) ? bankCard.getCardNo() : "6216261000000000018";
			return AcpService.getCustomerInfoWithEncrypt(customerInfoMap, testCardNo, DemoBase.encoding);
			
		} catch (Exception e) {
			logger.error("构建customerInfo失败", e);
			// 返回基础的customerInfo
			Map<String, String> basicCustomerInfoMap = new HashMap<>();
			basicCustomerInfoMap.put("smsCode", StringUtils.isEmpty(smsCode) ? "111111" : smsCode);
			try {
				String testCardNo = (bankCard != null) ? bankCard.getCardNo() : "6216261000000000018";
				return AcpService.getCustomerInfo(basicCustomerInfoMap, testCardNo, DemoBase.encoding);
			} catch (Exception ex) {
				logger.error("构建基础customerInfo也失败", ex);
				return "smsCode=" + (StringUtils.isEmpty(smsCode) ? "111111" : smsCode);
			}
		}
	}
}
