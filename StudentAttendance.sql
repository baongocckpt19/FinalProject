/*====================================================== 
= 0) TẠO MỚI DATABASE
======================================================*/
USE master;
IF DB_ID('StudentAttendanceDB') IS NOT NULL
BEGIN
    ALTER DATABASE StudentAttendanceDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE StudentAttendanceDB;
END;
CREATE DATABASE StudentAttendanceDB;
GO

USE StudentAttendanceDB;
GO

/*======================================================
= 1) TẠO TOÀN BỘ CÁC BẢNG + CONSTRAINT
======================================================*/

--------------------------------------------------------
-- 1.1 BẢNG VAI TRÒ
--------------------------------------------------------
IF OBJECT_ID('Role') IS NOT NULL DROP TABLE Role;
CREATE TABLE Role (
    RoleId   INT IDENTITY(1,1) PRIMARY KEY,
    RoleName NVARCHAR(50) NOT NULL -- Admin / Giảng viên / Học sinh
);
GO

--------------------------------------------------------
-- 1.2 BẢNG TÀI KHOẢN
--------------------------------------------------------
IF OBJECT_ID('Account') IS NOT NULL DROP TABLE Account;
CREATE TABLE Account (
    AccountId    INT IDENTITY(1,1) PRIMARY KEY,
    Username     NVARCHAR(50)  NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    RoleId       INT           NOT NULL FOREIGN KEY REFERENCES Role(RoleId),
    IsDeleted    BIT           NOT NULL DEFAULT 0
);
GO

--------------------------------------------------------
-- 1.3 BẢNG GIẢNG VIÊN
--------------------------------------------------------
IF OBJECT_ID('Teacher') IS NOT NULL DROP TABLE Teacher;
CREATE TABLE Teacher (
    TeacherId   INT IDENTITY(1,1) PRIMARY KEY,
    AccountId   INT NOT NULL FOREIGN KEY REFERENCES Account(AccountId),
    FullName    NVARCHAR(100) NOT NULL,
    Email       NVARCHAR(100),
    Phone       NVARCHAR(20),
    Department  NVARCHAR(100),
    Address     NVARCHAR(200),
    DateOfBirth DATE NULL,
    Gender      NVARCHAR(10) NULL,
    CONSTRAINT CHK_Teacher_Gender CHECK (Gender IN (N'Nam', N'Nữ', N'Khác', NULL))
);
GO

--------------------------------------------------------
-- 1.4 BẢNG HỌC SINH
--------------------------------------------------------
IF OBJECT_ID('Student') IS NOT NULL DROP TABLE Student;
CREATE TABLE Student (
    StudentId   INT IDENTITY(1,1) PRIMARY KEY,
    AccountId   INT NOT NULL FOREIGN KEY REFERENCES Account(AccountId),
    FullName    NVARCHAR(100) NOT NULL,
    Email       NVARCHAR(100),
    Phone       NVARCHAR(20),
    Address     NVARCHAR(200),
    DateOfBirth DATE NULL,
    Gender      NVARCHAR(10) NULL,
    CONSTRAINT CHK_Student_Gender CHECK (Gender IN (N'Nam', N'Nữ', N'Khác', NULL))
);
GO

--------------------------------------------------------
-- 1.5 BẢNG VÂN TAY ĐƠN GIẢN (Fingerprint) - GIỮ LẠI
--------------------------------------------------------
IF OBJECT_ID('Fingerprint') IS NOT NULL DROP TABLE Fingerprint;
CREATE TABLE Fingerprint (
    FingerprintID INT IDENTITY(1,1) PRIMARY KEY,
    StudentId     INT NOT NULL FOREIGN KEY REFERENCES Student(StudentId),
    SensorSlot    INT NOT NULL
);
GO

ALTER TABLE Fingerprint
ADD CONSTRAINT UQ_Fingerprint_SensorSlot UNIQUE (SensorSlot);
GO

