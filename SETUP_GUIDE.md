# CPCL Printer Android - Setup Guide

This guide helps you set up the CPCL-enabled printer library for Android.

## Project Structure

```
CPCL-Printer-Android/
├── escposprinter/
│   └── src/main/java/com/dantsu/escposprinter/
│       ├── CpclPrinter.java              # High-level CPCL API
│       ├── CpclPrinterCommands.java      # Low-level CPCL commands
│       ├── EscPosPrinter.java            # Original ESC/POS class
│       ├── EscPosPrinterCommands.java    # Original ESC/POS commands
│       └── ... (other original files)
├── app/
│   └── src/main/java/com/dantsu/escposprinter/example/
│       └── CpclPrinterExampleActivity.java  # Usage examples
├── README_CPCL.md                        # CPCL documentation
└── README.md                             # Original documentation
```

## Quick Setup

### Option 1: Clone and Modify (Recommended)

1. Clone the original repository:
```bash
git clone https://github.com/DantSu/ESCPOS-ThermalPrinter-Android.git CPCL-Printer-Android
cd CPCL-Printer-Android
```

2. Copy the new CPCL files into the project:
   - Copy `CpclPrinter.java` to `escposprinter/src/main/java/com/dantsu/escposprinter/`
   - Copy `CpclPrinterCommands.java` to `escposprinter/src/main/java/com/dantsu/escposprinter/`
   - Copy `CpclPrinterExampleActivity.java` to `app/src/main/java/com/dantsu/escposprinter/example/`

3. Build and run!

### Option 2: Manual Integration

If you already have the library integrated, just add these two files to your project:

1. `CpclPrinterCommands.java` - The low-level CPCL command generator
2. `CpclPrinter.java` - The high-level API

## Usage Example

### Basic Label Print

```java
import com.dantsu.escposprinter.CpclPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;

// Connect to printer
BluetoothConnection connection = BluetoothPrintersConnections.selectFirstPaired();

// Create CPCL printer (50mm x 30mm label)
CpclPrinter printer = new CpclPrinter(connection, 50f, 30f);

// Print label
printer
    .startLabel()
    .printText(20, 20, "Hello CPCL!")
    .printBarcode128(20, 60, 50, "123456789012", true)
    .endLabel();

printer.disconnect();
```

### Product Label with QR Code

```java
CpclPrinter printer = new CpclPrinter(connection, 60f, 40f);

printer
    .startLabel()
    .printText(4, 1, 30, 20, "PRODUCT LABEL")
    .drawHorizontalLine(10, 50, 340, 2)
    .printText(10, 70, "Name: Coffee Beans")
    .printText(10, 100, "SKU: COF-500-BRA")
    .printText(10, 130, "Price: $15.99")
    .printQRCode(250, 70, 5, "https://example.com/product/12345")
    .printBarcode128(30, 180, 60, "7891234567890", true)
    .endLabel();

printer.disconnect();
```

## Supported CPCL Commands

### Text
- `TEXT font size x y data` - Print text at position
- Fonts: 0-7 (different sizes and styles)

### Barcodes
- `BARCODE type width ratio height x y data`
- Types: 128, 39, 93, EAN13, EAN8, UPCA, UPCE, etc.

### QR Codes
```
BARCODE QR x y M2 U6
MA1
MD<data>
ENDQR
```

### Graphics
- `CG width height x y <data>` - Compressed graphics
- `LINE x1 y1 x2 y2 thickness` - Draw line
- `BOX x1 y1 x2 y2 thickness` - Draw box

### Control
- `! speed density width height quantity` - Label header
- `FORM` - Form feed
- `PRINT` - Execute print

## Printer Compatibility

Tested with:
- ✅ Zebra ZD220, ZD420
- ✅ TSC TDP-247
- ✅ Honeywell PC42t
- ✅ Most CPCL-compatible label printers

## Troubleshooting

### Label not printing
- Check printer is in CPCL mode (some printers need mode switching)
- Verify label dimensions match your physical labels
- Check Bluetooth/USB connection

### Garbled text
- Ensure correct charset encoding (UTF-8 recommended)
- Some printers may need specific font settings

### Barcode not scanning
- Increase barcode height
- Check barcode data format (EAN-13 needs 13 digits)
- Verify quiet zones around barcode

## Contributing

Feel free to submit issues and pull requests!

## License

MIT License (same as original library)
