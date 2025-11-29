// AttendanceSummaryDto.java
package com.FinalProject.backend.Dto;
import lombok.Data;
@Data
public class AttendanceSummaryDto {
    private long presentCount;  // Có mặt
    private long absentCount;   // Vắng
    private long lateCount;     // Đi muộn

    // getters + setters
}
