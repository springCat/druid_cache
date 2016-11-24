package org.springcat.druidcache.parse.operator;

import java.util.Set;

/**
 * Created by springcat on 16/6/4.
 */
public class Select implements UnModifyOperator {

    private Set<String> tableNames;
    private String sql;

    public Set<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(Set<String> tableNames) {
        this.tableNames = tableNames;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
