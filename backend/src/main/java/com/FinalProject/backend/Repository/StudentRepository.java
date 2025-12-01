//Đây là StudentRepository.java
package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {



    // Lấy thông tin sinh viên + username để hiển thị trong modal

    @Query(value = """
    SELECT 
        s.StudentId,                                             -- 0
        s.StudentCode,                                           -- 1  👈 THÊM
        s.FullName,                                              -- 2
        a.Username,                                              -- 3
        CONVERT(varchar(10), s.DateOfBirth, 23) AS DateOfBirth,  -- 4
        s.Gender,                                                -- 5
        s.Address,                                               -- 6
        s.Email,                                                 -- 7
        s.Phone                                                  -- 8
    FROM Student s
    JOIN Account a ON s.AccountId = a.AccountId
    WHERE s.StudentId = ?1
    """, nativeQuery = true)
    Object findStudentInfoById(int studentId);



//    // Tìm StudentId từ Username (Account.Username)
//    @Query(value = """
//        SELECT s.StudentId
//        FROM Student s
//        JOIN Account a ON s.AccountId = a.AccountId
//        WHERE a.Username = :username
//        """, nativeQuery = true)
//    Integer findStudentIdByUsername(String username);
    // MỚI: lấy info theo studentCode (MSSV) – dùng cho API /students/by-code/{studentCode}
    @Query(value = """
        SELECT 
            s.StudentId,        -- 0
            s.StudentCode,      -- 1
            s.FullName,         -- 2
            a.Username,         -- 3
            s.DateOfBirth,      -- 4
            s.Gender,           -- 5
            s.Address,          -- 6
            s.Email,            -- 7
            s.Phone             -- 8
        FROM Student s
        JOIN Account a ON s.AccountId = a.AccountId
        WHERE s.StudentCode = :studentCode
    """, nativeQuery = true)
    Object findStudentInfoByCode(@Param("studentCode") String studentCode);
    /**
     * Tìm sinh viên tương ứng với 1 tài khoản (AccountId).
     *  - Bảng Student có cột AccountId (FK sang Account).
     */
    Student findByAccountId(Integer accountId);

    // Tìm entity theo MSSV (StudentCode) – dùng cho import
    Optional<Student> findByStudentCode(String studentCode);

}
