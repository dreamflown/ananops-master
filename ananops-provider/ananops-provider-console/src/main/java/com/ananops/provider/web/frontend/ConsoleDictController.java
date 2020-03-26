package com.ananops.provider.web.frontend;

import com.ananops.base.dto.LoginAuthDto;
import com.ananops.core.support.BaseController;
import com.ananops.provider.model.domain.ConsoleDict;
import com.ananops.provider.model.dto.AddDictDto;
import com.ananops.provider.model.dto.GetDictDto;
import com.ananops.provider.service.ConsoleDictService;
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
@RequestMapping(value = "/consoleDict",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(value = "WEB - ConsoleDict",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ConsoleDictController extends BaseController {

    @Resource
    ConsoleDictService consoleDictService;

    /**
     * 创建或编辑字典库
     *
     * @param addDictDto HTTP请求参数
     *
     * @return 返回
     */
    @PostMapping(value = "/save")
    @ApiOperation(httpMethod = "POST",value = "创建或编辑字典库")
    public Wrapper<AddDictDto> saveTask(@ApiParam(name = "saveDict",value = "添加或编辑字典库")@RequestBody AddDictDto addDictDto){
        LoginAuthDto loginAuthDto = getLoginAuthDto();
        return WrapMapper.ok(consoleDictService.saveDict(addDictDto,loginAuthDto));
    }

    /**
     * 根据用户id获取字典库列表
     *
     * @param userId HTTP请求参数
     *
     * @return 返回
     */
    @GetMapping(value = "/getDictListByUserId")
    @ApiOperation(httpMethod = "GET",value = "根据用户id获取字典库列表")
    public Wrapper<List<GetDictDto>> getDictListByUserId(@ApiParam(name = "userId",value = "用户id")@RequestParam("userId") Long userId) {

        return WrapMapper.ok(consoleDictService.getDictListByUserId(userId));
    }

    /**
     * 根据字典库id删除字典库及其所属字典项
     *
     * @param dictId HTTP请求参数
     *
     * @return 返回
     */
    @PostMapping(value = "/deleteDictByDictId/{dictId}")
    @ApiOperation(httpMethod = "POST",value = "根据字典库id删除字典库及其所属字典项")
    public Wrapper<ConsoleDict> deleteDictByDictId(@PathVariable Long dictId) {
        LoginAuthDto loginAuthDto = getLoginAuthDto();
        return WrapMapper.ok(consoleDictService.deleteDictByDictId(dictId,loginAuthDto));
    }

}
