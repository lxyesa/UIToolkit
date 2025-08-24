package org.fish.uitoolkit.v2.interfaces;

import org.fish.uitoolkit.v2.controls.ControlObject;

import net.minecraft.client.gui.DrawContext;

/**
 * Minimal component interface for v2 UI framework.
 * Components may participate in update and render phases and have a numeric priority.
 * Lower priority value is executed earlier (0 is highest priority).
 */
public interface IComponent {
    /** Called every frame to update component state. */
    void update(ControlObject owner, float tickDelta);

    /**
     * Optional render step for components that need to draw.
     */
    default void render(ControlObject owner, DrawContext context, float tickDelta) {}

    /**
     * Priority number: smaller values run earlier. Default 0.
     */
    default int getPriority() { return 0; }
}
