package com.echo.common.hotswap.recompile;

import com.echo.common.hotswap.HotSwapMain;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于代理模式 热更代码
 */
public class RecompileHotSwapTest {

    private static final Logger log = LoggerFactory.getLogger(RecompileHotSwapTest.class);

    @Test
    public void hotswapTest() throws Exception {
        final Map<Integer, HotSwapMain> map = new HashMap<>(1);
        Class<?> oldClass = HotSwapMain.class;
        log.error("热更新前的类：{}" , oldClass);
        HotSwapMain hotSwapMain = new HotSwapMain();
        map.put(1, hotSwapMain);
        new Thread(() -> {
            System.out.println("开始circle循环");
            while (map.get(1).getInt() == 1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("跳出circle循环");
        }).start();
        // 30秒后热更
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Class<HotSwapMain> newClass = (Class<HotSwapMain>) RecompileHotSwap.recompileClass(oldClass);
        map.put(1, newClass.newInstance());
        log.error("热更新后的类(已替换为原类的子类)：{}" , newClass);
        // newClass是oldClass的子类
        Assert.assertTrue(oldClass.isAssignableFrom(newClass));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        String replace = localDateTime.format(pattern).replace(" ", "T");
        System.out.println(replace);
    }

}
