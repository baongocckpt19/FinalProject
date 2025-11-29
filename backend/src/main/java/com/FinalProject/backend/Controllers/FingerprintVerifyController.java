package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.FingerprintVerifyRequest;
import com.FinalProject.backend.Dto.FingerprintVerifyResponse;
import com.FinalProject.backend.Service.FingerprintVerifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fingerprint")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FingerprintVerifyController {

    private final FingerprintVerifyService fingerprintVerifyService;

    /**
     * ESP32: VERIFY vân tay để điểm danh
     * POST /api/fingerprint/verify
     * body: { deviceCode, sensorSlot, timestamp? }
     */
    @PostMapping("/verify")
    public ResponseEntity<FingerprintVerifyResponse> verify(
            @RequestBody FingerprintVerifyRequest request
    ) {
        FingerprintVerifyResponse resp = fingerprintVerifyService.verify(request);
        return ResponseEntity.ok(resp);
    }
}
