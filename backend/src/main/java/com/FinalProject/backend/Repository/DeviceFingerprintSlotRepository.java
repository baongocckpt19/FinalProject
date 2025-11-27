// FILE: src/main/java/com/FinalProject/backend/Repository/DeviceFingerprintSlotRepository.java
package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.DeviceFingerprintSlot;
import com.FinalProject.backend.Models.DeviceFingerprintSlotId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface DeviceFingerprintSlotRepository extends JpaRepository<DeviceFingerprintSlot, DeviceFingerprintSlotId> {

    // tìm tất cả slot của 1 student
    List<DeviceFingerprintSlot> findByIdStudentId(Integer studentId);

    // !!! THÊM MỚI: tìm theo DeviceId + SensorSlot (để check UNIQUE)
    Optional<DeviceFingerprintSlot> findByIdDeviceIdAndSensorSlot(Integer deviceId, Integer sensorSlot);
}
