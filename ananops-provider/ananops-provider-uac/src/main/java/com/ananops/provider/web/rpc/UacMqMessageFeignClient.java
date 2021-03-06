/*
 * Copyright (c) 2019. ananops.com All Rights Reserved.
 * 项目名称：ananops平台
 * 类名称：UacMqMessageFeignClient.java
 * 创建人：ananops
 * 平台官网: http://ananops.com
 */

package com.ananops.provider.web.rpc;


import com.github.pagehelper.PageInfo;
import com.ananops.base.dto.MessageQueryDto;
import com.ananops.base.dto.MqMessageVo;
import com.ananops.core.support.BaseController;
import com.ananops.provider.model.service.UacMqMessageFeignApi;
import com.ananops.provider.service.MqMessageService;
import com.ananops.wrapper.WrapMapper;
import com.ananops.wrapper.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * Mq 消息.
 *
 * @author ananops.com @gmail.com
 */
@RestController
@Api(value = "API - UacMqMessageFeignClient", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UacMqMessageFeignClient extends BaseController implements UacMqMessageFeignApi {
	@Resource
	private MqMessageService mqMessageService;

	@Override
	@ApiOperation(httpMethod = "POST", value = "查询含有的messageKey")
	public Wrapper<List<String>> queryMessageKeyList(@RequestParam("messageKeyList") List<String> messageKeyList) {
		logger.info("查询消息KEY. messageKeyList={}", messageKeyList);
		return WrapMapper.ok(mqMessageService.queryMessageKeyList(messageKeyList));
	}

	@Override
	public Wrapper<PageInfo<MqMessageVo>> queryMessageListWithPage(@RequestBody MessageQueryDto messageQueryDto) {
		return mqMessageService.queryMessageListWithPage(messageQueryDto);
	}
}
