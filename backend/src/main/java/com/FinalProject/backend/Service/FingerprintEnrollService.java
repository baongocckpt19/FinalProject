// FILE: src/main/java/com/FinalProject/backend/Service/FingerprintEnrollService.java
package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.*;
import com.FinalProject.backend.Models.*;
import com.FinalProject.backend.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;

@Service
public class FingerprintEnrollService {

    private final StudentRepository studentRepo;
    private final AccountRepository accountRepo;
    private final DeviceRepository deviceRepo;
    private final FingerprintTemplateRepository templateRepo;
    private final DeviceFingerprintSlotRepository slotRepo;
    private final FingerprintEnrollSessionRepository sessionRepo;

    public FingerprintEnrollService(
            StudentRepository studentRepo,
            AccountRepository accountRepo,
            DeviceRepository deviceRepo,
            FingerprintTemplateRepository templateRepo,
            DeviceFingerprintSlotRepository slotRepo,
            FingerprintEnrollSessionRepository sessionRepo
    ) {
        this.studentRepo = studentRepo;
        this.accountRepo = accountRepo;
        this.deviceRepo = deviceRepo;
        this.templateRepo = templateRepo;
        this.slotRepo = slotRepo;
        this.sessionRepo = sessionRepo;
    }

    // ---------- 1) GET /api/students/{id}/fingerprint ----------
    @Transactional(readOnly = true)
    public StudentFingerprintInfoDto getStudentFingerprintInfo(Integer studentId) {
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // lấy username/email từ Account nếu có
        String username = null;
        String email = student.getEmail(); // fallback
        if (student.getAccountId() != null) {
            Account acc = accountRepo.findById(student.getAccountId())
                    .orElse(null);
            if (acc != null) {
                username = acc.getUsername();
                if (acc.getUsername() != null && (email == null || email.isEmpty())) {
                    // giữ nguyên email từ student nếu có
                }
            }
        }

        // kiểm tra template
        boolean hasFingerprint = templateRepo.findByStudentId(studentId).isPresent();

        // devices
        List<DeviceFingerprintSlot> slotList = slotRepo.findByIdStudentId(studentId);
        List<DeviceFingerprintInfoDto> deviceDtos = slotList.stream().map(slot -> {
            Integer deviceId = slot.getId().getDeviceId();
            Device device = deviceRepo.findById(deviceId).orElse(null);
            if (device == null) return null;
            return new DeviceFingerprintInfoDto(
                    device.getDeviceId(),
                    device.getDeviceCode(),
                    device.getDeviceName(),
                    device.getRoom(),
                    slot.getSensorSlot()
            );
        }).filter(Objects::nonNull).collect(Collectors.toList());

        StudentFingerprintInfoDto dto = new StudentFingerprintInfoDto();
        dto.setStudentId(studentId);
        dto.setStudentCode(student.getStudentCode());
        dto.setFullName(student.getFullName());
        dto.setUsername(username != null ? username : "");
        dto.setEmail(email);
        dto.setHasFingerprint(hasFingerprint);
        dto.setFingerprintDevicesCount(deviceDtos.size());
        dto.setDevices(deviceDtos);

        return dto;
    }

    // ---------- 2) POST /api/fingerprint/enroll/session ----------
    @Transactional
    public CreateEnrollSessionResponse createEnrollSession(CreateEnrollSessionRequest req) {
        Integer studentId = req.getStudentId();
        if (studentId == null) {
            throw new RuntimeException("studentId is required");
        }
        if (!studentRepo.existsById(studentId)) {
            throw new RuntimeException("Student not found");
        }

        FingerprintEnrollSession session = new FingerprintEnrollSession();
        session.setStudentId(studentId);

        // nếu UI có chọn deviceCode sẵn
        if (req.getDeviceCode() != null && !req.getDeviceCode().isBlank()) {
            Device device = deviceRepo.findByDeviceCode(req.getDeviceCode())
                    .orElseThrow(() -> new RuntimeException("Device not found"));
            session.setDeviceId(device.getDeviceId());
        }

        session.setStatus("PENDING");
        session.setSessionCode(generateSessionCode(studentId));

        sessionRepo.save(session);

        return new CreateEnrollSessionResponse(session.getSessionCode());
    }

