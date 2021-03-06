/*
 * Copyright (c) 2019. ananops.com All Rights Reserved.
 * 项目名称：ananops平台
 * 类名称：UacGroupUserMapper.java
 * 创建人：ananops
 * 平台官网: http://ananops.com
 */

package com.ananops.provider.mapper;

import com.ananops.core.mybatis.MyMapper;
import com.ananops.provider.model.domain.UacGroup;
import com.ananops.provider.model.domain.UacGroupUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The interface Uac group user mapper.
 *
 * @author ananops.com@gmail.com
 */
@Mapper
@Component
public interface UacGroupUserMapper extends MyMapper<UacGroupUser> {

	/**
	 * Query by user id uac group user.
	 *
	 * @param userId the user id
	 *
	 * @return the uac group user
	 */
	UacGroupUser getByUserId(Long userId);

	/**
	 * Update by user id int.
	 *
	 * @param uacGroupUser the uac group user
	 *
	 * @return the int
	 */
	int updateByUserId(UacGroupUser uacGroupUser);

	/**
	 * Select group list by user id list.
	 *
	 * @param userId the user id
	 *
	 * @return the list
	 */
	List<UacGroup> selectGroupListByUserId(Long userId);

	/**
	 * List by group id list.
	 *
	 * @param groupId the group id
	 *
	 * @return the list
	 */
	List<UacGroupUser> listByGroupId(@Param("groupId") Long groupId);

	/**
	 * Delete exclude super mng int.
	 *
	 * @param groupId            the group id
	 * @param superManagerRoleId the super manager role id
	 *
	 * @return the int
	 */
	int deleteExcludeSuperMng(@Param("currentGroupId") Long groupId, @Param("superManagerRoleId") Long superManagerRoleId);
}