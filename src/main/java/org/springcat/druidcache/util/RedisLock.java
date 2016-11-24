package org.springcat.druidcache.util;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

;

/**
 * 基于redis的简易分布式锁
 * Created by springcat on 16/6/5.
 */
public class RedisLock {

    //单位毫秒
    private long lockTimeOut = 500;
    //重试前sleep时间
    private long perSleep = 50;

    protected JedisPool jedisPool;
    protected final ThreadLocal<Jedis> threadLocalJedis = new ThreadLocal();

    public RedisLock() {
        this.jedisPool = new JedisPool();
    }

    public RedisLock(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public RedisLock(JedisPool jedisPool,long lockTimeOut, long perSleep) {
        this.lockTimeOut = lockTimeOut;
        this.perSleep = perSleep;
        this.jedisPool = jedisPool;
    }


    private Jedis getJedis() {
        Jedis jedis = (Jedis)this.threadLocalJedis.get();
        return jedis != null?jedis:this.jedisPool.getResource();
    }

    private void close(Jedis jedis) {
        if(this.threadLocalJedis.get() == null && jedis != null) {
            jedis.close();
        }

    }

    private boolean setLock(Jedis jedis,String key){
        String result = null;

            result = jedis.set(key, "", "nx", "px", lockTimeOut);

        return result != null;
    }

    //可中断不可重入锁
    public boolean tryLock(String key) {
        Jedis jedis = this.getJedis();
        try {
            return setLock(jedis,key);
        }finally {
            this.close(jedis);
        }
    }

    //不可中断不可重入锁
    public boolean lock(String key) throws InterruptedException {
        Jedis jedis = this.getJedis();
        try {
            while (true){
                if(setLock(jedis,key)){
                    return true;
                }else{
                    Thread.sleep(perSleep);
                }
            }
        }finally {
            this.close(jedis);
        }

    }


    //过期可中断不可重入锁
    public boolean lock(String key,long expireMillis) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        Jedis jedis = this.getJedis();
        try {
            while (true){
                long now = System.currentTimeMillis();
                if((now - startTime) >= expireMillis ){
                    return false;
                }
                if(setLock(jedis,key)){
                    return true;
                }else{
                    now = System.currentTimeMillis();
                    if((now+perSleep - startTime) >= expireMillis ){
                        return false;
                    }
                    Thread.sleep(perSleep);
                }
            }
        }finally {
            this.close(jedis);
        }

    }

    //解锁
    public boolean unLock(String key){
        Jedis jedis = this.getJedis();
        Thread.interrupted();
        long success = 0;
        try {
            success = jedis.del(key);
        }finally {
            this.close(jedis);
        }
        return success > 0;

    }

}