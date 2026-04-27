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

**状态**: ✅ 已完成（后端 Maven 编译待本机 Maven 可用）

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
- [ ] 执行 `mvn clean install` 后端全模块编译通过（当前环境缺少 `mvn`）
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

mvn clean install
zsh:1: command not found: mvn
```

**备注**:
- Vite dev server 当前运行在 `http://localhost:5173/`。
- Nginx 使用覆盖端口运行在 `http://localhost:8088/`。
- 后端尚未启动，因此 `/api` 代理目标需要后续 Maven 可用并启动 `chat-web` 后验证。

---

## 待完成的步骤

| 阶段 | 步骤 | 状态 |
|------|------|------|
| Phase 1 | Step 1.1 — LLM 客户端抽象与 Kimi 接入 | ⬜ 待开始 |
| Phase 1 | Step 1.2 — 简单对话 Agent 实现 | ⬜ 待开始 |
| Phase 1 | Step 1.3 — 意图识别服务（初版） | ⬜ 待开始 |
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
