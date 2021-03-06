package com.ananops.provider.service.impl;


import com.ananops.base.dto.LoginAuthDto;
import com.ananops.base.enums.ErrorCodeEnum;
import com.ananops.base.exception.BusinessException;
import com.ananops.core.support.BaseService;
import com.ananops.provider.mapper.ImcDeviceMapper;
import com.ananops.provider.mapper.ImcDeviceOrderMapper;
import com.ananops.provider.mapper.ImcInspectionItemMapper;
import com.ananops.provider.mapper.ImcInspectionTaskMapper;
import com.ananops.provider.model.domain.ImcDevice;
import com.ananops.provider.model.domain.ImcDeviceOrder;
import com.ananops.provider.model.domain.ImcInspectionItem;
import com.ananops.provider.model.domain.ImcInspectionTask;
import com.ananops.provider.model.dto.DeviceDto;
import com.ananops.provider.model.dto.DeviceOrderQueryDto;
import com.ananops.provider.model.dto.ImcAddDeviceOrderDto;
import com.ananops.provider.model.vo.DeviceOrderVo;
import com.ananops.provider.service.ImcDeviceOrderService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rongshuai on 2019/11/27 10:23
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ImcDeviceOrderServiceImpl extends BaseService<ImcDeviceOrder> implements ImcDeviceOrderService {
    @Resource
    private ImcDeviceMapper imcDeviceMapper;

    @Resource
    private ImcDeviceOrderMapper imcDeviceOrderMapper;

    @Resource
    private ImcInspectionTaskMapper imcInspectionTaskMapper;

    @Resource
    private ImcInspectionItemMapper imcInspectionItemMapper;

    public ImcDeviceOrder saveDeviceOrder(ImcAddDeviceOrderDto imcAddDeviceOrderDto, LoginAuthDto loginAuthDto){//编辑备品备件订单
        ImcDeviceOrder imcDeviceOrder = new ImcDeviceOrder();
        BeanUtils.copyProperties(imcAddDeviceOrderDto,imcDeviceOrder);
        imcDeviceOrder.setUpdateInfo(loginAuthDto);
        Long inspectionTaskId = imcDeviceOrder.getInspectionTaskId();//获取当前订单对应的巡检任务ID
        Long inspectionItemId = imcDeviceOrder.getInspectionItemId();//获取当前订单对应的巡检任务子项ID
        Example example1 = new Example(ImcInspectionTask.class);
        Example.Criteria criteria1 = example1.createCriteria();
        criteria1.andEqualTo("id",inspectionTaskId);
        if(imcInspectionTaskMapper.selectCountByExample(example1)==0){//如果备品备件订单对应的巡检任务不存在
            throw new BusinessException(ErrorCodeEnum.GL9999098,inspectionTaskId);
        }
        List<ImcInspectionTask> imcInspectionTasks = imcInspectionTaskMapper.selectByExample(example1);
        ImcInspectionTask imcInspectionTask = imcInspectionTasks.get(0);
        Example example2 = new Example(ImcInspectionItem.class);
        Example.Criteria criteria2 = example2.createCriteria();
        criteria2.andEqualTo("id",inspectionItemId);
        if(imcInspectionItemMapper.selectCountByExample(example2)==0){//如果备品备件订单对应的巡检任务子项不存在
            throw new BusinessException(ErrorCodeEnum.GL9999097,inspectionItemId);
        }
        List<ImcInspectionItem> imcInspectionItems = imcInspectionItemMapper.selectByExample(example2);
        ImcInspectionItem imcInspectionItem = imcInspectionItems.get(0);
        if(!imcInspectionItem.getInspectionTaskId().equals(imcInspectionTask.getId())){
            throw new BusinessException(ErrorCodeEnum.GL9999097,inspectionItemId);
        }
        if(imcDeviceOrder.isNew()){//如果需要新增一条备品备件订单
            Long deviceOrderId = super.generateId();
            imcDeviceOrder.setId(deviceOrderId);
            imcDeviceOrderMapper.insert(imcDeviceOrder);
            List<DeviceDto> deviceDtoList = imcAddDeviceOrderDto.getDeviceDtoList();
            deviceDtoList.forEach(deviceDto -> {
                ImcDevice imcDevice = new ImcDevice();
                BeanUtils.copyProperties(deviceDto,imcDevice);
                Long deviceId = super.generateId();
                imcDevice.setId(deviceId);
                imcDevice.setUpdateInfo(loginAuthDto);
                imcDevice.setOrderId(deviceOrderId);
                imcDeviceMapper.insert(imcDevice);
            });
        }else{//如果需要修改已经存在的备品备件订单
            imcDeviceOrderMapper.updateByPrimaryKeySelective(imcDeviceOrder);//只更新变化的字段
        }

        return imcDeviceOrder;
    }

    public List<DeviceOrderVo> getDeviceOrderByTaskId(DeviceOrderQueryDto deviceOrderQueryDto){
        Long taskId = deviceOrderQueryDto.getTaskId();
        Example example = new Example(ImcDeviceOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("inspectionTaskId",taskId);
        PageHelper.startPage(deviceOrderQueryDto.getPageNum(),deviceOrderQueryDto.getPageSize());
        List<ImcDeviceOrder> imcDeviceOrderList = imcDeviceOrderMapper.selectByExample(example);
        List<DeviceOrderVo> deviceOrderVoList = new ArrayList<>();
        imcDeviceOrderList.forEach(imcDeviceOrder -> {
            DeviceOrderVo deviceOrderVo = new DeviceOrderVo();
            BeanUtils.copyProperties(imcDeviceOrder,deviceOrderVo);
            Long orderId = imcDeviceOrder.getId();
            Example example2 = new Example(ImcDevice.class);
            Example.Criteria criteria2 = example2.createCriteria();
            criteria2.andEqualTo("orderId",orderId);
            deviceOrderVo.setImcDeviceList(imcDeviceMapper.selectByExample(example2));
            deviceOrderVoList.add(deviceOrderVo);
        });
        return deviceOrderVoList;
    }

    public List<DeviceOrderVo> getDeviceOrderByItemId(DeviceOrderQueryDto deviceOrderQueryDto){
        Long itemId = deviceOrderQueryDto.getItemId();
        Example example = new Example(ImcDeviceOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("inspectionItemId",itemId);
        PageHelper.startPage(deviceOrderQueryDto.getPageNum(),deviceOrderQueryDto.getPageSize());
        List<ImcDeviceOrder> imcDeviceOrderList = imcDeviceOrderMapper.selectByExample(example);
        List<DeviceOrderVo> deviceOrderVoList = new ArrayList<>();
        imcDeviceOrderList.forEach(imcDeviceOrder -> {
            DeviceOrderVo deviceOrderVo = new DeviceOrderVo();
            BeanUtils.copyProperties(imcDeviceOrder,deviceOrderVo);
            Long orderId = imcDeviceOrder.getId();
            Example example2 = new Example(ImcDevice.class);
            Example.Criteria criteria2 = example2.createCriteria();
            criteria2.andEqualTo("orderId",orderId);
            deviceOrderVo.setImcDeviceList(imcDeviceMapper.selectByExample(example2));
            deviceOrderVoList.add(deviceOrderVo);
        });
        return deviceOrderVoList;
    }
}
