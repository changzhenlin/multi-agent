# 分步实施计划

## 企业内部多智能体对话机器人

**版本**: v1.0  
**日期**: 2026-04-27  
**依据**: memory-bank/prd.md + memory-bank/tech-stack.md

---

## 实施原则

1. **接口先行**：任何模块编码前，必须先定义接口契约（Java Interface、REST API、数据库 Schema）。
2. **模块化**：三大 Agent 各自为独立 Maven Module，仅依赖公共接口模块，禁止跨模块直接调用实现类。
3. **端到端验证**：每完成一个 Agent，立即跑通"前端输入 → 后端路由 → Agent 处理 → 流式返回"完整链路，再进入下一个 Agent。
4. **无代码**：本计划仅描述步骤、目标与验收标准，不包含具体实现代码。

---

## Phase 0：接口与骨架（Week 1）

### Step 0.1 —— 定义 Agent 统一接口契约

**目标**：确立所有 Agent 必须遵循的 Java 接口，确保后续三个 Agent 可独立并行开发。

**具体内容**：
- 定义 `ChatAgent` 接口，包含方法签名：`Stream<String> chat(ChatContext context, String userMessage)`
- 定义 `ChatContext` 值对象（会话ID、历史消息列表、用户标识）
- 定义 `AgentType` 枚举（SIMPLE_CHAT、RAG、SERVICE_SQL）
- 定义 `AgentRouter` 接口，输入用户消息，输出目标 AgentType

**验收标准**：
- [ ] 接口模块 `chat-api` 可独立编译通过
- [ ] 三个 Agent Module（`agent-simple`、`agent-rag`、`agent-sql`）已创建，且均声明依赖 `chat-api`
- [ ] 每个 Agent Module 内有一个空实现类实现 `ChatAgent` 接口，编译通过

**测试要点**：
- 验证 Maven 多模块依赖关系正确，`mvn clean compile` 全模块通过
- 验证接口变更时，下游模块编译失败（确保接口约束有效）

---

### Step 0.2 —— 定义前后端 REST API 契约

**目标**：前后端基于同一套 API 契约开发，避免联调时出现接口不匹配。

**具体内容**：
- 定义 `POST /api/chat/sse`：建立 SSE 连接，接收用户消息，流式返回 Agent 输出
- 定义 `GET /api/sessions`：获取当前用户的会话列表
- 定义 `POST /api/sessions`：创建新会话
- 定义 `DELETE /api/sessions/{id}`：清空/删除指定会话
- 定义 `GET /api/sessions/{id}/messages`：获取会话历史消息
- 使用 OpenAPI 3.0 编写 YAML 契约，生成前后端 DTO 存根

**验收标准**：
- [ ] `openapi.yaml` 文件通过 Swagger Editor 校验无错误
- [ ] 后端基于契约生成 Spring 接口存根（`@Controller` 骨架）
- [ ] 前端基于契约生成 TypeScript 类型定义和 API 调用函数

**测试要点**：
- 使用 Swagger UI 预览所有端点，确认路径、方法、参数、响应类型正确
- 前端调用 mock 端点，验证 TypeScript 类型推导无报错

---

### Step 0.3 —— 定义数据库 Schema 与 Redis 数据结构

**目标**：持久化层设计完成，所有表结构和 Redis Key 规范达成共识。

**具体内容**：
- MySQL 表设计：
  - `chat_session`（会话ID、用户ID、标题、创建时间、更新时间）
  - `chat_message`（消息ID、会话ID、角色、内容、Agent类型、创建时间）
- Redis 数据结构规范：
  - 会话上下文缓存：`chat:context:{sessionId}` → Hash（过期时间 24h）
  - 限流计数：`ratelimit:user:{userId}` → String（过期时间 1min）
  - 向量索引：`rag:doc:{docId}` → Redis Hash（含向量字段和元数据）

**验收标准**：
- [ ] MySQL DDL 脚本可执行，无语法错误
- [ ] Redis 数据结构文档列出所有 Key 模式、数据类型、TTL 策略
- [ ] 数据库连接配置（URL、用户名、密码）支持环境变量注入

