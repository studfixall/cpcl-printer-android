package com.dantsu.escposprinter.template;

import java.io.Serializable;

/**
 * 标签组件基类
 * 支持文本、条码、二维码等组件
 */
public abstract class LabelComponent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public enum ComponentType {
        TEXT,       // 文本
        BARCODE,    // 条码
        QRCODE,     // 二维码
        LINE,       // 线条
        BOX,        // 方框
        IMAGE       // 图片
    }
    
    protected String id;
    protected ComponentType type;
    protected int x;              // X坐标 (dots)
    protected int y;              // Y坐标 (dots)
    protected int width;          // 宽度
    protected int height;         // 高度
    protected boolean isDynamic;  // 是否动态值
    protected String dataSource;  // 数据源 (如: sql:product_name, api:price, etc.)
    protected String staticValue; // 静态值
    
    public LabelComponent(ComponentType type, String id) {
        this.type = type;
        this.id = id;
        this.x = 0;
        this.y = 0;
        this.width = 100;
        this.height = 30;
        this.isDynamic = false;
        this.dataSource = "";
        this.staticValue = "";
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public ComponentType getType() { return type; }
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    
    public boolean isDynamic() { return isDynamic; }
    public void setDynamic(boolean dynamic) { isDynamic = dynamic; }
    
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    
    public String getStaticValue() { return staticValue; }
    public void setStaticValue(String staticValue) { this.staticValue = staticValue; }
    
    /**
     * 获取实际要打印的值
     * @param dataProvider 数据提供者
     * @return 实际值
     */
    public String getValue(DataProvider dataProvider) {
        if (isDynamic && dataProvider != null && !dataSource.isEmpty()) {
            String value = dataProvider.getValue(dataSource);
            return value != null ? value : staticValue;
        }
        return staticValue;
    }
    
    /**
     * 克隆组件
     */
    public abstract LabelComponent clone();
}
