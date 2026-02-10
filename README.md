# Group-C - ParkEase Team Repository

**Project**: Smart Parking Management System  
**Team**: Group C (6 Members)  
**Repository**: https://github.com/Guruseelan8610661635/Group-C

---

> [!IMPORTANT]
> **This repository contains EMPTY FOLDERS ONLY**  
> Team members should push their individual files to the appropriate folders as specified in [MEMBER_ASSIGNMENTS.md](MEMBER_ASSIGNMENTS.md).  
> The folder structure is ready - just add your files!

---

## 📁 Project Structure

```
Group-C/
├── Backend/
│   └── src/main/java/com/smartparking/
│       ├── controller/      # REST API Controllers
│       ├── model/           # Entity/Model classes
│       ├── repository/      # JPA Repositories
│       ├── service/         # Business logic services
│       ├── security/        # JWT & Security config
│       ├── dto/             # Data Transfer Objects
│       └── config/          # Configuration classes
│
├── Frontend/
│   └── src/
│       ├── pages/           # Page components
│       │   └── admin/       # Admin pages
│       ├── components/      # Reusable components
│       │   └── admin/       # Admin components
│       ├── services/        # API service files
│       ├── utils/           # Utility functions
│       ├── assets/          # Images, icons, etc.
│       └── public/          # Public assets
│
├── .gitignore
├── README.md
└── MEMBER_ASSIGNMENTS.md
```

---

## 👥 Team Member Assignments

All members contribute to the **same common folders**. 

### Member 1: Authentication & User Management 🔐
- **Backend**: `AuthController`, `UserController`, `User` model, `security/` folder
- **Frontend**: `Login.jsx`, `Register.jsx`, `Profile.jsx`, `authService.js`

### Member 2: Booking System & Slot Management 🅿️
- **Backend**: `BookingController`, `SlotController`, `Booking`/`Slot` models
- **Frontend**: `Booking.jsx`, `BookingHistory.jsx`, `SlotCard.jsx`

### Member 3: Location & Map Features 🗺️
- **Backend**: `LocationController`, `MapController`, `OccupancyStatisticsController`
- **Frontend**: `Map.jsx`, `MapView.jsx`, `LocationCard.jsx`

### Member 4: Payment & Pricing System 💳
- **Backend**: `PaymentController`, `AdminPricingController`, `PromotionController`
- **Frontend**: `Payment.jsx`, `PaymentModal.jsx`, payment services

### Member 5: Admin Dashboard & Management 👨‍💼
- **Backend**: `AdminController`, `SubscriptionController`, `NotificationController`
- **Frontend**: Admin pages, `LocationFormInline.jsx`, `BookingList.jsx`

### Member 6: Reports, Analytics & Audit 📊
- **Backend**: `ReportController`, `AuditController`, audit services
- **Frontend**: `Reports.jsx`, `Analytics.jsx`, `ReportChart.jsx`

---

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/Guruseelan8610661635/Group-C.git
cd Group-C
```

### 2. Add Your Files
Navigate to the appropriate folder and add your files:

**For Backend files:**
```bash
cd Backend/src/main/java/com/smartparking/

# Add your controller
# Add to controller/ folder

# Add your model
# Add to model/ folder

# Add your service
# Add to service/ folder
```

**For Frontend files:**
```bash
cd Frontend/src/

# Add your page
# Add to pages/ or pages/admin/

# Add your component
# Add to components/ or components/admin/

# Add your service
# Add to services/
```

### 3. Commit and Push
```bash
git add .
git commit -m "feat(area): description of changes"
git push origin main
```

---

## 📝 Commit Message Convention

Follow **Conventional Commits**:

```
<type>(<scope>): <description>

Examples:
feat(auth): add JWT refresh token logic
fix(booking): resolve concurrent booking issue
docs(readme): update setup instructions
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

---

## 🤝 Collaboration Guidelines

### File Ownership
- Each member owns specific files (see MEMBER_ASSIGNMENTS.md)
- **DO NOT** modify files owned by other members without coordination
- Discuss shared interfaces (DTOs, models) in team chat

### Before Pushing
- ✅ Test your changes locally
- ✅ Ensure no merge conflicts
- ✅ Follow naming conventions
- ✅ Add comments for complex logic
- ✅ Remove console.logs and debug code

### Communication
- Coordinate on shared models/DTOs
- Announce breaking changes
- Ask questions early
- Help review each other's code

---

## 🔧 Tech Stack

### Backend
- **Framework**: Spring Boot (Java 17)
- **Database**: MySQL
- **Build Tool**: Maven
- **Security**: JWT Authentication

### Frontend
- **Framework**: React 18
- **Build Tool**: Vite
- **Styling**: Tailwind CSS
- **HTTP Client**: Axios

---

## 📋 Development Workflow

1. **Pull latest changes**
   ```bash
   git pull origin main
   ```

2. **Make your changes**
   - Add/modify your assigned files
   - Test locally

3. **Commit with clear message**
   ```bash
   git add .
   git commit -m "feat(booking): add vehicle type validation"
   ```

4. **Push to repository**
   ```bash
   git push origin main
   ```

5. **Coordinate with team**
   - Inform team of major changes
   - Resolve conflicts if they arise

---

## ⚠️ Important Notes

> **DO NOT commit**:
> - `node_modules/`
> - `target/`
> - `.env` or `application.properties` with secrets
> - IDE-specific files (`.idea/`, `.vscode/`)

> **DO commit**:
> - Source code (`.java`, `.jsx`, `.js`)
> - Configuration examples (`.example` files)
> - Documentation
> - Public assets

---

## 📞 Support

- **Questions**: Ask in team chat
- **Conflicts**: Coordinate with affected member
- **Issues**: Create GitHub issue with `[HELP]` tag

---

## ✅ Success Criteria

- [ ] All 6 members have pushed their files
- [ ] No merge conflicts
- [ ] All files in correct folders
- [ ] Code follows conventions
- [ ] Documentation is complete

---

**Let's build something amazing together! 🚀**
