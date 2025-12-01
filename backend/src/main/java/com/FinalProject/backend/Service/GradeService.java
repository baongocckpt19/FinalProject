package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.StudentGradeDto;
import com.FinalProject.backend.Repository.GradeRepository;
import com.FinalProject.backend.Repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;

    public GradeService(GradeRepository gradeRepository,
                        StudentRepository studentRepository) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
    }

    // ================== HÀM CŨ: LẤY DANH SÁCH ĐIỂM ==================
    public List<StudentGradeDto> getGradesForClass(int classId) {
        List<Object[]> rows = gradeRepository.findGradesByClassId(classId);
        return rows.stream().map(r -> {
            int i = 0;
            Integer studentId = (Integer) r[i++];
            String studentCode  = (String)  r[i++];
            String fullName   = (String) r[i++];
            String username   = (String) r[i++];
            Double att        = r[i] != null ? ((Number) r[i]).doubleValue() : null; i++;
            Double mid        = r[i] != null ? ((Number) r[i]).doubleValue() : null; i++;
            Double fin        = r[i] != null ? ((Number) r[i]).doubleValue() : null; i++;

            Double avg = null;
            if (att != null && mid != null && fin != null) {
                avg = 0.25 * att + 0.25 * mid + 0.5 * fin;
            }

            StudentGradeDto dto = new StudentGradeDto();
            dto.setStudentId(studentId);
            dto.setStudentCode(studentCode);
            dto.setFullName(fullName);
            dto.setUsername(username);
            dto.setAttendanceGrade(att);
            dto.setMidtermGrade(mid);
            dto.setFinalGrade(fin);
            dto.setAverageGrade(avg);
            return dto;
        }).toList();
    }

    @Transactional
    public void saveGradeForStudent(int classId, int studentId, StudentGradeDto dto) {
        gradeRepository.upsertGrade(
                studentId,
                classId,
                dto.getAttendanceGrade(),
                dto.getMidtermGrade(),
                dto.getFinalGrade()
        );
    }

    @Transactional
    public void saveAllGradesForClass(int classId, List<StudentGradeDto> list) {
        if (list == null) return;
        for (StudentGradeDto dto : list) {
            if (dto.getStudentId() != null) {
                saveGradeForStudent(classId, dto.getStudentId(), dto);
            }
        }
    }

    // ================== IMPORT CSV ĐIỂM ==================
    @Transactional
    public Map<String, Object> importGradesFromCsv(int classId,
                                                   String gradeType,
                                                   MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File CSV rỗng");
        }

        int total = 0;
        int imported = 0;
        List<Map<String, String>> rejected = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                total++;

                // Bỏ BOM ở dòng đầu tiên nếu có
                if (first) {
                    line = removeBom(line);
                    first = false;
                }

                if (line.trim().isEmpty()) continue;

                String[] parts = splitSmart(line);

                // A: Tên, B: MSSV (StudentCode), C..E: điểm
                if (parts.length < 3) {
                    pushReject(rejected, total, line, "Thiếu cột điểm (tối thiểu 3 cột)");
                    continue;
                }

                String name    = parts[0].trim(); // chỉ để hiển thị, không map
                String mssvStr = parts[1].trim(); // 🔥 Bây giờ là StudentCode

                if (mssvStr.isEmpty()) {
                    pushReject(rejected, total, line, "MSSV (cột B) trống");
                    continue;
                }

                // 🔥 Tìm student theo StudentCode trước
                Integer studentId = null;
                var optStudent = studentRepository.findByStudentCode(mssvStr);
                if (optStudent.isPresent()) {
                    studentId = optStudent.get().getStudentId();
                } else {
                    // fallback: nếu MSSV là số, thử hiểu như StudentId cũ
                    try {
                        Integer idByNumber = Integer.valueOf(mssvStr);
                        if (studentRepository.existsById(idByNumber)) {
                            studentId = idByNumber;
                        } else {
                            pushReject(rejected, total, line,
                                    "Không tìm thấy sinh viên với MSSV/Id: " + mssvStr);
                            continue;
                        }
                    } catch (NumberFormatException ex) {
                        pushReject(rejected, total, line,
                                "Không tìm thấy sinh viên với StudentCode: " + mssvStr);
                        continue;
                    }
                }

                Double att = null, mid = null, fin = null;

                try {
                    switch (gradeType) {
                        case "attendance" -> {
                            att = parseGrade(parts[2]);
                        }
                        case "midterm" -> {
                            mid = parseGrade(parts[2]);
                        }
                        case "final" -> {
                            fin = parseGrade(parts[2]);
                        }
                        case "all" -> {
                            if (parts.length < 5) {
                                pushReject(rejected, total, line,
                                        "Thiếu cột điểm (C,D,E) cho chế độ Cả 3 điểm");
                                continue;
                            }
                            att = parseGrade(parts[2]);
                            mid = parseGrade(parts[3]);
                            fin = parseGrade(parts[4]);
                        }
                        default -> {
                            pushReject(rejected, total, line,
                                    "Loại điểm không hợp lệ: " + gradeType);
                            continue;
                        }
                    }
                } catch (NumberFormatException ex) {
                    pushReject(rejected, total, line, "Điểm không phải số");
                    continue;
                }

                // Validate 0–10
                if (!isValidGrade(att) || !isValidGrade(mid) || !isValidGrade(fin)) {
                    pushReject(rejected, total, line, "Điểm ngoài khoảng 0-10");
                    continue;
                }

                // Upsert vào bảng Grade
                gradeRepository.upsertGrade(
                        studentId,
                        classId,
                        att,
                        mid,
                        fin
                );
                imported++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Import CSV hoàn tất");
        result.put("total", total);
        result.put("imported", imported);
        result.put("rejectedCount", rejected.size());
        result.put("rejectedRows", rejected);
        return result;
    }


    // ================== Helpers ==================

    private static String[] splitSmart(String line) {
        if (line.indexOf(',') >= 0) return line.split(",", -1);
        if (line.indexOf(';') >= 0) return line.split(";", -1);
        return line.split("\t", -1);
    }

    private static void pushReject(List<Map<String, String>> rejected,
                                   int rowNum,
                                   String raw,
                                   String reason) {
        rejected.add(Map.of(
                "row", String.valueOf(rowNum),
                "raw", raw,
                "reason", reason
        ));
    }

    private static Double parseGrade(String s) {
        s = s.trim();
        if (s.isEmpty()) return null;
        // cho phép "8,5" → "8.5"
        return Double.valueOf(s.replace(",", "."));
    }

    private static boolean isValidGrade(Double g) {
        if (g == null) return true; // null = không cập nhật
        return g >= 0 && g <= 10;
    }

    // Bỏ BOM (U+FEFF) ở đầu dòng nếu có
    private static String removeBom(String line) {
        if (line != null && !line.isEmpty() && line.charAt(0) == '\uFEFF') {
            return line.substring(1);
        }
        return line;
    }


    // ================== phần sinh vieeen================
    // ================== DASHBOARD ĐIỂM CHO SINH VIÊN ==================
    public List<com.FinalProject.backend.Dto.StudentClassGradeDto>
    getClassesWithGradesForStudent(Integer studentId) {

        List<Object[]> rows = gradeRepository.findClassGradesForStudent(studentId);

        return rows.stream().map(r -> {
            int i = 0;
            Integer classId      = (Integer) r[i++];
            String classCode     = (String)  r[i++];
            String className     = (String)  r[i++];
            Boolean status       = r[i] != null ? (Boolean) r[i] : null; i++;
            String teacherName   = (String)  r[i++];
            Double att           = r[i] != null ? ((Number) r[i]).doubleValue() : null; i++;
            Double mid           = r[i] != null ? ((Number) r[i]).doubleValue() : null; i++;
            Double fin           = r[i] != null ? ((Number) r[i]).doubleValue() : null; i++;

            Double avg = null;
            if (att != null && mid != null && fin != null) {
                avg = 0.25 * att + 0.25 * mid + 0.5 * fin;   // thang điểm 10
            }

            com.FinalProject.backend.Dto.StudentClassGradeDto dto =
                    new com.FinalProject.backend.Dto.StudentClassGradeDto();
            dto.setClassId(classId);
            dto.setClassCode(classCode);
            dto.setClassName(className);
            dto.setStatus(status);
            dto.setTeacherName(teacherName);
            dto.setAttendanceGrade(att);
            dto.setMidtermGrade(mid);
            dto.setFinalGrade(fin);
            dto.setAverageGrade(avg);
            return dto;
        }).toList();
    }

}
