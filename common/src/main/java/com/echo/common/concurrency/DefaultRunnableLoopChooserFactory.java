package com.echo.common.concurrency;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RunnableLoopChooserFactory实现
 *
 * @author li-yuanwen
 * @date 2022/7/15
 */
public class DefaultRunnableLoopChooserFactory implements RunnableLoopChooserFactory {

    public static final DefaultRunnableLoopChooserFactory INSTANCE = new DefaultRunnableLoopChooserFactory();

    @Override
    public RunnableLoopChooser newChooser(RunnableLoop[] loops) {
        if (isPowerOfTwo(loops.length)) {
            return new PowerOfTwoRunnableLoopChooser(loops);
        } else {
            return new GenericRunnableLoopChooser(loops);
        }
    }

    private static boolean isPowerOfTwo(int val) {
        return (val & -val) == val;
    }

    private static final class PowerOfTwoRunnableLoopChooser implements RunnableLoopChooser {

        private final AtomicInteger idx = new AtomicInteger();
        private final RunnableLoop[] loops;

        PowerOfTwoRunnableLoopChooser(RunnableLoop[] loops) {
            this.loops = loops;
        }

        @Override
        public RunnableLoop next() {
            return loops[idx.getAndIncrement() & loops.length];
        }
    }

    private static final class GenericRunnableLoopChooser implements RunnableLoopChooser {

        private final AtomicLong idx = new AtomicLong();
        private final RunnableLoop[] loops;

        GenericRunnableLoopChooser(RunnableLoop[] loops) {
            this.loops = loops;
        }

        @Override
        public RunnableLoop next() {
            return loops[(int) Math.abs(idx.getAndIncrement() % loops.length)];
        }
    }

}
