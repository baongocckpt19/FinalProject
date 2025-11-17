// com.FinalProject.backend.Dto.ClassDetailDto

package com.FinalProject.backend.Dto;

import lombok.Data;

@Data
public class ClassDetailDto {
    private Integer classId;
    private String classCode;
    private String className;
    private Integer teacherId;
    private String teacherName;
    private String createdDate;
    private Boolean status;
}
