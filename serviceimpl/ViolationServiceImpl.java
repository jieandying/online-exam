package com.rabbiter.oes.serviceimpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ViolationRecord;
import com.rabbiter.oes.mapper.ViolationRecordMapper;
import com.rabbiter.oes.service.ViolationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 违规检测服务实现类
 */
@Service
public class ViolationServiceImpl implements ViolationService {

    @Autowired
    private ViolationRecordMapper violationRecordMapper;

    /**
     * 违规类型常量
     */
    public static final String TYPE_COPY = "COPY";           // 复制
    public static final String TYPE_PASTE = "PASTE";         // 粘贴
    public static final String TYPE_CUT = "CUT";             // 剪切
    public static final String TYPE_SWITCH_TAB = "SWITCH_TAB";       // 切换标签页
    public static final String TYPE_SWITCH_WINDOW = "SWITCH_WINDOW"; // 切换窗口
    public static final String TYPE_BLUR = "BLUR";           // 窗口失去焦点
    public static final String TYPE_RIGHT_CLICK = "RIGHT_CLICK";     // 右键点击
    public static final String TYPE_KEYBOARD_SHORTCUT = "KEYBOARD_SHORTCUT"; // 快捷键
    public static final String TYPE_SCREEN_CAPTURE = "SCREEN_CAPTURE";       // 截屏
    public static final String TYPE_DEV_TOOLS = "DEV_TOOLS"; // 打开开发者工具

    @Override
    public int recordViolation(ViolationRecord record) {
        // 如果没有设置时间，自动设置当前时间
        if (record.getViolationTime() == null || record.getViolationTime().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            record.setViolationTime(sdf.format(new Date()));
        }
        return violationRecordMapper.add(record);
    }

    @Override
    public List<ViolationRecord> getViolationsByStudentAndExam(Integer studentId, Integer examCode) {
        return violationRecordMapper.findByStudentAndExam(studentId, examCode);
    }

    @Override
    public List<ViolationRecord> getViolationsByExam(Integer examCode) {
        return violationRecordMapper.findByExamCode(examCode);
    }

    @Override
    public IPage<ViolationRecord> getViolationsByExamPage(Page<ViolationRecord> page, Integer examCode) {
        return violationRecordMapper.findByExamCodePage(page, examCode);
    }

    @Override
    public Map<String, Object> getViolationStats(Integer studentId, Integer examCode) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总违规次数
        int totalCount = violationRecordMapper.countByStudentAndExam(studentId, examCode);
        stats.put("totalCount", totalCount);
        
        // 各类型违规次数
        stats.put("copyCount", violationRecordMapper.countByStudentExamAndType(studentId, examCode, TYPE_COPY));
        stats.put("pasteCount", violationRecordMapper.countByStudentExamAndType(studentId, examCode, TYPE_PASTE));
        stats.put("cutCount", violationRecordMapper.countByStudentExamAndType(studentId, examCode, TYPE_CUT));
        stats.put("switchTabCount", violationRecordMapper.countByStudentExamAndType(studentId, examCode, TYPE_SWITCH_TAB));
        stats.put("switchWindowCount", violationRecordMapper.countByStudentExamAndType(studentId, examCode, TYPE_SWITCH_WINDOW));
        stats.put("blurCount", violationRecordMapper.countByStudentExamAndType(studentId, examCode, TYPE_BLUR));
        stats.put("rightClickCount", violationRecordMapper.countByStudentExamAndType(studentId, examCode, TYPE_RIGHT_CLICK));
        stats.put("keyboardShortcutCount", violationRecordMapper.countByStudentExamAndType(studentId, examCode, TYPE_KEYBOARD_SHORTCUT));
        stats.put("screenCaptureCount", violationRecordMapper.countByStudentExamAndType(studentId, examCode, TYPE_SCREEN_CAPTURE));
        stats.put("devToolsCount", violationRecordMapper.countByStudentExamAndType(studentId, examCode, TYPE_DEV_TOOLS));
        
        // 违规记录列表
        stats.put("records", violationRecordMapper.findByStudentAndExam(studentId, examCode));
        
        return stats;
    }

    @Override
    public boolean isViolationExceeded(Integer studentId, Integer examCode, int threshold) {
        int count = violationRecordMapper.countByStudentAndExam(studentId, examCode);
        return count >= threshold;
    }

    @Override
    public int deleteViolation(Integer id) {
        return violationRecordMapper.deleteById(id);
    }

    @Override
    public int deleteViolationsByExam(Integer examCode) {
        return violationRecordMapper.deleteByExamCode(examCode);
    }
}
