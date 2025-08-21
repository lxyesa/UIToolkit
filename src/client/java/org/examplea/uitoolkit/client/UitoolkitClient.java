package org.examplea.uitoolkit.client;

import org.fish.uitoolkit.Canvas;
import org.fish.uitoolkit.Panel;
import org.fish.uitoolkit.UIElement;
import org.fish.uitoolkit.Control.NineSliceScaleMode;
import org.fish.uitoolkit.Label;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * 简单的初始化测试：在客户端初始化时创建 Canvas/Panel/Label 并打印它们的位置与尺寸。
 */
public class UitoolkitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        final Canvas canvas = new Canvas();
        final Panel panel = new Panel(canvas);
        final Label label = new Label(panel, "Hello, UIToolkit!");
        label.setColor(0xFF00FF00); // 设置文本颜色为绿色
        label.setMargins(10); // 设置文本边距

        panel.setHorizontalAnchor(UIElement.HAnchor.CENTER);
        panel.setNineSliceMode(NineSliceScaleMode.MINIMUM);
        panel.setNineSliceMinPx(6);

        panel.addChild(label);

        try {
            panel.setBackground(org.fish.uitoolkit.utils.Regions.WIDGET_PANEL);
        } catch (Throwable ignored) {
        }

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            canvas.updateSizeFromWindow();
            canvas.render(context, tickDelta);
        });
    }
}