**测试要点**：
- 在本地 Docker 中执行 DDL，验证表创建成功
- 使用 Redis CLI 模拟写入/读取向量数据，验证数据结构可行

---

### Step 0.4 —— 项目脚手架搭建

**目标**：前后端项目骨架、CI 配置、代码规范全部就绪，团队可基于此并行开发。

**具体内容**：
- 后端：Maven 多模块项目（`chat-api`、`chat-web`、`agent-simple`、`agent-rag`、`agent-sql`、`common`）
- 前端：Vite + Vue 3 + TypeScript 项目，配置 ESLint + Prettier
- 根目录放置 `docker-compose.yml`（MySQL 8.0 + Redis 7 + Nginx）
- 配置 `.gitignore`、README（本地启动命令）

**验收标准**：
- [ ] 执行 `docker-compose up -d` 后，MySQL、Redis、Nginx 均健康运行
- [ ] 执行 `mvn clean install` 后端全模块编译通过
- [ ] 执行 `npm install && npm run dev` 前端正常启动，看到默认页面
- [ ] Nginx 配置支持 `/api` 反向代理到后端，`/` 代理到前端 dev server

**测试要点**：
- 访问 `http://localhost` 可看到前端页面
- 访问 `http://localhost/api/health`（ mock 端点）可路由到后端

---

## Phase 1：核心对话链路 —— 简单对话 Agent（Week 2）

### Step 1.1 —— LLM 客户端抽象与 Kimi 接入

**目标**：封装 LLM 调用，支持流式响应，向上层屏蔽具体模型差异。

**具体内容**：
- 定义 `LlmClient` 接口，方法：`Flux<String> streamChat(List<Message> messages)`
- 实现 `KimiLlmClient`，基于 Spring `WebClient` 调用 Moonshot API
- 支持配置化切换模型（Kimi / OpenAI / 本地模型），配置项：`llm.provider`、`llm.api-key`、`llm.model`
- 实现请求/响应日志记录（脱敏处理 API Key）

**验收标准**：
- [ ] 调用 `KimiLlmClient.streamChat()` 可收到流式字符串输出
- [ ] 切换 `llm.provider` 配置项后，可路由到不同实现（Mock 实现即可验证）
- [ ] API Key 不出现在日志中

**测试要点**：
- 单元测试：使用 `MockWebServer` 模拟 Kimi API 返回 SSE 流，验证解析正确
- 集成测试：使用真实 Kimi API Key，发送"你好"，验证 5 秒内收到首字响应

---

### Step 1.2 —— 简单对话 Agent 实现

**目标**：完成第一个可运行的 Agent，验证 `ChatAgent` 接口设计合理。

**具体内容**：
- 在 `agent-simple` 模块实现 `SimpleChatAgent`，实现 `ChatAgent` 接口
- 逻辑：将用户消息直接透传给 `LlmClient`，流式返回结果
- 支持系统 Prompt 注入（如"你是一个企业内部助手"）

**验收标准**：
- [ ] `SimpleChatAgent` 实现类编译通过，单元测试覆盖正常流程
- [ ] 通过单元测试验证：输入"你好"，输出不为空且为流式数据
- [ ] 系统 Prompt 可配置，变更后重启生效

**测试要点**：
- Mock `LlmClient`，验证 `SimpleChatAgent` 正确组装消息列表并调用下游
- 验证流式输出不阻塞主线程（使用虚拟线程或异步执行）

---

### Step 1.3 —— 意图识别服务（初版）

**目标**：实现初版意图识别，能区分"通用对话"和"其他"。

**具体内容**：
- 定义 `IntentRecognizer` 接口，方法：`AgentType recognize(String userMessage)`
- 实现 `KeywordIntentRecognizer`（初版）：基于关键词规则匹配
  - 包含"多少"、"查询"、"统计"、"销售额"等词 → SERVICE_SQL
  - 包含"流程"、"制度"、"怎么申请"、"文档"等词 → RAG
  - 其他 → SIMPLE_CHAT
- 预留 `LlmIntentRecognizer` 扩展点（后续用 LLM 替代规则）

