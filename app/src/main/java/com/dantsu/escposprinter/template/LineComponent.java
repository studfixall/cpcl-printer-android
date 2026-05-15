package com.dantsu.escposprinter.template;

/**
 * 线条组件
 */
public class LineComponent extends LabelComponent {
    
    private static final long serialVersionUID = 1L;
    
    public enum LineStyle {
        SOLID,      // 实线
        DASHED,     // 虚线
        DOTTED      // 点线
    }
    
    private int endX;           // 终点X
    private int endY;           // 终点Y
    private int thickness = 1;  // 线宽
    private LineStyle style = LineStyle.SOLID;
    
    public LineComponent(String id) {
        super(ComponentType.LINE, id);
        this.endX = 100;
        this.endY = 0;
        this.width = 100;
        this.height = 1;
    }
    
    // Getters and Setters
    public int getEndX() { return endX; }
    public void setEndX(int endX) { this.endX = endX; }
    
    public int getEndY() { return endY; }
    public void setEndY(int endY) { this.endY = endY; }
    
    public int getThickness() { return thickness; }
    public void setThickness(int thickness) { this.thickness = Math.max(1, thickness); }
    
    public LineStyle getStyle() { return style; }
    public void setStyle(LineStyle style) { this.style = style; }
    
    @Override
    public LabelComponent clone() {
        LineComponent clone = new LineComponent(this.id);
        clone.x = this.x;
        clone.y = this.y;
        clone.width = this.width;
        clone.height = this.height;
        clone.isDynamic = this.isDynamic;
        clone.dataSource = this.dataSource;
        clone.staticValue = this.staticValue;
        clone.endX = this.endX;
        clone.endY = this.endY;
        clone.thickness = this.thickness;
        clone.style = this.style;
        return clone;
    }
}
