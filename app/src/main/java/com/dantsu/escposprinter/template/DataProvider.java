package com.dantsu.escposprinter.template;

import java.util.Map;

/**
 * 数据提供者接口
 * 用于获取动态数据
 */
public interface DataProvider {
    
    /**
     * 获取值
     * @param key 数据键 (如: "sql:product_name", "api:price", "field:barcode")
     * @return 值
     */
    String getValue(String key);
    
    /**
     * 批量获取值
     * @param keys 数据键数组
     * @return 键值对
     */
    Map<String, String> getValues(String... keys);
    
    /**
     * 检查是否支持某个数据源
     * @param sourceType 数据源类型 (sql, api, field, etc.)
     * @return 是否支持
     */
    boolean supports(String sourceType);
}
