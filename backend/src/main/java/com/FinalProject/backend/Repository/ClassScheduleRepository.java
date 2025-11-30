package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Integer> {

    /**
     * Lịch dạy của giảng viên trong khoảng tháng (dùng cho TeacherSchedule)
     * ĐÃ LỌC IsDeleted = 0
     */
    @Query(value = """
    SELECT
        cs.ScheduleId,          -- 0
        cs.ClassId,             -- 1
        c.ClassCode,            -- 2
        c.ClassName,            -- 3
        cs.ScheduleDate,        -- 4
        cs.StartTime,           -- 5
        cs.EndTime,             -- 6
        cs.Room,                -- 7
        cs.IsActive,            -- 8
        COUNT(sc.StudentId) AS StudentCount   -- 9
    FROM ClassSchedule cs
    JOIN Class c ON cs.ClassId = c.ClassId
    LEFT JOIN StudentClass sc 
        ON sc.ClassId = c.ClassId AND sc.IsDeleted = 0
    WHERE c.TeacherId = :teacherId
      AND cs.ScheduleDate BETWEEN :startDate AND :endDate
      AND cs.IsDeleted = 0                      -- ⭐ chỉ lấy lịch chưa bị xóa
    GROUP BY cs.ScheduleId, cs.ClassId, c.ClassCode, c.ClassName,
             cs.ScheduleDate, cs.StartTime, cs.EndTime, cs.Room, cs.IsActive
    ORDER BY cs.ScheduleDate, cs.StartTime
    """, nativeQuery = true)
    List<Object[]> findScheduleOfTeacherBetween(
            @Param("teacherId") Integer teacherId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**

     * - sc: để filter chỉ những lớp mà sinh viên đang học
     * - scAll: để đếm tổng số sinh viên trong lớp (StudentCount)
     */
    /**
     * Lịch học của sinh viên trong khoảng tháng (dùng cho /api/student/schedules)
     * ĐÃ LỌC IsDeleted = 0
     */
    /**
     * Lịch học của sinh viên trong khoảng tháng (dùng cho /api/student/schedules)
     * ĐÃ LỌC IsDeleted = 0
     */
    @Query(value = """
    SELECT
        cs.ScheduleId,          -- 0
        cs.ClassId,             -- 1
        c.ClassCode,            -- 2
        c.ClassName,            -- 3
        cs.ScheduleDate,        -- 4
        cs.StartTime,           -- 5
        cs.EndTime,             -- 6
        cs.Room,                -- 7
        cs.IsActive,            -- 8
        COUNT(sc2.StudentId) AS StudentCount   -- 9 tổng số SV trong lớp
    FROM Student s
    JOIN Account acc ON s.AccountId = acc.AccountId
    JOIN StudentClass sc ON sc.StudentId = s.StudentId AND sc.IsDeleted = 0
    JOIN ClassSchedule cs ON cs.ClassId = sc.ClassId AND cs.IsDeleted = 0
    JOIN Class c ON cs.ClassId = c.ClassId
    LEFT JOIN StudentClass sc2
           ON sc2.ClassId = c.ClassId
          AND sc2.IsDeleted = 0
    WHERE s.StudentId = :studentId
      AND cs.ScheduleDate BETWEEN :startDate AND :endDate
    GROUP BY cs.ScheduleId, cs.ClassId, c.ClassCode, c.ClassName,
             cs.ScheduleDate, cs.StartTime, cs.EndTime, cs.Room, cs.IsActive
    ORDER BY cs.ScheduleDate, cs.StartTime
    """, nativeQuery = true)
    List<Object[]> findScheduleOfStudentBetween(
            @Param("studentId") Integer studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    /**
     * Lấy lịch học theo ClassId (dùng cho Admin modal Lịch học)
     * ĐÃ LỌC IsDeleted = 0
     */
    List<ClassSchedule> findByClazz_ClassIdAndIsDeletedFalse(Integer classId);

    /**
     * Tìm một lịch học theo id, chỉ lấy nếu chưa bị xóa
     */
    Optional<ClassSchedule> findByScheduleIdAndIsDeletedFalse(Integer scheduleId);

    /**
     * Soft delete: set IsDeleted = 1
     */
    @Modifying
    @Query("UPDATE ClassSchedule cs SET cs.isDeleted = true WHERE cs.scheduleId = :id")
    void softDelete(@Param("id") Integer id);

    // tìm lịch theo Room + ngày (IsActive, chưa xóa)
    List<ClassSchedule> findByRoomAndScheduleDateAndIsActiveTrueAndIsDeletedFalse(
            String room,
            LocalDate scheduleDate
    );

    /**
     * Các buổi học đã kết thúc (dùng cho auto Vắng)
     * Dùng native query + CAST(:currentTime AS time) để tránh lỗi
     * "The data types time and datetime are incompatible in the less than or equal to operator."
     */
    @Query(value = """
        SELECT *
        FROM ClassSchedule cs
        WHERE cs.IsDeleted = 0
          AND cs.IsActive = 1
          AND (
                cs.ScheduleDate < :today
             OR (cs.ScheduleDate = :today AND cs.EndTime <= CAST(:currentTime AS time))
          )
        """, nativeQuery = true)
    List<ClassSchedule> findFinishedSchedules(
            @Param("today") LocalDate today,
            @Param("currentTime") String currentTime
    );


}
