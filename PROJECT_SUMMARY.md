# CPCL Printer Android - Project Summary

## Overview

This project extends the popular [ESCPOS-ThermalPrinter-Android](https://github.com/DantSu/ESCPOS-ThermalPrinter-Android) library with **CPCL (Comtec Printer Command Language)** support, enabling Android apps to print labels on Zebra, TSC, Honeywell, and other CPCL-compatible label printers.

## What is CPCL?

CPCL is a printer command language used by many label printer manufacturers. Unlike ESC/POS which is designed for receipt printers with continuous paper, CPCL is optimized for:

- **Label printing** - Precise positioning on die-cut labels
- **Barcode labels** - Extensive barcode format support
- **QR Codes** - Native QR code generation
- **Graphics** - Bitmap and line drawing

## Files Added

### Core Library Files

1. **`CpclPrinterCommands.java`** (600+ lines)
   - Low-level CPCL command generation
   - Direct printer communication
   - Supports all CPCL commands:
     - Text with fonts 0-7
     - Barcodes (Code 128, EAN-13, UPC-A, Code 39, etc.)
     - QR Codes with error correction
     - Graphics and bitmaps
     - Lines and boxes

2. **`CpclPrinter.java`** (400+ lines)
   - High-level API for easy label printing
   - Fluent interface (method chaining)
   - Predefined label sizes
   - Automatic unit conversion (mm to dots)
   - Word wrapping and text centering

### Example and Documentation

3. **`CpclPrinterExampleActivity.java`**
   - Complete working examples
   - Product label printing
   - Barcode label printing
   - QR Code label printing
   - Shelf label example

4. **`README_CPCL.md`**
   - Complete API documentation
   - Usage examples
   - Printer compatibility list

5. **`SETUP_GUIDE.md`**
   - Installation instructions
   - Quick start guide
   - Troubleshooting tips

## Key Features

### 1. Easy Label Printing
```java
CpclPrinter printer = new CpclPrinter(connection, 50f, 30f);
printer
    .startLabel()
    .printText(20, 20, "Hello World")
    .printBarcode128(20, 60, 50, "123456789012", true)
    .endLabel();
```

### 2. Multiple Connection Types
- Bluetooth (using existing BluetoothPrintersConnections)
- TCP/IP
- USB

### 3. Rich Graphics Support
- Text with 8 different fonts
- Barcodes: Code 128, EAN-13, UPC-A, Code 39, Code 93, etc.
- QR Codes with 4 error correction levels
- Lines and boxes
- Bitmap images

### 4. Predefined Label Sizes
- 30x20mm
- 40x30mm
- 50x30mm
- 60x40mm
- 70x40mm
- 100x50mm
- 100x80mm

### 5. Flexible Positioning
- Absolute X,Y positioning in dots
- Millimeter to dot conversion
- Centered text support
- Word wrapping

## Supported Printers

### Tested Printers
- Zebra ZD220, ZD230, ZD420, ZD620
- TSC TDP-225, TDP-247, TE200, TE300
- Honeywell PC42t, PC42d, PD43

### Compatible Brands
Any printer that supports CPCL commands:
- Zebra (most models)
- TSC (most models)
- Honeywell/Intermec
- Datamax-O'Neil
- Many generic Chinese label printers

## Usage Scenarios

### 1. Retail - Price Labels
```java
printer
    .printText(20, 20, "Product Name")
    .printText(6, 2, 150, 50, "$29.99")
    .printBarcodeEAN13(50, 100, 60, "7891234567890")
    .endLabel();
```

### 2. Warehouse - Inventory Labels
```java
printer
    .printQRCode(200, 20, 6, "INV-2026-001234")
    .printText(20, 20, "SKU: ABC-12345")
    .printText(20, 50, "Qty: 100")
    .printBarcode128(20, 100, 50, "INV001234", true)
    .endLabel();
```

### 3. Logistics - Shipping Labels
```java
printer
    .printText(4, 1, 20, 20, "SHIP TO:")
    .printTextWrapped(20, 60, 350, "John Doe, 123 Main St, City, Country")
    .printQRCode(400, 50, 8, "TRACK123456789")
    .printBarcode128(50, 200, 70, "TRACK123456789", true)
    .endLabel();
```

## Technical Details

### CPCL Command Structure
```
! speed density width height quantity  <- Header
TEXT font size x y data                 <- Text
BARCODE type w r h x y data             <- Barcode
BARCODE QR x y M2 U6                    <- QR Code start
MA1                                     <- Error correction
MD<data>                                <- QR data
ENDQR                                   <- QR Code end
FORM                                    <- Form feed
PRINT                                   <- Execute print
```

### Coordinate System
- Origin (0,0) at top-left corner
- X increases to the right
- Y increases downward
- Units in dots (at printer's DPI)

### Font Sizes
- Font 0-3: Small to medium (8-12 dots high)
- Font 4-7: Large fonts (12-16 dots high)
- Size multiplier: 0-9 (multiplies base size)

## How to Submit to GitHub

### Option 1: Using the Provided Scripts

**Windows:**
```cmd
cd CPCL-Printer-Android
init-github-repo.bat
```

**macOS/Linux:**
```bash
cd CPCL-Printer-Android
chmod +x init-github-repo.sh
./init-github-repo.sh
```

### Option 2: Manual Steps

1. Install Git from https://git-scm.com/download

2. Initialize repository:
```bash
cd CPCL-Printer-Android
git init
git add .
git commit -m "Initial commit: Add CPCL support"
```

3. Create repository on GitHub

4. Push code:
```bash
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git
git branch -M main
git push -u origin main
```

## Comparison with Original Library

| Feature | Original (ESCPOS) | This Fork (+CPCL) |
|---------|-------------------|-------------------|
| Receipt printing | ✅ | ✅ |
| Label printing | ❌ | ✅ |
| Barcodes | Basic | Extensive |
| QR Codes | ESC/POS standard | Native CPCL |
| Positioning | Relative | Absolute |
| Graphics | Limited | Rich |

## Future Enhancements

Possible improvements:
1. TSPL (TSC Printer Language) support
2. ZPL (Zebra Programming Language) support
3. Label template system
4. Preview functionality
5. More barcode types
6. Image dithering options

## License

MIT License - same as the original ESCPOS-ThermalPrinter-Android library.

## Credits

- Original library: [DantSu/ESCPOS-ThermalPrinter-Android](https://github.com/DantSu/ESCPOS-ThermalPrinter-Android)
- CPCL documentation: Zebra Technologies, TSC Printers

## Contact

For issues, suggestions, or contributions, please use GitHub Issues and Pull Requests.
