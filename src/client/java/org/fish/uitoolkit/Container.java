package org.fish.uitoolkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.DrawContext;

/**
 * Container：支持子控件管理的控件基类（继承自 Control）。
 */
public class Container extends Control {

    private final List<UIElement> children = new ArrayList<>();
    private final List<UIElement> childrenView = Collections.unmodifiableList(children);
    /** 内边距（padding）——移动自 Panel，作为通用容器属性 */
    private int paddingLeft = 0;
    private int paddingRight = 0;
    private int paddingTop = 0;
    private int paddingBottom = 0;

    public Container() {
        super();
    }

    public Container(Object owner) {
        super(owner);
    }

    /**
     * 将一个子元素添加到容器。
     *
     * @param child 要添加的子元素（若为 null 则忽略）
     * @return 返回当前容器以便链式调用
     */
    public Container addChild(UIElement child) {
        if (child != null)
            children.add(child);
        // 新增：当有结构性变更（添加子节点）时，通知该子及其后代失效锚点缓存
        if (child instanceof Control) {
            ((Control) child).invalidateAnchorContext();
        }
        return this;
    }

    /**
     * 从容器中移除指定子元素。
     *
     * @param child 要移除的子元素
     * @return 如果列表中存在并被移除则返回 true，否则返回 false
     */
    public boolean removeChild(UIElement child) {
        boolean r = children.remove(child);
        if (r) {
            // 移除子项也可能影响布局：失效其后代缓存
            if (child instanceof Control) {
                ((Control) child).invalidateAnchorContext();
            }
        }
        return r;
    }

    /**
     * 清空所有子元素。
     */
    public void clearChildren() {
        children.clear();
    }

    /**
     * 获取当前子元素的不可变视图。
     *
     * @return 子元素列表的只读视图
     */
    public List<UIElement> getChildren() {
        return childrenView;
    }

    /** 设置内边距（左, 上, 右, 下） */
    public void setPadding(int left, int top, int right, int bottom) {
        this.paddingLeft = left;
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
        // padding 变化会影响子元素锚定参考区，通知子控件失效
        propagateInvalidateChildren();
    }

    /** 设置统一内边距 */
    public void setPadding(int pad) {
        setPadding(pad, pad, pad, pad);
    }

    /**
     * 递归通知所有子控件及其后代失效锚点缓存。
     * 当容器的位置/尺寸/内边距发生变化时应调用此方法。
     */
    protected void propagateInvalidateChildren() {
        for (UIElement child : children) {
            if (child instanceof Control) {
                ((Control) child).invalidateAnchorContext();
            }
            if (child instanceof Container) {
                ((Container) child).propagateInvalidateChildren();
            }
        }
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        propagateInvalidateChildren();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        propagateInvalidateChildren();
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public int getPaddingRight() {
        return paddingRight;
    }

    public int getPaddingTop() {
        return paddingTop;
    }

    public int getPaddingBottom() {
        return paddingBottom;
    }

    @Override
    public int getHeight() {
        if (height > 0)
            return height;
        return computeAutoHeight();
    }

    @Override
    public int getWidth() {
        if (width > 0)
            return width;
        return computeAutoWidth();
    }

    /**
     * 计算容器基于子元素的自动宽度（包括 padding）。
     */
    protected int computeAutoWidth() {
        List<UIElement> children = getChildren();
        if (children.isEmpty()) {
            return paddingLeft + paddingRight;
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        boolean any = false;
        for (UIElement child : children) {
            if (child == null || !child.isVisible())
                continue;
            any = true;
            int lx = child.getLocalX() - child.getMarginLeft();
            int rx = child.getLocalX() + child.getWidth() + child.getMarginRight();
            if (lx < minX)
                minX = lx;
            if (rx > maxX)
                maxX = rx;
        }
        if (!any)
            return paddingLeft + paddingRight;
        int contentWidth = maxX - minX;
        return contentWidth + paddingLeft + paddingRight;
    }

    /**
     * 计算容器基于子元素的自动高度（包括 padding）。
     */
    protected int computeAutoHeight() {
        List<UIElement> children = getChildren();
        if (children.isEmpty()) {
            return paddingTop + paddingBottom;
        }

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        boolean any = false;
        for (UIElement child : children) {
            if (child == null || !child.isVisible())
                continue;
            any = true;
            int ty = child.getLocalY() - child.getMarginTop();
            int by = child.getLocalY() + child.getHeight() + child.getMarginBottom();
            if (ty < minY)
                minY = ty;
            if (by > maxY)
                maxY = by;
        }
        if (!any)
            return paddingTop + paddingBottom;
        int contentHeight = maxY - minY;
        return contentHeight + paddingTop + paddingBottom;
    }

    /**
     * 当作为另一个 Control 的 owner 时，Control 会调用该方法通知本容器已接收子控件，
     * 默认实现是将子控件添加到 children 列表。
     *
     * @param child 新加入的子元素
     */
    @Override
    public void setChild(UIElement child) {
        addChild(child);
    }

    /**
     * 容器在 renderContent 中绘制其子元素，确保已由 Control 计算好的绝对位置与裁剪生效。
     */
    @Override
    protected void renderContent(DrawContext context, int absX, int absY, int mouseX, int mouseY, float delta) {
        for (UIElement child : children) {
            if (child != null && child.isVisible()) {
                child.render(context, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public boolean containsPoint(int px, int py) {
        int gx = getX();
        int gy = getY();
        return px >= gx && py >= gy && px < gx + getWidth() && py < gy + getHeight();
    }

    /**
     * 每帧更新容器与子控件（逻辑 tick）。
     */
    @Override
    public void tick() {
        if (!isVisible())
            return;
        for (UIElement child : children) {
            if (child != null)
                child.tick();
        }
    }

    /**
     * 当容器被移除时，转发移除通知到所有子控件以便它们进行清理。
     */
    @Override
    public void onRemoved() {
        for (UIElement child : children) {
            if (child != null)
                child.onRemoved();
        }
    }
}