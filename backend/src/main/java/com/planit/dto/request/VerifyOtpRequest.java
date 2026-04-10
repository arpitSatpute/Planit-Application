package com.planit.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "OTP ID is required")
    private String otpId;

    @NotBlank(message = "OTP is required")
    private String otp;
}
