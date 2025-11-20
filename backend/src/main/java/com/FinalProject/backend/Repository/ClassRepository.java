// src/main/java/com/FinalProject/backend/Repository/ClassRepository.java
package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.Clazz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<Clazz, Integer> {

    // ====== 1) BẢNG LỚP + ĐẾM SỐ SV VÀ SỐ SV CÓ VÂN TAY ======
    @Query(value = """
    SELECT 
        c.ClassId,                                          -- 0
        c.ClassCode,                                        -- 1
        c.ClassName,                                        -- 2
        t.FullName AS TeacherName,                          -- 3
        (
            SELECT COUNT(*) 
            FROM StudentClass sc 
            WHERE sc.ClassId = c.ClassId
              AND sc.IsDeleted = 0                          -- 👈 CHỈ LẤY SV CÒN TRONG LỚP
        ) AS StudentCount,                                  -- 4
        CONVERT(varchar(19), c.CreatedDate, 120) AS CreatedDate, -- 5
        c.Status,                                           -- 6
        (
            SELECT COUNT(DISTINCT s.StudentId)
            FROM StudentClass sc
            JOIN Student s ON sc.StudentId = s.StudentId
            JOIN Fingerprint f ON f.StudentId = s.StudentId
            WHERE sc.ClassId = c.ClassId
              AND sc.IsDeleted = 0                          -- 👈 CHỈ ĐẾM SV CÒN TRONG LỚP
        ) AS FingerprintedCount                             -- 7
    FROM Class c
    LEFT JOIN Teacher t ON t.TeacherId = c.TeacherId
    WHERE c.IsDeleted = 0
    ORDER BY c.ClassId
    """, nativeQuery = true)
    List<Object[]> findAllClassTable();

    // ====== 2) DANH SÁCH SV CHO MODAL (ĐANG XEM LỚP) ======
    @Query(value = """
    SELECT 
        s.StudentId,                           -- 0
        s.FullName,                            -- 1
        a.Username,                            -- 2
        s.Email,                               -- 3
        COUNT(f.FingerprintID) AS FingerCount  -- 4
    FROM StudentClass sc
    JOIN Student s ON sc.StudentId = s.StudentId
    JOIN Account a ON s.AccountId = a.AccountId
    LEFT JOIN Fingerprint f ON f.StudentId = s.StudentId
    WHERE sc.ClassId = ?1
      AND sc.IsDeleted = 0                     -- 👈 CHỈ LẤY SV CÒN TRONG LỚP
    GROUP BY s.StudentId, s.FullName, a.Username, s.Email
    ORDER BY s.StudentId
    """, nativeQuery = true)
    List<Object[]> findStudentsForClassModal(int classId);


    // ====== 3) SOFT DELETE CLASS ======
    @Modifying
    @Query(value = "UPDATE Class SET IsDeleted = 1 WHERE ClassId = ?1", nativeQuery = true)
    void softDeleteClass(int classId);

    // ====== 4) THÔNG TIN LỚP (EXPORT HEADER) ======
    @Query(value = """
        SELECT 
            c.ClassId,
            c.ClassCode,
            c.ClassName,
            t.FullName AS TeacherName,
            CAST( (SELECT COUNT(*) 
                   FROM StudentClass sc 
                   WHERE sc.ClassId = c.ClassId
                     AND sc.IsDeleted = 0) AS INT ) AS StudentCount,
            CONVERT(varchar(19), c.CreatedDate, 120) AS CreatedDate,
            CAST(c.Status AS INT) AS Status
        FROM Class c
        LEFT JOIN Teacher t ON t.TeacherId = c.TeacherId
        WHERE c.ClassId = ?1 AND c.IsDeleted = 0
        """, nativeQuery = true)
    Object findClassInfoById(int classId);

    // ====== 5) LẤY DS SV THEO LỚP (EXPORT CSV) ======
    @Query(value = """
    SELECT 
        s.StudentId,
        s.FullName,
        a.Username,
        CONVERT(varchar(10), s.DateOfBirth, 23) AS DateOfBirth,
        s.Gender,
        s.Address,
        s.Email,
        s.Phone,
        ISNULL(COUNT(f.FingerprintID), 0) AS FingerCount
    FROM StudentClass sc
    JOIN Student s ON sc.StudentId = s.StudentId
    JOIN Account a ON s.AccountId = a.AccountId
    LEFT JOIN Fingerprint f ON f.StudentId = s.StudentId
    WHERE sc.ClassId = ?1
      AND sc.IsDeleted = 0                     -- 👈 CHỈ LẤY SV CÒN TRONG LỚP
    GROUP BY 
        s.StudentId,
        s.FullName,
        a.Username,
        s.DateOfBirth,
        s.Gender,
        s.Address,
        s.Email,
        s.Phone
    ORDER BY s.StudentId
    """, nativeQuery = true)
    List<Object[]> findStudentsByClassId(int classId);

    // ====== 6) CHI TIẾT LỚP (MODAL EDIT) ======
    @Query(value = """
        SELECT 
            c.ClassId,
            c.ClassCode,
            c.ClassName,
            c.TeacherId,
            t.FullName AS TeacherName,
            CONVERT(varchar(19), c.CreatedDate, 120) AS CreatedDate,
            CAST(c.Status AS INT) AS Status
        FROM Class c
        LEFT JOIN Teacher t ON t.TeacherId = c.TeacherId
        WHERE c.ClassId = ?1 AND c.IsDeleted = 0
        """, nativeQuery = true)
    Object findClassDetailById(int classId);

    // ====== 7) UPDATE LỚP ======
    @Modifying
    @Query(value = """
        UPDATE Class
        SET ClassCode = ?2,
            ClassName = ?3,
            TeacherId = ?4
        WHERE ClassId = ?1
        """, nativeQuery = true)
    void updateClass(int classId, String classCode, String className, Integer teacherId);

    // ====== 8) THÊM HỌC SINH VÀO LỚP (SOFT) ======
    @Modifying
    @Query(value = """
        IF EXISTS (
            SELECT 1 FROM StudentClass 
            WHERE StudentId = ?1 AND ClassId = ?2
        )
            UPDATE StudentClass
            SET IsDeleted = 0                 -- 👈 NẾU TỪNG XOÁ THÌ KHÔI PHỤC
            WHERE StudentId = ?1 AND ClassId = ?2;
        ELSE
            INSERT INTO StudentClass (StudentId, ClassId, IsDeleted)
            VALUES (?1, ?2, 0);               -- 👈 THÊM MỚI
        """, nativeQuery = true)
    void addStudentToClass(int studentId, int classId);

    // ====== 9) XOÁ (SOFT) 1 HỌC SINH KHỎI LỚP ======
    @Modifying
    @Query(value = """
        UPDATE StudentClass
        SET IsDeleted = 1
        WHERE StudentId = ?1 AND ClassId = ?2
        """, nativeQuery = true)
    void removeStudentFromClass(int studentId, int classId);


    // ====== 10) CẬP NHẬT TRẠNG THÁI LỚP ======
    @Modifying
    @Query(value = """
    UPDATE Class
    SET Status = ?2
    WHERE ClassId = ?1
    """, nativeQuery = true)
    void updateClassStatus(int classId, boolean newStatus);



    //========================================================//
    //======================== THỐNG KÊ CHO GIẢNG VIÊN =======================//
    //========================================================//


    // ====== 11) TỔNG SỐ SINH VIÊN CỦA GIẢNG VIÊN ======
    @Query(value = """
    SELECT COUNT(DISTINCT sc.StudentId)
    FROM Class c
    JOIN StudentClass sc ON sc.ClassId = c.ClassId
    WHERE c.TeacherId = ?1
      AND c.IsDeleted = 0
      AND c.Status = 0
      AND sc.IsDeleted = 0
    """, nativeQuery = true)
    Integer countStudentsForTeacher(int teacherId);


    // ====== 12) SỐ LỚP ĐANG DẠY (ĐANG HOẠT ĐỘNG) ======
    @Query(value = """
    SELECT COUNT(*)
    FROM Class c
    WHERE c.TeacherId = ?1
      AND c.IsDeleted = 0
      AND c.Status = 0          
    """, nativeQuery = true)
    Integer countActiveClassesForTeacher(int teacherId);
    // ====== 13) ĐIỂM TRUNG BÌNH TẤT CẢ SV CỦA GIẢNG VIÊN ======
    @Query(value = """
    SELECT AVG(0.25 * g.AttendanceGrade
             + 0.25 * g.MidtermGrade
             + 0.5  * g.FinalGrade)
    FROM Grade g
    JOIN Class c ON g.ClassId = c.ClassId
    WHERE c.TeacherId = ?1
      AND c.IsDeleted = 0
      AND c.Status = 0         
    """, nativeQuery = true)
    Double averageScoreForTeacher(int teacherId);

    // ====== 14) TỶ LỆ ĐIỂM DANH CỦA GIẢNG VIÊN ======
    @Query(value = """
    SELECT
        CASE WHEN COUNT(*) = 0 THEN 0.0
             ELSE 100.0 * SUM(CASE WHEN a.Status = N'Có mặt' THEN 1 ELSE 0 END) / COUNT(*)
        END
    FROM Attendance a
    JOIN Class c ON a.ClassId = c.ClassId
    WHERE c.TeacherId = ?1
      AND c.IsDeleted = 0
      AND c.Status = 0          -- 👈 LỚP ĐANG HOẠT ĐỘNG
    """, nativeQuery = true)
    Double attendanceRateForTeacher(int teacherId);


    // ====== 15) BẢNG LỚP CỦA 1 GIẢNG VIÊN + ĐẾM SỐ SV VÀ SỐ SV CÓ VÂN TAY (GIAO DIỆN QUẢN LÝ LỚP HỌC OF GV) ======
    @Query(value = """
    SELECT 
        c.ClassId,                                          -- 0
        c.ClassCode,                                        -- 1
        c.ClassName,                                        -- 2
        t.FullName AS TeacherName,                          -- 3
        (
            SELECT COUNT(*)
            FROM StudentClass sc
            WHERE sc.ClassId = c.ClassId
              AND sc.IsDeleted = 0
        ) AS StudentCount,                                  -- 4
        CONVERT(varchar(19), c.CreatedDate, 120) AS CreatedDate, -- 5
        c.Status,                                           -- 6
        (
            SELECT COUNT(DISTINCT s.StudentId)
            FROM StudentClass sc
            JOIN Student s ON sc.StudentId = s.StudentId
            JOIN Fingerprint f ON f.StudentId = s.StudentId
            WHERE sc.ClassId = c.ClassId
              AND sc.IsDeleted = 0
        ) AS FingerprintedCount                             -- 7
    FROM Class c
    LEFT JOIN Teacher t ON t.TeacherId = c.TeacherId
    WHERE c.IsDeleted = 0
      AND c.TeacherId = ?1
    ORDER BY c.ClassId
    """, nativeQuery = true)
    List<Object[]> findClassTableForTeacher(int teacherId);






}