--------------------------------------------------------
-- 1.6 BẢNG LỚP HỌC
--------------------------------------------------------
IF OBJECT_ID('Class') IS NOT NULL DROP TABLE Class;
CREATE TABLE Class (
    ClassId     INT IDENTITY(1,1) PRIMARY KEY,
    ClassCode   NVARCHAR(50) UNIQUE NOT NULL,
    ClassName   NVARCHAR(100) NOT NULL,
    TeacherId   INT FOREIGN KEY REFERENCES Teacher(TeacherId),
    CreatedDate DATETIME NOT NULL DEFAULT GETDATE(),
    Status      BIT NOT NULL DEFAULT 1, --0: HOAT DONG/1: KHONG HOAT DONG
    IsDeleted   BIT NOT NULL DEFAULT 0
);
GO

--------------------------------------------------------
-- 1.7 BẢNG LỊCH HỌC
--------------------------------------------------------
IF OBJECT_ID('ClassSchedule') IS NOT NULL DROP TABLE ClassSchedule;
CREATE TABLE ClassSchedule (
    ScheduleId   INT IDENTITY(1,1) PRIMARY KEY,
    ClassId      INT NOT NULL FOREIGN KEY REFERENCES Class(ClassId),
    ScheduleDate DATE NOT NULL,
    StartTime    TIME NOT NULL,
    EndTime      TIME NOT NULL,
    Room         NVARCHAR(50) NULL,
    IsActive     BIT NOT NULL DEFAULT 1,
    IsDeleted    BIT NOT NULL DEFAULT 0
);
GO

--------------------------------------------------------
-- 1.8 BẢNG SINH VIÊN - LỚP
--------------------------------------------------------
IF OBJECT_ID('StudentClass') IS NOT NULL DROP TABLE StudentClass;
CREATE TABLE StudentClass (
    StudentId INT NOT NULL FOREIGN KEY REFERENCES Student(StudentId),
    ClassId   INT NOT NULL FOREIGN KEY REFERENCES Class(ClassId),
    IsDeleted BIT NOT NULL DEFAULT 0,
    CONSTRAINT PK_StudentClass PRIMARY KEY (StudentId, ClassId)
);
GO

--------------------------------------------------------
-- 1.9 BẢNG ĐIỂM DANH
--------------------------------------------------------
IF OBJECT_ID('Attendance') IS NOT NULL DROP TABLE Attendance;
CREATE TABLE Attendance (
    AttendanceId   INT IDENTITY(1,1) PRIMARY KEY,
    StudentId      INT NOT NULL FOREIGN KEY REFERENCES Student(StudentId),
    ClassId        INT NOT NULL FOREIGN KEY REFERENCES Class(ClassId),
    ScheduleId     INT NOT NULL FOREIGN KEY REFERENCES ClassSchedule(ScheduleId),
    AttendanceTime TIME NOT NULL,
    Status         NVARCHAR(50) NOT NULL
);
GO

ALTER TABLE Attendance
ADD CONSTRAINT UQ_Attendance_Student_Schedule UNIQUE (StudentId, ScheduleId);
GO

--------------------------------------------------------
-- 1.10 BẢNG ĐIỂM SỐ
--------------------------------------------------------
IF OBJECT_ID('Grade') IS NOT NULL DROP TABLE Grade;
CREATE TABLE Grade (
    GradeId         INT IDENTITY(1,1) PRIMARY KEY,
    StudentId       INT NOT NULL FOREIGN KEY REFERENCES Student(StudentId),
    ClassId         INT NOT NULL FOREIGN KEY REFERENCES Class(ClassId),
    AttendanceGrade FLOAT CHECK(AttendanceGrade BETWEEN 0 AND 10),
    MidtermGrade    FLOAT CHECK(MidtermGrade    BETWEEN 0 AND 10),
    FinalGrade      FLOAT CHECK(FinalGrade      BETWEEN 0 AND 10)
);
GO

