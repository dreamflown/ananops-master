package com.ananops.provider.service.impl;

import com.ananops.base.dto.LoginAuthDto;
import com.ananops.base.enums.ErrorCodeEnum;
import com.ananops.base.exception.BusinessException;
import com.ananops.core.support.BaseService;
import com.ananops.provider.mapper.ConsoleDictItemMapper;
import com.ananops.provider.mapper.ConsoleDictMapper;
import com.ananops.provider.model.domain.ConsoleDict;
import com.ananops.provider.model.domain.ConsoleDictItem;
import com.ananops.provider.model.dto.AddDictDto;
import com.ananops.provider.model.dto.GetDictDto;
import com.ananops.provider.model.dto.attachment.OptAttachmentUpdateReqDto;
import com.ananops.provider.model.service.UacGroupBindUserFeignApi;
import com.ananops.provider.service.ConsoleDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by huqiaoqian on 2020/3/26
 */
@Slf4j
@Service
public class ConsoleDictServiceImpl extends BaseService<ConsoleDict> implements ConsoleDictService {

    @Resource
    ConsoleDictMapper dictMapper;

    @Resource
    ConsoleDictItemMapper dictItemMapper;

    @Resource
    UacGroupBindUserFeignApi uacGroupBindUserFeignApi;

    @Override
    public AddDictDto saveDict(AddDictDto addDictDto, LoginAuthDto loginAuthDto) {
        ConsoleDict dict = new ConsoleDict();
        copyPropertiesWithIgnoreNullProperties(addDictDto,dict);
        dict.setUpdateInfo(loginAuthDto);


        if(addDictDto.getId()==null){
            logger.info("创建一条字典库记录... CrateDictInfo = {}", addDictDto);

            Long dictId = super.generateId();
            dict.setId(dictId);
            dict.setDr(String.valueOf(0));
           dictMapper.insert(dict);
            BeanUtils.copyProperties(dict,addDictDto);
        } else {
            logger.info("编辑/修改字典库详情... UpdateInfo = {}", addDictDto);

            Long dictId = addDictDto.getId();
            ConsoleDict t =dictMapper.selectByPrimaryKey(dictId);
            if (t == null) {
                throw new BusinessException(ErrorCodeEnum.CONSOLE10110000,dictId);
            }

            // 更新字典库信息
            dictMapper.updateByPrimaryKeySelective(dict);



            // 更新返回结果
            BeanUtils.copyProperties(dict,addDictDto);

            logger.info("编辑/修改字典库成功[OK] Dict = {}", dict);

        }


        return addDictDto;
    }

    @Override
    public List<GetDictDto> getDictListByUserId(Long userId) {
        Long groupId=null;
        if(uacGroupBindUserFeignApi.getCompanyGroupIdByUserId(userId).getResult()!=null){
            groupId=uacGroupBindUserFeignApi.getCompanyGroupIdByUserId(userId).getResult();
        }
        List<GetDictDto> dictDtoList=new ArrayList<>();
        List<ConsoleDict> dictList=dictMapper.selectByDefault();
        if (dictList.size()>0){
            for (ConsoleDict dict:dictList){
                GetDictDto dictDto=new GetDictDto();
                dictDto.setDict(dict);
                dictDtoList.add(dictDto);
            }
        }
        if (groupId!=null){
            List<ConsoleDict> dictList1=dictMapper.selectBygroupId(groupId);
            if(dictList1.size()>0){
                for (ConsoleDict dict:dictList1){
                    GetDictDto dictDto=new GetDictDto();
                    dictDto.setDict(dict);
                    dictDtoList.add(dictDto);
                }
            }
        }
        return dictDtoList;
    }

    @Override
    public ConsoleDict deleteDictByDictId(Long dictId,LoginAuthDto loginAuthDto) {

        ConsoleDict dict=dictMapper.selectByPrimaryKey(dictId);

        if (dict==null){
            throw new BusinessException(ErrorCodeEnum.CONSOLE10110000,dictId);
        }
        dict.setUpdateInfo(loginAuthDto);
        dict.setDr(String.valueOf(1));
        dictMapper.updateByPrimaryKeySelective(dict);
        List<ConsoleDictItem> dictItemList=dictItemMapper.selectBygDictId(dictId);
        if (dictItemList.size()>0){
            for (ConsoleDictItem dictItem:dictItemList){
                dictItem.setUpdateInfo(loginAuthDto);
                dictItem.setDr(String.valueOf(1));
                dictItemMapper.updateByPrimaryKeySelective(dictItem);
            }
        }
        return dict;
    }

    private void copyPropertiesWithIgnoreNullProperties(Object source, Object target){
        String[] ignore = getNullPropertyNames(source);
        BeanUtils.copyProperties(source, target, ignore);
    }

    private String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
