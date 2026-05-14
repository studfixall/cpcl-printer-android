package com.dantsu.escposprinter.example;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dantsu.escposprinter.CpclPrinter;
import com.dantsu.escposprinter.R;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;

/**
 * Example Activity demonstrating CPCL label printer usage
 */
public class CpclPrinterExampleActivity extends AppCompatActivity {

    private CpclPrinter cpclPrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpcl_example);

        Button btnPrintProductLabel = findViewById(R.id.btn_print_product_label);
        Button btnPrintBarcodeLabel = findViewById(R.id.btn_print_barcode_label);
        Button btnPrintQRCodeLabel = findViewById(R.id.btn_print_qrcode_label);

        btnPrintProductLabel.setOnClickListener(v -> printProductLabel());
        btnPrintBarcodeLabel.setOnClickListener(v -> printBarcodeLabel());
        btnPrintQRCodeLabel.setOnClickListener(v -> printQRCodeLabel());
    }

    /**
     * Example: Print a product label
     */
    private void printProductLabel() {
        try {
            // Connect to first paired Bluetooth printer
            BluetoothPrintersConnections connections = new BluetoothPrintersConnections();
            BluetoothConnection connection = connections.selectFirstPaired();

            if (connection == null) {
                Toast.makeText(this, "No paired printer found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create CPCL printer instance (50mm x 30mm label at 203 DPI)
            cpclPrinter = new CpclPrinter(connection, 50f, 30f);

            // Print label
            cpclPrinter
                .startLabel()
                // Title
                .printText(4, 1, 50, 20, "PRODUCT LABEL")
                // Horizontal line
                .drawHorizontalLine(10, 50, 280, 2)
                // Product info
                .printText(10, 70, "Name: Sample Product")
                .printText(10, 100, "SKU: PRD-001234")
                .printText(10, 130, "Price: $29.99")
                // Box around price
                .drawBox(180, 120, 100, 30, 1)
                // Print date
                .printText(10, 170, "Date: 2026-05-14")
                .endLabel();

            cpclPrinter.disconnect();
            Toast.makeText(this, "Label printed successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Print failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Example: Print a barcode label
     */
    private void printBarcodeLabel() {
        try {
            BluetoothPrintersConnections connections = new BluetoothPrintersConnections();
            BluetoothConnection connection = connections.selectFirstPaired();

            if (connection == null) {
                Toast.makeText(this, "No paired printer found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create CPCL printer (60mm x 40mm label)
            cpclPrinter = new CpclPrinter(connection, 60f, 40f);

            String barcodeData = "123456789012";

            cpclPrinter
                .startLabel()
                // Title
                .printTextCentered(20, "BARCODE LABEL")
                // Barcode
                .printBarcode128(30, 60, 80, barcodeData, true)
                // Additional info
                .printText(30, 160, "Scan for product info")
                .endLabel();

            cpclPrinter.disconnect();
            Toast.makeText(this, "Barcode label printed", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Print failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Example: Print a QR Code label
     */
    private void printQRCodeLabel() {
        try {
            BluetoothPrintersConnections connections = new BluetoothPrintersConnections();
            BluetoothConnection connection = connections.selectFirstPaired();

            if (connection == null) {
                Toast.makeText(this, "No paired printer found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create CPCL printer (40mm x 30mm label)
            cpclPrinter = new CpclPrinter(connection, 40f, 30f);

            String qrData = "https://example.com/product/12345";

            cpclPrinter
                .startLabel()
                // QR Code
                .printQRCode(80, 20, 6, qrData)
                // Text below QR
                .printTextCentered(180, "Scan Me!")
                .endLabel();

            cpclPrinter.disconnect();
            Toast.makeText(this, "QR Code label printed", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Print failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Example: Print a shelf label with multiple elements
     */
    private void printShelfLabel() {
        try {
            BluetoothPrintersConnections connections = new BluetoothPrintersConnections();
            BluetoothConnection connection = connections.selectFirstPaired();

            if (connection == null) {
                Toast.makeText(this, "No paired printer found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create CPCL printer (100mm x 50mm shelf label)
            cpclPrinter = new CpclPrinter(connection, 100f, 50f);

            String productName = "Premium Coffee Beans 500g";
            String sku = "COF-500-BRA";
            String price = "$15.99";
            String barcode = "7891234567890";

            int centerX = cpclPrinter.getLabelWidth() / 2;

            cpclPrinter
                .startLabel()
                // Product name (centered, large)
                .printText(4, 1, centerX - 100, 20, productName)
                // SKU
                .printText(20, 60, "SKU: " + sku)
                // Price (large, in box)
                .printText(6, 2, 200, 90, price)
                .drawBox(190, 80, 150, 50, 2)
                // Barcode at bottom
                .printBarcodeEAN13(80, 160, 60, barcode)
                // Separator line
                .drawHorizontalLine(10, 140, 580, 1)
                .endLabel();

            cpclPrinter.disconnect();
            Toast.makeText(this, "Shelf label printed", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Print failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cpclPrinter != null) {
            cpclPrinter.disconnect();
        }
    }
}
