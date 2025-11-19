/*======================================================
= 0) TẠO DATABASE
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
= 1) BẢNG VAI TRÒ
======================================================*/
IF OBJECT_ID('Role') IS NOT NULL DROP TABLE Role;
CREATE TABLE Role (
    RoleId   INT IDENTITY(1,1) PRIMARY KEY,
    RoleName NVARCHAR(50) NOT NULL -- Admin / Giảng viên / Học sinh
);
GO

/*======================================================
= 2) BẢNG TÀI KHOẢN ĐĂNG NHẬP
======================================================*/
IF OBJECT_ID('Account') IS NOT NULL DROP TABLE Account;
CREATE TABLE Account (
    AccountId    INT IDENTITY(1,1) PRIMARY KEY,
    Username     NVARCHAR(50)  NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    RoleId       INT           NOT NULL FOREIGN KEY REFERENCES Role(RoleId),
    IsDeleted    BIT           NOT NULL DEFAULT 0 -- 0 hiển thị, 1 ẩn
);
GO

/*======================================================
= 3) BẢNG GIẢNG VIÊN
======================================================*/
IF OBJECT_ID('Teacher') IS NOT NULL DROP TABLE Teacher;
CREATE TABLE Teacher (
    TeacherId  INT IDENTITY(1,1) PRIMARY KEY,
    AccountId  INT NOT NULL FOREIGN KEY REFERENCES Account(AccountId),
    FullName   NVARCHAR(100) NOT NULL,
    Email      NVARCHAR(100),
    Phone      NVARCHAR(20),
    Department NVARCHAR(100),
    Address    NVARCHAR(200),
    DateOfBirth DATE NULL,
    Gender     NVARCHAR(10) NULL,
    CONSTRAINT CHK_Teacher_Gender CHECK (Gender IN (N'Nam', N'Nữ', N'Khác', NULL))
);
GO

/*======================================================
= 4) BẢNG HỌC SINH
======================================================*/
IF OBJECT_ID('Student') IS NOT NULL DROP TABLE Student;
CREATE TABLE Student (
    StudentId  INT IDENTITY(1,1) PRIMARY KEY,
    AccountId  INT NOT NULL FOREIGN KEY REFERENCES Account(AccountId),
    FullName   NVARCHAR(100) NOT NULL,
    Email      NVARCHAR(100),
    Phone      NVARCHAR(20),
    Address    NVARCHAR(200),
    DateOfBirth DATE NULL,
    Gender     NVARCHAR(10) NULL,
    CONSTRAINT CHK_Student_Gender CHECK (Gender IN (N'Nam', N'Nữ', N'Khác', NULL))
);
GO

/*======================================================
= 5) BẢNG VÂN TAY
======================================================*/
IF OBJECT_ID('Fingerprint') IS NOT NULL DROP TABLE Fingerprint;
CREATE TABLE Fingerprint (
    FingerprintID INT IDENTITY(1,1) PRIMARY KEY,   -- ID từng vân tay
    StudentId     INT NOT NULL FOREIGN KEY REFERENCES Student(StudentId),
    Fingerprint   NVARCHAR(255) NOT NULL
);
GO

/*======================================================
= 6) BẢNG LỚP HỌC
======================================================*/
IF OBJECT_ID('Class') IS NOT NULL DROP TABLE Class;
CREATE TABLE Class (
    ClassId     INT IDENTITY(1,1) PRIMARY KEY,
    ClassCode   NVARCHAR(50) UNIQUE NOT NULL,
    ClassName   NVARCHAR(100) NOT NULL,
    TeacherId   INT FOREIGN KEY REFERENCES Teacher(TeacherId),
    CreatedDate DATETIME NOT NULL DEFAULT GETDATE(),
    Status      BIT NOT NULL DEFAULT 1,  -- 1 = Đang hoạt động, 0 = Tạm dừng
    IsDeleted   BIT NOT NULL DEFAULT 0   -- 0: hiển thị, 1: ẩn (đã xóa)
);
GO

