/*
 * Copyright (c) 2019. ananops.com All Rights Reserved.
 * 项目名称：ananops平台
 * 类名称：MdcDictCheckNameDto.java
 * 创建人：ananops
 * 平台官网: http://ananops.com
 */

package com.ananops.provider.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;


/**
 * The class Uac menu check code dto.
 *
 * @author ananops.com@gmail.com
 */
@Data
@ApiModel
public class MdcDictCheckNameDto implements Serializable {
	private static final long serialVersionUID = 8687848883145768024L;
	/**
	 * 菜单的id
	 */
	@ApiModelProperty(value = "数据字典id")
	private Long dictId;
	/**
	 * 菜单的url
	 */
	@ApiModelProperty(value = "数据字典名称")
	@NotBlank(message = "数据字典名称不能为空")
	private String dictName;


}
