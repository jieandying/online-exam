package com.rabbiter.oes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Announcement;

public interface AnnouncementService {
    IPage<Announcement> findByRole(Page page, String role);
    IPage<Announcement> findAll(Page page);
    Announcement findById(Integer id);
    int add(Announcement announcement);
    int update(Announcement announcement);
    int delete(Integer id);
    int countPublished();
}
