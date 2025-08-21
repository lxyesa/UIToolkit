package org.fish.uitoolkit;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

/**
 * 主 UI 渲染画布：管理并渲染所有顶层 UIElement（允许多个 Canvas 实例）。
 *
 * 特性：
 * - 默认大小覆盖为全屏（基于 Minecraft 窗口的 scaled 大小，适配 DPI / 缩放因子）
 * - 提供渲染入口、事件分发和大小更新接口
 * - 不继承自 UIElement；它是 "主" 容器，适合作为屏幕上的根节点
 */
public class Canvas extends Container {

    private int width;
    private int height;
    /** Canvas 的内边距（padding）——用于缩小内容绘制区域。 */
    private int paddingLeft = 0;
    private int paddingRight = 0;
    private int paddingTop = 0;
    private int paddingBottom = 0;

    public Canvas() {
        updateSizeFromWindow();
    }

    /**
     * 将当前 Canvas 大小设置为 Minecraft 窗口的 scaled 大小（考虑 DPI / 缩放）。
     */
    public void updateSizeFromWindow() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getWindow() == null) {
            this.width = 0;
            this.height = 0;
            return;
        }
        // getScaledWidth/Height 已考虑窗口缩放（DPI）
        this.width = client.getWindow().getScaledWidth();
        this.height = client.getWindow().getScaledHeight();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * 设置画布内边距（左、上、右、下）。内边距会影响子元素的锚定参考区。
     */
    public void setPadding(int left, int top, int right, int bottom) {
        this.paddingLeft = left;
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
    }

    /** 返回内容区 X（考虑左侧 padding）。 */
    public int getContentX() {
        return paddingLeft;
    }

    /** 返回内容区 Y（考虑顶部 padding）。 */
    public int getContentY() {
        return paddingTop;
    }

    /** 返回内容区宽度（width - paddingLeft - paddingRight，最小为 0）。 */
    public int getContentWidth() {
        return Math.max(0, width - paddingLeft - paddingRight);
    }

    /** 返回内容区高度（height - paddingTop - paddingBottom，最小为 0）。 */
    public int getContentHeight() {
        return Math.max(0, height - paddingTop - paddingBottom);
    }

    /** 添加子元素（owner 类型为 Canvas）。 */
    @Override
    public Canvas addChild(UIElement child) {
        super.addChild(child);
        return this;
    }

    @Override
    public boolean removeChild(UIElement child) {
        return super.removeChild(child);
    }

    @Override
    public void clearChildren() {
        super.clearChildren();
    }

    @Override
    public List<UIElement> getChildren() {
        return super.getChildren();
    }

    /**
     * 渲染入口，接受鼠标坐标（屏幕坐标，scaled）和 delta。
     */
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // ensure size is up-to-date
        updateSizeFromWindow();

        // 绘制所有子元素（按添加顺序）
        for (UIElement child : getChildren()) {
            if (child != null && child.isVisible()) {
                child.render(context, mouseX, mouseY, delta);
            }
        }
    }

    /**
     * 渲染入口（不带鼠标坐标），会从 MinecraftClient 获取当前鼠标位置并转换为 scaled 坐标后调用上面的重载。
     */
    public void render(DrawContext context, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null)
            return;

        // 将鼠标原始像素坐标转换为 scaled 坐标
        double rawX = client.mouse.getX();
        double rawY = client.mouse.getY();
        int scaledMouseX = (int) (rawX * client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth());
        int scaledMouseY = (int) (rawY * client.getWindow().getScaledHeight()
                / (double) client.getWindow().getHeight());

        render(context, scaledMouseX, scaledMouseY, delta);
    }

    // --- 事件分发（按后添加优先） ---

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        java.util.List<UIElement> list = getChildren();
        for (int i = list.size() - 1; i >= 0; i--) {
            UIElement child = list.get(i);
            if (child == null || !child.isVisible())
                continue;
            if (child.containsPoint((int) mouseX, (int) mouseY)) {
                if (child.mouseClicked(mouseX, mouseY, button))
                    return true;
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        java.util.List<UIElement> list = getChildren();
        for (int i = list.size() - 1; i >= 0; i--) {
            UIElement child = list.get(i);
            if (child == null || !child.isVisible())
                continue;
            if (child.containsPoint((int) mouseX, (int) mouseY)) {
                if (child.mouseReleased(mouseX, mouseY, button))
                    return true;
            }
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        java.util.List<UIElement> list = getChildren();
        for (int i = list.size() - 1; i >= 0; i--) {
            UIElement child = list.get(i);
            if (child == null || !child.isVisible())
                continue;
            if (child.containsPoint((int) mouseX, (int) mouseY)) {
                if (child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
                    return true;
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        java.util.List<UIElement> list = getChildren();
        for (int i = list.size() - 1; i >= 0; i--) {
            UIElement child = list.get(i);
            if (child == null || !child.isVisible())
                continue;
            if (child.containsPoint((int) mouseX, (int) mouseY)) {
                if (child.mouseScrolled(mouseX, mouseY, amount))
                    return true;
            }
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        java.util.List<UIElement> list = getChildren();
        for (int i = list.size() - 1; i >= 0; i--) {
            UIElement child = list.get(i);
            if (child == null || !child.isVisible())
                continue;
            if (child.keyPressed(keyCode, scanCode, modifiers))
                return true;
        }
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        java.util.List<UIElement> list = getChildren();
        for (int i = list.size() - 1; i >= 0; i--) {
            UIElement child = list.get(i);
            if (child == null || !child.isVisible())
                continue;
            if (child.charTyped(codePoint, modifiers))
                return true;
        }
        return false;
    }

    // Tick lifecycle
    public void tick() {
        for (UIElement child : getChildren()) {
            if (child != null)
                child.tick();
        }
    }

    public void onRemoved() {
        for (UIElement child : getChildren()) {
            if (child != null)
                child.onRemoved();
        }
    }

}
