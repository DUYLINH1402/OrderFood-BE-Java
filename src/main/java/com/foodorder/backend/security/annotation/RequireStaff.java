package com.foodorder.backend.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để kiểm tra quyền Staff
 * Cho phép user có role STAFF, ADMIN hoặc SUPER_ADMIN truy cập
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('STAFF') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public @interface RequireStaff {
}
