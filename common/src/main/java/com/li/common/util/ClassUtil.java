package com.li.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class相关工具类
 */
public class ClassUtil {

    public static InputStream getClassInputStream(Class<?> clazz) throws Exception {
        String classLocation = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();

        if (classLocation.endsWith(".jar")) {
            throw new IOException("cannot recompile class from jar: " + clazz);
        } else {
            String clazzName = clazz.getName().replace('.', '/') + ".class";
            return new FileInputStream(new File(classLocation, clazzName));
        }
    }
}