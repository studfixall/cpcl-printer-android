package com.dantsu.escposprinter.example;

import com.dantsu.escposprinter.CpclPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;

/**
 * 标签模板类
 * 预定义多种常用标签格式
 */
public class LabelTemplate {
    
    public enum TemplateType {
        SUPERMARKET_PRICE,      // 超市价格标签
        INVENTORY,              // 库存标签
        LOGISTICS,              // 物流标签
        SHELF_TAG,              // 货架标签
        FRESH_FOOD,             // 生鲜标签
        CUSTOM                  // 自定义
    }
    
    private TemplateType type;
    private int width;
    private int height;
    private int dpi;
    
    public LabelTemplate(TemplateType type) {
        this.type = type;
        setDefaultSize();
    }
    
    public LabelTemplate(TemplateType type, int width, int height, int dpi) {
        this.type = type;
        this.width = width;
        this.height = height;
        this.dpi = dpi;
    }
    
    private void setDefaultSize() {
        switch (type) {
            case SUPERMARKET_PRICE:
                width = 50; height = 30; dpi = 203;
                break;
            case INVENTORY:
                width = 60; height = 40; dpi = 203;
                break;
            case LOGISTICS:
                width = 100; height = 80; dpi = 203;
                break;
            case SHELF_TAG:
                width = 40; height = 20; dpi = 203;
                break;
            case FRESH_FOOD:
                width = 50; height = 35; dpi = 203;
                break;
            default:
                width = 50; height = 30; dpi = 203;
        }
    }
    
    /**
     * 打印超市价格标签
     */
    public void printSupermarketPrice(DeviceConnection connection, ProductInfo product) throws Exception {
        CpclPrinter printer = new CpclPrinter(connection);
        printer.setLabelSize(width, height, dpi);
        
        // 店名/品牌
        printer.setMagnify(1, 1);
        printer.addText(10, 10, product.getStoreName());
        
        // 商品名称（大号）
        printer.setMagnify(2, 2);
        printer.addText(10, 40, truncate(product.getName(), 10));
        
        // 价格（超大号红色）
        printer.setMagnify(3, 3);
        printer.addText(10, 90, "￥" + product.getPrice());
        
        // 原价（删除线效果，用小号字体）
        if (product.getOriginalPrice() != null && !product.getOriginalPrice().isEmpty()) {
            printer.setMagnify(1, 1);
            printer.addText(10, 150, "原价:￥" + product.getOriginalPrice());
        }
        
        // 条形码
        printer.addBarcode(10, 180, product.getBarcode(), CpclPrinter.BarcodeType.CODE128, 2, 40);
        
        // 条码数字
        printer.setMagnify(1, 1);
        printer.addText(10, 230, product.getBarcode());
        
        printer.print();
    }
    
    /**
     * 打印库存标签
     */
    public void printInventory(DeviceConnection connection, InventoryInfo inventory) throws Exception {
        CpclPrinter printer = new CpclPrinter(connection);
        printer.setLabelSize(width, height, dpi);
        
        // 标题
        printer.setMagnify(2, 1);
        printer.addText(10, 10, "库存标签");
        
        // 商品信息
        printer.setMagnify(1, 1);
        printer.addText(10, 50, "名称:" + truncate(inventory.getProductName(), 12));
        printer.addText(10, 80, "SKU:" + inventory.getSku());
        printer.addText(10, 110, "数量:" + inventory.getQuantity());
        printer.addText(10, 140, "位置:" + inventory.getLocation());
        printer.addText(10, 170, "日期:" + inventory.getDate());
        
        // 二维码（可包含完整信息）
        printer.addQRCode(300, 50, inventory.getQrCodeData(), 5);
        
        printer.print();
    }
    
    /**
     * 打印物流标签
     */
    public void printLogistics(DeviceConnection connection, LogisticsInfo logistics) throws Exception {
        CpclPrinter printer = new CpclPrinter(connection);
        printer.setLabelSize(width, height, dpi);
        
        // 快递公司
        printer.setMagnify(2, 2);
        printer.addText(10, 10, logistics.getCompany());
        
        // 收件人信息
        printer.setMagnify(1, 1);
        printer.addText(10, 60, "收件:" + logistics.getReceiverName() + " " + logistics.getReceiverPhone());
        printer.addText(10, 90, "地址:" + truncate(logistics.getReceiverAddress(), 20));
        
        // 分隔线
        printer.addText(10, 130, "------------------------------");
        
        // 寄件人信息
        printer.addText(10, 150, "寄件:" + logistics.getSenderName() + " " + logistics.getSenderPhone());
        
        // 运单号条码
        printer.setMagnify(1, 1);
        printer.addText(10, 200, "运单号:" + logistics.getTrackingNumber());
        printer.addBarcode(10, 230, logistics.getTrackingNumber(), CpclPrinter.BarcodeType.CODE128, 3, 60);
        
        printer.print();
    }
    
