package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Integer> {

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


    // Upsert điểm 1 sinh viên trong 1 lớp
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
}
