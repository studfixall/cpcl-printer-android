package com.dantsu.escposprinter.template;

/**
 * 文本组件
 */
public class TextComponent extends LabelComponent {
    
    private static final long serialVersionUID = 1L;
    
    public enum FontFamily {
        FONT_0, FONT_1, FONT_2, FONT_3, FONT_4, FONT_5, FONT_6, FONT_7
    }
    
    public enum TextAlign {
        LEFT, CENTER, RIGHT
    }
    
    private FontFamily fontFamily = FontFamily.FONT_0;
    private int fontSize = 0;           // 字体大小倍数 (0-7)
    private int fontWidth = 1;          // 宽度倍数 (1-8)
    private int fontHeight = 1;         // 高度倍数 (1-8)
    private boolean bold = false;       // 粗体
    private boolean underline = false;  // 下划线
    private boolean reverse = false;    // 反白
    private TextAlign align = TextAlign.LEFT;
    private int maxChars = 0;           // 最大字符数 (0=无限制)
    private String prefix = "";         // 前缀 (如: "￥")
    private String suffix = "";         // 后缀 (如: "元")
    
    public TextComponent(String id) {
        super(ComponentType.TEXT, id);
        this.height = 24;  // 默认文本高度
    }
    
    // Getters and Setters
    public FontFamily getFontFamily() { return fontFamily; }
    public void setFontFamily(FontFamily fontFamily) { this.fontFamily = fontFamily; }
    
    public int getFontSize() { return fontSize; }
    public void setFontSize(int fontSize) { this.fontSize = Math.max(0, Math.min(7, fontSize)); }
    
    public int getFontWidth() { return fontWidth; }
    public void setFontWidth(int fontWidth) { this.fontWidth = Math.max(1, Math.min(8, fontWidth)); }
    
    public int getFontHeight() { return fontHeight; }
    public void setFontHeight(int fontHeight) { this.fontHeight = Math.max(1, Math.min(8, fontHeight)); }
    
    public boolean isBold() { return bold; }
    public void setBold(boolean bold) { this.bold = bold; }
    
    public boolean isUnderline() { return underline; }
    public void setUnderline(boolean underline) { this.underline = underline; }
    
    public boolean isReverse() { return reverse; }
    public void setReverse(boolean reverse) { this.reverse = reverse; }
    
    public TextAlign getAlign() { return align; }
    public void setAlign(TextAlign align) { this.align = align; }
    
    public int getMaxChars() { return maxChars; }
    public void setMaxChars(int maxChars) { this.maxChars = maxChars; }
    
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    
    @Override
    public String getValue(DataProvider dataProvider) {
        String value = super.getValue(dataProvider);
        if (value == null) value = "";
        
        // 截断处理
        if (maxChars > 0 && value.length() > maxChars) {
            value = value.substring(0, maxChars);
        }
        
        return prefix + value + suffix;
    }
    
    @Override
    public LabelComponent clone() {
        TextComponent clone = new TextComponent(this.id);
        clone.x = this.x;
        clone.y = this.y;
        clone.width = this.width;
        clone.height = this.height;
        clone.isDynamic = this.isDynamic;
        clone.dataSource = this.dataSource;
        clone.staticValue = this.staticValue;
        clone.fontFamily = this.fontFamily;
        clone.fontSize = this.fontSize;
        clone.fontWidth = this.fontWidth;
        clone.fontHeight = this.fontHeight;
        clone.bold = this.bold;
        clone.underline = this.underline;
        clone.reverse = this.reverse;
        clone.align = this.align;
        clone.maxChars = this.maxChars;
        clone.prefix = this.prefix;
        clone.suffix = this.suffix;
        return clone;
    }
}
