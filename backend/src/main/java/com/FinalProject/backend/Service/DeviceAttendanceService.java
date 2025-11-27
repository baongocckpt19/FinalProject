//package com.FinalProject.backend.Service;
//
//import com.FinalProject.backend.Dto.DeviceAttendanceRequest;
//import com.FinalProject.backend.Models.Attendance;
//import com.FinalProject.backend.Repository.AttendanceRepository;
//import com.FinalProject.backend.Repository.StudentRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Objects;
//
//@Service
//public class DeviceAttendanceService {
//
//    private final AttendanceRepository attendanceRepository;
//    private final StudentRepository studentRepository;
//
//    public DeviceAttendanceService(AttendanceRepository attendanceRepository,
//                                   StudentRepository studentRepository) {
//        this.attendanceRepository = attendanceRepository;
//        this.studentRepository = studentRepository;
//    }
//
//    @Transactional
//    public Attendance markAttendance(DeviceAttendanceRequest req) {
//        // 1) Xác định studentId
//        Integer studentId = req.getStudentId();
//        if (studentId == null) {
//            if (req.getStudentUsername() == null || req.getStudentUsername().isBlank()) {
//                throw new IllegalArgumentException("Thiếu studentId hoặc studentUsername");
//            }
//            studentId = studentRepository.findStudentIdByUsername(req.getStudentUsername());
//            if (studentId == null) {
//                throw new IllegalArgumentException("Không tìm thấy sinh viên với username: " + req.getStudentUsername());
//            }
//        }
//
//        if (req.getClassId() == null) {
//            throw new IllegalArgumentException("Thiếu classId");
//        }
//        Integer classId = req.getClassId();
//
//        // 2) Xử lý ngày & giờ
//        LocalDate date;
//        if (req.getAttendanceDate() == null || req.getAttendanceDate().isBlank()) {
//            date = LocalDate.now();
//        } else {
//            date = LocalDate.parse(req.getAttendanceDate(), DateTimeFormatter.ISO_DATE);
//        }
//
//        LocalTime attTime;
//        if (req.getAttendanceTime() == null || req.getAttendanceTime().isBlank()) {
//            attTime = LocalTime.now();
//        } else {
//            attTime = LocalTime.parse(req.getAttendanceTime());
//        }
//
//        // 3) Khung giờ buổi học
//        LocalTime sessionStart = LocalTime.parse(
//                Objects.requireNonNullElse(req.getSessionStart(), "07:00")
//        );
//        LocalTime sessionEnd = LocalTime.parse(
//                Objects.requireNonNullElse(req.getSessionEnd(), "09:00")
//        );
//
//        // 4) Xác định status
//        String statusCode = req.getStatus();
//        if (statusCode == null || statusCode.isBlank()) {
//            // Tự tính: nếu vào muộn hơn 15 phút sau giờ bắt đầu thì "late", ngược lại "present"
//            LocalTime lateThreshold = sessionStart.plusMinutes(15);
//            if (!attTime.isAfter(lateThreshold)) {
//                statusCode = "present";
//            } else {
//                statusCode = "late";
//            }
//        }
//
//        String statusVi = mapStatusCodeToVi(statusCode);
//
//        // 5) Tìm xem đã có bản ghi trong ngày chưa
//        Attendance existing = attendanceRepository
//                .findByStudentAndClassAndDate(studentId, classId, date);
//
//        Attendance a;
//        if (existing != null) {
//            // Cập nhật lại lần quét mới nhất
//            a = existing;
//            a.setAttendanceTime(attTime);
//            a.setSessionStart(sessionStart);
//            a.setSessionEnd(sessionEnd);
//            a.setStatus(statusVi);
//        } else {
//            // Tạo mới
//            a = new Attendance();
//            a.setStudentId(studentId);
//            a.setClassId(classId);
//            a.setAttendanceDate(date);
//            a.setAttendanceTime(attTime);
//            a.setSessionStart(sessionStart);
//            a.setSessionEnd(sessionEnd);
//            a.setStatus(statusVi);
//        }
//
//        return attendanceRepository.save(a);
//    }
//
//    private String mapStatusCodeToVi(String code) {
//        if (code == null) return "Vắng";
//        return switch (code) {
//            case "present" -> "Có mặt";
//            case "late"    -> "Muộn";
//            case "absent"  -> "Vắng";
//            default         -> "Vắng";
//        };
//    }
//}
