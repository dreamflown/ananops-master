package com.ananops.provider.web.frontend;

import com.alibaba.fastjson.JSONObject;
import com.ananops.base.enums.ErrorCodeEnum;
import com.ananops.base.exception.BusinessException;
import com.ananops.core.support.BaseController;
import com.ananops.provider.model.domain.elasticsearchDo.ImcTaskLog;
import com.ananops.provider.model.dto.ElasticPageDto;
import com.ananops.provider.model.dto.TaskLogQueryDto;
import com.ananops.provider.service.ElasticsearchService;
import com.ananops.wrapper.WrapMapper;
import com.ananops.wrapper.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by rongshuai on 2020/3/26 17:20
 */
@RestController
@RequestMapping(value = "/elasticsearch",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(value = "WEB - ImcElasticSearch",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ImcElasticDataController extends BaseController {
    @Autowired
    ElasticsearchService elasticsearchService;

    @PostMapping("/queryImcTaskLog")
    @ApiOperation(httpMethod = "POST",value = "查询巡检任务日志")
    public Wrapper<ElasticPageDto> queryImcTaskLog(@ApiParam(name = "巡检任务日志查询dto",value = "TaskLogQueryDto")@RequestBody TaskLogQueryDto taskLogQueryDto){
        logger.info("taskLogQueryDto={}",taskLogQueryDto);
        try{
            PageRequest request = new PageRequest(taskLogQueryDto.getPageNum(),taskLogQueryDto.getPageSize());
            Page<ImcTaskLog> pages = elasticsearchService.getByMovement(taskLogQueryDto.getContent(),request);
            ElasticPageDto<ImcTaskLog> elasticPageDto = new ElasticPageDto<>();
            elasticPageDto.setTotal(pages.getTotalElements());
            elasticPageDto.setList(pages.getContent());
            return WrapMapper.ok(elasticPageDto);
        }catch (Exception e){
            throw new BusinessException(ErrorCodeEnum.GL9999083);
        }
    }

    @PostMapping("/saveImcTaskLog")
    @ApiOperation(httpMethod = "POST",value = "创建巡检任务日志")
    public Wrapper<ImcTaskLog> saveImcTaskLog(@ApiParam(name = "巡检任务日志",value = "imcTaskLog")@RequestBody ImcTaskLog imcTaskLog){
        try{
            elasticsearchService.save(imcTaskLog);
            return WrapMapper.ok(imcTaskLog);
        }catch (Exception e){
            throw new BusinessException(ErrorCodeEnum.GL9999083);
        }
    }


}
