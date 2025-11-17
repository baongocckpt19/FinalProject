/*======================================================
= 0) TẠO DATABASE
======================================================*/
Use master
IF DB_ID('StudentAttendanceDB') IS NOT NULL
BEGIN
	Use master
    ALTER DATABASE StudentAttendanceDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE StudentAttendanceDB;
END
CREATE DATABASE StudentAttendanceDB;
GO
USE StudentAttendanceDB;
GO

/*======================================================
= 1) BẢNG VAI TRÒ
======================================================*/
IF OBJECT_ID('Role') IS NOT NULL DROP TABLE Role;
CREATE TABLE Role (
    RoleId INT IDENTITY(1,1) PRIMARY KEY,
    RoleName NVARCHAR(50) NOT NULL -- Admin / Giảng viên / Học sinh
);
GO

/*======================================================
= 2) BẢNG TÀI KHOẢN ĐĂNG NHẬP
======================================================*/
IF OBJECT_ID('Account') IS NOT NULL DROP TABLE Account;
CREATE TABLE Account (
    AccountId INT IDENTITY(1,1) PRIMARY KEY,
    Username NVARCHAR(50) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    RoleId INT NOT NULL FOREIGN KEY REFERENCES Role(RoleId),
	isDeleted BIT DEFAULT 0 --0 hiện 1 ẩn

);
GO

/*======================================================
= 3) BẢNG GIẢNG VIÊN (ĐÃ CẬP NHẬT: Thêm DateOfBirth, Gender)
======================================================*/
IF OBJECT_ID('Teacher') IS NOT NULL DROP TABLE Teacher;
CREATE TABLE Teacher (
    TeacherId INT IDENTITY(1,1) PRIMARY KEY,
    AccountId INT NOT NULL FOREIGN KEY REFERENCES Account(AccountId),
    FullName NVARCHAR(100) NOT NULL,
    Email NVARCHAR(100),
    Phone NVARCHAR(20),
    Department NVARCHAR(100),
	Address NVARCHAR(200),
    DateOfBirth DATE NULL,
    Gender NVARCHAR(10) NULL, -- ĐÃ THÊM TRƯỜNG GIỚI TÍNH
    CONSTRAINT CHK_Teacher_Gender CHECK (Gender IN (N'Nam', N'Nữ', N'Khác', NULL)) -- Ràng buộc kiểm tra
);
GO



/*======================================================
= 4) BẢNG HỌC SINH (ĐÃ CẬP NHẬT: Thêm DateOfBirth, Gender)
======================================================*/
IF OBJECT_ID('Student') IS NOT NULL DROP TABLE Student;
CREATE TABLE Student (
    StudentId INT IDENTITY(1,1) PRIMARY KEY,
    AccountId INT NOT NULL FOREIGN KEY REFERENCES Account(AccountId),
    FullName NVARCHAR(100) NOT NULL,
    Email NVARCHAR(100),
    Phone NVARCHAR(20),
    Address NVARCHAR(200),
    DateOfBirth DATE NULL,
    Gender NVARCHAR(10) NULL, -- ĐÃ THÊM TRƯỜNG GIỚI TÍNH
    CONSTRAINT CHK_Student_Gender CHECK (Gender IN (N'Nam', N'Nữ', N'Khác', NULL)), -- Ràng buộc kiểm tra
);
GO


/*======================================================
= 5) BẢNG VÂN TAY
======================================================*/
IF OBJECT_ID('Fingerprint') IS NOT NULL DROP TABLE Fingerprint;
CREATE TABLE Fingerprint (
    FingerprintID INT IDENTITY(1,1) PRIMARY KEY,   -- ID từng vân tay
    StudentId INT NOT NULL FOREIGN KEY REFERENCES Student(StudentId),
    Fingerprint NVARCHAR(255) NOT NULL
);
GO

/*======================================================
= 5) BẢNG LỚP HỌC
======================================================*/
IF OBJECT_ID('Class') IS NOT NULL DROP TABLE Class;
CREATE TABLE Class (
    ClassId INT IDENTITY(1,1) PRIMARY KEY,
    ClassCode NVARCHAR(50) UNIQUE NOT NULL,
    ClassName NVARCHAR(100) NOT NULL,
    TeacherId INT FOREIGN KEY REFERENCES Teacher(TeacherId),
    CreatedDate DATETIME DEFAULT GETDATE(),
    Status NVARCHAR DEFAULT N'Đang hoạt động', -- Đang hoạt động /  Tạm dừng
    IsDeleted BIT DEFAULT 0 -- 0: hiển thị, 1: ẩn (đã xóa)
);
GO

/*======================================================
= 6) BẢNG SINH VIÊN - LỚP (QUAN HỆ NHIỀU-NHIỀU)
======================================================*/
IF OBJECT_ID('StudentClass') IS NOT NULL DROP TABLE StudentClass;
CREATE TABLE StudentClass (
    StudentId INT FOREIGN KEY REFERENCES Student(StudentId),
    ClassId INT FOREIGN KEY REFERENCES Class(ClassId),
    PRIMARY KEY (StudentId, ClassId)
);
GO