    /**
     * 打印货架标签
     */
    public void printShelfTag(DeviceConnection connection, ShelfInfo shelf) throws Exception {
        CpclPrinter printer = new CpclPrinter(connection);
        printer.setLabelSize(width, height, dpi);
        
        // 货架编号（超大）
        printer.setMagnify(3, 3);
        printer.addText(10, 10, shelf.getShelfCode());
        
        // 分类
        printer.setMagnify(1, 1);
        printer.addText(10, 80, shelf.getCategory());
        
        printer.print();
    }
    
    /**
     * 打印生鲜标签
     */
    public void printFreshFood(DeviceConnection connection, FreshFoodInfo food) throws Exception {
        CpclPrinter printer = new CpclPrinter(connection);
        printer.setLabelSize(width, height, dpi);
        
        // 商品名
        printer.setMagnify(2, 2);
        printer.addText(10, 10, food.getName());
        
        // 价格
        printer.setMagnify(2, 2);
        printer.addText(10, 60, "￥" + food.getPrice() + "/" + food.getUnit());
        
        // 产地
        printer.setMagnify(1, 1);
        printer.addText(10, 110, "产地:" + food.getOrigin());
        
        // 日期信息
        printer.addText(10, 140, "上市:" + food.getStockDate());
        printer.addText(10, 170, "保质:" + food.getShelfLife());
        
        // 追溯码
        printer.addBarcode(10, 210, food.getTraceCode(), CpclPrinter.BarcodeType.QR_CODE, 4, 40);
        
        printer.print();
    }
    
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
    
    // Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDpi() { return dpi; }
    public TemplateType getType() { return type; }
    
    // ==================== 数据类 ====================
    
    public static class ProductInfo {
        private String storeName;
        private String name;
        private String price;
        private String originalPrice;
        private String barcode;
        
        public ProductInfo(String storeName, String name, String price, String barcode) {
            this.storeName = storeName;
            this.name = name;
            this.price = price;
            this.barcode = barcode;
        }
        
        // Getters and Setters
        public String getStoreName() { return storeName; }
        public void setStoreName(String storeName) { this.storeName = storeName; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        public String getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(String originalPrice) { this.originalPrice = originalPrice; }
        public String getBarcode() { return barcode; }
        public void setBarcode(String barcode) { this.barcode = barcode; }
    }
    
    public static class InventoryInfo {
        private String productName;
        private String sku;
        private String quantity;
        private String location;
        private String date;
        private String qrCodeData;
        
        public InventoryInfo(String productName, String sku, String quantity, String location, String date) {
            this.productName = productName;
            this.sku = sku;
            this.quantity = quantity;
            this.location = location;
            this.date = date;
            this.qrCodeData = sku + "|" + location + "|" + quantity;
        }
        
        // Getters and Setters
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getQrCodeData() { return qrCodeData; }
        public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
    }
    
    public static class LogisticsInfo {
        private String company;
        private String receiverName;
        private String receiverPhone;
        private String receiverAddress;
        private String senderName;
        private String senderPhone;
        private String trackingNumber;
        
        // Getters and Setters
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String getReceiverName() { return receiverName; }
        public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
        public String getReceiverPhone() { return receiverPhone; }
        public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
        public String getReceiverAddress() { return receiverAddress; }
        public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        public String getSenderPhone() { return senderPhone; }
        public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    }
    
    public static class ShelfInfo {
        private String shelfCode;
        private String category;
        
        public ShelfInfo(String shelfCode, String category) {
            this.shelfCode = shelfCode;
            this.category = category;
        }
        
        public String getShelfCode() { return shelfCode; }
        public void setShelfCode(String shelfCode) { this.shelfCode = shelfCode; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
    
    public static class FreshFoodInfo {
        private String name;
        private String price;
        private String unit;
        private String origin;
        private String stockDate;
        private String shelfLife;
        private String traceCode;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }
        public String getStockDate() { return stockDate; }
        public void setStockDate(String stockDate) { this.stockDate = stockDate; }
        public String getShelfLife() { return shelfLife; }
        public void setShelfLife(String shelfLife) { this.shelfLife = shelfLife; }
        public String getTraceCode() { return traceCode; }
        public void setTraceCode(String traceCode) { this.traceCode = traceCode; }
    }
}
