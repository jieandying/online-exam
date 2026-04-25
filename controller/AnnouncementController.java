package com.rabbiter.oes.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Announcement;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.serviceimpl.AnnouncementServiceImpl;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementServiceImpl announcementService;

    /**
     * 根据角色查询已发布公告（学生端使用）
     */
    @GetMapping("/list/{role}/{page}/{size}")
    public ApiResult findByRole(@PathVariable String role,
                                @PathVariable Integer page,
                                @PathVariable Integer size) {
        Page<Announcement> p = new Page<>(page, size);
        return ApiResultHandler.buildApiResult(200, "查询成功", announcementService.findByRole(p, role));
    }

    /**
     * 查询所有公告（管理端使用）
     */
    @GetMapping("/all/{page}/{size}")
    public ApiResult findAll(@PathVariable Integer page,
                             @PathVariable Integer size) {
        Page<Announcement> p = new Page<>(page, size);
        return ApiResultHandler.buildApiResult(200, "查询成功", announcementService.findAll(p));
    }

    /**
     * 根据ID查询公告详情
     */
    @GetMapping("/{id}")
    public ApiResult findById(@PathVariable Integer id) {
        return ApiResultHandler.success(announcementService.findById(id));
    }

    /**
     * 新增公告
     */
    @PostMapping
    public ApiResult add(@RequestBody Announcement announcement) {
        int res = announcementService.add(announcement);
        if (res > 0) {
            return ApiResultHandler.buildApiResult(200, "发布成功", announcement);
        }
        return ApiResultHandler.buildApiResult(400, "发布失败", null);
    }

    /**
     * 更新公告
     */
    @PutMapping
    public ApiResult update(@RequestBody Announcement announcement) {
        int res = announcementService.update(announcement);
        if (res > 0) {
            return ApiResultHandler.buildApiResult(200, "更新成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "更新失败", null);
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public ApiResult delete(@PathVariable Integer id) {
        announcementService.delete(id);
        return ApiResultHandler.buildApiResult(200, "删除成功", null);
    }

    /**
     * 获取已发布公告数量
     */
    @GetMapping("/count")
    public ApiResult count() {
        return ApiResultHandler.success(announcementService.countPublished());
    }
}
