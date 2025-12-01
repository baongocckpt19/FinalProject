// src/main/java/com/FinalProject/backend/Repository/AttendanceRepository.java
package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository làm việc với bảng Attendance.
 *
 * Lưu ý:
 *  - Đang dùng cả JPA mặc định (findById, save, ...) và query native để join nhiều bảng.
 */
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    /**
     * Lấy danh sách điểm danh của 1 buổi học (ScheduleId) theo từng sinh viên trong lớp đó.
     *
     * Ý tưởng:
     *  - Từ ClassSchedule (cs) JOIN sang Class (c) để biết buổi học thuộc lớp nào.
     *  - JOIN StudentClass (sc) để lấy danh sách sinh viên thuộc lớp đó (đang active: IsDeleted = 0).
     *  - JOIN Student (s) + Account (acc) để lấy thông tin sinh viên.
     *  - LEFT JOIN Attendance (a) để lấy trạng thái điểm danh của từng sinh viên trong buổi này
     *    (nếu chưa có bản ghi Attendance thì a = NULL).
     *
     * Kết quả trả về mỗi Object[] gồm:
     *  - [0] Integer  : ClassId
     *  - [1] Integer  : ScheduleId
     *  - [2] Integer  : StudentId
     *  - [3] String   : FullName
     *  - [4] String   : Username
     *  - [5] String   : Email
     *  - [6] String   : Phone
     *  - [7] Integer? : AttendanceId (có thể null nếu chưa điểm danh)
     *  - [8] String   : Status ("Có mặt" / "Vắng" / "Muộn" / NULL)
     *  - [9] Time     : AttendanceTime (giờ điểm danh)
     */
    @Query(value = """
    SELECT 
        c.ClassId,              -- 0
        cs.ScheduleId,          -- 1
        s.StudentId,            -- 2
        s.StudentCode,          -- 3 
        s.FullName,             -- 3
        acc.Username,           -- 4
        s.Email,                -- 5   -- THÊM THÔNG TIN SINH VIÊN
        s.Phone,                -- 6   -- THÊM THÔNG TIN SINH VIÊN
        a.AttendanceId,         -- 7
        a.Status,               -- 8   -- "Có mặt" / "Vắng" / "Muộn" / NULL
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

    /**
     * Tìm 1 bản ghi Attendance cụ thể theo StudentId + ScheduleId.
     * Dùng khi cần upsert (cập nhật nếu tồn tại, tạo mới nếu không).
     */
    Attendance findByStudentIdAndScheduleId(Integer studentId, Integer scheduleId);

    /**
     * Lấy lịch sử điểm danh của 1 sinh viên trong 1 lớp (ClassId).
     *
     * Ý tưởng:
     *  - Lấy danh sách tất cả buổi học (ClassSchedule) của lớp đó.
     *  - JOIN Attendance để biết trạng thái điểm danh của sinh viên trong từng buổi.
     *  - Lọc IsActive = 1 (lịch còn hiệu lực) và ScheduleDate <= hôm nay
     *    để chỉ lấy các buổi đã/đang diễn ra (không lấy tương lai).
     *
     * Kết quả mỗi Object[] gồm:
     *  - [0] Integer : ScheduleId
     *  - [1] Date    : ScheduleDate
     *  - [2] Time    : StartTime
     *  - [3] Time    : EndTime
     *  - [4] String  : Status ("Có mặt"/"Vắng"/"Muộn")
     *  - [5] Time    : AttendanceTime (giờ điểm danh)
     */
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
      AND cs.ScheduleDate <= CAST(GETDATE() AS date)   -- chỉ buổi đã qua (ngày <= hôm nay)
    ORDER BY cs.ScheduleDate, cs.StartTime
    """, nativeQuery = true)
    List<Object[]> findStudentHistoryByClass(
            @Param("classId") Integer classId,
            @Param("studentId") Integer studentId
    );

    /**
     * Thống kê tổng số buổi Có mặt / Vắng / Đi muộn của 1 sinh viên.
     *
     * Dùng cho:
     *  - Thẻ "Số buổi vắng"
     *  - Thẻ "Tỉ lệ điểm danh"
     *  - Biểu đồ tròn trên trang chủ sinh viên.
     *
     * Kết quả Object[]:
     *  - [0] Long : số buổi Có mặt
     *  - [1] Long : số buổi Vắng
     *  - [2] Long : số buổi Đi muộn
     */
    @Query(value = """
    SELECT 
        SUM(CASE WHEN a.Status = N'Có mặt' THEN 1 ELSE 0 END) AS PresentCount,
        SUM(CASE WHEN a.Status = N'Vắng'   THEN 1 ELSE 0 END) AS AbsentCount,
        SUM(CASE WHEN a.Status = N'Muộn'   THEN 1 ELSE 0 END) AS LateCount
    FROM Attendance a
    WHERE a.StudentId = :studentId
    """, nativeQuery = true)
    List<Object[]> summarizeAttendanceByStudent(@Param("studentId") Integer studentId);


}
