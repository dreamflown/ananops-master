/*
 * Copyright (c) 2019. ananops.com All Rights Reserved.
 * 项目名称：ananops平台
 * 类名称：OpcAttachmentFeignClient.java
 * 创建人：ananops
 * 平台官网: http://ananops.com
 */

package com.ananops.provider.web.rpc;

import com.ananops.base.exception.BusinessException;
import com.ananops.core.support.BaseController;
import com.ananops.provider.exceptions.OpcBizException;
import com.ananops.provider.model.domain.OptAttachment;
import com.ananops.provider.model.dto.attachment.OptAttachmentUpdateReqDto;
import com.ananops.provider.model.dto.oss.*;
import com.ananops.provider.service.OpcAttachmentService;
import com.ananops.provider.service.OpcOssFeignApi;
import com.ananops.wrapper.WrapMapper;
import com.ananops.wrapper.Wrapper;
import com.qiniu.common.QiniuException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * The class Opc attachment feign client.
 *
 * @author ananops.com@gmail.com
 */
@RestController
@Api(value = "API - OpcAttachmentFeignClient", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class OpcAttachmentFeignClient extends BaseController implements OpcOssFeignApi {

	@Resource
	private OpcAttachmentService opcAttachmentService;

	@Override
	@ApiOperation(httpMethod = "POST", value = "上传文件")
	public Wrapper<OptUploadFileRespDto> uploadFile(@RequestBody OptUploadFileReqDto optUploadFileReqDto) throws OpcBizException {
		OptUploadFileRespDto result;
		try {
			logger.info("rpcUploadFile - RPC上传附件信息. optUploadFileReqDto={}", optUploadFileReqDto);
			result = opcAttachmentService.rpcUploadFile(optUploadFileReqDto);
		} catch (BusinessException ex) {
			logger.error("RPC上传附件信息, 出现异常={}", ex.getMessage(), ex);
			return WrapMapper.wrap(ex);
		} catch (Exception e) {
			logger.error("RPC上传附件信息, 出现异常={}", e.getMessage(), e);
			return WrapMapper.error();
		}
		return WrapMapper.wrap(Wrapper.SUCCESS_CODE, "操作成功", result);
	}

	@Override
	@ApiOperation(httpMethod = "POST", value = "获取附件完整路径")
	public Wrapper<String> getFileUrl(@RequestBody OptGetUrlRequest optGetUrlRequest) {
		String result;
		try {
			logger.info("getFileUrl - 获取附件完整路径. optGetUrlRequest={}", optGetUrlRequest);
			result = opcAttachmentService.rpcGetFileUrl(optGetUrlRequest);
		} catch (BusinessException ex) {
			logger.error("RPC获取附件完整路径, 出现异常={}", ex.getMessage(), ex);
			return WrapMapper.wrap(ex);
		} catch (Exception e) {
			logger.error("RPC获取附件完整路径, 出现异常={}", e.getMessage(), e);
			return WrapMapper.error();
		}
		return WrapMapper.ok(result);
	}

	@Override
	@ApiOperation(httpMethod = "POST", value = "更新附件信息")
	public Wrapper<String> updateAttachmentInfo(@RequestBody OptAttachmentUpdateReqDto optAttachmentUpdateReqDto) {
		try {
			logger.info("updateAttachmentInfo - 更新附件信息. optAttachmentUpdateReqDto={}", optAttachmentUpdateReqDto);
			OptAttachment optAttachment = new OptAttachment();
			BeanUtils.copyProperties(optAttachment, optAttachmentUpdateReqDto);
			opcAttachmentService.saveAttachment(optAttachment, optAttachmentUpdateReqDto.getLoginAuthDto());
		} catch (BusinessException ex) {
			logger.error("RPC更新附件信息, 出现异常={}", ex.getMessage(), ex);
			return WrapMapper.wrap(ex);
		} catch (Exception e) {
			logger.error("RPC更新附件信息, 出现异常={}", e.getMessage(), e);
			return WrapMapper.error();
		}
		return WrapMapper.ok();
	}

	@Override
	@ApiOperation(httpMethod = "POST", value = "批量更新附件信息")
	public Wrapper<String> batchUpdateAttachment(@RequestBody OptAttachmentUpdateReqDto optAttachmentUpdateReqDto) {
		try {
			logger.info("batchUpdateAttachment - 批量更新附件信息. optAttachmentUpdateReqDto={}", optAttachmentUpdateReqDto);
			String refNo = optAttachmentUpdateReqDto.getRefNo();
			List<Long> attachmentIds = optAttachmentUpdateReqDto.getAttachmentIds();

			OptAttachment optAttachment = new OptAttachment();
			optAttachment.setRefNo(refNo);
			for (Long attachmentId : attachmentIds) {
				optAttachment.setId(attachmentId);
				opcAttachmentService.saveAttachment(optAttachment, optAttachmentUpdateReqDto.getLoginAuthDto());
			}
		} catch (BusinessException ex) {
			logger.error("RPC更新附件信息, 出现异常={}", ex.getMessage(), ex);
			return WrapMapper.wrap(ex);
		} catch (Exception e) {
			logger.error("RPC更新附件信息, 出现异常={}", e.getMessage(), e);
			return WrapMapper.error();
		}
		return WrapMapper.ok();
	}

	@Override
	@ApiOperation(httpMethod = "POST", value = "基于refNo列表显示所有图片URL")
	public Wrapper<List<ElementImgUrlDto>> listFileUrl(@RequestBody OptBatchGetUrlRequest urlRequest) {
		logger.info("getFileUrl - 批量获取url链接. urlRequest={}", urlRequest);
		List<ElementImgUrlDto> result = opcAttachmentService.listFileUrl(urlRequest);
		return WrapMapper.ok(result);
	}

	@Override
	public Wrapper<OptUploadFileRespDto> handleFileUpload(@RequestPart(value = "file") MultipartFile file) {
		return null;
	}

	@Override
	public void deleteExpireFile() {
		// 1.查询过期的文件
		List<OptAttachment> list = opcAttachmentService.listExpireFile();
		// 2.删除这些文件
		for (final OptAttachment attachment : list) {
			try {
				opcAttachmentService.deleteFile(attachment.getPath() + attachment.getName(), attachment.getBucketName(), attachment.getId());
			} catch (QiniuException e) {
				logger.info("删除文件失败, attachmentId={}", attachment.getId(), e);
			}
		}
	}
}
