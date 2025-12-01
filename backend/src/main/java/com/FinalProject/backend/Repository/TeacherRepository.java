//Đây là TeacherRepository.java
package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
    // Lấy thông tin giáo viên ngắn gọn theo TeacherId
    @Query(value = "SELECT TeacherId, FullName, TeacherCode FROM Teacher WHERE TeacherId = ?1", nativeQuery = true)
    Object findTeacherShortById(int teacherId);

//Lấy TeacherId theo AccountId (tài khoản đang đăng nhập)
    @Query(value = "SELECT TeacherId FROM Teacher WHERE AccountId = ?1", nativeQuery = true)
    Integer findTeacherIdByAccountId(int accountId);


    // ⭐ THÊM MỚI: Lấy TeacherId theo Username
    @Query(value = """
        SELECT t.TeacherId
        FROM Teacher t
        JOIN Account a ON t.AccountId = a.AccountId
        WHERE a.Username = ?1
        """, nativeQuery = true)
    Integer findTeacherIdByUsername(String username);

    // 👇 THÊM MỚI: tìm theo mã GV
    Optional<Teacher> findByTeacherCode(String teacherCode);
}
