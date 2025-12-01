// ĐÂY LÀ StudentOfClassDto
package com.FinalProject.backend.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentOfClassDto {
    private Integer studentId;
    private String fullName;
    private String username;
    private String email;
    private Integer fingerCount;

    private String studentCode;

}
