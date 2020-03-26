package com.ananops.provider.web.frontend;

import com.ananops.base.dto.LoginAuthDto;
import com.ananops.core.support.BaseController;
import com.ananops.provider.model.domain.ConsoleDictItem;
import com.ananops.provider.model.dto.AddDictItemDto;
import com.ananops.provider.service.ConsoleDictItemService;
import com.ananops.wrapper.WrapMapper;
import com.ananops.wrapper.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by huqiaoqian on 2020/3/26
 */
@RestController
@RequestMapping(value = "/consoleDictItem",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(value = "WEB - ConsoleDictItem",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ConsoleDictItemController extends BaseController {

    @Resource
    ConsoleDictItemService dictItemService;

    /**
     * 创建或编辑字典项
     *
     * @param addDictItemDto HTTP请求参数
     *
     * @return 返回
     */
    @PostMapping(value = "/save")
    @ApiOperation(httpMethod = "POST",value = "创建或编辑字典项")
    public Wrapper<AddDictItemDto> saveInspectionItem(@ApiParam(name = "saveDictItem",value = "创建或编辑字典项")@RequestBody AddDictItemDto addDictItemDto){
        LoginAuthDto loginAuthDto = getLoginAuthDto();
        return WrapMapper.ok(dictItemService.saveItem(addDictItemDto,loginAuthDto));
    }


    /**
     * 根据字典库id获取字典项列表
     *
     * @param dictId HTTP请求参数
     *
     * @return 返回
     */
    @GetMapping(value = "/getDictItemListByDictId")
    @ApiOperation(httpMethod = "GET",value = "根据字典库id获取字典项列表")
    public Wrapper<List<ConsoleDictItem>> getDictItemListByDictId(@RequestParam("dictId") Long dictId){
        List<ConsoleDictItem> dictItemList=dictItemService.getDictItemListByDictId(dictId);
        return WrapMapper.ok(dictItemList);
    }

    /**
     * 根据字典项id删除字典项
     *
     * @param itemId HTTP请求参数
     *
     * @return 返回
     */
    @PostMapping(value = "/deleteDictItemByItemDictId/{itemId}")
    @ApiOperation(httpMethod = "POST",value = "根据字典库id删除字典库及其所属字典项")
    public Wrapper<ConsoleDictItem> deleteDictByDictId(@PathVariable Long itemId) {
        LoginAuthDto loginAuthDto = getLoginAuthDto();
        return WrapMapper.ok(dictItemService.deleteDictItemByItemId(itemId,loginAuthDto));
    }


}
