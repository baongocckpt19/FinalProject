package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.*;
import com.FinalProject.backend.Service.FingerprintEnrollService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/fingerprint")
@CrossOrigin(origins = "*")
public class FingerprintEnrollController {

    private final FingerprintEnrollService fingerprintEnrollService;

    public FingerprintEnrollController(FingerprintEnrollService fingerprintEnrollService) {
        this.fingerprintEnrollService = fingerprintEnrollService;
    }

    /**
     * Frontend: tạo session enroll
     * POST /api/fingerprint/enroll/session
     * body: { studentId, deviceCode? }
     * response: { sessionCode }
     */
    @PostMapping("/enroll/session")
    public ResponseEntity<CreateEnrollSessionResponse> createEnrollSession(
            @RequestBody CreateEnrollSessionRequest req
    ) {
        CreateEnrollSessionResponse res = fingerprintEnrollService.createEnrollSession(req);
        return ResponseEntity.ok(res);
    }

    /**
     * ESP32: upload template từ thiết bị về server
     * POST /api/fingerprint/enroll/upload-from-device
     * body: { sessionCode, deviceCode, sensorSlot, templateBase64 }
     */
    @PostMapping("/enroll/upload-from-device")
    public ResponseEntity<Map<String, Object>> uploadFromDevice(
            @RequestBody UploadFromDeviceRequest req
    ) {
        fingerprintEnrollService.uploadFromDevice(req);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Upload from device OK");
        return ResponseEntity.ok(body);
    }

    /**
     * Frontend: kiểm tra session đã nhận template/sensorSlot chưa
     * GET /api/fingerprint/enroll/temp?sessionCode=...
     */
    @GetMapping("/enroll/temp")
    public ResponseEntity<CheckEnrollTempResponse> checkTemp(
            @RequestParam String sessionCode
    ) {
        CheckEnrollTempResponse res = fingerprintEnrollService.checkTemp(sessionCode);
        return ResponseEntity.ok(res);
    }

    /**
     * Frontend: confirm lưu template vào FingerprintTemplate + DeviceFingerprintSlot
     * POST /api/fingerprint/enroll/confirm
     * body: { studentId, sessionCode }
     */
    @PostMapping("/enroll/confirm")
    public ResponseEntity<Map<String, Object>> confirmEnroll(
            @RequestBody ConfirmEnrollRequest req
    ) {
        fingerprintEnrollService.confirmEnroll(req);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Enroll confirmed");
        return ResponseEntity.ok(body);
    }
}
