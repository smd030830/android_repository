package com.example.doglogbackend;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(boolean success, String userType) {
    public static LoginResponse failure() {
        return new LoginResponse(false, null);
    }
}
