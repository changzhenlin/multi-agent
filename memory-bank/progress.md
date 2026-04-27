# 项目进度跟踪

**版本**: v1.0  
**日期**: 2026-04-27  
**依据**: memory-bank/implementation-plan.md

---

## 已完成的步骤

### Step 0.1 — 定义 Agent 统一接口契约

**状态**: ✅ 已完成

**完成内容**:
- 创建 `backend/pom.xml` 父 POM（Spring Boot 3.2.0 + Java 21）
- 创建 `common` 模块，包含 `ChatException` 运行时异常基类
- 创建 `chat-api` 接口契约模块，定义以下核心接口：
  - `ChatAgent`：所有 Agent 必须实现的统一接口，`Stream<String> chat(ChatContext, String)`
  - `AgentType` 枚举：`SIMPLE_CHAT`、`RAG`、`SERVICE_SQL`
  - `ChatContext` 值对象：`sessionId`、`userId`、`history`
  - `AgentRouter`：根据 `AgentType` 路由到对应 Agent 实例
  - `IntentRecognizer`：识别用户意图，返回 `AgentType`
  - `LlmClient`：LLM 流式对话接口，`Flux<String> streamChat(List<Message>)`
  - `EmbeddingClient`：文本向量化接口，`List<float[]> embed(List<String>)`
  - `Message`：对话消息记录（`role`、`content`）
- 创建 `agent-simple` 模块，含 `SimpleChatAgent` 空实现
- 创建 `agent-rag` 模块，含 `RagAgent` 空实现
- 创建 `agent-sql` 模块，含 `ServiceSqlAgent` 空实现

**验收标准检查**:
- [x] 接口模块 `chat-api` 可独立编译通过
- [x] 三个 Agent Module（`agent-simple`、`agent-rag`、`agent-sql`）已创建，且均声明依赖 `chat-api`
- [x] 每个 Agent Module 内有一个空实现类实现 `ChatAgent` 接口，编译通过
- [x] `mvn clean compile` 全模块 BUILD SUCCESS

**验证结果**:
```
[INFO] Chat Parent ........................................ SUCCESS [  0.915 s]
[INFO] Common ............................................. SUCCESS [  1.425 s]
[INFO] Chat API ........................................... SUCCESS [  0.141 s]
[INFO] Agent Simple ....................................... SUCCESS [  0.048 s]
[INFO] Agent RAG .......................................... SUCCESS [  0.051 s]
[INFO] Agent SQL .......................................... SUCCESS [  0.055 s]
[INFO] BUILD SUCCESS
```

---

### Step 0.2 — 定义前后端 REST API 契约

**状态**: ✅ 已完成

**完成内容**:
- 创建根目录 `openapi.yaml`，定义 OpenAPI 3.0.3 契约，覆盖：
  - `POST /api/chat/sse`
  - `GET /api/sessions`
  - `POST /api/sessions`
  - `DELETE /api/sessions/{id}`
  - `GET /api/sessions/{id}/messages`
- 创建 `backend/chat-web` Maven 模块，并加入父 POM modules
- 创建 `ChatController` 契约存根，暴露 5 个计划端点
- 创建后端 DTO：
  - `ChatMessageRequest`
  - `ChatMessageResponse`
  - `CreateSessionRequest`
  - `SessionSummaryResponse`
- 创建前端契约类型与 API 壳：
  - `frontend/src/types/chat.ts`
  - `frontend/src/api/chat.ts`
- 为 Controller 契约增加 `ChatControllerContractTest`，锁定端点路径、SSE media type 和核心返回类型

**验收标准检查**:
- [x] `openapi.yaml` 文件通过 YAML 语法检查
- [x] 后端基于契约创建 Spring Controller / DTO 存根
- [x] 前端基于契约创建 TypeScript 类型定义和 API 调用函数
- [ ] Maven 测试待本地 Maven 可用后执行

**验证结果**:
```
ruby -e "require 'psych'; Psych.load_file('openapi.yaml'); puts 'openapi yaml syntax ok'"
openapi yaml syntax ok

mvn test -pl chat-web -Dtest=ChatControllerContractTest
zsh:1: command not found: mvn
```

**备注**:
- 当前环境可用 Java 21，但未安装或未暴露 `mvn` 命令，也没有 Maven Wrapper；因此后端编译和测试尚未在本机完成。
- `/api/chat/sse` 契约为 POST，前端使用 `fetch + ReadableStream`，不使用只能发 GET 的原生 `EventSource`。

---

### Step 0.3 — 定义数据库 Schema 与 Redis 数据结构

**状态**: ✅ 已完成