    private String generateSessionCode(Integer studentId) {
        // Ví dụ: STU10-ABC123
        String random = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 6).toUpperCase();
        return "STU" + studentId + "-" + random;
    }

    // ---------- 3) POST /api/fingerprint/enroll/upload-from-device ----------
    // ESP32 gọi endpoint này với sessionCode + deviceCode + sensorSlot + templateBase64
    @Transactional
    public void uploadFromDevice(UploadFromDeviceRequest req) {
        if (req.getSessionCode() == null || req.getSessionCode().isBlank()) {
            throw new RuntimeException("sessionCode is required");
        }
        if (req.getDeviceCode() == null || req.getDeviceCode().isBlank()) {
            throw new RuntimeException("deviceCode is required");
        }
        if (req.getSensorSlot() == null) {
            throw new RuntimeException("sensorSlot is required");
        }

        FingerprintEnrollSession session = sessionRepo.findBySessionCode(req.getSessionCode())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if ("COMPLETED".equals(session.getStatus()) || "CANCELED".equals(session.getStatus())) {
            throw new RuntimeException("Session already closed");
        }

        // 1) Tìm device theo deviceCode
        Device device = deviceRepo.findByDeviceCode(req.getDeviceCode())
                .orElseThrow(() -> new RuntimeException("Device not found: " + req.getDeviceCode()));

        session.setDeviceId(device.getDeviceId());
        session.setSensorSlot(req.getSensorSlot());

        // 2) Template (optional, chưa dùng thực sự)
        if (req.getTemplateBase64() != null && !req.getTemplateBase64().isBlank()) {
            byte[] tpl = Base64.getDecoder().decode(req.getTemplateBase64());
            session.setTemplateData(tpl);
        }

        session.setStatus("RECEIVED");
        sessionRepo.save(session);
    }

    // ---------- 4) GET /api/fingerprint/enroll/temp?sessionCode=... ----------
    @Transactional(readOnly = true)
    public CheckEnrollTempResponse checkTemp(String sessionCode) {
        FingerprintEnrollSession session = sessionRepo.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // CHỈ CẦN sensorSlot != null là coi như ESP đã gửi slot về
        boolean has = session.getSensorSlot() != null;
        Integer slot = has ? session.getSensorSlot() : null;

        return new CheckEnrollTempResponse(has, slot);
    }

    // ---------- 5) POST /api/fingerprint/enroll/confirm ----------
    @Transactional
    public ConfirmEnrollResponse confirmEnroll(ConfirmEnrollRequest req) {
        if (req.getStudentId() == null || req.getSessionCode() == null) {
            throw new RuntimeException("studentId & sessionCode are required");
        }

        FingerprintEnrollSession session = sessionRepo.findBySessionCode(req.getSessionCode())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!Objects.equals(session.getStudentId(), req.getStudentId())) {
            throw new RuntimeException("Session không thuộc về sinh viên này");
        }

        if (session.getSensorSlot() == null) {
            throw new RuntimeException("Chưa nhận được sensorSlot từ thiết bị");
        }

        // Template: nếu ESP chưa gửi, tạm lưu mảng rỗng để tránh NULL (vì hiện chưa dùng template thực sự)
        byte[] template = (session.getTemplateData() != null)
                ? session.getTemplateData()
                : new byte[0];

        // 1) Lưu/Update FingerprintTemplate (master)
        FingerprintTemplate tpl = templateRepo.findByStudentId(req.getStudentId())
                .orElseGet(FingerprintTemplate::new);
        tpl.setStudentId(req.getStudentId());
        tpl.setTemplateData(template);    // LUÔN KHÔNG NULL
        templateRepo.save(tpl);

        // 2) Lưu/Update DeviceFingerprintSlot (map slot trên thiết bị đã enroll)
        if (session.getDeviceId() != null) {
            Integer deviceId = session.getDeviceId();
            Integer sensorSlot = session.getSensorSlot();

            // --- GIẢI QUYẾT UNIQUE (DeviceId, SensorSlot) ---
            // Nếu slot này đang thuộc về SV khác thì xóa slot cũ để giải phóng
            slotRepo.findByIdDeviceIdAndSensorSlot(deviceId, sensorSlot)
                    .ifPresent(existing -> {
                        Integer oldStudentId = existing.getId().getStudentId();
                        if (!Objects.equals(oldStudentId, req.getStudentId())) {
                            // thu hồi slot khỏi SV cũ
                            slotRepo.delete(existing);
                        }
                    });

            // Bây giờ tạo / update theo PK (DeviceId, StudentId)
            DeviceFingerprintSlotId id = new DeviceFingerprintSlotId();
            id.setDeviceId(deviceId);
            id.setStudentId(req.getStudentId());

            DeviceFingerprintSlot slot = slotRepo.findById(id)
                    .orElseGet(DeviceFingerprintSlot::new);
            slot.setId(id);
            slot.setSensorSlot(sensorSlot);
            slotRepo.save(slot);
        }

        // 3) Cập nhật trạng thái session
        session.setStatus("COMPLETED");
        sessionRepo.save(session);

        // 4) Trả response về cho frontend
        return new ConfirmEnrollResponse(
                true,
                "Enroll confirmed",
                session.getSensorSlot(),
                req.getStudentId()
        );
    }
    // ---------- 6) DEVICE: lấy "lệnh enroll" tiếp theo cho 1 ESP theo deviceCode ----------
    // Flow:
    //  - Frontend tạo session với deviceCode -> session.status = "PENDING"
    //  - ESP gọi API này: /api/fingerprint/enroll/next-command?deviceCode=...
    //  - Service tìm session PENDING của device đó, đổi sang WAITING_DEVICE, trả về sessionCode
    //  - Nếu không có, trả null
    // Thêm vào FingerprintEnrollService

    // 1. Hàm tạo lệnh đồng bộ cho tất cả thiết bị chưa có vân tay này
    @Transactional
    public void createSyncTasks(Integer studentId) {
        // 1) Lấy template gốc của sinh viên
        FingerprintTemplate template = templateRepo.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Sinh viên chưa có vân tay"));

        // 2) Lấy tất cả Device hiện đang ACTIVE
        List<Device> devices = deviceRepo.findByIsActiveTrue();

        // 3) Lấy danh sách các Device mà sinh viên này ĐÃ có vân tay
        //    (tức là không cần sync nữa, bao gồm cả "máy 1" – nơi đã enroll)
        List<DeviceFingerprintSlot> existingSlots = slotRepo.findByIdStudentId(studentId);

        // Tập hợp các deviceId đã có vân tay của SV này
        Set<Integer> deviceIdsAlreadyHas = existingSlots.stream()
                .map(s -> s.getId().getDeviceId())
                .collect(Collectors.toSet());

        // 4) Tạo task SYNC CHỈ CHO NHỮNG DEVICE CHƯA CÓ VÂN TAY CỦA SV
        for (Device device : devices) {
            Integer deviceId = device.getDeviceId();

            // Nếu device này đã có vân tay của studentId -> bỏ qua, KHÔNG sync
            if (deviceIdsAlreadyHas.contains(deviceId)) {
                continue;
            }

            // (tuỳ chọn) Nếu bạn muốn bỏ qua luôn các device không có room / không dùng điểm danh
            // if (device.getRoom() == null) continue;

            FingerprintEnrollSession syncSession = new FingerprintEnrollSession();
            syncSession.setSessionCode("SYNC-" + UUID.randomUUID().toString().substring(0, 6));
            syncSession.setStudentId(studentId);
            syncSession.setDeviceId(deviceId);
            syncSession.setTemplateData(template.getTemplateData());
            syncSession.setStatus("PENDING_SYNC");

            sessionRepo.save(syncSession);
        }
    }


    // 2. Sửa hàm getNextCommand để ưu tiên lệnh SYNC
