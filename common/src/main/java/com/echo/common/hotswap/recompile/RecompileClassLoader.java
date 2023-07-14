package com.echo.common.hotswap.recompile;

/**
 * 用于重新加载某个指定类,从而实现将修改后的class加载进入jvm
 */
public class RecompileClassLoader extends ClassLoader {

    private final String className;
    private byte[] byteCodes;

    private Class<?> defineClass;

    public RecompileClassLoader(ClassLoader parent, String className, byte[] byteCodes) {
        super(parent);
        this.className = className;
        this.byteCodes = byteCodes;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        if (!name.equals(className)) {
            return null;
        }

        if (this.defineClass != null) {
            return this.defineClass;
        }

        this.defineClass = super.defineClass(name, byteCodes, 0, byteCodes.length);
        // 清空字节数组的内存，释放内存
        this.byteCodes = null;
        return defineClass;
    }
}
