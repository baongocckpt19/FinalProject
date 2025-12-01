// src/main/java/com/FinalProject/backend/Service/ClassTableService.java
package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.ClassDetailDto;
import com.FinalProject.backend.Dto.ClassListDto;
import com.FinalProject.backend.Dto.StudentOfClassDto;
import com.FinalProject.backend.Models.Clazz;
import com.FinalProject.backend.Repository.ClassRepository;
import com.FinalProject.backend.Repository.GradeRepository;
import com.FinalProject.backend.Repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class ClassTableService {

    private final ClassRepository classRepository;
    private final GradeRepository gradeRepository; // NEW
    private final StudentRepository studentRepository;

    public ClassTableService(ClassRepository classRepository,
                             GradeRepository gradeRepository,
                             StudentRepository studentRepository) {   // NEW
        this.classRepository = classRepository;
        this.gradeRepository = gradeRepository;// NEW
        this.studentRepository = studentRepository;
    }

    public List<ClassListDto> getAllClasses() {
        List<Object[]> rows = classRepository.findAllClassTable();
        return rows.stream().map(r -> {
            int i = 0;
            ClassListDto dto = new ClassListDto();
            dto.setClassId(asInt(r[i++]));         // 0
            dto.setClassCode(asStr(r[i++]));       // 1
            dto.setClassName(asStr(r[i++]));       // 2
            dto.setTeacherName(asStr(r[i++]));     // 3
            dto.setStudentCount(asInt(r[i++]));    // 4 (COUNT(*) có thể là Long/BigInteger)
            dto.setCreatedDate(asStr(r[i++]));     // 5 (đang là varchar(19))
            dto.setStatus(asBool(r[i++]));         // 6 (BIT có thể là Boolean/Short/Byte/Integer)
            dto.setFingerprintedCount(((Number) r[i++]).intValue()); // 7 NEW
            return dto;
        }).toList();
    }

    @Transactional
    public void softDelete(int classId) {
        classRepository.softDeleteClass(classId);
    }

    // ClassTableService.java
    public byte[] exportStudentsOfClass(int classId) {
        Object cls = classRepository.findClassInfoById(classId);
        if (cls == null) {
            return new byte[0];
        }
        Object[] c = (Object[]) cls;
        int i = 0;
        Integer cId          = asInt(c[i++]);
        String classCode     = asStr(c[i++]);
        String className     = asStr(c[i++]);
        String teacherName   = asStr(c[i++]);
        Integer studentCount = asInt(c[i++]);
        String createdDate   = asStr(c[i++]);
        Boolean status       = asBool(c[i++]);

        // Lấy danh sách sv (đã có FingerCount ở index 9)
        List<Object[]> students = classRepository.findStudentsByClassId(classId);

        // Đếm số SV đã có vân tay
        int fingerprintedCount = (int) students.stream()
                .filter(s -> {
                    if (s == null || s.length <= 9) return false;
                    Integer fc = asInt(s[9]); // FingerCount
                    return fc != null && fc > 0;
                })
                .count();

        StringBuilder sb = new StringBuilder();

        // THÔNG TIN LỚP
        sb.append("Mã lớp,").append(csv(classCode)).append("\n");
        sb.append("Tên lớp,").append(csv(className)).append("\n");
        sb.append("Giảng viên,").append(csv(teacherName)).append("\n");
        sb.append("Số lượng sinh viên,").append(studentCount != null ? studentCount : 0).append("\n");
        sb.append("Số sinh viên đã có vân tay,").append(fingerprintedCount).append("\n");
        sb.append("Ngày tạo,").append(csv(createdDate)).append("\n");
        sb.append("Trạng thái,").append(status != null && status ? "Đã hoàn thành" : "Đang hoạt động").append("\n");

        sb.append("\n");

        // HEADER DS SV – DÙNG MÃ SỐ
        sb.append("STT,Mã số sinh viên,Tên,Username,Ngày sinh,Giới tính,Địa chỉ,Email,Phone,Số vân tay\n");

        int stt = 1;
        for (Object[] s : students) {
            int j = 0;
            String studentCode  = asStr(s[j++]);   // 0
            Integer studentId   = asInt(s[j++]);   // 1 (không export, chỉ nội bộ)
            String fullName     = asStr(s[j++]);   // 2
            String username     = asStr(s[j++]);   // 3
            String dob          = asStr(s[j++]);   // 4
            String gender       = asStr(s[j++]);   // 5
            String address      = asStr(s[j++]);   // 6
            String email        = asStr(s[j++]);   // 7
            String phone        = asStr(s[j++]);   // 8
            Integer fingerCount = asInt(s[j++]);   // 9

            sb.append(stt++).append(",");
            sb.append(csv(studentCode)).append(",");         // 👈 MÃ SỐ
            sb.append(csv(fullName)).append(",");
            sb.append(csv(username)).append(",");
            sb.append(csv(dob)).append(",");
            sb.append(csv(gender)).append(",");
            sb.append(csv(address)).append(",");
            sb.append(csv(email)).append(",");
            sb.append(csv(phone)).append(",");
            sb.append(fingerCount != null ? fingerCount : 0).append("\n");
        }

        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[bom.length + data.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(data, 0, result, bom.length, data.length);
        return result;
    }




    public ClassDetailDto getClassDetail(int classId) {
        Object r = classRepository.findClassDetailById(classId);
        if (r == null) return null;
        Object[] o = (Object[]) r;
        int i = 0;
        ClassDetailDto dto = new ClassDetailDto();
        dto.setClassId(asInt(o[i++]));       // 0
        dto.setClassCode(asStr(o[i++]));     // 1
        dto.setClassName(asStr(o[i++]));     // 2
        dto.setTeacherId(asInt(o[i++]));     // 3
        dto.setTeacherName(asStr(o[i++]));   // 4
        dto.setCreatedDate(asStr(o[i++]));   // 5
        dto.setStatus(asBool(o[i++]));       // 6
        return dto;
    }

    @Transactional
    public void updateClass(int classId, String newCode, String newName, Integer newTeacherId) {
        classRepository.updateClass(classId, newCode, newName, newTeacherId);
    }

    @Transactional
    public Clazz createClass(String classCode, String className, Integer teacherId) {
        Clazz c = new Clazz();
        c.setClassCode(classCode);
        c.setClassName(className);
        c.setTeacherId(teacherId);
        c.setCreatedDate(new Date());
        c.setStatus(false);
        c.setIsDeleted(false);
        return classRepository.save(c);
    }

    // ===== Helpers =====

    private static String asStr(Object o) {
        return o == null ? null : o.toString();
    }

    private static Integer asInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return null; }
    }

    private static Boolean asBool(Object o) {
        if (o == null) return null;
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() != 0;
        String s = o.toString().trim();
        if ("true".equalsIgnoreCase(s)) return true;
        if ("false".equalsIgnoreCase(s)) return false;
        if ("1".equals(s)) return true;
        if ("0".equals(s)) return false;
        return null;
    }

    private String csv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = v.replace("\"", "\"\"");
            return "\"" + v + "\"";
        }
        return v;
    }

    //lấy danh sách sinh viên của lớp học theo id lớp

    public List<StudentOfClassDto> getStudentsOfClass(int classId) {
        List<Object[]> rows = classRepository.findStudentsForClassModal(classId);
        return rows.stream().map(r -> {
            int i = 0;
            StudentOfClassDto dto = new StudentOfClassDto();
            dto.setStudentId(asInt(r[i++]));        // 0
            dto.setStudentCode(asStr(r[i++]));      // 1 👈
            dto.setFullName(asStr(r[i++]));         // 2
            dto.setUsername(asStr(r[i++]));         // 3
            dto.setEmail(asStr(r[i++]));            // 4
            dto.setFingerCount(asInt(r[i++]));      // 5
            return dto;
        }).toList();
    }


    // cập nhật trạng thái lớp học
    @Transactional
    public void updateClassStatus(int classId, boolean newStatus) {
        classRepository.updateClassStatus(classId, newStatus);
    }

    //===========================================================//
    //====================GIẢNG VIÊN=======================//
    //===========================================================//

    // ClassTableService.java

    public List<ClassListDto> getClassesForTeacher(int teacherId) {
        List<Object[]> rows = classRepository.findClassTableForTeacher(teacherId);
        return rows.stream().map(r -> {
            int i = 0;
            ClassListDto dto = new ClassListDto();
            dto.setClassId(asInt(r[i++]));             // 0
            dto.setClassCode(asStr(r[i++]));           // 1
            dto.setClassName(asStr(r[i++]));           // 2
            dto.setTeacherName(asStr(r[i++]));         // 3
            dto.setStudentCount(asInt(r[i++]));        // 4
            dto.setCreatedDate(asStr(r[i++]));         // 5
            dto.setStatus(asBool(r[i++]));             // 6
            dto.setFingerprintedCount(((Number) r[i++]).intValue()); // 7
            return dto;
        }).toList();
    }


    // ======================= EXPORT ĐIỂM CỦA LỚP =======================

    public byte[] exportGradesOfClass(int classId) {
        Object cls = classRepository.findClassInfoById(classId);
        if (cls == null) {
            return new byte[0];
        }
        Object[] c = (Object[]) cls;
        int i = 0;
        Integer cId          = asInt(c[i++]);
        String classCode     = asStr(c[i++]);
        String className     = asStr(c[i++]);
        String teacherName   = asStr(c[i++]);
        Integer studentCount = asInt(c[i++]);
        String createdDate   = asStr(c[i++]);
        Boolean status       = asBool(c[i++]);

        // Lấy danh sách điểm (đã có StudentCode ở index 1)
        List<Object[]> grades = gradeRepository.findGradesByClassId(classId);

        StringBuilder sb = new StringBuilder();

        // THÔNG TIN LỚP (header trên cùng)
        sb.append("Tên lớp,").append(csv(className)).append("\n");
        sb.append("Mã lớp,").append(csv(classCode)).append("\n");
        sb.append("Giảng viên,").append(csv(teacherName)).append("\n");
        sb.append("Số sinh viên,").append(studentCount != null ? studentCount : 0).append("\n");
        sb.append("Ngày tạo,").append(csv(createdDate)).append("\n");
        sb.append("Trạng thái,")
                .append(status != null && status ? "Đã hoàn thành" : "Đang hoạt động")
                .append("\n\n");

        // HEADER – MSSV = MÃ SỐ SINH VIÊN
        sb.append("STT,Họ tên,MSSV,Điểm chuyên cần,Điểm giữa kỳ,Điểm cuối kỳ,Điểm trung bình,Xếp loại\n");

        int stt = 1;
        for (Object[] g : grades) {
            int j = 0;
            Integer studentId      = asInt(g[j++]);                      // 0
            String studentCode     = asStr(g[j++]);                      // 1  👈 LẤY MSSV TỪ QUERY
            String fullName        = asStr(g[j++]);                      // 2
            String username        = asStr(g[j++]);                      // 3
            Double attendanceGrade = g[j] != null ? ((Number) g[j]).doubleValue() : null; j++; // 4
            Double midtermGrade    = g[j] != null ? ((Number) g[j]).doubleValue() : null; j++; // 5
            Double finalGrade      = g[j] != null ? ((Number) g[j]).doubleValue() : null; j++; // 6

            double at  = attendanceGrade != null ? attendanceGrade : 0.0;
            double mid = midtermGrade    != null ? midtermGrade    : 0.0;
            double fin = finalGrade      != null ? finalGrade      : 0.0;
            double avg = 0.25 * at + 0.25 * mid + 0.5 * fin;

            String xepLoai;
            if (avg >= 9)      xepLoai = "Xuất sắc";
            else if (avg >= 8) xepLoai = "Giỏi";
            else if (avg >= 7) xepLoai = "Khá";
            else if (avg >= 5) xepLoai = "Trung bình";
            else               xepLoai = "Yếu";

            sb.append(stt++).append(",");
            sb.append(csv(fullName)).append(",");
            sb.append(csv(studentCode != null ? studentCode : "")).append(",");  // 👈 MSSV
            sb.append(at).append(",");
            sb.append(mid).append(",");
            sb.append(fin).append(",");
            sb.append(String.format(java.util.Locale.US, "%.2f", avg)).append(",");
            sb.append(csv(xepLoai)).append("\n");
        }

        // Thêm BOM UTF-8 để Excel hiểu tiếng Việt
        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[bom.length + data.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(data, 0, result, bom.length, data.length);
        return result;
    }


}
