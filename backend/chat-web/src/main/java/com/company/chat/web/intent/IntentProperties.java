package com.company.chat.web.intent;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "intent")
public class IntentProperties {

    private List<String> sqlKeywords = List.of("多少", "查询", "统计", "销售额", "用户量", "订单数");
    private List<String> ragKeywords = List.of("流程", "制度", "怎么申请", "如何申请", "文档", "手册", "报销");

    public List<String> getSqlKeywords() {
        return sqlKeywords;
    }

    public void setSqlKeywords(List<String> sqlKeywords) {
        this.sqlKeywords = normalize(sqlKeywords);
    }

    public List<String> getRagKeywords() {
        return ragKeywords;
    }

    public void setRagKeywords(List<String> ragKeywords) {
        this.ragKeywords = normalize(ragKeywords);
    }

    private List<String> normalize(List<String> keywords) {
        if (keywords == null) {
            return List.of();
        }
        return keywords.stream()
                .filter(keyword -> keyword != null && !keyword.isBlank())
                .map(String::strip)
                .toList();
    }
}
