# 可嵌入式跨课程 AI Agent 通用架构平台

面向高校的 AI 智能教学平台，将大模型能力嵌入传统教学管理流程，实现 AI 自动批改作业、课程知识库问答、学情分析报告生成等核心功能。

---

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端框架 | Spring Boot 3.2 + Spring Cloud 2023 |
| 服务注册 | Nacos 2.3 |
| 网关鉴权 | Spring Cloud Gateway + JWT |
| ORM | MyBatis-Plus 3.5 |
| 缓存 | Redis 7（三防策略封装） |
| 数据库 | MySQL 8.0 |
| AI 接入 | LangChain4j（DeepSeek / 通义千问 / OpenAI 可切换） |
| 向量检索 | Elasticsearch 8.11（RAG 知识库） |
| 文档处理 | Apache POI（Word）/ PDFBox（PDF） |
| 前端 | React 18 + Vite + TailwindCSS |
| 容器化 | Docker + Docker Compose |

---

## 项目结构

```
edu-platform/
├── edu-gateway/        # 网关服务（9000）— JWT 鉴权、路由转发
├── edu-auth/           # 认证服务（8081）— 登录、注册、JWT 签发
├── edu-system/         # 教学管理服务（8082）— 课程、作业、AI 批改
├── edu-agent/          # AI Agent 服务（8083）— LLM 调用、问答、分析
├── edu-knowledge/      # 知识库服务（8084）— 文档上传、RAG 检索
├── edu-security/       # 轻量安全模块 — JWT 签发与验证
├── edu-common/         # 公共模块 — Redis封装、异常处理、统一响应
├── edu-frontend/       # React 前端
├── sql/                # 数据库建表与初始化数据
├── docker-compose.yml  # 一键启动中间件
├── start-services.bat  # 一键启动后端服务（Windows）
└── start-frontend.bat  # 启动前端开发服务器（Windows）
```

---

## 核心功能

### AI 自动批改作业
学生提交 Word/PDF 作业后，系统异步调用 LLM Agent 完成：
- 文档文本提取（Apache POI / PDFBox）
- 逐段落批注 + 知识点掌握度评估 + 分项打分
- 生成总评与个性化改进建议
- 输出带批注的 Word 报告

### RAG 课程知识库问答
- 教师上传课程文档，事务保存 MySQL 分块后调用 Ollama `bge-m3:latest` 生成 1024 维向量
- Elasticsearch 同时执行课程过滤后的向量检索与 BM25，Java 层通过 RRF 融合排序
- 问答只接受 `question + courseId`，不信任客户端提供的课程上下文
- 回答返回文档、分块和检索模式；前端展示可追踪来源
- 索引状态为 `pending / processing / ready / failed`，失败后可重建

### 学情分析
- 统计学生作业完成率、成绩趋势、知识点薄弱项
- 调用 LLM 生成自然语言学情分析报告

---

## 快速启动

