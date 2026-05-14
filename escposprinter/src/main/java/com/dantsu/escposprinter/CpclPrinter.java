package com.dantsu.escposprinter;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;

/**
 * CPCL Label Printer - High-level API for CPCL-compatible label printers
 * Supports Zebra, TSC, Honeywell and other CPCL-compatible printers
 */
public class CpclPrinter {

    private CpclPrinterCommands printer;
    private int labelWidth;
    private int labelHeight;
    private int dpi;
    
    /**
     * Barcode types for CPCL printing
     */
    public enum BarcodeType {
        CODE128(CpclPrinterCommands.BARCODE_128),
        CODE39(CpclPrinterCommands.BARCODE_39),
        CODE93(CpclPrinterCommands.BARCODE_93),
        CODABAR(CpclPrinterCommands.BARCODE_CODABAR),
        EAN13(CpclPrinterCommands.BARCODE_EAN13),
        EAN8(CpclPrinterCommands.BARCODE_EAN8),
        UPCA(CpclPrinterCommands.BARCODE_UPCA),
        UPCE(CpclPrinterCommands.BARCODE_UPCE),
        I2OF5(CpclPrinterCommands.BARCODE_I2OF5),
        QR_CODE(100); // Special value for QR
        
        private final int value;
        BarcodeType(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    // Common label sizes (in mm)
    public static final int SIZE_30x20_MM = 1;   // 30mm x 20mm
    public static final int SIZE_40x30_MM = 2;   // 40mm x 30mm
    public static final int SIZE_50x30_MM = 3;   // 50mm x 30mm
    public static final int SIZE_60x40_MM = 4;   // 60mm x 40mm
    public static final int SIZE_70x40_MM = 5;   // 70mm x 40mm
    public static final int SIZE_100x50_MM = 6;  // 100mm x 50mm
    public static final int SIZE_100x80_MM = 7;  // 100mm x 80mm

    // Common DPI values
    public static final int DPI_203 = 203;
    public static final int DPI_300 = 300;

    /**
     * Create new instance of CpclPrinter.
     *
     * @param printerConnection Instance of class which implement DeviceConnection
     * @param dpi               Printer DPI (203 or 300)
     * @param labelWidthMM      Label width in millimeters
     * @param labelHeightMM     Label height in millimeters
     */
    public CpclPrinter(DeviceConnection printerConnection, int dpi, float labelWidthMM, float labelHeightMM) throws EscPosConnectionException {
        this.dpi = dpi;
        this.labelWidth = mmToDots(labelWidthMM, dpi);
        this.labelHeight = mmToDots(labelHeightMM, dpi);

        if (printerConnection != null) {
            this.printer = new CpclPrinterCommands(printerConnection);
            this.printer.connect();
            this.printer.setLabelSize(this.labelWidth, this.labelHeight);
        }
    }

    /**
     * Create new instance of CpclPrinter with predefined size.
     *
     * @param printerConnection Instance of class which implement DeviceConnection
     * @param dpi               Printer DPI
     * @param size              Predefined size constant
     */
    public CpclPrinter(DeviceConnection printerConnection, int dpi, int size) throws EscPosConnectionException {
        this.dpi = dpi;
        setLabelSize(size);

        if (printerConnection != null) {
            this.printer = new CpclPrinterCommands(printerConnection);
            this.printer.connect();
            this.printer.setLabelSize(this.labelWidth, this.labelHeight);
        }
    }

    /**
     * Create new instance of CpclPrinter with default 203 DPI.
     *
     * @param printerConnection Instance of class which implement DeviceConnection
     * @param labelWidthMM      Label width in millimeters
     * @param labelHeightMM     Label height in millimeters
     */
    public CpclPrinter(DeviceConnection printerConnection, float labelWidthMM, float labelHeightMM) throws EscPosConnectionException {
        this(printerConnection, DPI_203, labelWidthMM, labelHeightMM);
    }
    
    /**
     * Create new instance of CpclPrinter with simple connection.
     * Uses default 50x30mm label size at 203 DPI.
     *
     * @param printerConnection Instance of class which implement DeviceConnection
     */
    public CpclPrinter(DeviceConnection printerConnection) throws EscPosConnectionException {
        this(printerConnection, DPI_203, 50f, 30f);
    }
    
    /**
     * Set label size with width, height in mm and DPI.
     * This is used by LabelTemplate.
     *
     * @param widthMM   Label width in millimeters
     * @param heightMM  Label height in millimeters
     * @param dpi       Printer DPI
     */
    public void setLabelSize(int widthMM, int heightMM, int dpi) {
        this.dpi = dpi;
        this.labelWidth = mmToDots(widthMM, dpi);
        this.labelHeight = mmToDots(heightMM, dpi);
        if (this.printer != null) {
            this.printer.setLabelSize(this.labelWidth, this.labelHeight);
        }
    }
    
    /**
     * Start a new label for printing.
     * This is used by LabelTemplate.
     */
    public void newLabel() throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.startLabel(1);
        }
    }
    
