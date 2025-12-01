package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.DeviceRequest;
import com.FinalProject.backend.Models.Device;
import com.FinalProject.backend.Repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepo;

    public DeviceService(DeviceRepository deviceRepo) {
        this.deviceRepo = deviceRepo;
    }

    public List<Device> getAll() {
        return deviceRepo.findAll();
    }

    public List<Device> getActive() {
        return deviceRepo.findByIsActiveTrue();
    }

    public Optional<Device> getById(Integer id) {
        return deviceRepo.findById(id);
    }

    public String create(DeviceRequest req) {
        if (req.getDeviceCode() == null || req.getDeviceCode().trim().isEmpty()) {
            return "DeviceCode không được để trống";
        }

        String code = req.getDeviceCode().trim();
        if (deviceRepo.findByDeviceCode(code).isPresent()) {
            return "DeviceCode đã tồn tại";
        }

        Device d = new Device();
        d.setDeviceCode(code);
        d.setDeviceName(req.getDeviceName());
        d.setRoom(req.getRoom());
        d.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);

        deviceRepo.save(d);
        return "Tạo thiết bị thành công";
    }

    public String update(Integer id, DeviceRequest req) {
        Optional<Device> opt = deviceRepo.findById(id);
        if (opt.isEmpty()) return "Device không tồn tại";

        Device d = opt.get();

        if (req.getDeviceCode() == null || req.getDeviceCode().trim().isEmpty()) {
            return "DeviceCode không được để trống";
        }

        String newCode = req.getDeviceCode().trim();

        Optional<Device> sameCode = deviceRepo.findByDeviceCode(newCode);
        if (sameCode.isPresent() && !sameCode.get().getDeviceId().equals(id)) {
            return "DeviceCode đã tồn tại";
        }

        d.setDeviceCode(newCode);
        d.setDeviceName(req.getDeviceName());
        d.setRoom(req.getRoom());

        if (req.getIsActive() != null) {
            d.setIsActive(req.getIsActive());
        }

        deviceRepo.save(d);
        return "Cập nhật thiết bị thành công";
    }

    public String delete(Integer id) {
        if (!deviceRepo.existsById(id)) {
            return "Device không tồn tại";
        }
        deviceRepo.deleteById(id);
        return "Xóa thiết bị thành công";
    }

    public String updateStatus(Integer id, boolean isActive) {
        Optional<Device> opt = deviceRepo.findById(id);
        if (opt.isEmpty()) return "Device không tồn tại";

        Device d = opt.get();
        d.setIsActive(isActive);
        deviceRepo.save(d);
        return "Cập nhật trạng thái thiết bị thành công";
    }
}
