package org.springcat.druidcache.parse.operator;

/**
 * Created by springcat on 16/6/6.
 */
public interface ModifyOperator extends Operator{

    public String getTableName();

    public void setTableName(String tableName);
}
