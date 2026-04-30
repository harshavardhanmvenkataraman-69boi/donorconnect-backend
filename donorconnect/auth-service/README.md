# Auth Service - Frontend Integration Guide

## 🎉 Overview

The React frontend has been successfully integrated into the auth-service. The service now serves both the frontend UI and backend API on **port 8001**.

---

## 🚀 Quick Start

### Prerequisites
Ensure these services are running IN ORDER:
1. ✅ MySQL on `localhost:3306`
2. ✅ Config Server on port `8888`
3. ✅ Eureka Server on port `8761`
4. ✅ **API Gateway on port `7069`** (Routes requests to microservices)
5. ✅ Auth Service on port `8001`

### Start Services in Order

**1. Start Config Server:**
```powershell
cd C:\DC-frontend_works\donorconnect-backend\donorconnect\config-server
mvn spring-boot:run
```

**2. Start Eureka Server:**
```powershell
cd C:\DC-frontend_works\donorconnect-backend\donorconnect\eureka-server
mvn spring-boot:run
```

**3. Start API Gateway:**
```powershell
cd C:\DC-frontend_works\donorconnect-backend\donorconnect\api-gateway
mvn spring-boot:run
```

**4. Start Auth Service:**
```powershell
cd C:\DC-frontend_works\donorconnect-backend\donorconnect\auth-service
.\restart-auth-service.ps1
```
OR
```powershell
mvn spring-boot:run
```

**Wait for:** `Started AuthServiceApplication in X.XXX seconds`

### Access the Application

**For Development (Vite Dev Server with Hot Reload):**
```powershell
cd C:\DC-frontend_works\donorconnect-frontend\donorconnect
npm run dev
```
Then open: http://localhost:5173
- API calls automatically proxy to API Gateway on port 7069
- API Gateway routes to auth-service on port 8001

**For Production (Integrated Frontend):**
Open your browser to: http://localhost:8001
- Serves static React frontend
- But you should access through API Gateway for consistency

**Routes Available:**
- **Setup:** http://localhost:5173/setup (dev) or http://localhost:8001/setup (prod)
- **Login:** http://localhost:5173/login (dev) or http://localhost:8001/login (prod)
- **Forgot Password:** http://localhost:5173/forgot-password
- **Reset Password:** http://localhost:5173/reset-password

---

## 📋 What Was Integrated

### Frontend Pages
1. **Login Page** - User authentication with role-based redirection
2. **Setup Admin Page** - One-time admin account creation
3. **Forgot Password Page** - Password reset request
4. **Reset Password Page** - Reset password with token

### Backend Changes
1. **Static Resources** - Frontend build files in `src/main/resources/static/`
2. **SecurityConfig.java** - Added public access for frontend routes and static assets
3. **SpaWebConfig.java** - Handles SPA routing (redirects non-API routes to index.html)
4. **API Gateway** - Routes `/api/auth/**` → `/api/v1/auth/**` on auth-service

---

## 🔌 API Endpoints

**Frontend calls go through API Gateway (port 7069):**

| Frontend Call | API Gateway | Routes To | Auth Required |
|---------------|-------------|-----------|---------------|
| POST `/api/auth/login` | Port 7069 | auth-service `/api/v1/auth/login` | No |
| POST `/api/auth/setup-admin` | Port 7069 | auth-service `/api/v1/auth/setup-admin` | No |
| POST `/api/auth/forgot-password?email={email}` | Port 7069 | auth-service `/api/v1/auth/forgot-password` | No |
| POST `/api/auth/reset-password` | Port 7069 | auth-service `/api/v1/auth/reset-password` | No |
| POST `/api/auth/register` | Port 7069 | auth-service `/api/v1/auth/register` | Yes (Admin) |
| PUT `/api/auth/change-password` | Port 7069 | auth-service `/api/v1/auth/change-password` | Yes |

**Architecture:**
```
Frontend (Vite/React)
      ↓
API Gateway (port 7069)
  - Rewrites: /api/auth/** → /api/v1/auth/**
  - Handles CORS
  - JWT validation for protected routes
      ↓
Auth Service (port 8001)
  - Handles /api/v1/auth/** endpoints
  - Also serves static frontend files
```

---

## 🧪 Testing

### First-Time Setup
1. Go to: http://localhost:8001/setup
2. Fill in admin details (name, email, password)
3. Click "Create Admin Account"
4. You'll be redirected to login

### Login
1. Go to: http://localhost:8001/login
2. Enter your credentials
3. Click "Sign In"
4. You'll be redirected based on your role

### Password Reset
1. Go to: http://localhost:8001/forgot-password
2. Enter your email
3. Copy the displayed token (dev mode)
4. Go to: http://localhost:8001/reset-password
5. Enter email, token, and new password
6. Submit to reset

---

## 💻 Development

### Using Vite Dev Server (For Hot Reload)

If you want to actively develop the frontend with hot-reload:

1. **The `vite.config.js` is already configured** to proxy to port 8001

2. **Start Vite dev server:**
   ```bash
   cd C:\DC-frontend_works\donorconnect-frontend\donorconnect
   npm run dev
   ```

