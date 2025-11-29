// src/main/java/com/FinalProject/backend/Repository/GradeRepository.java
package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository làm việc với bảng Grade (Điểm).
 *
 * Gồm:
 *  - Query lấy danh sách điểm theo lớp (cho màn hình GV nhập điểm).
 *  - Upsert điểm cho 1 sinh viên trong 1 lớp.
 *  - Các query hỗ trợ Dashboard sinh viên (GPA, tỉ lệ qua môn, top 6 lớp...).
 */
@Repository
public interface GradeRepository extends JpaRepository<Grade, Integer> {

    /**
     * Lấy danh sách điểm của toàn bộ sinh viên trong 1 lớp.
     *
     * Ý tưởng:
     *  - Từ StudentClass (sc) JOIN sang Student (s) + Account (a) để lấy thông tin sinh viên.
     *  - LEFT JOIN Grade (g) để lấy điểm nếu đã có, nếu chưa có thì g = NULL.
     *  - Chỉ lấy những bản ghi StudentClass chưa xóa (IsDeleted = 0).
     *
     * Kết quả Object[]:
     *  - [0] Integer : StudentId
     *  - [1] String  : FullName
     *  - [2] String  : Username
     *  - [3] Double? : AttendanceGrade
     *  - [4] Double? : MidtermGrade
     *  - [5] Double? : FinalGrade
     */
    @Query(value = """
    SELECT 
        s.StudentId,           -- 0
        s.FullName,            -- 1
        a.Username,            -- 2
        g.AttendanceGrade,     -- 3
        g.MidtermGrade,        -- 4
        g.FinalGrade           -- 5
    FROM StudentClass sc
    JOIN Student s ON sc.StudentId = s.StudentId
    JOIN Account a ON s.AccountId = a.AccountId
    LEFT JOIN Grade g 
           ON g.StudentId = s.StudentId 
          AND g.ClassId   = sc.ClassId
    WHERE sc.ClassId = ?1
      AND sc.IsDeleted = 0
    ORDER BY s.FullName
    """, nativeQuery = true)
    List<Object[]> findGradesByClassId(int classId);

    /**
     * Upsert điểm của 1 sinh viên trong 1 lớp:
     *  - Nếu đã tồn tại dòng Grade(StudentId, ClassId) thì UPDATE.
     *  - Nếu chưa có thì INSERT mới.
     *
     * Thích hợp dùng cho màn giảng viên nhập/sửa điểm.
     */
    @Modifying
    @Query(value = """
        IF EXISTS (SELECT 1 FROM Grade WHERE StudentId = ?1 AND ClassId = ?2)
            UPDATE Grade
            SET AttendanceGrade = ?3,
                MidtermGrade    = ?4,
                FinalGrade      = ?5
            WHERE StudentId = ?1 AND ClassId = ?2;
        ELSE
            INSERT INTO Grade (StudentId, ClassId, AttendanceGrade, MidtermGrade, FinalGrade)
            VALUES (?1, ?2, ?3, ?4, ?5);
        """, nativeQuery = true)
    void upsertGrade(int studentId,
                     int classId,
                     Double attendanceGrade,
                     Double midtermGrade,
                     Double finalGrade);

    // ================== PHẦN DƯỚI: HỖ TRỢ DASHBOARD SINH VIÊN ==================

    // Tính điểm trung bình thang 10 cho 1 sinh viên (đã áp dụng 25/25/50)
    @Query(value = """
    SELECT AVG(
        COALESCE(g.AttendanceGrade, 0) * 0.25 +
        COALESCE(g.MidtermGrade,    0) * 0.25 +
        COALESCE(g.FinalGrade,      0) * 0.50
    )
    FROM Grade g
    WHERE g.StudentId = ?1
    """, nativeQuery = true)
    Double calcAverageGpa10(Integer studentId);

    // Lấy tối đa 6 lớp + điểm trung bình 25/25/50 cho từng lớp (dùng cho biểu đồ cột)
    @Query(value = """
    SELECT 
        c.ClassId,                                                -- 0
        c.ClassCode,                                              -- 1
        c.ClassName,                                              -- 2
        (COALESCE(g.AttendanceGrade, 0) * 0.25 +
         COALESCE(g.MidtermGrade,    0) * 0.25 +
         COALESCE(g.FinalGrade,      0) * 0.50) AS Average10      -- 3
    FROM StudentClass sc
    JOIN Class c ON sc.ClassId = c.ClassId
    LEFT JOIN Grade g
           ON g.StudentId = sc.StudentId
          AND g.ClassId   = sc.ClassId
    WHERE sc.StudentId = ?1
      AND sc.IsDeleted = 0
    ORDER BY c.ClassName
    OFFSET 0 ROWS FETCH NEXT 6 ROWS ONLY
    """, nativeQuery = true)
    List<Object[]> findTop6GradesByStudent(Integer studentId);

    // Tổng số môn
    @Query(value = """
    SELECT COUNT(*)
    FROM Grade g
    WHERE g.StudentId = ?1
    """, nativeQuery = true)
    Long countAllSubjects(Integer studentId);

    // Số môn qua (ví dụ điểm TB >= 5 được coi là qua)
    @Query(value = """
    SELECT COUNT(*)
    FROM Grade g
    WHERE g.StudentId = ?1
      AND (COALESCE(g.AttendanceGrade, 0) * 0.25 +
           COALESCE(g.MidtermGrade,    0) * 0.25 +
           COALESCE(g.FinalGrade,      0) * 0.50) >= 5
    """, nativeQuery = true)
    Long countPassedSubjects(Integer studentId);
    /**
     * Lấy danh sách lớp + 3 đầu điểm của 1 sinh viên.
     *  - Chỉ lấy lớp chưa bị IsDeleted = 0
     *  - map: 0 = đang học, 1 = đã hoàn thành
     *
     * Cột:
     *  [0] ClassId
     *  [1] ClassCode
     *  [2] ClassName
     *  [3] Status (bit)
     *  [4] TeacherName
     *  [5] AttendanceGrade
     *  [6] MidtermGrade
     *  [7] FinalGrade
     */
    @Query(value = """
        SELECT 
            c.ClassId,              -- 0
            c.ClassCode,            -- 1
            c.ClassName,            -- 2
            c.Status,               -- 3
            t.FullName AS TeacherName, -- 4
            g.AttendanceGrade,      -- 5
            g.MidtermGrade,         -- 6
            g.FinalGrade            -- 7
        FROM StudentClass sc
        JOIN Class c ON sc.ClassId = c.ClassId
        LEFT JOIN Teacher t ON t.TeacherId = c.TeacherId
        LEFT JOIN Grade g 
               ON g.ClassId   = sc.ClassId
              AND g.StudentId = sc.StudentId
        WHERE sc.StudentId = ?1
          AND sc.IsDeleted = 0
          AND c.IsDeleted  = 0
        ORDER BY c.CreatedDate DESC, c.ClassName
        """, nativeQuery = true)
    List<Object[]> findClassGradesForStudent(Integer studentId);

}
