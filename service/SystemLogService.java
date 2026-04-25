package com.rabbiter.oes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.SystemLog;

public interface SystemLogService {
    IPage<SystemLog> findAll(Page page);
    IPage<SystemLog> search(Page page, String action, String operatorName);
    int add(SystemLog systemLog);
    int delete(Integer id);
    int count();
}
