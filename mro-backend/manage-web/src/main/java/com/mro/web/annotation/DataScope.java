package com.mro.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Controller 方法上，触发数据权限计算。
 * AOP 切面会根据当前用户角色的 dataScope 算出可见 deptId 列表，
 * 通过 Dubbo Attachment 传递给下游 service。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
}
