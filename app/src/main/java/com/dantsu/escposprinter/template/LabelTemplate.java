package com.dantsu.escposprinter.template;

import com.dantsu.escposprinter.CpclPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 标签模板
 * 包含纸张尺寸和组件列表
 */
public class LabelTemplate implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String description;
    private int width;          // 宽度 (mm)
    private int height;         // 高度 (mm)
    private int dpi;            // DPI
    private List<LabelComponent> components;
    private long createdTime;
    private long modifiedTime;
    private boolean isDefault;  // 是否为默认模板
    
    public LabelTemplate() {
        this.id = UUID.randomUUID().toString();
        this.components = new ArrayList<>();
        this.createdTime = System.currentTimeMillis();
        this.modifiedTime = System.currentTimeMillis();
        this.width = 50;
        this.height = 30;
        this.dpi = 203;
        this.isDefault = false;
    }
    
    public LabelTemplate(String name, int width, int height, int dpi) {
        this();
        this.name = name;
        this.width = width;
        this.height = height;
        this.dpi = dpi;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        this.modifiedTime = System.currentTimeMillis();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.modifiedTime = System.currentTimeMillis();
    }
    
    public int getWidth() { return width; }
    public void setWidth(int width) { 
        this.width = width;
        this.modifiedTime = System.currentTimeMillis();
    }
    
    public int getHeight() { return height; }
    public void setHeight(int height) { 
        this.height = height;
        this.modifiedTime = System.currentTimeMillis();
    }
    
    public int getDpi() { return dpi; }
    public void setDpi(int dpi) { 
        this.dpi = dpi;
        this.modifiedTime = System.currentTimeMillis();
    }
    
    public List<LabelComponent> getComponents() { return components; }
    
    public long getCreatedTime() { return createdTime; }
    public long getModifiedTime() { return modifiedTime; }
    
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    
    /**
     * 添加组件
     */
    public void addComponent(LabelComponent component) {
        components.add(component);
        modifiedTime = System.currentTimeMillis();
    }
    
    /**
     * 移除组件
     */
    public void removeComponent(LabelComponent component) {
        components.remove(component);
        modifiedTime = System.currentTimeMillis();
    }
    
    /**
     * 移除指定ID的组件
     */
    public void removeComponent(String componentId) {
        components.removeIf(c -> c.getId().equals(componentId));
        modifiedTime = System.currentTimeMillis();
    }
    
    /**
     * 获取指定ID的组件
     */
    public LabelComponent getComponent(String componentId) {
        for (LabelComponent component : components) {
            if (component.getId().equals(componentId)) {
                return component;
            }
        }
        return null;
    }
    
    /**
     * 获取指定类型的组件
     */
    public List<LabelComponent> getComponentsByType(LabelComponent.ComponentType type) {
        List<LabelComponent> result = new ArrayList<>();
        for (LabelComponent component : components) {
            if (component.getType() == type) {
                result.add(component);
            }
        }
        return result;
    }
    
    /**
     * 清空所有组件
     */
    public void clearComponents() {
        components.clear();
        modifiedTime = System.currentTimeMillis();
    }
    
    /**
     * 复制模板
     */
    public LabelTemplate copy() {
        LabelTemplate copy = new LabelTemplate();
        copy.name = this.name + " (复制)";
        copy.description = this.description;
        copy.width = this.width;
        copy.height = this.height;
        copy.dpi = this.dpi;
        for (LabelComponent component : this.components) {
            copy.addComponent(component.clone());
        }
        return copy;
    }
    
    /**
     * 打印模板
     * @param connection 打印机连接
     * @param dataProvider 数据提供者
     */
    public void print(DeviceConnection connection, DataProvider dataProvider) throws Exception {
        CpclPrinter printer = new CpclPrinter(connection);
        printer.setLabelSize(width, height, dpi);
        printer.newLabel();
        
        for (LabelComponent component : components) {
            printComponent(printer, component, dataProvider);
        }
        
        printer.print();
    }
    
    /**
     * 打印单个组件
     */
    private void printComponent(CpclPrinter printer, LabelComponent component, DataProvider dataProvider) 
            throws EscPosEncodingException {
        
        switch (component.getType()) {
            case TEXT:
                printTextComponent(printer, (TextComponent) component, dataProvider);
                break;
            case BARCODE:
                printBarcodeComponent(printer, (BarcodeComponent) component, dataProvider);
                break;
            case QRCODE:
                printQRCodeComponent(printer, (QRCodeComponent) component, dataProvider);
                break;
            case LINE:
                printLineComponent(printer, (LineComponent) component);
                break;
        }
    }
    
    private void printTextComponent(CpclPrinter printer, TextComponent component, DataProvider dataProvider) 
            throws EscPosEncodingException {
        String value = component.getValue(dataProvider);
        int x = component.getX();
        
        // 处理对齐
        if (component.getAlign() == TextComponent.TextAlign.CENTER) {
            int textWidth = value.length() * 8 * component.getFontWidth();
            x = (mmToDots(width) - textWidth) / 2;
        } else if (component.getAlign() == TextComponent.TextAlign.RIGHT) {
            int textWidth = value.length() * 8 * component.getFontWidth();
            x = mmToDots(width) - textWidth - component.getX();
        }
        
        printer.printText(
            component.getFontFamily().ordinal(),
            component.getFontSize(),
            x,
            component.getY(),
            value
        );
    }
    
    private void printBarcodeComponent(CpclPrinter printer, BarcodeComponent component, DataProvider dataProvider) 
            throws EscPosEncodingException {
        String value = component.getValue(dataProvider);
        // 使用 CpclPrinter 的公共 API - 根据条码类型选择对应方法
        switch (component.getBarcodeType()) {
            case CODE128:
                printer.printBarcode128(component.getX(), component.getY(), component.getHeight(), value);
                break;
            case EAN13:
                printer.printBarcodeEAN13(component.getX(), component.getY(), component.getHeight(), value);
                break;
            case UPCA:
                printer.printBarcodeUPCA(component.getX(), component.getY(), component.getHeight(), value);
                break;
            default:
                // 对于 CODE39、EAN8、UPCE 等，使用通用的 addBarcode 方法
                printer.addBarcode(component.getX(), component.getY(), value, 
                    CpclPrinter.BarcodeType.valueOf(component.getBarcodeType().name()), 
                    component.getBarWidth(), component.getHeight());
                break;
        }
    }
    
    private void printQRCodeComponent(CpclPrinter printer, QRCodeComponent component, DataProvider dataProvider) 
            throws EscPosEncodingException {
        String value = component.getValue(dataProvider);
        printer.printQRCode(
            component.getX(),
            component.getY(),
            component.getCellWidth(),
            value
        );
    }
    
    private void printLineComponent(CpclPrinter printer, LineComponent component) 
            throws EscPosEncodingException {
        printer.drawLine(
            component.getX(),
            component.getY(),
            component.getEndX(),
            component.getEndY(),
            component.getThickness()
        );
    }
    
    private int mmToDots(float mm) {
        return Math.round(mm * dpi / 25.4f);
    }
    
    @Override
    public String toString() {
        return name + " (" + width + "x" + height + "mm @ " + dpi + "DPI)";
    }
}
