package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {

    // Lấy thông tin sinh viên + username để hiển thị trong modal
    @Query(value = """
        SELECT 
            s.StudentId,
            s.FullName,
            a.Username,
            CONVERT(varchar(10), s.DateOfBirth, 23) AS DateOfBirth,
            s.Gender,
            s.Address,
            s.Email,
            s.Phone
        FROM Student s
        JOIN Account a ON s.AccountId = a.AccountId
        WHERE s.StudentId = ?1
        """, nativeQuery = true)
    Object findStudentInfoById(int studentId);

    // Tìm StudentId dựa theo Username (MSSV)
    @Query(value = """
        SELECT s.StudentId
        FROM Student s
        JOIN Account a ON s.AccountId = a.AccountId
        WHERE a.Username = ?1
        """, nativeQuery = true)
    Integer findStudentIdByUsername(String username);
    /**
     * Tìm sinh viên tương ứng với 1 tài khoản (AccountId).
     *  - Bảng Student có cột AccountId (FK sang Account).
     */
    Student findByAccountId(Integer accountId);
}
