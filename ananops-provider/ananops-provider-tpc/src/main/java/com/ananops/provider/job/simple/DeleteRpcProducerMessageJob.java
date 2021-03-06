
package com.ananops.provider.job.simple;

import com.alibaba.fastjson.JSON;
import com.ananops.config.properties.AnanopsProperties;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.ananops.base.constant.AliyunMqTopicConstants;
import com.ananops.core.generator.UniqueIdGenerator;
import com.ananops.elastic.lite.annotation.ElasticJobConfig;
import com.ananops.provider.model.dto.TpcMqMessageDto;
import com.ananops.provider.service.TpcMqMessageService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * 定时清理所有生产者发送成功的消息数据.
 *
 * @author ananops.com @gmail.com
 */
@Slf4j
@ElasticJobConfig(cron = "0 0 1 1/1 * ?")
public class DeleteRpcProducerMessageJob implements SimpleJob {

	@Resource
	private AnanopsProperties ananOpsProperties;
	@Resource
	private TpcMqMessageService tpcMqMessageService;

	/**
	 * Execute.
	 *
	 * @param shardingContext the sharding context
	 */
	@Override
	public void execute(final ShardingContext shardingContext) {

		final TpcMqMessageDto message = new TpcMqMessageDto();
		message.setMessageBody(JSON.toJSONString(shardingContext));
		message.setMessageTag(AliyunMqTopicConstants.MqTagEnum.DELETE_PRODUCER_MESSAGE.getTag());
		message.setMessageTopic(AliyunMqTopicConstants.MqTopicEnum.TPC_TOPIC.getTopic());
		message.setProducerGroup(ananOpsProperties.getAliyun().getRocketMq().getProducerGroup());
		String refNo = Long.toString(UniqueIdGenerator.generateId());
		message.setRefNo(refNo);
		message.setMessageKey(refNo);
		tpcMqMessageService.saveAndSendMessage(message);
	}
}
