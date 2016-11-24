package org.springcat.druidcache.parse.operator;

import java.util.Set;

/**
 * Created by springcat on 16/6/6.
 */
public interface UnModifyOperator extends Operator {

    public Set<String> getTableNames();

    public void setTableNames(Set<String> tableNames);
}
