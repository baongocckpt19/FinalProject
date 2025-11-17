// src/main/java/com/FinalProject/backend/Service/ClassTableService.java
package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.ClassDetailDto;
import com.FinalProject.backend.Dto.ClassListDto;
import com.FinalProject.backend.Dto.StudentOfClassDto;
import com.FinalProject.backend.Models.Clazz;
import com.FinalProject.backend.Repository.ClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class ClassTableService {

    private final ClassRepository classRepository;

    public ClassTableService(ClassRepository classRepository) {
        this.classRepository = classRepository;
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

    public byte[] exportStudentsOfClass(int classId) {
        // 1) lấy info lớp
        Object cls = classRepository.findClassInfoById(classId);
        if (cls == null) {
            return new byte[0];
        }
        Object[] c = (Object[]) cls;
        int i = 0;
        Integer cId          = asInt(c[i++]);   // 0
        String classCode     = asStr(c[i++]);   // 1
        String className     = asStr(c[i++]);   // 2
        String teacherName   = asStr(c[i++]);   // 3
        Integer studentCount = asInt(c[i++]);   // 4
        String createdDate   = asStr(c[i++]);   // 5
        Boolean status       = asBool(c[i++]);  // 6

        // 2) lấy danh sách SV (đã có thêm cột FingerCount ở index 8)
        List<Object[]> students = classRepository.findStudentsByClassId(classId);

        // 2.1) Đếm số học sinh đã có vân tay (FingerCount > 0)
        int fingerprintedCount = (int) students.stream()
                .filter(s -> {
                    if (s == null || s.length <= 8) return false;
                    Integer fc = asInt(s[8]); // FingerCount
                    return fc != null && fc > 0;
                })
                .count();

        StringBuilder sb = new StringBuilder();

        // ====== PHẦN THÔNG TIN LỚP ======
        sb.append("Mã lớp,").append(csv(classCode)).append("\n");
        sb.append("Tên lớp,").append(csv(className)).append("\n");
        sb.append("Giảng viên,").append(csv(teacherName)).append("\n");
        sb.append("Số lượng sinh viên,").append(studentCount != null ? studentCount : 0).append("\n");

        // DÒNG MỚI: SỐ HỌC SINH ĐÃ CÓ VÂN TAY
        sb.append("Số học sinh đã có vân tay,").append(fingerprintedCount).append("\n");

        sb.append("Ngày tạo,").append(csv(createdDate)).append("\n");
        sb.append("Trạng thái,").append(status != null && status ? "Đã hoàn thành" : "Hoạt động").append("\n");

        sb.append("\n"); // 1 dòng trống

        // ====== HEADER DANH SÁCH SV ======
        sb.append("STT,Mã học sinh,Tên,Username,Ngày sinh,Giới tính,Địa chỉ,Email,Phone,Số vân tay\n");

        int stt = 1;
        for (Object[] s : students) {
            int j = 0;
            Integer studentId   = asInt(s[j++]);  // 0
            String fullName     = asStr(s[j++]);  // 1
            String username     = asStr(s[j++]);  // 2
            String dob          = asStr(s[j++]);  // 3
            String gender       = asStr(s[j++]);  // 4
            String address      = asStr(s[j++]);  // 5
            String email        = asStr(s[j++]);  // 6
            String phone        = asStr(s[j++]);  // 7
            Integer fingerCount = asInt(s[j++]);  // 8

            sb.append(stt++).append(",");
            sb.append(studentId != null ? studentId : "").append(",");
            sb.append(csv(fullName)).append(",");
            sb.append(csv(username)).append(",");
            sb.append(csv(dob)).append(",");
            sb.append(csv(gender)).append(",");
            sb.append(csv(address)).append(",");
            sb.append(csv(email)).append(",");
            sb.append(csv(phone)).append(",");
            sb.append(fingerCount != null ? fingerCount : 0).append("\n");
        }

        // BOM UTF-8 cho Excel
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
            dto.setStudentId(asInt(r[i++]));           // 0
            dto.setFullName(asStr(r[i++]));            // 1
            dto.setUsername(asStr(r[i++]));            // 2
            dto.setEmail(asStr(r[i++]));               // 3
            dto.setFingerCount(asInt(r[i++]));         // 4
            return dto;
        }).toList();
    }

    // cập nhật trạng thái lớp học
    @Transactional
    public void updateClassStatus(int classId, boolean newStatus) {
        classRepository.updateClassStatus(classId, newStatus);
    }


}
