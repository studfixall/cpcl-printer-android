package com.dantsu.escposprinter.template;

/**
 * 二维码组件
 */
public class QRCodeComponent extends LabelComponent {
    
    private static final long serialVersionUID = 1L;
    
    public enum ErrorCorrection {
        L(0),  // 7%
        M(1),  // 15%
        Q(2),  // 25%
        H(3);  // 30%
        
        private final int value;
        ErrorCorrection(int value) { this.value = value; }
        public int getValue() { return value; }
    }
    
    private int cellWidth = 2;          // 单元格宽度 (1-32)
    private ErrorCorrection ecLevel = ErrorCorrection.M;
    private String format = "";         // 格式化字符串
    
    public QRCodeComponent(String id) {
        super(ComponentType.QRCODE, id);
        this.width = 100;
        this.height = 100;
    }
    
    // Getters and Setters
    public int getCellWidth() { return cellWidth; }
    public void setCellWidth(int cellWidth) { this.cellWidth = Math.max(1, Math.min(32, cellWidth)); }
    
    public ErrorCorrection getEcLevel() { return ecLevel; }
    public void setEcLevel(ErrorCorrection ecLevel) { this.ecLevel = ecLevel; }
    
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    
    @Override
    public String getValue(DataProvider dataProvider) {
        String value = super.getValue(dataProvider);
        if (value == null) value = "";
        
        // 应用格式化
        if (!format.isEmpty()) {
            try {
                return format.replace("{0}", value);
            } catch (Exception e) {
                return value;
            }
        }
        
        return value;
    }
    
    @Override
    public LabelComponent clone() {
        QRCodeComponent clone = new QRCodeComponent(this.id);
        clone.x = this.x;
        clone.y = this.y;
        clone.width = this.width;
        clone.height = this.height;
        clone.isDynamic = this.isDynamic;
        clone.dataSource = this.dataSource;
        clone.staticValue = this.staticValue;
        clone.cellWidth = this.cellWidth;
        clone.ecLevel = this.ecLevel;
        clone.format = this.format;
        return clone;
    }
}
