package com.dantsu.escposprinter.example;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.dantsu.escposprinter.template.BarcodeComponent;
import com.dantsu.escposprinter.template.LabelComponent;
import com.dantsu.escposprinter.template.LabelTemplate;
import com.dantsu.escposprinter.template.QRCodeComponent;
import com.dantsu.escposprinter.template.TextComponent;

/**
 * 标签画布视图
 * 用于可视化编辑标签模板
 */
public class LabelCanvasView extends View {

    private LabelTemplate template;
    private LabelComponent selectedComponent;
    private OnComponentSelectedListener listener;

    private Paint backgroundPaint;
    private Paint borderPaint;
    private Paint gridPaint;
    private Paint componentPaint;
    private Paint selectedPaint;
    private Paint textPaint;
    private Paint barcodePaint;

    private float scale = 3.0f; // 缩放比例
    private float offsetX = 20;
    private float offsetY = 20;

    private float lastTouchX;
    private float lastTouchY;
    private boolean isDragging = false;

    public LabelCanvasView(Context context) {
        super(context);
        init();
    }

    public LabelCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LabelCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 背景画笔
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);

        // 边框画笔
        borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);

        // 网格画笔
        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));

        // 组件画笔
        componentPaint = new Paint();
        componentPaint.setColor(Color.BLUE);
        componentPaint.setStyle(Paint.Style.STROKE);
        componentPaint.setStrokeWidth(2);

        // 选中画笔
        selectedPaint = new Paint();
        selectedPaint.setColor(Color.RED);
        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeWidth(3);

        // 文本画笔
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(24);

        // 条码画笔
        barcodePaint = new Paint();
        barcodePaint.setColor(Color.BLACK);
        barcodePaint.setStyle(Paint.Style.FILL);
    }

    public void setTemplate(LabelTemplate template) {
        this.template = template;
        invalidate();
    }

    public void setOnComponentSelectedListener(OnComponentSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (template == null) return;

        // 计算标签尺寸（像素）
        int labelWidthPx = (int) (mmToPx(template.getWidth()) * scale);
        int labelHeightPx = (int) (mmToPx(template.getHeight()) * scale);

        // 绘制背景
        canvas.drawRect(offsetX, offsetY, offsetX + labelWidthPx, offsetY + labelHeightPx, backgroundPaint);

        // 绘制网格
        drawGrid(canvas, labelWidthPx, labelHeightPx);

        // 绘制边框
        canvas.drawRect(offsetX, offsetY, offsetX + labelWidthPx, offsetY + labelHeightPx, borderPaint);

        // 绘制组件
        if (template.getComponents() != null) {
            for (LabelComponent component : template.getComponents()) {
                drawComponent(canvas, component);
            }
        }
    }

    private void drawGrid(Canvas canvas, int width, int height) {
        int gridSize = (int) (10 * scale); // 10mm 网格

        for (int x = 0; x <= width; x += gridSize) {
            canvas.drawLine(offsetX + x, offsetY, offsetX + x, offsetY + height, gridPaint);
        }

        for (int y = 0; y <= height; y += gridSize) {
            canvas.drawLine(offsetX, offsetY + y, offsetX + width, offsetY + y, gridPaint);
        }
    }

    private void drawComponent(Canvas canvas, LabelComponent component) {
        float x = offsetX + dotsToPx(component.getX()) * scale;
        float y = offsetY + dotsToPx(component.getY()) * scale;
        float width = dotsToPx(component.getWidth()) * scale;
        float height = dotsToPx(component.getHeight()) * scale;

        // 绘制选中框
        if (component == selectedComponent) {
            canvas.drawRect(x - 5, y - 5, x + width + 5, y + height + 5, selectedPaint);
        }

        // 根据组件类型绘制
        switch (component.getType()) {
            case TEXT:
                drawTextComponent(canvas, (TextComponent) component, x, y, width, height);
                break;
            case BARCODE:
                drawBarcodeComponent(canvas, (BarcodeComponent) component, x, y, width, height);
                break;
            case QRCODE:
                drawQRCodeComponent(canvas, (QRCodeComponent) component, x, y, width, height);
                break;
            case LINE:
                drawLineComponent(canvas, component, x, y, width, height);
                break;
            default:
                // 默认绘制矩形框
                canvas.drawRect(x, y, x + width, y + height, componentPaint);
                break;
        }
    }

    private void drawTextComponent(Canvas canvas, TextComponent component, float x, float y, float width, float height) {
        String text = component.getStaticValue();
        if (text == null || text.isEmpty()) {
            text = "[文本]";
        }

        // 计算字体大小
        float fontSize = 12 * (component.getFontHeight() + 1) * scale;
        textPaint.setTextSize(fontSize);

        // 粗体
        textPaint.setFakeBoldText(component.isBold());

        // 绘制文本
        canvas.drawText(text, x, y + fontSize, textPaint);

        // 绘制下划线
        if (component.isUnderline()) {
            canvas.drawLine(x, y + fontSize + 2, x + width, y + fontSize + 2, textPaint);
        }

        // 绘制边框（调试）
        canvas.drawRect(x, y, x + width, y + height, componentPaint);
    }

    private void drawBarcodeComponent(Canvas canvas, BarcodeComponent component, float x, float y, float width, float height) {
        String text = component.getStaticValue();
        if (text == null || text.isEmpty()) {
            text = "1234567890";
        }

        // 绘制条码示意（简化）
        float barWidth = component.getBarWidth() * scale;
        float currentX = x;

        for (int i = 0; i < text.length(); i++) {
            if (currentX >= x + width) break;

            // 交替绘制黑白条
            if (i % 2 == 0) {
                canvas.drawRect(currentX, y, currentX + barWidth, y + height - 20, barcodePaint);
            }
            currentX += barWidth * 2;
        }

        // 绘制文字
        if (component.isShowText()) {
            textPaint.setTextSize(12 * scale);
            canvas.drawText(text, x, y + height, textPaint);
        }

        // 绘制边框
        canvas.drawRect(x, y, x + width, y + height, componentPaint);
    }

    private void drawQRCodeComponent(Canvas canvas, QRCodeComponent component, float x, float y, float width, float height) {
        // 绘制二维码示意（简化）
        float cellSize = component.getCellWidth() * scale;
        int cells = (int) (width / cellSize);

        for (int row = 0; row < cells; row++) {
            for (int col = 0; col < cells; col++) {
                // 随机填充一些单元格模拟二维码
                if ((row + col) % 3 == 0 || row < 3 || col < 3 || row >= cells - 3 || col >= cells - 3) {
                    canvas.drawRect(
                        x + col * cellSize,
                        y + row * cellSize,
                        x + (col + 1) * cellSize,
                        y + (row + 1) * cellSize,
                        barcodePaint
                    );
                }
            }
        }

        // 绘制边框
        canvas.drawRect(x, y, x + width, y + height, componentPaint);
    }

    private void drawLineComponent(Canvas canvas, LabelComponent component, float x, float y, float width, float height) {
        canvas.drawLine(x, y, x + width, y + height, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = touchX;
                lastTouchY = touchY;

                // 检查是否点击了组件
                LabelComponent clickedComponent = findComponentAt(touchX, touchY);
                if (clickedComponent != null) {
                    selectedComponent = clickedComponent;
                    isDragging = true;
                    if (listener != null) {
                        listener.onComponentSelected(selectedComponent);
                    }
                    invalidate();
                } else {
                    selectedComponent = null;
                    isDragging = false;
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isDragging && selectedComponent != null) {
                    float dx = (touchX - lastTouchX) / scale;
                    float dy = (touchY - lastTouchY) / scale;

                    // 转换为 dots 并更新位置
                    int newX = selectedComponent.getX() + pxToDots(dx);
                    int newY = selectedComponent.getY() + pxToDots(dy);

                    // 限制在标签范围内
                    newX = Math.max(0, newX);
                    newY = Math.max(0, newY);

                    selectedComponent.setX(newX);
                    selectedComponent.setY(newY);

                    if (listener != null) {
                        listener.onComponentMoved(selectedComponent, newX, newY);
                    }

                    lastTouchX = touchX;
                    lastTouchY = touchY;
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                isDragging = false;
                return true;
        }

        return super.onTouchEvent(event);
    }

    private LabelComponent findComponentAt(float x, float y) {
        if (template == null || template.getComponents() == null) {
            return null;
        }

        // 从后向前查找（上面的组件优先）
        for (int i = template.getComponents().size() - 1; i >= 0; i--) {
            LabelComponent component = template.getComponents().get(i);

            float compX = offsetX + dotsToPx(component.getX()) * scale;
            float compY = offsetY + dotsToPx(component.getY()) * scale;
            float compW = dotsToPx(component.getWidth()) * scale;
            float compH = dotsToPx(component.getHeight()) * scale;

            if (x >= compX && x <= compX + compW && y >= compY && y <= compY + compH) {
                return component;
            }
        }

        return null;
    }

    // 单位转换
    private float mmToPx(float mm) {
        // 假设 203 DPI: 1 inch = 25.4mm = 203 dots
        return mm * 203 / 25.4f;
    }

    private float dotsToPx(int dots) {
        // dots 转像素 (假设 203 DPI)
        return dots * 25.4f / 203;
    }

    private int pxToDots(float px) {
        return (int) (px * 203 / 25.4f);
    }

    // 监听器接口
    public interface OnComponentSelectedListener {
        void onComponentSelected(LabelComponent component);
        void onComponentMoved(LabelComponent component, int x, int y);
    }
}
