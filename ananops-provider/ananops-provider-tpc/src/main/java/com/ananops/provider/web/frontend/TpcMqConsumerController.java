/*
 * Copyright (c) 2019. ananops.com All Rights Reserved.
 * 项目名称：ananops平台
 * 类名称：TpcMqConsumerController.java
 * 创建人：ananops
 * 平台官网: http://ananops.com
 */

package com.ananops.provider.web.frontend;

import com.ananops.provider.model.domain.TpcMqSubscribe;
import com.ananops.provider.model.dto.AddMqConsumerDto;
import com.ananops.provider.model.dto.ConsumerSubscribeTopicDto;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ananops.PublicUtil;
import com.ananops.base.dto.LoginAuthDto;
import com.ananops.base.dto.UpdateStatusDto;
import com.ananops.core.annotation.LogAnnotation;
import com.ananops.core.support.BaseController;
import com.ananops.provider.model.domain.TpcMqConsumer;
import com.ananops.provider.model.vo.TpcMqConsumerVo;
import com.ananops.provider.model.vo.TpcMqSubscribeVo;
import com.ananops.provider.service.TpcMqConsumerService;
import com.ananops.wrapper.WrapMapper;
import com.ananops.wrapper.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * 消费者管理.
 *
 * @author ananops.com @gmail.com
 */
