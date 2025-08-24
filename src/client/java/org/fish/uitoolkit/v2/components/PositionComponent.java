package org.fish.uitoolkit.v2.components;

import org.fish.uitoolkit.v2.controls.ControlObject;
import org.fish.uitoolkit.v2.interfaces.IComponent;

/**
 * Position component manages x/y and local offset for a ControlObject.
 */
public class PositionComponent extends IComponent {
    private int x = 0;
    private int y = 0;
    private int localX = 0;
    private int localY = 0;
    // computed absolute position after anchors are applied
    private int computedX = 0;
    private int computedY = 0;

    public enum Anchor {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        CENTER_LEFT, CENTER, CENTER_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    // this control's anchor point and the parent's anchor point to align to
    private Anchor anchor = Anchor.TOP_LEFT;
    private Anchor parentAnchor = Anchor.TOP_LEFT;

    public PositionComponent() {}

    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setLocalPosition(int lx, int ly) { this.localX = lx; this.localY = ly; }

    public void setAnchor(Anchor a) { if (a != null) this.anchor = a; }
    public Anchor getAnchor() { return this.anchor; }

    public void setParentAnchor(Anchor a) { if (a != null) this.parentAnchor = a; }
    public Anchor getParentAnchor() { return this.parentAnchor; }

    public int getAbsX() { return computedX; }
    public int getAbsY() { return computedY; }

    @Override
    public void update(ControlObject owner, float tickDelta) {
        // compute final absolute position, taking anchors and parent into account
    ControlObject parent = owner.getParent();
        int baseX = x + localX;
        int baseY = y + localY;

        if (parent == null) {
            computedX = baseX;
            computedY = baseY;
            return;
        }

        int parentX = parent.getX();
        int parentY = parent.getY();
        int parentW = parent.getWidth();
        int parentH = parent.getHeight();

        int[] parentPt = anchorToPoint(parentAnchor, parentX, parentY, parentW, parentH);

        int childW = owner.getWidth();
        int childH = owner.getHeight();
        int[] childOff = anchorOffset(anchor, childW, childH);

        // align child anchor to parent anchor, then apply base offsets
        computedX = parentPt[0] + baseX - childOff[0];
        computedY = parentPt[1] + baseY - childOff[1];
    }

    private int[] anchorToPoint(Anchor a, int px, int py, int pw, int ph) {
        switch (a) {
            case TOP_LEFT: return new int[] { px, py };
            case TOP_CENTER: return new int[] { px + pw/2, py };
            case TOP_RIGHT: return new int[] { px + pw, py };
            case CENTER_LEFT: return new int[] { px, py + ph/2 };
            case CENTER: return new int[] { px + pw/2, py + ph/2 };
            case CENTER_RIGHT: return new int[] { px + pw, py + ph/2 };
            case BOTTOM_LEFT: return new int[] { px, py + ph };
            case BOTTOM_CENTER: return new int[] { px + pw/2, py + ph };
            case BOTTOM_RIGHT: return new int[] { px + pw, py + ph };
            default: return new int[] { px, py };
        }
    }

    private int[] anchorOffset(Anchor a, int w, int h) {
        switch (a) {
            case TOP_LEFT: return new int[] { 0, 0 };
            case TOP_CENTER: return new int[] { w/2, 0 };
            case TOP_RIGHT: return new int[] { w, 0 };
            case CENTER_LEFT: return new int[] { 0, h/2 };
            case CENTER: return new int[] { w/2, h/2 };
            case CENTER_RIGHT: return new int[] { w, h/2 };
            case BOTTOM_LEFT: return new int[] { 0, h };
            case BOTTOM_CENTER: return new int[] { w/2, h };
            case BOTTOM_RIGHT: return new int[] { w, h };
            default: return new int[] { 0, 0 };
        }
    }

    @Override
    public int getPriority() { return 10; } // position should run after sizing but before background/render
}