--------------------------------------------------------
-- 1.11 BẢNG DEVICE (THIẾT BỊ VÂN TAY)
--------------------------------------------------------
IF OBJECT_ID('Device') IS NOT NULL DROP TABLE Device;
CREATE TABLE Device (
    DeviceId     INT IDENTITY(1,1) PRIMARY KEY,
    DeviceCode   NVARCHAR(50) UNIQUE NOT NULL,
    DeviceName   NVARCHAR(100) NULL,
    Room         NVARCHAR(50) NULL,
    IsActive     BIT NOT NULL DEFAULT 1
);
GO

--------------------------------------------------------
-- 1.12 BẢNG FINGERPRINT TEMP (nếu cần)
--------------------------------------------------------
IF OBJECT_ID('FingerprintTemp') IS NOT NULL DROP TABLE FingerprintTemp;
GO

CREATE TABLE FingerprintTemp (
    TempId      INT IDENTITY(1,1) PRIMARY KEY,
    SessionCode NVARCHAR(100) NOT NULL UNIQUE,
    SensorSlot  INT NULL,
    CreatedAt   DATETIME NOT NULL DEFAULT GETDATE()
);
GO

--------------------------------------------------------
-- 1.13 BẢNG FINGERPRINT TEMPLATE (LƯU TEMPLATE NHỊ PHÂN)
--------------------------------------------------------
IF OBJECT_ID('FingerprintTemplate') IS NOT NULL DROP TABLE FingerprintTemplate;
GO

CREATE TABLE FingerprintTemplate (
    TemplateId   INT IDENTITY(1,1) PRIMARY KEY,
    StudentId    INT NOT NULL FOREIGN KEY REFERENCES Student(StudentId),
    TemplateData VARBINARY(MAX) NOT NULL,
    CreatedAt    DATETIME NOT NULL DEFAULT GETDATE()
);
GO

CREATE UNIQUE INDEX UX_FingerprintTemplate_Student
ON FingerprintTemplate(StudentId);
GO

--------------------------------------------------------
-- 1.14 BẢNG DEVICE - FINGERPRINT SLOT (MAP DEVICE + STUDENT + SLOT)
--------------------------------------------------------
IF OBJECT_ID('DeviceFingerprintSlot') IS NOT NULL DROP TABLE DeviceFingerprintSlot;
GO

CREATE TABLE DeviceFingerprintSlot (
    DeviceId   INT NOT NULL FOREIGN KEY REFERENCES Device(DeviceId),
    StudentId  INT NOT NULL FOREIGN KEY REFERENCES Student(StudentId),
    SensorSlot INT NOT NULL,

    CONSTRAINT PK_DeviceFingerprintSlot PRIMARY KEY (DeviceId, StudentId),
    CONSTRAINT UQ_Device_SensorSlot UNIQUE (DeviceId, SensorSlot)
);
GO

--------------------------------------------------------
-- 1.15 BẢNG FINGERPRINT ENROLL SESSION
--------------------------------------------------------
IF OBJECT_ID('FingerprintEnrollSession') IS NOT NULL DROP TABLE FingerprintEnrollSession;
GO

CREATE TABLE FingerprintEnrollSession (
    SessionId    INT IDENTITY(1,1) PRIMARY KEY,
    SessionCode  NVARCHAR(100) NOT NULL UNIQUE,
    StudentId    INT NOT NULL FOREIGN KEY REFERENCES Student(StudentId),
    DeviceId     INT NULL FOREIGN KEY REFERENCES Device(DeviceId),
    SensorSlot   INT NULL,
    TemplateData VARBINARY(MAX) NULL,
    Status       NVARCHAR(20) NOT NULL, 
    CreatedAt    DATETIME NOT NULL DEFAULT GETDATE()
);
GO

ALTER TABLE Student ADD StudentCode NVARCHAR(50) NULL;
ALTER TABLE Teacher ADD TeacherCode NVARCHAR(50) NULL;

--------------------------------------------------------
-- 1.16 BẢNG ESP LOG
--------------------------------------------------------
IF OBJECT_ID('EspLog') IS NOT NULL DROP TABLE EspLog;
GO

