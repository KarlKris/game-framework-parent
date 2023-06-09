package com.li.common.hotswap.agent;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.li.agent.HotSwapAgent;
import com.li.common.util.StringUtils;
import com.li.agent.HotSwapAgentLocation;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * agent方式热更代码
 */
public class HotSwap {

    public static final Logger LOGGER = LoggerFactory.getLogger(HotSwap.class);

    /** 热更agent.jar路径 **/
    private static final String AGENT_JAR_PATH;
    /** VirtualMachine **/
    private static VirtualMachine VM;
    /** 进程pid **/
    private static final String PID;

    static {
        AGENT_JAR_PATH = getAgentJarPath();
        LOGGER.error("java hotswap agent path: {}", AGENT_JAR_PATH);

        // 当前进程pid
        String name = ManagementFactory.getRuntimeMXBean().getName();
        PID = StringUtils.substringBefore(name, "@");
        LOGGER.error("current pid: {}", PID);
    }

    private static String getAgentJarPath() {
        // 基于jar包中的类定位jar包位置
        String path = HotSwapAgentLocation.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        // 定位绝对路径
        return new File(path).getAbsolutePath();
    }

    private static void init() throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        Instrumentation instrumentation = HotSwapAgent.getInstrumentation();
        if (instrumentation != null) {
            // 已经有此对象，则无需再次初始化获取
            return;
        }

        // 连接虚拟机，并attach当前agent的jar包
        // agentmain()方法会设置Instrumentation
        VM = VirtualMachine.attach(PID);
        VM.loadAgent(AGENT_JAR_PATH);

        // 从而获取到当前虚拟机
        Instrumentation ins = HotSwapAgent.getInstrumentation();
        if (ins == null) {
            LOGGER.error("java hotswap instrumentation is null");
        }
    }


    private static void destroy() throws IOException {
        if (VM != null) {
            VM.detach();
        }
        LOGGER.error("java hotswap agent redefine classes end");
    }


    /**
     * 代码热更
     * @param clzArray 需要热更的class全限定名 数组
     * @throws AgentLoadException
     * @throws IOException
     * @throws AttachNotSupportedException
     * @throws AgentInitializationException
     * @throws ClassNotFoundException
     * @throws UnmodifiableClassException
     */
    public static void hotswap(String[] clzArray) throws AgentLoadException, IOException, AttachNotSupportedException
            , AgentInitializationException, ClassNotFoundException, UnmodifiableClassException {
        LOGGER.error("java hotswap agent redefine classes started");
        init();

        try {
            LinkedHashMap<String, LinkedHashSet<Class<?>>> redefineMap = new LinkedHashMap<>();
            // 1.整理需要重定义的类
            List<ClassDefinition> classDefList = new ArrayList<ClassDefinition>();
            for (String className : clzArray) {
                Class<?> c = Class.forName(className);
                String classLocation = c.getProtectionDomain().getCodeSource().getLocation().getPath();
                LinkedHashSet<Class<?>> classSet = redefineMap.computeIfAbsent(classLocation,
                        k -> new LinkedHashSet<>());
                classSet.add(c);
            }
            if (!redefineMap.isEmpty()) {
                for (Map.Entry<String, LinkedHashSet<Class<?>>> entry : redefineMap.entrySet()) {
                    String classLocation = entry.getKey();
                    LOGGER.error("class read from:{}", classLocation);
                    if (classLocation.endsWith(".jar")) {
                        try (JarFile jf = new JarFile(classLocation)) {
                            for (Class<?> cls : entry.getValue()) {
                                String clazz = cls.getName().replace('.', '/') + ".class";
                                JarEntry je = jf.getJarEntry(clazz);
                                if (je != null) {
                                    LOGGER.error("class redefined:\t{}", clazz);
                                    try (InputStream stream = jf.getInputStream(je)) {
                                        byte[] data = IoUtil.readBytes(stream);
                                        classDefList.add(new ClassDefinition(cls, data));
                                    }
                                } else {
                                    throw new IOException("JarEntry " + clazz + " not found");
                                }
                            }
                        }
                    } else {
                        File file;
                        for (Class<?> cls : entry.getValue()) {
                            String clazz = cls.getName().replace('.', '/') + ".class";
                            file = new File(classLocation, clazz);
                            LOGGER.error("class redefined:{}", file.getAbsolutePath());
                            byte[] data =FileUtil.readBytes(file);
                            classDefList.add(new ClassDefinition(cls, data));
                        }
                    }
                }
                // 2.redefine
                HotSwapAgent.getInstrumentation().redefineClasses(classDefList.toArray(new ClassDefinition[0]));
            }
        } finally {
            destroy();
        }
    }

}
