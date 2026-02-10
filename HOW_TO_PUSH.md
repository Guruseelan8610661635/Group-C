# How to Push Your Files to Group-C Repository

This guide explains how each team member should push their allocated files to the GitHub repository.

---

## 📋 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/Guruseelan8610661635/Group-C.git
cd Group-C
```

### 2. Check Your File Assignments
Open [MEMBER_ASSIGNMENTS.md](MEMBER_ASSIGNMENTS.md) to see which files you're responsible for.

### 3. Add Your Files to the Correct Folders

#### For Backend Files:
```bash
# Navigate to the appropriate folder
cd Backend/src/main/java/com/smartparking/

# Example: Adding a controller
cd controller/
# Copy your AuthController.java here

# Example: Adding a model
cd ../model/
# Copy your User.java here

# Example: Adding a service
cd ../service/
# Copy your UserService.java here
```

#### For Frontend Files:
```bash
# Navigate to the appropriate folder
cd Frontend/src/

# Example: Adding a page
cd pages/
# Copy your Login.jsx here

# Example: Adding a component
cd components/
# Copy your SlotCard.jsx here

# Example: Adding a service
cd services/
# Copy your authService.js here
```

### 4. Commit and Push
```bash
# Go back to repository root
cd Group-C

# Add your files
git add .

# Commit with a clear message
git commit -m "feat(auth): add authentication files"

# Push to GitHub
git push origin main
```

---

## 📂 Folder Reference

### Backend Folders
- `Backend/src/main/java/com/smartparking/controller/` - Add your controllers here
- `Backend/src/main/java/com/smartparking/model/` - Add your models here
- `Backend/src/main/java/com/smartparking/repository/` - Add your repositories here
- `Backend/src/main/java/com/smartparking/service/` - Add your services here
- `Backend/src/main/java/com/smartparking/security/` - Add security files here
- `Backend/src/main/java/com/smartparking/dto/` - Add DTOs here
- `Backend/src/main/java/com/smartparking/config/` - Add config files here

### Frontend Folders
- `Frontend/src/pages/` - Add your page components here
- `Frontend/src/pages/admin/` - Add admin pages here
- `Frontend/src/components/` - Add your components here
- `Frontend/src/components/admin/` - Add admin components here
- `Frontend/src/services/` - Add your service files here
- `Frontend/src/utils/` - Add utility files here

---

## 👥 Member-Specific Instructions

### Member 1: Authentication & User Management
**Your files go in:**
- `Backend/.../controller/` - AuthController.java, UserController.java
- `Backend/.../model/` - User.java
- `Backend/.../repository/` - UserRepository.java
- `Backend/.../service/` - UserService.java
- `Backend/.../security/` - All JWT and security files
- `Frontend/.../pages/` - Login.jsx, Register.jsx, Profile.jsx
- `Frontend/.../services/` - authService.js
- `Frontend/.../utils/` - auth.js

### Member 2: Booking System & Slot Management
**Your files go in:**
- `Backend/.../controller/` - BookingController.java, SlotController.java, FastBookingController.java
- `Backend/.../model/` - Booking.java, Slot.java
- `Backend/.../repository/` - BookingRepository.java, SlotRepository.java
- `Backend/.../service/` - BookingService.java, SlotService.java
- `Frontend/.../pages/` - Booking.jsx, BookingHistory.jsx
- `Frontend/.../components/` - SlotCard.jsx
- `Frontend/.../services/` - bookingService.js, slotService.js

### Member 3: Location & Map Features
**Your files go in:**
- `Backend/.../controller/` - LocationController.java, MapController.java, OccupancyStatisticsController.java
- `Backend/.../model/` - Location.java
- `Backend/.../repository/` - LocationRepository.java
- `Backend/.../service/` - LocationService.java, OccupancyStatisticsService.java
- `Frontend/.../pages/` - Map.jsx
- `Frontend/.../components/` - MapView.jsx, LocationCard.jsx
- `Frontend/.../services/` - mapService.js, locationService.js

### Member 4: Payment & Pricing System
**Your files go in:**
- `Backend/.../controller/` - PaymentController.java, AdminPricingController.java, PromotionController.java
- `Backend/.../model/` - Payment.java, Pricing.java, Promotion.java
- `Backend/.../repository/` - PaymentRepository.java
- `Backend/.../service/` - PaymentService.java, PricingService.java
- `Frontend/.../pages/` - Payment.jsx
- `Frontend/.../components/` - PaymentModal.jsx
- `Frontend/.../services/` - paymentService.js, pricingService.js, promotionService.js

### Member 5: Admin Dashboard & Management
**Your files go in:**
- `Backend/.../controller/` - AdminController.java, SubscriptionController.java, NotificationController.java
- `Backend/.../model/` - Subscription.java, Notification.java
- `Backend/.../service/` - AdminService.java, NotificationService.java
- `Frontend/.../pages/admin/` - Dashboard.jsx, LocationManagement.jsx, SlotManagement.jsx
- `Frontend/.../components/admin/` - LocationFormInline.jsx, BookingList.jsx
- `Frontend/.../services/` - adminService.js, notificationService.js

### Member 6: Reports, Analytics & Audit
**Your files go in:**
- `Backend/.../controller/` - ReportController.java, AuditController.java
- `Backend/.../model/` - AuditLog.java
- `Backend/.../repository/` - AuditLogRepository.java
- `Backend/.../service/` - ReportService.java, AuditService.java
- `Frontend/.../pages/admin/` - Reports.jsx, Analytics.jsx
- `Frontend/.../components/admin/` - ReportChart.jsx
- `Frontend/.../services/` - reportService.js, auditService.js

---

## ⚠️ Important Reminders

1. **Check folder path carefully** - Make sure you're in the right directory
2. **Only push your assigned files** - Don't modify files owned by other members
3. **Use clear commit messages** - Follow the format: `feat(area): description`
4. **Pull before pushing** - Always run `git pull` before `git push` to avoid conflicts
5. **Test locally first** - Make sure your code works before pushing

---

## 🔄 Typical Workflow

```bash
# 1. Clone (first time only)
git clone https://github.com/Guruseelan8610661635/Group-C.git
cd Group-C

# 2. Pull latest changes
git pull origin main

# 3. Add your files to correct folders
# (Copy your files to the appropriate directories)

# 4. Check what you're adding
git status

# 5. Add your files
git add .

# 6. Commit with message
git commit -m "feat(booking): add booking controller and service"

# 7. Push to GitHub
git push origin main
```

---

## 📞 Need Help?

- **Not sure where to put a file?** Check [MEMBER_ASSIGNMENTS.md](MEMBER_ASSIGNMENTS.md)
- **Getting merge conflicts?** Ask in team chat
- **Questions about Git?** Reach out to team members

---

**Good luck! 🚀**