/*======================================================
= 7) BẢNG SINH VIÊN - LỚP (N-N)
======================================================*/
IF OBJECT_ID('StudentClass') IS NOT NULL DROP TABLE StudentClass;
CREATE TABLE StudentClass (
    StudentId INT NOT NULL FOREIGN KEY REFERENCES Student(StudentId),
    ClassId   INT NOT NULL FOREIGN KEY REFERENCES Class(ClassId),
    IsDeleted BIT NOT NULL DEFAULT 0,
    CONSTRAINT PK_StudentClass PRIMARY KEY (StudentId, ClassId)
);
GO

/*======================================================
= 8) BẢNG ĐIỂM DANH
======================================================*/
IF OBJECT_ID('Attendance') IS NOT NULL DROP TABLE Attendance;
CREATE TABLE Attendance (
    AttendanceId   INT IDENTITY(1,1) PRIMARY KEY,
    StudentId      INT NOT NULL FOREIGN KEY REFERENCES Student(StudentId),
    ClassId        INT NOT NULL FOREIGN KEY REFERENCES Class(ClassId),
    AttendanceDate DATE NOT NULL,
    AttendanceTime TIME NOT NULL,
    SessionStart   TIME NOT NULL,
    SessionEnd     TIME NOT NULL,
    Status         NVARCHAR(50) NOT NULL -- Có mặt / Vắng / Muộn ...
);
GO

/*======================================================
= 9) BẢNG ĐIỂM SỐ
======================================================*/
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

/*======================================================
= 10) DỮ LIỆU MẪU
======================================================*/

-----------------------
-- 10.1 ROLE
-----------------------
INSERT INTO Role (RoleName)
VALUES (N'Admin'), (N'Giảng viên'), (N'Học sinh');
-- RoleId: 1=Admin, 2=Giảng viên, 3=Học sinh

-----------------------
-- 10.2 ACCOUNT
-----------------------
-- Admin
INSERT INTO Account (Username, PasswordHash, RoleId, IsDeleted)
VALUES ('admin01', 'admin123', 1, 0);   -- AccountId = 1

-- 6 Giảng viên
INSERT INTO Account (Username, PasswordHash, RoleId, IsDeleted)
VALUES 
('teacher01', '12345', 2, 0),   -- AccountId = 2
('teacher02', '12345', 2, 0),   -- 3
('teacher03', '12345', 2, 0),   -- 4
('teacher04', '12345', 2, 0),   -- 5
('teacher05', '12345', 2, 0),   -- 6
('teacher06', '12345', 2, 0);   -- 7

-- 24 Sinh viên
INSERT INTO Account (Username, PasswordHash, RoleId, IsDeleted)
VALUES
('student01', '12345', 3, 0),   -- 8
('student02', '12345', 3, 0),   -- 9
('student03', '12345', 3, 0),   -- 10
('student04', '12345', 3, 0),   -- 11
('student05', '12345', 3, 0),   -- 12
('student06', '12345', 3, 0),   -- 13
('student07', '12345', 3, 0),   -- 14
('student08', '12345', 3, 0),   -- 15
('student09', '12345', 3, 0),   -- 16
('student10', '12345', 3, 0),   -- 17
('student11', '12345', 3, 0),   -- 18
('student12', '12345', 3, 0),   -- 19
('student13', '12345', 3, 0),   -- 20
('student14', '12345', 3, 0),   -- 21
('student15', '12345', 3, 0),   -- 22
('student16', '12345', 3, 0),   -- 23
('student17', '12345', 3, 0),   -- 24
('student18', '12345', 3, 0),   -- 25
('student19', '12345', 3, 0),   -- 26
('student20', '12345', 3, 0),   -- 27
('student21', '12345', 3, 0),   -- 28
('student22', '12345', 3, 0),   -- 29
('student23', '12345', 3, 0),   -- 30
('student24', '12345', 3, 0);   -- 31
GO

-----------------------
-- 10.3 TEACHER  (dùng AccountId 2..7)
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
-- TeacherId: 1..6 theo thứ tự trên

-----------------------
-- 10.4 STUDENT  (dùng AccountId 8..31)
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
-- StudentId = 1..24 theo thứ tự trên

