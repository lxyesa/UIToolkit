package org.examplea.uitoolkit.client;

import org.fish.uitoolkit.Canvas;
import org.fish.uitoolkit.Progress;
import org.fish.uitoolkit.UIElement;
import org.fish.uitoolkit.Control.ClipAxis;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * 简单的初始化测试：在客户端初始化时创建 Canvas/Panel/Label 并打印它们的位置与尺寸。
 */
public class UitoolkitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        final Canvas canvas = new Canvas();
        final Progress progress = new Progress(canvas, 0.5f, 1.0f);

        progress.setVerticalAnchor(UIElement.VAnchor.BOTTOM);
        progress.setHorizontalAnchor(UIElement.HAnchor.CENTER);
        progress.setBackgroundColor(255, 25, 25, 255);
        progress.setBackgroundBrightness(2.0f);
        progress.setMarginBottom(65);
        progress.setHeader("a Progress Bar");
        progress.setContentClip(true, 0.5f, ClipAxis.HORIZONTAL);

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            canvas.updateSizeFromWindow();
            canvas.render(context, tickDelta);
        });
    }
}