CREATE TABLE EspLog (
    LogId      INT IDENTITY(1,1) PRIMARY KEY,
    DeviceCode NVARCHAR(50) NOT NULL,
    Message    NVARCHAR(255) NOT NULL,
    CreatedAt  DATETIME NOT NULL DEFAULT GETDATE()
);
GO


CREATE TABLE PasswordResetToken (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    AccountId INT NOT NULL,
    Code NVARCHAR(20) NOT NULL,
    ExpiryDate DATETIME2 NOT NULL,
    IsUsed BIT NOT NULL DEFAULT 0,
    CONSTRAINT FK_PasswordResetToken_Account
    FOREIGN KEY (AccountId) REFERENCES Account(AccountId)
);

/*======================================================
= 2) DỮ LIỆU MẪU
======================================================*/

-----------------------
-- 2.1 ROLE
-----------------------
INSERT INTO Role (RoleName)
VALUES (N'Admin'), (N'Giảng viên'), (N'Học sinh');
GO

-----------------------
-- 2.2 ACCOUNT
-----------------------
INSERT INTO Account (Username, PasswordHash, RoleId, IsDeleted)
VALUES ('admin01', 'admin123', 1, 0);   -- Admin

INSERT INTO Account (Username, PasswordHash, RoleId, IsDeleted)
VALUES 
('teacher01', '12345', 2, 0),
('teacher02', '12345', 2, 0),
('teacher03', '12345', 2, 0),
('teacher04', '12345', 2, 0),
('teacher05', '12345', 2, 0),
('teacher06', '12345', 2, 0);

INSERT INTO Account (Username, PasswordHash, RoleId, IsDeleted)
VALUES
('student01', '12345', 3, 0),
('student02', '12345', 3, 0),
('student03', '12345', 3, 0),
('student04', '12345', 3, 0),
('student05', '12345', 3, 0),
('student06', '12345', 3, 0),
('student07', '12345', 3, 0),
('student08', '12345', 3, 0),
('student09', '12345', 3, 0),
('student10', '12345', 3, 0),
('student11', '12345', 3, 0),
('student12', '12345', 3, 0),
('student13', '12345', 3, 0),
('student14', '12345', 3, 0),
('student15', '12345', 3, 0),
('student16', '12345', 3, 0),
('student17', '12345', 3, 0),
('student18', '12345', 3, 0),
('student19', '12345', 3, 0),
('student20', '12345', 3, 0),
('student21', '12345', 3, 0),
('student22', '12345', 3, 0),
('student23', '12345', 3, 0),
('student24', '12345', 3, 0);
GO

-----------------------
-- 2.3 TEACHER
-----------------------
INSERT INTO Teacher (AccountId, FullName, Email, Phone, Department, Address, DateOfBirth, Gender)
VALUES
(2, N'Nguyễn Văn A', 'a.nguyen@univ.edu.vn',  '0901000001', N'Công nghệ thông tin', N'Quận 1, TP.HCM', '1980-05-15', N'Nam'),
(3, N'Lê Thị B',     'b.le@univ.edu.vn',      '0901000002', N'Khoa Toán - Tin',     N'Đống Đa, Hà Nội', '1975-11-20', N'Nữ'),
(4, N'Phạm Quốc C',  'c.pham@univ.edu.vn',    '0901000003', N'Kỹ thuật phần mềm',   N'Thanh Xuân, Hà Nội', '1982-03-10', N'Nam'),
(5, N'Trần Thị D',   'd.tran@univ.edu.vn',    '0901000004', N'Ngoại ngữ',           N'Hải Châu, Đà Nẵng', '1988-09-25', N'Nữ'),
(6, N'Hoàng Minh E', 'e.hoang@univ.edu.vn',   '0901000005', N'Vật lý',              N'Ninh Kiều, Cần Thơ', '1979-01-05', N'Nam'),
(7, N'Vũ Thị F',     'f.vu@univ.edu.vn',      '0901000006', N'Khoa học dữ liệu',    N'Thủ Đức, TP.HCM', '1985-07-30', N'Nữ');
GO

