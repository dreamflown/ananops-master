package com.ananops.provider.model.domain.elasticsearchDo;

import com.ananops.provider.model.domain.ImcInspectionTaskLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * Created by rongshuai on 2020/3/26 17:04
 */
@Document(indexName = "imc",type = "task")
@Data
public class ImcTaskLog implements Serializable {
    private static final long serialVersionUID = -7800624472943750353L;
    @Id
    String id;

    /**
     * 对应的巡检任务ID
     */
    @Field(type = FieldType.Long)
    private Long taskId;

    /**
     * 当前任务状态
     */
    @Field(type = FieldType.Integer)
    private Integer status;

    /**
     * 操作对应的时间戳
     */
    @Field(type = FieldType.Date)
    private Date statusTimestamp;

    /**
     * 当前操作对应的描述（有可能不存在）
     */
    @Field(type = FieldType.String)
    private String movement;

    /**
     * 操作系统
     */
    @Field(type = FieldType.String)
    private String os;

    /**
     * 浏览器
     */
    @Field(type = FieldType.String)
    private String browser;

    /**
     * ip地址
     */
    @Field(type = FieldType.String)
    private String ipAddress;

    /**
     * 版本号
     */
    @Field(type = FieldType.Integer)
    private Integer version;
    /**
     * 创建人
     */
    @Field(type = FieldType.String)
    private String creator;

    /**
     * 创建人ID
     */
    @Field(type = FieldType.Long)
    private Long creatorId;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Field(type = FieldType.Date,format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    /**
     * 最近操作人
     */
    @Field(type = FieldType.String)
    private String lastOperator;

    /**
     * 最后操作人ID
     */
    @Field(type = FieldType.Long)
    private Long lastOperatorId;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Field(type = FieldType.Date,format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Field(type = FieldType.Integer)
    private Integer pageNum;

    @Field(type = FieldType.Integer)
    private Integer pageSize;

    @Field(type = FieldType.String)
    private String orderBy;
}