**验收标准**：
- [ ] 输入"你好"，识别为 SIMPLE_CHAT
- [ ] 输入"请假流程是什么"，识别为 RAG
- [ ] 输入"上个月销售额多少"，识别为 SERVICE_SQL
- [ ] 识别逻辑可配置（关键词列表支持外部 YAML 配置）

**测试要点**：
- 单元测试覆盖所有 PRD 中的意图识别规则示例
- 测试模糊输入（如"帮我查一下"），验证有默认兜底策略

---

### Step 1.4 —— Agent 路由与对话编排

**目标**：将意图识别、Agent 调用、会话管理串联成完整对话流程。

**具体内容**：
- 实现 `AgentRouter`，根据 `IntentRecognizer` 结果选择对应 `ChatAgent` 实现
- 实现 `ChatOrchestrator`：
  1. 接收用户消息
  2. 查询会话上下文（Redis 缓存）
  3. 调用 `IntentRecognizer`
  4. 调用对应 `ChatAgent`
  5. 流式返回结果给前端
  6. 异步保存消息到 MySQL
- 实现 `ChatController`，暴露 `POST /api/chat/sse` 端点

**验收标准**：
- [ ] 前端发送消息，后端通过 SSE 流式返回，前端逐字显示
- [ ] 每次对话后，MySQL `chat_message` 表新增两条记录（用户消息 + Agent 回复）
- [ ] 刷新页面后，调用 `GET /api/sessions/{id}/messages` 可看到历史消息

**测试要点**：
- 集成测试：完整链路测试（前端 Mock → Controller → Orchestrator → Router → SimpleChatAgent → Mock LLM → 流式返回）
- 验证并发场景：两个用户同时对话，消息不串流

---

### Step 1.5 —— 前端对话界面

**目标**：用户可在 Web 页面与机器人对话，看到流式打字机效果。

**具体内容**：
- 实现聊天主界面：消息列表（用户右对齐、机器人左对齐）、输入框、发送按钮
- 使用 `fetch + ReadableStream` 接收 POST SSE，实现流式渲染（逐字显示）
- 实现会话侧边栏：展示历史会话列表，点击切换会话
- 实现"新建会话"、"清空会话"按钮
- 集成 `marked` 渲染 Markdown（为后续 RAG Agent 的富文本做准备）

**验收标准**：
- [ ] 输入文字点击发送，页面立即显示用户消息，机器人消息区域开始逐字显示
- [ ] 支持 Enter 发送、Shift+Enter 换行
- [ ] 切换会话后，历史消息正确加载
- [ ] 新建会话后，侧边栏新增一项，内容区清空

**测试要点**：
- E2E 测试：使用 Playwright / Cypress 模拟用户发送消息，验证流式输出在 10 秒内完成
- 测试快速切换会话，验证无消息错乱或内存泄漏

---

## Phase 2：RAG Agent（Week 3）

### Step 2.1 —— 文档接入与文本分块

**目标**：支持将企业文档导入系统，拆分为适合向量检索的文本块。

**具体内容**：
- 定义 `DocumentLoader` 接口，支持多种格式（Markdown、TXT、PDF 文本层）
- 实现 `ChunkingService`，将长文档按固定长度（如 512 字）+ 重叠窗口（如 128 字）分块
- 每个文本块保留元数据：源文档ID、标题、分段位置、原文链接
- 提供管理端 API：`POST /api/admin/documents`（上传文档并触发分块）

**验收标准**：
- [ ] 上传一个 5000 字的 Markdown 文件，生成 10~12 个文本块
- [ ] 每个文本块包含完整的元数据，可追溯回源文档
- [ ] 分块边界不截断句子（优先在段落或句号处切割）

**测试要点**：
- 单元测试：输入不同长度文档，验证分块数量和重叠窗口正确
- 测试中文分块，验证不会在标点符号中间截断

---

### Step 2.2 —— Embedding 服务与向量入库

**目标**：将文本块转化为向量，存入 Redis Vector。

