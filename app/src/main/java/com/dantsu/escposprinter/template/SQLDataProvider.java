package com.dantsu.escposprinter.template;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * SQL Server 数据提供者
 * 从 SQL Server 数据库获取动态数据
 */
public class SQLDataProvider implements DataProvider {
    
    private static final String TAG = "SQLDataProvider";
    
    private String server;
    private String database;
    private String username;
    private String password;
    private int port;
    
    private Connection connection;
    private Map<String, String> cache = new HashMap<>();
    
    public SQLDataProvider(String server, String database, String username, String password) {
        this(server, 1433, database, username, password);
    }
    
    public SQLDataProvider(String server, int port, String database, String username, String password) {
        this.server = server;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }
    
    /**
     * 连接数据库
     */
    public boolean connect() {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String url = String.format("jdbc:jtds:sqlserver://%s:%d/%s", server, port, database);
            connection = DriverManager.getConnection(url, username, password);
            Log.d(TAG, "SQL Server connected successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to connect SQL Server", e);
            return false;
        }
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                Log.e(TAG, "Error closing connection", e);
            }
            connection = null;
        }
    }
    
    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    @Override
    public String getValue(String key) {
        // 解析数据源格式: "sql:table.field?condition" 或 "sql:query"
        if (!key.startsWith("sql:")) {
            return cache.get(key);
        }
        
        String sqlPart = key.substring(4);
        
        // 检查缓存
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        
        // 执行查询
        String value = executeQuery(sqlPart);
        if (value != null) {
            cache.put(key, value);
        }
        
        return value;
    }
    
    @Override
    public Map<String, String> getValues(String... keys) {
        Map<String, String> result = new HashMap<>();
        for (String key : keys) {
            result.put(key, getValue(key));
        }
        return result;
    }
    
    @Override
    public boolean supports(String sourceType) {
        return "sql".equals(sourceType);
    }
    
    /**
     * 执行查询
     */
    private String executeQuery(String query) {
        if (!isConnected()) {
            Log.e(TAG, "Not connected to SQL Server");
            return null;
        }
        
        try {
            // 如果是简单字段格式 (table.field)
            if (query.contains(".") && !query.contains(" ")) {
                String[] parts = query.split("\\.");
                if (parts.length == 2) {
                    String table = parts[0];
                    String field = parts[1];
                    String condition = "";
                    
                    // 检查是否有条件
                    if (field.contains("?")) {
                        String[] fieldParts = field.split("\\?");
                        field = fieldParts[0];
                        condition = "WHERE " + fieldParts[1];
                    }
                    
                    String sql = String.format("SELECT TOP 1 %s FROM %s %s", field, table, condition);
                    return executeSingleValueQuery(sql);
                }
            }
            
            // 直接执行 SQL
            return executeSingleValueQuery(query);
            
        } catch (Exception e) {
            Log.e(TAG, "Query failed: " + query, e);
            return null;
        }
    }
    
    /**
     * 执行返回单个值的查询
     */
    private String executeSingleValueQuery(String sql) {
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getString(1);
            }
            
        } catch (SQLException e) {
            Log.e(TAG, "SQL error: " + sql, e);
        }
        
        return null;
    }
    
    /**
     * 执行查询并返回多行结果
     */
    public Map<String, Object> executeQueryWithColumns(String sql) {
        Map<String, Object> result = new HashMap<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // 获取列名
            String[] columns = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columns[i] = metaData.getColumnName(i + 1);
            }
            result.put("columns", columns);
            
            // 获取数据
            java.util.List<Map<String, String>> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < columnCount; i++) {
                    row.put(columns[i], rs.getString(i + 1));
                }
                rows.add(row);
            }
            result.put("rows", rows);
            
        } catch (SQLException e) {
            Log.e(TAG, "SQL error: " + sql, e);
        }
        
        return result;
    }
    
    /**
     * 异步查询
     */
    public void queryAsync(final String sql, final QueryCallback callback) {
        new AsyncTask<Void, Void, Map<String, Object>>() {
            @Override
            protected Map<String, Object> doInBackground(Void... voids) {
                return executeQueryWithColumns(sql);
            }
            
            @Override
            protected void onPostExecute(Map<String, Object> result) {
                if (callback != null) {
                    callback.onResult(result);
                }
            }
        }.execute();
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        cache.clear();
    }
    
    /**
     * 查询回调接口
     */
    public interface QueryCallback {
        void onResult(Map<String, Object> result);
    }
}
