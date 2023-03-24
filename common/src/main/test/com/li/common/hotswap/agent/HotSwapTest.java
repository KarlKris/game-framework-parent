package com.li.common.hotswap.agent;

import com.li.common.hotswap.HotSwapMain;
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
        HotSwap.hotswap(new String[] {HotSwapMain.class.getName()});
    }

}
