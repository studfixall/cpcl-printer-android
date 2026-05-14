package com.dantsu.escposprinter.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dantsu.escposprinter.example.LabelTemplate.ProductInfo;
import com.dantsu.thermalprinter.R;

import java.util.List;

/**
 * 批量打印 Activity
 * 演示模板打印和批量打印功能
 */
public class BatchPrintActivity extends AppCompatActivity implements BatchPrintManager.BatchPrintListener {

    private static final int PERMISSION_BLUETOOTH = 1;
    private static final int PERMISSION_BLUETOOTH_ADMIN = 2;
    private static final int PERMISSION_BLUETOOTH_CONNECT = 3;
    private static final int PERMISSION_BLUETOOTH_SCAN = 4;

    private Spinner spinnerTemplate;
    private EditText etStoreName, etProductList;
    private Button btnBatchPrint, btnCancelPrint, btnLoadDemo;
    private ProgressBar progressBar;
    private TextView tvProgress;

    private BatchPrintManager batchPrintManager;
    private LabelTemplate.TemplateType selectedTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_print);

        initViews();
        checkPermissions();
        
        batchPrintManager = new BatchPrintManager();
        batchPrintManager.setListener(this);
    }

    private void initViews() {
        spinnerTemplate = findViewById(R.id.spinnerTemplate);
        etStoreName = findViewById(R.id.etStoreName);
        etProductList = findViewById(R.id.etProductList);
        btnBatchPrint = findViewById(R.id.btnBatchPrint);
        btnCancelPrint = findViewById(R.id.btnCancelPrint);
        btnLoadDemo = findViewById(R.id.btnLoadDemo);
        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);

        // 模板选择
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.template_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTemplate.setAdapter(adapter);
        
        spinnerTemplate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTemplate = LabelTemplate.TemplateType.values()[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnBatchPrint.setOnClickListener(v -> startBatchPrint());
        btnCancelPrint.setOnClickListener(v -> cancelBatchPrint());
        btnLoadDemo.setOnClickListener(v -> loadDemoData());
        
        updateUI(false);
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_BLUETOOTH_SCAN);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, PERMISSION_BLUETOOTH);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, PERMISSION_BLUETOOTH_ADMIN);
            }
        }
    }

    private void startBatchPrint() {
        String storeName = etStoreName.getText().toString().trim();
        if (storeName.isEmpty()) {
            storeName = "我的店铺";
        }

        String productData = etProductList.getText().toString().trim();
        if (productData.isEmpty()) {
            Toast.makeText(this, "请先输入商品数据", Toast.LENGTH_SHORT).show();
            return;
        }

        // 解析商品数据
        List<ProductInfo> products = parseProductData(storeName, productData);
        if (products.isEmpty()) {
            Toast.makeText(this, "商品数据格式错误", Toast.LENGTH_SHORT).show();
            return;
        }

        LabelTemplate template = new LabelTemplate(selectedTemplate);
        batchPrintManager.startBatchPrint(template, products);
        updateUI(true);
    }

    private void cancelBatchPrint() {
        batchPrintManager.cancelBatchPrint();
    }

    private List<ProductInfo> parseProductData(String storeName, String data) {
        BatchPrintManager.ProductListBuilder builder = new BatchPrintManager.ProductListBuilder()
                .setStoreName(storeName);

        String[] lines = data.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 格式: 商品名,价格,条码 或 商品名,价格,原价,条码
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                String name = parts[0].trim();
                String price = parts[1].trim();
                String barcode = parts[parts.length - 1].trim();
                
                if (parts.length >= 4) {
                    String originalPrice = parts[2].trim();
                    builder.addProduct(name, price, originalPrice, barcode);
                } else {
                    builder.addProduct(name, price, barcode);
                }
            }
        }

        return builder.build();
    }

    private void loadDemoData() {
        etStoreName.setText("阳泉超市");
        String demoData = "苹果,5.99,6901234567890\n" +
                "香蕉,3.50,2.80,6901234567891\n" +
                "牛奶,12.80,6901234567892\n" +
                "面包,8.50,6.90,6901234567893\n" +
                "鸡蛋,15.90,6901234567894\n" +
                "大米,58.00,45.00,6901234567895";
        etProductList.setText(demoData);
    }

    private void updateUI(boolean isPrinting) {
        btnBatchPrint.setEnabled(!isPrinting);
        btnLoadDemo.setEnabled(!isPrinting);
        btnCancelPrint.setEnabled(isPrinting);
        spinnerTemplate.setEnabled(!isPrinting);
        etStoreName.setEnabled(!isPrinting);
        etProductList.setEnabled(!isPrinting);
        progressBar.setVisibility(isPrinting ? View.VISIBLE : View.GONE);
        tvProgress.setVisibility(isPrinting ? View.VISIBLE : View.GONE);
    }

    // ==================== BatchPrintListener 回调 ====================

    @Override
    public void onStart(int total) {
        tvProgress.setText("准备打印: " + total + " 张标签");
        progressBar.setMax(total);
        progressBar.setProgress(0);
    }

    @Override
    public void onProgress(int current, int total) {
        tvProgress.setText("正在打印: " + current + "/" + total);
        progressBar.setProgress(current);
    }

    @Override
    public void onComplete() {
        updateUI(false);
        tvProgress.setText("打印完成!");
        Toast.makeText(this, "批量打印完成", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelled() {
        updateUI(false);
        tvProgress.setText("已取消");
        Toast.makeText(this, "打印已取消", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String message) {
        updateUI(false);
        tvProgress.setText("错误: " + message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemError(int index, String itemName, String error) {
        Toast.makeText(this, "第" + (index + 1) + "项失败: " + itemName, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        batchPrintManager.release();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "需要蓝牙权限才能打印", Toast.LENGTH_SHORT).show();
        }
    }
}
