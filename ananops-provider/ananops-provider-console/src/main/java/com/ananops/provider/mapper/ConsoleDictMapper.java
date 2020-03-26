package com.ananops.provider.mapper;

import com.ananops.core.mybatis.MyMapper;
import com.ananops.provider.model.domain.ConsoleDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

@Mapper
@Component
public interface ConsoleDictMapper extends MyMapper<ConsoleDict> {
    @Select("select * from sys_dict where `group_id`=-1 and `dr`=0")
    List<ConsoleDict> selectByDefault();

    @Select("select * from sys_dict where `group_id`=#{id} and `dr`=0")
    List<ConsoleDict> selectBygroupId(@Param("id")Long groupId);
}