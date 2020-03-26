package com.ananops.provider.service;

import com.ananops.provider.model.domain.elasticsearchDo.ImcTaskLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;


/**
 * Created by rongshuai on 2020/3/26 16:51
 */
public interface ElasticsearchService extends CrudRepository<ImcTaskLog,String> {
    /**
     * 根据巡检任务的日志movement查询
     * @param key
     * @param pageable
     * @return
     */
    Page<ImcTaskLog> getByMovement(String key, Pageable pageable);
}
