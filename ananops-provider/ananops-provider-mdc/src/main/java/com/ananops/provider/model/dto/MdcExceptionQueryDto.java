/*
 * Copyright (c) 2018. ananops.net All Rights Reserved.
 * 项目名称：ananops快速搭建企业级分布式微服务平台
 * 类名称：MdcExceptionQueryDto.java
 * 创建人：刘兆明
 * 联系方式：ananops.net@gmail.com
 * 开源地址: https://github.com/ananops
 * 博客地址: http://blog.ananops.net
 * 项目官网: http://ananops.net
 */

package com.ananops.provider.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ananops.base.dto.BaseQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


/**
 * The class Mdc exception query dto.
 *
 * @author ananops.net @gmail.com
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel
public class MdcExceptionQueryDto extends BaseQuery {

	private static final long serialVersionUID = 3967075132487249652L;
	/**
	 * 操作用户名称
	 */
	@ApiModelProperty(value = "操作用户名称")
	private String creator;
	/**
	 * 异常原因
	 */
	@ApiModelProperty(value = "异常原因")
	private String exceptionCause;

	/**
	 * 异常栈信息
	 */
	@ApiModelProperty(value = "异常栈信息")
	private String exceptionStack;

	/**
	 * 开始时间
	 */
	@ApiModelProperty(value = "开始时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date startQueryTime;

	/**
	 * 结束时间
	 */
	@ApiModelProperty(value = "结束时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date endQueryTime;
}