@RestController
@RequestMapping(value = "/consumer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(value = "WEB - TpcMqConsumerController", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TpcMqConsumerController extends BaseController {

	@Resource
	private TpcMqConsumerService tpcMqConsumerService;

	/**
	 * 创建生产者
	 * @param addMqConsumerDto
	 * @return
	 */
	@PostMapping(value = "/addConsumer")
	@ApiOperation(httpMethod = "POST", value = "创建Mq消费者")
	public Wrapper<TpcMqConsumer> addConsumer(@ApiParam(name = "consumer", value = "Mq消费者") @RequestBody AddMqConsumerDto addMqConsumerDto){
		LoginAuthDto loginAuthDto = super.getLoginAuthDto();
		logger.info("创建消费者addMqConsumer={}",addMqConsumerDto);
		return WrapMapper.ok(tpcMqConsumerService.addConsumer(addMqConsumerDto,loginAuthDto));
	}

	@PostMapping(value = "/consumerSubscribeTopic")
	@ApiOperation(httpMethod = "POST", value = "Mq消费者订阅topic")
	public Wrapper<TpcMqSubscribe> consumerSubscribeTopic(@ApiParam(name = "consumerSubscribeTopicDto", value = "Mq消费者订阅topic") @RequestBody ConsumerSubscribeTopicDto consumerSubscribeTopicDto){
		logger.info("消费者订阅主题consumerSubscribeTopic={}",consumerSubscribeTopicDto);
		return WrapMapper.ok(tpcMqConsumerService.consumerSubcribeTopic(consumerSubscribeTopicDto));
	}
	/**
	 * 查询Mq消费者列表.
	 *
	 * @param tpcMqConsumer the tpc mq consumer
	 *
	 * @return the wrapper
	 */
	@PostMapping(value = "/queryConsumerVoListWithPage")
	@ApiOperation(httpMethod = "POST", value = "查询Mq消费者列表")
	public Wrapper<List<TpcMqConsumerVo>> queryConsumerVoList(@ApiParam(name = "consumer", value = "Mq消费者") @RequestBody TpcMqConsumer tpcMqConsumer) {

		logger.info("查询消费者列表tpcMqProducerQuery={}", tpcMqConsumer);
		List<TpcMqConsumerVo> list = tpcMqConsumerService.listConsumerVoWithPage(tpcMqConsumer);
		return WrapMapper.ok(list);
	}

	/**
	 * 查询订阅者列表.
	 *
	 * @param tpcMqConsumer the tpc mq consumer
	 *
	 * @return the wrapper
	 */
	@PostMapping(value = "/querySubscribeListWithPage")
	@ApiOperation(httpMethod = "POST", value = "查询订阅者列表")
	public Wrapper<PageInfo<TpcMqSubscribeVo>> querySubscribeListWithPage(@ApiParam(name = "consumer", value = "Mq消费者") @RequestBody TpcMqConsumer tpcMqConsumer) {
		logger.info("查询Mq订阅列表tpcMqConsumerQuery={}", tpcMqConsumer);
		PageHelper.startPage(tpcMqConsumer.getPageNum(), tpcMqConsumer.getPageSize());
		tpcMqConsumer.setOrderBy("update_time desc");
		List<TpcMqSubscribeVo> list = tpcMqConsumerService.listSubscribeVoWithPage(tpcMqConsumer);
		PageInfo<TpcMqSubscribeVo> pageInfo = new PageInfo<>(list);
		if (PublicUtil.isNotEmpty(list)) {
			Map<Long, TpcMqSubscribeVo> tpcMqSubscribeVoMap = this.trans2Map(list);
			List<Long> subscribeIdList = new ArrayList<>(tpcMqSubscribeVoMap.keySet());
			List<TpcMqSubscribeVo> tagVoList = tpcMqConsumerService.listSubscribeVo(subscribeIdList);
			for (TpcMqSubscribeVo vo : tagVoList) {
				Long subscribeId = vo.getId();
				if (!tpcMqSubscribeVoMap.containsKey(subscribeId)) {
					continue;
				}
				TpcMqSubscribeVo tpcMqSubscribeVo = tpcMqSubscribeVoMap.get(subscribeId);
				tpcMqSubscribeVo.setTagVoList(vo.getTagVoList());
			}
			pageInfo.setList(new ArrayList<>(tpcMqSubscribeVoMap.values()));
		}
		return WrapMapper.ok(pageInfo);
	}

	private Map<Long, TpcMqSubscribeVo> trans2Map(List<TpcMqSubscribeVo> resultDTOS) {
		Map<Long, TpcMqSubscribeVo> resultMap = new TreeMap<>((o1, o2) -> {
			o1 = o1 == null ? 0 : o1;
			o2 = o2 == null ? 0 : o2;
			return o2.compareTo(o1);
		});
		for (TpcMqSubscribeVo resultDTO : resultDTOS) {
			resultMap.put(resultDTO.getId(), resultDTO);
		}
		return resultMap;
	}

	/**
	 * 更改消费者状态.
	 *
	 * @param updateStatusDto the update status dto
	 *
	 * @return the wrapper
	 */
	@PostMapping(value = "/modifyStatusById")
	@ApiOperation(httpMethod = "POST", value = "更改消费者状态")
	@LogAnnotation
	public Wrapper modifyConsumerStatusById(@ApiParam(value = "更改消费者状态") @RequestBody UpdateStatusDto updateStatusDto) {
		logger.info("修改consumer状态 updateStatusDto={}", updateStatusDto);
		Long consumerId = updateStatusDto.getId();

		LoginAuthDto loginAuthDto = getLoginAuthDto();

		TpcMqConsumer consumer = new TpcMqConsumer();
		consumer.setId(consumerId);
		consumer.setStatus(updateStatusDto.getStatus());
		consumer.setUpdateInfo(loginAuthDto);

		int result = tpcMqConsumerService.update(consumer);
		return super.handleResult(result);
	}

	/**
	 * 根据消费者ID删除消费者.
	 *
	 * @param id the id
	 *
	 * @return the wrapper
	 */
	@PostMapping(value = "/deleteById/{id}")
	@ApiOperation(httpMethod = "POST", value = "根据消费者ID删除消费者")
	@LogAnnotation
	public Wrapper deleteConsumerById(@PathVariable Long id) {
		logger.info("删除consumer id={}", id);
		int result = tpcMqConsumerService.deleteConsumerById(id);
		return super.handleResult(result);
	}
}
