package org.fish.uitoolkit.v2.controls;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;

/** 文本标签，用于显示文本 */
public class Label extends ControlObject {
    private String text;
    private float textScale = 1.0f;
    private int textColor = 0xFFFFFF;
    private int fontSizePx = 0;

    public enum HAlign {
        LEFT, CENTER, RIGHT
    }

    public enum VAlign {
        TOP, CENTER, BOTTOM
    }

    private HAlign horizontalAlign = HAlign.LEFT;
    private VAlign verticalAlign = VAlign.TOP;
    // padding in pixels
    private int paddingLeft = 2;
    private int paddingRight = 2;
    private int paddingTop = 2;
    private int paddingBottom = 2;

    public Label(String text) {
        this.text = text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public void setTextScale(float s) {
        if (s > 0f)
            this.textScale = s;
    }

    public float getTextScale() {
        return this.textScale;
    }

    /**
     * 设置期望的字体像素高度（像素为单位）。传入 0 可禁用（恢复使用渲染器原始高度 * textScale）。
     */
    public void setFontSizePx(int px) {
        if (px >= 0)
            this.fontSizePx = px;
    }

    /** 返回当前的像素字体高度（0 表示未设置，使用默认字体高度乘以 textScale）。 */
    public int getFontSizePx() {
        return this.fontSizePx;
    }

    public void setTextColor(int color) {
        this.textColor = color & 0xFFFFFF;
    }

    public int getTextColor() {
        return this.textColor;
    }

    public void setHorizontalAlign(HAlign a) {
        if (a != null)
            this.horizontalAlign = a;
    }

    public HAlign getHorizontalAlign() {
        return this.horizontalAlign;
    }

    public void setVerticalAlign(VAlign a) {
        if (a != null)
            this.verticalAlign = a;
    }

    public VAlign getVerticalAlign() {
        return this.verticalAlign;
    }

    /** 设置所有方向相同的内边距（像素）。 */
    public void setPadding(int p) {
        if (p >= 0) {
            this.paddingLeft = p;
            this.paddingRight = p;
            this.paddingTop = p;
            this.paddingBottom = p;
        }
    }

    /** 分别设置水平和垂直的内边距。 */
    public void setPadding(int horizontal, int vertical) {
        if (horizontal >= 0 && vertical >= 0) {
            this.paddingLeft = horizontal;
            this.paddingRight = horizontal;
            this.paddingTop = vertical;
            this.paddingBottom = vertical;
        }
    }

    /** 分别设置四个方向的内边距。 */
    public void setPadding(int left, int top, int right, int bottom) {
        if (left >= 0 && top >= 0 && right >= 0 && bottom >= 0) {
            this.paddingLeft = left;
            this.paddingTop = top;
            this.paddingRight = right;
            this.paddingBottom = bottom;
        }
    }

    public int getPaddingLeft() {
        return this.paddingLeft;
    }

    public int getPaddingRight() {
        return this.paddingRight;
    }

    public int getPaddingTop() {
        return this.paddingTop;
    }

    public int getPaddingBottom() {
        return this.paddingBottom;
    }

    // --- 位置辅助方法（水平/垂直） ---
    /** 返回控件左边 X 坐标（等同于 getX()）。 */
    public int getLeft() {
        return this.getX();
    }

    /** 返回控件右边 X 坐标（getX + width）。 */
    public int getRight() {
        return this.getX() + this.getWidth();
    }

    /** 返回控件水平中心 X 坐标（getX + width/2）。 */
    public int getCenterX() {
        return this.getX() + this.getWidth() / 2;
    }

    /** 返回控件顶端 Y 坐标（等同于 getY()）。 */
    public int getTop() {
        return this.getY();
    }

    /** 返回控件底部 Y 坐标（getY + height）。 */
    public int getBottom() {
        return this.getY() + this.getHeight();
    }

    /** 返回控件垂直中心 Y 坐标（getY + height/2）。 */
    public int getCenterY() {
        return this.getY() + this.getHeight() / 2;
    }

    @Override
    public void update(float tickDelta) {
        super.update(tickDelta);
        // compute size from actual font metrics when available so multi-line text
        // and scaling are handled correctly
        var client = MinecraftClient.getInstance();
        if (client != null && client.textRenderer != null) {
            // split on literal newline to support multi-line labels
            String[] lines = this.text.split("\n", -1);
            int linesCount = Math.max(1, lines.length);
            int maxWidth = 0;
            // compute effective scale: base textScale multiplied by font-size adjustment
            int baseFontHeight = client.textRenderer.fontHeight;
            float effectiveScale = this.textScale;
            if (this.fontSizePx > 0 && baseFontHeight > 0) {
                effectiveScale *= (this.fontSizePx / (float) baseFontHeight);
            }
            for (String l : lines) {
                int w = client.textRenderer.getWidth(l);
                if (w > maxWidth)
                    maxWidth = w;
            }
            int fontHeight = client.textRenderer.fontHeight;
            // apply effectiveScale because rendering scales the text via matrices
            // account for padding (scale padding by effectiveScale)
            int padH = Math.round((this.paddingLeft + this.paddingRight) * effectiveScale);
            int padV = Math.round((this.paddingTop + this.paddingBottom) * effectiveScale);
            int width = Math.round(maxWidth * effectiveScale) + padH;
            int height = Math.round(fontHeight * linesCount * effectiveScale) + padV;
            this.setSize(width, height);
        } else {
            // fallback to previous heuristic if font info isn't available
            this.setSize(this.text.length() * 6 + 4, 12);
        }
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        super.render(context, tickDelta);
        try {
            var client = MinecraftClient.getInstance();
            if (client != null && client.textRenderer != null) {
                // compute effective scale the same way as in update
                int baseFontHeight = client.textRenderer.fontHeight;
                float effectiveScale = this.textScale;
                if (this.fontSizePx > 0 && baseFontHeight > 0) {
                    effectiveScale *= (this.fontSizePx / (float) baseFontHeight);
                }

                // measure text (multi-line)
                String[] lines = this.text.split("\n", -1);
                int linesCount = Math.max(1, lines.length);
                int maxWidth = 0;
                for (String l : lines) {
                    int w = client.textRenderer.getWidth(l);
                    if (w > maxWidth)
                        maxWidth = w;
                }
                int fontHeight = client.textRenderer.fontHeight;
                float textWidthScaled = maxWidth * effectiveScale;
                float textHeightScaled = fontHeight * linesCount * effectiveScale;

                // compute offsets inside the control based on padding
                float padLeft = this.paddingLeft;
                float padRight = this.paddingRight;
                float padTop = this.paddingTop;
                float padBottom = this.paddingBottom;
                // scale padding when applying effectiveScale to measurements
                float scaledPadLeft = padLeft * effectiveScale;
                float scaledPadRight = padRight * effectiveScale;
                float scaledPadTop = padTop * effectiveScale;
                float scaledPadBottom = padBottom * effectiveScale;
                float offsetX;
                switch (this.horizontalAlign) {
                    case CENTER:
                        offsetX = (this.getWidth() - textWidthScaled) / 2f;
                        break;
                    case RIGHT:
                        offsetX = this.getWidth() - textWidthScaled - scaledPadRight;
                        break;
                    default:
                        offsetX = scaledPadLeft;
                        break;
                }

                float offsetY;
                switch (this.verticalAlign) {
                    case CENTER:
                        offsetY = (this.getHeight() - textHeightScaled + 1) / 2f;
                        break;
                    case BOTTOM:
                        offsetY = this.getHeight() - textHeightScaled - scaledPadBottom;
                        break;
                    default:
                        offsetY = scaledPadTop;
                        break;
                }

                // render lines with scaling and per-line vertical spacing
                if (effectiveScale != 1.0f) {
                    context.getMatrices().push();
                    float s = effectiveScale;
                    context.getMatrices().scale(s, s, s);
                    for (int i = 0; i < linesCount; i++) {
                        String line = lines[i];
                        int dx = Math.round((this.getX() + offsetX) / s);
                        int dy = Math.round((this.getY() + offsetY) / s + i * fontHeight);
                        context.drawTextWithShadow(client.textRenderer, line, dx, dy, this.textColor);
                    }
                    context.getMatrices().pop();
                } else {
                    for (int i = 0; i < linesCount; i++) {
                        String line = lines[i];
                        int dx = this.getX() + Math.round(offsetX);
                        int dy = this.getY() + Math.round(offsetY + i * fontHeight);
                        context.drawTextWithShadow(client.textRenderer, line, dx, dy, this.textColor);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
