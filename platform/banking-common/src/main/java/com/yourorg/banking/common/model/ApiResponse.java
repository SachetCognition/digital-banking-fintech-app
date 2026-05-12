package com.yourorg.banking.common.model;

import java.time.Instant;

public record ApiResponse<T>(
    T data,
    String message,
    Instant timestamp,
    String requestId
) {
    public static <T> ApiResponse<T> success(T data, String requestId) {
        return new ApiResponse<>(data, "Success", Instant.now(), requestId);
    }

    public static <T> ApiResponse<T> success(T data, String message, String requestId) {
        return new ApiResponse<>(data, message, Instant.now(), requestId);
    }
}
