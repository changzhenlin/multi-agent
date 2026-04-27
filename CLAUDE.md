# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

企业内部多智能体对话机器人。Web 对话界面，后端根据用户意图将请求路由到三个 Agent 之一（简单对话、RAG 知识检索、SQL 数据查询），通过 SSE 流式返回答案。

技术约束：**Java 21 + Vue 3**。详细需求见 `memory-bank/prd.md`，技术栈见 `memory-bank/tech-stack.md`，实施计划见 `memory-bank/implementation-plan.md`。

---

## 架构总览

后端采用 **Maven 多模块**结构，核心设计原则是**接口先行、模块隔离**。

```
┌─────────────┐
│   chat-web  │  ← Controller 层，聚合所有 Agent，暴露 SSE 端点
└──────┬──────┘
       │
   ┌───┴───┬───────────┬───────────┐
   ↓       ↓           ↓           ↓
┌──────┐ ┌────────┐ ┌────────┐ ┌────────┐
│Intent│ │ agent- │ │ agent- │ │ agent- │
│Router│ │ simple │ │  rag   │ │  sql   │
└──────┘ └────────┘ └────────┘ └────────┘
              │           │           │
              └───────────┴───────────┘
                          │
                    ┌─────┴─────┐
                    │  chat-api │  ← 接口契约层：ChatAgent、AgentRouter、IntentRecognizer、LlmClient
                    └─────┬─────┘
                          │
                    ┌─────┴─────┐
                    │  common   │  ← 工具类、全局异常、常量
                    └───────────┘
```

**关键接口**：
- `ChatAgent`：所有 Agent 必须实现，`Stream<String> chat(ChatContext, String)`
- `AgentRouter`：根据意图选择 Agent 实现
- `IntentRecognizer`：`AgentType recognize(String)`，初版关键词规则，后续升级 LLM 版
- `LlmClient`：屏蔽模型差异，`Flux<String> streamChat(List<Message>)`，支持 Kimi/OpenAI/本地模型切换

**Agent 间禁止直接调用实现类**，全部通过 `chat-api` 中的接口契约交互。

---

## 数据流

```
用户输入 → IntentRecognizer 识别意图 → AgentRouter 选择 Agent
                                              ↓
                    ┌─────────────────────────┼─────────────────────────┐
                    ↓                         ↓                         ↓
              SimpleChatAgent            RagAgent                 ServiceSqlAgent
              （透传 LLM）         （Embedding → Redis Vector      （Schema → NL2SQL
                                     → 检索 Top-K → LLM 生成）        → MySQL 执行）
                    └─────────────────────────┬─────────────────────────┘
                                              ↓
                                       SSE 流式返回前端
```

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia + Axios |
| 后端 | Spring Boot 3 + JDK 21 + Spring MVC + JPA / MyBatis-Plus |
| 缓存/向量 | Redis 7（启用 RediSearch 模块） |
| 关系数据库 | MySQL 8.0 |
| LLM | WebClient + Kimi（Moonshot）/ OpenAI / 本地模型 |
| 部署 | Docker + Docker Compose + Nginx |

**关键约束**：
- 无 Spring Security，鉴权由企业网关或 Nginx 统一处理
- SQL Agent **强制只读**，必须拦截 INSERT/UPDATE/DELETE/DROP
- LLM API Key 必须脱敏，禁止出现在日志和配置明文

---

## 常用命令

### 启动开发环境

```bash
# 一键启动基础设施（MySQL + Redis + Nginx）
docker-compose up -d

# 后端编译
mvn clean install

# 前端开发
npm install
npm run dev
```

### 后端开发

```bash
# 编译全模块
mvn clean compile

# 运行全模块测试
mvn test

# 运行单个模块测试
mvn test -pl agent-rag

# 运行单个测试类
mvn test -pl agent-simple -Dtest=SimpleChatAgentTest

# 打包
mvn clean package -DskipTests

# 启动主服务
mvn spring-boot:run -pl chat-web
```

### 前端开发

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产包
npm run build

# 运行 lint
npm run lint
```

### 数据库

```bash
# MySQL DDL 位于 backend/src/main/resources/db/migration/（或类似路径，按实际项目结构）
# Redis 向量索引创建脚本位于 backend/src/main/resources/redis/
```

### 部署

```bash
# 生产部署
docker-compose -f docker-compose.yml up -d

# 查看日志
docker-compose logs -f chat-web
```

---

## 模块依赖顺序

开发时必须遵守以下顺序：

1. `common` → `chat-api`（接口契约，Phase 0 完成）
2. `agent-simple` / `agent-rag` / `agent-sql` 可并行开发（均只依赖 `chat-api`）
3. `chat-web` 最后整合（依赖所有 Agent 模块）

新增 Agent 时，只需：
1. 创建新 Maven Module，依赖 `chat-api`
2. 实现 `ChatAgent` 接口
3. 在 `chat-web` 中将新 Agent 注册到 `AgentRouter`

---

## 测试策略

| 类型 | 工具 | 覆盖场景 |
|------|------|---------|
| 单元测试 | JUnit 5 + Mockito | Agent 内部逻辑、意图识别规则、Prompt 组装 |
| 集成测试 | `MockWebServer` / Testcontainers | LLM 客户端、MyBatis SQL 执行、Redis 向量检索 |
| E2E | Playwright / Cypress | 前端 SSE 流式渲染、会话切换、多端适配 |
| 压测 | JMeter / k6 | 50 并发 SSE 连接、P95 < 5s |

**关键测试用例必须覆盖**：
- 意图识别：PRD 中所有示例输入（"你好"→SIMPLE_CHAT，"请假流程"→RAG，"销售额"→SQL）
- SQL 安全：注入 `"; DROP TABLE users; --"` 必须被拦截
- 流式并发：两个用户同时对话，消息不串流

---

## 重要文件位置

| 文件 | 说明 |
|------|------|
| `memory-bank/prd.md` | 产品需求文档（三大 Agent 功能定义） |
| `memory-bank/tech-stack.md` | 技术栈方案（版本、选型理由） |
| `memory-bank/implementation-plan.md` | 分步实施计划（15 个 Step，含验收标准） |

---

## 开发约定

- **接口变更**：修改 `chat-api` 中的接口后，必须确保所有实现模块编译通过后再提交
- **Prompt 管理**：所有 Prompt 模板放在 `src/main/resources/prompts/*.yaml`，禁止硬编码在 Java 类中
- **配置注入**：数据库连接、API Key 等通过环境变量注入，`application.yml` 中只保留默认值和占位符
- **虚拟线程**：JDK 21 的虚拟线程特性应用于 IO 密集型操作（LLM 调用、数据库查询）

必须始终先阅读 memory-bank/prd.md 和 memory-bank/architecture.md 和 memory-bank/progress.md 后再写代码。
一次只处理一个模块/功能。
接口先行，模块化开发，禁止创建单体巨文件。
每完成重大功能后，必须更新或者创建(如果不存在)memory-bank/目录下的 architecture.md 和 progress.md, 然后 Git 提交。
遵守奥卡姆剃刀原则、目的主导等。