**完成内容**:
- 创建 MySQL DDL：
  - `backend/chat-web/src/main/resources/db/migration/V1__create_chat_schema.sql`
  - 包含 `chat_session`、`chat_message`、`kb_document`
  - `chat_message.session_id` 外键级联删除会话消息
  - `role` 和 `agent_type` 使用 CHECK 约束限制枚举值
- 创建 Redis 数据结构文档：
  - `backend/chat-web/src/main/resources/redis/redis-data-structures.md`
  - 覆盖 `chat:context:{sessionId}`、`ratelimit:user:{userId}`、`rag:doc:{docId}:chunk:{chunkIdx}`、`sql:schema:metadata`
  - 明确类型、TTL、字段和负责模块
- 创建 RediSearch 索引脚本：
  - `backend/chat-web/src/main/resources/redis/create-rag-index.redis`
  - `rag_index` 使用 `VECTOR HNSW`，`DIM 1024`，`DISTANCE_METRIC COSINE`
- 创建环境变量化配置：
  - `backend/chat-web/src/main/resources/application.yml`
  - 支持 `MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DATABASE`、`MYSQL_USER`、`MYSQL_PASSWORD`
  - 支持 `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`、`REDIS_DATABASE`
- `chat-web` 增加 JPA、Redis、Flyway MySQL、MySQL Connector 依赖
- 增加 `PersistenceContractTest`，锁定 DDL、Redis 规范、索引脚本和环境变量配置资源

**验收标准检查**:
- [x] MySQL DDL 脚本已创建
- [x] Redis 数据结构文档列出所有 Key 模式、数据类型、TTL 策略
- [x] 数据库连接配置支持环境变量注入
- [x] 在本地 MySQL 中执行 DDL，验证表创建成功
- [x] 使用 Redis CLI 创建 `rag_index` 并验证 `FT.INFO`

**验证结果**:
```
xmllint --noout backend/pom.xml backend/chat-web/pom.xml
# 通过，无输出

ruby -e "require 'psych'; Psych.load_file('backend/chat-web/src/main/resources/application.yml'); puts 'application yaml syntax ok'"
application yaml syntax ok

mvn test -pl chat-web -Dtest=PersistenceContractTest
zsh:1: command not found: mvn

MYSQL_HOST_PORT=13306 REDIS_HOST_PORT=16379 NGINX_HOST_PORT=8088 docker compose up -d
# MySQL / Redis healthy, Nginx started

docker exec multi-agent-mysql mysql -uchat_user -pchat_password multi_agent_chat -e "SHOW TABLES; SHOW CREATE TABLE chat_message\\G"
# chat_message / chat_session / kb_document 均存在
# chat_message 包含 fk_chat_message_session、chk_chat_message_role、chk_chat_message_agent_type

docker exec -i multi-agent-redis redis-cli < backend/chat-web/src/main/resources/redis/create-rag-index.redis
OK

docker exec multi-agent-redis redis-cli FT.INFO rag_index
# rag_index 存在，embedding 维度 1024，distance_metric COSINE
```

**备注**:
- 当前环境仍缺少 Maven，因此后端 JUnit 测试尚未执行。

---

### Step 0.4 — 项目脚手架搭建

**状态**: ✅ 已完成

**完成内容**:
- 创建根目录 `docker-compose.yml`
  - MySQL 8.0
  - Redis Stack Server 7.2（包含 RediSearch）
  - Nginx 1.25
  - 支持 `MYSQL_HOST_PORT`、`REDIS_HOST_PORT`、`NGINX_HOST_PORT` 覆盖宿主机端口
- 创建 `nginx/default.conf`
  - `/api/` 代理到 `host.docker.internal:8080`
  - `/` 代理到 Vite dev server `host.docker.internal:5173`
  - 关闭 `/api/` proxy buffering，适配 SSE
- 创建前端 Vite + Vue 3 + TypeScript 骨架
  - `frontend/package.json`
  - `frontend/index.html`
  - `frontend/vite.config.ts`
  - `frontend/tsconfig.json`
  - `frontend/eslint.config.js`
  - `frontend/.prettierrc`
  - `frontend/src/main.ts`
  - `frontend/src/App.vue`
  - `frontend/src/styles/main.css`
- 创建 `README.md`，记录本地启动、端口覆盖和验证命令

**验收标准检查**:
- [x] 执行 `docker compose up -d` 后，MySQL、Redis、Nginx 均健康运行（宿主机 3306 被占用，使用覆盖端口验证）
- [x] 执行 `mvn clean install` 后端全模块编译通过（使用本机 Maven 3.9.14 分发路径）
- [x] 执行 `npm install && npm run dev` 前端正常启动
- [x] Nginx 配置支持 `/api` 反向代理到后端，`/` 代理到前端 dev server

