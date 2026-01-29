# HỆ THỐNG QUẢN LÝ GIÁO DỤC VỚI CHẤM CÔNG VÂN TAY

Hệ thống quản lý sinh viên, giáo viên tích hợp công nghệ chấm công vân tay sử dụng ESP32 và cảm biến vân tay. Dự án bao gồm Backend API (Spring Boot), Frontend (Angular) và các module phần cứng (Arduino/ESP32).

## 📋 Mục Lục
- [Tổng Quan Dự Án](#tổng-quan-dự-án)
- [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
- [Cài Đặt](#cài-đặt)
- [Chạy Ứng Dụng](#chạy-ứng-dụng)
- [Tính Năng](#tính-năng)
- [API Documentation](#api-documentation)
- [Hardware Setup](#hardware-setup)

## 🎯 Tổng Quan Dự Án

Hệ thống quản lý giáo dục toàn diện với các chức năng:
- Quản lý sinh viên, giáo viên
- Quản lý lớp học, lịch học
- Quản lý điểm số
- Chấm công bằng vân tay (ESP32 + Fingerprint Sensor)
- Chatbot hỗ trợ
- AI Review
- Xác thực JWT và phân quyền người dùng

## 🛠 Công Nghệ Sử Dụng

### Backend
- **Framework**: Spring Boot 3.5.7
- **Language**: Java 21
- **Database**: Microsoft SQL Server
- **Authentication**: JWT (JSON Web Token)
- **Security**: Spring Security
- **ORM**: Spring Data JPA
- **Email**: Spring Boot Mail
- **Build Tool**: Maven
- **Other Libraries**: 
  - Lombok
  - OkHttp 4.12.0
  - JJWT 0.11.5

### Frontend
- **Framework**: Angular 19.2.0
- **Language**: TypeScript 5.7.2
- **UI**: Bootstrap 5.3.8
- **Charts**: Chart.js 4.5.1
- **State Management**: RxJS 7.8.0
- **Styling**: SCSS

### Hardware/IoT
- **Platform**: Arduino/ESP32
- **Sensor**: Fingerprint Sensor Module
- **Communication**: Serial/WiFi

## 📁 Cấu Trúc Dự Án

```
FinalProject/
├── backend/                    # Spring Boot API Server
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── env_secrets.properties
│   │   └── test/
│   └── pom.xml
│
├── Frontend/                   # Angular Application
│   ├── src/
│   │   ├── app/
│   │   │   ├── admin/         # Admin components
│   │   │   ├── chatbot/       # Chatbot feature
│   │   │   ├── gv-*/          # Teacher (Giáo viên) modules
│   │   │   ├── sv-*/          # Student (Sinh viên) modules
│   │   │   ├── guards/        # Route guards
│   │   │   ├── interceptors/  # HTTP interceptors
│   │   │   ├── services/      # API services
│   │   │   └── model/         # Data models
│   │   └── assets/
│   ├── angular.json
│   └── package.json
│
├── fingerPrint/                # Fingerprint attendance
├── fingerPrintEnroll/          # Fingerprint enrollment
├── Verify/                     # Fingerprint verification
├── xoavantay/                  # Delete fingerprint
├── tonghop/                    # Integrated module
├── esp32/                      # ESP32 specific code
└── README.md
```

## 💻 Yêu Cầu Hệ Thống

### Software Requirements
- **Java**: JDK 21 hoặc cao hơn
- **Node.js**: v18.x hoặc cao hơn
- **npm**: v9.x hoặc cao hơn
- **Maven**: 3.6.x hoặc cao hơn
- **Database**: Microsoft SQL Server 2019 hoặc cao hơn
- **Arduino IDE**: 1.8.x hoặc cao hơn (cho phần cứng)

### Hardware Requirements (Optional - for fingerprint feature)
- ESP32 Development Board
- Fingerprint Sensor Module (AS608/R307)
- Jumper wires
- USB Cable

## 🚀 Cài Đặt

### 1. Clone Repository
```bash
git clone <repository-url>
cd FinalProject
```

### 2. Cấu Hình Database

Tạo database trong SQL Server và cập nhật file cấu hình:

**backend/src/main/resources/application.properties**
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=your_database_name
spring.datasource.username=your_username
spring.datasource.password=your_password
```

**backend/src/main/resources/env_secrets.properties**
```properties
# Thêm các thông tin nhạy cảm như JWT secret, email credentials, etc.
```

### 3. Cài Đặt Backend

```bash
cd backend

# Windows
mvnw.cmd clean install

# Linux/Mac
./mvnw clean install
```

### 4. Cài Đặt Frontend

```bash
cd Frontend
npm install
```

### 5. Cài Đặt Hardware (Optional)

1. Mở Arduino IDE
2. Cài đặt board ESP32 (Tools > Board > Boards Manager > ESP32)
3. Cài đặt thư viện cần thiết:
   - Adafruit Fingerprint Sensor Library
   - WiFi library (đi kèm ESP32)

## 🎮 Chạy Ứng Dụng

### Chạy Backend

```bash
cd backend

# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

Backend sẽ chạy tại: `http://localhost:8080`

### Chạy Frontend

```bash
cd Frontend
npm start
```

Frontend sẽ chạy tại: `http://localhost:4200`

### Upload Code lên ESP32

1. Mở file `.ino` trong Arduino IDE
2. Chọn board ESP32 và COM port
3. Click Upload
4. Cấu hình WiFi credentials trong code nếu cần

## ✨ Tính Năng

### Cho Quản Trị Viên
- ✅ Quản lý người dùng (sinh viên, giáo viên)
- ✅ Quản lý hệ thống
- ✅ Xem báo cáo tổng hợp

### Cho Giáo Viên
- ✅ **Quản lý lớp học** (gv-quanlylophoc)
- ✅ **Quản lý điểm số** (gv-quanlydiemso)
- ✅ **Quản lý vân tay** (gv-quanlyvantay)
- ✅ **Xem lịch dạy** (gv-lichday)
- ✅ **AI Review** - Hỗ trợ đánh giá bằng AI (gv-aireview)
- ✅ Trang chủ giáo viên (gv-trangchu)

### Cho Sinh Viên
- ✅ **Xem điểm số** (sv-diemso)
- ✅ **Xem lịch học** (sv-lichhoc)
- ✅ Trang chủ sinh viên (sv-trangchu)
- ✅ Quản lý trang cá nhân

### Chức Năng Chung
- ✅ **Chatbot** - Hỗ trợ tự động
- ✅ **Đăng nhập/Đăng xuất** với JWT
- ✅ **Phân quyền** - Role-based access control
- ✅ **Slideshow** - Hiển thị thông tin
- ✅ **Toast Notifications** - Thông báo người dùng

### Hardware Features
- ✅ **Đăng ký vân tay** (fingerPrintEnroll)
- ✅ **Chấm công vân tay** (fingerPrint)
- ✅ **Xác thực vân tay** (Verify)
- ✅ **Xóa vân tay** (xoavantay)
- ✅ **Module tổng hợp** (tonghop)

## 📚 API Documentation

### Authentication Endpoints
```
POST /api/auth/login          - Đăng nhập
POST /api/auth/register       - Đăng ký
POST /api/auth/refresh-token  - Refresh JWT token
```

### Base URL
```
Development: http://localhost:8080/api
```

### Authentication
Tất cả API (trừ login/register) yêu cầu JWT token trong header:
```
Authorization: Bearer <your-jwt-token>
```

## 🔧 Hardware Setup

### Fingerprint Sensor Wiring (ESP32)
```
Fingerprint Sensor    ESP32
-----------------    ------
VCC (Red)        ->  3.3V
GND (Black)      ->  GND
TX (White)       ->  RX (GPIO 16)
RX (Green)       ->  TX (GPIO 17)
```

### Fingerprint Module Functions

1. **fingerPrintEnroll.ino**: Đăng ký vân tay mới
2. **fingerPrint.ino**: Chấm công bằng vân tay
3. **Verify.ino**: Xác thực vân tay
4. **xoavantay.ino**: Xóa vân tay khỏi hệ thống
5. **tonghop.ino**: Module tích hợp đầy đủ

## 🔐 Security

- JWT-based authentication
- Password encryption
- Role-based access control (RBAC)
- HTTP interceptors for token management
- Route guards for protected routes

## 📝 Environment Variables

### Backend (application.properties)
```properties
server.port=8080
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.mail.host=
spring.mail.port=
```

### Frontend (environment.ts)
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

## 🤝 Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

Dự án này được phát triển cho mục đích học tập và nghiên cứu.

## 👥 Authors

- Nhóm phát triển FinalProject

## 📞 Contact

- Email: [baongock52@gmail.com]
- Project Link: [https://github.com/your-username/FinalProject]

## 🙏 Acknowledgments

- Spring Boot Documentation
- Angular Documentation
- ESP32 Community
- Adafruit Fingerprint Sensor Library

---

**Note**: Đảm bảo cấu hình đúng database và các biến môi trường trước khi chạy ứng dụng.
