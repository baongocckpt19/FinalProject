// FILE: src/main/java/com/FinalProject/backend/Repository/DeviceRepository.java
package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Integer> {

    Optional<Device> findByDeviceCode(String deviceCode);

    List<Device> findByIsActiveTrue();

}
