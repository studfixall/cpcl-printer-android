package com.dantsu.escposprinter.template;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于Map的简单数据提供者
 */
public class MapDataProvider implements DataProvider {
    
    private Map<String, String> data = new HashMap<>();
    
    public MapDataProvider() {}
    
    public MapDataProvider(Map<String, String> data) {
        this.data.putAll(data);
    }
    
    public void put(String key, String value) {
        data.put(key, value);
    }
    
    public void putAll(Map<String, String> map) {
        data.putAll(map);
    }
    
    public void clear() {
        data.clear();
    }
    
    @Override
    public String getValue(String key) {
        // 解析数据源格式: "type:key" 或直接使用key
        String actualKey = key;
        if (key.contains(":")) {
            String[] parts = key.split(":", 2);
            actualKey = parts[1];
        }
        return data.get(actualKey);
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
        return true; // 支持所有类型
    }
}
