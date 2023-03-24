package com.li.common.hotswap;

/**
 * 热更方法main
 */
public class HotSwapMain {

    public int getInt() {
        return 1;
    }

    public static void main(String[] args) {
        HotSwapMain hotSwapMain = new HotSwapMain();
        while (hotSwapMain.getInt() == 1) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("跳出main循环");
    }



}
