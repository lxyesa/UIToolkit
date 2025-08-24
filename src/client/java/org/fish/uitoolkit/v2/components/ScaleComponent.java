package org.fish.uitoolkit.v2.components;

import org.fish.uitoolkit.v2.controls.ControlObject;
import org.fish.uitoolkit.v2.interfaces.IComponent;

/** Scale component now owns size and scale factor. */
public class ScaleComponent extends IComponent {
    private float scale = 1f;
    private int width = 0;
    private int height = 0;

    public ScaleComponent() {}

    public void setScale(float s) { if (s > 0) this.scale = s; }
    public float getScaleFactor() { return this.scale; }

    public void setSize(int w, int h) { this.width = w; this.height = h; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int[] getActualControlSize() {
        return new int[] { width, height };
    }

    public int[] getScaledControlSize() {
        int w = Math.round(width * scale);
        int h = Math.round(height * scale);
        return new int[] { w, h };
    }

    @Override
    public void update(ControlObject owner, float tickDelta) {
        // size adjustments could be applied here if needed
    }

    @Override
    public int getPriority() { return 5; } // sizing runs early
}

