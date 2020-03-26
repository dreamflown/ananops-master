package com.ananops.provider.web;

import com.ananops.core.support.BaseController;
import com.ananops.provider.model.domain.BmcBill;
import com.ananops.provider.model.dto.*;
import com.ananops.provider.model.dto.PmcContractDto;
import com.ananops.provider.model.dto.PmcProjectDto;
import com.ananops.provider.service.PmcContractFeignApi;
import com.ananops.provider.service.PmcProjectFeignApi;
import com.ananops.provider.service.impl.BaseServiceImpl;
import com.ananops.provider.utils.WrapMapper;
import com.ananops.provider.utils.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/bill", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(value = "WEB - BaseBillController", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class BaseBillController extends BaseController {

//    @Resource
//    RdcDeviceOrderFeignApi rdcDeviceOrderFeignApi;
    @Resource
    PmcContractFeignApi pmcContractFeignApi;
    @Resource
    PmcProjectFeignApi pmcProjectFeignApi;
    @Resource
    BaseServiceImpl baseServiceImpl;

    @PostMapping(value = "/create")
    @ApiOperation(httpMethod = "POST",value = "创建账单")
    public Wrapper<String> createNew(@ApiParam(name = "body",value="账单信息") @RequestBody BillCreateDto billCreateDto){
        // 判断projectId是否为空
        if (billCreateDto.getProjectId() == null){
            return WrapMapper.error("创建工单时传入的参数projectId为空！getProjectId = "+billCreateDto.getProjectId());
        }
        // 通过projectId找到project，再通过project信息找到ContractId，然后找到contract信息
        PmcProjectDto pmcProjectDto = pmcProjectFeignApi.getProjectByProjectId(billCreateDto.getProjectId()).getResult();
        if (pmcProjectDto == null){
            return WrapMapper.error("getProjectByProjectId获得的项目信息为空！projectId = "+billCreateDto.getProjectId());
        }
        if (pmcProjectDto.getContractId() == null){
            return WrapMapper.error("pmcProjectDto中的合同id为空！pmcProjectDto.getContractId() = "+pmcProjectDto.getContractId());
        }
        PmcContractDto pmcContractDto = pmcContractFeignApi.getContractById(pmcProjectDto.getContractId()).getResult();
        if (pmcContractDto == null){
            return WrapMapper.error("通过getContractById获得的合同信息为空！ContractId = "+pmcProjectDto.getContractId());
        }
        BigDecimal servicePrice = pmcContractDto.getPaymentMoney();
        String transactionMethod = pmcContractDto.getPaymentType().toString();

        if (billCreateDto.getDeviceDtos() != null){
            List<DeviceDto> deviceDtoList = billCreateDto.getDeviceDtos();
            List<Long> deviceIds = new ArrayList<>();
            for (DeviceDto deviceDto : deviceDtoList) {
                deviceIds.add(deviceDto.getDeviceId());
            }
        }
//        com.ananops.wrapper.Wrapper<Object> objects = rdcDeviceOrderFeignApi.getPriceOfDevices(deviceIds);
//        List<JSONObject> jsonObjects = (List<JSONObject>)objects.getResult();
        BigDecimal devicePrice = BigDecimal.valueOf(0);
//        for (JSONObject object : jsonObjects) {
//            devicePrice += object.getBigDecimal("price").floatValue();
//        }

        baseServiceImpl.insert(billCreateDto, devicePrice, servicePrice, transactionMethod);
        return  WrapMapper.ok("success");
    }

    @PostMapping(value = "/createFakeOrder")
    @ApiOperation(httpMethod = "POST",value = "创建账单")
    public Wrapper<BigDecimal> createFakeNew(@ApiParam(name = "body",value="账单信息") @RequestBody BillCreateDto billCreateDto){
        logger.info("createFakeNew - 创建账单. billCreateDto={}", billCreateDto);
        // 判断projectId是否为空
        if (billCreateDto.getProjectId() == null){
            return WrapMapper.error("创建工单时传入的参数projectId为空！getProjectId = "+billCreateDto.getProjectId());
        }
        // 通过projectId找到project，再通过project信息找到ContractId，然后找到contract信息
        Long contractId = getContractId(billCreateDto.getProjectId());
//        if (pmcProjectDto == null){
//            return WrapMapper.error("getProjectByProjectId获得的项目信息为空！projectId = "+billCreateDto.getProjectId());
//        }
//        if (pmcProjectDto.getContractId() == null){
//            return WrapMapper.error("pmcProjectDto中的合同id为空！pmcProjectDto.getContractId() = "+pmcProjectDto.getContractId());
//        }
//        PmcContractDto pmcContractDto = pmcContractFeignApi.getContractById(contractId).getResult();
//        if (pmcContractDto == null){
//            return WrapMapper.error("通过getContractById获得的合同信息为空！ContractId = "+contractId);
//        }

        BigDecimal servicePrice = getServicePrice(contractId);
//        List<DeviceDto> deviceDtoList = billCreateDto.getDeviceDtos();
//        List<Long> deviceIds = new ArrayList<>();
//        for (DeviceDto deviceDto : deviceDtoList) {
//            deviceIds.add(deviceDto.getDeviceId());
//        }
//        com.ananops.wrapper.Wrapper<Object> objects = rdcDeviceOrderFeignApi.getPriceOfDevices(deviceIds);
//        List<JSONObject> jsonObjects = (List<JSONObject>)objects.getResult();
        BigDecimal devicePrice = BigDecimal.valueOf(0);
//        for (JSONObject object : jsonObjects) {
//            devicePrice += object.getBigDecimal("price").floatValue();
//        }
        if (servicePrice != null) {
            return WrapMapper.ok(servicePrice.add(devicePrice));
        }
        return WrapMapper.ok(devicePrice);
    }

    public Long getContractId(Long projectId){
        return pmcProjectFeignApi.getProjectByProjectId(projectId).getResult().getContractId();
    }

    public BigDecimal getServicePrice(Long contractId){
        return pmcContractFeignApi.getContractById(contractId).getResult().getPaymentMoney();
    }

    @GetMapping(value = "/getAllByUser/{userId}")
    @ApiOperation(httpMethod = "GET",value = "根据用户id获取所有账单")
    public  Wrapper<List<BillDisplayDto>> getAllBillByUserId(@ApiParam(name = "userId",value = "用户id") @RequestParam Long userId){
        return WrapMapper.ok(baseServiceImpl.getAllBillByUserId(userId));
    }

    @GetMapping(value = "/getBillNumByUserId/{userId}")
    @ApiOperation(httpMethod = "GET",value = "根据用户id获取账单数量")
    public Wrapper<Integer> getBillNumByUserId(@ApiParam(name = "userId",value = "用户id") @RequestParam Long userId){
        return WrapMapper.ok(baseServiceImpl.getBillNumByUserId(userId));
    }

    @GetMapping(value = "/getBillNumByUserIdAndState/{userId}/{state}")
    @ApiOperation(httpMethod = "GET",value = "根据用户id和支付状态获取账单数量")
    public Wrapper<Integer> getBillNumByUserIdAndState(@ApiParam(name = "userId",value = "用户id") @RequestParam Long userId,
                                                     @ApiParam(name = "state",value = "状态") @RequestParam String state){
        return WrapMapper.ok(baseServiceImpl.getBillNumByUserIdAndState(userId, state));
    }

    @GetMapping(value = "/getBillNumByUserIdAndTransactionMethod/{userId}/{transactionMethod}")
    @ApiOperation(httpMethod = "GET",value = "根据用户id和交易方式获取帐单数量")
    public Wrapper<Integer> getBillNumByUserIdAndTransactionMethod(@ApiParam(name = "userId",value = "用户id") @RequestParam Long userId,
                                                       @ApiParam(name = "transactionMethod",value = "交易方式") @RequestParam String transactionMethod){
        return WrapMapper.ok(baseServiceImpl.getBillNumByUserIdAndTransactionMethod(userId, transactionMethod));
    }

    @GetMapping(value = "/getBillNumByUserIdAndAmount/{userId}/{amount}")
    @ApiOperation(httpMethod = "GET",value = "根据用户id和预约金额获取账单数量")
    public Wrapper<Integer> getBillNumByUserIdAndAmount(@ApiParam(name = "userId",value = "用户id") @RequestParam Long userId,
                                                                   @ApiParam(name = "amount",value = "交易方式") @RequestParam BigDecimal amount){
        return WrapMapper.ok(baseServiceImpl.getBillNumByUserIdAndAmount(userId, amount));
    }

    @PostMapping(value = "/getBillById/{id}")
    @ApiOperation(httpMethod = "POST",value = "根据账单id获取账单")
    public  Wrapper<BillDisplayDto> getBillById(@PathVariable Long id){
        return WrapMapper.ok(baseServiceImpl.getOneBillById(id));
    }

    @GetMapping(value = "/getAmountByWorkOrder/{workOrderId}")
    @ApiOperation(httpMethod = "GET",value = "根据工单id获取金额")
    public Wrapper<BigDecimal> getAmountByWorkOrderId(@ApiParam(name = "workOrderId",value = "工单id") @RequestParam Long workOrderId){
        return WrapMapper.ok(baseServiceImpl.getAmountByWorkOrderId(workOrderId));
    }

    @PutMapping(value = "/modifyAmount")
    @ApiOperation(httpMethod = "PUT",value = "修改金额")
    public void modifyAmount(@ApiParam(name = "modifyAmount",value = "修改金额") @RequestBody BillModifyAmountDto billModifyAmountDto){
        BmcBill bmcBill = baseServiceImpl.getBillById(billModifyAmountDto.getId());
        baseServiceImpl.modifyAmount(bmcBill, billModifyAmountDto.getModifyAmount());
    }

    @GetMapping(value = "/getUBill/{userId}/{state}")
    @ApiOperation(httpMethod = "GET",value = "根据状态及用户id获取账单")
    public Wrapper<List<BmcBill>> getAllUBillByState(@ApiParam(name = "userId",value = "用户id") @RequestParam Long userId,
                                                      @ApiParam(name = "state",value = "状态") @RequestParam String state){
        return WrapMapper.ok(baseServiceImpl.getAllUBillByState(userId, state));
    }

    @GetMapping(value = "/getBillByWorkOrderId/{workOrderId}")
    @ApiOperation(httpMethod = "GET",value = "根据状态及用户id获取账单")
    public Wrapper<List<BmcBill>> getBillByWorkOrderId(@ApiParam(name = "workOrderId",value = "工单id") @RequestParam Long workOrderId){
        return WrapMapper.ok(baseServiceImpl.getBillByWorkOrderId(workOrderId));
    }
}
