/*
 * Copyright (c) 2019. ananops.com All Rights Reserved.
 * 项目名称：ananops平台
 * 类名称：UacRole.java
 * 创建人：ananops
 * 平台官网: http://ananops.com
 */

package com.ananops.provider.model.domain;

import com.ananops.core.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * The class Uac role.
 *
 * @author ananops.com@gmail.com
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "an_uac_role")
@Alias(value = "uacRole")
public class UacRole extends BaseEntity implements Serializable {
	private static final long serialVersionUID = -6049575043793281879L;

	/**
	 * 角色编码
	 */
	@Column(name = "role_code")
	@Pattern(regexp = "^[A-Za-z0-9]+$", message = "{role.roleCode.pattern}")
	@Length(min = 6, max = 20, message = "{role.roleCode.length}")
	private String roleCode;

	/**
	 * 角色名称
	 */
	@Column(name = "role_name")
	@Pattern(regexp = "^[\\u4e00-\\u9faf]+$", message = "{role.roleName.pattern}")
	@Length(min = 4, max = 10, message = "{role.roleName.length}")
	private String roleName;

	/**
	 * 状态
	 */
	private String status;

	/**
	 * 备注
	 */
	@Length(max = 150, message = "{role.remark.length}")
	private String remark;
}