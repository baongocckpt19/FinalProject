// FILE: src/main/java/com/FinalProject/backend/Repository/FingerprintTemplateRepository.java
package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.FingerprintTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FingerprintTemplateRepository extends JpaRepository<FingerprintTemplate, Integer> {
    Optional<FingerprintTemplate> findByStudentId(Integer studentId);
}
