/*
 * Copyright (c) 2019. ananops.com All Rights Reserved.
 * 项目名称：ananops平台
 * 类名称：OptAttachmentDeleteReqDto.java
 * 创建人：ananops
 * 平台官网: http://ananops.com
 */

package com.ananops.provider.model.dto.attachment;

import lombok.Data;

import java.io.Serializable;

/**
 * The class Opt attachment delete req dto.
 *
 * @author ananops.com@gmail.com
 */
@Data
public class OptAttachmentDeleteReqDto implements Serializable {


	private static final long serialVersionUID = 731160897713227519L;
	private String[] serialNos;
}