-----------------------
-- 2.4 STUDENT
-----------------------
INSERT INTO Student (AccountId, FullName, Email, Phone, Address, DateOfBirth, Gender)
VALUES
(8,  N'Nguyễn Minh An',      'an.nguyen@student.edu.vn',     '0912000001', N'Ba Đình, Hà Nội',       '2003-03-12', N'Nam'),
(9,  N'Trần Thị Bình',       'binh.tran@student.edu.vn',     '0912000002', N'Cầu Giấy, Hà Nội',      '2004-07-21', N'Nữ'),
(10, N'Lê Hoàng Cường',      'cuong.le@student.edu.vn',      '0912000003', N'Hồng Bàng, Hải Phòng', '2003-10-05', N'Nam'),
(11, N'Phạm Anh Dũng',       'dung.pham@student.edu.vn',     '0912000004', N'Hai Bà Trưng, Hà Nội', '2002-12-19', N'Nam'),
(12, N'Đỗ Thu Hà',           'ha.do@student.edu.vn',         '0912000005', N'Tân Bình, TP.HCM',     '2004-02-08', N'Nữ'),
(13, N'Hoàng Minh Huy',      'huy.hoang@student.edu.vn',     '0912000006', N'Thủ Đức, TP.HCM',      '2003-08-30', N'Nam'),
(14, N'Vũ Thị Lan',          'lan.vu@student.edu.vn',        '0912000007', N'Lê Chân, Hải Phòng',   '2004-11-11', N'Nữ'),
(15, N'Bùi Quang Long',      'long.bui@student.edu.vn',      '0912000008', N'Sơn Trà, Đà Nẵng',     '2003-06-22', N'Nam'),
(16, N'Ngô Thị Mai',         'mai.ngo@student.edu.vn',       '0912000009', N'Liên Chiểu, Đà Nẵng',  '2003-09-02', N'Nữ'),
(17, N'Phan Anh Khoa',       'khoa.phan@student.edu.vn',     '0912000010', N'Ninh Kiều, Cần Thơ',   '2002-05-17', N'Nam'),
(18, N'Đặng Thị Ngọc',       'ngoc.dang@student.edu.vn',     '0912000011', N'Bình Thạnh, TP.HCM',   '2004-03-27', N'Nữ'),
(19, N'Lý Hoàng Nam',        'nam.ly@student.edu.vn',        '0912000012', N'Thanh Khê, Đà Nẵng',   '2003-01-09', N'Nam'),
(20, N'Nguyễn Thị Oanh',     'oanh.nguyen@student.edu.vn',   '0912000013', N'Quận 3, TP.HCM',       '2004-06-14', N'Nữ'),
(21, N'Trương Minh Phúc',    'phuc.truong@student.edu.vn',   '0912000014', N'Quận 10, TP.HCM',      '2003-04-18', N'Nam'),
(22, N'Đoàn Thị Quỳnh',      'quynh.doan@student.edu.vn',    '0912000015', N'Long Biên, Hà Nội',    '2004-12-01', N'Nữ'),
(23, N'Nguyễn Đức Sơn',      'son.nguyen@student.edu.vn',    '0912000016', N'Hoàn Kiếm, Hà Nội',    '2002-09-29', N'Nam'),
(24, N'Phạm Thị Trang',      'trang.pham@student.edu.vn',    '0912000017', N'Hồng Bàng, Hải Phòng', '2003-02-03', N'Nữ'),
(25, N'Hoàng Anh Tuấn',      'tuan.hoang@student.edu.vn',    '0912000018', N'Thanh Xuân, Hà Nội',   '2002-07-07', N'Nam'),
(26, N'Lê Thị Uyên',         'uyen.le@student.edu.vn',       '0912000019', N'Cẩm Lệ, Đà Nẵng',      '2004-09-15', N'Nữ'),
(27, N'Võ Minh Việt',        'viet.vo@student.edu.vn',       '0912000020', N'Thủ Đức, TP.HCM',      '2003-11-23', N'Nam'),
(28, N'Đinh Thị Yến',        'yen.dinh@student.edu.vn',      '0912000021', N'Ninh Kiều, Cần Thơ',   '2004-01-19', N'Nữ'),
(29, N'Ngô Thanh Hải',       'hai.ngo@student.edu.vn',       '0912000022', N'Bình Thạnh, TP.HCM',   '2003-05-01', N'Nam'),
(30, N'Phan Thị Giang',      'giang.phan@student.edu.vn',    '0912000023', N'Ba Đình, Hà Nội',      '2004-08-28', N'Nữ'),
(31, N'Bùi Anh Khánh',       'khanh.bui@student.edu.vn',     '0912000024', N'Sơn Trà, Đà Nẵng',     '2003-10-30', N'Nam');
GO