-----------------------
-- 10.5 CLASS  (sử dụng TeacherId 1..5)
-----------------------
INSERT INTO Class (ClassCode, ClassName, TeacherId, Status)
VALUES
('CNTT101', N'Lập trình Web',                         1, 1), -- hoạt động
('CNTT102', N'Cơ sở dữ liệu',                         1, 1), -- hoạt động
('CNTT201', N'Kiến trúc máy tính',                    1, 0), -- tạm dừng
('MATH201', N'Toán rời rạc',                          2, 1),
('MATH202', N'Xác suất thống kê',                     2, 1),
('PHY101',  N'Vật lý đại cương',                      3, 1),
('ENG101',  N'Tiếng Anh cơ bản',                      4, 1),
('AI301',   N'Nhập môn Trí tuệ nhân tạo',             5, 0);
GO
-- ClassId = 1..8
/*======================================================
= StudentClass - Gán sinh viên vào lớp
======================================================*/

-- CNTT101: SV 1..8
INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(1,1,0),(2,1,0),(3,1,0),(4,1,0),(5,1,0),(6,1,0),(7,1,0),(8,1,0);

-- CNTT102: SV 5..12
INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(5,2,0),(6,2,0),(7,2,0),(8,2,0),(9,2,0),(10,2,0),(11,2,0),(12,2,0);

-- CNTT201: SV 9..14
INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(9,3,0),(10,3,0),(11,3,0),(12,3,0),(13,3,0),(14,3,0);

-- MATH201: SV 1,2,3 và 9,10,11
INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(1,4,0),(2,4,0),(3,4,0),(9,4,0),(10,4,0),(11,4,0);

-- MATH202: SV 12..18
INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(12,5,0),(13,5,0),(14,5,0),(15,5,0),(16,5,0),(17,5,0),(18,5,0);

-- PHY101: SV 15..20
INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(15,6,0),(16,6,0),(17,6,0),(18,6,0),(19,6,0),(20,6,0);

-- ENG101: SV 18..24
INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(18,7,0),(19,7,0),(20,7,0),(21,7,0),(22,7,0),(23,7,0),(24,7,0);

-- AI301: SV mang tính nâng cao
INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
VALUES
(3,8,0),(4,8,0),(7,8,0),(8,8,0),(11,8,0),(14,8,0),(17,8,0),(20,8,0);


-----------------------
-- 10.7 FINGERPRINT  (một số SV có vân tay)
-----------------------
INSERT INTO Fingerprint (StudentId, Fingerprint)
VALUES
(1,  N'FPT-TEMPLATE-0001'),
(2,  N'FPT-TEMPLATE-0002'),
(3,  N'FPT-TEMPLATE-0003'),
(4,  N'FPT-TEMPLATE-0004'),
(5,  N'FPT-TEMPLATE-0005'),
(6,  N'FPT-TEMPLATE-0006'),
(7,  N'FPT-TEMPLATE-0007'),
(8,  N'FPT-TEMPLATE-0008'),
(10, N'FPT-TEMPLATE-0010'),
(12, N'FPT-TEMPLATE-0012'),
(15, N'FPT-TEMPLATE-0015'),
(18, N'FPT-TEMPLATE-0018'),
(20, N'FPT-TEMPLATE-0020'),
(22, N'FPT-TEMPLATE-0022'),
(24, N'FPT-TEMPLATE-0024');
GO

-----------------------
-- 10.8 ATTENDANCE  (điểm danh nhiều buổi cho vài lớp)
-----------------------
-- Ngày 2025-10-01: CNTT101 (ClassId = 1)
INSERT INTO Attendance (StudentId, ClassId, AttendanceDate, AttendanceTime, SessionStart, SessionEnd, Status)
VALUES
(1,1,'2025-10-01','07:55','07:45','09:15',N'Có mặt'),
(2,1,'2025-10-01','08:10','07:45','09:15',N'Muộn'),
(3,1,'2025-10-01','07:50','07:45','09:15',N'Có mặt'),
(4,1,'2025-10-01','08:30','07:45','09:15',N'Vắng'),
(5,1,'2025-10-01','07:47','07:45','09:15',N'Có mặt'),
(6,1,'2025-10-01','07:59','07:45','09:15',N'Có mặt'),
(7,1,'2025-10-01','08:20','07:45','09:15',N'Muộn'),
(8,1,'2025-10-01','08:05','07:45','09:15',N'Có mặt');

