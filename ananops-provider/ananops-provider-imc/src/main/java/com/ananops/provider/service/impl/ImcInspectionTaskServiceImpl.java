package com.ananops.provider.service.impl;


import com.ananops.PublicUtil;
import com.ananops.base.dto.LoginAuthDto;
import com.ananops.base.enums.ErrorCodeEnum;
import com.ananops.base.exception.BusinessException;
import com.ananops.core.support.BaseService;
import com.ananops.provider.manager.ImcTaskManager;
import com.ananops.provider.mapper.*;
import com.ananops.provider.model.domain.*;
import com.ananops.provider.model.dto.*;
import com.ananops.provider.model.dto.group.CompanyDto;
import com.ananops.provider.model.enums.ItemStatusEnum;
import com.ananops.provider.model.enums.TaskStatusEnum;
import com.ananops.provider.model.exceptions.UacBizException;
import com.ananops.provider.model.service.UacGroupFeignApi;
import com.ananops.provider.mq.producer.TaskMsgProducer;
import com.ananops.provider.service.ImcInspectionItemService;
import com.ananops.provider.service.ImcInspectionTaskService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by rongshuai on 2019/11/27 19:31
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ImcInspectionTaskServiceImpl extends BaseService<ImcInspectionTask> implements ImcInspectionTaskService {
    @Resource
    ImcInspectionTaskMapper imcInspectionTaskMapper;

    @Resource
    ImcTaskManager imcTaskManager;

    @Resource
    ImcInspectionItemService imcInspectionItemService;

    @Resource
    ImcUserTaskMapper imcUserTaskMapper;

    @Resource
    ImcInspectionItemMapper imcInspectionItemMapper;

    @Resource
    ImcFacilitatorManagerTaskMapper imcFacilitatorManagerTaskMapper;

    @Resource
    ImcFacilitatorGroupTaskMapper imcFacilitatorGroupTaskMapper;

    @Resource
    TaskMsgProducer taskMsgProducer;

    @Resource
    private UacGroupFeignApi uacGroupFeignApi;

    /**
     * 插入一条巡检任务记录
     * @param imcAddInspectionTaskDto
     * @return
     */
    @Override
    public ImcAddInspectionTaskDto saveTask(ImcAddInspectionTaskDto imcAddInspectionTaskDto, LoginAuthDto loginAuthDto){
        ImcInspectionTask imcInspectionTask = new ImcInspectionTask();
        BeanUtils.copyProperties(imcAddInspectionTaskDto,imcInspectionTask);
        imcInspectionTask.setUpdateInfo(loginAuthDto);
        if(imcInspectionTask.isNew()){
            //如果当前是新建一条任务
            //获取所有的巡检任务子项
            List<ImcAddInspectionItemDto> imcAddInspectionItemDtoList = imcAddInspectionTaskDto.getImcAddInspectionItemDtoList();
            Integer times = imcAddInspectionTaskDto.getTimes();
            Integer days = imcAddInspectionTaskDto.getDays();
            Date startTime = imcAddInspectionTaskDto.getScheduledStartTime();
            Calendar calendar = new GregorianCalendar();
            Integer inspectionType = imcAddInspectionTaskDto.getInspectionType();
            int count=1;
            if(times!=null) count=times;
            for(int i=0;i<count;i++){
                Long taskId = super.generateId();
                Long userId = imcAddInspectionTaskDto.getUserId();
                Long facilitatorManagerId = imcAddInspectionTaskDto.getFacilitatorManagerId();
                Long facilitatorGroupId = imcAddInspectionTaskDto.getFacilitatorGroupId();
                imcInspectionTask.setId(taskId);
                imcInspectionTask.setScheduledStartTime(startTime);
                if(inspectionType==1){
                    //如果是从巡检方案中发起的，则无需甲方负责人审核
                    imcInspectionTask.setStatus(TaskStatusEnum.WAITING_FOR_ACCEPT.getStatusNum());
                }else{
                    //将巡检任务的状态设置为等待甲方负责人审核
                    imcInspectionTask.setStatus(TaskStatusEnum.WAITING_FOR_PRINCIPAL.getStatusNum());
                }
                imcInspectionTaskMapper.insert(imcInspectionTask);
                logger.info("新创建一条巡检记录：" + imcInspectionTask.toString());
                //更新startTime
                calendar.setTime(startTime);
                calendar.add(calendar.DATE,days);//当前时间向后增加days天
                startTime=calendar.getTime();

                //增加一条甲方用户和巡检任务的关系记录
                ImcUserTask imcUserTask = new ImcUserTask();
                imcUserTask.setTaskId(taskId);
                imcUserTask.setUserId(userId);
                imcUserTaskMapper.insert(imcUserTask);

                //增加一条服务商管理员和巡检任务的关系记录
                ImcFacilitatorManagerTask imcFacilitatorManagerTask = new ImcFacilitatorManagerTask();
                imcFacilitatorManagerTask.setFacilitatorManagerId(facilitatorManagerId);
                imcFacilitatorManagerTask.setTaskId(taskId);
                imcFacilitatorManagerTaskMapper.insert(imcFacilitatorManagerTask);

                //增加一条服务商组织和巡检任务的关系记录
                ImcFacilitatorGroupTask imcFacilitatorGroupTask = new ImcFacilitatorGroupTask();
                imcFacilitatorGroupTask.setFacilitatorGroupId(facilitatorGroupId);
                imcFacilitatorGroupTask.setTaskId(taskId);
                imcFacilitatorGroupTaskMapper.insert(imcFacilitatorGroupTask);
                if(imcAddInspectionItemDtoList!=null){
                    //保存新创建的巡检任务子项
                    imcAddInspectionItemDtoList.forEach(item->{
                        //保存所有巡检任务子项
                        item.setInspectionTaskId(taskId);//设置巡检任务子项对应的任务id
                        item.setDays(imcInspectionTask.getDays());//设置巡检任务子项对应的巡检周期
                        item.setFrequency(imcInspectionTask.getFrequency());//设置巡检任务子项对应的巡检频率
                        item.setScheduledStartTime(imcInspectionTask.getScheduledStartTime());//设置巡检任务子项的对应的计划开始时间
                        item.setUserId(userId);
                        item.setStatus(ItemStatusEnum.WAITING_FOR_MAINTAINER.getStatusNum());
                        Long scheduledStartTime = item.getScheduledStartTime().getTime();//获得巡检任务子项的预计开始时间
                        Long currentTime = System.currentTimeMillis();//获得当前时间
                        if(scheduledStartTime<=currentTime){//如果计划执行时间<=当前时间，说明，巡检任务需要立即执行
                            //将巡检任务子项的状态设置为等待分配工程师
                            item.setStatus(ItemStatusEnum.WAITING_FOR_MAINTAINER.getStatusNum());
                        }
                        //创建新的任务子项，并更新返回结果
                        BeanUtils.copyProperties(imcInspectionItemService.saveInspectionItem(item,loginAuthDto),item);
                    });
                    BeanUtils.copyProperties(imcAddInspectionItemDtoList,imcAddInspectionTaskDto);
                }
                //更新返回结果
                BeanUtils.copyProperties(imcInspectionTask,imcAddInspectionTaskDto);
                //推送消息
                MqMessageData mqMessageData = taskMsgProducer.sendTaskStatusMsgMq(imcInspectionTask);
                imcTaskManager.modifyTaskStatus(mqMessageData);
            }
        }else{
            //如果当前是更新一条记录
            imcInspectionTaskMapper.updateByPrimaryKeySelective(imcInspectionTask);
            //更新返回结果
            BeanUtils.copyProperties(imcInspectionTask,imcAddInspectionTaskDto);
        }
        System.out.println(imcInspectionTask.getTaskName());
        return imcAddInspectionTaskDto;
    }

    /**
     * 根据巡检任务的Id获取巡检任务
     * @param taskId
     * @return
     */
    @Override
    public ImcInspectionTask getTaskByTaskId(Long taskId){//根据巡检任务的ID，获取巡检任务的详情
        return imcInspectionTaskMapper.selectByPrimaryKey(taskId);
    }


    /**
     * 修改巡检任务的状态
     * @param imcTaskChangeStatusDto
     * @param loginAuthDto
     * @return
     */
    @Override
    public ImcInspectionTask modifyTaskStatus(ImcTaskChangeStatusDto imcTaskChangeStatusDto, LoginAuthDto loginAuthDto){
        Long taskId = imcTaskChangeStatusDto.getTaskId();
        Integer status = imcTaskChangeStatusDto.getStatus();
        ImcInspectionTask imcInspectionTask = new ImcInspectionTask();
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",taskId);
        if(imcInspectionTaskMapper.selectCountByExample(example)==0){
            //如果当前任务不存在
            throw new BusinessException(ErrorCodeEnum.GL9999098,taskId);
        }
        //如果当前任务存在
        imcInspectionTask = imcInspectionTaskMapper.selectByPrimaryKey(taskId);
        if(status.equals(TaskStatusEnum.WAITING_FOR_PAY.getStatusNum())){
            //如果当前任务状态修改为等待支付，意味着任务已经被确认
            Example example2 = new Example(ImcInspectionItem.class);
            Example.Criteria criteria2 = example2.createCriteria();
            criteria2.andEqualTo("inspectionTaskId",taskId);
            List<ImcInspectionItem> imcInspectionItemList = imcInspectionItemMapper.selectByExample(example2);
            imcInspectionItemList.forEach(imcInspectionItem -> {
                //任务已经巡检完毕，将全部任务子项的状态修改为已完成
                imcInspectionItem.setStatus(ItemStatusEnum.VERIFIED.getStatusNum());
                imcInspectionItemService.update(imcInspectionItem);
            });
        }
        else if(status.equals(TaskStatusEnum.WAITING_FOR_CONFIRM.getStatusNum())){
            //如果当前状态处于巡检完成等待甲方负责人确认的阶段
            //更新巡检完成的实际时间
            imcInspectionTask.setActualFinishTime(new Date(System.currentTimeMillis()));
        }
        imcInspectionTask.setStatus(status);
        imcInspectionTask.setUpdateInfo(loginAuthDto);
        int result = imcInspectionTaskMapper.updateByPrimaryKeySelective(imcInspectionTask);
        if(result < 1){
            throw new BusinessException(ErrorCodeEnum.GL9999092);
        }
        MqMessageData mqMessageData = taskMsgProducer.sendTaskStatusMsgMq(imcInspectionTask);
        imcTaskManager.modifyTaskStatus(mqMessageData);
        return imcInspectionTask;
    }

    /**
     * 删除指定的巡检任务以及对应的子项
     * @param taskId
     */
    @Override
    public void deleteTaskById(Long taskId){
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",taskId);
        ItemQueryDto itemQueryDto = new ItemQueryDto();
        itemQueryDto.setTaskId(taskId);
        List<ImcInspectionItem> imcInspectionItems = imcInspectionItemService.getAllItemByTaskId(itemQueryDto);
        for(int i=0;i<imcInspectionItems.size();i++){
            Long itemId = imcInspectionItems.get(i).getId();
            imcInspectionItemService.deleteItemByItemId(itemId);
        }
        imcInspectionTaskMapper.deleteByExample(example);
    }

    /**
     * 获得某一状态下的全部巡检任务（可分页）
     * @param taskQueryDto
     * @return
     */
    @Override
    public PageInfo getTaskByStatusAndPage(TaskQueryDto taskQueryDto){
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        Integer status = taskQueryDto.getStatus();
        String taskName = taskQueryDto.getTaskName();
        if(StringUtils.isNotBlank(taskName)){
            taskName = "%" + taskName + "%";
            criteria.andLike("taskName",taskName);
        }
        criteria.andEqualTo("status",status);
        if(imcInspectionTaskMapper.selectCountByExample(example)==0){
            //当前状态没有对应的任务
            throw new BusinessException(ErrorCodeEnum.GL9999091);
        }
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        List<ImcInspectionTask> imcInspectionTaskList = imcInspectionTaskMapper.selectByExample(example);
        return new PageInfo<>(imcInspectionTaskList);
    }

    /**
     * 获得某一状态下的全部巡检任务
     * @param taskQueryDto
     * @return
     */
    @Override
    public List<ImcInspectionTask> getTaskByStatus(TaskQueryDto taskQueryDto){
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        Integer status = taskQueryDto.getStatus();
        String taskName = taskQueryDto.getTaskName();
        if(StringUtils.isNotBlank(taskName)){
            taskName = "%" + taskName + "%";
            criteria.andLike("taskName",taskName);
        }
        criteria.andEqualTo("status",status);
        if(imcInspectionTaskMapper.selectCountByExample(example)==0){
            //当前状态没有对应的任务
            throw new BusinessException(ErrorCodeEnum.GL9999091);
        }
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        return imcInspectionTaskMapper.selectByExample(example);
    }

    /**
     * 修改巡检任务的名字
     * @param taskNameChangeDto
     * @param loginAuthDto
     * @return
     */
    @Override
    public ImcInspectionTask modifyTaskName(TaskNameChangeDto taskNameChangeDto, LoginAuthDto loginAuthDto){
        ImcInspectionTask imcInspectionTask = new ImcInspectionTask();
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",taskNameChangeDto.getTaskId());
        if(imcInspectionTaskMapper.selectCountByExample(example)==0){
            throw new BusinessException(ErrorCodeEnum.GL9999098);
        }
        imcInspectionTask = imcInspectionTaskMapper.selectByExample(example).get(0);
        imcInspectionTask.setTaskName(taskNameChangeDto.getTaskName());
        imcInspectionTask.setUpdateInfo(loginAuthDto);
        imcInspectionTaskMapper.updateByPrimaryKeySelective(imcInspectionTask);
        return imcInspectionTask;
    }

    /**
     * 根据项目id查询对应的巡检任务（可分页）
     * @param taskQueryDto
     * @return
     */
    @Override
    public PageInfo getTaskByProjectIdAndPage(TaskQueryDto taskQueryDto){
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        Long projectId = taskQueryDto.getProjectId();
        String taskName = taskQueryDto.getTaskName();
        if(StringUtils.isNotBlank(taskName)){
            taskName = "%" + taskName + "%";
            criteria.andLike("taskName",taskName);
        }
        criteria.andEqualTo("projectId",projectId);
        if(imcInspectionTaskMapper.selectCountByExample(example)==0){
            //当前状态没有对应的任务
            throw new BusinessException(ErrorCodeEnum.GL9999091);
        }
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        List<ImcInspectionTask> imcInspectionTaskList = imcInspectionTaskMapper.selectByExample(example);
        return new PageInfo<>(imcInspectionTaskList);
    }
    /**
     * 根据项目id查询对应的巡检任务
     * @param taskQueryDto
     * @return
     */
    @Override
    public List<ImcInspectionTask> getTaskByProjectId(TaskQueryDto taskQueryDto){
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        Long projectId = taskQueryDto.getProjectId();
        String taskName = taskQueryDto.getTaskName();
        if(StringUtils.isNotBlank(taskName)){
            taskName = "%" + taskName + "%";
            criteria.andLike("taskName",taskName);
        }
        criteria.andEqualTo("projectId",projectId);
        if(imcInspectionTaskMapper.selectCountByExample(example)==0){
            //当前状态没有对应的任务
            throw new BusinessException(ErrorCodeEnum.GL9999091);
        }
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        return imcInspectionTaskMapper.selectByExample(example);
    }

    /**
     * 根据用户id和用户角色获取全部的巡检任务数目
     * @param taskQueryDto
     * @return
     */
    @Override
    public Integer getImcTaskNumberByUserIdAndRole(TaskQueryDto taskQueryDto){
        Integer role = taskQueryDto.getRole();
        Long userId = taskQueryDto.getUserId();
        if(role==null||userId==null){
            throw new BusinessException(ErrorCodeEnum.IMC10090008);
        }else{
            switch (role){
                case 1://如果角色是甲方负责人
                    return imcInspectionTaskMapper.countTaskByUserId(userId);
                case 2://如果角色是服务商
                    return this.countTaskByFacilitatorId(userId);
                default:
                    throw new BusinessException(ErrorCodeEnum.GL9999089);
            }
        }
    }


    /**
     * 根据用户id获取对应的巡检任务（可分页）
     * @param taskQueryDto
     * @return
     */
    @Override
    public PageInfo getTaskByUserIdAndPage(TaskQueryDto taskQueryDto){
        Integer role = taskQueryDto.getRole();
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        String taskName = taskQueryDto.getTaskName();
        List<ImcInspectionTask> imcInspectionTaskList;
        if(StringUtils.isNotBlank(taskName)){
            taskName = "%" + taskName + "%";
            if(role == null){
                throw new BusinessException(ErrorCodeEnum.GL9999089);
            }
            switch (role){
                case 1://如果角色是甲方用户
                    imcInspectionTaskList = imcInspectionTaskMapper.queryTaskByUserIdAndTaskName(taskQueryDto.getUserId(),taskName);
                    return new PageInfo<>(imcInspectionTaskList);
                case 2://如果角色是服务商
                    imcInspectionTaskList = this.getTaskByFacilitatorId(taskQueryDto);
                    return new PageInfo<>(imcInspectionTaskList);
                case 3://如果角色是服务商管理员
                    imcInspectionTaskList = imcInspectionTaskMapper.queryTaskByFacilitatorManagerIdAndTaskName(taskQueryDto.getUserId(),taskName);
                    return new PageInfo<>(imcInspectionTaskList);
                case 4://如果角色是服务商组织
                    imcInspectionTaskList = imcInspectionTaskMapper.queryTaskByFacilitatorGroupIdAndTaskName(getCompanyGroupIdFromUserGroupId(taskQueryDto.getUserId()),taskName);
                    return new PageInfo<>(imcInspectionTaskList);
                default:
                    throw new BusinessException(ErrorCodeEnum.GL9999089);
            }
        }else{
            if(role == null){
                throw new BusinessException(ErrorCodeEnum.GL9999089);
            }
            switch (role){
                case 1://如果角色是甲方用户
                    imcInspectionTaskList = imcInspectionTaskMapper.queryTaskByUserId(taskQueryDto.getUserId());
                    return new PageInfo<>(imcInspectionTaskList);
                case 2://如果角色是服务商
                    imcInspectionTaskList = this.getTaskByFacilitatorId(taskQueryDto);
                    return new PageInfo<>(imcInspectionTaskList);
                case 3://如果角色是服务商管理员
                    imcInspectionTaskList = imcInspectionTaskMapper.queryTaskByFacilitatorManagerId(taskQueryDto.getUserId());
                    return new PageInfo<>(imcInspectionTaskList);
                case 4://如果角色是服务商组织
                    imcInspectionTaskList = imcInspectionTaskMapper.queryTaskByFacilitatorGroupId(getCompanyGroupIdFromUserGroupId(taskQueryDto.getUserId()));
                    return new PageInfo<>(imcInspectionTaskList);
                default:
                    throw new BusinessException(ErrorCodeEnum.GL9999089);
            }
        }
    }
    /**
     * 根据用户id获取对应的巡检任务
     * @param taskQueryDto
     * @return
     */
    @Override
    public List<ImcInspectionTask> getTaskByUserId(TaskQueryDto taskQueryDto){
        Integer role = taskQueryDto.getRole();
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        String taskName = taskQueryDto.getTaskName();
        if(StringUtils.isNotBlank(taskName)){
            taskName = "%" + taskName + "%";
            if(role == null){
                throw new BusinessException(ErrorCodeEnum.GL9999089);
            }
            switch (role){
                case 1://如果角色是甲方用户
                    return imcInspectionTaskMapper.queryTaskByUserIdAndTaskName(taskQueryDto.getUserId(),taskName);
                case 2://如果角色是服务商
                    return this.getTaskByFacilitatorId(taskQueryDto);
                case 3://如果角色是服务商管理员
                    return imcInspectionTaskMapper.queryTaskByFacilitatorManagerIdAndTaskName(taskQueryDto.getUserId(),taskName);
                case 4://如果角色是服务商组织
                    return imcInspectionTaskMapper.queryTaskByFacilitatorGroupIdAndTaskName(getCompanyGroupIdFromUserGroupId(taskQueryDto.getUserId()),taskName);
                default:
                    throw new BusinessException(ErrorCodeEnum.GL9999089);
            }
        }else{
            if(role == null){
                throw new BusinessException(ErrorCodeEnum.GL9999089);
            }
            switch (role){
                case 1://如果角色是甲方用户
                    return imcInspectionTaskMapper.queryTaskByUserId(taskQueryDto.getUserId());
                case 2://如果角色是服务商
                    return this.getTaskByFacilitatorId(taskQueryDto);
                case 3://如果角色是服务商管理员
                    return imcInspectionTaskMapper.queryTaskByFacilitatorManagerId(taskQueryDto.getUserId());
                case 4://如果角色是服务商组织
                    return imcInspectionTaskMapper.queryTaskByFacilitatorGroupId(getCompanyGroupIdFromUserGroupId(taskQueryDto.getUserId()));
                default:
                    throw new BusinessException(ErrorCodeEnum.GL9999089);
            }
        }
    }

    /**
     * 根据用户Id查询对应状态的巡检任务（可分页）
     * @param taskQueryDto
     * @return
     */
    @Override
    public PageInfo getTaskByUserIdAndStatusAndPage(TaskQueryDto taskQueryDto){
        Integer role = taskQueryDto.getRole();
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        String taskName = taskQueryDto.getTaskName();
        List<ImcInspectionTask> imcInspectionTaskList;
        if(StringUtils.isNotBlank(taskName)){
            taskName = "%" + taskName + "%";
            switch (role){
                case 1://如果角色是甲方用户
                    imcInspectionTaskList = imcInspectionTaskMapper.queryTaskByUserIdAndStatusAndTaskName(taskQueryDto.getUserId(),taskQueryDto.getStatus(),taskName);
                    return new PageInfo<>(imcInspectionTaskList);
                case 2://如果角色是服务商
                    imcInspectionTaskList = this.getTaskByFacilitatorIdAndStatus(taskQueryDto);
                    return new PageInfo<>(imcInspectionTaskList);
                case 3://如果角色是服务商管理员
                    imcInspectionTaskList = imcInspectionTaskMapper.queryTaskByFacilitatorManagerIdAndStatusAndTaskName(taskQueryDto.getUserId(),taskQueryDto.getStatus(),taskName);
                    return new PageInfo<>(imcInspectionTaskList);
                case 4://如果角色是服务商组织
                    imcInspectionTaskList = imcInspectionTaskMapper.queryTaskByFacilitatorGroupIdAndStatusAndTaskName(getCompanyGroupIdFromUserGroupId(taskQueryDto.getUserId()),taskQueryDto.getStatus(),taskName);
                    return new PageInfo<>(imcInspectionTaskList);
                default:
                    throw new BusinessException(ErrorCodeEnum.GL9999089);
            }
        }else{
            switch (role){
                case 1://如果角色是甲方用户
                    imcInspectionTaskList = imcInspectionTaskMapper.queryTaskByUserIdAndStatus(taskQueryDto.getUserId(),taskQueryDto.getStatus());
                    return new PageInfo<>(imcInspectionTaskList);
                case 2://如果角色是服务商
                    imcInspectionTaskList = this.getTaskByFacilitatorIdAndStatus(taskQueryDto);
                    return new PageInfo<>(imcInspectionTaskList);
                case 3://如果角色是服务商管理员
                    imcInspectionTaskList = imcInspectionTaskMapper.queryTaskByFacilitatorManagerIdAndStatus(taskQueryDto.getUserId(),taskQueryDto.getStatus());
                    return new PageInfo<>(imcInspectionTaskList);
                case 4://如果角色是服务商组织
                    imcInspectionTaskList = imcInspectionTaskMapper.queryTaskByFacilitatorGroupIdAndStatus(getCompanyGroupIdFromUserGroupId(taskQueryDto.getUserId()),taskQueryDto.getStatus());
                    return new PageInfo<>(imcInspectionTaskList);
                default:
                    throw new BusinessException(ErrorCodeEnum.GL9999089);
            }
        }
    }
    /**
     * 根据用户Id查询对应状态的巡检任务
     * @param taskQueryDto
     * @return
     */
    @Override
    public List<ImcInspectionTask> getTaskByUserIdAndStatus(TaskQueryDto taskQueryDto){
        Integer role = taskQueryDto.getRole();
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        String taskName = taskQueryDto.getTaskName();
        if(StringUtils.isNotBlank(taskName)){
            taskName = "%" + taskName + "%";
            switch (role){
                case 1://如果角色是甲方用户
                    return imcInspectionTaskMapper.queryTaskByUserIdAndStatusAndTaskName(taskQueryDto.getUserId(),taskQueryDto.getStatus(),taskName);
                case 2://如果角色是服务商
                    return this.getTaskByFacilitatorIdAndStatus(taskQueryDto);
                case 3://如果角色是服务商管理员
                    return imcInspectionTaskMapper.queryTaskByFacilitatorManagerIdAndStatusAndTaskName(taskQueryDto.getUserId(),taskQueryDto.getStatus(),taskName);
                case 4://如果角色是服务商组织
                    return imcInspectionTaskMapper.queryTaskByFacilitatorGroupIdAndStatusAndTaskName(getCompanyGroupIdFromUserGroupId(taskQueryDto.getUserId()),taskQueryDto.getStatus(),taskName);
                default:
                    throw new BusinessException(ErrorCodeEnum.GL9999089);
            }
        }else{
            switch (role){
                case 1://如果角色是甲方用户
                    return imcInspectionTaskMapper.queryTaskByUserIdAndStatus(taskQueryDto.getUserId(),taskQueryDto.getStatus());
                case 2://如果角色是服务商
                    return this.getTaskByFacilitatorIdAndStatus(taskQueryDto);
                case 3://如果角色是服务商管理员
                    return imcInspectionTaskMapper.queryTaskByFacilitatorManagerIdAndStatus(taskQueryDto.getUserId(),taskQueryDto.getStatus());
                case 4://如果角色是服务商组织
                    return imcInspectionTaskMapper.queryTaskByFacilitatorGroupIdAndStatus(getCompanyGroupIdFromUserGroupId(taskQueryDto.getUserId()),taskQueryDto.getStatus());
                default:
                    throw new BusinessException(ErrorCodeEnum.GL9999089);
            }
        }
    }

    /**
     * 根据服务商Id查询服务商对应的巡检任务（可分页）
     * @param taskQueryDto
     * @return
     */
    @Override
    public PageInfo getTaskByFacilitatorIdAndPage(TaskQueryDto taskQueryDto){
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        Long facilitatorId = taskQueryDto.getUserId();
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        String taskName = taskQueryDto.getTaskName();
        if(StringUtils.isNotBlank(taskName)){
            taskName = "%" + taskName + "%";
            criteria.andLike("taskName",taskName);
        }
        criteria.andEqualTo("facilitatorId",facilitatorId);
        List<ImcInspectionTask> imcInspectionTaskList = imcInspectionTaskMapper.selectByExample(example);
        return new PageInfo<>(imcInspectionTaskList);
    }

    /**
     * 根据服务商Id查询服务商对应的巡检任务
     * @param taskQueryDto
     * @return
     */
    @Override
    public List<ImcInspectionTask> getTaskByFacilitatorId(TaskQueryDto taskQueryDto){
//        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        Long facilitatorId = taskQueryDto.getUserId();
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        String taskName = taskQueryDto.getTaskName();
        if(StringUtils.isNotBlank(taskName)){
            taskName = "%" + taskName + "%";
            criteria.andLike("taskName",taskName);
        }
        criteria.andEqualTo("facilitatorId",facilitatorId);
        return imcInspectionTaskMapper.selectByExample(example);
    }

    public Integer countTaskByFacilitatorId(Long facilitatorId){
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("facilitatorId",facilitatorId);
        return imcInspectionTaskMapper.selectCountByExample(example);
    }

    /**
     * 根据服务商id查询对应状态的巡检任务（可分页）
     * @param taskQueryDto
     * @return
     */
    @Override
    public PageInfo getTaskByFacilitatorIdAndStatusAndPage(TaskQueryDto taskQueryDto){
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        Long facilitatorId = taskQueryDto.getUserId();
        Integer status = taskQueryDto.getStatus();
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        String taskName = taskQueryDto.getTaskName();
        if(StringUtils.isNotBlank(taskName)){
            taskName = "%" + taskName + "%";
            criteria.andLike("taskName",taskName);
        }
        criteria.andEqualTo("facilitatorId",facilitatorId);
        criteria.andEqualTo("status",status);
        List<ImcInspectionTask> imcInspectionTaskList = imcInspectionTaskMapper.selectByExample(example);
        return new PageInfo<>(imcInspectionTaskList);
    }

    /**
     * 根据服务商id查询对应状态的巡检任务
     * @param taskQueryDto
     * @return
     */
    @Override
    public List<ImcInspectionTask> getTaskByFacilitatorIdAndStatus(TaskQueryDto taskQueryDto){
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        // 登录用户的组织Id
        Long userGroupId = taskQueryDto.getUserId();
        // 获取该用户所属公司Id
        Long facilitatorId = getCompanyGroupIdFromUserGroupId(userGroupId); // 根据组织Id查询公司Id
        Integer status = taskQueryDto.getStatus();
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        String taskName = taskQueryDto.getTaskName();
        if(StringUtils.isNotBlank(taskName)){
            taskName = "%" + taskName + "%";
            criteria.andLike("taskName",taskName);
        }
        criteria.andEqualTo("facilitatorId",facilitatorId);
        criteria.andEqualTo("status",status);
        return imcInspectionTaskMapper.selectByExample(example);
    }

    /**
     * 查询当前甲方负责人下全部未授权的巡检任务（可分页）
     * @param taskQueryDto
     * @return
     */
    @Override
    public PageInfo getAllUnauthorizedTaskByPrincipalIdAndPage(TaskQueryDto taskQueryDto){
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        Long principalId = taskQueryDto.getUserId();
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("principalId",principalId);
        criteria.andEqualTo("status",TaskStatusEnum.WAITING_FOR_PRINCIPAL.getStatusNum());
        List<ImcInspectionTask> imcInspectionTaskList = imcInspectionTaskMapper.selectByExample(example);
        return new PageInfo<>(imcInspectionTaskList);
    }
    /**
     * 查询当前甲方负责人下全部未授权的巡检任务
     * @param taskQueryDto
     * @return
     */
    @Override
    public List<ImcInspectionTask> getAllUnauthorizedTaskByPrincipalId(TaskQueryDto taskQueryDto){
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        Long principalId = taskQueryDto.getUserId();
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("principalId",principalId);
        criteria.andEqualTo("status",TaskStatusEnum.WAITING_FOR_PRINCIPAL.getStatusNum());
        return imcInspectionTaskMapper.selectByExample(example);
    }

    /**
     * 查询当前甲方负责人下全部被否决的巡检任务（可分页）
     * @param taskQueryDto
     * @return
     */
    @Override
    public PageInfo getAllDeniedTaskByPrincipalIdAndPage(TaskQueryDto taskQueryDto){
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        Long principalId = taskQueryDto.getUserId();
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("principalId",principalId);
        criteria.andEqualTo("status",TaskStatusEnum.NO_SUCH_STATUS.getStatusNum());
        List<ImcInspectionTask> imcInspectionTaskList = imcInspectionTaskMapper.selectByExample(example);
        return new PageInfo<>(imcInspectionTaskList);
    }
    /**
     * 查询当前甲方负责人下全部被否决的巡检任务
     * @param taskQueryDto
     * @return
     */
    @Override
    public List<ImcInspectionTask> getAllDeniedTaskByPrincipalId(TaskQueryDto taskQueryDto){
        PageHelper.startPage(taskQueryDto.getPageNum(),taskQueryDto.getPageSize());
        Long principalId = taskQueryDto.getUserId();
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("principalId",principalId);
        criteria.andEqualTo("status",TaskStatusEnum.NO_SUCH_STATUS.getStatusNum());
        return imcInspectionTaskMapper.selectByExample(example);
    }

    /**
     * 同意一项巡检任务
     * @param imcTaskChangeStatusDto
     * @return
     */
    @Override
    public ImcTaskChangeStatusDto acceptImcTaskByPrincipal(ImcTaskChangeStatusDto imcTaskChangeStatusDto){
        Long taskId = imcTaskChangeStatusDto.getTaskId();
        LoginAuthDto loginAuthDto = imcTaskChangeStatusDto.getLoginAuthDto();
        ImcInspectionTask imcInspectionTask = this.getTaskByTaskId(taskId);
        Long facilitatorId = imcInspectionTask.getFacilitatorId();
        if(facilitatorId ==null){
            //如果当前巡检任务不存在服务商
            imcInspectionTask.setStatus(TaskStatusEnum.WAITING_FOR_FACILITATOR.getStatusNum());
        }else{//如果存在服务商
            imcInspectionTask.setStatus(TaskStatusEnum.WAITING_FOR_ACCEPT.getStatusNum());
        }
        imcInspectionTask.setUpdateInfo(loginAuthDto);
        this.update(imcInspectionTask);
        int status = imcInspectionTask.getStatus();
        //推送消息
        MqMessageData mqMessageData = taskMsgProducer.sendTaskStatusMsgMq(imcInspectionTask);
        imcTaskManager.modifyTaskStatus(mqMessageData);
        imcTaskChangeStatusDto.setStatus(status);
        imcTaskChangeStatusDto.setStatusMsg(TaskStatusEnum.getStatusMsg(status));
        return imcTaskChangeStatusDto;
    }

    /**
     * 否决一项巡检任务
     * @param imcTaskChangeStatusDto
     * @return
     */
    @Override
    public ImcTaskChangeStatusDto denyImcTaskByPrincipal(ImcTaskChangeStatusDto imcTaskChangeStatusDto){
        Long taskId = imcTaskChangeStatusDto.getTaskId();
        LoginAuthDto loginAuthDto = imcTaskChangeStatusDto.getLoginAuthDto();
        ImcInspectionTask imcInspectionTask = this.getTaskByTaskId(taskId);
        imcInspectionTask.setStatus(TaskStatusEnum.NO_SUCH_STATUS.getStatusNum());
        imcInspectionTask.setUpdateInfo(loginAuthDto);
        this.update(imcInspectionTask);
        int status = imcInspectionTask.getStatus();
        //推送消息
        MqMessageData mqMessageData = taskMsgProducer.sendTaskStatusMsgMq(imcInspectionTask);
        imcTaskManager.modifyTaskStatus(mqMessageData);
        imcTaskChangeStatusDto.setStatus(status);
        imcTaskChangeStatusDto.setStatusMsg(TaskStatusEnum.getStatusMsg(status));
        return imcTaskChangeStatusDto;
    }

    /**
     * 修改巡检任务对应的服务商ID
     * @param taskChangeFacilitatorDto
     * @return
     */
    @Override
    public TaskChangeFacilitatorDto modifyFacilitator(TaskChangeFacilitatorDto taskChangeFacilitatorDto){
        Long taskId = taskChangeFacilitatorDto.getTaskId();
        Long facilitatorId = taskChangeFacilitatorDto.getFacilitatorId();
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",taskId);
        if(imcInspectionTaskMapper.selectCountByExample(example)==0){
            throw new BusinessException(ErrorCodeEnum.GL9999098);
        }
        ImcInspectionTask imcInspectionTask = this.getTaskByTaskId(taskId);
        imcInspectionTask.setFacilitatorId(facilitatorId);
        int result = this.update(imcInspectionTask);
        if(result == 1){
            return taskChangeFacilitatorDto;
        }
        throw new BusinessException(ErrorCodeEnum.GL9999093);
    }

    /**
     * 服务商拒单
     * @param confirmImcTaskDto
     * @return
     */
    @Override
    public ImcTaskChangeStatusDto refuseImcTaskByFacilitator(ConfirmImcTaskDto confirmImcTaskDto){
        LoginAuthDto loginAuthDto = confirmImcTaskDto.getLoginAuthDto();
        Long taskId = confirmImcTaskDto.getTaskId();
        ImcTaskChangeStatusDto imcTaskChangeStatusDto = new ImcTaskChangeStatusDto();
        imcTaskChangeStatusDto.setStatusMsg(TaskStatusEnum.getStatusMsg(TaskStatusEnum.WAITING_FOR_FACILITATOR.getStatusNum()));
        imcTaskChangeStatusDto.setStatus(TaskStatusEnum.WAITING_FOR_FACILITATOR.getStatusNum());
        imcTaskChangeStatusDto.setTaskId(taskId);
        imcTaskChangeStatusDto.setLoginAuthDto(loginAuthDto);
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",taskId);
        if(imcInspectionTaskMapper.selectCountByExample(example)==0){
            //如果当前任务不存在
            throw new BusinessException(ErrorCodeEnum.GL9999098);
        }
        ImcInspectionTask imcInspectionTask = imcInspectionTaskMapper.selectByExample(example).get(0);
        if(imcInspectionTask.getStatus().equals(TaskStatusEnum.WAITING_FOR_ACCEPT.getStatusNum())){
            //如果当前任务的状态是等待服务商接单，才允许服务商拒单
            imcInspectionTask.setStatus(TaskStatusEnum.WAITING_FOR_FACILITATOR.getStatusNum());
            imcInspectionTask.setUpdateInfo(loginAuthDto);
            imcInspectionTaskMapper.updateByPrimaryKeySelective(imcInspectionTask);
            //推送消息
            MqMessageData mqMessageData = taskMsgProducer.sendTaskStatusMsgMq(imcInspectionTask);
            imcTaskManager.modifyTaskStatus(mqMessageData);
        }else{
            throw new BusinessException(ErrorCodeEnum.GL9999086);
        }
        return imcTaskChangeStatusDto;
    }

    /**
     * 服务商接单
     * @param confirmImcTaskDto
     * @return
     */
    @Override
    public ImcTaskChangeStatusDto acceptImcTaskByFacilitator(ConfirmImcTaskDto confirmImcTaskDto){
        LoginAuthDto loginAuthDto = confirmImcTaskDto.getLoginAuthDto();
        Long taskId = confirmImcTaskDto.getTaskId();
        ImcTaskChangeStatusDto imcTaskChangeStatusDto = new ImcTaskChangeStatusDto();
        imcTaskChangeStatusDto.setStatusMsg(TaskStatusEnum.getStatusMsg(TaskStatusEnum.EXECUTING.getStatusNum()));
        imcTaskChangeStatusDto.setStatus(TaskStatusEnum.EXECUTING.getStatusNum());
        imcTaskChangeStatusDto.setTaskId(taskId);
        imcTaskChangeStatusDto.setLoginAuthDto(loginAuthDto);
        Example example = new Example(ImcInspectionTask.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",taskId);
        if(imcInspectionTaskMapper.selectCountByExample(example)==0){
            //如果当前任务不存在
            throw new BusinessException(ErrorCodeEnum.GL9999098);
        }
        ImcInspectionTask imcInspectionTask = imcInspectionTaskMapper.selectByExample(example).get(0);
        if(imcInspectionTask.getStatus().equals(TaskStatusEnum.WAITING_FOR_ACCEPT.getStatusNum())){
            //如果当前任务的状态是等待服务商接单，才允许服务商接单
            imcInspectionTask.setStatus(TaskStatusEnum.EXECUTING.getStatusNum());
            imcInspectionTask.setUpdateInfo(loginAuthDto);
            imcInspectionTaskMapper.updateByPrimaryKeySelective(imcInspectionTask);
            //推送消息
            MqMessageData mqMessageData = taskMsgProducer.sendTaskStatusMsgMq(imcInspectionTask);
            imcTaskManager.modifyTaskStatus(mqMessageData);
        }else{
            throw new BusinessException(ErrorCodeEnum.GL9999085);
        }
        return imcTaskChangeStatusDto;
    }

    /**
     * 根据任务id查询任务下的子项数
     * @param taskId
     * @return
     */
    @Override
    public Integer getItemNumberByTaskId(Long taskId){
        if(null!=taskId){
            Example example = new Example(ImcInspectionItem.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("inspectionTaskId",taskId);
            return imcInspectionItemService.selectCountByExample(example);
        }else{
            throw new BusinessException(ErrorCodeEnum.GL99990100);
        }
    }

    /**
     * 获取全部未分配工程师，且还剩10天截止的巡检任务
     * @return
     */
    @Override
    public List<UndistributedImcTaskDto> queryAllUndistributedTask(){
        return imcInspectionTaskMapper.queryAllUndistributedTask();
    }

    /**
     * 判断巡检任务是否完成
     * @param taskId
     * @return
     */
    @Override
    public Boolean isTaskFinish(Long taskId){
        Example example = new Example(ImcInspectionItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("inspectionTaskId",taskId);
        List<ImcInspectionItem> imcInspectionItemList = imcInspectionItemMapper.selectByExample(example);
        for(int i=0;i<imcInspectionItemList.size();i++){
            if(imcInspectionItemList.get(i).getStatus()<ItemStatusEnum.INSPECTION_OVER.getStatusNum()){
                //如果还有巡检任务子项没完成
                return false;
            }
        }
        //如果巡检任务子项都完成了，则巡检任务也完成了
        return true;
    }

    /**
     * 从用户的部门组织Id获取用户所属公司的组织Id
     *
     * @param userGroupId
     *
     * @return
     */
    private Long getCompanyGroupIdFromUserGroupId(Long userGroupId) {
        if (userGroupId == null) {
            throw new UacBizException(ErrorCodeEnum.UAC10015010, userGroupId);
        }
        // 根据组织Id查询公司信息
        CompanyDto companyDto = uacGroupFeignApi.getCompanyInfoById(userGroupId).getResult();
        if (PublicUtil.isEmpty(companyDto)) {
            throw new UacBizException(ErrorCodeEnum.UAC10015001, userGroupId);
        }
        Long facilitatorId = companyDto.getId();
        return facilitatorId;
    }
}
