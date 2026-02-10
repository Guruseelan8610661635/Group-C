# Member File Assignments

This document specifies which files each team member is responsible for. All members work in the **same common folders**, but each owns specific files.

---

## 📋 File Ownership Rules

1. **Own your files**: Only modify files assigned to you
2. **Coordinate on shared files**: Discuss changes to models/DTOs that affect multiple members
3. **Communicate**: Inform team before making breaking changes
4. **Review**: Help review code from other members

---

## Member 1: Authentication & User Management 🔐

### Backend Files (`Backend/src/main/java/com/smartparking/`)

**Controllers** (`controller/`):
- `AuthController.java`
- `UserController.java`

**Models** (`model/`):
- `User.java`

**Repositories** (`repository/`):
- `UserRepository.java`

**Services** (`service/`):
- `UserService.java`

**Security** (`security/`):
- All files in security folder (JWT utilities, filters, configs)

**DTOs** (`dto/`):
- `LoginRequest.java`
- `RegisterRequest.java`
- `UserDTO.java`

### Frontend Files (`Frontend/src/`)

**Pages** (`pages/`):
- `Login.jsx`
- `Register.jsx`
- `Profile.jsx`

**Services** (`services/`):
- `authService.js`

**Utils** (`utils/`):
- `auth.js`

---

## Member 2: Booking System & Slot Management 🅿️

### Backend Files (`Backend/src/main/java/com/smartparking/`)

**Controllers** (`controller/`):
- `BookingController.java`
- `FastBookingController.java`
- `SlotController.java`

**Models** (`model/`):
- `Booking.java`
- `Slot.java`

**Repositories** (`repository/`):
- `BookingRepository.java`
- `SlotRepository.java`

**Services** (`service/`):
- `BookingService.java`
- `SlotService.java`

### Frontend Files (`Frontend/src/`)

**Pages** (`pages/`):
- `Booking.jsx`
- `BookingHistory.jsx`

**Components** (`components/`):
- `SlotCard.jsx`

**Services** (`services/`):
- `bookingService.js`
- `slotService.js`

---

## Member 3: Location & Map Features 🗺️

### Backend Files (`Backend/src/main/java/com/smartparking/`)

**Controllers** (`controller/`):
- `LocationController.java`
- `MapController.java`
- `OccupancyStatisticsController.java`

**Models** (`model/`):
- `Location.java`

**Repositories** (`repository/`):
- `LocationRepository.java`

**Services** (`service/`):
- `LocationService.java`
- `OccupancyStatisticsService.java`

### Frontend Files (`Frontend/src/`)

**Pages** (`pages/`):
- `Map.jsx`

**Components** (`components/`):
- `MapView.jsx`
- `LocationCard.jsx`

**Services** (`services/`):
- `mapService.js`
- `locationService.js`

---

## Member 4: Payment & Pricing System 💳

### Backend Files (`Backend/src/main/java/com/smartparking/`)

**Controllers** (`controller/`):
- `PaymentController.java`
- `AdminPricingController.java`
- `PromotionController.java`

**Models** (`model/`):
- `Payment.java`
- `Pricing.java`
- `Promotion.java`

**Repositories** (`repository/`):
- `PaymentRepository.java`

**Services** (`service/`):
- `PaymentService.java`
- `PricingService.java`

### Frontend Files (`Frontend/src/`)

**Pages** (`pages/`):
- `Payment.jsx`

**Components** (`components/`):
- `PaymentModal.jsx`

**Services** (`services/`):
- `paymentService.js`
- `pricingService.js`
- `promotionService.js`

---

## Member 5: Admin Dashboard & Management 👨‍💼

### Backend Files (`Backend/src/main/java/com/smartparking/`)

**Controllers** (`controller/`):
- `AdminController.java`
- `SubscriptionController.java`
- `NotificationController.java`

**Models** (`model/`):
- `Subscription.java`
- `Notification.java`

**Services** (`service/`):
- `AdminService.java`
- `NotificationService.java`

### Frontend Files (`Frontend/src/`)

**Pages** (`pages/admin/`):
- `Dashboard.jsx`
- `LocationManagement.jsx`
- `SlotManagement.jsx`

**Components** (`components/admin/`):
- `LocationFormInline.jsx`
- `BookingList.jsx`

**Services** (`services/`):
- `adminService.js`
- `notificationService.js`

---

## Member 6: Reports, Analytics & Audit 📊

### Backend Files (`Backend/src/main/java/com/smartparking/`)

**Controllers** (`controller/`):
- `ReportController.java`
- `AuditController.java`

**Models** (`model/`):
- `AuditLog.java`

**Repositories** (`repository/`):
- `AuditLogRepository.java`

**Services** (`service/`):
- `ReportService.java`
- `AuditService.java`

### Frontend Files (`Frontend/src/`)

**Pages** (`pages/admin/`):
- `Reports.jsx`
- `Analytics.jsx`

**Components** (`components/admin/`):
- `ReportChart.jsx`

**Services** (`services/`):
- `reportService.js`
- `auditService.js`

---

## 🔄 Shared Files (Coordinate Before Modifying)

These files may be used by multiple members. Discuss changes in team chat:

### Backend
- **DTOs**: Any DTO used by multiple controllers
- **Config files**: Application configuration
- **Common utilities**: Shared helper classes

### Frontend
- **Common components**: Shared UI components
- **API configuration**: Base API setup
- **Routing**: Main routing configuration

---

## 📝 How to Add Your Files

### Step 1: Navigate to Correct Folder
```bash
cd Group-C

# For Backend
cd Backend/src/main/java/com/smartparking/controller/

# For Frontend
cd Frontend/src/pages/
```

### Step 2: Add Your File
Copy or create your file in the appropriate folder

### Step 3: Commit and Push
```bash
git add .
git commit -m "feat(area): add [filename]"
git push origin main
```

---

## ⚠️ Important Reminders

1. **Only modify your assigned files**
2. **Coordinate on shared interfaces**
3. **Test before pushing**
4. **Use clear commit messages**
5. **Communicate with team**

---

## 📞 Questions?

If you're unsure about file ownership or need to modify a shared file, ask in the team chat!
