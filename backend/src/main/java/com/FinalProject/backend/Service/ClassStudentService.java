package com.FinalProject.backend.Service;

import com.FinalProject.backend.Repository.ClassRepository;
import com.FinalProject.backend.Repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ClassStudentService {

    private static final Pattern BOM = Pattern.compile("^\uFEFF");

    private final ClassRepository classRepository;
    private final StudentRepository studentRepository;

    public ClassStudentService(ClassRepository classRepository,
                               StudentRepository studentRepository) {
        this.classRepository = classRepository;
        this.studentRepository = studentRepository;
    }

    // =========================================================
    // 1. LẤY THÔNG TIN SINH VIÊN THEO studentId
    // =========================================================
    public Map<String, Object> getStudentInfo(int studentId) {
        Object r = studentRepository.findStudentInfoById(studentId);
        if (r == null) return null;

        return convertStudentInfoRow(r);
    }

    // =========================================================
    // 2. LẤY THÔNG TIN SINH VIÊN THEO studentCode (MSSV)
    //     -> dùng cho API /students/by-code/{studentCode}
    // =========================================================
    public Map<String, Object> getStudentInfoByCode(String studentCode) {
        Object r = studentRepository.findStudentInfoByCode(studentCode);
        if (r == null) return null;

        Object[] o = (Object[]) r;
        int i = 0;
        Map<String, Object> map = new HashMap<>();
        map.put("studentId",   o[i++]);
        map.put("studentCode", o[i++]);
        map.put("fullName",    o[i++]);
        map.put("username",    o[i++]);
        map.put("dateOfBirth", o[i++]);
        map.put("gender",      o[i++]);
        map.put("address",     o[i++]);
        map.put("email",       o[i++]);
        map.put("phone",       o[i++]);
        return map;
    }


    // Hàm dùng chung để map Object[] -> Map
    private Map<String, Object> convertStudentInfoRow(Object row) {
        Object[] o = (Object[]) row;
        int i = 0;
        Map<String, Object> map = new HashMap<>();
        map.put("studentId",   o[i++]); // 0
        map.put("studentCode", o[i++]); // 1  👈 MÃ SỐ SINH VIÊN
        map.put("fullName",    o[i++]); // 2
        map.put("username",    o[i++]); // 3
        map.put("dateOfBirth", o[i++]); // 4
        map.put("gender",      o[i++]); // 5
        map.put("address",     o[i++]); // 6
        map.put("email",       o[i++]); // 7
        map.put("phone",       o[i++]); // 8
        return map;
    }

    // =========================================================
    // 3. THÊM NHIỀU SINH VIÊN VÀO LỚP
    // =========================================================
    @Transactional
    public void addStudentsToClass(int classId, List<Integer> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) return;
        for (Integer sid : studentIds) {
            if (sid != null) {
                classRepository.addStudentToClass(sid, classId);
            }
        }
    }

    // =========================================================
    // 4. IMPORT CSV THEO MSSV (StudentCode)
    //    Cột đầu tiên trong file là MÃ SỐ SINH VIÊN
    // =========================================================
    @Transactional
    public Map<String, Object> importStudentsFromCsv(int classId, MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File CSV rỗng");
        }

        List<Integer> studentIds = new ArrayList<>();
        List<Map<String, String>> rejected = new ArrayList<>();
        int total = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                total++;

                // Bỏ BOM ở dòng đầu
                if (first) {
                    line = BOM.matcher(line).replaceFirst("");
                    first = false;
                }

                if (line.trim().isEmpty()) continue;

                // Cho phép ',', ';' hoặc '\t'
                String[] parts = splitSmart(line);
                if (parts.length < 1) {
                    pushReject(rejected, total, line, "Thiếu cột MSSV");
                    continue;
                }

                String studentCode = parts[0].trim();
                if (studentCode.isEmpty()) {
                    pushReject(rejected, total, line, "Cột MSSV trống");
                    continue;
                }

                // Nếu đây là dòng header (ví dụ: "Mã SV" hoặc "MaSV") thì bỏ qua
                if (total == 1 && studentCode.toLowerCase().contains("mã")) {
                    // không tính là lỗi, chỉ skip
                    continue;
                }

                // Tìm theo studentCode (MSSV)
                var opt = studentRepository.findByStudentCode(studentCode);
                if (opt.isEmpty()) {
                    pushReject(rejected, total, line,
                            "Không tìm thấy sinh viên với MSSV: " + studentCode);
                    continue;
                }

                Integer studentId = opt.get().getStudentId();
                studentIds.add(studentId);
            }
        }

        // Ghi vào DB
        this.addStudentsToClass(classId, studentIds);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Import CSV hoàn tất");
        result.put("imported", studentIds.size());
        result.put("rejectedCount", rejected.size());
        result.put("rejectedRows", rejected);
        return result;
    }

    // ================== HÀM HỖ TRỢ ==================
    private static String[] splitSmart(String line) {
        if (line.indexOf(',') >= 0) return line.split(",", -1);
        if (line.indexOf(';') >= 0) return line.split(";", -1);
        return line.split("\t", -1);
    }

    private static void pushReject(List<Map<String, String>> rejected, int rowNum, String raw, String reason) {
        rejected.add(Map.of(
                "row", String.valueOf(rowNum),
                "raw", raw,
                "reason", reason
        ));
    }

    // =========================================================
    // 5. XOÁ 1 SINH VIÊN KHỎI LỚP
    // =========================================================
    @Transactional
    public void removeStudentFromClass(int classId, int studentId) {
        classRepository.removeStudentFromClass(studentId, classId);
    }

}