-----------------------
-- 2.5 CLASS
-----------------------
INSERT INTO Class (ClassCode, ClassName, TeacherId, Status)
VALUES
('CNTT101', N'Lập trình Web',                         1, 1),
('CNTT102', N'Cơ sở dữ liệu',                         1, 1),
('CNTT201', N'Kiến trúc máy tính',                    1, 0),
('MATH201', N'Toán rời rạc',                          2, 1),
('MATH202', N'Xác suất thống kê',                     2, 1),
('PHY101',  N'Vật lý đại cương',                      3, 1),
('ENG101',  N'Tiếng Anh cơ bản',                      4, 1),
('AI301',   N'Nhập môn Trí tuệ nhân tạo',             5, 0);
GO

-----------------------
-- 2.6 CLASS SCHEDULE
-----------------------
INSERT INTO ClassSchedule (ClassId, ScheduleDate, StartTime, EndTime, Room, IsActive, IsDeleted)
VALUES
(1, '2025-10-01', '07:45', '09:15', N'Phòng Lab 1', 1, 0),
(1, '2025-10-08', '07:45', '09:15', N'Phòng Lab 1', 1, 0),
(2, '2025-10-01', '10:00', '11:30', N'Phòng Lab 2', 1, 0),
(4, '2025-10-08', '13:00', '14:30', N'Phòng 202-A', 1, 0),

(1, '2025-11-27', '07:00', '09:00', N'Phòng Lab 1', 1, 0),
(1, '2025-12-04', '07:00', '09:00', N'Phòng Lab 1', 1, 0),
(1, '2025-12-11', '07:00', '09:00', N'Phòng Lab 1', 1, 0),

(2, '2025-11-27', '10:00', '11:30', N'Phòng Lab 2', 1, 0),
(4, '2025-11-27', '13:00', '14:30', N'Phòng 202-A', 1, 0),

(1, '2025-10-15', '07:45', '09:15', N'Phòng Lab test', 0, 0);
GO

-----------------------
-- 2.7 STUDENTCLASS
-----------------------
INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(1,1,0),(2,1,0),(3,1,0),(4,1,0),(5,1,0),(6,1,0),(7,1,0),(8,1,0);

INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(5,2,0),(6,2,0),(7,2,0),(8,2,0),(9,2,0),(10,2,0),(11,2,0),(12,2,0);

INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(9,3,0),(10,3,0),(11,3,0),(12,3,0),(13,3,0),(14,3,0);

INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(1,4,0),(2,4,0),(3,4,0),(9,4,0),(10,4,0),(11,4,0);

INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(12,5,0),(13,5,0),(14,5,0),(15,5,0),(16,5,0),(17,5,0),(18,5,0);

INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(15,6,0),(16,6,0),(17,6,0),(18,6,0),(19,6,0),(20,6,0);

INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(18,7,0),(19,7,0),(20,7,0),(21,7,0),(22,7,0),(23,7,0),(24,7,0);

INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(3,8,0),(4,8,0),(7,8,0),(8,8,0),(11,8,0),(14,8,0),(17,8,0),(20,8,0);
GO

