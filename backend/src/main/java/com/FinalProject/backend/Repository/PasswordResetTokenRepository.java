package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByAccountIdAndCodeAndIsUsedFalse(Integer accountId, String code);
}
