package com.ananops.provider.mapper;

import com.ananops.core.mybatis.MyMapper;
import com.ananops.provider.model.domain.ConsoleDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ConsoleDictItemMapper extends MyMapper<ConsoleDictItem> {
    @Select("select * from sys_dict_item where `dict_id`=#{id} and `dr`=0 order by sort")
    List<ConsoleDictItem> selectBygDictId(@Param("id")Long dictId);
}