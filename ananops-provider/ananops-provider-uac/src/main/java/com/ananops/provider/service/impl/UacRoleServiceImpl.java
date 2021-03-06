package com.ananops.provider.service.impl;

import com.ananops.provider.mapper.UacGroupMapper;
import com.ananops.provider.mapper.UacMenuMapper;
import com.ananops.provider.model.constant.RoleConstant;
import com.ananops.provider.model.enums.UacMenuStatusEnum;
import com.ananops.provider.model.service.UacUserFeignApi;
import com.ananops.provider.model.vo.GroupZtreeVo;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ananops.Collections3;
import com.ananops.PublicUtil;
import com.ananops.base.constant.GlobalConstant;
import com.ananops.base.dto.LoginAuthDto;
import com.ananops.base.enums.ErrorCodeEnum;
import com.ananops.core.support.BaseService;
import com.ananops.provider.mapper.UacRoleMapper;
import com.ananops.provider.mapper.UacRoleMenuMapper;
import com.ananops.provider.model.domain.*;
import com.ananops.provider.model.dto.role.*;
import com.ananops.provider.model.exceptions.UacBizException;
import com.ananops.provider.model.vo.BindAuthVo;
import com.ananops.provider.model.vo.MenuVo;
import com.ananops.provider.model.vo.RoleVo;
import com.ananops.provider.model.vo.role.MenuCountVo;
import com.ananops.provider.service.*;
import com.ananops.provider.utils.TreeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;


