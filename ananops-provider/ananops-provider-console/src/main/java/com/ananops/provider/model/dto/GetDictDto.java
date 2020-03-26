package com.ananops.provider.model.dto;

import com.ananops.provider.model.domain.ConsoleDict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by huqiaoqian on 2020/3/26
 */
@Data
@ApiModel
public class GetDictDto  implements Serializable {
    private static final long serialVersionUID = 982044666591201843L;

    @ApiModelProperty("字典库信息")
    private ConsoleDict dict;
}