-----------------------
-- 2.8 FINGERPRINT SAMPLE (GIỮ LẠI)
-----------------------
INSERT INTO Fingerprint (StudentId, SensorSlot)
VALUES
(1,  1),
(2,  2),
(3,  3),
(4,  4),
(5,  5),
(6,  6),
(7,  7),
(8,  8),
(10, 10),
(12, 12),
(15, 15),
(18, 18),
(20, 20),
(22, 22),
(24, 24);
GO

-----------------------
-- 2.9 ATTENDANCE SAMPLE
-----------------------
DECLARE 
    @SCH_CNTT101_20251001 INT,
    @SCH_CNTT101_20251008 INT,
    @SCH_CNTT102_20251001 INT,
    @SCH_MATH201_20251008 INT;

SELECT @SCH_CNTT101_20251001 = ScheduleId 
FROM ClassSchedule 
WHERE ClassId = 1 AND ScheduleDate = '2025-10-01' AND StartTime = '07:45' AND EndTime = '09:15';

SELECT @SCH_CNTT101_20251008 = ScheduleId 
FROM ClassSchedule 
WHERE ClassId = 1 AND ScheduleDate = '2025-10-08' AND StartTime = '07:45' AND EndTime = '09:15';

SELECT @SCH_CNTT102_20251001 = ScheduleId 
FROM ClassSchedule 
WHERE ClassId = 2 AND ScheduleDate = '2025-10-01' AND StartTime = '10:00' AND EndTime = '11:30';

SELECT @SCH_MATH201_20251008 = ScheduleId 
FROM ClassSchedule 
WHERE ClassId = 4 AND ScheduleDate = '2025-10-08' AND StartTime = '13:00' AND EndTime = '14:30';

INSERT INTO Attendance (StudentId, ClassId, ScheduleId, AttendanceTime, Status)
VALUES
(1,1,@SCH_CNTT101_20251001,'07:55',N'Có mặt'),
(2,1,@SCH_CNTT101_20251001,'08:10',N'Muộn'),
(3,1,@SCH_CNTT101_20251001,'07:50',N'Có mặt'),
(4,1,@SCH_CNTT101_20251001,'08:30',N'Vắng'),
(5,1,@SCH_CNTT101_20251001,'07:47',N'Có mặt'),
(6,1,@SCH_CNTT101_20251001,'07:59',N'Có mặt'),
(7,1,@SCH_CNTT101_20251001,'08:20',N'Muộn'),
(8,1,@SCH_CNTT101_20251001,'08:05',N'Có mặt');

INSERT INTO Attendance (StudentId, ClassId, ScheduleId, AttendanceTime, Status)
VALUES
(1,1,@SCH_CNTT101_20251008,'07:53',N'Có mặt'),
(2,1,@SCH_CNTT101_20251008,'08:12',N'Muộn'),
(3,1,@SCH_CNTT101_20251008,'07:49',N'Có mặt'),
(4,1,@SCH_CNTT101_20251008,'08:35',N'Vắng'),
(5,1,@SCH_CNTT101_20251008,'07:46',N'Có mặt'),
(6,1,@SCH_CNTT101_20251008,'07:58',N'Có mặt'),
(7,1,@SCH_CNTT101_20251008,'08:18',N'Muộn'),
(8,1,@SCH_CNTT101_20251008,'08:03',N'Có mặt');

INSERT INTO Attendance (StudentId, ClassId, ScheduleId, AttendanceTime, Status)
VALUES
(5,2,@SCH_CNTT102_20251001,'10:05',N'Có mặt'),
(6,2,@SCH_CNTT102_20251001,'10:15',N'Muộn'),
(7,2,@SCH_CNTT102_20251001,'10:02',N'Có mặt'),
(8,2,@SCH_CNTT102_20251001,'10:20',N'Muộn'),
(9,2,@SCH_CNTT102_20251001,'10:01',N'Có mặt'),
(10,2,@SCH_CNTT102_20251001,'10:25',N'Vắng'),
(11,2,@SCH_CNTT102_20251001,'10:08',N'Có mặt'),
(12,2,@SCH_CNTT102_20251001,'10:12',N'Muộn');

