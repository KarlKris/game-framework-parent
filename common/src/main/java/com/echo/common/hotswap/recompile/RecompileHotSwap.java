package com.echo.common.hotswap.recompile;

import cn.hutool.core.io.FileUtil;
import com.echo.common.util.StringUtils;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 新版热更
 *
 * <a href="https://yeas.fun/archives/java-hotswap-compile"/a>
 *
 * <pre>
 * 原理：
 * 1.动态生成子类，替换原有父类实现
 *
 * 重点：
 * 1.因为采取继承的方式，则类必须要有构造函数！
 * 2.方法内的final方法无法覆写，则无法热更，动态生成的子类会忽略final方法
 * 3.需要借助对象注册机制,简单理解就是对象的映射关系,进行热更时,只要把当前注册的对象替换为新对象，
 *   因为新对象是原对象的子类，可覆写方法，从而实现逻辑的替换
 *   同时因为是借助对象注册机制,可以轻松实现动态新增类
 *
 * </pre>
 *
 */
public class RecompileHotSwap {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecompileHotSwap.class);

    /** 子类后缀名 */
    public static final String SUBCLASS_SUFFIX = "$$SUBCLASS";

    /**
     * 获取需要替换的新类的Class
     *
     * @param oldClass 原class名字
     * @return
     * @throws IOException
     */
    public static Class<?> recompileClass(Class<?> oldClass) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass oldCtClass;
        try {
            oldCtClass = pool.getCtClass(oldClass.getName());
        } catch (NotFoundException e) {
            LOGGER.error("javassist.NotFoundException: {}", oldClass.getName());
            // https://www.javassist.org/tutorial/tutorial.html
            // 页面搜索"Class search path"
            // 运行在web容器(如tomcat)中的程序,可能存在多个ClassLoader,导致ClassPool.getDefault()找不到对应的class
            pool.insertClassPath(new ClassClassPath(oldClass));
            oldCtClass = pool.getCtClass(oldClass.getName());
        }

        // 从class文件中获取CtClass
        CtClass newCtClass;
        try (InputStream classInputStream = getClassInputStream(oldClass)) {
            newCtClass = pool.makeClass(classInputStream);
        }

        String newClassName = oldClass.getSimpleName() + SUBCLASS_SUFFIX;
        String newFullClassName = oldClass.getName() + SUBCLASS_SUFFIX;

        // 新类改名，设置父类为原来的类
        newCtClass.replaceClassName(oldClass.getName(), newFullClassName);
        newCtClass.setSuperclass(oldCtClass);

        // 如果有默认构造函数，则调用父类构造函数
        CtConstructor constructor = newCtClass.getDeclaredConstructor(new CtClass[0]);
        if (constructor == null) {
            throw new RuntimeException("has no default constructor:" + oldClass.getName());
        }

        if (Modifier.isPrivate(constructor.getModifiers())) {
            throw new RuntimeException("the constructor is private, cannot extend:" + oldClass.getName());
        }

        // 设置子类的构造函数为public的，方便后面newInstance
        constructor.setModifiers(Modifier.PUBLIC);
        // 设置默认构造函数为 super();
        constructor.setBody("super();");

        // final的方法忽略
        CtMethod[] declaredMethods = newCtClass.getDeclaredMethods();
        for (CtMethod declaredMethod : declaredMethods) {
            int modifiers = declaredMethod.getModifiers();
            boolean isPrivate = Modifier.isPrivate(modifiers);
            boolean isFinal = Modifier.isFinal(modifiers);
            if (!isPrivate && isFinal) {
                // 从新类中移除
                LOGGER.error("{}.{}(), method is removed in newClass", oldClass, declaredMethod.getName());
                newCtClass.removeMethod(declaredMethod);
            }
        }

        // 二进制内容
        byte[] targetBytes = newCtClass.toBytecode();

        // 移除临时类，让它从CtPool中移除，以便多次热更
        newCtClass.detach();

        dump(targetBytes, newClassName);

        // 重新获取新类
        RecompileClassLoader recompileClassLoader = new RecompileClassLoader(oldClass.getClassLoader(), newFullClassName,
                targetBytes);
        return recompileClassLoader.findClass(newFullClassName);
    }

    private static void dump(byte[] targetBytes, String className) throws IOException {
        // class dump到日志中
        String basePath = new File("").getAbsolutePath();

        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        String time = localDateTime.format(pattern).replace(" ", "T");

        String finalPath = StringUtils.join(
                new String[]{basePath, "recompile-output", className + "-" + time + ".class"},
                File.separator);
        File to = new File(finalPath);
        FileUtil.mkParentDirs(to);
        LOGGER.error("class dumpd: {}", to.getAbsolutePath());
        FileUtil.writeBytes(targetBytes, to);
    }

    private static InputStream getClassInputStream(Class<?> clazz) throws Exception {
        String classLocation = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();

        if (classLocation.endsWith(".jar")) {
            throw new IOException("cannot recompile class from jar: " + clazz);
        } else {
            String clazzName = clazz.getName().replace('.', '/') + ".class";
            return Files.newInputStream(new File(classLocation, clazzName).toPath());
        }
    }
}
