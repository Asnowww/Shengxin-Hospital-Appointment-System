package org.example.backend.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要审计的接口或类
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {
    /**
     * 操作类型，留空则按 HTTP 方法推断
     */
    String action() default "";

    /**
     * 资源类型，留空则按 URL 片段推断
     */
    String resourceType() default "";

    /**
     * 额外说明
     */
    String message() default "";
}
