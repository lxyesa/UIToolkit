package org.fish.uitoolkit.v2;

import org.fish.uitoolkit.v2.controls.ControlObject;

import net.minecraft.client.gui.DrawContext;

/** Minimal UIManager that holds a root ControlObject and renders it. */
public class UIManager {
    private final ControlObject root = new ControlObject();

    /**
     * 构造一个新的 UIManager 实例（不再使用单例模式）。
     */
    public UIManager() {
        root.setPosition(0, 0);
        root.setSize(320, 240);
    }

    public ControlObject getRoot() {
        return root;
    }

    public void initDemo(Runnable r) {
        if (r != null)
            r.run();
    }

    public void render(DrawContext context, float tickDelta) {
        // ensure root matches current game window size so UI follows the game window
        try {
            int w = context.getScaledWindowWidth();
            int h = context.getScaledWindowHeight();
            root.setSize(w, h);
            root.setPosition(0, 0);
        } catch (Throwable ignored) {
        }

        root.update(tickDelta);
        root.render(context, tickDelta);
    }
}