/**
 * The class Uac role service.
 *
 * @author ananops.com@gmail.com
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UacRoleServiceImpl extends BaseService<UacRole> implements UacRoleService {

    @Resource
    private UacMenuMapper uacMenuMapper;

    @Resource
    private UacRoleMapper uacRoleMapper;

    @Resource
    private UacGroupMapper uacGroupMapper;

    @Resource
    private UacRoleUserService uacRoleUserService;

    @Resource
    private UacRoleMenuMapper uacRoleMenuMapper;

    @Resource
    private UacUserService uacUserService;

    @Resource
    private UacRoleMenuService uacRoleMenuService;

    @Resource
    private UacMenuService uacMenuService;

    @Resource
    private UacActionService uacActionService;

    @Resource
    private UacRoleActionService uacRoleActionService;

    @Resource
    private UacGroupService uacGroupService;

    @Resource
    private UacRoleGroupService uacRoleGroupService;

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public UacRole findByRoleCode(String roleCode) {
        return uacRoleMapper.findByRoleCode(roleCode);
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<RoleVo> queryRoleListWithPage(UacRole role, LoginAuthDto loginAuthDto) {
        // 登录用户的组织Id
        Long groupId = loginAuthDto.getGroupId();
        // 获取公司组织Id
        Long rootGroupId = 1L;
        // 获取登录用户的组织树
        List<GroupZtreeVo> groupTree = uacGroupService.getGroupTree(groupId);
        if (groupTree != null) {
            if (groupId != 1L) {
                logger.info("groupTree.get(0) = {}", groupTree.get(0));
                rootGroupId = groupTree.get(0).getId();
            }
        }
        // 获取该Group下的所有角色；
        List<Long> roleIds = uacRoleGroupService.listByGroupId(rootGroupId);
        QueryGroupRoleDto queryGroupRoleDto = new QueryGroupRoleDto();
        queryGroupRoleDto.setRoleIds(roleIds);
        queryGroupRoleDto.setRoleCode(role.getRoleCode());
        queryGroupRoleDto.setRoleName(role.getRoleName());
        queryGroupRoleDto.setStatus(role.getStatus());
        if (roleIds != null && roleIds.size() > 0) {
//			return uacRoleMapper.queryRoleListWithBatchRoleId(roleIds);
            //增加筛选条件
            return uacRoleMapper.queryRoleListWithQueryGroupRoleDto(queryGroupRoleDto);
        }
        return new ArrayList<>();
    }

    @Override
    public int deleteRoleById(Long roleId) {
        //查询该角色下是否有用户绑定, 有的话提醒不能删除
        if (null == roleId) {
            throw new IllegalArgumentException(ErrorCodeEnum.UAC10012001.msg());
        }

        // 超级管理员不能删除
        if (Objects.equals(roleId, GlobalConstant.Sys.SUPER_MANAGER_ROLE_ID)) {
            throw new UacBizException(ErrorCodeEnum.UAC10012003);
        }

        List<UacRoleUser> uruList = uacRoleUserService.listByRoleId(roleId);

        if (!uruList.isEmpty()) {
            uacRoleUserService.deleteByRoleId(roleId);
        }

        uacRoleActionService.deleteByRoleId(roleId);
        uacRoleGroupService.deleteByRoleId(roleId);
        uacRoleMenuService.deleteByRoleId(roleId);
        return uacRoleMapper.deleteByPrimaryKey(roleId);
    }

    @Override
    public int saveRole(UacRole role, LoginAuthDto loginAuthDto) {
        int result = 0;
        role.setUpdateInfo(loginAuthDto);
        if (role.isNew()) {
            Long roleId = super.generateId();
//            List<UacRoleUser> roles = uacRoleUserService.queryByUserId(loginAuthDto.getUserId());
//            if (roles != null) {
//                Iterator<UacRoleUser> iterator = roles.iterator();
//                if (iterator.hasNext()) {
//                    UacRoleUser uacRoleUser = iterator.next();
//                    UacRole uacRoleQuery = new UacRole();
//                    uacRoleQuery.setId(uacRoleUser.getRoleId());
//                    UacRole uacRole = uacRoleMapper.selectOne(uacRoleQuery);
//                    role.setVersion(uacRole.getVersion() + 1);
//                }
//            }
            role.setVersion(0);
            role.setId(roleId);
            uacRoleMapper.insertSelective(role);
            // 插入组织和角色的关系
            UacRoleGroup uacRoleGroup = new UacRoleGroup();
            uacRoleGroup.setRoleId(roleId);
            // 这里默认登录账号的组织就是公司组织，意味着公司管理员账号都只能挂在公司组织下。
            uacRoleGroup.setGroupId(loginAuthDto.getGroupId());
            uacRoleGroupService.save(uacRoleGroup);
        } else {
            result = uacRoleMapper.updateByPrimaryKeySelective(role);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<Long> getAuthTreeNoCheckMenu(Long roleId) {
        //查询某个角色下一级菜单下的二级菜单个数, 去掉二级菜单个数为0的一级菜单选中状态
        List<MenuCountVo> menuCountVos = uacRoleMenuMapper.countChildMenuNum(roleId);
        List<Long> noCheckedMenu = Lists.newArrayList();
        for (MenuCountVo vo : menuCountVos) {
            noCheckedMenu.add(vo.getId());
        }

        return noCheckedMenu;
    }

    @Override
    public void bindAction(RoleBindActionDto grantAuthRole) {
        Long roleId = grantAuthRole.getRoleId();
        Set<Long> actionIdList = grantAuthRole.getActionIdList();

        if (roleId == null) {
            throw new UacBizException(ErrorCodeEnum.UAC10012001);
        }

        if (Objects.equals(roleId, GlobalConstant.Sys.SUPER_MANAGER_ROLE_ID)) {
            logger.error("越权操作, 超级管理员用户不允许操作");
            throw new UacBizException(ErrorCodeEnum.UAC10011023);
        }

        UacRole uacRole = uacRoleMapper.selectByPrimaryKey(roleId);

        if (uacRole == null) {
            logger.error("找不到角色信息. roleId={}", roleId);
            throw new UacBizException(ErrorCodeEnum.UAC10012005, roleId);
        }

        // TODO 校验参数的合法性(这里不写了 累得慌 也就是校验菜单和权限是否存在)
        List<UacRoleAction> uacRoleActionList = uacRoleActionService.listByRoleId(roleId);

        if (PublicUtil.isNotEmpty(uacRoleActionList)) {
            uacRoleActionService.deleteByRoleId(roleId);
        }

        if (PublicUtil.isEmpty(actionIdList)) {
            logger.error("传入按钮权限Id为空, 取消所有按钮权限");
        } else {
            // 绑定权限
            uacRoleActionService.insert(roleId, actionIdList);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public UacRole getRoleById(Long roleId) {
        return uacRoleMapper.selectByPrimaryKey(roleId);
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public RoleBindUserDto getRoleBindUserDto(Long roleId, Long currentUserId, Long currentUserGroupId) {
        RoleBindUserDto roleBindUserDto = new RoleBindUserDto();
        Set<BindUserDto> alreadyBindUserIdSet = Sets.newHashSet();
        UacRole uacRole = uacRoleMapper.selectByPrimaryKey(roleId);
        if (PublicUtil.isEmpty(uacRole)) {
            logger.error("找不到roleId={}, 的角色", roleId);
            throw new UacBizException(ErrorCodeEnum.UAC10012005, roleId);
        }

        // 查询所有用户包括已禁用的用户
        Set<BindUserDto> allUserSet = new HashSet<>();
        // 查询该组织下所有用户包括已禁用的用户
        List<GroupZtreeVo> groupZtreeVos = uacGroupService.getGroupTree(currentUserGroupId);
        for (GroupZtreeVo groupZtreeVo : groupZtreeVos) {
            List<BindUserDto> bindUserDtoList = uacGroupMapper.selectAllUserByGroupId(GlobalConstant.Sys.SUPER_MANAGER_ROLE_ID, groupZtreeVo.getId(), currentUserId);
            allUserSet.addAll(bindUserDtoList);
        }

        // 获取该组织下所有用户Id
        List<Long> userIds = new ArrayList<>();
        for (BindUserDto bindUserDto : allUserSet) {
            userIds.add(bindUserDto.getUserId());
            bindUserDto.setKey(bindUserDto.getUserId());
        }
        // 该组织以绑定该角色的用户
        if (!userIds.isEmpty()) {
            List<Long> alreadyUserId = uacRoleUserService.listByRoleIdUserIds(roleId, userIds);
            if (!PublicUtil.isEmpty(alreadyUserId)) {
                List<UacUser> alreadyUserInfo = uacUserService.batchGetUserInfo(alreadyUserId);
                for (UacUser uacUser : alreadyUserInfo) {
                    BindUserDto bindUserDto = new BindUserDto();
                    bindUserDto.setUserId(uacUser.getId());
                    bindUserDto.setKey(uacUser.getId());
                    if (uacUser.getUserName() != null)
                        bindUserDto.setUserName(uacUser.getUserName());
                    bindUserDto.setRoleCode(uacRole.getRoleCode());
                    bindUserDto.setMobileNo(uacUser.getMobileNo());
                    alreadyBindUserIdSet.add(bindUserDto);
                }
            }
        }

        roleBindUserDto.setAllUserSet(allUserSet);
        roleBindUserDto.setAlreadyBindUserIdSet(alreadyBindUserIdSet);

        return roleBindUserDto;
    }

    @Override
    public void bindUser4Role(RoleBindUserReqDto roleBindUserReqDto, LoginAuthDto authResDto) {

        if (roleBindUserReqDto == null) {
            logger.error("参数不能为空");
            throw new IllegalArgumentException("参数不能为空");
        }

        Long roleId = roleBindUserReqDto.getRoleId();
        Long loginUserId = authResDto.getUserId();
        List<Long> userIdList = roleBindUserReqDto.getUserIdList();

        if (null == roleId) {
            throw new IllegalArgumentException(ErrorCodeEnum.UAC10012001.msg());
        }

        UacRole role = this.getRoleById(roleId);

        if (role == null) {
            logger.error("找不到角色信息 roleId={}", roleId);
            throw new UacBizException(ErrorCodeEnum.UAC10012005, roleId);
        }

        if (PublicUtil.isNotEmpty(userIdList) && userIdList.contains(loginUserId)) {
            logger.error("不能操作当前登录用户 userId={}", loginUserId);
            throw new UacBizException(ErrorCodeEnum.UAC10011023);
        }

        // 查询超级管理员用户Id集合
        List<Long> superUserList = uacRoleUserService.listSuperUser(GlobalConstant.Sys.SUPER_MANAGER_ROLE_ID);
        List<Long> unionList = Collections3.intersection(userIdList, superUserList);
        if (PublicUtil.isNotEmpty(userIdList) && PublicUtil.isNotEmpty(unionList)) {
            logger.error("不能操作超级管理员用户 超级用户={}", unionList);
            throw new UacBizException(ErrorCodeEnum.UAC10011023);
        }

        // 1. 先取消对该角色的用户绑定(不包含超级管理员用户)
        List<UacRoleUser> userRoles = uacRoleUserService.listByRoleId(roleId);
        logger.info("角色用户关系表：" + userRoles);
        if (PublicUtil.isNotEmpty(userRoles)) {
            uacRoleUserService.deleteExcludeSuperMng(roleId, GlobalConstant.Sys.SUPER_MANAGER_ROLE_ID);
        }

        if (PublicUtil.isEmpty(userIdList)) {
            // 取消该角色的所有用户的绑定
            logger.info("取消绑定所有非超级管理员用户成功");
            return;
        }

        // 绑定所选用户
        for (Long userId : userIdList) {
            UacUser uacUser = uacUserService.queryByUserId(userId);
            if (PublicUtil.isEmpty(uacUser)) {
                logger.error("找不到绑定的用户 userId={}", userId);
                throw new UacBizException(ErrorCodeEnum.UAC10011024, userId);
            }
            uacRoleUserService.saveRoleUser(userId, roleId);

            // 如果给该角色分配的不是（用户或者服务商）管理员账号，去除掉默认的平台角色
            if (uacUser.getGroupId() != null) {
                uacRoleGroupService.deleteDefaultByGroupId(uacUser.getGroupId());
            }

            // 如果是用户方或者服务方管理员角色，为其添加默认的平台角色支持
            if ("user_manager".equals(role.getRoleCode())) {
                if (uacUser.getGroupId() != null) {
                    uacRoleGroupService.saveRolesGroup(uacUser.getGroupId(), RoleConstant.USER_DEFAULT_ROLE_IDS);
                }
            } else if ("fac_manager".equals(role.getRoleCode())) {
                if (uacUser.getGroupId() != null) {
                    uacRoleGroupService.saveRolesGroup(uacUser.getGroupId(), RoleConstant.FAC_DEFAULT_ROLE_IDS);
                }
            }
        }

    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<UacRole> findAllRoleInfoByUserId(Long userId) {
        return uacRoleMapper.selectAllRoleInfoByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public BindAuthVo getActionTreeByRoleId(Long roleId) {
        BindAuthVo bindAuthVo = new BindAuthVo();
        if (roleId == null) {
            throw new UacBizException(ErrorCodeEnum.UAC10012001);
        }

        UacRole roleById = this.getRoleById(roleId);
        if (roleById == null) {
            logger.error("找不到角色信息 roleId={}", roleId);
            throw new UacBizException(ErrorCodeEnum.UAC10012005, roleId);
        }
        //返回所有菜单，不包括其它角色自定义菜单和禁用菜单
        List<UacMenu> uacMenus = Lists.newArrayList();
        UacMenu uacMenuQuery = new UacMenu();
        uacMenuQuery.setStatus(UacMenuStatusEnum.ENABLE.getType());
        uacMenuQuery.setApplicationId(1L);
        //菜单排序
        uacMenuQuery.setOrderBy(" level asc,number asc");
        uacMenus = uacMenuMapper.selectMenuList(uacMenuQuery);

        if (PublicUtil.isEmpty(uacMenus)) {
            throw new UacBizException(ErrorCodeEnum.UAC10013009);
        }
        // 查询所有的权限信息
		List<UacAction>  uacActions = uacActionService.listActionList(uacMenus);

        // 合并菜单和按钮权限 递归生成树结构

        List<MenuVo> menuVoList = this.getAuthList(uacMenus, uacActions);

        List<MenuVo> tree = TreeUtil.getChildMenuVos(menuVoList, 0L);

        // 获取该角色已经绑定的菜单和按钮权限Id集合
        List<Long> checkedAuthList = uacActionService.getCheckedActionList(roleId);
		List<Long> checkedMenuList = uacActionService.getCheckedMenuList(roleId);

		checkedAuthList.addAll(checkedMenuList);

        bindAuthVo.setAuthTree(tree);
        bindAuthVo.setCheckedAuthList(checkedAuthList);

        return bindAuthVo;
    }

    private List<MenuVo> getAuthList(List<UacMenu> uacMenus, List<UacAction> uacActions) {
        List<MenuVo> menuVoList = Lists.newArrayList();
        MenuVo menuVo;
        for (UacMenu uacMenu : uacMenus) {
            menuVo = new MenuVo();
            BeanUtils.copyProperties(uacMenu, menuVo);
            menuVo.setRemark("菜单");
            menuVo.setKey(menuVo.getId());
            menuVoList.add(menuVo);
        }
        if (PublicUtil.isNotEmpty(uacActions)) {
            for (UacAction uacAction : uacActions) {
                menuVo = new MenuVo();
                menuVo.setId(uacAction.getId());
                menuVo.setMenuName(uacAction.getActionName());
                menuVo.setMenuCode(uacAction.getActionCode());
                menuVo.setPid(uacAction.getMenuId());
                menuVo.setUrl(uacAction.getUrl());
                menuVo.setStatus(uacAction.getStatus());
                menuVo.setRemark("按钮");
                menuVo.setKey(menuVo.getId());
                menuVoList.add(menuVo);
            }
        }
        return menuVoList;
    }

    @Override
    public void batchDeleteByIdList(List<Long> roleIdList) {
        logger.info("批量删除角色. idList={}", roleIdList);
        Preconditions.checkArgument(PublicUtil.isNotEmpty(roleIdList), "删除角色ID不存在");

        List<UacRoleUser> uruList = uacRoleUserService.listByRoleIdList(roleIdList);
        if (!uruList.isEmpty()) {
            uacRoleUserService.deleteByRoleIdList(roleIdList);
        }

        uacRoleMenuService.deleteByRoleIdList(roleIdList);
        uacRoleGroupService.deleteByRoleIdList(roleIdList);
        uacRoleActionService.deleteByRoleIdList(roleIdList);

        int result = uacRoleMapper.batchDeleteByIdList(roleIdList);
        if (result < roleIdList.size()) {
            throw new UacBizException(ErrorCodeEnum.UAC10012006, Joiner.on(GlobalConstant.Symbol.COMMA).join(roleIdList));
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<MenuVo> getOwnAuthTree(Long userId) {
        if (userId == null) {
            throw new UacBizException(ErrorCodeEnum.UAC10011001);
        }

        return uacMenuService.getMenuVoList(userId, GlobalConstant.Sys.OPER_APPLICATION_ID);
    }

    @Override
    public void bindMenu(RoleBindMenuDto roleBindMenuDto) {

        Long roleId = roleBindMenuDto.getRoleId();
        Set<Long> menuIdList = roleBindMenuDto.getMenuIdList();

        if (roleId == null) {
            throw new UacBizException(ErrorCodeEnum.UAC10012001);
        }

        if (Objects.equals(roleId, GlobalConstant.Sys.SUPER_MANAGER_ROLE_ID)) {
            logger.error("越权操作, 超级管理员用户不允许操作");
            throw new UacBizException(ErrorCodeEnum.UAC10011023);
        }

        UacRole uacRole = uacRoleMapper.selectByPrimaryKey(roleId);

        if (uacRole == null) {
            logger.error("找不到角色信息. roleId={}", roleId);
            throw new UacBizException(ErrorCodeEnum.UAC10012005, roleId);
        }

        // TODO 校验参数的合法性(这里不写了 累得慌 也就是校验菜单和权限是否存在)
        List<UacRoleMenu> uacRoleMenuList = uacRoleMenuService.listByRoleId(roleId);

        if (PublicUtil.isNotEmpty(uacRoleMenuList)) {
            uacRoleMenuService.deleteByRoleId(roleId);
        }

        // menuSet actionIdList 如果为空则 取消该角色所有权限
        if (PublicUtil.isEmpty(menuIdList)) {
            logger.error("传入菜单权限Id为空, 取消菜单权限");
        } else {
            // 绑定菜单
            uacRoleMenuService.insert(roleId, menuIdList);

        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public BindAuthVo getMenuTreeByRoleId(Long roleId) {
        BindAuthVo bindAuthVo = new BindAuthVo();
        Preconditions.checkArgument(roleId != null, ErrorCodeEnum.UAC10012001.msg());

        UacRole roleById = this.getRoleById(roleId);
        if (roleById == null) {
            logger.error("找不到角色信息 roleId={}", roleId);
            throw new UacBizException(ErrorCodeEnum.UAC10012005, roleId);
        }

        // 查询所有的菜单信息
        List<UacMenu> uacMenus = uacMenuService.selectAll();
        // 合并菜单和按钮权限 递归生成树结构

        List<MenuVo> menuVoList = this.getAuthList(uacMenus, null);

        List<MenuVo> tree = TreeUtil.getChildMenuVos(menuVoList, 0L);

        // 获取所有绑定的菜单和按钮权限Id集合
        List<Long> checkedAuthList = uacActionService.getCheckedMenuList(roleId);

        bindAuthVo.setAuthTree(tree);
        bindAuthVo.setCheckedAuthList(checkedAuthList);

        return bindAuthVo;
    }

    @Override
    public List<UacRole> queryBindRoleWithPage(Long roleId) {
        UacRole uacRole = uacRoleMapper.selectByPrimaryKey(roleId);
        Example example = new Example(UacRole.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("version", uacRole.getVersion() + 1);
        List<UacRole> uacRoleList = uacRoleMapper.selectByExample(example);
        return uacRoleList;
    }
}
