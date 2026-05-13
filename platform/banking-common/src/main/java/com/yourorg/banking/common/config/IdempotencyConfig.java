package com.yourorg.banking.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IdempotencyConfig {
    String headerName() default "Idempotency-Key";
    long ttlSeconds() default 86400;
}
