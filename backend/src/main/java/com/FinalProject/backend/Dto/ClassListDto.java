package com.FinalProject.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassListDto {
    private Integer classId;
    private String classCode;
    private String className;
    private String teacherName;
    private Integer studentCount;
    private String createdDate; // format sẵn yyyy-MM-dd HH:mm hoặc yyyy-MM-dd
    private Boolean status;     // 0: hoạt động, 1: hoàn thành
    private Integer fingerprintedCount;

}
