package org.fish.uitoolkit;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * 可渲染的文本标签，使用 {@link DrawContext#drawText} 绘制文本。
 * 支持颜色、阴影、居中、锚点与 margin。
 */
public class Label extends Control {

    private Text text;
    private int color = 0xFFFFFFFF;
    private boolean shadow = true;
    private boolean centered = false; // 水平居中

    public Label(Object owner, String text) {
        super(owner);
        this.text = Text.literal(text);
    }

    public Label(Object owner, Text text) {
        super(owner);
        this.text = text != null ? text : Text.empty();
    }

    public void setText(String text) {
        setText(Text.literal(text));
    }

    public void setText(Text text) {
        this.text = text != null ? text : Text.empty();
    }

    public Text getText() {
        return text;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    public void setPosition(int x, int y) {
        super.setPosition(x, y);
    }

    public void setHorizontalAnchor(HAnchor a) {
        super.setHorizontalAnchor(a);
    }

    public void setVerticalAnchor(VAnchor a) {
        super.setVerticalAnchor(a);
    }

    public void setMargins(int left, int top, int right, int bottom) {
        super.setMargins(left, top, right, bottom);
    }

    @Override
    public Object getOwner() {
        return super.getOwner();
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

        int drawX = getX();
        int drawY = getY();
        if (centered) {
            int textWidth = tr.getWidth(text);
            drawX = drawX - textWidth / 2;
        }
        context.drawText(tr, text, drawX, drawY, color, shadow);
    }

    @Override
    public int getLocalX() {
        return x;
    }

    @Override
    public int getLocalY() {
        return y;
    }

    @Override
    public int getWidth() {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        return tr != null ? tr.getWidth(text) : 0;
    }

    @Override
    public int getHeight() {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        return tr != null ? tr.fontHeight : 9;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public HAnchor getHorizontalAnchor() {
        return hAnchor;
    }

    @Override
    public VAnchor getVerticalAnchor() {
        return vAnchor;
    }

    @Override
    public int getMarginLeft() {
        return marginLeft;
    }

    @Override
    public int getMarginRight() {
        return marginRight;
    }

    @Override
    public int getMarginTop() {
        return marginTop;
    }

    @Override
    public int getMarginBottom() {
        return marginBottom;
    }

}
