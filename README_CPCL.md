# CPCL Label Printer Support

This fork adds **CPCL (Comtec Printer Command Language)** support to the ESC/POS Thermal Printer library, enabling printing on **Zebra, TSC, Honeywell** and other CPCL-compatible label printers.

## What's New

### CPCL Classes

- **`CpclPrinter`** - High-level API for CPCL label printing
- **`CpclPrinterCommands`** - Low-level CPCL command generation

### Features

- ✅ Text printing with multiple fonts and sizes
- ✅ Barcode printing (Code 128, EAN-13, UPC-A, Code 39, etc.)
- ✅ QR Code printing with configurable error correction
- ✅ Graphics and bitmap support
- ✅ Lines and boxes
- ✅ Word wrapping for text
- ✅ Predefined label sizes
- ✅ Bluetooth, TCP, and USB connections (inherited from base library)

## Quick Start

### 1. Create a CPCL Printer Instance

```java
// Connect via Bluetooth
BluetoothConnection connection = BluetoothPrintersConnections.selectFirstPaired();

// Create printer (50mm x 30mm label at 203 DPI)
CpclPrinter printer = new CpclPrinter(connection, 50f, 30f);
```

### 2. Print a Label

```java
printer
    .startLabel()
    .printText(20, 30, "Product Name")
    .printBarcode128(20, 60, 50, "123456789012", true)
    .printQRCode(200, 50, 5, "https://example.com")
    .endLabel();

printer.disconnect();
```

## API Reference

### CpclPrinter

#### Constructors

```java
// Custom dimensions
CpclPrinter(DeviceConnection connection, int dpi, float widthMM, float heightMM)

// Default 203 DPI
CpclPrinter(DeviceConnection connection, float widthMM, float heightMM)

// Predefined sizes
CpclPrinter(DeviceConnection connection, int dpi, int size)
```

#### Predefined Sizes

- `SIZE_30x20_MM`
- `SIZE_40x30_MM`
- `SIZE_50x30_MM`
- `SIZE_60x40_MM`
- `SIZE_70x40_MM`
- `SIZE_100x50_MM`
- `SIZE_100x80_MM`

#### Text Methods

```java
printText(int x, int y, String text)
printText(int font, int size, int x, int y, String text)
printTextWrapped(int x, int y, int maxWidth, String text)
printTextCentered(int y, String text)
```

#### Barcode Methods

```java
printBarcode128(int x, int y, int height, String data)
printBarcode128(int x, int y, int height, String data, boolean showText)
printBarcodeEAN13(int x, int y, int height, String data)
printBarcodeUPCA(int x, int y, int height, String data)
```

#### QR Code Methods

```java
printQRCode(int x, int y, int size, String data)
printQRCode(int x, int y, int size, int ecLevel, String data)
```

#### Graphics Methods

```java
drawLine(int x1, int y1, int x2, int y2, int thickness)
drawHorizontalLine(int x, int y, int width, int thickness)
drawVerticalLine(int x, int y, int height, int thickness)
drawBox(int x, int y, int width, int height, int thickness)
```

### CpclPrinterCommands (Low-level)

For advanced users who need direct CPCL command access:

```java
CpclPrinterCommands commands = new CpclPrinterCommands(connection);
commands.connect();

// Set label size
commands.setLabelSize(576, 406);  // width, height in dots

// Start label
commands.startLabel(1);

// Print text
commands.printText(4, 0, 100, 100, "Hello World");

// Print barcode
commands.printBarcode(
    CpclPrinterCommands.BARCODE_128,
    2, 0, 50, 100, 150, "1234567890"
);

// Print QR Code
commands.printQRCode(200, 200, 2, 6, CpclPrinterCommands.QR_EC_M, "https://example.com");

// End and print
commands.endLabel();
commands.disconnect();
```

## Complete Examples

### Product Label

```java
CpclPrinter printer = new CpclPrinter(connection, 60f, 40f);

printer
    .startLabel()
    .printText(4, 1, 50, 20, "PRODUCT LABEL")
    .drawHorizontalLine(10, 50, 340, 2)
    .printText(10, 70, "Name: Sample Product")
    .printText(10, 100, "SKU: PRD-001234")
    .printText(10, 130, "Price: $29.99")
    .drawBox(220, 120, 120, 35, 1)
    .printBarcode128(30, 180, 60, "123456789012", true)
    .endLabel();

printer.disconnect();
```

### Shelf Label

```java
CpclPrinter printer = new CpclPrinter(connection, 100f, 50f);

String productName = "Premium Coffee Beans 500g";
String sku = "COF-500-BRA";
String price = "$15.99";
String barcode = "7891234567890";

int centerX = printer.getLabelWidth() / 2;

printer
    .startLabel()
    .printText(4, 1, centerX - 120, 20, productName)
    .printText(20, 60, "SKU: " + sku)
    .printText(6, 2, 200, 90, price)
    .drawBox(190, 80, 150, 50, 2)
    .printBarcodeEAN13(80, 160, 60, barcode)
    .drawHorizontalLine(10, 140, 580, 1)
    .endLabel();

printer.disconnect();
```

## Supported Printers

CPCL is supported by many label printer brands:

- **Zebra** - ZD220, ZD230, ZD420, ZD620, ZT230, ZT410, etc.
- **TSC** - TDP-225, TDP-247, TE200, TE300, etc.
- **Honeywell** - PC42t, PC42d, PD43, etc.
- **Other** - Many generic Chinese label printers

## Differences from ESC/POS

| Feature | ESC/POS | CPCL |
|---------|---------|------|
| Use case | Receipts, tickets | Labels, tags |
| Paper type | Continuous roll | Die-cut labels |
| Print command | Feed + cut | Single label print |
| Positioning | Relative | Absolute (X, Y) |
| Barcodes | Limited types | Extensive types |
| QR Codes | ESC/POS standard | Native support |

## Original Library

This fork is based on [ESCPOS-ThermalPrinter-Android](https://github.com/DantSu/ESCPOS-ThermalPrinter-Android) by DantSu.

All original ESC/POS functionality remains available and unchanged.

## License

MIT License - same as the original library.
