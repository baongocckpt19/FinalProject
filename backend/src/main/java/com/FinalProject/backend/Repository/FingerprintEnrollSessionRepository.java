// FILE: src/main/java/com/FinalProject/backend/Repository/FingerprintEnrollSessionRepository.java
package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.FingerprintEnrollSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FingerprintEnrollSessionRepository
        extends JpaRepository<FingerprintEnrollSession, Integer> {

    Optional<FingerprintEnrollSession> findBySessionCode(String sessionCode);


    // 🔹 THÊM MỚI: lấy session sớm nhất cho 1 device với status cho trước
    Optional<FingerprintEnrollSession>
    findFirstByDeviceIdAndStatusOrderByCreatedAtAsc(Integer deviceId, String status);
}
