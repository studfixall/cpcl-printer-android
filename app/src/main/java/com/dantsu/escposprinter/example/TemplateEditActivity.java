package com.dantsu.escposprinter.example;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.template.BarcodeComponent;
import com.dantsu.escposprinter.template.LabelComponent;
import com.dantsu.escposprinter.template.LabelTemplate;
import com.dantsu.escposprinter.template.MapDataProvider;
import com.dantsu.escposprinter.template.QRCodeComponent;
import com.dantsu.escposprinter.template.TemplateManager;
import com.dantsu.escposprinter.template.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 模板编辑 Activity
 * 可视化编辑标签模板
 */
public class TemplateEditActivity extends AppCompatActivity {

    private static final int REQUEST_CONNECT_DEVICE = 1;

    private TemplateManager templateManager;
    private LabelTemplate currentTemplate;
    private LabelCanvasView canvasView;
    private LabelComponent selectedComponent;

    private EditText etTemplateName;
    private EditText etTemplateWidth;
    private EditText etTemplateHeight;
    private EditText etTemplateDpi;
    private LinearLayout componentPropertiesPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_edit);

        templateManager = new TemplateManager(this);

        initViews();
        loadTemplate();
    }

    private void initViews() {
        etTemplateName = findViewById(R.id.etTemplateName);
        etTemplateWidth = findViewById(R.id.etTemplateWidth);
        etTemplateHeight = findViewById(R.id.etTemplateHeight);
        etTemplateDpi = findViewById(R.id.etTemplateDpi);
        canvasView = findViewById(R.id.labelCanvas);
        componentPropertiesPanel = findViewById(R.id.componentPropertiesPanel);

        // 画布组件选择监听
        canvasView.setOnComponentSelectedListener(new LabelCanvasView.OnComponentSelectedListener() {
            @Override
            public void onComponentSelected(LabelComponent component) {
                selectedComponent = component;
                showComponentProperties(component);
            }

            @Override
            public void onComponentMoved(LabelComponent component, int x, int y) {
                component.setX(x);
                component.setY(y);
            }
        });

        // 添加组件按钮
        findViewById(R.id.btnAddText).setOnClickListener(v -> addTextComponent());
        findViewById(R.id.btnAddBarcode).setOnClickListener(v -> addBarcodeComponent());
        findViewById(R.id.btnAddQRCode).setOnClickListener(v -> addQRCodeComponent());
        findViewById(R.id.btnAddLine).setOnClickListener(v -> addLineComponent());
        findViewById(R.id.btnDeleteComponent).setOnClickListener(v -> deleteSelectedComponent());

        // 打印测试按钮
        findViewById(R.id.btnPrintTest).setOnClickListener(v -> printTest());

        // 保存按钮
        findViewById(R.id.btnSaveTemplate).setOnClickListener(v -> saveTemplate());
    }

    private void loadTemplate() {
        String templateId = getIntent().getStringExtra("template_id");

        if (templateId != null) {
            currentTemplate = templateManager.loadTemplate(templateId);
        }

        if (currentTemplate == null) {
            // 创建新模板
            currentTemplate = new LabelTemplate("新模板", 50, 30, 203);
        }

        // 更新 UI
        etTemplateName.setText(currentTemplate.getName());
        etTemplateWidth.setText(String.valueOf(currentTemplate.getWidth()));
        etTemplateHeight.setText(String.valueOf(currentTemplate.getHeight()));
        etTemplateDpi.setText(String.valueOf(currentTemplate.getDpi()));

        updateCanvas();
    }

    private void updateCanvas() {
        int width = parseInt(etTemplateWidth.getText().toString(), 50);
        int height = parseInt(etTemplateHeight.getText().toString(), 30);
        int dpi = parseInt(etTemplateDpi.getText().toString(), 203);

        currentTemplate.setWidth(width);
        currentTemplate.setHeight(height);
        currentTemplate.setDpi(dpi);

        canvasView.setTemplate(currentTemplate);
    }

    private void addTextComponent() {
        TextComponent component = new TextComponent("text_" + UUID.randomUUID().toString().substring(0, 8));
        component.setX(50);
        component.setY(50);
        component.setStaticValue("文本");
        currentTemplate.addComponent(component);
        canvasView.invalidate();
    }

    private void addBarcodeComponent() {
        BarcodeComponent component = new BarcodeComponent("barcode_" + UUID.randomUUID().toString().substring(0, 8));
        component.setX(50);
        component.setY(50);
        component.setStaticValue("1234567890");
        currentTemplate.addComponent(component);
        canvasView.invalidate();
    }

    private void addQRCodeComponent() {
        QRCodeComponent component = new QRCodeComponent("qrcode_" + UUID.randomUUID().toString().substring(0, 8));
        component.setX(50);
        component.setY(50);
        component.setStaticValue("https://example.com");
        currentTemplate.addComponent(component);
        canvasView.invalidate();
    }

    private void addLineComponent() {
        // 简化处理，实际应该创建 LineComponent
        TextComponent component = new TextComponent("line_" + UUID.randomUUID().toString().substring(0, 8));
        component.setX(50);
        component.setY(50);
        component.setStaticValue("__________");
        currentTemplate.addComponent(component);
        canvasView.invalidate();
    }

    private void deleteSelectedComponent() {
        if (selectedComponent != null) {
            currentTemplate.removeComponent(selectedComponent);
            selectedComponent = null;
            componentPropertiesPanel.removeAllViews();
            canvasView.invalidate();
        }
    }

    private void showComponentProperties(LabelComponent component) {
        componentPropertiesPanel.removeAllViews();

        // 通用属性
        addPropertyLabel("组件类型: " + component.getType());
        addPropertyLabel("ID: " + component.getId());

        // X 坐标
        addPropertyInput("X坐标", String.valueOf(component.getX()), value -> {
            component.setX(parseInt(value, 0));
            canvasView.invalidate();
        });

        // Y 坐标
        addPropertyInput("Y坐标", String.valueOf(component.getY()), value -> {
            component.setY(parseInt(value, 0));
            canvasView.invalidate();
        });

        // 根据组件类型显示特定属性
        if (component instanceof TextComponent) {
            showTextComponentProperties((TextComponent) component);
        } else if (component instanceof BarcodeComponent) {
            showBarcodeComponentProperties((BarcodeComponent) component);
        } else if (component instanceof QRCodeComponent) {
            showQRCodeComponentProperties((QRCodeComponent) component);
        }

        // 数据源设置
        addPropertyCheckbox("动态数据", component.isDynamic(), checked -> {
            component.setDynamic(checked);
        });

        if (component.isDynamic()) {
            addPropertyInput("数据源", component.getDataSource(), value -> {
                component.setDataSource(value);
            });
        } else {
            addPropertyInput("静态值", component.getStaticValue(), value -> {
                component.setStaticValue(value);
                canvasView.invalidate();
            });
        }
    }

    private void showTextComponentProperties(TextComponent component) {
        addPropertyInput("字体大小", String.valueOf(component.getFontSize()), value -> {
            component.setFontSize(parseInt(value, 0));
            canvasView.invalidate();
        });

        addPropertyInput("宽度倍数", String.valueOf(component.getFontWidth()), value -> {
            component.setFontWidth(parseInt(value, 1));
            canvasView.invalidate();
        });

        addPropertyInput("高度倍数", String.valueOf(component.getFontHeight()), value -> {
            component.setFontHeight(parseInt(value, 1));
            canvasView.invalidate();
        });

        addPropertyCheckbox("粗体", component.isBold(), checked -> {
            component.setBold(checked);
            canvasView.invalidate();
        });
    }

    private void showBarcodeComponentProperties(BarcodeComponent component) {
        // 条码类型选择
        addPropertySpinner("条码类型", new String[]{"CODE128", "CODE39", "EAN13", "EAN8"}, selected -> {
            component.setBarcodeType(BarcodeComponent.BarcodeType.values()[selected]);
            canvasView.invalidate();
        });

        addPropertyInput("条宽", String.valueOf(component.getBarWidth()), value -> {
            component.setBarWidth(parseInt(value, 2));
            canvasView.invalidate();
        });

        addPropertyInput("高度", String.valueOf(component.getHeight()), value -> {
            component.setHeight(parseInt(value, 60));
            canvasView.invalidate();
        });
    }

    private void showQRCodeComponentProperties(QRCodeComponent component) {
        addPropertyInput("单元格宽度", String.valueOf(component.getCellWidth()), value -> {
            component.setCellWidth(parseInt(value, 2));
            canvasView.invalidate();
        });
    }

    private void addPropertyLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(0, 8, 0, 8);
        componentPropertiesPanel.addView(tv);
    }

    private void addPropertyInput(String label, String value, PropertyChangeListener listener) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label + ": ");
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        EditText etValue = new EditText(this);
        etValue.setText(value);
        etValue.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
        etValue.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                listener.onChange(etValue.getText().toString());
            }
        });

        layout.addView(tvLabel);
        layout.addView(etValue);
        componentPropertiesPanel.addView(layout);
    }

    private void addPropertyCheckbox(String label, boolean checked, PropertyCheckListener listener) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label + ": ");

        Button btnCheck = new Button(this);
        btnCheck.setText(checked ? "是" : "否");
        btnCheck.setOnClickListener(v -> {
            boolean newChecked = !checked;
            listener.onCheck(newChecked);
            btnCheck.setText(newChecked ? "是" : "否");
            showComponentProperties(selectedComponent);
        });

        layout.addView(tvLabel);
        layout.addView(btnCheck);
        componentPropertiesPanel.addView(layout);
    }

    private void addPropertySpinner(String label, String[] items, PropertySelectListener listener) {
        TextView tvLabel = new TextView(this);
        tvLabel.setText(label + ":");
        componentPropertiesPanel.addView(tvLabel);

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listener.onSelect(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        componentPropertiesPanel.addView(spinner);
    }

    private void saveTemplate() {
        currentTemplate.setName(etTemplateName.getText().toString());
        updateCanvas();

        if (templateManager.saveTemplate(currentTemplate)) {
            Toast.makeText(this, "模板保存成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "模板保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void printTest() {
        // 显示蓝牙设备选择对话框
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_CONNECT_DEVICE);
            return;
        }

        showBluetoothDeviceDialog();
    }

    private void showBluetoothDeviceDialog() {
        BluetoothPrintersConnections printers = new BluetoothPrintersConnections();
        BluetoothDevice[] devices = printers.getList();

        if (devices == null || devices.length == 0) {
            Toast.makeText(this, "未找到蓝牙打印机", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] deviceNames = new String[devices.length];
        for (int i = 0; i < devices.length; i++) {
            deviceNames[i] = devices[i].getName() + "\n" + devices[i].getAddress();
        }

        new AlertDialog.Builder(this)
            .setTitle("选择打印机")
            .setItems(deviceNames, (dialog, which) -> {
                printWithDevice(devices[which]);
            })
            .show();
    }

    private void printWithDevice(BluetoothDevice device) {
        // 使用测试数据打印
        MapDataProvider dataProvider = new MapDataProvider();
        dataProvider.put("product_name", "测试商品");
        dataProvider.put("barcode", "1234567890123");
        dataProvider.put("price", "99.00");

        try {
            currentTemplate.print(new com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection(device), dataProvider);
            Toast.makeText(this, "打印成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "打印失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_template_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveTemplate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 属性监听器接口
    private interface PropertyChangeListener {
        void onChange(String value);
    }

    private interface PropertyCheckListener {
        void onCheck(boolean checked);
    }

    private interface PropertySelectListener {
        void onSelect(int position);
    }
}
