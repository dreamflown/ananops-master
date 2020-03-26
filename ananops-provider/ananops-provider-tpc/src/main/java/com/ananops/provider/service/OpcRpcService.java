/*
 * Copyright (c) 2019. ananops.com All Rights Reserved.
 * 项目名称：ananops平台
 * 类名称：OpcRpcService.java
 * 创建人：ananops
 * 平台官网: http://ananops.com
 */

package com.ananops.provider.service;


import com.github.pagehelper.PageInfo;
import com.ananops.base.dto.MessageQueryDto;
import com.ananops.base.dto.MqMessageVo;
import com.ananops.base.enums.ErrorCodeEnum;
import com.ananops.provider.exceptions.TpcBizException;
import com.ananops.provider.model.dto.robot.ChatRobotMsgDto;
import com.ananops.wrapper.Wrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * The class Opc rpc service.
 *
 * @author ananops.com @gmail.com
 */
@Slf4j
@Component
public class OpcRpcService {

	@Resource
	private DingtalkFeignApi dingtalkFeignApi;
	@Resource
	private OpcOssFeignApi opcOssFeignApi;
	@Resource
	private OpcMqMessageFeignApi opcMqMessageFeignApi;

	/**
	 * Send chat robot msg boolean.
	 *
	 * @param chatRobotMsgDto the chat robot msg dto
	 *
	 * @return the boolean
	 */
	public boolean sendChatRobotMsg(ChatRobotMsgDto chatRobotMsgDto) {
		Wrapper<Boolean> result = dingtalkFeignApi.sendChatRobotMsg(chatRobotMsgDto);
		return result.getResult();
	}

	/**
	 * Delete expire file.
	 */
	public void deleteExpireFile() {
		opcOssFeignApi.deleteExpireFile();
	}

	public Wrapper<PageInfo<MqMessageVo>> queryMessageListWithPage(final MessageQueryDto messageQueryDto) {
		Wrapper<PageInfo<MqMessageVo>> wrapper = opcMqMessageFeignApi.queryMessageListWithPage(messageQueryDto);
		if (wrapper == null) {
			log.error("查询消息记录 失败 result is null");
			throw new TpcBizException(ErrorCodeEnum.GL99990002);
		}
		return wrapper;
	}
}