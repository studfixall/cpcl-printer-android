package com.dantsu.escposprinter.example;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dantsu.escposprinter.CpclPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.thermalprinter.R;

/**
 * CPCL 标签打印示例 Activity
 * 演示如何使用 CPCL 指令打印商品标签
 */
public class CpclLabelPrintActivity extends AppCompatActivity {

    private static final String TAG = "CpclLabelPrint";
    private static final int PERMISSION_BLUETOOTH = 1;
    private static final int PERMISSION_BLUETOOTH_ADMIN = 2;
    private static final int PERMISSION_BLUETOOTH_CONNECT = 3;
    private static final int PERMISSION_BLUETOOTH_SCAN = 4;
    private static final int PERMISSION_LOCATION = 5;

    private EditText etProductName, etProductPrice, etBarcode;
    private Button btnPrintLabel, btnPrintTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpcl_label_print);

        initViews();
        checkPermissions();
    }

    private void initViews() {
        etProductName = findViewById(R.id.etProductName);
        etProductPrice = findViewById(R.id.etProductPrice);
        etBarcode = findViewById(R.id.etBarcode);
        btnPrintLabel = findViewById(R.id.btnPrintLabel);
        btnPrintTest = findViewById(R.id.btnPrintTest);

        btnPrintLabel.setOnClickListener(v -> printProductLabel());
        btnPrintTest.setOnClickListener(v -> printTestLabel());
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            // Android 12+ 需要 BLUETOOTH_CONNECT 和 BLUETOOTH_SCAN
            String[] permissions = new String[]{
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            };
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_BLUETOOTH_CONNECT);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Android 6-11 需要位置权限来扫描蓝牙设备
            String[] permissions = new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            };
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_BLUETOOTH);
        }
    }

    /**
     * 打印商品标签 - 适用于超市商品
     */
    private void printProductLabel() {
        String productName = etProductName.getText().toString().trim();
        String price = etProductPrice.getText().toString().trim();
        String barcode = etBarcode.getText().toString().trim();

        if (productName.isEmpty()) {
            productName = "测试商品";
        }
        if (price.isEmpty()) {
            price = "99.99";
        }
        if (barcode.isEmpty()) {
            barcode = "6901234567890";
        }

        // 检查蓝牙权限
        if (!checkBluetoothPermission()) {
            return;
        }

        // 检查蓝牙适配器
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_LONG).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "请先开启蓝牙", Toast.LENGTH_LONG).show();
            return;
        }

        BluetoothConnection connection = null;
        try {
            connection = BluetoothPrintersConnections.selectFirstPaired();
        } catch (Exception e) {
            Log.e(TAG, "蓝牙连接错误", e);
            Toast.makeText(this, "蓝牙连接错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (connection == null) {
            Toast.makeText(this, "未找到配对的蓝牙打印机\n请先配对打印机", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // 确保连接已建立
            if (!connection.isConnected()) {
                connection.connect();
            }
            
            CpclPrinter printer = new CpclPrinter(connection);

            // 标签尺寸：50mm x 30mm，300dpi
            printer.setLabelSize(50, 30, 300);

            // 商品名称（大号字体）
            printer.setMagnify(2, 2);
            printer.addText(10, 10, productName);

            // 价格（红色大字）
            printer.setMagnify(3, 3);
            printer.addText(10, 80, "￥" + price);

            // 条形码
            printer.addBarcode(10, 160, barcode, CpclPrinter.BarcodeType.CODE128, 2, 50);

            // 条码文字
            printer.setMagnify(1, 1);
            printer.addText(10, 220, barcode);

            // 打印
            printer.print();

            Toast.makeText(this, "标签打印成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "打印失败", e);
            Toast.makeText(this, "打印失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            // 断开连接
            if (connection != null && connection.isConnected()) {
                connection.disconnect();
            }
        }
    }

    /**
     * 打印测试标签 - 多种元素展示
     */
    private void printTestLabel() {
        // 检查蓝牙权限
        if (!checkBluetoothPermission()) {
            Toast.makeText(this, "请先授予蓝牙权限", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查蓝牙适配器
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_LONG).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "请先开启蓝牙", Toast.LENGTH_LONG).show();
            return;
        }

        BluetoothConnection connection = null;
        try {
            connection = BluetoothPrintersConnections.selectFirstPaired();
        } catch (Exception e) {
            Log.e(TAG, "蓝牙连接错误", e);
            Toast.makeText(this, "蓝牙连接错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (connection == null) {
            Toast.makeText(this, "未找到配对的蓝牙打印机\n请先配对打印机", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // 确保连接已建立
            if (!connection.isConnected()) {
                connection.connect();
            }
            
            CpclPrinter printer = new CpclPrinter(connection);

            // 标签尺寸：60mm x 40mm，203dpi
            printer.setLabelSize(60, 40, 203);

            // 标题
            printer.setMagnify(2, 2);
            printer.addText(10, 10, "CPCL TEST");

            // 不同大小的文字
            printer.setMagnify(1, 1);
            printer.addText(10, 60, "Normal Text");

            printer.setMagnify(1, 2);
            printer.addText(10, 90, "Tall Text");

            printer.setMagnify(2, 1);
            printer.addText(10, 130, "Wide Text");

            // 各种条码类型
            printer.setMagnify(1, 1);
            printer.addText(10, 170, "Code128:");
            printer.addBarcode(10, 190, "1234567890", CpclPrinter.BarcodeType.CODE128, 2, 40);

            printer.addText(10, 240, "Code39:");
            printer.addBarcode(10, 260, "ABC123", CpclPrinter.BarcodeType.CODE39, 2, 40);

            // 打印
            printer.print();

            Toast.makeText(this, "测试标签打印成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "打印失败", e);
            Toast.makeText(this, "打印失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            // 断开连接
            if (connection != null && connection.isConnected()) {
                connection.disconnect();
            }
        }
    }

    /**
     * 检查蓝牙权限
     */
    private boolean checkBluetoothPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            // Android 12+ 检查 BLUETOOTH_CONNECT
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                };
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_BLUETOOTH_CONNECT);
                return false;
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Android 6-11 检查位置和蓝牙权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION
                };
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_BLUETOOTH);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "需要蓝牙权限才能打印", Toast.LENGTH_SHORT).show();
        }
    }
}
