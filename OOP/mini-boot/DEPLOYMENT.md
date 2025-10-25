# 🚀 HƯỚNG DẪN DEPLOY MINI-BOOT SERVER LÊN RENDER

## 📋 Mục lục
1. [Chuẩn bị](#chuẩn-bị)
2. [Test Docker locally](#test-docker-locally)
3. [Deploy lên Render](#deploy-lên-render)
4. [Cấu hình Environment Variables](#cấu-hình-environment-variables)
5. [Verify Deployment](#verify-deployment)
6. [Troubleshooting](#troubleshooting)

---

## 🔧 Chuẩn bị

### Yêu cầu
- ✅ Tài khoản GitHub (đã có repository)
- ✅ Tài khoản Render.com (free tier hoặc paid)
- ✅ Database Supabase đã setup
- ✅ Docker Desktop (để test local)

### Files đã tạo
```
mini-boot/
├── Dockerfile              # Docker build configuration
├── .dockerignore          # Files to ignore in Docker build
├── docker-compose.yml     # Local testing
├── render.yaml           # Render deployment config
└── DEPLOYMENT.md         # This file
```

---

## 🧪 Test Docker locally

### Bước 1: Build Docker image
```bash
cd mini-boot
docker build -t miniboot-server .
```

### Bước 2: Run container locally
```bash
docker run -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres" \
  -e DB_USER="postgres.dwcpuomioxgqznusjewq" \
  -e DB_PASSWORD="your_password_here" \
  miniboot-server
```

### Bước 3: Hoặc sử dụng docker-compose
```bash
# Tạo file .env với password
echo "DB_PASSWORD=your_password_here" > .env

# Run với docker-compose
docker-compose up
```

### Bước 4: Test endpoints
```bash
# Test health endpoint
curl http://localhost:8080/doctors

# Nếu trả về JSON list doctors => SUCCESS! ✅
```

---

## 🌐 Deploy lên Render

### Phương án 1: Deploy qua Render Dashboard (Khuyến nghị)

#### 1. Push code lên GitHub
```bash
git add .
git commit -m "Add Docker configuration for Render deployment"
git push origin main
```

#### 2. Tạo Web Service trên Render
1. Đăng nhập vào [Render.com](https://render.com)
2. Click **"New +"** → **"Web Service"**
3. Connect với GitHub repository của bạn
4. Chọn repository: `BTL_OOP`

#### 3. Cấu hình Build Settings
```
Name:             miniboot-api
Region:           Singapore (hoặc gần bạn nhất)
Branch:           main (hoặc branch bạn đang dùng)
Root Directory:   mini-boot
Environment:      Docker
Dockerfile Path:  Dockerfile
Docker Context:   .
Docker Command:   (leave blank - use CMD from Dockerfile)
```

#### 4. Chọn Instance Type
- **Free**: 512 MB RAM, 0.1 CPU (đủ cho testing)
- **Starter**: $7/month, 512 MB RAM, 0.5 CPU
- **Standard**: $25/month, 2 GB RAM, 1 CPU

#### 5. Advanced Settings (Expand)
```
Auto-Deploy:      Yes
Health Check Path: /doctors
```

---

### Phương án 2: Deploy qua Render Blueprint (Advanced)

#### 1. Push render.yaml lên GitHub
```bash
git add render.yaml
git commit -m "Add Render blueprint"
git push origin main
```

#### 2. Deploy from Blueprint
1. Truy cập: https://render.com/docs/blueprint-spec
2. Click **"New +"** → **"Blueprint"**
3. Connect repository và chọn `render.yaml`
4. Render sẽ tự động đọc config và deploy

---

## 🔐 Cấu hình Environment Variables

### Trong Render Dashboard

1. Vào **Dashboard** → Chọn service **miniboot-api**
2. Click tab **"Environment"**
3. Add các environment variables:

#### Required Variables (BẮT BUỘC):
```
PORT=8080
DB_URL=jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres
DB_USER=postgres.dwcpuomioxgqznusjewq
DB_PASSWORD=<YOUR_SUPABASE_PASSWORD>
```

#### Optional Variables:
```
MAX_BODY_BYTES=1000000
WORKER_THREADS=64
LOG_LEVEL=INFO
JSON_PRETTY=false
```

### ⚠️ QUAN TRỌNG: Secure DB_PASSWORD

**Option 1: Environment Variable (Simple)**
- Add `DB_PASSWORD` như bình thường
- Render tự động encrypt

**Option 2: Secret File (More Secure)**
```bash
# Trong Render Dashboard
1. Click "Environment" tab
2. Scroll to "Secret Files"
3. Click "Add Secret File"
4. File path: /app/.env
5. Contents:
   DB_PASSWORD=your_password_here
```

---

## ✅ Verify Deployment

### 1. Check Deploy Logs
```
Dashboard → miniboot-api → Logs
```

Logs thành công sẽ hiển thị:
```
🚀 Starting mini-boot HTTP Server...
📊 Using PostgreSQL repositories (Supabase)
✅ Repositories initialized
🌐 Server starting on http://localhost:8080
✅ Server is ready!
```

### 2. Test Public URL
Render sẽ cung cấp URL dạng:
```
https://miniboot-api-xxxx.onrender.com
```

Test endpoints:
```bash
# Test doctors endpoint
curl https://miniboot-api-xxxx.onrender.com/doctors

# Test appointments endpoint
curl https://miniboot-api-xxxx.onrender.com/appointments

# Test auth login
curl -X POST https://miniboot-api-xxxx.onrender.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'
```

### 3. Check Health Status
```
Dashboard → miniboot-api → Events
```

Health check nên hiển thị: ✅ Healthy

---

## 🐛 Troubleshooting

### Vấn đề 1: Build Failed
**Triệu chứng**: Build error trong logs
```
Error: Could not find or load main class org.miniboot.app.ServerMain
```

**Giải pháp**:
1. Check `pom.xml` có đúng mainClass:
   ```xml
   <mainClass>org.miniboot.app.ServerMain</mainClass>
   ```
2. Verify package structure trong `src/main/java/`

---

### Vấn đề 2: Database Connection Failed
**Triệu chứng**:
```
java.sql.SQLException: Connection refused
```

**Giải pháp**:
1. ✅ Check DB_URL format:
   ```
   jdbc:postgresql://HOST:PORT/DATABASE
   ```
2. ✅ Verify Supabase connection:
   - Login vào Supabase Dashboard
   - Settings → Database → Connection string
   - Copy **Session Mode** connection (port 5432)
   
3. ✅ Check database trong Supabase:
   - Table Editor → Verify tables exist
   - Doctors, Appointments, Products, etc.

4. ✅ Test connection từ local:
   ```bash
   psql "postgresql://postgres.xxx:PASSWORD@xxx.supabase.com:5432/postgres"
   ```

---

### Vấn đề 3: Port Binding Error
**Triệu chứng**:
```
Address already in use: bind
```

**Giải pháp**:
- Render tự động set PORT environment variable
- Đảm bảo code đọc từ env: `System.getProperty("PORT")`
- Check `ServerMain.java` line 42

---

### Vấn đề 4: Health Check Failing
**Triệu chứng**: Service shows "Unhealthy"

**Giải pháp**:
1. Change health check path trong Render:
   ```
   Health Check Path: /doctors
   ```
   
2. Hoặc tạo dedicated health endpoint:
   ```java
   router.get("/health", ctx -> {
       return Response.ok("Server is running");
   });
   ```

3. Update Dockerfile health check:
   ```dockerfile
   HEALTHCHECK CMD wget --spider http://localhost:8080/health
   ```

---

### Vấn đề 5: Free Tier Sleep
**Triệu chứng**: Service ngủ sau 15 phút không dùng

**Note**: Render free tier tự động sleep
- Request đầu tiên sẽ mất 30-60s để wake up
- Consider upgrade Starter plan ($7/month) để 24/7

**Workaround**: Setup cron job ping server mỗi 10 phút
```bash
# Use cron-job.org hoặc UptimeRobot
curl https://miniboot-api-xxxx.onrender.com/health
```

---

## 📊 Performance Optimization

### 1. Enable Connection Pooling (Đã có HikariCP)
```java
// DatabaseConfig.java already uses HikariCP
// Pool size: 10 connections
```

### 2. Add Caching (Optional)
```java
// Add Redis/Caffeine cache for frequent queries
```

### 3. Optimize Docker Image
```dockerfile
# Use multi-stage build (đã có)
# Alpine Linux (đã có)
# Non-root user (đã có)
```

---

## 🔄 CI/CD Auto Deploy

### Setup Auto Deploy
1. ✅ Enable "Auto-Deploy" trong Render settings
2. Mỗi khi push code lên GitHub:
   ```bash
   git add .
   git commit -m "Update feature"
   git push origin main
   ```
3. Render tự động:
   - Pull code
   - Build Docker image
   - Deploy new version
   - Health check
   - Switch traffic

### Deploy Notifications
- Setup webhook để nhận thông báo deploy
- Slack/Discord integration

---

## 📱 Update Frontend API URL

Sau khi deploy, update URL trong frontend:

### File: `oop_ui/src/main/java/org/example/oop/Utils/ApiConfig.java`

```java
private static final String PROD_BASE_URL = "https://miniboot-api-xxxx.onrender.com";
private static final Environment CURRENT_ENV = Environment.PRODUCTION;
```

---

## 📞 Support

### Documentation
- Render Docs: https://render.com/docs
- Docker Docs: https://docs.docker.com
- Supabase Docs: https://supabase.com/docs

### Contact
- GitHub Issues: [Your repo]/issues
- Team member: Check TEAM_ALLOCATION_5_PEOPLE.md

---

## ✨ Checklist Deploy

- [ ] Build Docker image thành công locally
- [ ] Test container chạy được locally
- [ ] Push code + Dockerfile lên GitHub
- [ ] Tạo Web Service trên Render
- [ ] Add environment variables (DB_URL, DB_USER, DB_PASSWORD)
- [ ] Deploy thành công (check logs)
- [ ] Health check passed
- [ ] Test public URL endpoints
- [ ] Update frontend API_URL
- [ ] Test E2E flow (frontend → backend → database)
- [ ] Setup monitoring/alerts (optional)
- [ ] Document production URL trong README

---

## 🎉 Success!

Nếu tất cả checklist đã ✅, chúc mừng bạn đã deploy thành công!

Production URL:
```
https://miniboot-api-xxxx.onrender.com
```

Enjoy! 🚀
