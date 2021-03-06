/*
 * Copyright (c) 2019. ananops.com All Rights Reserved.
 * 项目名称：ananops平台
 * 类名称：TpcMqTopicController.java
 * 创建人：ananops
 * 平台官网: http://ananops.com
 */

package com.ananops.provider.web.frontend;

import com.ananops.base.dto.LoginAuthDto;
import com.ananops.base.dto.UpdateStatusDto;
import com.ananops.core.annotation.LogAnnotation;
import com.ananops.core.support.BaseController;
import com.ananops.provider.model.domain.TpcMqTag;
import com.ananops.provider.model.domain.TpcMqTopic;
import com.ananops.provider.model.dto.AddTopicDto;
import com.ananops.provider.model.dto.TopicBindTagDto;
import com.ananops.provider.model.vo.TpcMqTopicVo;
import com.ananops.provider.service.TpcMqTopicService;
import com.ananops.wrapper.WrapMapper;
import com.ananops.wrapper.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


/**
 * The class Tpc mq topic controller.
 *
 * @author ananops.com @gmail.com
 */
@RestController
@RequestMapping(value = "/topic", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(value = "WEB - TpcMqTopicController", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TpcMqTopicController extends BaseController {

	@Resource
	private TpcMqTopicService tpcMqTopicService;

	/**
	 * 添加主题
	 * @param addTopicDto
	 * @return
	 */
	@PostMapping(value = "/addMqTopic")
	@ApiOperation(httpMethod = "POST", value = "新建MQ topic")
	public Wrapper<TpcMqTopic> addMqTopic(@ApiParam(name = "topic", value = "MQ-Topic") @RequestBody AddTopicDto addTopicDto){
		LoginAuthDto loginAuthDto = super.getLoginAuthDto();
		logger.info("新建主题addTopicDto={}", addTopicDto);
		return WrapMapper.ok(tpcMqTopicService.addMqTopic(addTopicDto,loginAuthDto));
	}

	@PostMapping(value = "/topicBindTag")
	@ApiOperation(httpMethod = "POST", value = "为topic绑定tag")
	public Wrapper<TpcMqTag> topicBindTag(@ApiParam(name = "tag", value = "MQ-Tag") @RequestBody TopicBindTagDto topicBindTagDto){
		LoginAuthDto loginAuthDto = super.getLoginAuthDto();
		logger.info("新建主题topicBindTag={}", topicBindTagDto);
		return WrapMapper.ok(tpcMqTopicService.topicBindTag(topicBindTagDto,loginAuthDto));
	}

	/**
	 * 查询MQ topic列表.
	 *
	 * @param tpcMqTopic the tpc mq topic
	 *
	 * @return the wrapper
	 */
	@PostMapping(value = "/queryTopicListWithPage")
	@ApiOperation(httpMethod = "POST", value = "查询MQ topic列表")
	public Wrapper<List<TpcMqTopicVo>> queryTopicListWithPage(@ApiParam(name = "topic", value = "MQ-Topic") @RequestBody TpcMqTopic tpcMqTopic) {

		logger.info("查询角色列表tpcMqTopicQuery={}", tpcMqTopic);
		List<TpcMqTopicVo> list = tpcMqTopicService.listWithPage(tpcMqTopic);
		return WrapMapper.ok(list);
	}

	/**
	 * 修改topic状态.
	 *
	 * @param updateStatusDto the update status dto
	 *
	 * @return the wrapper
	 */
	@PostMapping(value = "/modifyStatusById")
	@ApiOperation(httpMethod = "POST", value = "修改topic状态")
	@LogAnnotation
	public Wrapper modifyTopicStatusById(@ApiParam(value = "修改topic状态") @RequestBody UpdateStatusDto updateStatusDto) {
		logger.info("修改topic状态 updateStatusDto={}", updateStatusDto);
		Long roleId = updateStatusDto.getId();

		LoginAuthDto loginAuthDto = getLoginAuthDto();

		TpcMqTopic topic = new TpcMqTopic();
		topic.setId(roleId);
		topic.setStatus(updateStatusDto.getStatus());
		topic.setUpdateInfo(loginAuthDto);

		int result = tpcMqTopicService.update(topic);
		return super.handleResult(result);
	}
}
