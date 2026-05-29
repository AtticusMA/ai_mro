package com.mro.common.core.util;

import com.mro.common.core.annotation.Encrypted;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * 递归遍历对象中所有 @Encrypted 注解的 String 字段，执行 SM4 加密或解密。
 * key/iv 由调用方从请求上下文中提取后传入。
 */
public class FieldEncryptionUtil {

    public static void encrypt(Object obj, byte[] key, byte[] iv) {
        process(obj, key, iv, true);
    }

    public static void decrypt(Object obj, byte[] key, byte[] iv) {
        process(obj, key, iv, false);
    }

    private static void process(Object obj, byte[] key, byte[] iv, boolean encrypt) {
        if (obj == null) {
            return;
        }
        if (obj instanceof Collection<?> col) {
            for (Object item : col) {
                process(item, key, iv, encrypt);
            }
            return;
        }
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Encrypted.class)) {
                    continue;
                }
                if (field.getType() != String.class) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    String value = (String) field.get(obj);
                    if (value == null || value.isBlank()) {
                        continue;
                    }
                    String result = encrypt
                            ? SM4Util.encrypt(value, key, iv)
                            : SM4Util.decrypt(value, key, iv);
                    field.set(obj, result);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("FieldEncryptionUtil: cannot access field " + field.getName(), e);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
