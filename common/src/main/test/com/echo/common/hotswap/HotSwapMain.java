package com.echo.common.hotswap;

/**
 * 热更方法circle0
 */
public class HotSwapMain {

    public void circle() {
        System.out.println("开始circle循环");
        while (getInt() == 1) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("跳出circle循环");
    }

    public int getInt() {
        return 1;
    }


}
