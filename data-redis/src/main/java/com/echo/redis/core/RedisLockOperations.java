package com.echo.redis.core;

public interface RedisLockOperations {

    /**
     * 对key进行加锁(阻塞锁)
     *
     * @param key key
     */
    void lock(String key);

    /**
     * 对key尝试进行加锁(非阻塞锁)
     *
     * @param key key
     * @return 是否加锁成功
     */
    boolean tryLock(String key);

    /**
     * 对key进行解锁
     *
     * @param key key
     */
    void unlock(String key);

}
