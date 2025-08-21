package org.examplea.uitoolkit.client;

import org.fish.uitoolkit.Canvas;
import org.fish.uitoolkit.Panel;
import org.fish.uitoolkit.Progress;
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
        final Progress progress = new Progress(canvas, 0.0f, 1.0f, 1.5f);

        progress.setMargins(15);

        label.setColor(0xFF00FF00); // 设置文本颜色为绿色
        label.setMargins(10); // 设置文本边距

        panel.setHorizontalAnchor(UIElement.HAnchor.CENTER);
        panel.setNineSliceMode(NineSliceScaleMode.MINIMUM);
        panel.setNineSliceMinPx(6);
        panel.setBackground(org.fish.uitoolkit.utils.Regions.WIDGET_PANEL);

        panel.addChild(label);

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            canvas.updateSizeFromWindow();
            // 动态测试：让 progressFill 的 clip 在 0..1 范围内平滑往返
            double periodMs = 3000.0; // 周期 3 秒
            double phase = (System.currentTimeMillis() % (long) periodMs) / periodMs; // 0..1
            float clip = (float) (0.5 * (1.0 + Math.sin(2.0 * Math.PI * phase)));
            progress.setValue(clip);
            canvas.render(context, tickDelta);
        });
    }
}