// (Sửa lại logic cũ một chút)
    @Transactional
    public String getNextEnrollSessionForDevice(String deviceCode) {
        Device device = deviceRepo.findByDeviceCode(deviceCode).orElseThrow();

        // Ưu tiên 1: Tìm lệnh SYNC trước (để đẩy vân tay xuống)
        Optional<FingerprintEnrollSession> syncTask = sessionRepo
                .findFirstByDeviceIdAndStatusOrderByCreatedAtAsc(device.getDeviceId(), "PENDING_SYNC");

        if (syncTask.isPresent()) {
            FingerprintEnrollSession s = syncTask.get();
            s.setStatus("SYNCING");
            sessionRepo.save(s);
            // Format trả về đặc biệt để ESP biết là lệnh download: "SYNC|sessionCode"
            return "SYNC|" + s.getSessionCode();
        }

        // Ưu tiên 2: Tìm lệnh Enroll (như cũ)
        return sessionRepo
                .findFirstByDeviceIdAndStatusOrderByCreatedAtAsc(device.getDeviceId(), "PENDING")
                .map(s -> {
                    s.setStatus("WAITING_DEVICE");
                    sessionRepo.save(s);
                    return "ENROLL|" + s.getSessionCode(); // Thêm prefix cho rõ
                })
                .orElse(null);
    }

    // 3. Thêm hàm API cho ESP tải dữ liệu Template
