/*
 * Copyright (c) 2019. ananops.com All Rights Reserved.
 * 项目名称：ananops平台
 * 类名称：UacUserMapper.java
 * 创建人：ananops
 * 平台官网: http://ananops.com
 */

package com.ananops.provider.mapper;

import com.ananops.core.mybatis.MyMapper;
import com.ananops.provider.model.domain.UacUser;
import com.ananops.provider.model.dto.user.BindRoleDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * The interface Uac user mapper.
 *
 * @author ananops.com @gmail.com
 */
@Mapper
@Component
public interface UacUserMapper extends MyMapper<UacUser> {

	/**
	 * Find by login name uac user.
	 *
	 * @param loginName the login name
	 *
	 * @return the uac user
	 */
	UacUser findByLoginName(String loginName);

	/**
	 * Find by mobile no uac user.
	 *
	 * @param mobileNo the mobile no
	 *
	 * @return the uac user
	 */
	UacUser findByMobileNo(@Param("mobileNo") String mobileNo);

	/**
	 * Find by login name and login pwd uac user.
	 *
	 * @param loginNamePwdMap the login name pwd map
	 *
	 * @return the uac user
	 */
	UacUser findByLoginNameAndLoginPwd(Map<String, String> loginNamePwdMap);

	/**
	 * Select user list list.
	 *
	 * @param uacUser the uac user
	 *
	 * @return the list
	 */
	List<UacUser> selectUserList(UacUser uacUser);

	/**
	 * Select user info by user id uac user.
	 *
	 * @param userId the user id
	 *
	 * @return the uac user
	 */
	UacUser selectUserInfoByUserId(Long userId);

	/**
	 * Update uac user int.
	 *
	 * @param user the user
	 *
	 * @return the int
	 */
	int updateUacUser(UacUser user);

	/**
	 * Select all need bind role list.
	 *
	 * @param superManagerRoleId the super manager role id
	 *
	 * @return the list
	 */
	List<BindRoleDto> selectAllNeedBindRole(@Param("superManagerRoleId") Long superManagerRoleId);

	/**
	 * Find user info by login name uac user.
	 *
	 * @param loginName the login name
	 *
	 * @return the uac user
	 */
	UacUser findUserInfoByLoginName(@Param("loginName") String loginName);

	/**
	 * 查询角色的下级角色
	 * @param version
	 * @return
	 */
	List<BindRoleDto> selectAllPermitBindRole(Integer version);
}