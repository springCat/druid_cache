package org.springcat.druidcache.parse;

import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import org.springcat.druidcache.parse.operator.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by springcat on 16/6/4.
 */
public class SqlParse {

    private static String dbType;

    public SqlParse(String dbType) {
        this.dbType = dbType;
    }

    public String getParameters(StatementProxy statement){
        int length = statement.getParametersSize();
        if(length == 0){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(statement.getParameter(0).getValue());
        for (int i = 1; i < length; i++) {
            sb.append("_").append(statement.getParameter(i).getValue());
        }
        return sb.toString();
    }

    public Operator parse(String sql){
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        List<SQLStatement> stmtList = parser.parseStatementList();


        for (SQLStatement stmt : stmtList) {
            if (stmt instanceof SQLSelectStatement) {

                Set<String> tableNames = new HashSet<String>();
                SQLSelectStatement select = (SQLSelectStatement) stmt;
                SQLSelectQuery query = select.getSelect().getQuery();
                parseSQLSelectQuery(query, tableNames);

                Select selectOperator = new Select();
                selectOperator.setTableNames(tableNames);
                selectOperator.setSql(sql);
                return selectOperator;

            } else if (stmt instanceof SQLInsertInto) {

                SQLInsertInto insert = (SQLInsertInto) stmt;
                Insert insertOperator = new Insert();
                insertOperator.setTableName(insert.getTableName().toString());
                insertOperator.setSql(sql);
                return insertOperator;

            } else if (stmt instanceof SQLUpdateStatement) {

                SQLUpdateStatement update = (SQLUpdateStatement) stmt;
                Update updateOperator = new Update();
                updateOperator.setTableName(update.getTableName().toString());
                updateOperator.setSql(sql);
                return updateOperator;

            } else if (stmt instanceof SQLDeleteStatement) {

                SQLDeleteStatement delete = (SQLDeleteStatement) stmt;
                Delete deleteOperator = new Delete();
                deleteOperator.setTableName(delete.getTableName().toString());
                deleteOperator.setSql(sql);
                return deleteOperator;

            } else {
                System.out.println("error");
            }
        }
        return null;
    }

    private  void parseSQLSelectQuery(SQLSelectQuery query, Set<String> tableNames){

        if(query instanceof SQLSelectQueryBlock){
            SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) query;
            parseSQLSelectQuery(sqlSelectQueryBlock.getFrom(),tableNames);
        }else if(query instanceof SQLUnionQuery){
            SQLUnionQuery sqlUnionQuery = (SQLUnionQuery)query;
            parseSQLSelectQuery(sqlUnionQuery.getLeft(),tableNames);
            parseSQLSelectQuery(sqlUnionQuery.getRight(),tableNames);
        }else{
            System.out.println("parseSQLSelectQuery not implements");
        }


    }


    private void parseSQLSelectQuery(SQLTableSource sqlTableSource, Set<String> tableNames){
        //join
        if(sqlTableSource instanceof SQLJoinTableSource){
            SQLJoinTableSource sqlJoinTableSource = (SQLJoinTableSource) sqlTableSource;
            parseSQLSelectQuery(sqlJoinTableSource.getLeft(),tableNames);
            parseSQLSelectQuery(sqlJoinTableSource.getRight(),tableNames);
        }
        //subquery
        else if(sqlTableSource instanceof SQLSubqueryTableSource){
            SQLSubqueryTableSource sqlSubqueryTableSource = (SQLSubqueryTableSource)sqlTableSource;
            SQLSelect sqlselect = sqlSubqueryTableSource.getSelect();
            SQLSelectQueryBlock query = (SQLSelectQueryBlock) sqlselect.getQuery();
            SQLTableSource from = query.getFrom();
            parseSQLSelectQuery(from,tableNames);
        }
        //one table
        else if(sqlTableSource instanceof SQLExprTableSource){
            SQLExprTableSource sqlExprTableSource = (SQLExprTableSource) sqlTableSource;
            tableNames.add(sqlExprTableSource.toString());
        }else{
            System.out.println("parseSQLSelectQuery not implements");
        }
    }

}
