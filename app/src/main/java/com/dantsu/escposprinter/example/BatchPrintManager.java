package com.dantsu.escposprinter.example;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 批量打印管理器
 * 支持批量打印标签，带进度回调
 */
public class BatchPrintManager {
    
    private ExecutorService executorService;
    private Handler mainHandler;
    private BatchPrintListener listener;
    private boolean isPrinting = false;
    private boolean shouldCancel = false;
    
    // 打印间隔（毫秒），避免打印机过热
    private int printInterval = 500;
    
    public BatchPrintManager() {
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void setListener(BatchPrintListener listener) {
        this.listener = listener;
    }
    
    public void setPrintInterval(int intervalMs) {
        this.printInterval = intervalMs;
    }
    
    /**
     * 开始批量打印
     * @param template 标签模板
     * @param products 商品列表
     */
    public void startBatchPrint(LabelTemplate template, List<LabelTemplate.ProductInfo> products) {
        if (isPrinting) {
            if (listener != null) {
                listener.onError("已有打印任务在进行中");
            }
            return;
        }
        
        isPrinting = true;
        shouldCancel = false;
        executorService = Executors.newSingleThreadExecutor();
        
        executorService.execute(() -> {
            try {
                BluetoothConnection connection = BluetoothPrintersConnections.selectFirstPaired();
                if (connection == null) {
                    notifyError("未找到配对的蓝牙打印机");
                    return;
                }
                
                int total = products.size();
                notifyStart(total);
                
                for (int i = 0; i < products.size(); i++) {
                    if (shouldCancel) {
                        notifyCancelled();
                        return;
                    }
                    
                    try {
                        template.printSupermarketPrice(connection, products.get(i));
                        notifyProgress(i + 1, total);
                        
                        // 打印间隔
                        if (i < products.size() - 1) {
                            Thread.sleep(printInterval);
                        }
                    } catch (Exception e) {
                        notifyItemError(i, products.get(i).getName(), e.getMessage());
                    }
                }
                
                notifyComplete();
            } catch (Exception e) {
                notifyError(e.getMessage());
            } finally {
                isPrinting = false;
            }
        });
    }
    
    /**
     * 批量打印库存标签
     */
    public void startBatchPrintInventory(LabelTemplate template, List<LabelTemplate.InventoryInfo> inventories) {
        if (isPrinting) {
            if (listener != null) {
                listener.onError("已有打印任务在进行中");
            }
            return;
        }
        
        isPrinting = true;
        shouldCancel = false;
        executorService = Executors.newSingleThreadExecutor();
        
        executorService.execute(() -> {
            try {
                BluetoothConnection connection = BluetoothPrintersConnections.selectFirstPaired();
                if (connection == null) {
                    notifyError("未找到配对的蓝牙打印机");
                    return;
                }
                
                int total = inventories.size();
                notifyStart(total);
                
                for (int i = 0; i < inventories.size(); i++) {
                    if (shouldCancel) {
                        notifyCancelled();
                        return;
                    }
                    
                    try {
                        template.printInventory(connection, inventories.get(i));
                        notifyProgress(i + 1, total);
                        
                        if (i < inventories.size() - 1) {
                            Thread.sleep(printInterval);
                        }
                    } catch (Exception e) {
                        notifyItemError(i, inventories.get(i).getProductName(), e.getMessage());
                    }
                }
                
                notifyComplete();
            } catch (Exception e) {
                notifyError(e.getMessage());
            } finally {
                isPrinting = false;
            }
        });
    }
    
    /**
     * 批量打印货架标签
     */
    public void startBatchPrintShelf(LabelTemplate template, List<LabelTemplate.ShelfInfo> shelves) {
        if (isPrinting) {
            if (listener != null) {
                listener.onError("已有打印任务在进行中");
            }
            return;
        }
        
        isPrinting = true;
        shouldCancel = false;
        executorService = Executors.newSingleThreadExecutor();
        
        executorService.execute(() -> {
            try {
                BluetoothConnection connection = BluetoothPrintersConnections.selectFirstPaired();
                if (connection == null) {
                    notifyError("未找到配对的蓝牙打印机");
                    return;
                }
                
                int total = shelves.size();
                notifyStart(total);
                
                for (int i = 0; i < shelves.size(); i++) {
                    if (shouldCancel) {
                        notifyCancelled();
                        return;
                    }
                    
                    try {
                        template.printShelfTag(connection, shelves.get(i));
                        notifyProgress(i + 1, total);
                        
                        if (i < shelves.size() - 1) {
                            Thread.sleep(printInterval);
                        }
                    } catch (Exception e) {
                        notifyItemError(i, shelves.get(i).getShelfCode(), e.getMessage());
                    }
                }
                
                notifyComplete();
            } catch (Exception e) {
                notifyError(e.getMessage());
            } finally {
                isPrinting = false;
            }
        });
    }
    
    /**
     * 取消批量打印
     */
    public void cancelBatchPrint() {
        shouldCancel = true;
    }
    
    /**
     * 释放资源
     */
    public void release() {
        shouldCancel = true;
        if (executorService != null) {
            executorService.shutdown();
        }
    }
    
    public boolean isPrinting() {
        return isPrinting;
    }
    
    // ==================== 通知回调 ====================
    
    private void notifyStart(int total) {
        if (listener != null) {
            mainHandler.post(() -> listener.onStart(total));
        }
    }
    
    private void notifyProgress(int current, int total) {
        if (listener != null) {
            mainHandler.post(() -> listener.onProgress(current, total));
        }
    }
    
    private void notifyComplete() {
        if (listener != null) {
            mainHandler.post(() -> listener.onComplete());
        }
    }
    
    private void notifyCancelled() {
        if (listener != null) {
            mainHandler.post(() -> listener.onCancelled());
        }
    }
    
    private void notifyError(String message) {
        if (listener != null) {
            mainHandler.post(() -> listener.onError(message));
        }
    }
    
    private void notifyItemError(int index, String itemName, String error) {
        if (listener != null) {
            mainHandler.post(() -> listener.onItemError(index, itemName, error));
        }
    }
    
    // ==================== 回调接口 ====================
    
    public interface BatchPrintListener {
        void onStart(int total);
        void onProgress(int current, int total);
        void onComplete();
        void onCancelled();
        void onError(String message);
        void onItemError(int index, String itemName, String error);
    }
    
    // ==================== 批量打印数据构建器 ====================
    
    public static class ProductListBuilder {
        private List<LabelTemplate.ProductInfo> products = new ArrayList<>();
        private String storeName = "我的店铺";
        
        public ProductListBuilder setStoreName(String storeName) {
            this.storeName = storeName;
            return this;
        }
        
        public ProductListBuilder addProduct(String name, String price, String barcode) {
            products.add(new LabelTemplate.ProductInfo(storeName, name, price, barcode));
            return this;
        }
        
        public ProductListBuilder addProduct(String name, String price, String originalPrice, String barcode) {
            LabelTemplate.ProductInfo product = new LabelTemplate.ProductInfo(storeName, name, price, barcode);
            product.setOriginalPrice(originalPrice);
            products.add(product);
            return this;
        }
        
        public List<LabelTemplate.ProductInfo> build() {
            return products;
        }
    }
    
    public static class InventoryListBuilder {
        private List<LabelTemplate.InventoryInfo> inventories = new ArrayList<>();
        
        public InventoryListBuilder addInventory(String productName, String sku, String quantity, String location) {
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(new java.util.Date());
            inventories.add(new LabelTemplate.InventoryInfo(productName, sku, quantity, location, date));
            return this;
        }
        
        public List<LabelTemplate.InventoryInfo> build() {
            return inventories;
        }
    }
    
    public static class ShelfListBuilder {
        private List<LabelTemplate.ShelfInfo> shelves = new ArrayList<>();
        
        public ShelfListBuilder addShelf(String shelfCode, String category) {
            shelves.add(new LabelTemplate.ShelfInfo(shelfCode, category));
            return this;
        }
        
        public List<LabelTemplate.ShelfInfo> build() {
            return shelves;
        }
    }
}
