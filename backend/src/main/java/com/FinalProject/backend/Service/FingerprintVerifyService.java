package com.FinalProject.backend.Service;

import com.FinalProject.backend.Dto.FingerprintVerifyRequest;
import com.FinalProject.backend.Dto.FingerprintVerifyResponse;

public interface FingerprintVerifyService {
    FingerprintVerifyResponse verify(FingerprintVerifyRequest request);
}