**具体内容**：
- 定义 `EmbeddingClient` 接口，方法：`List<float[]> embed(List<String> texts)`
- 实现 `KimiEmbeddingClient`，调用 Moonshot Embedding API（1024 维）
- 配置 Redis RediSearch，创建向量索引：`FT.CREATE rag_index ON HASH ... VECTOR HNSW 6 DIM 1024 ...`
- 实现 `VectorStoreService`：文本块 → Embedding → Redis Hash（含向量字段 `embedding` 和元数据字段）

**验收标准**：
- [ ] 调用 `KimiEmbeddingClient.embed(["请假流程"])`，返回 1024 维浮点数组
- [ ] 执行分块后的文档入库，Redis 中可通过 `FT.INFO rag_index` 看到文档数量增长
- [ ] 向量索引支持 HNSW 近似检索，单次查询耗时 < 100ms

**测试要点**：
- 集成测试：真实调用 Kimi Embedding API，验证返回维度正确
- 使用 Redis CLI 执行 `FT.SEARCH rag_index "*=>[KNN 3 @embedding $vec]"`，验证 Top-K 检索返回结果

---

### Step 2.3 —— RAG Agent 实现

**目标**：实现"检索 + 生成"完整链路，回答知识库问题并附带来源。

**具体内容**：
- 在 `agent-rag` 模块实现 `RagAgent`，实现 `ChatAgent` 接口
- 处理流程：
  1. 将用户问题通过 `EmbeddingClient` 转为向量
  2. 通过 `VectorStoreService` 检索 Top-5 相似文本块
  3. 拼接 Prompt：系统指令 + 检索到的上下文（带来源标记）+ 用户问题
  4. 调用 `LlmClient` 流式生成答案
  5. 在答案末尾附加引用来源列表
- 实现 Prompt 模板外部化（YAML 配置），便于调优

**验收标准**：
- [ ] 询问已入库文档中的问题，答案内容与文档一致
- [ ] 答案末尾包含引用来源（如"来源：《员工手册》第3章"）
- [ ] 询问未入库的问题，Agent 诚实回答"未找到相关信息"而非编造

**测试要点**：
- 集成测试：准备 3 篇测试文档入库，询问具体问题，验证答案正确且带引用
- 测试边界：问题与文档无关时，验证答案不 hallucinate
- 性能测试：单次 RAG 查询（Embedding + 向量检索 + LLM 生成）总耗时 < 5s

---

### Step 2.4 —— 意图识别升级（LLM 版）

**目标**：用 LLM 替代关键词规则，提升意图识别准确率。

**具体内容**：
- 实现 `LlmIntentRecognizer`，调用轻量级 LLM（或同一模型）判断意图
- Prompt 设计：告知模型三个 Agent 的能力边界，让其根据用户输入选择最合适的 Agent
- 输出格式要求：严格返回 JSON，如 `{"agentType": "RAG", "reason": "用户查询制度流程"}`
- 配置开关：`intent.recognizer=keyword` 或 `llm`，支持热切换

**验收标准**：
- [ ] 使用 LLM 识别 PRD 中所有示例，准确率 > 90%
- [ ] 模糊问法（如"帮我看看这个数据"）有合理推断或追问策略
- [ ] 配置切换后无需重启即可生效（或使用 `@RefreshScope`）

**测试要点**：
- 准备 50 条测试用例（覆盖三种意图 + 边界case），对比 Keyword 和 LLM 识别准确率
- 测试 LLM 返回非预期格式时的降级处理（fallback 到 SIMPLE_CHAT）

---

## Phase 3：Service SQL Agent（Week 4）

### Step 3.1 —— 数据库元数据采集

**目标**：让 SQL Agent 了解可用表结构，为 NL2SQL 提供上下文。

**具体内容**：
- 实现 `SchemaMetadataService`，自动从 MySQL `information_schema` 读取：
  - 表名、表注释
  - 列名、列类型、列注释
  - 主键、外键关系
- 支持配置化选择哪些表对 SQL Agent 可见（白名单机制）
- 将元数据缓存到 Redis，定时刷新（如每 10 分钟）

