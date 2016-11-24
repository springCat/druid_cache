package org.springcat.druidcache.parse.operator;

/**
 * Created by springcat on 16/6/4.
 */
public class Update implements ModifyOperator {

    private String tableName;

    private String sql;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
