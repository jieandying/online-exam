package com.rabbiter.oes.entity;

import java.util.List;

//AI出题响应实体类
public class AIQuestionResponse {
    private boolean success; // 是否成功
    private String message; // 响应消息
    private List<Object> questions; // 生成的题目列表(可以是选择题、填空题或判断题)
    private String taskId; // 任务ID(用于异步处理)
    private String status; // 状态: processing(处理中), completed(已完成), failed(失败)

    public AIQuestionResponse() {}

    public AIQuestionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AIQuestionResponse(boolean success, String message, List<Object> questions) {
        this.success = success;
        this.message = message;
        this.questions = questions;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Object> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Object> questions) {
        this.questions = questions;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AIQuestionResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", questions=" + questions +
                ", taskId='" + taskId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