// GET /api/fingerprint/sync/data?sessionCode=...
    public byte[] getSyncData(String sessionCode) {
        FingerprintEnrollSession s = sessionRepo.findBySessionCode(sessionCode).orElseThrow();
        return s.getTemplateData();
    }

    @Transactional(readOnly = true)
    public String getTemplateDataBase64(String sessionCode) {
        // 1. Tìm session
        FingerprintEnrollSession session = sessionRepo.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionCode));

        // 2. Lấy mảng byte
        byte[] data = session.getTemplateData();
        if (data == null || data.length == 0) {
            throw new RuntimeException("Session này không có dữ liệu Template (null)");
        }

        // 3. Encode sang Base64 và trả về
        return Base64.getEncoder().encodeToString(data);
    }


    @Transactional
    public void handleSyncResult(SyncResultRequest req) {
        if (req.getSessionCode() == null || req.getSessionCode().isBlank()) {
            throw new RuntimeException("sessionCode is required");
        }
        if (req.getSensorSlot() == null) {
            throw new RuntimeException("sensorSlot is required");
        }

        // 1) Tìm session
        FingerprintEnrollSession session = sessionRepo.findBySessionCode(req.getSessionCode())
                .orElseThrow(() -> new RuntimeException("Session not found: " + req.getSessionCode()));

        if (!"SYNCING".equals(session.getStatus()) && !"PENDING_SYNC".equals(session.getStatus())) {
            // tuỳ bạn, có thể bỏ check này
            throw new RuntimeException("Session is not in SYNCING/PENDING_SYNC status");
        }

        if (session.getDeviceId() == null) {
            throw new RuntimeException("Session has no deviceId");
        }

        Integer deviceId = session.getDeviceId();
        Integer studentId = session.getStudentId();
        Integer sensorSlot = req.getSensorSlot();

        // 2) đảm bảo (DeviceId, SensorSlot) là unique
        slotRepo.findByIdDeviceIdAndSensorSlot(deviceId, sensorSlot)
                .ifPresent(existing -> {
                    Integer oldStudentId = existing.getId().getStudentId();
                    if (!Objects.equals(oldStudentId, studentId)) {
                        // thu hồi slot khỏi sinh viên khác
                        slotRepo.delete(existing);
                    }
                });

        // 3) Tạo / update DeviceFingerprintSlot theo PK (DeviceId, StudentId)
        DeviceFingerprintSlotId id = new DeviceFingerprintSlotId();
        id.setDeviceId(deviceId);
        id.setStudentId(studentId);

        DeviceFingerprintSlot slot = slotRepo.findById(id)
                .orElseGet(DeviceFingerprintSlot::new);
        slot.setId(id);
        slot.setSensorSlot(sensorSlot);
        slotRepo.save(slot);

        // 4) Cập nhật session (để bạn dễ debug / theo dõi)
        session.setSensorSlot(sensorSlot);
        session.setStatus("SYNC_DONE");  // hoặc "COMPLETED_SYNC" tuỳ bạn
        sessionRepo.save(session);
    }

}
