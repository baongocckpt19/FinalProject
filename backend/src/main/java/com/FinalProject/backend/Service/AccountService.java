package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.AccountDto;
import com.FinalProject.backend.Repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    public AccountDto login(String username, String password) {
        Object result = accountRepository.login(username, password);
        if (result == null) {
            return null;
        }
        Object[] user = (Object[]) result;
        AccountDto accountDto = new AccountDto();
        int i = 0;
        accountDto.setAccountId((Integer) user[i++]);   // 0
        accountDto.setUsername((String) user[i++]);     // 1
        accountDto.setPassword((String) user[i++]);     // 2
        accountDto.setRoleId((Integer) user[i++]);      // 3
        accountDto.setRoleName((String) user[i++]);     // 4
        accountDto.setFullName((String) user[i++]);     // 5
        return accountDto;
    }


    public AccountDto getUser(int id) {
        Object result = accountRepository.findById(id);
        if (result == null) {
            return null;
        }
        Object[] user = (Object[]) result;
        AccountDto accountDto = new AccountDto();
        int i = 0;
        accountDto.setAccountId((Integer) user[i++]);   // 0
        accountDto.setUsername((String) user[i++]);     // 1
        accountDto.setPassword((String) user[i++]);     // 2
        accountDto.setRoleId((Integer) user[i++]);      // 3
        accountDto.setRoleName((String) user[i++]);     // 4
        accountDto.setFullName((String) user[i++]);     // 5
        return accountDto;
    }


    @Transactional
    public void softDeleteAccount(int id) {
        accountRepository.softDeleteAccount(id);
    }


    // cách mã hoá password: String hashedPassword = passwordEncoder.encode(body.getPassword() + passwordSecret);
    // cách check !passwordEncoder.matches(body.getPassword() + passwordSecret, user.getPasswordHash())
}