**验证结果**:
```
docker compose config
# 通过

npm install
# added 243 packages

npm run build
# vue-tsc --noEmit && vite build 通过
# Vite 提示 Element Plus 相关 chunk 超过 500 kB，这是体积提示，不是构建失败

npm run lint
# 通过

MYSQL_HOST_PORT=13306 REDIS_HOST_PORT=16379 NGINX_HOST_PORT=8088 docker compose up -d
# MySQL healthy, Redis healthy, Nginx started

curl -I http://localhost:5173/
# HTTP/1.1 200 OK

curl -I http://localhost:8088/
# HTTP/1.1 200 OK

/Users/tyler/.m2/wrapper/dists/apache-maven-3.9.14-bin/1cb7fhup6b5n3bed6kckbrnspv/apache-maven-3.9.14/bin/mvn clean install
# BUILD SUCCESS
```

**备注**:
- Vite dev server 当前运行在 `http://localhost:5173/`。
- Nginx 使用覆盖端口运行在 `http://localhost:8088/`。
- 后端尚未启动，因此 `/api` 代理目标需要后续 Maven 可用并启动 `chat-web` 后验证。

---

### Step 1.1 — LLM 客户端抽象与 Kimi 接入

**状态**: ✅ 已完成

**完成内容**:
- 在 `chat-web` 中实现 LLM 运行时集成：
  - `KimiLlmClient`：基于 Spring `WebClient` 调用 Moonshot `/chat/completions`
  - `MockLlmClient`：本地默认 mock 实现，方便无 API Key 启动
  - `LlmClientFactory`：按 `llm.provider` 创建客户端
  - `LlmConfiguration`：注册 `LlmClient` Bean
  - `LlmProperties`：绑定 `llm.*` 配置并提供 API Key 脱敏
  - `LlmProvider`：`MOCK`、`KIMI`、`OPENAI`、`LOCAL`
- `application.yml` 新增环境变量化 LLM 配置：
  - `LLM_PROVIDER`
  - `LLM_BASE_URL`
  - `LLM_API_KEY`
  - `LLM_MODEL`
- `chat-web` 新增 Spring Boot 启动类 `ChatWebApplication`
- `chat-web` 新增依赖：
  - `spring-boot-starter-webflux`
  - `mockwebserver`（测试）
  - `netty-resolver-dns-native-macos`（macOS 测试输出清理）
- 增加测试：
  - `KimiLlmClientTest`：使用 `MockWebServer` 验证 Moonshot SSE 解析、请求路径、Authorization header、请求体
  - `LlmClientFactoryTest`：验证 provider 切换和 API Key 脱敏

**验收标准检查**:
- [x] 调用 `KimiLlmClient.streamChat()` 可收到流式字符串输出（MockWebServer SSE）
- [x] 切换 `llm.provider` 配置项后，可路由到不同实现（Mock / Kimi）
- [x] API Key 不出现在日志中，并提供脱敏输出方法
- [ ] 真实 Kimi API Key 集成测试待提供 `LLM_API_KEY` 后执行

**验证结果**:
```
/Users/tyler/.m2/wrapper/dists/apache-maven-3.9.14-bin/1cb7fhup6b5n3bed6kckbrnspv/apache-maven-3.9.14/bin/mvn test -pl chat-web -am -Dtest='KimiLlmClientTest,LlmClientFactoryTest' -Dsurefire.failIfNoSpecifiedTests=false
# Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS

/Users/tyler/.m2/wrapper/dists/apache-maven-3.9.14-bin/1cb7fhup6b5n3bed6kckbrnspv/apache-maven-3.9.14/bin/mvn test -pl chat-web -am
# Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS

/Users/tyler/.m2/wrapper/dists/apache-maven-3.9.14-bin/1cb7fhup6b5n3bed6kckbrnspv/apache-maven-3.9.14/bin/mvn test
# BUILD SUCCESS

npm run build
# 通过

npm run lint
# 通过
```

**备注**:
- 当前默认 `LLM_PROVIDER=mock`，避免本地开发必须配置真实 API Key。
- `OPENAI` 与 `LOCAL` provider 已预留枚举，目前回退到 `MockLlmClient`，后续实现对应客户端时再切换。

---

### Step 1.2 — 简单对话 Agent 实现

**状态**: ✅ 已完成

**完成内容**:
- 实现 `agent-simple` 模块的 `SimpleChatAgent`
  - 构造器注入 `LlmClient`
  - 支持系统 Prompt 注入
  - 空白系统 Prompt 自动回退到默认企业内部助手 Prompt
  - 按顺序组装 `system`、历史消息、当前用户消息
  - 调用 `LlmClient.streamChat(...)`，将 `Flux<String>` 转为 `Stream<String>` 返回
