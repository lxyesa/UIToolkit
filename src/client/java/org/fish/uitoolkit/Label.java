package org.fish.uitoolkit;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * 渲染文本的控件。使用 {@link DrawContext#drawText} 绘制文本，
 * 支持颜色、阴影、水平居中、以及可调整的字体像素高度。
 */
public class Label extends Control {

    private Text text;
    private int color = 0xFFFFFFFF;
    private boolean shadow = true;
    private boolean centered = false;
    /** 目标字体像素高度 */
    private float fontSize = 9f;
    /** 缩放倍率 = fontSize / TextRenderer.fontHeight */
    private float fontScale = 1.0f;

    /**
     * 创建一个新的标签控件。
     * 
     * @param owner 控件的所有者，通常是一个 {@link Canvas} 或 {@link Panel}。
     * @param text  标签文本内容，不能为空。
     */
    public Label(Object owner, String text) {
        super(owner);
        this.text = Text.literal(text);
    }

    /**
     * 创建一个新的标签控件。
     * 
     * @param owner 控件的所有者，通常是一个 {@link Canvas} 或 {@link Panel}。
     * @param text  标签文本内容，不能为空。
     */
    public Label(Object owner, Text text) {
        super(owner);
        this.text = text != null ? text : Text.empty();
    }

    /**
     * 设置目标字体高度（像素）。此方法接受浮点数以支持精细缩放。
     * 当 {@link MinecraftClient#textRenderer} 可用时会根据其 base fontHeight 计算内部缩放比例，
     * 否则会延迟计算到首次渲染/测量时。
     *
     * @param size 像素高度，必须大于 0
     */
    public void setFontSize(float size) {
        if (size <= 0f)
            return;
        this.fontSize = size;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.textRenderer != null) {
            int base = client.textRenderer.fontHeight > 0 ? client.textRenderer.fontHeight : 9;
            this.fontScale = this.fontSize / (float) base;
        } else {
            this.fontScale = 1.0f;
        }
        this.invalidateAnchorContext();
    }

    /**
     * 延迟计算并同步内部 fontScale（在 textRenderer 可用时）。
     */
    private void ensureFontScale() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.textRenderer != null) {
            int base = client.textRenderer.fontHeight > 0 ? client.textRenderer.fontHeight : 9;
            float computed = this.fontSize / (float) base;
            if (Float.compare(computed, this.fontScale) != 0) {
                this.fontScale = computed;
                // 改变字体缩放后可能影响布局
                this.invalidateAnchorContext();
            }
        }
    }

    /**
     * 获取当前目标字体高度（像素）。
     *
     * @return 字体像素高度
     */
    public float getFontSize() {
        return this.fontSize;
    }

    /**
     * 设置标签文本内容。
     * 
     * @param text 文本内容，可以为 null，此时将设置为空文本。
     */
    public void setText(String text) {
        setText(Text.literal(text));
    }

    /**
     * 设置标签文本内容。
     * 
     * @param text 文本内容，可以为 null，此时将设置为空文本。
     */
    public void setText(Text text) {
        this.text = text != null ? text : Text.empty();
    }

    /**
     * 获取当前标签文本内容。
     * 
     * @return 当前标签文本内容
     */
    public Text getText() {
        return text;
    }

    /**
     * 设置文本颜色。
     * 
     * @param color 颜色值，格式为 ARGB（Alpha, Red, Green, Blue）
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * 获取当前文本颜色。
     * 
     * @param shadow 是否启用阴影
     */
    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    /**
     * 设置文本是否水平居中。
     * 
     * @param centered 如果为 true，则文本将水平居中显示；否则左对齐。
     */
    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    @Override
    protected void renderContent(DrawContext context, int absX, int absY, int mouseX, int mouseY, float delta) {
        super.renderContent(context, absX, absY, mouseX, mouseY, delta);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null)
            return;
        TextRenderer tr = client.textRenderer;
        if (tr == null)
            return;

        // 确保在渲染时使用最新的 fontScale（可能在 setFontSize 时 textRenderer 不可用）
        ensureFontScale();
        // 计算缩放后的宽度/位置
        float scale = this.fontScale > 0f ? this.fontScale : 1.0f;
        int rawWidth = tr.getWidth(text);
        int scaledWidth = Math.round(rawWidth * scale);
        int drawX = getX();
        int drawY = getY();
        if (centered) {
            drawX = drawX - scaledWidth / 2;
        }

        if (scale == 1.0f) {
            context.drawText(tr, text, drawX, drawY, color, shadow);
        } else {
            // 使用矩阵缩放来绘制缩放后的文本；坐标需要反向缩放以补偿全局缩放
            context.getMatrices().push();
            context.getMatrices().scale(scale, scale, scale);
            try {
                context.drawText(tr, text, Math.round(drawX / scale), Math.round(drawY / scale), color, shadow);
            } finally {
                context.getMatrices().pop();
            }
        }
    }

    @Override
    public int getWidth() {
        ensureFontScale();
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        if (tr == null)
            return 0;
        return Math.round(tr.getWidth(text) * this.fontScale);
    }

    @Override
    public int getHeight() {
        ensureFontScale();
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        if (tr == null)
            return Math.round(9 * this.fontScale);
        return Math.max(1, Math.round(tr.fontHeight * this.fontScale));
    }
}
