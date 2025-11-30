package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.AccountDto;
import com.FinalProject.backend.Dto.ChangePasswordRequest;
import com.FinalProject.backend.Dto.UpdateProfileRequest;
import com.FinalProject.backend.Models.Account;
import com.FinalProject.backend.Models.Student;
import com.FinalProject.backend.Models.Teacher;
import com.FinalProject.backend.Repository.AccountRepository;
import com.FinalProject.backend.Repository.StudentRepository;
import com.FinalProject.backend.Repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    // =========================================================
    // 1. LOGIN (dùng cho /api/auth/login)
    // =========================================================
    public AccountDto login(String username, String password) {
        Object result = accountRepository.login(username, password);
        if (result == null) {
            return null;
        }
        Object[] user = (Object[]) result;
        AccountDto dto = new AccountDto();
        int i = 0;
        dto.setAccountId((Integer) user[i++]);   // 0
        dto.setUsername((String) user[i++]);     // 1
        dto.setPassword((String) user[i++]);     // 2 - PasswordHash (plain hiện tại)
        dto.setRoleId((Integer) user[i++]);      // 3
        dto.setRoleName((String) user[i++]);     // 4
        dto.setFullName((String) user[i++]);     // 5
        return dto;
    }

    // =========================================================
    // 2. LẤY THÔNG TIN NGƯỜI DÙNG HIỆN TẠI
    // =========================================================
    public AccountDto getUser(int accountId) {
        Object result = accountRepository.findUserDetailByAccountId(accountId);
        if (result == null) {
            return null;
        }
        Object[] row = (Object[]) result;
        int i = 0;

        AccountDto dto = new AccountDto();
        dto.setAccountId((Integer) row[i++]);    // 0 - accountId
        dto.setUsername((String) row[i++]);      // 1 - username
        dto.setRoleName((String) row[i++]);      // 2 - roleName

        dto.setFullName((String) row[i++]);      // 3 - fullName
        dto.setEmail((String) row[i++]);         // 4 - email

        dto.setTeacherId((Integer) row[i++]);    // 5 - teacherId
        dto.setStudentId((Integer) row[i++]);    // 6 - studentId

        dto.setPhone((String) row[i++]);         // 7 - phone
        dto.setAddress((String) row[i++]);       // 8 - address
        dto.setDateOfBirth((String) row[i++]);   // 9 - yyyy-MM-dd
        dto.setGender((String) row[i++]);        // 10 - gender

        return dto;
    }

    // =========================================================
    // 3. SOFT DELETE TÀI KHOẢN
    // =========================================================
    @Transactional
    public void softDeleteAccount(int id) {
        accountRepository.softDeleteAccount(id);
    }

    // =========================================================
    // 4. LẤY TEACHER / STUDENT ID THEO USERNAME
    // =========================================================

    public Integer getTeacherIdByUsername(String username) {
        Integer teacherId = teacherRepository.findTeacherIdByUsername(username);
        if (teacherId == null) {
            throw new RuntimeException("Không tìm thấy Teacher cho username: " + username);
        }
        return teacherId;
    }

    public Integer getStudentIdByUsername(String username) {
        Integer studentId = accountRepository.findStudentIdByUsername(username);
        if (studentId == null) {
            throw new RuntimeException("Không tìm thấy Student cho username: " + username);
        }
        return studentId;
    }

    // =========================================================
    // 5. CẬP NHẬT THÔNG TIN CÁ NHÂN
    // =========================================================
    @Transactional
    public void updateProfile(int accountId, UpdateProfileRequest req) {
        Object result = accountRepository.findUserDetailByAccountId(accountId);
        if (result == null) {
            throw new RuntimeException("Không tìm thấy tài khoản với id = " + accountId);
        }
        Object[] row = (Object[]) result;
        Integer teacherId = (Integer) row[5];
        Integer studentId = (Integer) row[6];

        if (studentId != null) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Student với id = " + studentId));

            student.setFullName(req.getFullName());
            student.setEmail(req.getEmail());
            student.setPhone(req.getPhone());
            student.setAddress(req.getAddress());

            if (req.getBirthDate() != null && !req.getBirthDate().isEmpty()) {
                student.setDateOfBirth(java.time.LocalDate.parse(req.getBirthDate()));
            } else {
                student.setDateOfBirth(null);
            }

            student.setGender(req.getGender());
            studentRepository.save(student);
            return;
        }

        if (teacherId != null) {
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Teacher với id = " + teacherId));

            teacher.setFullName(req.getFullName());
            teacher.setEmail(req.getEmail());
            teacher.setPhone(req.getPhone());
            teacher.setAddress(req.getAddress());

            if (req.getBirthDate() != null && !req.getBirthDate().isEmpty()) {
                teacher.setDateOfBirth(java.time.LocalDate.parse(req.getBirthDate()));
            } else {
                teacher.setDateOfBirth(null);
            }

            teacher.setGender(req.getGender());
            teacherRepository.save(teacher);
            return;
        }

        throw new RuntimeException("Tài khoản không gắn với Student hoặc Teacher, không thể cập nhật hồ sơ.");
    }

    // =========================================================
    // 6. ĐỔI MẬT KHẨU
    // =========================================================
    @Transactional
    public void changePassword(int accountId, ChangePasswordRequest req) {
        Account account = accountRepository.getReferenceById(accountId);

        String currentHash = account.getPasswordHash();
        if (currentHash == null || !currentHash.equals(req.getCurrentPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng.");
        }

        account.setPasswordHash(req.getNewPassword());
    }

    // =========================================================
    // 7. CHECK CURRENT PASSWORD (cho POST /api/account/check-password)
    // =========================================================
    @Transactional(readOnly = true)
    public boolean checkCurrentPassword(int accountId, String currentPassword) {
        if (currentPassword == null || currentPassword.isEmpty()) {
            return false;
        }

        Account account = accountRepository.getReferenceById(accountId);
        String stored = account.getPasswordHash();
        if (stored == null) {
            return false;
        }

        // Hiện tại là plain-text, so sánh trực tiếp:
        return stored.equals(currentPassword);

        // Nếu sau này bạn dùng PasswordEncoder:
        // return passwordEncoder.matches(currentPassword + passwordSecret, stored);
    }
}
