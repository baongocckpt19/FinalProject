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
        accountDto.setAccountId((Integer) user[0]);
        accountDto.setUsername((String) user[1]);
        accountDto.setPassword((String) user[2]);
        accountDto.setRoleId((Integer) user[3]);
        accountDto.setRoleName((String) user[4]);
        return accountDto;
    }

    public AccountDto getUser(int id) {
        Object result = accountRepository.findById(id);
        if (result == null) {
            return null;
        }
        Object[] user = (Object[]) result;
        AccountDto accountDto = new AccountDto();
        accountDto.setAccountId((Integer) user[0]);
        accountDto.setUsername((String) user[1]);
        accountDto.setPassword((String) user[2]);
        accountDto.setRoleId((Integer) user[3]);
        accountDto.setRoleName((String) user[4]);
        return accountDto;
    }

    @Transactional
    public void softDeleteAccount(int id) {
        accountRepository.softDeleteAccount(id);
    }


    // cách mã hoá password: String hashedPassword = passwordEncoder.encode(body.getPassword() + passwordSecret);
    // cách check !passwordEncoder.matches(body.getPassword() + passwordSecret, user.getPasswordHash())
}