**验收标准**：
- [ ] 启动后自动扫描配置的数据库，输出可见表清单到日志
- [ ] 元数据缓存命中时，不重复查询 `information_schema`
- [ ] 新增表后，10 分钟内自动刷新缓存（或提供手动刷新 API）

**测试要点**：
- 单元测试：Mock `information_schema` 返回，验证元数据解析正确
- 测试白名单：配置只暴露 `order` 和 `user` 表，验证其他表不可见

---

### Step 3.2 —— SQL 生成与执行引擎

**目标**：将自然语言转为 SQL，安全执行并返回结果。

**具体内容**：
- 实现 `SqlGenerationService`：
  - Prompt 工程：注入表元数据 + 示例问答对 + 用户问题
  - 调用 LLM 生成 SQL，要求输出严格为可执行 SQL，无多余解释
  - 解析 LLM 输出，提取 SQL 语句
- 实现 `SqlExecutionService`：
  - 使用 `MyBatis-Plus` 或 `JdbcTemplate` 执行 SQL
  - **强制只读**：通过连接池配置或 SQL 解析，拦截 INSERT/UPDATE/DELETE/DROP
  - 结果转为 `List<Map<String, Object>>` 统一格式
- 实现结果格式化：根据数据特征自动选择表格、文本或简单数值展示

**验收标准**：
- [ ] 输入"查询上个月订单总数"，生成正确 SQL 并返回数值结果
- [ ] 输入包含写操作的意图（如"删除订单表"），被安全拦截，返回错误提示
- [ ] 生成的 SQL 语法错误时，捕获异常并返回友好提示（不暴露堆栈）

**测试要点**：
- 准备 10 组 NL + 期望 SQL 测试用例，验证生成准确率 > 80%
- 安全测试：尝试注入 `"; DROP TABLE users; --"`，验证被拦截
- 性能测试：复杂查询（多表 JOIN）执行时间 < 3s

---

### Step 3.3 —— Service SQL Agent 实现

**目标**：完成第三个 Agent，支持数据查询类对话。

**具体内容**：
- 在 `agent-sql` 模块实现 `ServiceSqlAgent`，实现 `ChatAgent` 接口
- 处理流程：
  1. 接收用户消息
  2. 调用 `SchemaMetadataService` 获取表结构上下文
  3. 调用 `SqlGenerationService` 生成 SQL
  4. 调用 `SqlExecutionService` 执行并获取结果
  5. 将结果格式化为自然语言描述（可选：再调用一次 LLM 做结果润色）
  6. 流式返回给前端

**验收标准**：
- [ ] 前端输入数据查询问题，后端返回结构化结果（表格或数值）
- [ ] 结果展示支持 Markdown 表格格式
- [ ] 查询失败时，返回友好错误信息而非技术堆栈

**测试要点**：
- 端到端测试：从用户输入到前端表格渲染的完整链路
- 测试多轮 SQL 对话："上个月销售额" → "那上上个月呢"，验证上下文正确传递

---

## Phase 4：集成优化与上线（Week 5）

### Step 4.1 —— Agent 路由策略完善

**目标**：提升多 Agent 协作体验，处理边界情况。

**具体内容**：
- 实现 Agent 置信度阈值：当意图识别置信度 < 0.6 时，主动询问用户确认
- 实现 Agent 手动切换：前端展示当前使用的 Agent，用户可手动指定其他 Agent
- 实现 Agent 间调用：RAG Agent 回答中涉及数据时，可内部调用 SQL Agent 补充
- 实现兜底策略：任意 Agent 异常时，fallback 到 Simple Chat Agent 并告知用户

**验收标准**：
- [ ] 输入模糊问题（如"帮我查一下"），机器人追问"您想查询知识文档还是业务数据？"
- [ ] 用户点击切换 Agent 后，当前对话使用新 Agent 处理
- [ ] 某个 Agent 服务异常时，对话不中断，自动切换到简单对话模式

**测试要点**：
- 混沌测试：故意让 RAG Agent 抛出异常，验证 fallback 机制生效
- 用户体验测试：3 名非技术人员使用系统，记录是否需要手动切换 Agent

---

### Step 4.2 —— 前端富文本与体验优化