    /**
     * Add text at specified position.
     * This is used by LabelTemplate.
     *
     * @param x    X position in dots
     * @param y    Y position in dots
     * @param text Text to print
     */
    public void addText(int x, int y, String text) throws EscPosEncodingException {
        printText(x, y, text);
    }
    
    /**
     * Set text magnification.
     * This is used by LabelTemplate.
     *
     * @param width  Width multiplier (1-8)
     * @param height Height multiplier (1-8)
     */
    public void setMagnify(int width, int height) {
        // CPCL uses font size for magnification
        // This is a simplified implementation
    }
    
    /**
     * Add barcode at specified position.
     * This is used by LabelTemplate.
     *
     * @param x         X position
     * @param y         Y position
     * @param data      Barcode data
     * @param type      Barcode type
     * @param width     Bar width
     * @param height    Barcode height
     */
    public void addBarcode(int x, int y, String data, BarcodeType type, int width, int height) throws EscPosEncodingException {
        if (type == BarcodeType.QR_CODE) {
            printQRCode(x, y, width, data);
        } else {
            printBarcode(type.getValue(), x, y, height, data);
        }
    }
    
    /**
     * Add QR Code at specified position.
     * This is used by LabelTemplate.
     *
     * @param x      X position
     * @param y      Y position
     * @param data   QR Code data
     * @param size   QR Code size
     */
    public void addQRCode(int x, int y, String data, int size) throws EscPosEncodingException {
        printQRCode(x, y, size, data);
    }
    
    /**
     * Print the label.
     * This is used by LabelTemplate.
     */
    public void print() throws EscPosEncodingException {
        endLabel();
    }
    