### 前置条件
- JDK 17+
- Maven 3.8+
- Node.js 18+
- Docker Desktop
- [Ollama](https://ollama.com/) 与 `bge-m3:latest` 模型

### 1. 配置环境变量

```bash
cp .env.example .env
```

编辑 `.env` 填入真实的数据库密码和 LLM API Key：

```env
MYSQL_ROOT_PASSWORD=your_db_password
MYSQL_PWD=your_db_password
REDIS_PWD=your_redis_password

# 认证与网关必须使用同一个值，至少 32 个随机字符
JWT_SECRET=replace_with_at_least_32_random_characters

# 宿主机运行知识服务时使用此地址
OLLAMA_BASE_URL=http://127.0.0.1:11434
OLLAMA_EMBEDDING_MODEL=bge-m3:latest
ES_URIS=http://127.0.0.1:9200

# 选择 LLM 提供商（deepseek / dashscope / openai）
LLM_PROVIDER=deepseek
DEEPSEEK_API_KEY=your_deepseek_api_key
```

可以用 PowerShell 生成 JWT 密钥：

```powershell
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
```

### 2. 准备 Ollama

```bash
ollama list
ollama pull bge-m3:latest
```

宿主机直接运行 `edu-knowledge` 时，默认访问 `http://127.0.0.1:11434`；知识服务在 Docker 中运行时，Compose 默认使用 `http://host.docker.internal:11434`。

### 3. 启动中间件（MySQL / Redis / Nacos / Elasticsearch）

```bash
docker compose up -d mysql redis nacos elasticsearch
docker compose ps
```

等待健康检查通过。Elasticsearch 可用以下命令检查：

```bash
curl http://127.0.0.1:9200/_cluster/health
```

已有数据库（不是新建 Docker 数据卷）需先执行一次增量脚本：

```bash
mysql -u root -p edu_platform < sql/migrations/20260711_rag_index_state.sql
```

### 4. 测试并打包后端服务

```bash
mvn test
mvn package -DskipTests
```

### 5. 启动后端服务

双击运行 `start-services.bat`（Windows），或手动启动：

```bash
# 依次启动，每个服务开独立窗口显示日志
java -jar edu-auth/target/edu-auth-1.0.0.jar
java -jar edu-system/target/edu-system-1.0.0.jar
java -jar edu-agent/target/edu-agent-1.0.0.jar
java -jar edu-knowledge/target/edu-knowledge-1.0.0.jar
java -jar edu-gateway/target/edu-gateway-1.0.0.jar
```

### 6. 启动前端

```bash
cd edu-frontend
npm ci
npm run dev
```

或双击 `start-frontend.bat`。

前端访问地址：**http://localhost:3000**

---

## 服务端口一览

| 服务 | 端口 | 说明 |
|---|---|---|
| edu-gateway | 9000 | 统一入口，所有请求经此路由 |
| edu-auth | 8081 | Docker 内网：登录 / 注册 / JWT |
| edu-system | 8082 | Docker 内网：课程 / 作业 / AI批改 |
| edu-agent | 8083 | Docker 内网：LLM 推理 / 问答 / 学情分析 |
| edu-knowledge | 8084 | Docker 内网：知识库文档管理 / RAG |
| Nacos | 8848 | 服务注册中心控制台 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Elasticsearch | 9200 | 向量检索 |

---

## 架构说明

```
前端 React
  │ Authorization: Bearer <JWT>
  ▼
edu-gateway（9000）
  ├─ 验证 JWT，解析 userId/role
  ├─ 写入 X-User-Id / X-User-Role 请求头
  └─ 路由转发至对应微服务
        ├─ /auth/**   → edu-auth:8081
        ├─ /system/** → edu-system:8082
        ├─ /agent/**  → edu-agent:8083
        └─ /knowledge/** → edu-knowledge:8084

edu-system（AI批改）
  └─ @Async → gradingExecutor 线程池
       ├─ 提取文档文本（POI / PDFBox）
       ├─ HTTP → edu-agent（LLM批改）
       ├─ 生成带批注 Word 报告
       └─ 结果写回 MySQL

edu-agent（LLM推理）
  ├─ 调用 edu-knowledge 的受保护检索接口
  └─ LangChain4j → DeepSeek / 通义 / OpenAI

edu-knowledge（RAG）
  ├─ MySQL：文档、分块和索引状态的事实来源
  ├─ Ollama bge-m3：文档与问题向量化
  ├─ Elasticsearch：1024 维向量 + BM25 检索投影
  └─ RRF：双路候选融合，返回 Top 5 来源
```

---

## 安全说明

- 所有密码和 API Key 通过 `.env` 文件管理，`.env` 已加入 `.gitignore`，不会提交到代码仓库
- 仓库中只有 `.env.example` 占位符文件
- JWT 密钥通过环境变量或系统属性注入
- 公共注册固定创建学生账号，教师与管理员不能通过注册参数创建
- Docker 只向宿主机发布网关端口，业务服务不直接暴露
- 网关覆盖客户端伪造的 `X-User-*` 身份头
- 课程、班级、作业、提交和知识库均执行对象级权限校验
- 文件下载路径限制在上传根目录内

---

## 索引重建

管理员可通过网关触发重建；请求必须携带管理员 JWT：

```bash
curl -X POST "http://127.0.0.1:9000/api/knowledge/admin/reindex" \
  -H "Authorization: Bearer <ADMIN_JWT>"
```

可追加 `?courseId=12` 只重建一个课程。响应包含 `taskId`、总数、成功数、失败数和错误摘要；同一 MySQL 分块始终使用相同 Elasticsearch `_id`，重复重建不会生成重复文档。

---

## 测试与验证

```bash
# 全部后端单元与契约测试
mvn test

# 前端组件测试、生产构建和生产依赖审计
cd edu-frontend
npm test
npm run build
npm audit --omit=dev

# Compose 配置检查
docker compose config --quiet

# 真实 Ollama + Elasticsearch RAG 集成测试（需先启动两者）
mvn -pl edu-knowledge -am -Dtest=RagEndToEndIT \
  -Dollama.it.enabled=true -Dsurefire.failIfNoSpecifiedTests=false test
```

真实集成测试覆盖语义改写召回、课程隔离、幂等写入和删除后不可召回；默认 CI 不下载大型 Ollama 模型，因此该测试必须显式启用。

---

## 常见问题

- `JWT_SECRET 未配置`：在 `.env` 中设置至少 32 个 UTF-8 字节的随机密钥，且网关与认证服务必须一致。
- `课程知识检索暂时不可用`：依次检查 `ollama list`、`bge-m3:latest`、`http://127.0.0.1:11434/api/embed` 和 Elasticsearch 集群健康状态。
- 文档显示 `index_status=failed`：查看 `index_error`，恢复 Ollama/Elasticsearch 后使用管理员重建接口重试。
- Docker 中无法连接 Ollama：确认 `OLLAMA_BASE_URL=http://host.docker.internal:11434`，并确认 Ollama 允许来自 Docker Desktop 的连接。
- Redis 不可用：课程/班级查询会回源 MySQL，对话历史暂时丢失，但核心业务不应因缓存故障退出。

---

## 默认账号

数据库初始化后内置以下测试账号（密码均为 `123456`）：

| 角色 | 用户名 |
|---|---|
| 管理员 | admin |
| 教师 | teacher1 |
| 学生 | student1 |
