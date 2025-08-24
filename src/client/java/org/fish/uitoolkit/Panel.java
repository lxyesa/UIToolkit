package org.fish.uitoolkit;

import org.fish.uitoolkit.utils.Regions;
import org.joml.Vector2d;

/**
 * 简单的容器面板，作为一个可复用的 UI 容器。
 * <p>
 * Panel 管理子元素的添加/移除，并负责按顺序渲染子元素与分发输入事件。
 * 默认情况下渲染顺序为添加顺序；事件分发从后向前（后添加的元素位于上层并优先接收事件）。
 */
public class Panel extends Container {

    enum PanelType {
        NONE,
        VERTICAL,
        HORIZONTAL
    }

    private PanelType panelType = PanelType.NONE;

    public Panel(Object owner, int x, int y, int width, int height) {
        super(owner);
        this.owner = owner;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.setBackground(Regions.WIDGET_PANEL);
    }

    public Panel(Object owner) {
        super(owner);
        this.owner = owner;

        this.setBackground(Regions.WIDGET_PANEL);
    }

    public void setPanelType(PanelType type) {
        this.panelType = type;
        // 重新布局子控件以适应新的面板类型
        layoutChildren();
    }

    @Override
    public void setScale(float scale) {
        // 确保在改变缩放后对子控件重新布局，避免读取到缩放前的尺寸
        super.setScale(scale);
        layoutChildren();
    }

    /**
     * 预测下一个控件的位置
     * 
     * @return
     */
    public Vector2d getNextControlPosition() {
        int nextX = getPaddingLeft();
        int nextY = getPaddingTop();

        for (UIElement child : getChildren()) {
            if (child == null || !child.isVisible())
                continue;
            int childRight = child.getLocalX() + child.getWidth();
            if (childRight > nextX) {
                nextX = childRight;
            }
        }

        return new Vector2d(nextX, nextY);
    }

    private void layoutChildren() {
        if (panelType == PanelType.NONE) {
            return;
        }
        this.clearChildrenPosition();
        for (UIElement child : getChildren()) {
            if (child == null || !child.isVisible())
                continue;
            if (panelType == PanelType.HORIZONTAL) {
                var childCon = (Control) child;
                childCon.setPosition((int) getNextControlPosition().x(), childCon.getLocalY());
            }
        }
    }

    @Override
    protected void onControlOrderInvalidated() {
        super.onControlOrderInvalidated();
        layoutChildren();
    }
}
