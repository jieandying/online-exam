package com.rabbiter.oes.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.SystemLog;
import com.rabbiter.oes.serviceimpl.SystemLogServiceImpl;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/systemLog")
public class SystemLogController {

    @Autowired
    private SystemLogServiceImpl systemLogService;

    /**
     * 分页查询所有日志
     */
    @GetMapping("/all/{page}/{size}")
    public ApiResult findAll(@PathVariable Integer page,
                             @PathVariable Integer size) {
        Page<SystemLog> p = new Page<>(page, size);
        return ApiResultHandler.buildApiResult(200, "查询成功", systemLogService.findAll(p));
    }

    /**
     * 搜索日志
     */
    @GetMapping("/search/{page}/{size}")
    public ApiResult search(@PathVariable Integer page,
                            @PathVariable Integer size,
                            @RequestParam(required = false, defaultValue = "") String action,
                            @RequestParam(required = false, defaultValue = "") String operatorName) {
        Page<SystemLog> p = new Page<>(page, size);
        return ApiResultHandler.buildApiResult(200, "查询成功", systemLogService.search(p, action, operatorName));
    }

    /**
     * 新增日志
     */
    @PostMapping
    public ApiResult add(@RequestBody SystemLog systemLog) {
        return ApiResultHandler.success(systemLogService.add(systemLog));
    }

    /**
     * 删除日志
     */
    @DeleteMapping("/{id}")
    public ApiResult delete(@PathVariable Integer id) {
        systemLogService.delete(id);
        return ApiResultHandler.buildApiResult(200, "删除成功", null);
    }

    /**
     * 获取日志总数
     */
    @GetMapping("/count")
    public ApiResult count() {
        return ApiResultHandler.success(systemLogService.count());
    }
}
