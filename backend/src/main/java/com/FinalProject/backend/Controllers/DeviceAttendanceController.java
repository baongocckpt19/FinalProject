//package com.FinalProject.backend.Controllers;
//
//import com.FinalProject.backend.Dto.DeviceAttendanceRequest;
//import com.FinalProject.backend.Models.Attendance;
//import com.FinalProject.backend.Service.DeviceAttendanceService;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//import java.util.Objects;
//
//@RestController
//@RequestMapping("/api/device")
//public class DeviceAttendanceController {
//
//    private final DeviceAttendanceService deviceAttendanceService;
//
//    @Value("${device.secret}")
//    private String deviceSecret;  // cấu hình trong application.properties
//
//    public DeviceAttendanceController(DeviceAttendanceService deviceAttendanceService) {
//        this.deviceAttendanceService = deviceAttendanceService;
//    }
//
//    @PostMapping("/attendance")
//    public ResponseEntity<?> receiveAttendance(
//            @RequestHeader(value = "X-DEVICE-KEY", required = false) String deviceKey,
//            @RequestBody DeviceAttendanceRequest body
//    ) {
//        // 1) Kiểm tra key thiết bị
//        if (deviceKey == null || !Objects.equals(deviceKey, deviceSecret)) {
//            return ResponseEntity.status(401)
//                    .body(Map.of("message", "Thiết bị không hợp lệ"));
//        }
//
//        try {
//            Attendance a = deviceAttendanceService.markAttendance(body);
//            return ResponseEntity.ok(Map.of(
//                    "message", "Ghi nhận điểm danh thành công",
//                    "attendanceId", a.getAttendanceId(),
//                    "status", a.getStatus(),
//                    "studentId", a.getStudentId(),
//                    "classId", a.getClassId(),
//                    "attendanceDate", a.getAttendanceDate().toString(),
//                    "attendanceTime", a.getAttendanceTime().toString()
//            ));
//        } catch (IllegalArgumentException ex) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("error", ex.getMessage()));
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return ResponseEntity.status(500)
//                    .body(Map.of("error", "Lỗi hệ thống: " + ex.getMessage()));
//        }
//    }
//}
