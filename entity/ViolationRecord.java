package com.rabbiter.oes.entity;

/**
 * 考试违规记录实体类
 * 用于记录考试期间的违规行为，如复制、粘贴、切换窗口等
 */
public class ViolationRecord {
    
    private Integer id;
    
    /**
     * 考试编号
     */
    private Integer examCode;
    
    /**
     * 学生ID
     */
    private Integer studentId;
    
    /**
     * 违规类型：COPY(复制), PASTE(粘贴), SWITCH_TAB(切换标签), 
     * SWITCH_WINDOW(切换窗口), BLUR(失去焦点), RIGHT_CLICK(右键点击),
     * KEYBOARD_SHORTCUT(快捷键), SCREEN_CAPTURE(截屏)
     */
    private String violationType;
    
    /**
     * 违规详情描述
     */
    private String description;
    
    /**
     * 违规发生时间
     */
    private String violationTime;
    
    /**
     * 客户端IP地址
     */
    private String clientIp;
    
    /**
     * 浏览器信息
     */
    private String userAgent;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getExamCode() {
        return examCode;
    }

    public void setExamCode(Integer examCode) {
        this.examCode = examCode;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getViolationTime() {
        return violationTime;
    }

    public void setViolationTime(String violationTime) {
        this.violationTime = violationTime;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return "ViolationRecord{" +
                "id=" + id +
                ", examCode=" + examCode +
                ", studentId=" + studentId +
                ", violationType='" + violationType + '\'' +
                ", description='" + description + '\'' +
                ", violationTime='" + violationTime + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