INSERT INTO Attendance (StudentId, ClassId, ScheduleId, AttendanceTime, Status)
VALUES
(1,4,@SCH_MATH201_20251008,'13:03',N'Có mặt'),
(2,4,@SCH_MATH201_20251008,'13:15',N'Muộn'),
(3,4,@SCH_MATH201_20251008,'13:02',N'Có mặt'),
(9,4,@SCH_MATH201_20251008,'13:25',N'Vắng'),
(10,4,@SCH_MATH201_20251008,'13:05',N'Có mặt'),
(11,4,@SCH_MATH201_20251008,'13:10',N'Có mặt');
GO

-----------------------
-- 2.10 GRADE SAMPLE
-----------------------
INSERT INTO Grade (StudentId, ClassId, AttendanceGrade, MidtermGrade, FinalGrade)
VALUES
(1,1,9.0, 8.0, 8.5),
(2,1,8.0, 7.0, 7.5),
(3,1,10.0,9.0, 8.5),
(4,1,6.0, 5.5, 6.0),
(5,1,9.5, 8.5, 9.0),
(6,1,8.5, 7.5, 8.0),
(7,1,7.0, 6.5, 7.0),
(8,1,9.0, 8.0, 8.0);

INSERT INTO Grade (StudentId, ClassId, AttendanceGrade, MidtermGrade, FinalGrade)
VALUES
(5,2,8.5, 7.5, 8.0),
(6,2,7.5, 7.0, 7.5),
(7,2,9.0, 8.5, 8.0),
(8,2,8.0, 7.5, 7.5),
(9,2,9.5, 8.5, 9.0),
(10,2,6.0,5.5, 6.0),
(11,2,8.5,7.5, 8.0),
(12,2,7.5,7.0, 7.0);

INSERT INTO Grade (StudentId, ClassId, AttendanceGrade, MidtermGrade, FinalGrade)
VALUES
(1,4,9.0, 8.5, 8.5),
(2,4,8.0, 7.5, 7.5),
(3,4,9.5, 9.0, 9.0),
(9,4,7.0, 6.5, 7.0),
(10,4,8.5,7.5, 8.0),
(11,4,9.0,8.5, 8.5);

INSERT INTO Grade (StudentId, ClassId, AttendanceGrade, MidtermGrade, FinalGrade)
VALUES
(12,5,8.5,7.5,8.0),
(13,5,9.0,8.5,9.0),
(14,5,7.5,7.0,7.0),
(15,5,8.0,7.5,8.0),
(16,5,9.5,9.0,9.0),
(17,5,7.0,6.5,7.0),
(18,5,8.5,8.0,8.0);
GO

-----------------------
-- 2.11 DEVICE SAMPLE (để ESP chạy được)
-----------------------
INSERT INTO Device (DeviceCode, DeviceName, Room, IsActive)
VALUES 
  (N'ESP_ROOM_LAB1', N'Thiết bị Lab 1', N'Phòng Lab 1', 1),
  (N'ESP_ROOM_LAB2', N'Thiết bị vân tay phòng Lab 2', N'LAB2', 1),
  (N'ESP_ROOM_A101', N'Thiết bị vân tay phòng A101', N'A101', 1);
GO

/*======================================================
= 3) KIỂM TRA NHANH
======================================================*/
SELECT COUNT(*) AS TotalTeachers FROM Teacher;
SELECT COUNT(*) AS TotalStudents FROM Student;
SELECT COUNT(*) AS ActiveClasses FROM Class WHERE Status = 1 AND IsDeleted = 0;

SELECT * FROM Class WHERE TeacherId = 1;

SELECT * FROM Grade g
JOIN Class c ON g.ClassId = c.ClassId
WHERE c.TeacherId = 1 AND c.Status = 1;

SELECT * FROM Attendance a
JOIN Class c ON a.ClassId = c.ClassId
WHERE c.TeacherId = 1 AND c.Status = 1;
GO
