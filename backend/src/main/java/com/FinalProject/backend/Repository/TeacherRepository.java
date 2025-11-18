package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
    // Lấy thông tin giáo viên ngắn gọn theo TeacherId
    @Query(value = "SELECT TeacherId, FullName FROM Teacher WHERE TeacherId = ?1", nativeQuery = true)
    Object findTeacherShortById(int teacherId);

    //  Lấy TeacherId theo AccountId (tài khoản đang đăng nhập)
    @Query(value = "SELECT TeacherId FROM Teacher WHERE AccountId = ?1", nativeQuery = true)
    Integer findTeacherIdByAccountId(int accountId);
}
