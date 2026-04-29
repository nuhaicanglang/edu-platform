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
├── edu-common/         # 公共模块 — JWT工具、Redis封装、统一响应
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
- 教师上传课程文档，自动分块向量化存入 Elasticsearch
- 学生提问时先向量检索相关知识片段，再由 LLM 生成答案
- 限定课程范围回答，避免模型幻觉

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

### 1. 配置环境变量

```bash
cp .env.example .env
```

编辑 `.env` 填入真实的数据库密码和 LLM API Key：

```env
MYSQL_ROOT_PASSWORD=your_db_password
MYSQL_PWD=your_db_password
REDIS_PWD=your_redis_password

# 选择 LLM 提供商（deepseek / dashscope / openai）
LLM_PROVIDER=deepseek
DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxxxxx
```

### 2. 启动中间件（MySQL / Redis / Nacos / Elasticsearch）

```bash
docker-compose up -d edu-mysql edu-redis edu-nacos edu-elasticsearch
```

等待约 30 秒待中间件就绪。

### 3. 打包后端服务

```bash
mvn clean package -DskipTests
```

### 4. 启动后端服务

双击运行 `start-services.bat`（Windows），或手动启动：

```bash
# 依次启动，每个服务开独立窗口显示日志
java -jar edu-auth/target/edu-auth-1.0.0.jar
java -jar edu-system/target/edu-system-1.0.0.jar
java -jar edu-agent/target/edu-agent-1.0.0.jar
java -jar edu-knowledge/target/edu-knowledge-1.0.0.jar
java -jar edu-gateway/target/edu-gateway-1.0.0.jar
```

### 5. 启动前端

```bash
cd edu-frontend
npm install
npm run dev
```

或双击 `start-frontend.bat`。

前端访问地址：**http://localhost:5173**

---

## 服务端口一览

| 服务 | 端口 | 说明 |
|---|---|---|
| edu-gateway | 9000 | 统一入口，所有请求经此路由 |
| edu-auth | 8081 | 登录 / 注册 / JWT |
| edu-system | 8082 | 课程 / 作业 / AI批改 |
| edu-agent | 8083 | LLM 推理 / 问答 / 学情分析 |
| edu-knowledge | 8084 | 知识库文档管理 / RAG |
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
  └─ LangChain4j → DeepSeek / 通义 / OpenAI
```

---

## 安全说明

- 所有密码和 API Key 通过 `.env` 文件管理，`.env` 已加入 `.gitignore`，不会提交到代码仓库
- 仓库中只有 `.env.example` 占位符文件
- JWT 密钥通过环境变量或系统属性注入

---

## 默认账号

数据库初始化后内置以下测试账号（密码均为 `123456`）：

| 角色 | 用户名 |
|---|---|
| 管理员 | admin |
| 教师 | teacher1 |
| 学生 | student1 |