3. **Access at:** http://localhost:5173 (or whatever port Vite shows)

4. **API calls will proxy** to the auth-service on port 8001

### Updating Frontend Build

If you make changes to the frontend code:

1. **Build the frontend:**
   ```bash
   cd C:\DC-frontend_works\donorconnect-frontend\donorconnect
   npm run build
   ```

2. **Copy to auth-service:**
   ```powershell
   Copy-Item -Path "dist\*" -Destination "..\..\donorconnect-backend\donorconnect\auth-service\src\main\resources\static\" -Recurse -Force
   ```

3. **Restart auth-service:**
   ```powershell
   .\restart-auth-service.ps1
   ```

---

## 📁 File Structure

```
auth-service/
├── restart-auth-service.ps1          ← Script to restart service
├── README.md                          ← This file
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/donorconnect/
    │   │   ├── config/
    │   │   │   ├── SpaWebConfig.java         [NEW] SPA routing
    │   │   │   └── SwaggerConfig.java
    │   │   ├── controller/auth/
    │   │   │   └── AuthController.java       Handles /api/v1/auth/**
    │   │   ├── security/
    │   │   │   └── SecurityConfig.java       [MODIFIED] Public routes
    │   │   └── ...
    │   └── resources/
    │       ├── application.properties
    │       └── static/                        [NEW] Frontend files
    │           ├── index.html
    │           ├── favicon.svg
    │           ├── icons.svg
    │           └── assets/
    │               ├── index-BDTY9Mnt.js
    │               └── index-B7B_DW17.css
    └── test/
```

---

## 🐛 Troubleshooting

### Issue: Blank page at http://localhost:8001
**Solution:**
- Check browser console for errors
- Verify static files exist in `src/main/resources/static/`
- Check auth-service logs for startup errors

### Issue: 403 Forbidden on API calls
**Solution:**
- Restart the auth-service with `.\restart-auth-service.ps1`
- Verify SecurityConfig and AuthAliasController are loaded
- Check that you're calling `/api/auth/**` not `/api/v1/auth/**`

### Issue: 404 on page refresh
**Solution:**
- Verify `SpaWebConfig.java` exists and has no compilation errors
- This should automatically forward all non-API routes to index.html

### Issue: Cannot connect to database
**Solution:**
- Ensure MySQL is running on localhost:3306
- Check config-server's `auth-service.properties` for correct credentials
- Database `auth_db` will be auto-created if it doesn't exist

### Issue: Config Server connection failed
**Solution:**
- Start config-server first: `cd ../config-server && mvn spring-boot:run`
- Wait for "Started ConfigServerApplication" message
- Then start auth-service

---

## ⚙️ Configuration

### API Gateway Routing
- **Gateway Port:** 7069
- **Auth Service Port:** 8001
- **Route Pattern:** `/api/auth/**` → `/api/v1/auth/**`
- **Public Routes:** login, setup-admin, forgot-password, reset-password
- **Protected Routes:** register, change-password (require JWT)

### Security
- **Public endpoints:** `/login`, `/setup`, `/forgot-password`, `/reset-password`, `/assets/**`
- **Protected endpoints:** All others require JWT token
- **JWT stored in:** Browser localStorage
- **Token included in:** All API requests via Axios interceptors

### SPA Routing
- React Router handles client-side navigation
- `SpaWebConfig` forwards non-API routes to `index.html`
- API routes (starting with `/api/`) are not redirected

### CORS
- **Configured in:** API Gateway (port 7069)
- **Allowed Origins:** http://localhost:3000, http://localhost:5173, http://localhost:8001
- **Handles:** All CORS preflight requests
- **Deduplication:** Prevents duplicate CORS headers from microservices

---

## ℹ️ Important Notes

1. **Only Auth Service Integrated** - As requested, only authentication pages are integrated. Other services are NOT affected.

2. **API Gateway Required** - Frontend calls go through API Gateway (port 7069) which routes to auth-service (port 8001). Start the API Gateway before the auth-service.

3. **Production Ready** - Frontend is built with optimizations (minified, tree-shaken).

4. **Development Mode** - Use Vite dev server (`npm run dev`) for hot-reload during development. It proxies to API Gateway.

5. **Integrated Frontend** - Auth-service also serves the static frontend on port 8001, but in production you'd typically access everything through a load balancer or API Gateway.

---

## 🎯 Quick Commands

**Restart auth-service:**
```powershell
.\restart-auth-service.ps1
```

**Check if running:**
```powershell
netstat -ano | Select-String ":8001"
```

**View logs:**
Watch terminal output for "Started AuthServiceApplication"

**Access frontend:**
```
http://localhost:8001
```

**Access Swagger API docs:**
```
http://localhost:8001/swagger-ui.html
```

---

## 🎊 Status

- ✅ Frontend integrated
- ✅ Static resources deployed
- ✅ Security configured
- ✅ SPA routing working
- ✅ API endpoints functional
- ✅ 403 error fixed
- ✅ Production ready

**Integration Date:** April 30, 2026  
**Version:** 1.0.0

---

For issues or questions, check the Spring Boot logs and browser console for detailed error messages.

