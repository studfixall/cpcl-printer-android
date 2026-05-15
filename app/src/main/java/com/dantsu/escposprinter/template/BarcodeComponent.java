package com.dantsu.escposprinter.template;

/**
 * 条码组件
 */
public class BarcodeComponent extends LabelComponent {
    
    private static final long serialVersionUID = 1L;
    
    public enum BarcodeType {
        CODE128,    // Code 128
        CODE39,     // Code 39
        CODE93,     // Code 93
        EAN13,      // EAN-13
        EAN8,       // EAN-8
        UPCA,       // UPC-A
        UPCE,       // UPC-E
        CODABAR,    // Codabar
        I2OF5       // Interleaved 2 of 5
    }
    
    public enum TextPosition {
        NONE,       // 不显示文字
        BELOW,      // 文字在下方
        ABOVE,      // 文字在上方
        BOTH        // 上下都显示
    }
    
    private BarcodeType barcodeType = BarcodeType.CODE128;
    private int barWidth = 2;           // 条宽 (1-10)
    private int ratio = 0;              // 宽窄比
    private boolean showText = true;    // 显示文字
    private TextPosition textPosition = TextPosition.BELOW;
    private int textFont = 0;           // 文字字体
    private String format = "";         // 格式化字符串 (如: "{0} {1}")
    
    public BarcodeComponent(String id) {
        super(ComponentType.BARCODE, id);
        this.height = 60;  // 默认条码高度
        this.width = 200;
    }
    
    // Getters and Setters
    public BarcodeType getBarcodeType() { return barcodeType; }
    public void setBarcodeType(BarcodeType barcodeType) { this.barcodeType = barcodeType; }
    
    public int getBarWidth() { return barWidth; }
    public void setBarWidth(int barWidth) { this.barWidth = Math.max(1, Math.min(10, barWidth)); }
    
    public int getRatio() { return ratio; }
    public void setRatio(int ratio) { this.ratio = ratio; }
    
    public boolean isShowText() { return showText; }
    public void setShowText(boolean showText) { this.showText = showText; }
    
    public TextPosition getTextPosition() { return textPosition; }
    public void setTextPosition(TextPosition textPosition) { this.textPosition = textPosition; }
    
    public int getTextFont() { return textFont; }
    public void setTextFont(int textFont) { this.textFont = textFont; }
    
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
    
    /**
     * 获取CPCL条码类型值
     */
    public int getCpclBarcodeType() {
        switch (barcodeType) {
            case CODE128: return 0;
            case CODE39: return 1;
            case CODE93: return 2;
            case CODABAR: return 3;
            case EAN13: return 4;
            case EAN8: return 5;
            case UPCA: return 6;
            case UPCE: return 7;
            case I2OF5: return 8;
            default: return 0;
        }
    }
    
    @Override
    public LabelComponent clone() {
        BarcodeComponent clone = new BarcodeComponent(this.id);
        clone.x = this.x;
        clone.y = this.y;
        clone.width = this.width;
        clone.height = this.height;
        clone.isDynamic = this.isDynamic;
        clone.dataSource = this.dataSource;
        clone.staticValue = this.staticValue;
        clone.barcodeType = this.barcodeType;
        clone.barWidth = this.barWidth;
        clone.ratio = this.ratio;
        clone.showText = this.showText;
        clone.textPosition = this.textPosition;
        clone.textFont = this.textFont;
        clone.format = this.format;
        return clone;
    }
}
