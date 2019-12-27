/*
 * Copyright (c) 2018. ananops.net All Rights Reserved.
 * 项目名称：ananops快速搭建企业级分布式微服务平台
 * 类名称：OpcRpcService.java
 * 创建人：刘兆明
 * 联系方式：ananops.net@gmail.com
 * 开源地址: https://github.com/ananops
 * 博客地址: http://blog.ananops.net
 * 项目官网: http://ananops.net
 */

package com.ananops.provider.service;


import com.ananops.provider.model.dto.oss.ElementImgUrlDto;
import com.ananops.provider.model.dto.oss.OptBatchGetUrlRequest;
import com.ananops.provider.model.dto.oss.OptGetUrlRequest;
import com.ananops.provider.model.dto.robot.ChatRobotMsgDto;
import com.ananops.wrapper.Wrapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * The class Opc rpc service.
 *
 * @author ananops.net @gmail.com
 */
@Component
public class OpcRpcService {

	@Resource
	private DingtalkFeignApi dingtalkFeignApi;
	@Resource
	private OpcOssFeignApi opcOssFeignApi;

	public boolean sendChatRobotMsg(ChatRobotMsgDto chatRobotMsgDto) {
		Wrapper<Boolean> result = dingtalkFeignApi.sendChatRobotMsg(chatRobotMsgDto);
		return result.getResult();
	}

	public List<ElementImgUrlDto> listFileUrl(OptBatchGetUrlRequest urlRequest) {
		Wrapper<List<ElementImgUrlDto>> result = opcOssFeignApi.listFileUrl(urlRequest);
		return result.getResult();
	}

	public String getFileUrl(final OptGetUrlRequest request) {
		Wrapper<String> result = opcOssFeignApi.getFileUrl(request);
		return result.getResult();
	}
}