package com.ananops.provider.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.ananops.PublicUtil;
import com.ananops.base.constant.GlobalConstant;
import com.ananops.base.dto.LoginAuthDto;
import com.ananops.base.enums.ErrorCodeEnum;
import com.ananops.core.support.BaseService;
import com.ananops.provider.mapper.UacActionMapper;
import com.ananops.provider.mapper.UacRoleActionMapper;
import com.ananops.provider.model.domain.UacAction;
import com.ananops.provider.model.domain.UacMenu;
import com.ananops.provider.model.dto.action.ActionMainQueryDto;
import com.ananops.provider.model.exceptions.UacBizException;
import com.ananops.provider.model.vo.ActionVo;
import com.ananops.provider.model.vo.MenuVo;
import com.ananops.provider.service.UacActionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * The class Uac action service.
 *
 * @author ananops.com@gmail.com
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class UacActionServiceImpl extends BaseService<UacAction> implements UacActionService {
	@Resource
	private UacActionMapper uacActionMapper;
	@Resource
	private UacRoleActionMapper uacRoleActionMapper;
	private AntPathMatcher antPathMatcher = new AntPathMatcher();

	@Override
	@Transactional(readOnly = true, rollbackFor = Exception.class)
	public PageInfo queryActionListWithPage(ActionMainQueryDto actionMainQueryDto) {
		List<Long> menuIdList = actionMainQueryDto.getMenuIdList();
		Long menuId = null;
		if (PublicUtil.isNotEmpty(menuIdList)) {
			menuId = menuIdList.get(menuIdList.size() - 1);  //菜单树叶子节点
		}
		logger.info("menuId = "+menuId);
		UacAction uacAction = new UacAction();
		uacAction.setMenuId(menuId);
		BeanUtils.copyProperties(actionMainQueryDto, uacAction);
		uacAction.setOrderBy("update_time desc");
		PageHelper.startPage(actionMainQueryDto.getPageNum(), actionMainQueryDto.getPageSize());
		List<ActionVo> actionList = uacActionMapper.queryActionListWithPage(uacAction);
		return new PageInfo<>(actionList);
	}

	@Override
	public int deleteActionById(Long actionId) {
		//查询该角色下是否有用户绑定, 有的话提醒不能删除
		if (null == actionId) {
			throw new IllegalArgumentException("权限ID不能为空");
		}

		UacAction uacAction = uacActionMapper.selectByPrimaryKey(actionId);
		if (uacAction == null) {
			logger.error("找不到权限信息 actionId={}", actionId);
			throw new UacBizException(ErrorCodeEnum.UAC10014001, actionId);
		}

		// 删除角色权限表数据  不查询了 直接删除了
		uacRoleActionMapper.deleteByActionId(actionId);

		return uacActionMapper.deleteByPrimaryKey(actionId);
	}

	@Override
	public void batchDeleteByIdList(List<Long> deleteIdList) {
		logger.info("批量删除权限. deleteIdList={}", deleteIdList);
		Preconditions.checkArgument(PublicUtil.isNotEmpty(deleteIdList), "删除权限ID不能为空");
		int result = uacActionMapper.batchDeleteByIdList(deleteIdList);
		if (result < deleteIdList.size()) {
			throw new UacBizException(ErrorCodeEnum.UAC10014002, Joiner.on(GlobalConstant.Symbol.COMMA).join(deleteIdList));
		}
	}

	@Override
	public void saveAction(UacAction action, LoginAuthDto loginAuthDto) {
		List<Long> menuIdList = action.getMenuIdList();
		Long menuId;
		Preconditions.checkArgument(PublicUtil.isNotEmpty(menuIdList), "菜单名称不能为空");
		menuId = menuIdList.get(menuIdList.size() - 1);
		action.setMenuId(menuId);
		action.setUpdateInfo(loginAuthDto);
		if (action.isNew()) {
			Long actionId = super.generateId();
			action.setId(actionId);
			uacActionMapper.insertSelective(action);
		} else {
			int result = uacActionMapper.updateByPrimaryKeySelective(action);
			if (result < 1) {
				throw new UacBizException(ErrorCodeEnum.UAC10014003);
			}
		}
	}

	@Override
	public int deleteByMenuId(Long id) {
		Preconditions.checkArgument(id != null, "菜单ID不能为空");

		return uacActionMapper.deleteByMenuId(id);
	}

	@Override
	@Transactional(readOnly = true, rollbackFor = Exception.class)
	public List<Long> getCheckedActionList(Long roleId) {
		if (roleId == null) {
			throw new UacBizException(ErrorCodeEnum.UAC10012001);
		}
		return uacActionMapper.getCheckedActionList(roleId);
	}

	@Override
	@Transactional(readOnly = true, rollbackFor = Exception.class)
	public List<MenuVo> getOwnAuthList(Long userId) {
		return uacActionMapper.getOwnAuthList(userId);
	}

	@Override
	@Transactional(readOnly = true, rollbackFor = Exception.class)
	public List<Long> getCheckedMenuList(Long roleId) {
		if (roleId == null) {
			throw new UacBizException(ErrorCodeEnum.UAC10012001);
		}
		return uacActionMapper.getCheckedMenuList(roleId);
	}

	@Override
	public List<UacAction> getOwnActionListByUserId(Long userId) {
		if (userId == null) {
			throw new UacBizException(ErrorCodeEnum.UAC10011001);
		}
		List<UacAction> uacActionList;
		if (Objects.equals(userId, GlobalConstant.Sys.SUPER_MANAGER_USER_ID)) {
			// 获取全部权限信息
			uacActionList = uacActionMapper.selectAll();
		} else {
			uacActionList = uacActionMapper.getOwnUacActionListByUserId(userId);
		}
		return uacActionList;
	}

	@Override
	public List<UacAction> listActionListByRoleId(Long roleId) {
		return uacActionMapper.listActionListByRoleId(roleId);
	}

	@Override
	public List<UacAction> listActionList(List<UacMenu> uacMenus) {
		return uacActionMapper.listActionList(uacMenus);
	}

	@Override
	public UacAction matchesByUrl(String requestUrl) {
		List<UacAction> uacActionList = uacActionMapper.selectAll();
		for (UacAction uacAction : uacActionList) {
			String url = uacAction.getUrl();
			if (StringUtils.isEmpty(url)) {
				continue;
			}

			if (antPathMatcher.match(url, requestUrl)) {
				return uacAction;
			}

		}
		return null;
	}
}
