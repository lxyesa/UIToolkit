package org.fish.uitoolkit;

import net.minecraft.client.gui.DrawContext;

/**
 * 通用的基本 UI 元素接口，面向 Fabric / Minecraft 的 GUI 渲染与输入回调。
 *
 * 设计原则：
 * - 最小契约：实现者需要提供渲染方法并可选择覆写输入事件与生命周期方法。
 * - 提供默认空实现以便快速实现简单组件。
 *
 * 参见 Fabric DrawContext
 * 文档：https://docs.fabricmc.net/1.20.4/develop/rendering/draw-context
 */
public interface UIElement {

    /** 水平锚点 */
    enum HAnchor {
        LEFT, CENTER, RIGHT
    }

    /** 垂直锚点 */
    enum VAnchor {
        TOP, MIDDLE, BOTTOM
    }

    /** 返回该元素所属的 owner（例如一个 screen 或 manager）。 */
    Object getOwner();

    /**
     * 绘制方法。必须实现。
     *
     * @param context DrawContext 用于绘制
     * @param mouseX  当前鼠标 x（相对于窗口）
     * @param mouseY  当前鼠标 y（相对于窗口）
     * @param delta   部分帧时间，用于平滑动画
     */
    void render(DrawContext context, int mouseX, int mouseY, float delta);

    /**
     * 重载的绘制方法（不包含鼠标坐标）。默认实现会将鼠标坐标设为元素的左上角坐标，以便实现者可以只实现带坐标的方法。
     * 实现者也可以覆写此方法以提供不同的行为。
     *
     * @param context DrawContext 用于绘制
     * @param delta   部分帧时间，用于平滑动画
     */
    default void render(DrawContext context, float delta) {
        render(context, getX(), getY(), delta);
    }

    // --- 常用输入事件（均提供默认 no-op / false 返回，以便实现者按需覆写） ---

    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    // --- 布局 / 可见性 / 尺寸（默认值为 0 / 可见），实现者可覆盖以返回真实值 ---

    default int getX() {
    // 默认实现：使用更高效的 getAnchoredX，避免每帧分配临时数组
    int parentX = getParentX();
    int parentY = getParentY();
    int parentW = getParentWidth();
    int parentH = getParentHeight();
    return getAnchoredX(parentX, parentY, parentW, parentH) + getLocalX();
    }

    default int getY() {
    int parentX = getParentX();
    int parentY = getParentY();
    int parentW = getParentWidth();
    int parentH = getParentHeight();
    return getAnchoredY(parentX, parentY, parentW, parentH) + getLocalY();
    }

    /**
     * 元素在父容器内的本地偏移（默认 0）。实现类可以覆盖以返回各自的 x/y 字段。
     */
    default int getLocalX() {
        return 0;
    }

    /**
     * 元素在父容器内的本地偏移（默认 0）。实现类可以覆盖以返回各自的 x/y 字段。
     */
    default int getLocalY() {
        return 0;
    }

    /**
     * 以下方法用于从 owner 推断父容器的 origin/size，默认支持 Canvas 与 Panel。
     */
    default int getParentX() {
        Object owner = getOwner();
        if (owner instanceof Canvas)
            return ((Canvas) owner).getContentX();
        if (owner instanceof Panel)
            return ((Panel) owner).getX();
        return 0;
    }

    default int getParentY() {
        Object owner = getOwner();
        if (owner instanceof Canvas)
            return ((Canvas) owner).getContentY();
        if (owner instanceof Panel)
            return ((Panel) owner).getY();
        return 0;
    }

    default int getParentWidth() {
        Object owner = getOwner();
        if (owner instanceof Canvas)
            return ((Canvas) owner).getContentWidth();
        if (owner instanceof Panel)
            return ((Panel) owner).getWidth();
        return 0;
    }

    default int getParentHeight() {
        Object owner = getOwner();
        if (owner instanceof Canvas)
            return ((Canvas) owner).getContentHeight();
        if (owner instanceof Panel)
            return ((Panel) owner).getHeight();
        return 0;
    }

    default int getWidth() {
        return 0;
    }

    default int getHeight() {
        return 0;
    }

    default boolean isVisible() {
        return true;
    }

    /**
     * 返回水平锚点（默认 LEFT）。
     * 锚点决定元素如何相对于父容器定位。
     */
    default HAnchor getHorizontalAnchor() {
        return HAnchor.LEFT;
    }

    /**
     * 返回垂直锚点（默认 TOP）。
     */
    default VAnchor getVerticalAnchor() {
        return VAnchor.TOP;
    }

    /** 返回左侧 margin（默认 0）。 */
    default int getMarginLeft() {
        return 0;
    }

    /** 返回右侧 margin（默认 0）。 */
    default int getMarginRight() {
        return 0;
    }

    /** 返回上方 margin（默认 0）。 */
    default int getMarginTop() {
        return 0;
    }

    /** 返回下方 margin（默认 0）。 */
    default int getMarginBottom() {
        return 0;
    }

    /**
     * 计算锚定位置（相对于父容器 origin 和 size），返回长度为2的数组 [x,y]。
     * 默认实现使用元素的
     * getWidth/getHeight/getHorizontalAnchor/getVerticalAnchor/getMargin*。
     */
    /**
     * 兼容方法：返回 [x,y]，内部委托到高效的 getAnchoredX/Y。
     */
    default int[] computeAnchoredPosition(int parentX, int parentY, int parentWidth, int parentHeight) {
        return new int[] { getAnchoredX(parentX, parentY, parentWidth, parentHeight),
                getAnchoredY(parentX, parentY, parentWidth, parentHeight) };
    }

    /**
     * 更高效的单值锚点计算：返回相对于父容器 origin 的 X 坐标（不包含 getLocalX）。
     */
    default int getAnchoredX(int parentX, int parentY, int parentWidth, int parentHeight) {
        int w = getWidth();
        int x;
        switch (getHorizontalAnchor()) {
            case CENTER:
                x = parentX + (parentWidth - w) / 2 + getMarginLeft() - getMarginRight();
                break;
            case RIGHT:
                x = parentX + parentWidth - w - getMarginRight();
                break;
            case LEFT:
            default:
                x = parentX + getMarginLeft();
                break;
        }
        return x;
    }

    /**
     * 更高效的单值锚点计算：返回相对于父容器 origin 的 Y 坐标（不包含 getLocalY）。
     */
    default int getAnchoredY(int parentX, int parentY, int parentWidth, int parentHeight) {
        int h = getHeight();
        int y;
        switch (getVerticalAnchor()) {
            case MIDDLE:
                y = parentY + (parentHeight - h) / 2 + getMarginTop() - getMarginBottom();
                break;
            case BOTTOM:
                y = parentY + parentHeight - h - getMarginBottom();
                break;
            case TOP:
            default:
                y = parentY + getMarginTop();
                break;
        }
        return y;
    }

    /** Convenience: 判断指定点是否在元素区域内。 */
    default boolean containsPoint(int x, int y) {
        return x >= getX() && y >= getY() && x < getX() + getWidth() && y < getY() + getHeight();
    }

    // --- 生命周期 / 节拍 ---

    /** 每个客户端 tick 调用一次（可选实现）。 */
    default void tick() {
    }

    /** 当元素从界面移除时调用（可选实现）。 */
    default void onRemoved() {
    }

    /**
     * 初始化钩子：在控件创建或首次加入界面时调用，便于实现者在此构建或添加子控件。
     * 示例用途：UserControl 可以在此方法中 addChild(...) 创建内部元素。
     * 默认实现为空，子类可覆盖。
     */
    default void initialize() {
    }

}
