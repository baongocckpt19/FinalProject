package com.FinalProject.backend.Controllers;

import com.FinalProject.backend.Dto.UserDetailDto;
import com.FinalProject.backend.Dto.UserListDto;
import com.FinalProject.backend.Service.UserTableService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserTableService userTableService;

    public UserController(UserTableService userTableService) {
        this.userTableService = userTableService;
    }
    // Lấy danh sách tất cả người dùng để hiển thị bảng
    @GetMapping
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userTableService.getAllUsers());
    }


    // Lấy chi tiết 1 người dùng để hiện modal
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserDetail(@PathVariable int id) {
        UserDetailDto dto = userTableService.getUserDetail(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
    // Export danh sách người dùng ra file CSV để mở bằng Excel
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportUsersCsv() {
        var users = userTableService.getAllUsers(); // dùng lại service cũ

        StringBuilder sb = new StringBuilder();

        // 1) Header tiếng Việt, đúng thứ tự bạn muốn
        sb.append("STT,Tên,Tên tài khoản ,Ngày sinh,Giới tính,Chức vụ,Email,Số điện thoại,Địa chỉ\n");

        int stt = 1;
        for (var u : users) {
            String fullName   = safe(u.getFullName());
            String username   = safe(u.getUsername());
            String dateOfBirth = safe(u.getDateOfBirth());  // đã map từ query chi tiết
            String gender     = safe(u.getGender());
            String role       = safe(u.getRoleName());
            String email      = safe(u.getEmail());
            String phone      = safe(u.getPhone());
            String address    = safe(u.getAddress());

            sb.append(stt++).append(",");
            sb.append(csv(fullName)).append(",");
            sb.append(csv(username)).append(",");
            sb.append(csv(dateOfBirth)).append(",");
            sb.append(csv(gender)).append(",");
            sb.append(csv(role)).append(",");
            sb.append(csv(email)).append(",");
            sb.append(csv(phone)).append(",");
            sb.append(csv(address)).append("\n");
        }

        // 2) Thêm BOM UTF-8 ở đầu để Excel hiểu tiếng Việt
        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] data = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        byte[] result = new byte[bom.length + data.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(data, 0, result, bom.length, data.length);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.csv")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(result);
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    // bọc nếu có dấu phẩy
    private String csv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = v.replace("\"", "\"\"");
            return "\"" + v + "\"";
        }
        return v;
    }

}