-- Ngày 2025-10-08: CNTT101
INSERT INTO Attendance (StudentId, ClassId, AttendanceDate, AttendanceTime, SessionStart, SessionEnd, Status)
VALUES
(1,1,'2025-10-08','07:53','07:45','09:15',N'Có mặt'),
(2,1,'2025-10-08','08:12','07:45','09:15',N'Muộn'),
(3,1,'2025-10-08','07:49','07:45','09:15',N'Có mặt'),
(4,1,'2025-10-08','08:35','07:45','09:15',N'Vắng'),
(5,1,'2025-10-08','07:46','07:45','09:15',N'Có mặt'),
(6,1,'2025-10-08','07:58','07:45','09:15',N'Có mặt'),
(7,1,'2025-10-08','08:18','07:45','09:15',N'Muộn'),
(8,1,'2025-10-08','08:03','07:45','09:15',N'Có mặt');

-- Ngày 2025-10-01: CNTT102 (ClassId = 2)
INSERT INTO Attendance (StudentId, ClassId, AttendanceDate, AttendanceTime, SessionStart, SessionEnd, Status)
VALUES
(5,2,'2025-10-01','10:05','10:00','11:30',N'Có mặt'),
(6,2,'2025-10-01','10:15','10:00','11:30',N'Muộn'),
(7,2,'2025-10-01','10:02','10:00','11:30',N'Có mặt'),
(8,2,'2025-10-01','10:20','10:00','11:30',N'Muộn'),
(9,2,'2025-10-01','10:01','10:00','11:30',N'Có mặt'),
(10,2,'2025-10-01','10:25','10:00','11:30',N'Vắng'),
(11,2,'2025-10-01','10:08','10:00','11:30',N'Có mặt'),
(12,2,'2025-10-01','10:12','10:00','11:30',N'Muộn');

-- Ngày 2025-10-08: MATH201 (ClassId = 4)
INSERT INTO Attendance (StudentId, ClassId, AttendanceDate, AttendanceTime, SessionStart, SessionEnd, Status)
VALUES
(1,4,'2025-10-08','13:03','13:00','14:30',N'Có mặt'),
(2,4,'2025-10-08','13:15','13:00','14:30',N'Muộn'),
(3,4,'2025-10-08','13:02','13:00','14:30',N'Có mặt'),
(9,4,'2025-10-08','13:25','13:00','14:30',N'Vắng'),
(10,4,'2025-10-08','13:05','13:00','14:30',N'Có mặt'),
(11,4,'2025-10-08','13:10','13:00','14:30',N'Có mặt');
GO

-----------------------
-- 10.9 GRADE  (điểm số cho một số lớp quan trọng)
-----------------------
-- CNTT101 (ClassId = 1)
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

-- CNTT102 (ClassId = 2)
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

-- MATH201 (ClassId = 4)
INSERT INTO Grade (StudentId, ClassId, AttendanceGrade, MidtermGrade, FinalGrade)
VALUES
(1,4,9.0, 8.5, 8.5),
(2,4,8.0, 7.5, 7.5),
(3,4,9.5, 9.0, 9.0),
(9,4,7.0, 6.5, 7.0),
(10,4,8.5,7.5, 8.0),
(11,4,9.0,8.5, 8.5);

-- MATH202 (ClassId = 5)
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

/*======================================================
= 11) KIỂM TRA NHANH
======================================================*/
-- Tổng GV
SELECT COUNT(*) AS TotalTeachers FROM Teacher;

-- Tổng SV
SELECT COUNT(*) AS TotalStudents FROM Student;

-- Tổng lớp đang hoạt động
SELECT COUNT(*) AS ActiveClasses FROM Class WHERE Status = 1 AND IsDeleted = 0;

-- Dữ liệu mẫu cho dashboard teacher01 (TeacherId = 1)
SELECT * FROM Class WHERE TeacherId = 1;

SELECT * FROM Grade g
JOIN Class c ON g.ClassId = c.ClassId
WHERE c.TeacherId = 1 AND c.Status = 1;

SELECT * FROM Attendance a
JOIN Class c ON a.ClassId = c.ClassId
WHERE c.TeacherId = 1 AND c.Status = 1;
