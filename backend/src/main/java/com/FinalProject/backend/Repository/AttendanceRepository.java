// src/main/java/com/FinalProject/backend/Repository/AttendanceRepository.java
package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {


    @Query(value = """
    SELECT 
        c.ClassId,              -- 0
        cs.ScheduleId,          -- 1
        s.StudentId,            -- 2
        s.FullName,             -- 3
        acc.Username,           -- 4
        s.Email,                -- 5   ⭐ THÊM
        s.Phone,                -- 6   ⭐ THÊM
        a.AttendanceId,         -- 7
        a.Status,               -- 8  -- "Có mặt" / "Vắng" / "Muộn" / NULL
        a.AttendanceTime        -- 9
    FROM ClassSchedule cs
    JOIN Class c ON cs.ClassId = c.ClassId
    JOIN StudentClass sc 
         ON sc.ClassId = c.ClassId 
        AND sc.IsDeleted = 0
    JOIN Student s ON sc.StudentId = s.StudentId
    JOIN Account acc ON s.AccountId = acc.AccountId
    LEFT JOIN Attendance a
           ON a.StudentId = s.StudentId
          AND a.ScheduleId = cs.ScheduleId
    WHERE cs.ScheduleId = :scheduleId
    ORDER BY s.FullName
    """, nativeQuery = true)
    List<Object[]> findClassAttendanceBySchedule(@Param("scheduleId") Integer scheduleId);

    Attendance findByStudentIdAndScheduleId(Integer studentId, Integer scheduleId);




    @Query(value = """
    SELECT
        cs.ScheduleId,      -- 0
        cs.ScheduleDate,    -- 1
        cs.StartTime,       -- 2
        cs.EndTime,         -- 3
        a.Status,           -- 4  -- "Có mặt"/"Vắng"/"Muộn"
        a.AttendanceTime    -- 5
    FROM ClassSchedule cs
    JOIN Attendance a
           ON a.ScheduleId = cs.ScheduleId
          AND a.StudentId = :studentId
    WHERE cs.ClassId = :classId
      AND cs.IsActive = 1
      AND cs.ScheduleDate <= CAST(GETDATE() AS date)   -- ⭐ chỉ buổi đã qua (ngày < hôm nay)
    ORDER BY cs.ScheduleDate, cs.StartTime
    """, nativeQuery = true)
    List<Object[]> findStudentHistoryByClass(
            @Param("classId") Integer classId,
            @Param("studentId") Integer studentId
    );

}