- 新增 `SimpleChatAgentTest`
  - 验证消息组装顺序
  - 验证流式输出透传
  - 验证默认系统 Prompt 回退
- `agent-simple` 增加测试依赖 `spring-boot-starter-test`

**验收标准检查**:
- [x] `SimpleChatAgent` 实现类编译通过，单元测试覆盖正常流程
- [x] 通过单元测试验证：输入“你好”，输出不为空且为流式数据
- [x] 系统 Prompt 可通过构造器配置，空白时使用默认值

**验证结果**:
```
/Users/tyler/.m2/wrapper/dists/apache-maven-3.9.14-bin/1cb7fhup6b5n3bed6kckbrnspv/apache-maven-3.9.14/bin/mvn test -pl agent-simple -am -Dtest=SimpleChatAgentTest -Dsurefire.failIfNoSpecifiedTests=false
# Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS

/Users/tyler/.m2/wrapper/dists/apache-maven-3.9.14-bin/1cb7fhup6b5n3bed6kckbrnspv/apache-maven-3.9.14/bin/mvn test
# BUILD SUCCESS

npm run build
# 通过

npm run lint
# 通过
```

---

### Step 1.3 — 意图识别服务（初版）

**状态**: ✅ 已完成

**完成内容**:
- 在 `chat-web` 中实现 `KeywordIntentRecognizer`
  - 基于关键词匹配 `SERVICE_SQL`、`RAG`、`SIMPLE_CHAT`
  - SQL 关键词优先于 RAG 关键词
  - 空输入或未命中关键词时默认 `SIMPLE_CHAT`
- 新增 `IntentProperties`
  - `intent.sql-keywords`
  - `intent.rag-keywords`
  - 支持通过 `application.yml` 或外部配置覆盖
- 新增 `IntentConfiguration`
  - 注册 `IntentRecognizer` Bean
- `application.yml` 增加默认关键词：
  - SQL：`多少`、`查询`、`统计`、`销售额`、`用户量`、`订单数`
  - RAG：`流程`、`制度`、`怎么申请`、`如何申请`、`文档`、`手册`、`报销`
- 新增 `KeywordIntentRecognizerTest`
  - 覆盖 PRD 示例
  - 覆盖自定义关键词覆盖默认值
  - 覆盖同时命中 SQL/RAG 时 SQL 优先

**验收标准检查**:
- [x] 输入“你好”，识别为 `SIMPLE_CHAT`
- [x] 输入“请假流程是什么”，识别为 `RAG`
- [x] 输入“上个月销售额多少”，识别为 `SERVICE_SQL`
- [x] 识别逻辑可配置，关键词列表支持外部 YAML 配置

**验证结果**:
```
/Users/tyler/.m2/wrapper/dists/apache-maven-3.9.14-bin/1cb7fhup6b5n3bed6kckbrnspv/apache-maven-3.9.14/bin/mvn test -pl chat-web -am -Dtest=KeywordIntentRecognizerTest -Dsurefire.failIfNoSpecifiedTests=false
# Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS

/Users/tyler/.m2/wrapper/dists/apache-maven-3.9.14-bin/1cb7fhup6b5n3bed6kckbrnspv/apache-maven-3.9.14/bin/mvn test
# Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS

npm run build
# 通过

npm run lint
# 通过
```

---

## 待完成的步骤

| 阶段 | 步骤 | 状态 |
|------|------|------|
| Phase 1 | Step 1.4 — Agent 路由与对话编排 | ⬜ 待开始 |
| Phase 1 | Step 1.5 — 前端对话界面 | ⬜ 待开始 |
| Phase 2 | Step 2.1 — 文档接入与文本分块 | ⬜ 待开始 |
| Phase 2 | Step 2.2 — Embedding 服务与向量入库 | ⬜ 待开始 |
| Phase 2 | Step 2.3 — RAG Agent 实现 | ⬜ 待开始 |
| Phase 2 | Step 2.4 — 意图识别升级（LLM 版） | ⬜ 待开始 |
| Phase 3 | Step 3.1 — 数据库元数据采集 | ⬜ 待开始 |
| Phase 3 | Step 3.2 — SQL 生成与执行引擎 | ⬜ 待开始 |
| Phase 3 | Step 3.3 — Service SQL Agent 实现 | ⬜ 待开始 |
| Phase 4 | Step 4.1 — Agent 路由策略完善 | ⬜ 待开始 |
| Phase 4 | Step 4.2 — 前端富文本与体验优化 | ⬜ 待开始 |
| Phase 4 | Step 4.3 — 端到端集成测试 | ⬜ 待开始 |
| Phase 4 | Step 4.4 — 部署与文档交付 | ⬜ 待开始 |

---

*本文档随开发进度迭代更新。*
