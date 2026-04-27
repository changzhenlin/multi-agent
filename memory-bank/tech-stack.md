# 技术栈方案

## 企业内部多智能体对话机器人

**版本**: v1.0  
**日期**: 2026-04-27  
**约束**: Java 21 + Vue3

---

## 1. 前端

| 技术 | 版本建议 | 用途 | 选型理由 |
|------|---------|------|---------|
| **Vue 3** | 3.4+ | 框架 | 组合式 API 逻辑复用强，TypeScript 支持好，生态成熟 |
| **Vite** | 5.x | 构建工具 | 冷启动秒级，HMR 极快，配置简单 |
| **TypeScript** | 5.x | 类型系统 | 大型项目协作必需，IDE 提示友好，减少运行时错误 |
| **Element Plus** | 2.x | UI 组件库 | 企业级设计，组件丰富，文档完善，Vue3 首选 |
| **Pinia** | 2.x | 状态管理 | Vue 官方推荐，TypeScript 支持原生，API 简洁 |
| **Vue Router** | 4.x | 路由 | Vue3 官方配套，支持懒加载和路由守卫 |
| **Axios** | 1.x | HTTP 客户端 | 拦截器、请求取消、SSE 支持完善，生态最成熟 |
| **marked** + **highlight.js** | latest | Markdown 渲染 | Agent 返回的富文本（表格、代码块）渲染 |

**SSE 流式输出**：使用原生 `EventSource` 或 `fetch + ReadableStream` 接收后端流式响应，实现打字机效果。

---

## 2. 后端

| 技术 | 版本建议 | 用途 | 选型理由 |
|------|---------|------|---------|
| **JDK** | 21 (LTS) | 运行时 | 虚拟线程大幅提升并发性能，模式匹配增强代码简洁性，长期支持 |
| **Spring Boot** | 3.2+ | 主框架 | Java 生态事实标准，自动配置，开箱即用，社区最活跃 |
| **Spring MVC** | 6.x | Web 层 | 团队熟悉度高，配合 `SseEmitter` 支持流式输出，调试方便 |
| **Spring Data JPA** | 3.x | ORM | 标准规范，复杂查询用 `@Query`，简单 CRUD 零样板代码 |
| **MyBatis-Plus** | 3.5+ | 辅助 ORM | Service SQL Agent 需灵活拼动态 SQL，MyBatis-Plus 比 JPA 更直观 |
| **Spring Validation** | 内置 | 参数校验 | 声明式校验，减少手写校验代码 |
| **Lombok** | 1.18+ | 代码简化 | 减少 Getter/Setter/Builder 样板代码（团队接受前提下） |
| **MapStruct** | 1.5+ | DTO 转换 | 编译期生成转换代码，性能优于 BeanUtils |

**核心模块划分**：

```
chat-api/                     # 主入口与对话管理
├── chat-controller           # REST API / SSE 端点
├── intent-service            # 意图识别（调用 LLM 判断路由）
├── router-service            # Agent 路由分发
├── agent-simple/             # 简单对话 Agent
├── agent-rag/                # RAG Agent（向量检索 + LLM 生成）
├── agent-sql/                # Service SQL Agent（NL → SQL → 数据）
└── common/                   # 工具类、异常、常量
```

---

## 3. 数据层

| 技术 | 版本建议 | 用途 | 选型理由 |
|------|---------|------|---------|
| **MySQL** | 8.0+ | 关系数据库 | 国内团队最熟悉，运维工具丰富，企业级支持完善 |
| **Redis** | 7.x | 缓存 / 会话 / 向量检索 | 会话存储、缓存限流；启用 RediSearch 模块支持向量相似度检索 |

**数据设计要点**：

- **对话记录**（MySQL）：会话表 + 消息表，TEXT 存储消息内容
- **知识库文档**（MySQL + Redis）：原文存储在 MySQL；文本向量化后存入 Redis Vector，RAG Agent 通过 `FT.SEARCH` 做相似度检索 Top-K
- **向量维度**：Moonshot/Kimi Embedding 为 1024 维，Redis Vector 支持到 32000 维，无压力
- **缓存策略**：LLM 调用结果 TTL 5 分钟，热点知识库答案 TTL 1 小时

