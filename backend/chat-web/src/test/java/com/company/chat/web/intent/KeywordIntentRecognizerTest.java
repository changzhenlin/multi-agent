package com.company.chat.web.intent;

import com.company.chat.api.agent.AgentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordIntentRecognizerTest {

    @Test
    void recognizesPrdExamplesWithDefaultKeywords() {
        KeywordIntentRecognizer recognizer = new KeywordIntentRecognizer(new IntentProperties());

        assertThat(recognizer.recognize("你好")).isEqualTo(AgentType.SIMPLE_CHAT);
        assertThat(recognizer.recognize("请假流程是什么")).isEqualTo(AgentType.RAG);
        assertThat(recognizer.recognize("上个月销售额多少")).isEqualTo(AgentType.SERVICE_SQL);
    }

    @Test
    void customKeywordsOverrideDefaultsFromConfiguration() {
        IntentProperties properties = new IntentProperties();
        properties.setRagKeywords(List.of("知识库"));
        properties.setSqlKeywords(List.of("指标"));
        KeywordIntentRecognizer recognizer = new KeywordIntentRecognizer(properties);

        assertThat(recognizer.recognize("帮我看知识库")).isEqualTo(AgentType.RAG);
        assertThat(recognizer.recognize("本周指标")).isEqualTo(AgentType.SERVICE_SQL);
        assertThat(recognizer.recognize("请假流程是什么")).isEqualTo(AgentType.SIMPLE_CHAT);
    }

    @Test
    void sqlKeywordsWinWhenMessageMatchesMultipleAgentTypes() {
        KeywordIntentRecognizer recognizer = new KeywordIntentRecognizer(new IntentProperties());

        assertThat(recognizer.recognize("统计报销流程数量")).isEqualTo(AgentType.SERVICE_SQL);
    }
}
