/*
 * Copyright (c) 2019. ananops.com All Rights Reserved.
 * 项目名称：ananops平台
 * 类名称：TpcMqProducerController.java
 * 创建人：ananops
 * 平台官网: http://ananops.com
 */

package com.ananops.provider.web.frontend;

import com.ananops.provider.model.dto.AddMqProducerDto;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ananops.base.dto.LoginAuthDto;
import com.ananops.base.dto.UpdateStatusDto;
import com.ananops.core.annotation.LogAnnotation;
import com.ananops.core.support.BaseController;
import com.ananops.provider.model.domain.TpcMqProducer;
import com.ananops.provider.model.vo.TpcMqProducerVo;
import com.ananops.provider.model.vo.TpcMqPublishVo;
import com.ananops.provider.service.TpcMqProducerService;
import com.ananops.wrapper.WrapMapper;
import com.ananops.wrapper.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * 生产者管理.
 *
 * @author ananops.com @gmail.com
 */
@RestController
@RequestMapping(value = "/producer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(value = "WEB - TpcMqProducerController", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TpcMqProducerController extends BaseController {

	@Resource
	private TpcMqProducerService tpcMqProducerService;

	@PostMapping(value = "/addProducer")
	@ApiOperation(httpMethod = "POST", value = "创建一个生产者")
	public Wrapper<TpcMqProducer> addProducer(@ApiParam(name = "producer", value = "Mq生产者") @RequestBody AddMqProducerDto addMqProducerDto){
		LoginAuthDto loginAuthDto = super.getLoginAuthDto();
		logger.info("创建生产者addMqProducerDto={}",addMqProducerDto);
		return WrapMapper.ok(tpcMqProducerService.addProducer(addMqProducerDto,loginAuthDto));
	}

	/**
	 * 查询生产者列表.
	 *
	 * @param tpcMqProducer the tpc mq producer
	 *
	 * @return the wrapper
	 */
	@PostMapping(value = "/queryProducerVoListWithPage")
	@ApiOperation(httpMethod = "POST", value = "查询生产者列表")
	public Wrapper<List<TpcMqProducerVo>> queryProducerList(@ApiParam(name = "producer", value = "Mq生产者") @RequestBody TpcMqProducer tpcMqProducer) {

		logger.info("查询生产者列表tpcMqTopicQuery={}", tpcMqProducer);
		List<TpcMqProducerVo> list = tpcMqProducerService.listProducerVoWithPage(tpcMqProducer);
		return WrapMapper.ok(list);
	}

	/**
	 * 查询发布者列表.
	 *
	 * @param tpcMqProducer the tpc mq producer
	 *
	 * @return the wrapper
	 */
	@PostMapping(value = "/queryPublishListWithPage")
	@ApiOperation(httpMethod = "POST", value = "查询发布者列表")
	public Wrapper<PageInfo<TpcMqPublishVo>> queryPublishListWithPage(@ApiParam(name = "producer", value = "Mq生产者") @RequestBody TpcMqProducer tpcMqProducer) {
		logger.info("查询Mq发布列表tpcMqTopicQuery={}", tpcMqProducer);
		PageHelper.startPage(tpcMqProducer.getPageNum(), tpcMqProducer.getPageSize());
		tpcMqProducer.setOrderBy("update_time desc");
		List<TpcMqPublishVo> list = tpcMqProducerService.listPublishVoWithPage(tpcMqProducer);
		return WrapMapper.ok(new PageInfo<>(list));
	}

	/**
	 * 修改生产者状态.
	 *
	 * @param updateStatusDto the update status dto
	 *
	 * @return the wrapper
	 */
	@PostMapping(value = "/modifyStatusById")
	@ApiOperation(httpMethod = "POST", value = "修改生产者状态")
	@LogAnnotation
	public Wrapper modifyProducerStatusById(@ApiParam(value = "修改producer状态") @RequestBody UpdateStatusDto updateStatusDto) {
		logger.info("修改producer状态 updateStatusDto={}", updateStatusDto);
		Long roleId = updateStatusDto.getId();

		LoginAuthDto loginAuthDto = getLoginAuthDto();

		TpcMqProducer producer = new TpcMqProducer();
		producer.setId(roleId);
		producer.setStatus(updateStatusDto.getStatus());
		producer.setUpdateInfo(loginAuthDto);

		int result = tpcMqProducerService.update(producer);
		return super.handleResult(result);
	}

	/**
	 * 根据生产者ID删除生产者.
	 *
	 * @param id the id
	 *
	 * @return the wrapper
	 */
	@PostMapping(value = "/deleteById/{id}")
	@ApiOperation(httpMethod = "POST", value = "根据生产者ID删除生产者")
	@LogAnnotation
	public Wrapper deleteProducerById(@PathVariable Long id) {
		logger.info("删除producer id={}", id);
		int result = tpcMqProducerService.deleteProducerById(id);
		return super.handleResult(result);
	}
}
