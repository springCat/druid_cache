package org.springcat.druidcache.core;

import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.*;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.druid.util.JdbcUtils;
import com.sun.rowset.CachedRowSetImpl;
import org.springcat.druidcache.parse.SqlParse;
import org.springcat.druidcache.parse.operator.ModifyOperator;
import org.springcat.druidcache.parse.operator.Select;
import org.springcat.druidcache.redis.RedisCache;
import redis.clients.jedis.JedisPool;

import javax.sql.rowset.CachedRowSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 *  (1)不支持excute执行的sql语句的结果缓存
 *  (2)为了性能考虑,未使用RedisLock,但是redis是单线程,只有很小的可能才会出现数据不一致的情况
 *  (3)不支持存储过程缓存
 *
 * Created by springcat on 16/6/3.
 */
public class DruidCacheFilter extends FilterEventAdapter {

    private final static Log LOG = LogFactory.getLog(DruidCacheFilter.class);

    private String dbType;

    private SqlParse sqlParser;

    private DataLoader dataLoader;

    private int redisPort = 6379;

    private String redisHost = "127.0.0.1";

    private DataSourceProxy dataSource;

    public DruidCacheFilter(){
        super();
    }

    public DruidCacheFilter(String redisHost,int redisPort) {
        super();
        this.redisPort = redisPort;
        this.redisHost = redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    @Override
    public void init(DataSourceProxy dataSource) {
        if (null == dataSource) {
            LOG.error("dataSource should not be null");
            return;
        }
        this.dataSource = dataSource;

        if (this.dbType == null || this.dbType.trim().length() == 0) {
            if (dataSource.getDbType() != null) {
                this.dbType = dataSource.getDbType();
            } else {
                this.dbType = JdbcUtils.getDbType(dataSource.getRawJdbcUrl(), "");
            }
        }

        if (dbType == null) {
            dbType = JdbcUtils.getDbType(dataSource.getUrl(), null);
        }

        if(sqlParser == null) {
            sqlParser = new SqlParse(dbType);
        }

        if(dataLoader == null){
            dataLoader = new DataLoader();
        }
    }
    //excuteQuery  statement
    @Override
    public ResultSetProxy statement_executeQuery(FilterChain chain, StatementProxy statement, String sql)
            throws SQLException {
        statementExecuteQueryBefore(statement, sql);

        try {
            ResultSetProxy resultSetProxy = null;

            JedisPool jedisPool = new JedisPool(redisHost,redisPort);
            RedisCache redisCache = new RedisCache(jedisPool);

            String parameters = sqlParser.getParameters(statement);

            CachedRowSet resultSet = (CachedRowSet) dataLoader.get(redisCache,sql,parameters);

            if (resultSet == null) {
                resultSetProxy = super.statement_executeQuery(chain, statement, sql);
                ResultSet resultSetRaw = resultSetProxy.getResultSetRaw();
                CachedRowSet cachedRowSet = new CachedRowSetImpl();
                cachedRowSet.populate(resultSetRaw);

                Select select = (Select) sqlParser.parse(sql);
                dataLoader.load(redisCache,select,parameters,cachedRowSet);
                LOG.debug("load cahce data");
            }else {
                resultSetProxy  = new ResultSetProxyImpl(statement, resultSet, dataSource.createResultSetId(), statement.getLastExecuteSql());
                LOG.debug("hit cahce");
            }

            if (resultSetProxy != null) {
                statementExecuteQueryAfter(statement, sql, resultSetProxy);
                resultSetOpenAfter(resultSetProxy);
            }

            return resultSetProxy;
        } catch (SQLException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, sql, error);
            throw error;
        }
    }

    //excuteQuery preparedStatement
    @Override
    public ResultSetProxy preparedStatement_executeQuery(FilterChain chain, PreparedStatementProxy statement)
            throws SQLException {
        try {
            statementExecuteQueryBefore(statement, statement.getSql());


            ResultSetProxy resultSetProxy = null;

            JedisPool jedisPool = new JedisPool(redisHost,redisPort);
            RedisCache redisCache = new RedisCache(jedisPool);

            String sql = statement.getSql();

            String parameters = sqlParser.getParameters(statement);

            CachedRowSet resultSet = (CachedRowSet) dataLoader.get(redisCache,sql,parameters);

            if (resultSet == null) {
                resultSetProxy = chain.preparedStatement_executeQuery(statement);
                ResultSet resultSetRaw = resultSetProxy.getResultSetRaw();
                CachedRowSet cachedRowSet = new CachedRowSetImpl();
                cachedRowSet.populate(resultSetRaw);
                Select select = (Select) sqlParser.parse(sql);
                dataLoader.load(redisCache,select,parameters,cachedRowSet);
                resultSetProxy  = new ResultSetProxyImpl(statement, cachedRowSet, dataSource.createResultSetId(), statement.getLastExecuteSql());
                LOG.debug("load cahce data");
            }else {
                resultSetProxy  = new ResultSetProxyImpl(statement, resultSet, dataSource.createResultSetId(), statement.getLastExecuteSql());
                LOG.debug("hit cahce");
            }

            if (resultSetProxy != null) {
                statementExecuteQueryAfter(statement, statement.getSql(), resultSetProxy);

                resultSetOpenAfter(resultSetProxy);
            }
            return resultSetProxy;

        } catch (SQLException error) {
            statement_executeErrorAfter(statement, statement.getSql(), error);
            throw error;
        } catch (RuntimeException error) {
            statement_executeErrorAfter(statement, statement.getSql(), error);
            throw error;
        } catch (Error error) {
            statement_executeErrorAfter(statement, statement.getSql(), error);
            throw error;
        }
    }




    //after
    protected void statementExecuteAfter(StatementProxy statement, String sql, boolean result) {
        if(result){
            ModifyOperator modifyOperator = inValied(sql);
            LOG.debug("clear cache on " + modifyOperator.getTableName());
        }else{
            LOG.debug("can not cache select by use execute()");
        }
    }

    @Override
    protected void statementExecuteUpdateAfter(StatementProxy statement, String sql, int updateCount) {
        if(updateCount > 0) {
            ModifyOperator modifyOperator = inValied(sql);
            LOG.debug("clear cache on " + modifyOperator.getTableName());
        }
    }

    @Override
    protected void statementExecuteBatchAfter(StatementProxy statement, int[] result) {
        JedisPool jedisPool = new JedisPool(redisHost,redisPort);
        RedisCache redisCache = new RedisCache(jedisPool);
        Set<String> tableNames = new HashSet<String>();
        List<String> batchSqlList = statement.getBatchSqlList();

        for (int i = 0; i < batchSqlList.size(); i++) {
            ModifyOperator modifyOperator = (ModifyOperator) sqlParser.parse(batchSqlList.get(i));
            if(result[i] > 0) {
                tableNames.add(modifyOperator.getTableName());
            }
        }

        for (String tableName : tableNames) {
            dataLoader.remove(redisCache,tableName);
            LOG.debug("clear cache on "+tableName);
        }
    }

    private ModifyOperator inValied(String sql){
        JedisPool jedisPool = new JedisPool(redisHost,redisPort);
        RedisCache redisCache = new RedisCache(jedisPool);
        ModifyOperator modifyOperator = (ModifyOperator) sqlParser.parse(sql);
        dataLoader.remove(redisCache,modifyOperator.getTableName());
        return  modifyOperator;
    }
}
