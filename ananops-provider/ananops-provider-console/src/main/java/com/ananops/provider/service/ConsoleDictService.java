package com.ananops.provider.service;

import com.ananops.base.dto.LoginAuthDto;
import com.ananops.core.support.IService;
import com.ananops.provider.model.domain.ConsoleDict;
import com.ananops.provider.model.dto.AddDictDto;
import com.ananops.provider.model.dto.GetDictDto;

import java.util.List;

/**
 * Created by huqiaoqian on 2020/3/26
 */
public interface ConsoleDictService extends IService<ConsoleDict> {

    /**
     * 创建/编辑字典库
     * @param addDictDto
     * @param loginAuthDto
     * @return
     */
    AddDictDto saveDict(AddDictDto addDictDto, LoginAuthDto loginAuthDto);

    /**
     * 根据用户id获取字典库列表
     * @param userId
     * @return
     */
    List<GetDictDto> getDictListByUserId(Long userId);

    /**
     * 根据字典库id删除字典库及其所属字典项
     * @param dictId
     * @return
     */
    ConsoleDict deleteDictByDictId(Long dictId,LoginAuthDto loginAuthDto);
}
