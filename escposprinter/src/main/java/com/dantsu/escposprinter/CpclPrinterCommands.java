package com.dantsu.escposprinter;

import android.graphics.Bitmap;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;

import java.io.UnsupportedEncodingException;

/**
 * CPCL (Comtec Printer Command Language) Printer Commands
 * Support for Zebra, TSC, Honeywell and other label printers
 */
public class CpclPrinterCommands {

    private DeviceConnection printerConnection;
    private EscPosCharsetEncoding charsetEncoding;

    // CPCL Command Constants
    public static final String COMMAND_PREFIX = "! ";
    public static final String END_COMMAND = "PRINT\r\n";
    public static final String FORM_COMMAND = "FORM\r\n";

    // Text alignment
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;

    // Barcode types
    public static final int BARCODE_128 = 1;
    public static final int BARCODE_39 = 2;
    public static final int BARCODE_93 = 3;
    public static final int BARCODE_CODABAR = 4;
    public static final int BARCODE_EAN13 = 5;
    public static final int BARCODE_EAN8 = 6;
    public static final int BARCODE_UPCA = 7;
    public static final int BARCODE_UPCE = 8;
    public static final int BARCODE_I2OF5 = 9;

    // QR Code error correction levels
    public static final int QR_EC_L = 0;  // Low
    public static final int QR_EC_M = 1;  // Medium
    public static final int QR_EC_Q = 2;  // Quartile
    public static final int QR_EC_H = 3;  // High

    private int labelWidth = 576;   // Default width in dots (72mm at 203dpi)
    private int labelHeight = 406;  // Default height in dots (51mm at 203dpi)
    private int printSpeed = 3;     // Default print speed
    private int printDensity = 0;   // Default density

    /**
     * Create new instance of CpclPrinterCommands.
     *
     * @param printerConnection an instance of a class which implement DeviceConnection
     */
    public CpclPrinterCommands(DeviceConnection printerConnection) {
        this(printerConnection, null);
    }

    /**
     * Create new instance of CpclPrinterCommands.
     *
     * @param printerConnection an instance of a class which implement DeviceConnection
     * @param charsetEncoding   Set the charset encoding.
     */
    public CpclPrinterCommands(DeviceConnection printerConnection, EscPosCharsetEncoding charsetEncoding) {
        this.printerConnection = printerConnection;
        this.charsetEncoding = charsetEncoding != null ? charsetEncoding : new EscPosCharsetEncoding("UTF-8", 0);
    }

    /**
     * Start socket connection and open stream with the device.
     */
    public CpclPrinterCommands connect() throws EscPosConnectionException {
        this.printerConnection.connect();
        return this;
    }

    /**
     * Close the socket connection and stream with the device.
     */
    public void disconnect() {
        this.printerConnection.disconnect();
    }

    /**
     * Set label dimensions
     *
     * @param width  Label width in dots
     * @param height Label height in dots
     * @return Fluent interface
     */
    public CpclPrinterCommands setLabelSize(int width, int height) {
        this.labelWidth = width;
        this.labelHeight = height;
        return this;
    }

    /**
     * Set print speed (0-5)
     *
     * @param speed Print speed (0-5)
     * @return Fluent interface
     */
    public CpclPrinterCommands setPrintSpeed(int speed) {
        this.printSpeed = Math.max(0, Math.min(5, speed));
        return this;
    }

    /**
     * Set print density (-3 to 3)
     *
     * @param density Print density (-3 to 3)
     * @return Fluent interface
     */
    public CpclPrinterCommands setPrintDensity(int density) {
        this.printDensity = Math.max(-3, Math.min(3, density));
        return this;
    }

    /**
     * Initialize a new label with CPCL header
     *
     * @param quantity Number of labels to print
     * @return Fluent interface
     */
    public CpclPrinterCommands startLabel(int quantity) throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        String header = String.format(
            "! %d %d %d %d %d\r\n",
            printSpeed, printDensity, labelWidth, labelHeight, quantity
        );

