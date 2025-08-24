package org.fish.uitoolkit.v2.interfaces;

import org.fish.uitoolkit.v2.controls.ControlObject;
import net.minecraft.client.gui.DrawContext;

/**
 * Minimal base component class for v2 UI framework.
 * Converted from interface to abstract class so components may optionally
 * store a reference to their owner control.
 */
public abstract class IComponent {
    // optional owner reference (set when the component is added to a ControlObject)
    private ControlObject owner;

    public ControlObject getOwner() { return owner; }

    public void setOwner(ControlObject owner) { this.owner = owner; }

    /** Called every frame to update component state. */
    public abstract void update(ControlObject owner, float tickDelta);

    /** Optional render step for components that need to draw. */
    public void render(ControlObject owner, DrawContext context, float tickDelta) {}

    /** Priority number: smaller values run earlier. Default 0. */
    public int getPriority() { return 0; }
}
