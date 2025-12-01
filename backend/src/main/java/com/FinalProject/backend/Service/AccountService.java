//Đây là AccountService.java
package com.FinalProject.backend.Service;
import com.FinalProject.backend.Models.PasswordResetToken;
import com.FinalProject.backend.Repository.PasswordResetTokenRepository;

import java.time.LocalDateTime;
import java.util.Random;

import com.FinalProject.backend.Dto.AccountDto;
import com.FinalProject.backend.Dto.ChangePasswordRequest;
import com.FinalProject.backend.Dto.RegisterRequest;
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

        dto.setUserCode((String) row[i++]);      // 7 - userCode

        dto.setPhone((String) row[i++]);         // 8 - phone
        dto.setAddress((String) row[i++]);       // 9 - address
        dto.setDateOfBirth((String) row[i++]);   // 10 - yyyy-MM-dd
        dto.setGender((String) row[i++]);        // 11 - gender

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
    // 5. CẬP NHẬT THÔNG TIN CÁ NHÂN + MÃ SỐ
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
        String roleName   = (String) row[2]; // "Admin" / "Giảng viên" / "Học sinh" ...

        String userCode = req.getUserCode(); // có thể null

        // ==== CASE 1: đã có Student -> UPDATE ====
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

            // ⭐ Cập nhật mã số nếu có gửi lên (FE có thể khóa không cho đổi sau này)
            if (userCode != null && !userCode.isEmpty()) {
                student.setStudentCode(userCode);
            }

            studentRepository.save(student);
            return;
        }

        // ==== CASE 2: đã có Teacher -> UPDATE ====
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

            if (userCode != null && !userCode.isEmpty()) {
                teacher.setTeacherCode(userCode);
            }

            teacherRepository.save(teacher);
            return;
        }

        // CASE 3: Chưa có Student/Teacher -> tạo mới + gán mã số
        String rn = roleName != null ? roleName.trim() : "";

        if (rn.equalsIgnoreCase("Học sinh") || rn.equalsIgnoreCase("Student")) {
            Student student = new Student();
            student.setAccountId(accountId);
            student.setFullName(req.getFullName() != null ? req.getFullName() : "Chưa cập nhật");

            if (userCode != null && !userCode.isEmpty()) {
                student.setStudentCode(userCode);
            }

            studentRepository.save(student);
            return;
        }

        if (rn.equalsIgnoreCase("Giảng viên") || rn.equalsIgnoreCase("Teacher")) {
            Teacher teacher = new Teacher();
            teacher.setAccountId(accountId);
            teacher.setFullName(req.getFullName() != null ? req.getFullName() : "Chưa cập nhật");

            if (userCode != null && !userCode.isEmpty()) {
                teacher.setTeacherCode(userCode);
            }

            teacherRepository.save(teacher);
            return;
        }

        throw new RuntimeException("Role không hỗ trợ tạo hồ sơ");
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
    // 7. CHECK CURRENT PASSWORD
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
        return stored.equals(currentPassword);
    }

    // =========================================================
    // 8. ĐĂNG KÝ: CHỈ TẠO ACCOUNT
    // =========================================================
    @Transactional
    public void register(RegisterRequest req) {
        if (req.getUsername() == null || req.getUsername().isEmpty()) {
            throw new RuntimeException("Username không được để trống");
        }
        if (req.getPassword() == null || req.getPassword().isEmpty()) {
            throw new RuntimeException("Mật khẩu không được để trống");
        }

        if (accountRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Tài khoản đã tồn tại");
        }

        int roleId;
        if ("teacher".equalsIgnoreCase(req.getRole())) {
            roleId = 2;
        } else if ("student".equalsIgnoreCase(req.getRole())) {
            roleId = 3;
        } else if ("admin".equalsIgnoreCase(req.getRole())) {
            roleId = 1;
        } else {
            throw new RuntimeException("Role không hợp lệ");
        }

        Account acc = new Account();
        acc.setUsername(req.getUsername());
        acc.setPasswordHash(req.getPassword());
        acc.setRoleId(roleId);
        accountRepository.save(acc);
    }


    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    // ===== Helper tạo mã 6 số =====
    private String generateResetCode() {
        Random random = new Random();
        int num = 100000 + random.nextInt(900000); // 6 chữ số
        return String.valueOf(num);
    }
    // =========================================================
    // 9. QUÊN MẬT KHẨU - GỬI MÃ RESET QUA EMAIL
    // =========================================================
    @Transactional
    public void requestPasswordReset(String username) {
        if (username == null || username.isEmpty()) {
            throw new RuntimeException("Username không được để trống");
        }

        Account acc = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với username: " + username));

        // Lấy thông tin user (gồm email) bằng getUser(accountId)
        AccountDto dto = getUser(acc.getAccountId());
        if (dto == null || dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new RuntimeException("Tài khoản này chưa cập nhật email, không thể gửi mã.");
        }

        String email = dto.getEmail();

        // Tạo mã và lưu vào bảng PasswordResetToken
        String code = generateResetCode();

        PasswordResetToken token = new PasswordResetToken();
        token.setAccountId(acc.getAccountId());
        token.setCode(code);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // hạn 10 phút
        token.setUsed(false);

        passwordResetTokenRepository.save(token);

        // Gửi mail
        String subject = "[EduSmart] Mã xác nhận đặt lại mật khẩu";
        String body = "Xin chào " + (dto.getFullName() != null ? dto.getFullName() : username) + ",\n\n"
                + "Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản EduSmart.\n"
                + "Mã xác nhận của bạn là: " + code + "\n\n"
                + "Mã này có hiệu lực trong 10 phút.\n\n"
                + "Nếu bạn không yêu cầu thao tác này, vui lòng bỏ qua email.\n\n"
                + "Trân trọng,\nEduSmart";

        emailService.sendSimpleEmail(email, subject, body);
    }
    // =========================================================
    // 10. QUÊN MẬT KHẨU - XÁC NHẬN MÃ + ĐỔI MẬT KHẨU
    // =========================================================
    @Transactional
    public void resetPasswordWithCode(String username, String code, String newPassword) {
        if (username == null || username.isEmpty()) {
            throw new RuntimeException("Username không được để trống");
        }
        if (code == null || code.isEmpty()) {
            throw new RuntimeException("Mã xác nhận không được để trống");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new RuntimeException("Mật khẩu mới không được để trống");
        }

        Account acc = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với username: " + username));

        // Tìm token trong DB
        PasswordResetToken token = passwordResetTokenRepository
                .findByAccountIdAndCodeAndIsUsedFalse(acc.getAccountId(), code)
                .orElseThrow(() -> new RuntimeException("Mã xác nhận không hợp lệ."));

        // Check hết hạn
        if (LocalDateTime.now().isAfter(token.getExpiryDate())) {
            token.setUsed(true);
            passwordResetTokenRepository.save(token);
            throw new RuntimeException("Mã xác nhận đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        // OK -> đổi mật khẩu
        acc.setPasswordHash(newPassword);
        accountRepository.save(acc);

        // Đánh dấu token đã dùng
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
    }

}
