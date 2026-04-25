package com.rabbiter.oes.serviceimpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.SystemLog;
import com.rabbiter.oes.mapper.SystemLogMapper;
import com.rabbiter.oes.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemLogServiceImpl implements SystemLogService {

    @Autowired
    private SystemLogMapper systemLogMapper;

    @Override
    public IPage<SystemLog> findAll(Page page) {
        return systemLogMapper.findAll(page);
    }

    @Override
    public IPage<SystemLog> search(Page page, String action, String operatorName) {
        return systemLogMapper.search(page, action, operatorName);
    }

    @Override
    public int add(SystemLog systemLog) {
        return systemLogMapper.add(systemLog);
    }

    @Override
    public int delete(Integer id) {
        return systemLogMapper.delete(id);
    }

    @Override
    public int count() {
        return systemLogMapper.count();
    }
}
