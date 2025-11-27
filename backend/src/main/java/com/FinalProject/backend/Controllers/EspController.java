package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.EspLogRequest;
import com.FinalProject.backend.Models.EspLog;
import com.FinalProject.backend.Repository.EspLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/esp")
@CrossOrigin(origins = "*")
public class EspController {

    private final EspLogRepository espLogRepository;

    public EspController(EspLogRepository espLogRepository) {
        this.espLogRepository = espLogRepository;
    }

    @PostMapping("/log")
    public ResponseEntity<?> logFromDevice(@RequestBody EspLogRequest request) {
        EspLog log = new EspLog();
        log.setDeviceCode(request.getDeviceCode());
        log.setMessage(request.getMessage());
        espLogRepository.save(log);

        return ResponseEntity.ok("OK");
    }
}
