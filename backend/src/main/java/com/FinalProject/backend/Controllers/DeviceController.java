// FILE: src/main/java/com/FinalProject/backend/Controllers/DeviceController.java
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.DeviceRequest;
import com.FinalProject.backend.Models.Device;
import com.FinalProject.backend.Repository.DeviceRepository;
import com.FinalProject.backend.Service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin("*")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public List<Device> getAllDevices() {
        return deviceService.getAll();
    }

    @GetMapping("/active")
    public List<Device> getActiveDevices() {
        return deviceService.getActive();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Integer id) {
        return deviceService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createDevice(@RequestBody DeviceRequest req) {
        return ResponseEntity.ok(deviceService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateDevice(@PathVariable Integer id,
                                               @RequestBody DeviceRequest req) {
        return ResponseEntity.ok(deviceService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDevice(@PathVariable Integer id) {
        return ResponseEntity.ok(deviceService.delete(id));
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<String> updateActive(@PathVariable Integer id,
                                               @RequestParam boolean isActive) {
        return ResponseEntity.ok(deviceService.updateStatus(id, isActive));
    }
}
