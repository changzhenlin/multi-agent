package com.company.chat.web.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceContractTest {

    @Test
    void persistenceResourcesDefineRequiredTablesKeysAndEnvironmentConfiguration() throws IOException {
        String ddl = readResource("/db/migration/V1__create_chat_schema.sql");
        assertThat(ddl).contains("CREATE TABLE chat_session");
        assertThat(ddl).contains("CREATE TABLE chat_message");
        assertThat(ddl).contains("CREATE TABLE kb_document");
        assertThat(ddl).contains("CONSTRAINT fk_chat_message_session");

        String redisDoc = readResource("/redis/redis-data-structures.md");
        assertThat(redisDoc).contains("chat:context:{sessionId}");
        assertThat(redisDoc).contains("ratelimit:user:{userId}");
        assertThat(redisDoc).contains("rag:doc:{docId}:chunk:{chunkIdx}");
        assertThat(redisDoc).contains("sql:schema:metadata");

        String redisIndex = readResource("/redis/create-rag-index.redis");
        assertThat(redisIndex).contains("FT.CREATE rag_index");
        assertThat(redisIndex).contains("DIM 1024");

        String application = readResource("/application.yml");
        assertThat(application).contains("${MYSQL_HOST:localhost}");
        assertThat(application).contains("${MYSQL_DATABASE:multi_agent_chat}");
        assertThat(application).contains("${REDIS_HOST:localhost}");
    }

    private String readResource(String path) throws IOException {
        try (var inputStream = getClass().getResourceAsStream(path)) {
            assertThat(inputStream).as("resource %s", path).isNotNull();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
