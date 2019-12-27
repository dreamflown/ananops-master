/*
 * Copyright (c) 2018. ananops.net All Rights Reserved.
 * 项目名称：ananops快速搭建企业级分布式微服务平台
 * 类名称：TpcJobTask.java
 * 创建人：刘兆明
 * 联系方式：ananops.net@gmail.com
 * 开源地址: https://github.com/ananops
 * 博客地址: http://blog.ananops.net
 * 项目官网: http://ananops.net
 */

package com.ananops.provider.model.domain;

import lombok.Data;
import org.apache.ibatis.type.Alias;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * The class Tpc job task.
 *
 * @author ananops.net @gmail.com
 */
@Data
@Table(name = "an_tpc_job_task")
@Alias(value = "tpcJobTask")
public class TpcJobTask implements Serializable {
	private static final long serialVersionUID = -7833392442916077253L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Integer version;

	/**
	 * 关联业务单号
	 */
	@Column(name = "ref_no")
	private String refNo;

	/**
	 * 业务类型
	 */
	@Column(name = "task_type")
	private String taskType;

	/**
	 * 执行次数
	 */
	@Column(name = "task_exe_count")
	private Integer taskExeCount;

	/**
	 * 任务数据
	 */
	@Column(name = "task_data")
	private String taskData;

	/**
	 * 是否死亡
	 */
	private Integer dead;

	/**
	 * 状态
	 */
	private Integer status;

	/**
	 * 执行实例IP
	 */
	@Column(name = "exe_instance_ip")
	private String exeInstanceIp;

	/**
	 * 创建时间
	 */
	@Column(name = "create_time")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@Column(name = "update_time")
	private Date updateTime;

	/**
	 * 执行时间
	 */
	@Column(name = "exe_time")
	private Integer exeTime;

	/**
	 * 删除标识
	 */
	private Integer yn;

	/**
	 * Add send times.
	 */
	public void addSendTimes() {
		this.taskExeCount++;
	}
}