        try {
            this.printerConnection.write(header.getBytes(this.charsetEncoding.getName()));
        } catch (UnsupportedEncodingException e) {
            throw new EscPosEncodingException(e.getMessage());
        }
        return this;
    }

    /**
     * Initialize a new label with default quantity (1)
     *
     * @return Fluent interface
     */
    public CpclPrinterCommands startLabel() throws EscPosEncodingException {
        return startLabel(1);
    }

    /**
     * End the label and send print command
     *
     * @return Fluent interface
     */
    public CpclPrinterCommands endLabel() throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        try {
            this.printerConnection.write(FORM_COMMAND.getBytes(this.charsetEncoding.getName()));
            this.printerConnection.write(END_COMMAND.getBytes(this.charsetEncoding.getName()));
        } catch (UnsupportedEncodingException e) {
            throw new EscPosEncodingException(e.getMessage());
        }
        return this;
    }

    /**
     * Print text at specified position
     *
     * @param font     Font number (0-7)
     * @param size     Font size multiplier
     * @param x        X position in dots
     * @param y        Y position in dots
     * @param text     Text to print
     * @return Fluent interface
     */
    public CpclPrinterCommands printText(int font, int size, int x, int y, String text) throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        String command = String.format("TEXT %d %d %d %d %s\r\n", font, size, x, y, text);

        try {
            this.printerConnection.write(command.getBytes(this.charsetEncoding.getName()));
        } catch (UnsupportedEncodingException e) {
            throw new EscPosEncodingException(e.getMessage());
        }
        return this;
    }

    /**
     * Print text with default font
     *
     * @param x    X position in dots
     * @param y    Y position in dots
     * @param text Text to print
     * @return Fluent interface
     */
    public CpclPrinterCommands printText(int x, int y, String text) throws EscPosEncodingException {
        return printText(4, 0, x, y, text);
    }

    /**
     * Print text with custom font and automatic line wrapping
     *
     * @param font      Font number
     * @param size      Font size
     * @param x         X position
     * @param y         Y position
     * @param maxWidth  Maximum width for wrapping
     * @param text      Text to print
     * @return Fluent interface
     */
    public CpclPrinterCommands printTextWrapped(int font, int size, int x, int y, int maxWidth, String text) throws EscPosEncodingException {
        // Simple word wrapping implementation
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int lineHeight = getFontHeight(font, size);
        int currentY = y;

        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            int lineWidth = calculateTextWidth(font, size, testLine);

            if (lineWidth > maxWidth && currentLine.length() > 0) {
                printText(font, size, x, currentY, currentLine.toString());
                currentLine = new StringBuilder(word);
                currentY += lineHeight;
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }

        if (currentLine.length() > 0) {
            printText(font, size, x, currentY, currentLine.toString());
        }

        return this;
    }

    /**
     * Print a barcode
     *
     * @param type        Barcode type (use BARCODE_ constants)
     * @param width       Bar width in dots
     * @param ratio       Wide to narrow ratio (0-3)
     * @param height      Barcode height in dots
     * @param x           X position
     * @param y           Y position
     * @param data        Barcode data
     * @return Fluent interface
     */
    public CpclPrinterCommands printBarcode(int type, int width, int ratio, int height, int x, int y, String data) throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        String command = String.format("BARCODE %d %d %d %d %d %d %s\r\n", type, width, ratio, height, x, y, data);

        try {
            this.printerConnection.write(command.getBytes(this.charsetEncoding.getName()));
        } catch (UnsupportedEncodingException e) {
            throw new EscPosEncodingException(e.getMessage());
        }
        return this;
    }

    /**
     * Print a barcode with text below
     *
     * @param type        Barcode type
     * @param width       Bar width
     * @param ratio       Wide to narrow ratio
     * @param height      Barcode height
     * @param x           X position
     * @param y           Y position
     * @param data        Barcode data
     * @param showText    Whether to show text below barcode
     * @return Fluent interface
     */
    public CpclPrinterCommands printBarcode(int type, int width, int ratio, int height, int x, int y, String data, boolean showText) throws EscPosEncodingException {
        printBarcode(type, width, ratio, height, x, y, data);

        if (showText) {
            int textY = y + height + 5;
            int textX = x;
            printText(4, 0, textX, textY, data);
        }

        return this;
    }

    /**
     * Print a QR Code
     *
     * @param x         X position
     * @param y         Y position
     * @param model     QR Code model (1 or 2)
     * @param size      QR Code size (1-32)
     * @param ecLevel   Error correction level (0-3)
     * @param data      QR Code data
     * @return Fluent interface
     */
    public CpclPrinterCommands printQRCode(int x, int y, int model, int size, int ecLevel, String data) throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        try {
            // Set QR Code parameters
            String setParams = String.format("BARCODE QR %d %d M%d U%d\r\n", x, y, model, size);
            this.printerConnection.write(setParams.getBytes(this.charsetEncoding.getName()));

            // Set error correction level
            String setEC = String.format("MA%d\r\n", ecLevel);
            this.printerConnection.write(setEC.getBytes(this.charsetEncoding.getName()));

            // Set data
            String setData = String.format("MD%s\r\n", data);
            this.printerConnection.write(setData.getBytes(this.charsetEncoding.getName()));

            // End QR Code
            this.printerConnection.write("ENDQR\r\n".getBytes(this.charsetEncoding.getName()));
        } catch (UnsupportedEncodingException e) {
            throw new EscPosEncodingException(e.getMessage());
        }
        return this;
    }

    /**
     * Print QR Code with default parameters
     *
     * @param x     X position
     * @param y     Y position
     * @param size  Size (1-32)
     * @param data  QR Code data
     * @return Fluent interface
     */
    public CpclPrinterCommands printQRCode(int x, int y, int size, String data) throws EscPosEncodingException {
        return printQRCode(x, y, 2, size, QR_EC_M, data);
    }

    /**
     * Print a bitmap image
     *
     * @param x         X position
     * @param y         Y position
     * @param width     Image width in dots
     * @param height    Image height in dots
     * @param bitmap    Bitmap data
     * @return Fluent interface
     */
    public CpclPrinterCommands printBitmap(int x, int y, int width, int height, Bitmap bitmap) throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        byte[] imageData = bitmapToCPCL(bitmap, x, y);
        this.printerConnection.write(imageData);

        return this;
    }

    /**
     * Draw a line
     *
     * @param x1        Start X
     * @param y1        Start Y
     * @param x2        End X
     * @param y2        End Y
     * @param thickness Line thickness in dots
     * @return Fluent interface
     */
    public CpclPrinterCommands drawLine(int x1, int y1, int x2, int y2, int thickness) throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        String command = String.format("LINE %d %d %d %d %d\r\n", x1, y1, x2, y2, thickness);

        try {
            this.printerConnection.write(command.getBytes(this.charsetEncoding.getName()));
        } catch (UnsupportedEncodingException e) {
            throw new EscPosEncodingException(e.getMessage());
        }
        return this;
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
    public CpclPrinterCommands drawBox(int x, int y, int width, int height, int thickness) throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        String command = String.format("BOX %d %d %d %d %d\r\n", x, y, x + width, y + height, thickness);

        try {
            this.printerConnection.write(command.getBytes(this.charsetEncoding.getName()));
        } catch (UnsupportedEncodingException e) {
            throw new EscPosEncodingException(e.getMessage());
        }
        return this;
    }

    /**
     * Draw an inverted box (black background)
     *
     * @param x         X position
     * @param y         Y position
     * @param width     Box width
     * @param height    Box height
     * @return Fluent interface
     */
    public CpclPrinterCommands drawInvertedBox(int x, int y, int width, int height) throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        String command = String.format("INVERSE-LINE %d %d %d %d %d\r\n", x, y, x + width, y, height);

        try {
            this.printerConnection.write(command.getBytes(this.charsetEncoding.getName()));
        } catch (UnsupportedEncodingException e) {
            throw new EscPosEncodingException(e.getMessage());
        }
        return this;
    }

    /**
     * Set page width (for continuous paper)
     *
     * @param width Page width in dots
     * @return Fluent interface
     */
    public CpclPrinterCommands setPageWidth(int width) throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        String command = String.format("PAGE-WIDTH %d\r\n", width);

        try {
            this.printerConnection.write(command.getBytes(this.charsetEncoding.getName()));
        } catch (UnsupportedEncodingException e) {
            throw new EscPosEncodingException(e.getMessage());
        }
        return this;
    }

    /**
     * Send raw CPCL command
     *
     * @param command Raw CPCL command string
     * @return Fluent interface
     */
    public CpclPrinterCommands sendRawCommand(String command) throws EscPosEncodingException {
        if (!this.printerConnection.isConnected()) {
            return this;
        }

        try {
            this.printerConnection.write(command.getBytes(this.charsetEncoding.getName()));
        } catch (UnsupportedEncodingException e) {
            throw new EscPosEncodingException(e.getMessage());
        }
        return this;
    }

    /**
     * Convert Bitmap to CPCL CG command format
     */
    private byte[] bitmapToCPCL(Bitmap bitmap, int x, int y) throws EscPosEncodingException {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Calculate bytes per row (8 pixels per byte)
        int bytesPerRow = (width + 7) / 8;
        int totalBytes = bytesPerRow * height;

        // CPCL CG command: CG width height x y data
        String header = String.format("CG %d %d %d %d ", bytesPerRow, height, x, y);

        try {
            byte[] headerBytes = header.getBytes(this.charsetEncoding.getName());
            byte[] imageBytes = new byte[headerBytes.length + totalBytes + 2]; // +2 for \r\n

            System.arraycopy(headerBytes, 0, imageBytes, 0, headerBytes.length);

            int byteIndex = headerBytes.length;
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < bytesPerRow; col++) {
                    byte pixelByte = 0;
                    for (int bit = 0; bit < 8; bit++) {
                        int pixelX = col * 8 + bit;
                        if (pixelX < width) {
                            int pixel = bitmap.getPixel(pixelX, row);
                            int r = (pixel >> 16) & 0xff;
                            int g = (pixel >> 8) & 0xff;
                            int blue = pixel & 0xff;
                            // If dark pixel, set bit
                            if ((r + g + blue) / 3 < 128) {
                                pixelByte |= (1 << (7 - bit));
                            }
                        }
                    }
                    imageBytes[byteIndex++] = pixelByte;
                }
            }

            // Add CRLF
            imageBytes[byteIndex++] = '\r';
            imageBytes[byteIndex] = '\n';

            return imageBytes;
        } catch (UnsupportedEncodingException e) {
            throw new EscPosEncodingException(e.getMessage());
        }
    }

    /**
     * Calculate approximate text width for word wrapping
     */
    private int calculateTextWidth(int font, int size, String text) {
        // Approximate width calculation
        int baseWidth = 8; // Base character width
        if (font >= 4 && font <= 7) {
            baseWidth = 12; // Larger fonts
        }
        return text.length() * baseWidth * (size + 1);
    }

    /**
     * Get font height for line spacing
     */
    private int getFontHeight(int font, int size) {
        int baseHeight = 12; // Base line height
        if (font >= 4 && font <= 7) {
            baseHeight = 16;
        }
        return baseHeight * (size + 1);
    }

    /**
     * @return Charset encoding
     */
    public EscPosCharsetEncoding getCharsetEncoding() {
        return this.charsetEncoding;
    }
}
