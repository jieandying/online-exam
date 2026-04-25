package com.rabbiter.oes.entity;

//讯飞星火配置实体类
public class SparkConfig {
    private String appId;
    private String apiSecret;
    private String apiKey;
    private String hostUrl; // WebSocket地址
    private String domain; // 模型版本域名

    // 默认使用Spark4.0 Ultra
    public SparkConfig() {
        this.hostUrl = "wss://spark-api.xf-yun.com/v4.0/chat";
        this.domain = "4.0Ultra";
    }

    public SparkConfig(String appId, String apiSecret, String apiKey) {
        this.appId = appId;
        this.apiSecret = apiSecret;
        this.apiKey = apiKey;
        this.hostUrl = "wss://spark-api.xf-yun.com/v4.0/chat";
        this.domain = "4.0Ultra";
    }
    
    // 完整配置构造函数
    public SparkConfig(String appId, String apiSecret, String apiKey, String hostUrl, String domain) {
        this.appId = appId;
        this.apiSecret = apiSecret;
        this.apiKey = apiKey;
        this.hostUrl = hostUrl;
        this.domain = domain;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String toString() {
        return "SparkConfig{" +
                "appId='" + appId + '\'' +
                ", apiSecret='" + apiSecret + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", hostUrl='" + hostUrl + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }
}
