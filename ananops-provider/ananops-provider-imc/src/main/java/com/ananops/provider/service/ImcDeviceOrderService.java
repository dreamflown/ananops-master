package com.ananops.provider.service;


import com.ananops.base.dto.LoginAuthDto;
import com.ananops.core.support.IService;
import com.ananops.provider.model.domain.ImcDevice;
import com.ananops.provider.model.domain.ImcDeviceOrder;
import com.ananops.provider.model.dto.DeviceOrderQueryDto;
import com.ananops.provider.model.dto.ImcAddDeviceOrderDto;
import com.ananops.provider.model.vo.DeviceOrderVo;

import java.util.List;

/**
 * Created by rongshuai on 2019/11/27 10:23
 */
public interface ImcDeviceOrderService extends IService<ImcDeviceOrder> {
    ImcDeviceOrder saveDeviceOrder(ImcAddDeviceOrderDto imcAddDeviceOrderDto, LoginAuthDto loginAuthDto);

    List<DeviceOrderVo> getDeviceOrderByTaskId(DeviceOrderQueryDto deviceOrderQueryDto);//根据巡检任务的ID查询对应的备品备件订单

    List<DeviceOrderVo> getDeviceOrderByItemId(DeviceOrderQueryDto deviceOrderQueryDto);//根据巡检任务的子项查询对应的备品备件订单
}
