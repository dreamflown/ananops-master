package com.ananops.provider.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by rongshuai on 2020/3/26 20:32
 */
@Data
@ApiModel
public class ElasticPageDto<T> implements Serializable {
    private static final long serialVersionUID = 2411150893504637448L;

    /**
     * 总的记录数
     */
    @ApiModelProperty(value = "总的记录数")
    private Long total;

    /**
     * 记录列表
     */
    @ApiModelProperty(value = "记录列表")
    private List<T> list;
}
