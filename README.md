# Enterprise Multi-Agent Chat

企业内部多智能体对话机器人。后端使用 Java 21 + Spring Boot 3 多模块架构，前端使用 Vue 3 + TypeScript + Vite。

## Requirements

- Java 21
- Maven 3.9+
- Node.js 20+
- Docker Desktop

## Local Development

Start infrastructure:

```bash
docker compose up -d
```

If a local service already uses one of the default ports, override the published port:

```bash
MYSQL_HOST_PORT=13306 REDIS_HOST_PORT=16379 NGINX_HOST_PORT=8088 docker compose up -d
```

Run backend:

```bash
cd backend
mvn spring-boot:run -pl chat-web
```

If `mvn` is not on `PATH`, this workstation has Maven available at:

```bash
/Users/tyler/.m2/wrapper/dists/apache-maven-3.9.14-bin/1cb7fhup6b5n3bed6kckbrnspv/apache-maven-3.9.14/bin/mvn
```

Run frontend:

```bash
cd frontend
npm install
npm run dev
```

Open:

- Frontend dev server: `http://localhost:5173`
- Nginx proxy: `http://localhost`
- Backend health/API base: `http://localhost:8080`

## Verification

```bash
cd backend
mvn clean test

cd ../frontend
npm run build
```

## Project Layout

- `backend/`: Maven multi-module backend
- `frontend/`: Vue 3 frontend
- `memory-bank/`: product, architecture, implementation, and progress docs
- `openapi.yaml`: REST/SSE API contract
- `docker-compose.yml`: local MySQL, Redis Stack, and Nginx
