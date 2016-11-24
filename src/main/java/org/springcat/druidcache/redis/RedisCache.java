package org.springcat.druidcache.redis;


import org.springcat.druidcache.redis.serializer.FstSerializer;
import org.springcat.druidcache.redis.serializer.ISerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

/**
 * Created by springcat on 16/6/5.
 */
public class RedisCache {
    protected String name;
    protected JedisPool jedisPool;
    protected ISerializer serializer;
    protected IKeyNamingPolicy keyNamingPolicy;
    protected final ThreadLocal<Jedis> threadLocalJedis = new ThreadLocal();

    public RedisCache() {
        this.jedisPool = new JedisPool();
        this.keyNamingPolicy = IKeyNamingPolicy.defaultKeyNamingPolicy;
        this.serializer = FstSerializer.me;
    }


    public RedisCache(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.keyNamingPolicy = IKeyNamingPolicy.defaultKeyNamingPolicy;
        this.serializer = FstSerializer.me;
    }

    public RedisCache(String name, JedisPool jedisPool, ISerializer serializer, IKeyNamingPolicy keyNamingPolicy) {
        this.name = name;
        this.jedisPool = jedisPool;
        this.serializer = serializer;
        this.keyNamingPolicy = keyNamingPolicy;
    }

    public String set(Object key, Object value) {
        Jedis jedis = this.getJedis();

        String var4;
        try {
            var4 = jedis.set(this.keyToBytes(key), this.valueToBytes(value));
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public String setex(Object key, int seconds, Object value) {
        Jedis jedis = this.getJedis();

        String var5;
        try {
            var5 = jedis.setex(this.keyToBytes(key), seconds, this.valueToBytes(value));
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public <T> T get(Object key) {
        Jedis jedis = this.getJedis();

        Object var3;
        try {
            var3 = this.valueFromBytes(jedis.get(this.keyToBytes(key)));
        } finally {
            this.close(jedis);
        }

        return (T)var3;
    }

    public Long del(Object key) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = jedis.del(this.keyToBytes(key));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public Long del(Object... keys) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = jedis.del(this.keysToBytesArray(keys));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public Set<String> keys(String pattern) {
        Jedis jedis = this.getJedis();

        Set var3;
        try {
            var3 = jedis.keys(pattern);
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public String mset(Object... keysValues) {
        if (keysValues.length % 2 != 0) {
            throw new IllegalArgumentException("wrong number of arguments for met, keysValues length can not be odd");
        } else {
            Jedis jedis = this.getJedis();

            try {
                byte[][] kv = new byte[keysValues.length][];

                for (int i = 0; i < keysValues.length; ++i) {
                    if (i % 2 == 0) {
                        kv[i] = this.keyToBytes(keysValues[i]);
                    } else {
                        kv[i] = this.valueToBytes(keysValues[i]);
                    }
                }

                String var8 = jedis.mset(kv);
                return var8;
            } finally {
                this.close(jedis);
            }
        }
    }

    public List mget(Object... keys) {
        Jedis jedis = this.getJedis();

        List var5;
        try {
            byte[][] keysBytesArray = this.keysToBytesArray(keys);
            List data = jedis.mget(keysBytesArray);
            var5 = this.valueListFromBytesList(data);
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public Long decr(Object key) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = jedis.decr(this.keyToBytes(key));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public Long decrBy(Object key, long longValue) {
        Jedis jedis = this.getJedis();

        Long var5;
        try {
            var5 = jedis.decrBy(this.keyToBytes(key), longValue);
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public Long incr(Object key) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = jedis.incr(this.keyToBytes(key));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public Long incrBy(Object key, long longValue) {
        Jedis jedis = this.getJedis();

        Long var5;
        try {
            var5 = jedis.incrBy(this.keyToBytes(key), longValue);
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public boolean exists(Object key) {
        Jedis jedis = this.getJedis();

        boolean var3;
        try {
            var3 = jedis.exists(this.keyToBytes(key)).booleanValue();
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public String randomKey() {
        Jedis jedis = this.getJedis();

        String var2;
        try {
            var2 = jedis.randomKey();
        } finally {
            this.close(jedis);
        }

        return var2;
    }

    public String rename(Object oldkey, Object newkey) {
        Jedis jedis = this.getJedis();

        String var4;
        try {
            var4 = jedis.rename(this.keyToBytes(oldkey), this.keyToBytes(newkey));
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public Long move(Object key, int dbIndex) {
        Jedis jedis = this.getJedis();

        Long var4;
        try {
            var4 = jedis.move(this.keyToBytes(key), dbIndex);
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public String migrate(String host, int port, Object key, int destinationDb, int timeout) {
        Jedis jedis = this.getJedis();

        String var7;
        try {
            var7 = jedis.migrate(this.valueToBytes(host), port, this.keyToBytes(key), destinationDb, timeout);
        } finally {
            this.close(jedis);
        }

        return var7;
    }

    public String select(int databaseIndex) {
        Jedis jedis = this.getJedis();

        String var3;
        try {
            var3 = jedis.select(databaseIndex);
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public Long expire(Object key, int seconds) {
        Jedis jedis = this.getJedis();

        Long var4;
        try {
            var4 = jedis.expire(this.keyToBytes(key), seconds);
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public Long expireAt(Object key, long unixTime) {
        Jedis jedis = this.getJedis();

        Long var5;
        try {
            var5 = jedis.expireAt(this.keyToBytes(key), unixTime);
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public Long pexpire(Object key, long milliseconds) {
        Jedis jedis = this.getJedis();

        Long var5;
        try {
            var5 = jedis.pexpire(this.keyToBytes(key), milliseconds);
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public Long pexpireAt(Object key, long millisecondsTimestamp) {
        Jedis jedis = this.getJedis();

        Long var5;
        try {
            var5 = jedis.pexpireAt(this.keyToBytes(key), millisecondsTimestamp);
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public <T> T getSet(Object key, Object value) {
        Jedis jedis = this.getJedis();

        Object var4;
        try {
            var4 = this.valueFromBytes(jedis.getSet(this.keyToBytes(key), this.valueToBytes(value)));
        } finally {
            this.close(jedis);
        }

        return (T)var4;
    }

    public Long persist(Object key) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = jedis.persist(this.keyToBytes(key));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public String type(Object key) {
        Jedis jedis = this.getJedis();

        String var3;
        try {
            var3 = jedis.type(this.keyToBytes(key));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public Long ttl(Object key) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = jedis.ttl(this.keyToBytes(key));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public Long pttl(Object key) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = jedis.pttl(this.keyToBytes(key));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public Long objectRefcount(Object key) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = jedis.objectRefcount(this.keyToBytes(key));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public Long objectIdletime(Object key) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = jedis.objectIdletime(this.keyToBytes(key));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public Long hset(Object key, Object field, Object value) {
        Jedis jedis = this.getJedis();

        Long var5;
        try {
            var5 = jedis.hset(this.keyToBytes(key), this.fieldToBytes(field), this.valueToBytes(value));
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public String hmset(Object key, Map<Object, Object> hash) {
        Jedis jedis = this.getJedis();

        try {
            HashMap para = new HashMap();
            Iterator i$ = hash.entrySet().iterator();

            while (i$.hasNext()) {
                Map.Entry e = (Map.Entry) i$.next();
                para.put(this.fieldToBytes(e.getKey()), this.valueToBytes(e.getValue()));
            }

            String i$1 = jedis.hmset(this.keyToBytes(key), para);
            return i$1;
        } finally {
            this.close(jedis);
        }
    }

    public <T> T hget(Object key, Object field) {
        Jedis jedis = this.getJedis();

        Object var4;
        try {
            var4 = this.valueFromBytes(jedis.hget(this.keyToBytes(key), this.fieldToBytes(field)));
        } finally {
            this.close(jedis);
        }

        return (T)var4;
    }

    public List hmget(Object key, Object... fields) {
        Jedis jedis = this.getJedis();

        List var5;
        try {
            List data = jedis.hmget(this.keyToBytes(key), this.fieldsToBytesArray(fields));
            var5 = this.valueListFromBytesList(data);
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public Long hdel(Object key, Object... fields) {
        Jedis jedis = this.getJedis();

        Long var4;
        try {
            var4 = jedis.hdel(this.keyToBytes(key), this.fieldsToBytesArray(fields));
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public boolean hexists(Object key, Object field) {
        Jedis jedis = this.getJedis();

        boolean var4;
        try {
            var4 = jedis.hexists(this.keyToBytes(key), this.fieldToBytes(field)).booleanValue();
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public Map hgetAll(Object key) {
        Jedis jedis = this.getJedis();

        try {
            Map data = jedis.hgetAll(this.keyToBytes(key));
            HashMap result = new HashMap();
            Iterator i$ = data.entrySet().iterator();

            while (i$.hasNext()) {
                Map.Entry e = (Map.Entry) i$.next();
                result.put(this.fieldFromBytes((byte[]) e.getKey()), this.valueFromBytes((byte[]) e.getValue()));
            }

            HashMap i$1 = result;
            return i$1;
        } finally {
            this.close(jedis);
        }
    }

    public List hvals(Object key) {
        Jedis jedis = this.getJedis();

        List var4;
        try {
            List data = jedis.hvals(this.keyToBytes(key));
            var4 = this.valueListFromBytesList(data);
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public Set<Object> hkeys(Object key) {
        Jedis jedis = this.getJedis();

        HashSet var5;
        try {
            Set fieldSet = jedis.hkeys(this.keyToBytes(key));
            HashSet result = new HashSet();
            this.fieldSetFromBytesSet(fieldSet, result);
            var5 = result;
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public Long hlen(Object key) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = jedis.hlen(this.keyToBytes(key));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public Long hincrBy(Object key, Object field, long value) {
        Jedis jedis = this.getJedis();

        Long var6;
        try {
            var6 = jedis.hincrBy(this.keyToBytes(key), this.fieldToBytes(field), value);
        } finally {
            this.close(jedis);
        }

        return var6;
    }

    public Double hincrByFloat(Object key, Object field, double value) {
        Jedis jedis = this.getJedis();

        Double var6;
        try {
            var6 = jedis.hincrByFloat(this.keyToBytes(key), this.fieldToBytes(field), value);
        } finally {
            this.close(jedis);
        }

        return var6;
    }

    public <T> T lindex(Object key, long index) {
        Jedis jedis = this.getJedis();

        Object var5;
        try {
            var5 = this.valueFromBytes(jedis.lindex(this.keyToBytes(key), index));
        } finally {
            this.close(jedis);
        }

        return (T)var5;
    }

    public Long getCounter(Object key) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = Long.valueOf(Long.parseLong(jedis.get(this.keyNamingPolicy.getKeyName(key))));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public Long llen(Object key) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = jedis.llen(this.keyToBytes(key));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public <T> T lpop(Object key) {
        Jedis jedis = this.getJedis();

        Object var3;
        try {
            var3 = this.valueFromBytes(jedis.lpop(this.keyToBytes(key)));
        } finally {
            this.close(jedis);
        }

        return (T)var3;
    }

    public Long lpush(Object key, Object... values) {
        Jedis jedis = this.getJedis();

        Long var4;
        try {
            var4 = jedis.lpush(this.keyToBytes(key), this.valuesToBytesArray(values));
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public String lset(Object key, long index, Object value) {
        Jedis jedis = this.getJedis();

        String var6;
        try {
            var6 = jedis.lset(this.keyToBytes(key), index, this.valueToBytes(value));
        } finally {
            this.close(jedis);
        }

        return var6;
    }

    public Long lrem(Object key, long count, Object value) {
        Jedis jedis = this.getJedis();

        Long var6;
        try {
            var6 = jedis.lrem(this.keyToBytes(key), count, this.valueToBytes(value));
        } finally {
            this.close(jedis);
        }

        return var6;
    }

    public List lrange(Object key, long start, long end) {
        Jedis jedis = this.getJedis();

        List var8;
        try {
            List data = jedis.lrange(this.keyToBytes(key), start, end);
            var8 = this.valueListFromBytesList(data);
        } finally {
            this.close(jedis);
        }

        return var8;
    }

    public String ltrim(Object key, long start, long end) {
        Jedis jedis = this.getJedis();

        String var7;
        try {
            var7 = jedis.ltrim(this.keyToBytes(key), start, end);
        } finally {
            this.close(jedis);
        }

        return var7;
    }

    public <T> T rpop(Object key) {
        Jedis jedis = this.getJedis();

        Object var3;
        try {
            var3 = this.valueFromBytes(jedis.rpop(this.keyToBytes(key)));
        } finally {
            this.close(jedis);
        }

        return (T)var3;
    }

    public <T> T rpoplpush(Object srcKey, Object dstKey) {
        Jedis jedis = this.getJedis();

        Object var4;
        try {
            var4 = this.valueFromBytes(jedis.rpoplpush(this.keyToBytes(srcKey), this.keyToBytes(dstKey)));
        } finally {
            this.close(jedis);
        }

        return (T)var4;
    }

    public Long rpush(Object key, Object... values) {
        Jedis jedis = this.getJedis();

        Long var4;
        try {
            var4 = jedis.rpush(this.keyToBytes(key), this.valuesToBytesArray(values));
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public List blpop(Object... keys) {
        Jedis jedis = this.getJedis();

        List var4;
        try {
            List data = jedis.blpop(this.keysToBytesArray(keys));
            var4 = this.valueListFromBytesList(data);
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public List blpop(int timeout, Object... keys) {
        Jedis jedis = this.getJedis();

        List var5;
        try {
            List data = jedis.blpop(timeout, this.keysToBytesArray(keys));
            var5 = this.valueListFromBytesList(data);
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public List brpop(Object... keys) {
        Jedis jedis = this.getJedis();

        List var4;
        try {
            List data = jedis.brpop(this.keysToBytesArray(keys));
            var4 = this.valueListFromBytesList(data);
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public List brpop(int timeout, Object... keys) {
        Jedis jedis = this.getJedis();

        List var5;
        try {
            List data = jedis.brpop(timeout, this.keysToBytesArray(keys));
            var5 = this.valueListFromBytesList(data);
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public String ping() {
        Jedis jedis = this.getJedis();

        String var2;
        try {
            var2 = jedis.ping();
        } finally {
            this.close(jedis);
        }

        return var2;
    }

    public Long sadd(Object key, Object... members) {
        Jedis jedis = this.getJedis();

        Long var4;
        try {
            var4 = jedis.sadd(this.keyToBytes(key), this.valuesToBytesArray(members));
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public Long scard(Object key) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = jedis.scard(this.keyToBytes(key));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public <T> T spop(Object key) {
        Jedis jedis = this.getJedis();

        Object var3;
        try {
            var3 = this.valueFromBytes(jedis.spop(this.keyToBytes(key)));
        } finally {
            this.close(jedis);
        }

        return (T)var3;
    }

    public Set smembers(Object key) {
        Jedis jedis = this.getJedis();

        HashSet var5;
        try {
            Set data = jedis.smembers(this.keyToBytes(key));
            HashSet result = new HashSet();
            this.valueSetFromBytesSet(data, result);
            var5 = result;
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public boolean sismember(Object key, Object member) {
        Jedis jedis = this.getJedis();

        boolean var4;
        try {
            var4 = jedis.sismember(this.keyToBytes(key), this.valueToBytes(member)).booleanValue();
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public Set sinter(Object... keys) {
        Jedis jedis = this.getJedis();

        HashSet var5;
        try {
            Set data = jedis.sinter(this.keysToBytesArray(keys));
            HashSet result = new HashSet();
            this.valueSetFromBytesSet(data, result);
            var5 = result;
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public <T> T srandmember(Object key) {
        Jedis jedis = this.getJedis();

        Object var3;
        try {
            var3 = this.valueFromBytes(jedis.srandmember(this.keyToBytes(key)));
        } finally {
            this.close(jedis);
        }

        return (T)var3;
    }

    public List srandmember(Object key, int count) {
        Jedis jedis = this.getJedis();

        List var5;
        try {
            List data = jedis.srandmember(this.keyToBytes(key), count);
            var5 = this.valueListFromBytesList(data);
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public Long srem(Object key, Object... members) {
        Jedis jedis = this.getJedis();

        Long var4;
        try {
            var4 = jedis.srem(this.keyToBytes(key), this.valuesToBytesArray(members));
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public Set sunion(Object... keys) {
        Jedis jedis = this.getJedis();

        HashSet var5;
        try {
            Set data = jedis.sunion(this.keysToBytesArray(keys));
            HashSet result = new HashSet();
            this.valueSetFromBytesSet(data, result);
            var5 = result;
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public Set sdiff(Object... keys) {
        Jedis jedis = this.getJedis();

        HashSet var5;
        try {
            Set data = jedis.sdiff(this.keysToBytesArray(keys));
            HashSet result = new HashSet();
            this.valueSetFromBytesSet(data, result);
            var5 = result;
        } finally {
            this.close(jedis);
        }

        return var5;
    }

    public Long zadd(Object key, double score, Object member) {
        Jedis jedis = this.getJedis();

        Long var6;
        try {
            var6 = jedis.zadd(this.keyToBytes(key), score, this.valueToBytes(member));
        } finally {
            this.close(jedis);
        }

        return var6;
    }

    public Long zadd(Object key, Map<Object, Double> scoreMembers) {
        Jedis jedis = this.getJedis();

        try {
            HashMap para = new HashMap();
            Iterator i$ = scoreMembers.entrySet().iterator();

            while (i$.hasNext()) {
                Map.Entry e = (Map.Entry) i$.next();
                para.put(this.valueToBytes(e.getKey()), e.getValue());
            }

            Long i$1 = jedis.zadd(this.keyToBytes(key), para);
            return i$1;
        } finally {
            this.close(jedis);
        }
    }

    public Long zcard(Object key) {
        Jedis jedis = this.getJedis();

        Long var3;
        try {
            var3 = jedis.zcard(this.keyToBytes(key));
        } finally {
            this.close(jedis);
        }

        return var3;
    }

    public Long zcount(Object key, double min, double max) {
        Jedis jedis = this.getJedis();

        Long var7;
        try {
            var7 = jedis.zcount(this.keyToBytes(key), min, max);
        } finally {
            this.close(jedis);
        }

        return var7;
    }

    public Double zincrby(Object key, double score, Object member) {
        Jedis jedis = this.getJedis();

        Double var6;
        try {
            var6 = jedis.zincrby(this.keyToBytes(key), score, this.valueToBytes(member));
        } finally {
            this.close(jedis);
        }

        return var6;
    }

    public Set zrange(Object key, long start, long end) {
        Jedis jedis = this.getJedis();

        LinkedHashSet var9;
        try {
            Set data = jedis.zrange(this.keyToBytes(key), start, end);
            LinkedHashSet result = new LinkedHashSet();
            this.valueSetFromBytesSet(data, result);
            var9 = result;
        } finally {
            this.close(jedis);
        }

        return var9;
    }

    public Set zrevrange(Object key, long start, long end) {
        Jedis jedis = this.getJedis();

        LinkedHashSet var9;
        try {
            Set data = jedis.zrevrange(this.keyToBytes(key), start, end);
            LinkedHashSet result = new LinkedHashSet();
            this.valueSetFromBytesSet(data, result);
            var9 = result;
        } finally {
            this.close(jedis);
        }

        return var9;
    }

    public Set zrangeByScore(Object key, double min, double max) {
        Jedis jedis = this.getJedis();

        LinkedHashSet var9;
        try {
            Set data = jedis.zrangeByScore(this.keyToBytes(key), min, max);
            LinkedHashSet result = new LinkedHashSet();
            this.valueSetFromBytesSet(data, result);
            var9 = result;
        } finally {
            this.close(jedis);
        }

        return var9;
    }

    public Long zrank(Object key, Object member) {
        Jedis jedis = this.getJedis();

        Long var4;
        try {
            var4 = jedis.zrank(this.keyToBytes(key), this.valueToBytes(member));
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public Long zrevrank(Object key, Object member) {
        Jedis jedis = this.getJedis();

        Long var4;
        try {
            var4 = jedis.zrevrank(this.keyToBytes(key), this.valueToBytes(member));
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public Long zrem(Object key, Object... members) {
        Jedis jedis = this.getJedis();

        Long var4;
        try {
            var4 = jedis.zrem(this.keyToBytes(key), this.valuesToBytesArray(members));
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    public Double zscore(Object key, Object member) {
        Jedis jedis = this.getJedis();

        Double var4;
        try {
            var4 = jedis.zscore(this.keyToBytes(key), this.valueToBytes(member));
        } finally {
            this.close(jedis);
        }

        return var4;
    }

    protected byte[] keyToBytes(Object key) {
        String keyStr = this.keyNamingPolicy.getKeyName(key);
        return this.serializer.keyToBytes(keyStr);
    }

    protected byte[][] keysToBytesArray(Object... keys) {
        byte[][] result = new byte[keys.length][];

        for (int i = 0; i < result.length; ++i) {
            result[i] = this.keyToBytes(keys[i]);
        }

        return result;
    }

    protected byte[] fieldToBytes(Object field) {
        return this.serializer.fieldToBytes(field);
    }

    protected Object fieldFromBytes(byte[] bytes) {
        return this.serializer.fieldFromBytes(bytes);
    }

    protected byte[][] fieldsToBytesArray(Object... fieldsArray) {
        byte[][] data = new byte[fieldsArray.length][];

        for (int i = 0; i < data.length; ++i) {
            data[i] = this.fieldToBytes(fieldsArray[i]);
        }

        return data;
    }

    protected void fieldSetFromBytesSet(Set<byte[]> data, Set<Object> result) {
        Iterator i$ = data.iterator();

        while (i$.hasNext()) {
            byte[] fieldBytes = (byte[]) i$.next();
            result.add(this.fieldFromBytes(fieldBytes));
        }

    }

    protected byte[] valueToBytes(Object value) {
        return this.serializer.valueToBytes(value);
    }

    protected Object valueFromBytes(byte[] bytes) {
        return this.serializer.valueFromBytes(bytes);
    }

    protected byte[][] valuesToBytesArray(Object... valuesArray) {
        byte[][] data = new byte[valuesArray.length][];

        for (int i = 0; i < data.length; ++i) {
            data[i] = this.valueToBytes(valuesArray[i]);
        }

        return data;
    }

    protected void valueSetFromBytesSet(Set<byte[]> data, Set<Object> result) {
        Iterator i$ = data.iterator();

        while (i$.hasNext()) {
            byte[] valueBytes = (byte[]) i$.next();
            result.add(this.valueFromBytes(valueBytes));
        }

    }

    protected List valueListFromBytesList(List<byte[]> data) {
        ArrayList result = new ArrayList();
        Iterator i$ = data.iterator();

        while (i$.hasNext()) {
            byte[] d = (byte[]) i$.next();
            result.add(this.valueFromBytes(d));
        }

        return result;
    }

    public String getName() {
        return this.name;
    }

    public ISerializer getSerializer() {
        return this.serializer;
    }

    public IKeyNamingPolicy getKeyNamingPolicy() {
        return this.keyNamingPolicy;
    }

    public Jedis getJedis() {
        Jedis jedis = (Jedis) this.threadLocalJedis.get();
        return jedis != null ? jedis : this.jedisPool.getResource();
    }

    public void close(Jedis jedis) {
        if (this.threadLocalJedis.get() == null && jedis != null) {
            jedis.close();
        }

    }

    public Jedis getThreadLocalJedis() {
        return (Jedis) this.threadLocalJedis.get();
    }

    public void setThreadLocalJedis(Jedis jedis) {
        this.threadLocalJedis.set(jedis);
    }

    public void removeThreadLocalJedis() {
        this.threadLocalJedis.remove();
    }

}
