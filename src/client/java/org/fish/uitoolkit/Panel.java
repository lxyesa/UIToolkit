package org.fish.uitoolkit;

import org.fish.uitoolkit.utils.Regions;

/**
 * 简单的容器面板，作为一个可复用的 UI 容器。
 * <p>
 * Panel 管理子元素的添加/移除，并负责按顺序渲染子元素与分发输入事件。
 * 默认情况下渲染顺序为添加顺序；事件分发从后向前（后添加的元素位于上层并优先接收事件）。
 */
public class Panel extends Container {

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
}
