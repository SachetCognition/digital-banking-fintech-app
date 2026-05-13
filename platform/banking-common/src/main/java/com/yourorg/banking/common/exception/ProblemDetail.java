package com.yourorg.banking.common.exception;

import java.time.Instant;

public record ProblemDetail(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    Instant timestamp,
    String traceId
) {}