    /**
     * Print barcode with type value
     */
    private CpclPrinter printBarcode(int type, int x, int y, int height, String data) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.printBarcode(type, 2, 0, height, x, y, data);
        }
        return this;
    }

    /**
     * Set label size from predefined constants
     */
    private void setLabelSize(int size) {
        switch (size) {
            case SIZE_30x20_MM:
                labelWidth = mmToDots(30, dpi);
                labelHeight = mmToDots(20, dpi);
                break;
            case SIZE_40x30_MM:
                labelWidth = mmToDots(40, dpi);
                labelHeight = mmToDots(30, dpi);
                break;
            case SIZE_50x30_MM:
                labelWidth = mmToDots(50, dpi);
                labelHeight = mmToDots(30, dpi);
                break;
            case SIZE_60x40_MM:
                labelWidth = mmToDots(60, dpi);
                labelHeight = mmToDots(40, dpi);
                break;
            case SIZE_70x40_MM:
                labelWidth = mmToDots(70, dpi);
                labelHeight = mmToDots(40, dpi);
                break;
            case SIZE_100x50_MM:
                labelWidth = mmToDots(100, dpi);
                labelHeight = mmToDots(50, dpi);
                break;
            case SIZE_100x80_MM:
                labelWidth = mmToDots(100, dpi);
                labelHeight = mmToDots(80, dpi);
                break;
            default:
                // Default to 50x30mm
                labelWidth = mmToDots(50, dpi);
                labelHeight = mmToDots(30, dpi);
        }
    }

    /**
     * Convert millimeters to dots
     */
    private int mmToDots(float mm, int dpi) {
        return Math.round(mm * dpi / 25.4f);
    }

    /**
     * Convert millimeters to dots for current DPI
     */
    public int mmToDots(float mm) {
        return mmToDots(mm, this.dpi);
    }

    /**
     * Close the connection with the printer.
     */
    public void disconnect() {
        if (this.printer != null) {
            this.printer.disconnect();
            this.printer = null;
        }
    }

    /**
     * Start a new label
     *
     * @param quantity Number of copies
     * @return Fluent interface
     */
    public CpclPrinter startLabel(int quantity) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.startLabel(quantity);
        }
        return this;
    }

    /**
     * Start a new label (single copy)
     *
     * @return Fluent interface
     */
    public CpclPrinter startLabel() throws EscPosEncodingException {
        return startLabel(1);
    }

    /**
     * End the label and print
     *
     * @return Fluent interface
     */
    public CpclPrinter endLabel() throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.endLabel();
        }
        return this;
    }

    // ==================== Text Methods ====================

    /**
     * Print text at specified position
     *
     * @param x    X position in dots
     * @param y    Y position in dots
     * @param text Text to print
     * @return Fluent interface
     */
    public CpclPrinter printText(int x, int y, String text) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.printText(x, y, text);
        }
        return this;
    }

    /**
     * Print text with custom font
     *
     * @param font Font number (0-7)
     * @param size Font size multiplier
     * @param x    X position
     * @param y    Y position
     * @param text Text to print
     * @return Fluent interface
     */
    public CpclPrinter printText(int font, int size, int x, int y, String text) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.printText(font, size, x, y, text);
        }
        return this;
    }

    /**
     * Print text with word wrapping
     *
     * @param x        X position
     * @param y        Y position
     * @param maxWidth Maximum width for wrapping
     * @param text     Text to print
     * @return Fluent interface
     */
    public CpclPrinter printTextWrapped(int x, int y, int maxWidth, String text) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.printTextWrapped(4, 0, x, y, maxWidth, text);
        }
        return this;
    }

    /**
     * Print centered text
     *
     * @param y    Y position
     * @param text Text to print
     * @return Fluent interface
     */
    public CpclPrinter printTextCentered(int y, String text) throws EscPosEncodingException {
        int textWidth = estimateTextWidth(text);
        int x = (labelWidth - textWidth) / 2;
        return printText(Math.max(0, x), y, text);
    }

    // ==================== Barcode Methods ====================

    /**
     * Print a Code 128 barcode
     *
     * @param x      X position
     * @param y      Y position
     * @param height Barcode height in dots
     * @param data   Barcode data
     * @return Fluent interface
     */
    public CpclPrinter printBarcode128(int x, int y, int height, String data) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.printBarcode(
                CpclPrinterCommands.BARCODE_128,
                2,  // width
                0,  // ratio
                height,
                x, y,
                data
            );
        }
        return this;
    }

    /**
     * Print a Code 128 barcode with text
     *
     * @param x         X position
     * @param y         Y position
     * @param height    Barcode height
     * @param data      Barcode data
     * @param showText  Show text below barcode
     * @return Fluent interface
     */
    public CpclPrinter printBarcode128(int x, int y, int height, String data, boolean showText) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.printBarcode(
                CpclPrinterCommands.BARCODE_128,
                2, 0, height, x, y, data, showText
            );
        }
        return this;
    }

    /**
     * Print an EAN-13 barcode
     *
     * @param x      X position
     * @param y      Y position
     * @param height Barcode height
     * @param data   Barcode data (13 digits)
     * @return Fluent interface
     */
    public CpclPrinter printBarcodeEAN13(int x, int y, int height, String data) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.printBarcode(
                CpclPrinterCommands.BARCODE_EAN13,
                2, 0, height, x, y, data
            );
        }
        return this;
    }

    /**
     * Print a UPC-A barcode
     *
     * @param x      X position
     * @param y      Y position
     * @param height Barcode height
     * @param data   Barcode data (12 digits)
     * @return Fluent interface
     */
    public CpclPrinter printBarcodeUPCA(int x, int y, int height, String data) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.printBarcode(
                CpclPrinterCommands.BARCODE_UPCA,
                2, 0, height, x, y, data
            );
        }
        return this;
    }

    // ==================== QR Code Methods ====================

    /**
     * Print a QR Code
     *
     * @param x    X position
     * @param y    Y position
     * @param size Size (1-32)
     * @param data QR Code data
     * @return Fluent interface
     */
    public CpclPrinter printQRCode(int x, int y, int size, String data) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.printQRCode(x, y, size, data);
        }
        return this;
    }

    /**
     * Print a QR Code with custom error correction
     *
     * @param x       X position
     * @param y       Y position
     * @param size    Size
     * @param ecLevel Error correction level
     * @param data    QR Code data
     * @return Fluent interface
     */
    public CpclPrinter printQRCode(int x, int y, int size, int ecLevel, String data) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.printQRCode(x, y, 2, size, ecLevel, data);
        }
        return this;
    }

    // ==================== Graphics Methods ====================

    /**
     * Draw a line
     *
     * @param x1        Start X
     * @param y1        Start Y
     * @param x2        End X
     * @param y2        End Y
     * @param thickness Line thickness
     * @return Fluent interface
     */
    public CpclPrinter drawLine(int x1, int y1, int x2, int y2, int thickness) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.drawLine(x1, y1, x2, y2, thickness);
        }
        return this;
    }

    /**
     * Draw a horizontal line
     *
     * @param x         Start X
     * @param y         Y position
     * @param width     Line width
     * @param thickness Line thickness
     * @return Fluent interface
     */
    public CpclPrinter drawHorizontalLine(int x, int y, int width, int thickness) throws EscPosEncodingException {
        return drawLine(x, y, x + width, y, thickness);
    }

    /**
     * Draw a vertical line
     *
     * @param x         X position
     * @param y         Start Y
     * @param height    Line height
     * @param thickness Line thickness
     * @return Fluent interface
     */
    public CpclPrinter drawVerticalLine(int x, int y, int height, int thickness) throws EscPosEncodingException {
        return drawLine(x, y, x, y + height, thickness);
    }

    /**
     * Draw a box
     *
     * @param x         X position
     * @param y         Y position
     * @param width     Box width
     * @param height    Box height
     * @param thickness Border thickness
     * @return Fluent interface
     */
    public CpclPrinter drawBox(int x, int y, int width, int height, int thickness) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.drawBox(x, y, width, height, thickness);
        }
        return this;
    }

    // ==================== Utility Methods ====================

    /**
     * Set print speed
     *
     * @param speed Speed (0-5)
     * @return Fluent interface
     */
    public CpclPrinter setPrintSpeed(int speed) {
        if (this.printer != null) {
            this.printer.setPrintSpeed(speed);
        }
        return this;
    }

    /**
     * Set print density
     *
     * @param density Density (-3 to 3)
     * @return Fluent interface
     */
    public CpclPrinter setPrintDensity(int density) {
        if (this.printer != null) {
            this.printer.setPrintDensity(density);
        }
        return this;
    }

    /**
     * Send raw CPCL command
     *
     * @param command Raw CPCL command
     * @return Fluent interface
     */
    public CpclPrinter sendRawCommand(String command) throws EscPosEncodingException {
        if (this.printer != null) {
            this.printer.sendRawCommand(command);
        }
        return this;
    }

    /**
     * Get label width in dots
     */
    public int getLabelWidth() {
        return labelWidth;
    }

    /**
     * Get label height in dots
     */
    public int getLabelHeight() {
        return labelHeight;
    }

    /**
     * Get DPI
     */
    public int getDpi() {
        return dpi;
    }

    /**
     * Estimate text width for positioning
     * Simple estimation: 8 dots per character
     */
    private int estimateTextWidth(String text) {
        return text.length() * 8;
    }
}
