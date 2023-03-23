package com.li.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

/**
 * 热更Agent方式
 */
public class HotSwapAgent {

    private static Instrumentation instrumentation;

    private static Object lock = new Object();

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public void setInstrumentation(Instrumentation ins) {
        HotSwapAgent.instrumentation = ins;
    }

    public static void agentmain(String args, Instrumentation ins) {
        synchronized (lock) {
            boolean suc = false;
            for (Class<?> loadClz : ins.getAllLoadedClasses()) {
                if (!"HotSwap".equalsIgnoreCase(loadClz.getSimpleName())) {
                    continue;
                }
                try {
                    ClassLoader classLoader = loadClz.getClassLoader();
                    Class<?> hotSwapAgentClz = classLoader.loadClass(HotSwapAgent.class.getName());
                    Method method = hotSwapAgentClz.getDeclaredMethod("setInstrumentation", Instrumentation.class);
                    method.invoke(null, ins);
                    System.out.println("HotSwapAgent.setInstrumentation: " + ins);
                    suc = true;
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }



}
