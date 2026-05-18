# 🍽️ MessMaster

A modern smart mess management mobile application built for shared living environments such as student messes, hostels, bachelor accommodations, and shared apartments.  
MessMaster simplifies meal tracking, utility management, member communication, and monthly expense calculation in a single platform.

The frontend is developed using **Kotlin + XML** for Android, while the backend is powered by **NestJS** with **PostgreSQL** for scalable and secure data management.

---

# 🚀 Features

## 👨‍💼 Manager Features
- Manage mess members
- Add and update daily meal records
- Calculate automatic meal rates
- Manage utility bills
- Post notices and announcements
- View mess statistics and reports
- Track monthly expenses
- Manage member requests and notifications

## 👤 Member Features
- View personal meal history
- Check monthly bills and utility costs
- Receive notices from manager
- Send meal updates or requests
- View calculated meal rates
- Update personal profile

## 🔐 Authentication System
- Login & Registration
- Forgot Password
- Secure account management

## ⭐ Additional Features
- Review & feedback system
- Clean modern UI/UX
- Role-based access control
- Real-time data synchronization
- Responsive Android design

---

# 🛠️ Tech Stack

## 📱 Frontend
- Kotlin
- XML Layouts
- Material Design

## ⚙️ Backend
- NestJS
- REST API
- JWT Authentication
- TypeORM

## 🗄️ Database
- PostgreSQL

---

# 📸 Application Modules

## 🔑 Authentication
- Login Screen
- Registration Screen
- Forgot Password Screen

## 🏠 Dashboard
- Total Members
- Meal Statistics
- Utility Cost Summary
- Monthly Expense Overview

## 🍛 Meal Management
- Add Meals
- Update Meal Records
- Daily Meal Tracking
- Automatic Meal Rate Calculation

## ⚡ Utility Management
- Electricity Bill
- Water Bill
- Internet Bill
- Grocery/Bazar Costs

## 📢 Notice System
- Manager Announcements
- Member Requests
- Notifications

## 👤 Profile & Settings
- User Profile
- Role Management
- Notification Settings
- Account Settings

---

# 🔄 System Workflow

```text
User Authentication
        ↓
Role Detection (Manager/User)
        ↓
Dashboard Access
        ↓
Meal & Utility Management
        ↓
Monthly Cost Calculation
        ↓
Meal Rate Generation
        ↓
Bill Distribution
```

---

# 🧮 Meal Rate Formula

```text
Meal Rate = Total Monthly Expense / Total Meals
```

### Example

```text
Total Expense = ৳22,776
Total Meals   = 438

Meal Rate = ৳52
```

---

# 🔐 Authentication & Security

- JWT Based Authentication
- Password Encryption
- Protected API Routes
- Role-Based Authorization
- Secure PostgreSQL Database Handling

---

# 📡 API Modules

| Module | Description |
|---|---|
| Auth API | Login, Register, OTP |
| User API | Member management |
| Meal API | Meal tracking |
| Utility API | Utility bill handling |
| Notice API | Notice management |
| Bill API | Monthly bill generation |

---

# 🎨 UI/UX Design

The application follows:
- Minimal modern design
- Black & white professional theme
- Clean dashboard layout
- Mobile-first experience
- User-friendly navigation

---

# 📈 Future Improvements

- Push Notifications
- Real-time Chat
- Multi-Mess Support
- Analytics Dashboard

---

# 👨‍💻 Author

**Al-Amin Hossain Nahid**

---

# ⭐ Support

If you like this project, give it a ⭐ on GitHub and support the development of MessMaster.