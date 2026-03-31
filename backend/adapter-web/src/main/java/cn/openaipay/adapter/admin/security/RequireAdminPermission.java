package cn.openaipay.adapter.admin.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  * RequireAdminPermission说明。
 *
 * @author: tenggk.ai
 * @date: 2026/03/01
   */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdminPermission {

    String value();
}
