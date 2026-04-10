package com.planit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendOtpRequest {

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    @NotBlank(message = "Purpose is required")
    @Pattern(regexp = "REGISTRATION|LOGIN|PASSWORD_RESET", message = "Invalid OTP purpose")
    private String purpose;
}
