package com.ananops.provider.service;

import com.ananops.base.dto.LoginAuthDto;
import com.ananops.core.support.IService;
import com.ananops.provider.model.domain.ConsoleDictItem;
import com.ananops.provider.model.dto.AddDictItemDto;

import java.util.List;

/**
 * Created by huqiaoqian on 2020/3/26
 */
public interface ConsoleDictItemService extends IService<ConsoleDictItem> {

    /**
     * 创建/编辑字典项
     * @param addDictItemDto
     * @param loginAuthDto
     * @return
     */
    AddDictItemDto saveItem(AddDictItemDto addDictItemDto, LoginAuthDto loginAuthDto);

    /**
     * 根据字典库id获取字典项列表
     * @param dictId
     * @return
     */
    List<ConsoleDictItem> getDictItemListByDictId(Long dictId);

    /**
     * 根据字典项id删除字典项
     * @param itemId
     * @return
     */
    ConsoleDictItem deleteDictItemByItemId(Long itemId,LoginAuthDto loginAuthDto);
}
