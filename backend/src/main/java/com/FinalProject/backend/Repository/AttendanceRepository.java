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
}
