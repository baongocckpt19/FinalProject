//ĐÂY LÀ CUSTOMUSERDETAIL.JAVA
package com.FinalProject.backend.Dto;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.*;

@Data
public class CustomUserDetail implements UserDetails {
    private final AccountDto account;

    public CustomUserDetail(AccountDto account) {
        this.account = account;
    }

    public int getId() {
        return account.getAccountId();
    }

    public String getRole() {
        return account.getRoleName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Chuẩn Spring Security: "ROLE_Admin", "ROLE_Giảng viên", ...
        return List.of(new SimpleGrantedAuthority("ROLE_" + account.getRoleName()));
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

