package com.rabbiter.oes.serviceimpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Announcement;
import com.rabbiter.oes.mapper.AnnouncementMapper;
import com.rabbiter.oes.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public IPage<Announcement> findByRole(Page page, String role) {
        return announcementMapper.findByRole(page, role);
    }

    @Override
    public IPage<Announcement> findAll(Page page) {
        return announcementMapper.findAll(page);
    }

    @Override
    public Announcement findById(Integer id) {
        return announcementMapper.findById(id);
    }

    @Override
    public int add(Announcement announcement) {
        return announcementMapper.add(announcement);
    }

    @Override
    public int update(Announcement announcement) {
        return announcementMapper.update(announcement);
    }

    @Override
    public int delete(Integer id) {
        return announcementMapper.delete(id);
    }

    @Override
    public int countPublished() {
        return announcementMapper.countPublished();
    }
}
