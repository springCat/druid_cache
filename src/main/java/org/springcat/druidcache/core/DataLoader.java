package org.springcat.druidcache.core;


import org.springcat.druidcache.parse.operator.Select;
import org.springcat.druidcache.redis.RedisCache;

import java.util.Set;

/**
 * Created by springcat on 16/6/5.
 */
public class DataLoader {


    public  Object get(RedisCache cache, String sql, String paramters){
        return cache.hget(sql,paramters);
    }

    public boolean load(RedisCache cache,Select select,String paramters,Object data){
        Set<String> tableNames = select.getTableNames();
        String sql = select.getSql();
        for (String tableName : tableNames) {
            cache.sadd(trimTableName(tableName)+"_sql",sql);
        }
        cache.hset(sql,paramters,data);
        return false;
    }

    public boolean remove(RedisCache cache, String tableName){
        Set<String> sqls = cache.smembers(trimTableName(tableName)+"_sql");
        for (String sql : sqls) {
            cache.del(sql);
        }
        return false;
    }

    private String trimTableName(String tableName){
        int length = tableName.length();
        if(tableName.startsWith("`") && tableName.endsWith("`")){
            tableName = tableName.substring(1,length-1);
        }
        return tableName;
    }

}