**目标**：提升前端展示效果，支持复杂内容渲染。

**具体内容**：
- 优化 Markdown 渲染：代码块语法高亮、表格自适应、引用块样式
- 实现消息反馈：每条机器人消息支持点赞/点踩
- 实现加载状态：Agent 思考/检索时显示打字动画或进度提示
- 实现错误重试：网络中断时自动重连 SSE，或提供"重新生成"按钮
- 移动端适配：确保 Element Plus 组件在手机浏览器可用

**验收标准**：
- [ ] RAG Agent 返回的 Markdown 表格在手机端可横向滚动查看
- [ ] 代码块显示语言标签和复制按钮
- [ ] 网络断开 5 秒后恢复，SSE 自动重连或提示用户刷新
- [ ] 页面在 Chrome 移动端模拟器中无样式错乱

**测试要点**：
- 使用不同设备（PC、iPad、手机）访问，验证布局适配
- 使用 Chrome DevTools 模拟断网，验证错误处理行为

---

### Step 4.3 —— 端到端集成测试

**目标**：验证三大 Agent 在真实场景下协同工作正常。

**具体内容**：
- 准备 20 条覆盖三类意图的真实测试用例
- 执行全链路测试：上传真实文档 → 询问知识 → 查询数据 → 闲聊 → 切换会话
- 压测：模拟 50 并发用户，验证系统响应时间和稳定性
- 数据清理：测试数据与生产数据隔离方案

**验收标准**：
- [ ] 20 条测试用例全部通过，意图识别准确率 > 85%
- [ ] 50 并发下，P95 响应时间 < 5s，无 500 错误
- [ ] 连续运行 2 小时，内存无持续增长（无内存泄漏）

**测试要点**：
- 使用 JMeter 或 k6 执行并发压测
- 使用 `docker stats` 监控容器资源占用

---

### Step 4.4 —— 部署与文档交付

**目标**：系统可一键部署，文档齐全，交付运维。

**具体内容**：
- 完善 `docker-compose.yml`：生产模式配置（资源限制、日志轮转、健康检查）
- 编写 `DEPLOY.md`：环境要求、启动步骤、配置说明、常见问题
- 编写 `ADMIN.md`：如何上传文档、查看日志、切换模型配置
- 配置 Nginx：Gzip 压缩、静态资源缓存、SSE 超时设置
- 数据初始化：提供默认系统 Prompt 和示例文档

**验收标准**：
- [ ] 新机器上执行 `docker-compose up -d`，10 分钟内系统可访问
- [ ] 部署文档中的每一步均可复制执行，无遗漏依赖
- [ ] 健康检查端点 `GET /actuator/health` 返回 UP

**测试要点**：
- 在干净环境（新虚拟机）中按文档部署，验证步骤完整
- 测试健康检查、MySQL 连接、Redis 连接、Kimi API 连通性

---

## 附录：模块依赖关系图

```
                    ┌─────────────┐
                    │   chat-web  │  ← Spring Boot 主模块，依赖所有 Agent
                    │ (Controller)│
                    └──────┬──────┘
                           │ 依赖
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────┴────┐      ┌─────┴─────┐     ┌─────┴─────┐
   │agent-   │      │ agent-rag │     │agent-sql  │
   │simple   │      │           │     │           │
   └────┬────┘      └─────┬─────┘     └─────┬─────┘
        │                 │                  │
        └─────────────────┼──────────────────┘
                          │ 均依赖
                    ┌─────┴─────┐
                    │  chat-api │  ← 接口契约层（ChatAgent、AgentRouter）
                    └─────┬─────┘
                          │
                    ┌─────┴─────┐
                    │  common   │  ← 工具类、异常、常量
                    └───────────┘
```

**开发顺序约束**：
1. `common` 和 `chat-api` 必须先完成（Step 0.1 ~ 0.4）
2. `agent-simple` 可独立开发，仅依赖 `chat-api`
3. `agent-rag` 和 `agent-sql` 可并行开发，均依赖 `chat-api`
4. `chat-web` 最后整合，依赖所有 Agent 模块

---

*本文档随开发进度迭代更新。*
