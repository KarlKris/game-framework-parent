package com.echo.common.concurrency;

/**
 * RunnableLoop选择器工厂
 *
 * @author li-yuanwen
 * @date 2022/7/15
 */
public interface RunnableLoopChooserFactory {

    /**
     * 根据RunnableLoop数量创建相应的选择器
     *
     * @param loops RunnableLoop数组
     * @return 选择器
     */
    RunnableLoopChooser newChooser(RunnableLoop[] loops);


    interface RunnableLoopChooser {

        /**
         * 选择下一个RunnableLoop
         *
         * @return RunnableLoop
         */
        RunnableLoop next();

    }

}
