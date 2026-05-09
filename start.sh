#!/bin/bash

echo "=============================="
echo "  工单系统启动脚本"
echo "=============================="

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: 请先安装 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "错误: 请先安装 Docker Compose"
    exit 1
fi

# 启动服务
echo "正在启动服务..."
docker-compose up -d --build

# 等待服务启动
echo "等待服务启动..."
sleep 10

# 检查服务状态
echo "服务状态:"
docker-compose ps

echo ""
echo "=============================="
echo "  启动完成！"
echo "=============================="
echo "前端访问: http://localhost"
echo "后端API: http://localhost:8080"
echo "数据库: localhost:3306"
echo ""
echo "默认账号: admin / admin123"
echo "=============================="
