package com.ananops.provider.model.domain;

import com.ananops.core.mybatis.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "an_amc_alarm")
@Data
@ApiModel
public class AmcAlarm extends BaseEntity {

    /**
     * 告警名称
     */
    @ApiModelProperty(value = "告警名称")
    @Column(name = "alarm_name")
    private String alarmName;

    /**
     * 告警类型
     */
    @ApiModelProperty(value = "告警类型")
    @Column(name = "alarm_type")
    private String alarmType;

    /**
     * 告警等级，1-紧急，2-可疑，3-提醒
     */
    @ApiModelProperty(value = "告警等级，1-紧急，2-可疑，3-提醒")
    @Column(name = "alarm_level")
    private Integer alarmLevel;

    /**
     * 受影响资产
     */
    @ApiModelProperty(value = "受影响资产")
    @Column(name = "alarm_asset")
    private String alarmAsset;

    /**
     * 受影响资产类型
     */
    @ApiModelProperty(value = "受影响资产类型")
    @Column(name = "asset_type")
    private String assetType;

    /**
     * 告警状态，1-待处理，0-已处理
     */
    @ApiModelProperty(value = "告警状态，1-待处理，0-已处理")
    @Column(name = "alarm_status")
    private Integer alarmStatus;

    /**
     * 报警位置
     */
    @ApiModelProperty(value = "报警位置")
    @Column(name = "alarm_location")
    private String alarmLocation;

    /**
     * 报警经度
     */
    @ApiModelProperty(value = "报警经度")
    @Column(name = "alarm_longitude")
    private BigDecimal alarmLongitude;

    /**
     * 报警维度
     */
    @ApiModelProperty(value = "报警维度")
    @Column(name = "alarm_latitude")
    private BigDecimal alarmLatitude;

    /**
     * 报警维度
     */
    @ApiModelProperty(value = "报警图片")
    @Column(name = "alarm_photo")
    private String alarmPhoto;

    /**
     * 最近发生时间
     */
    @ApiModelProperty(value = "最近发生时间",example = "2019-12-01 12:18:48")
    @Column(name = "last_occur_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastOccurTime;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;

    /**
     * 组织id
     */
    @ApiModelProperty(value = "组织id")
    @Column(name = "group_id")
    private Long groupId;

    /**
     * 组织名称
     */
    @ApiModelProperty(value = "组织名称")
    @Column(name = "group_name")
    private String groupName;



}