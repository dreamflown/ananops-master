package com.ananops.provider.model.domain;

import com.ananops.core.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Table(name = "sys_dict")
public class ConsoleDict extends BaseEntity {
    private static final long serialVersionUID = -1821457036294854125L;


    @Column(name = "dict_level")
    private String dictLevel;

    /**
     * 状态
     */
    private String status;

    private String mark;

    /**
     * 如果为-1，表示都能查看
     */
    @Column(name = "group_id")
    private Long groupId;

    /**
     * 逻辑删除
     */
    private String dr;



    @Column(name = "create_by")
    private String createBy;


    @Column(name = "update_by")
    private String updateBy;


    private String name;


}