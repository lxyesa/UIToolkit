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

        int drawX = getX();
        int drawY = getY();
        if (centered) {
            int textWidth = tr.getWidth(text);
            drawX = drawX - textWidth / 2;
        }
        context.drawText(tr, text, drawX, drawY, color, shadow);
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
}
