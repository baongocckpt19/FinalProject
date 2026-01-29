// đây là AttendanceService.java
package com.FinalProject.backend.Service;

import com.FinalProject.backend.Repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceService {
    @Autowired
    private AttendanceRepository attendanceRepository;

    public List<Object[]> getRecentAttendance(Integer classId) {
        return attendanceRepository.findRecentAttendance(classId);
    }
}
