# 🎓 Smart Campus EMS — Fixed & Streamlit-Ready

## 🐛 Bugs Fixed

| # | File | Bug | Fix |
|---|------|-----|-----|
| 1 | `AuthController.java` | `Optional.orElseReturn()` — **does not exist** in Java. Caused `SymbolNotFound` compile error. | Replaced with `.orElse(null)` + null check |
| 2 | `Services.java` — `EmailService`, `QrCodeService`, `NotificationService` | Package-private classes cannot be `@Autowired` into controllers in other packages. Caused `NoSuchBeanDefinitionException`. | Made all service classes `public` |
| 3 | `Controllers.java` — All inner controllers | Package-private classes cannot be Spring-proxied for `@PreAuthorize` and `@Autowired` cross-package injection. | Made all controller classes `public` |
| 4 | `AllRepositories.java` / `Repositories.java` | Package-private repository interfaces — Spring Data JPA cannot generate proxy beans for them. Caused `NoSuchBeanDefinitionException` for all repositories. | Made all interfaces `public`. Removed duplicate `Repositories.java` file. |
| 5 | `SecurityConfig.java` — JWT filter | Always set authority `ROLE_USER` regardless of actual user role, so `hasRole('ADMIN')` always failed — admins could not access admin endpoints. | Extracts role claim from JWT and sets `ROLE_<ACTUAL_ROLE>` |
| 6 | `JwtUtils.java` | Missing `getRoleFromToken()` method referenced by the fixed `SecurityConfig`. | Added `getRoleFromToken()` that reads the `role` claim from JWT |

---

## 📁 Project Structure

```
smart-campus-ems/
├── streamlit_app/           # ← Streamlit Frontend (deploy on Streamlit Cloud)
│   ├── app.py               # Main entry point
│   ├── api_utils.py         # HTTP helper for API calls
│   └── pages/
│       ├── login.py
│       ├── register.py      # With OTP verification
│       ├── forgot_password.py
│       ├── dashboard.py
│       ├── events.py        # Browse, register, create events
│       ├── my_events.py     # QR code ticket viewer
│       ├── venues.py        # Map + venue cards
│       ├── announcements.py
│       └── admin.py         # Admin panel
├── backend/                 # Spring Boot REST API (deploy on Railway/Render)
│   ├── pom.xml
│   └── src/main/java/com/campus/ems/
│       ├── SmartCampusEmsApplication.java
│       ├── model/
│       ├── repository/AllRepositories.java  # All repos (public)
│       ├── controller/
│       │   ├── AuthController.java          # Fixed
│       │   ├── Controllers.java             # Fixed (public classes)
│       │   └── EventController.java
│       ├── service/Services.java            # Fixed (public classes)
│       ├── security/JwtUtils.java           # Fixed (getRoleFromToken added)
│       └── config/SecurityConfig.java       # Fixed (role-aware JWT filter)
├── database/schema.sql
├── requirements.txt         # Streamlit dependencies
├── Procfile                 # For Railway/Render backend
└── .streamlit/config.toml   # Streamlit theme & server config
```

---

## 🚀 Deployment Guide

### Option A — Streamlit Cloud (Frontend)

1. Push this repo to GitHub
2. Go to [share.streamlit.io](https://share.streamlit.io)
3. Select your repo, set **Main file path** to `streamlit_app/app.py`
4. Set **Secrets** (⚙️ → Secrets):
   ```toml
   # No secrets needed for frontend — it reads api_base from session_state
   ```
5. Deploy ✅

> To point your Streamlit app at a deployed backend, edit `app.py` line:
> ```python
> "api_base": "https://your-backend.railway.app/api",
> ```

---

### Option B — Railway (Backend)

1. Connect your GitHub repo on [railway.app](https://railway.app)
2. Add environment variables:
   ```
   DB_URL=jdbc:mysql://<host>:<port>/smart_campus_ems?useSSL=false&serverTimezone=UTC
   DB_USERNAME=your_db_user
   DB_PASSWORD=your_db_password
   JWT_SECRET=YourSuperSecretKeyAtLeast256BitsLong
   MAIL_USERNAME=your@gmail.com
   MAIL_PASSWORD=your_gmail_app_password
   CORS_ORIGINS=https://your-streamlit-app.streamlit.app
   ```
3. Set **Start Command**: `java -jar backend/target/smart-campus-ems-1.0.0.jar`
4. Or build first: `cd backend && mvn clean package -DskipTests`

---

### Option C — Local Development

#### Backend
```bash
cd backend
# Edit src/main/resources/application.properties with your MySQL credentials
mvn clean package -DskipTests
java -jar target/smart-campus-ems-1.0.0.jar
# Runs on http://localhost:8080/api
```

#### Frontend (Streamlit)
```bash
pip install -r requirements.txt
cd streamlit_app
streamlit run app.py
# Runs on http://localhost:8501
```

---

## 🔑 Default Credentials

| Role    | Email                   | Password     |
|---------|-------------------------|--------------|
| Admin   | admin@campus.edu        | password123  |
| Faculty | faculty@campus.edu      | password123  |
| Student | student@campus.edu      | password123  |

*(These are seeded in `database/schema.sql`)*

---

## 🛠️ Tech Stack

| Layer     | Technology                          |
|-----------|-------------------------------------|
| Frontend  | Streamlit (Python)                  |
| Backend   | Spring Boot 3.2, Spring Security    |
| Auth      | JWT (jjwt 0.11.5)                   |
| Database  | MySQL 8.x                           |
| Email     | JavaMailSender (Gmail SMTP)         |
| QR Code   | ZXing (Google)                      |
| Maps      | Streamlit st.map (built-in)         |
| Build     | Maven                               |
