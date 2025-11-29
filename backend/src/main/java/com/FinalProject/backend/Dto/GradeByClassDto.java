// GradeByClassDto.java
package com.FinalProject.backend.Dto;
import lombok.Data;
@Data
public class GradeByClassDto {
    private Integer classId;
    private String classCode;
    private String className;
    private Double finalGrade;

    // getters + setters
}
