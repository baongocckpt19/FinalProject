//ĐÂY LÀ FINGERPRINTENROLLCONTROLLER.JAVA
package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.*;
import com.FinalProject.backend.Service.FingerprintEnrollService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
    public ResponseEntity<ConfirmEnrollResponse> confirmEnroll(
            @RequestBody ConfirmEnrollRequest req
    ) {
        ConfirmEnrollResponse res = fingerprintEnrollService.confirmEnroll(req);
        return ResponseEntity.ok(res);
    }


    /**
     * ESP32: Poll lấy "lệnh enroll" tiếp theo cho device
     * GET /api/fingerprint/enroll/next-command?deviceCode=ESP_ROOM_LAB1
     * <p>
     * - Nếu KHÔNG có lệnh -> trả về 204 No Content
     * - Nếu CÓ lệnh -> 200 OK, body = sessionCode (text/plain)
     */
    @GetMapping("/enroll/next-command")
    public ResponseEntity<String> getNextCommand(
            @RequestParam String deviceCode
    ) {
        String sessionCode = fingerprintEnrollService.getNextEnrollSessionForDevice(deviceCode);
        if (sessionCode == null) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(sessionCode);         // 200 + "STU4-ABC123"
    }


    @PostMapping("/sync/distribute")
    public ResponseEntity<?> distributeFingerprint(@RequestBody DistributeRequest req) {
        fingerprintEnrollService.createSyncTasks(req.getStudentId());
        return ResponseEntity.ok(Collections.singletonMap("message", "Sync tasks created"));
    }

    @GetMapping("/sync/data")
    public ResponseEntity<String> getSyncData(@RequestParam String sessionCode) {
        // Tìm session (tạm hoặc gốc)
        // Ở đây giả sử bạn lưu data vào EnrollSession hoặc lấy từ Template gốc
        // Trả về chuỗi Base64
        // String base64 = Base64.getEncoder().encodeToString(templateBytes);
        // return ResponseEntity.ok(base64);

        // Logic giả định:
        return ResponseEntity.ok(fingerprintEnrollService.getTemplateDataBase64(sessionCode));
    }

    @PostMapping("/sync/result")
    public ResponseEntity<?> syncResult(@RequestBody SyncResultRequest req) {
        fingerprintEnrollService.handleSyncResult(req);
        return ResponseEntity.ok(Collections.singletonMap("message", "Sync result updated"));
    }

}


