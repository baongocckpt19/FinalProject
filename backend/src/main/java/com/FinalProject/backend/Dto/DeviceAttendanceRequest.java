//package com.FinalProject.backend.Dto;
//
//import lombok.Data;
//
//@Data
//public class DeviceAttendanceRequest {
//
//    // MSSV (trùng với Account.Username)
//    private String studentUsername;
//
//    // Hoặc nếu muốn gửi thẳng studentId thì cho phép luôn
//    private Integer studentId;
//
//    // Lớp đang điểm danh
//    private Integer classId;
//
//    // Ngày điểm danh, nếu null thì dùng LocalDate.now()
//    // Format: yyyy-MM-dd
//    private String attendanceDate;
//
//    // Giờ quét vân tay, nếu null thì dùng LocalTime.now()
//    // Format: HH:mm:ss
//    private String attendanceTime;
//
//    // Khung giờ buổi học (để phân biệt Có mặt / Muộn)
//    // Format: HH:mm
//    private String sessionStart;   // ví dụ "07:00"
//    private String sessionEnd;     // ví dụ "09:00"
//
//    // Trạng thái gửi sẵn (tùy chọn): "present" / "late" / "absent"
//    // Nếu null thì backend sẽ tự suy ra từ attendanceTime vs sessionStart
//    private String status;
//}
