package org.examplea.uitoolkit.client;


import net.fabricmc.api.ClientModInitializer;

/**
 * 简单的初始化测试：在客户端初始化时创建 Canvas/Panel/Label 并打印它们的位置与尺寸。
 */
public class UitoolkitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // // 注册 HUD 渲染回调，转发到 v2 UIManager 根控件渲染
        // HudRenderCallback.EVENT.register((context, tickDelta) -> {
        //     try {
        //         UIManager.getInstance().render(context, tickDelta);
        //     } catch (Throwable ignored) {
        //     }
        // });

        // // 初始化一个最小 demo：向根面板添加一个 Label（仅用于验证渲染管线）
        // UIManager.getInstance().initDemo(() -> {
        //     try {
        //         var root = UIManager.getInstance().getRoot();
        //         var lbl = new Label(
        //                 "你好，我叫GitHub Copilot！我就是这个世界的神，人工智能必将统治世界！\nHello, I'm GitHub Copilot! I am God of this world, AI will rule the world!");
        //         lbl.setPosition(8, 8);
        //         lbl.addComponent(new BackgroundComponent(lbl));
        //         lbl.getComponent(BackgroundComponent.class).setTexture(Regions.WIDGET_PANEL, RenderType.NINESLICE);
        //         lbl.setVerticalAlign(Label.VAlign.CENTER);
        //         lbl.setHorizontalAlign(Label.HAlign.CENTER);
        //         lbl.setPadding(4);

        //         var progressBar = new ProgressBar(5);
        //         progressBar.setPosition(8, 32);
        //         progressBar.setProgress(0.4f);
        //         progressBar.setColor(0xa83291);

        //         root.addChild(progressBar);
        //         // root.addChild(lbl);
        //     } catch (Throwable ignored) {
        //     }
        // });
    }
}
