package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.UserDetailDto;
import com.FinalProject.backend.Dto.UserListDto;
import com.FinalProject.backend.Repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTableService {

    @Autowired
    private AccountRepository accountRepository;

    // LẤY DANH SÁCH ĐỂ ĐỔ BẢNG + EXPORT
    public List<UserListDto> getAllUsers() {
        List<Object[]> rows = accountRepository.findAllUserTable();
        return rows.stream().map(r -> {
            int i = 0;
            UserListDto dto = new UserListDto();
            dto.setAccountId((Integer) r[i++]);     // 0
            dto.setFullName((String) r[i++]);       // 1
            dto.setUsername((String) r[i++]);       // 2
            dto.setRoleName((String) r[i++]);       // 3
            dto.setEmail((String) r[i++]);          // 4
            dto.setTeacherId((Integer) r[i++]);     // 5
            dto.setStudentId((Integer) r[i++]);     // 6
            dto.setPhone((String) r[i++]);          // 7
            dto.setAddress((String) r[i++]);        // 8
            dto.setDateOfBirth((String) r[i++]);    // 9
            dto.setGender((String) r[i++]);         // 10
            dto.setFingerCount((Integer) r[i++]);   // 11
            return dto;
        }).toList();
    }

    // LẤY CHI TIẾT MODAL (bạn đang dùng UserDetailDto thì cứ để, không sao)
    public UserDetailDto getUserDetail(int accountId) {
        Object result = accountRepository.findUserDetailByAccountId(accountId);
        if (result == null) return null;

        Object[] r = (Object[]) result;
        int i = 0;
        UserDetailDto dto = new UserDetailDto();
        dto.setAccountId((Integer) r[i++]);  // 0
        dto.setUsername((String) r[i++]);    // 1
        dto.setRoleName((String) r[i++]);    // 2
        dto.setFullName((String) r[i++]);    // 3
        dto.setEmail((String) r[i++]);       // 4
        dto.setTeacherId((Integer) r[i++]);  // 5
        dto.setStudentId((Integer) r[i++]);  // 6
        dto.setPhone((String) r[i++]);       // 7
        dto.setAddress((String) r[i++]);     // 8
        dto.setDateOfBirth((String) r[i++]); // 9
        dto.setGender((String) r[i++]);      // 10
        dto.setFingerCount((Integer) r[i++]); // 11
        return dto;
    }
}