/*======================================================
= 7) BẢNG ĐIỂM DANH
======================================================*/
IF OBJECT_ID('Attendance') IS NOT NULL DROP TABLE Attendance;
CREATE TABLE Attendance (
    AttendanceId INT IDENTITY(1,1) PRIMARY KEY,
    StudentId INT FOREIGN KEY REFERENCES Student(StudentId),
    ClassId INT FOREIGN KEY REFERENCES Class(ClassId),
    AttendanceDate DATE NOT NULL,
    AttendanceTime TIME NOT NULL,
    SessionStart TIME NOT NULL, --thời điểm bắt đầu bật thiết bị điểm danh
    SessionEnd TIME NOT NULL,--thời điểm đóng thiết bị điểm danh
    Status NVARCHAR(50) NOT NULL -- Có mặt / Vắng 
);
GO

/*======================================================
= 8) BẢNG ĐIỂM SỐ
======================================================*/
IF OBJECT_ID('Grade') IS NOT NULL DROP TABLE Grade;
CREATE TABLE Grade (
    GradeId INT IDENTITY(1,1) PRIMARY KEY,
    StudentId INT FOREIGN KEY REFERENCES Student(StudentId),
    ClassId INT FOREIGN KEY REFERENCES Class(ClassId),
    AttendanceGrade FLOAT CHECK(AttendanceGrade BETWEEN 0 AND 10),
    MidtermGrade FLOAT CHECK(MidtermGrade BETWEEN 0 AND 10),
    FinalGrade FLOAT CHECK(FinalGrade BETWEEN 0 AND 10)
);
GO

/*======================================================
= 9) DỮ LIỆU MẪU (INSERT DEMO ĐÃ CẬP NHẬT)
======================================================*/

-- Vai trò
INSERT INTO Role (RoleName) VALUES (N'Admin'), (N'Giảng viên'), (N'Học sinh');

-- Admin
INSERT INTO Account (Username, PasswordHash, RoleId, isDeleted)
VALUES ('admin01', 'admin123', 1,0);

-- Giảng viên
INSERT INTO Account (Username, PasswordHash, RoleId, isDeleted)
VALUES ('teacher01', '12345', 2,0),
       ('teacher02', '12345', 2,0);

-- **CẬP NHẬT BẢNG TEACHER:** Thêm DateOfBirth và Gender
INSERT INTO Teacher (AccountId, FullName, Email, Phone, Department, Address, DateOfBirth, Gender)
VALUES (2, N'Nguyễn Văn A', 'vana@school.edu.vn', '0901234567', N'Công nghệ thông tin', N'Quận 1, TP.HCM', '1980-05-15', N'Nam'),
       (3, N'Lê Thị B', 'leb@school.edu.vn', '0909876543', N'Khoa Toán - Tin', N'Đống Đa, Hà Nội', '1975-11-20', N'Nữ');


-- Sinh viên
INSERT INTO Account (Username, PasswordHash, RoleId, isDeleted)
VALUES ('student01', '12345', 3,0),
       ('student02', '12345', 3,0),
       ('student03', '12345', 3,0);

-- **CẬP NHẬT BẢNG STUDENT:** Thêm DateOfBirth và Gender
INSERT INTO Student (AccountId, FullName, Email, Phone, Address, DateOfBirth, Gender)
VALUES (4, N'Phạm Minh C', 'minhc@student.edu.vn', '0901111111', N'Hà Nội', '2003-01-25', N'Nam'),
       (5, N'Trần Thị D', 'trand@student.edu.vn', '0902222222', N'Hải Phòng', '2004-07-10', N'Nữ'),
       (6, N'Ngô Văn E', 'ngoe@student.edu.vn', '0903333333', N'Đà Nẵng', '2003-11-05', N'Nam');


-- Lớp học
INSERT INTO Class (ClassCode, ClassName, TeacherId, Status)
VALUES ('CNTT101', N'Lập trình Web', 1, 0),
       ('MATH201', N'Toán rời rạc', 2, 0);

-- Sinh viên thuộc lớp
INSERT INTO StudentClass (StudentId, ClassId)
VALUES (1,1), (2,1), (3,2);

-- Điểm danh
INSERT INTO Attendance (StudentId, ClassId, AttendanceDate, AttendanceTime, SessionStart, SessionEnd, Status)
VALUES 
(1,1,'2025-10-01','07:55','07:45','09:15',N'Có mặt'),
(2,1,'2025-10-01','08:10','07:45','09:15',N'Muộn'),
(3,2,'2025-10-01','13:05','13:00','15:00',N'Có mặt');

-- Điểm số
INSERT INTO Grade (StudentId, ClassId, AttendanceGrade, MidtermGrade, FinalGrade)
VALUES 
(1,1,9,8,7),
(2,1,8,6.5,7.5),
(3,2,10,9,8);
GO
-- Lớp học
INSERT INTO Class (ClassCode, ClassName, TeacherId, Status)
VALUES ('CNTT103', N'học càng nhiều càng đơ', 1, 0),
       ('MATH203', N'lập trình hướng đối tượng ', 2, 1);

ALTER TABLE StudentClass
ADD IsDeleted BIT NOT NULL DEFAULT 0;
