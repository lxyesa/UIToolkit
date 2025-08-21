package org.examplea.uitoolkit.client;

import org.fish.uitoolkit.Canvas;
import org.fish.uitoolkit.Panel;
import org.fish.uitoolkit.UIElement;
import org.fish.uitoolkit.UIElement.HAnchor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * 简单的初始化测试：在客户端初始化时创建 Canvas/Panel/Label 并打印它们的位置与尺寸。
 */
public class UitoolkitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // final Canvas canvas = new Canvas();
        // final Panel panel = new Panel(canvas);

        // panel.setHorizontalAnchor(HAnchor.CENTER);
        // panel.setVerticalAnchor(UIElement.VAnchor.BOTTOM);
        // panel.setSize(46, 16);
        // panel.setMarginBottom(45);
        // panel.setMarginRight(68);

        // HudRenderCallback.EVENT.register((context, tickDelta) -> {
        //     canvas.updateSizeFromWindow();
        //     canvas.render(context, tickDelta);
        // });
    }
}
