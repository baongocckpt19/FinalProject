// src/main/java/com/FinalProject/backend/Repository/AttendanceRepository.java
package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    // 1) Tổng số lớp có điểm danh theo ngày (cho 1 giảng viên, trong khoảng start–end)
    @Query(value = """
        SELECT 
            a.AttendanceDate,                 -- 0
            COUNT(DISTINCT a.ClassId) AS ClassCount  -- 1
        FROM Attendance a
        JOIN Class c ON a.ClassId = c.ClassId
        WHERE c.TeacherId = ?1
          AND c.IsDeleted = 0
          AND a.AttendanceDate BETWEEN ?2 AND ?3
        GROUP BY a.AttendanceDate
        ORDER BY a.AttendanceDate
        """, nativeQuery = true)
    List<Object[]> findCalendarForTeacher(int teacherId, Date start, Date end);

    // 2) Thông tin điểm danh theo lớp trong 1 ngày (giảng viên + ngày)
    @Query(value = """
        SELECT 
            c.ClassId,                                     -- 0
            c.ClassCode,                                   -- 1
            c.ClassName,                                   -- 2
            MIN(a.SessionStart) AS SessionStart,           -- 3
            MAX(a.SessionEnd)   AS SessionEnd,             -- 4
            c.Status,                                      -- 5
            COUNT(*) AS Total,                             -- 6
            SUM(CASE WHEN a.Status = N'Có mặt' THEN 1 ELSE 0 END) AS Present, -- 7
            SUM(CASE WHEN a.Status = N'Vắng'   THEN 1 ELSE 0 END) AS Absent,  -- 8
            SUM(CASE WHEN a.Status = N'Muộn'   THEN 1 ELSE 0 END) AS Late     -- 9
        FROM Attendance a
        JOIN Class c ON a.ClassId = c.ClassId
        WHERE c.TeacherId = ?1
          AND c.IsDeleted = 0
          AND a.AttendanceDate = ?2
        GROUP BY c.ClassId, c.ClassCode, c.ClassName, c.Status
        ORDER BY c.ClassName
        """, nativeQuery = true)
    List<Object[]> findClassesForTeacherAndDate(int teacherId, Date date);
    // ... phần import, @Repository, interface, 2 query cũ ...

    // 3) DS sinh viên và trạng thái điểm danh của 1 lớp trong 1 ngày + thống kê theo lớp
    @Query(value = """
        SELECT 
            a.AttendanceId,       -- 0
            s.StudentId,          -- 1
            s.FullName,           -- 2
            acc.Username,         -- 3
            a.Status,             -- 4
            a.AttendanceTime,     -- 5

            -- Tổng số buổi của lớp
            (SELECT COUNT(DISTINCT AttendanceDate)
             FROM Attendance
             WHERE ClassId = ?1) AS TotalSessions,             -- 6

            -- Số buổi Có mặt của SV trong lớp
            (SELECT COUNT(*)
             FROM Attendance
             WHERE ClassId = ?1
               AND StudentId = s.StudentId
               AND Status = N'Có mặt') AS PresentSessions,    -- 7

            -- Số buổi Muộn của SV trong lớp
            (SELECT COUNT(*)
             FROM Attendance
             WHERE ClassId = ?1
               AND StudentId = s.StudentId
               AND Status = N'Muộn') AS LateSessions,         -- 8

            -- Số buổi Vắng của SV trong lớp
            (SELECT COUNT(*)
             FROM Attendance
             WHERE ClassId = ?1
               AND StudentId = s.StudentId
               AND Status = N'Vắng') AS AbsentSessions        -- 9
        FROM Attendance a
        JOIN Student s ON a.StudentId = s.StudentId
        JOIN Account acc ON s.AccountId = acc.AccountId
        WHERE a.ClassId = ?1
          AND a.AttendanceDate = ?2
        ORDER BY s.FullName
        """, nativeQuery = true)
    List<Object[]> findStudentAttendanceForClassAndDate(int classId, Date date);

    // 4) Lịch sử điểm danh của 1 sinh viên trong 1 lớp
    @Query(value = """
        SELECT 
            a.AttendanceId,        -- 0
            a.AttendanceDate,      -- 1
            a.SessionStart,        -- 2
            a.SessionEnd,          -- 3
            a.Status,              -- 4
            a.AttendanceTime       -- 5
        FROM Attendance a
        WHERE a.ClassId = ?1
          AND a.StudentId = ?2
        ORDER BY a.AttendanceDate DESC, a.SessionStart
        """, nativeQuery = true)
    List<Object[]> findAttendanceHistoryForStudentInClass(int classId, int studentId);

    // 5) Tổng số buổi của lớp (distinct ngày)
    @Query(value = """
        SELECT COUNT(DISTINCT AttendanceDate)
        FROM Attendance
        WHERE ClassId = ?1
        """, nativeQuery = true)
    Integer countTotalSessionsForClass(int classId);

    // 6) Cập nhật trạng thái 1 bản ghi điểm danh
    @org.springframework.data.jpa.repository.Modifying
    @Query(value = """
        UPDATE Attendance
        SET Status = ?2
        WHERE AttendanceId = ?1
        """, nativeQuery = true)
    void updateAttendanceStatus(int attendanceId, String newStatus);
}


