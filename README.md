# 工单系统

基于 Vue 3 + Spring Boot 的小型公司工单管理系统

## 技术栈

### 前端
- Vue 3 + TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router

### 后端
- Spring Boot 3.1
- MyBatis-Plus
- MySQL
- JWT 认证

## 项目结构

```
ticket-system/
├── frontend/              # 前端项目
│   ├── src/
│   │   ├── api/          # API 接口
│   │   ├── components/   # 公共组件
│   │   ├── router/       # 路由配置
│   │   ├── stores/       # 状态管理
│   │   ├── types/        # 类型定义
│   │   └── views/        # 页面组件
│   └── package.json
├── backend/               # 后端项目
│   ├── src/main/java/
│   │   └── com/ticket/
│   │       ├── controller/  # 控制器
│   │       ├── entity/      # 实体类
│   │       ├── mapper/      # 数据访问层
│   │       ├── service/     # 业务逻辑层
│   │       └── config/      # 配置类
│   └── pom.xml
└── database/              # 数据库脚本
    └── init.sql
```

## 快速开始

### 1. 数据库初始化

```bash
mysql -u root -p < database/init.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 4. 访问系统

- 前端: http://localhost:5173
- 后端 API: http://localhost:8080/api

## 功能特性

### 核心功能
- ✅ 工单创建、编辑、删除
- ✅ 工单状态流转（待处理 → 处理中 → 已解决 → 已关闭）
- ✅ 优先级管理（P0紧急、P1高、P2普通、P3低）
- ✅ 看板视图
- ✅ 统计报表

### 角色权限
- **普通员工**: 创建工单、查看自己的工单、添加回复
- **技术负责人**: 查看所有工单、修改状态、添加回复、关闭工单
- **管理员**: 全部权限 + 负责人分配、报表查看、工单归档

## 优先级说明

| 级别 | 标签 | 响应时间 | 场景 |
|------|------|----------|------|
| P0 | 紧急 | 30分钟 | 生产环境宕机、数据丢失 |
| P1 | 高 | 2小时 | 核心功能受损，影响多人 |
| P2 | 普通 | 1工作日 | 功能异常，有临时方案 |
| P3 | 低 | 1周 | 体验优化、非紧迫需求 |

## 统计指标

- 首次响应时间
- 平均解决时长
- 月解决率
- 逾期率

## API 接口

### 工单相关

- `GET /api/tickets` - 获取工单列表
- `GET /api/tickets/:id` - 获取工单详情
- `POST /api/tickets` - 创建工单
- `PATCH /api/tickets/:id/status` - 更新工单状态
- `GET /api/tickets/:id/replies` - 获取回复列表
- `POST /api/tickets/:id/replies` - 添加回复
- `GET /api/tickets/kanban` - 获取看板数据

### 认证相关

- `POST /api/auth/login` - 用户登录

## 许可证

MIT
