package com.echo.common.hotswap.agent;

import com.echo.common.hotswap.HotSwapMain;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import org.junit.Test;

import java.io.IOException;
import java.lang.instrument.UnmodifiableClassException;

/**
 * 代码热更 agent
 */
public class HotSwapTest {

    @Test
    public void hotswapTest() throws UnmodifiableClassException, AgentLoadException
            , IOException, AttachNotSupportedException, AgentInitializationException, ClassNotFoundException {
        HotSwapMain hotSwapMain = new HotSwapMain();
        new Thread(hotSwapMain::circle).start();
        // 30秒后热更
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HotSwap.hotswap(new String[] {HotSwapMain.class.getName()});
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