---

## 4. AI / LLM 集成

| 技术 | 用途 | 选型理由 |
|------|------|---------|
| **Spring WebClient** | HTTP 客户端 | 非阻塞 IO，原生支持流式响应（Flux），比 RestTemplate 更适配 SSE 场景 |
| **Kimi / OpenAI / 本地模型 API** | 大模型服务 | Kimi（Moonshot）国内访问稳定，长文本支持好；也保留 OpenAI / 本地模型备选 |
| **Prompt 模板引擎**（StringTemplate / 自定义） | Prompt 管理 | 将系统 Prompt、RAG 上下文、SQL 生成模板抽离为可维护的配置文件 |

**Agent 实现策略**：

| Agent | 技术实现 |
|-------|---------|
| 简单对话 Agent | 直接透传用户输入到 LLM API，流式返回 |
| RAG Agent | `Redis Vector` 检索 Top-K 片段 → 拼接 Prompt（系统指令 + 上下文 + 用户问题）→ LLM 生成带引用的答案 |
| Service SQL Agent | Prompt 工程（表结构 + 示例 + 用户问题）→ LLM 生成 SQL → `MyBatis-Plus` 执行 → 结果格式化返回 |

---

## 5. 部署与运维

| 技术 | 用途 | 选型理由 |
|------|------|---------|
| **Docker** | 容器化 | 环境一致性，消除"本地能跑"问题，Java 和 Vue 均可容器化 |
| **Docker Compose** | 单机编排 | 开发环境 + 测试环境一键启动（MySQL + Redis + Backend + Frontend + Nginx） |
| **Nginx** | 反向代理 / 静态资源 | 负载均衡、HTTPS 终结、Vue 路由 History 模式支持、SSE 连接保持 |
| **GitHub Actions / GitLab CI** | CI/CD | 与代码仓库集成，自动化构建镜像、跑单元测试 |

**部署架构（最小可用）**：

```
┌─────────────┐
│   Nginx     │  ← 反向代理、SSL、静态资源
│  (Vue 产物)  │
└──────┬──────┘
       │
┌──────┴──────┐
│  Spring Boot │  ← Java 后端（多实例可横向扩展）
│   (Docker)   │
└──────┬──────┘
       │
┌──────┴──────┐
│    MySQL    │  ← 关系数据（会话、消息、配置）
│  (Docker)   │
└─────────────┘
       │
┌──────┴──────┐
│    Redis    │  ← 缓存 + 会话 + 向量检索
│  (Docker)   │
└─────────────┘
```

---

## 6. 技术栈全景图

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia + Axios |
| 网关 | Nginx |
| 后端 | Spring Boot 3 + Spring MVC + JPA / MyBatis-Plus |
| 缓存 / 向量 | Redis（启用 RediSearch） |
| 关系数据库 | MySQL 8.0 |
| LLM 调用 | WebClient + Kimi / OpenAI / 本地 API |
| 容器化 | Docker + Docker Compose |

---

## 7. 选型核心原则

1. **MySQL + Redis 双栈**：MySQL 负责关系数据（团队最熟悉），Redis 负责缓存和向量检索，不引入额外向量数据库，运维成本最低
2. **团队熟悉度优先**：Spring Boot + Vue 是国内企业最主流的技术栈，招聘、交接、排障成本最低
3. **无鉴权依赖**：内部系统简化鉴权，由企业网关或 Nginx 统一处理，减少 Spring Security 配置复杂度
4. **流式支持原生**：`SseEmitter`（后端）+ `fetch + ReadableStream`（前端，用于 POST SSE），不引入 WebSocket 或 WebFlux，降低复杂度
5. **Agent 扩展性**：每个 Agent 独立 Module，新增 Agent 只需实现统一接口，1 周内可完成接入
6. **部署极简**：Docker Compose 单文件即可拉起全套环境，适合企业内部私有化部署

---

*本文档随 PRD 迭代更新。*
