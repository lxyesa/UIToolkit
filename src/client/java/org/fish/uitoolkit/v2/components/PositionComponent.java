package org.fish.uitoolkit.v2.components;

import org.fish.uitoolkit.v2.controls.ControlObject;
import org.fish.uitoolkit.v2.interfaces.IComponent;

/**
 * Position component manages x/y and local offset for a ControlObject.
 *
 * New features added:
 * - normalized parent anchor and pivot support (0..1)
 * - convenience preset helpers to set parent anchor or pivot from the Anchor
 * enum
 *
 * Usage examples:
 * - setParentAnchorNormalized(0.5f, 1.0f); // parent bottom-center
 * - setPivotNormalized(0.5f, 0.5f); // child center pivot
 * - setAlignment(Anchor.BOTTOM_CENTER, Anchor.CENTER); // convenience preset
 */
public class PositionComponent extends IComponent {
    private int x = 0;
    private int y = 0;
    private int localX = 0;
    private int localY = 0;
    // computed absolute position after anchors are applied
    private int computedX = 0;
    private int computedY = 0;
    // coordinate offset: applied after all anchor/pivot computations
    // differs from localX/localY which are applied relative to parent anchor
    private int offsetX = 0;
    private int offsetY = 0;

    public enum Anchor {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        CENTER_LEFT, CENTER, CENTER_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    // this control's anchor point (pivot) and the parent's anchor point to align to
    // keep enum for presets but also allow normalized float anchors (0..1)
    private Anchor anchor = Anchor.TOP_LEFT;
    private Anchor parentAnchor = Anchor.TOP_LEFT;

    // normalized anchors (use when the corresponding "use*Normalized" is true)
    private float parentAnchorX = 0f, parentAnchorY = 0f;
    private boolean useParentAnchorNormalized = false;

    private float pivotX = 0f, pivotY = 0f;
    private boolean usePivotNormalized = false;

    public PositionComponent() {
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setLocalPosition(int lx, int ly) {
        this.localX = lx;
        this.localY = ly;
    }

    public void setAnchor(Anchor a) {
        if (a != null)
            this.anchor = a;
    }

    public Anchor getAnchor() {
        return this.anchor;
    }

    public void setParentAnchor(Anchor a) {
        if (a != null)
            this.parentAnchor = a;
    }

    public Anchor getParentAnchor() {
        return this.parentAnchor;
    }

    // normalized API (0..1)
    public void setParentAnchorNormalized(float ax, float ay) {
        this.parentAnchorX = ax;
        this.parentAnchorY = ay;
        this.useParentAnchorNormalized = true;
    }

    public void clearParentAnchorNormalized() {
        this.useParentAnchorNormalized = false;
    }

    public void setPivotNormalized(float px, float py) {
        this.pivotX = px;
        this.pivotY = py;
        this.usePivotNormalized = true;
    }

    public void clearPivotNormalized() {
        this.usePivotNormalized = false;
    }

    /**
     * Convenience: set this control's pivot using an Anchor preset (e.g. CENTER,
     * TOP_LEFT).
     * This sets the pivot as a normalized (0..1) value and enables normalized pivot
     * mode.
     */
    public void setPivotPreset(Anchor a) {
        if (a == null)
            return;
        float[] n = anchorEnumToNormalized(a);
        setPivotNormalized(n[0], n[1]);
    }

    /**
     * Convenience: set the parent anchor using an Anchor preset (e.g.
     * BOTTOM_CENTER).
     * This sets the parent anchor as a normalized (0..1) value and enables
     * normalized parent anchor mode.
     */
    public void setParentAnchorPreset(Anchor a) {
        if (a == null)
            return;
        float[] n = anchorEnumToNormalized(a);
        setParentAnchorNormalized(n[0], n[1]);
    }

    /**
     * Convenience: set both parent anchor and child pivot presets at once.
     */
    public void setAlignment(Anchor parentPreset, Anchor pivotPreset) {
        if (parentPreset != null)
            setParentAnchorPreset(parentPreset);
        if (pivotPreset != null)
            setPivotPreset(pivotPreset);
    }

    /**
     * Map Anchor enum to normalized coordinates (0..1) where (0,0)=top-left,
     * (1,1)=bottom-right.
     */
    private float[] anchorEnumToNormalized(Anchor a) {
        switch (a) {
            case TOP_LEFT:
                return new float[] { 0f, 0f };
            case TOP_CENTER:
                return new float[] { 0.5f, 0f };
            case TOP_RIGHT:
                return new float[] { 1f, 0f };
            case CENTER_LEFT:
                return new float[] { 0f, 0.5f };
            case CENTER:
                return new float[] { 0.5f, 0.5f };
            case CENTER_RIGHT:
                return new float[] { 1f, 0.5f };
            case BOTTOM_LEFT:
                return new float[] { 0f, 1f };
            case BOTTOM_CENTER:
                return new float[] { 0.5f, 1f };
            case BOTTOM_RIGHT:
                return new float[] { 1f, 1f };
            default:
                return new float[] { 0f, 0f };
        }
    }

    public int getAbsX() {
        return computedX;
    }

    public int getAbsY() {
        return computedY;
    }

    /**
     * Set a coordinate offset which will be added to the computed absolute
     * position after anchor/pivot alignment. This is applied on the final
     * coordinates and is not relative to the parent anchor (unlike
     * localPosition).
     */
    public void setOffset(int ox, int oy) {
        this.offsetX = ox;
        this.offsetY = oy;
    }

    /** Add a delta to the current offset (useful for nudging). */
    public void addOffset(int dx, int dy) {
        this.offsetX += dx;
        this.offsetY += dy;
    }

    /** Clear the offset (set to zero). */
    public void clearOffset() {
        this.offsetX = 0;
        this.offsetY = 0;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

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
        // prefer scaled sizes when available
        int parentW = parent.getWidth();
        int parentH = parent.getHeight();
        try {
            org.fish.uitoolkit.v2.components.ScaleComponent ps = parent
                    .getComponent(org.fish.uitoolkit.v2.components.ScaleComponent.class);
            if (ps != null) {
                int[] psz = ps.getScaledControlSize();
                parentW = psz[0];
                parentH = psz[1];
            }
        } catch (Throwable ignored) {
        }

        int[] parentPt;
        if (useParentAnchorNormalized) {
            int px = parentX + Math.round(parentAnchorX * parentW);
            int py = parentY + Math.round(parentAnchorY * parentH);
            parentPt = new int[] { px, py };
        } else {
            parentPt = anchorToPoint(parentAnchor, parentX, parentY, parentW, parentH);
        }

        int childW = owner.getWidth();
        int childH = owner.getHeight();
        try {
            org.fish.uitoolkit.v2.components.ScaleComponent cs = owner
                    .getComponent(org.fish.uitoolkit.v2.components.ScaleComponent.class);
            if (cs != null) {
                int[] csz = cs.getScaledControlSize();
                childW = csz[0];
                childH = csz[1];
            }
        } catch (Throwable ignored) {
        }
        int[] childOff;
        if (usePivotNormalized) {
            int ox = Math.round(pivotX * childW);
            int oy = Math.round(pivotY * childH);
            childOff = new int[] { ox, oy };
        } else {
            childOff = anchorOffset(anchor, childW, childH);
        }

        // align child anchor (pivot) to parent anchor, then apply base offsets
        computedX = parentPt[0] + baseX - childOff[0];
        computedY = parentPt[1] + baseY - childOff[1];

        // finally apply the coordinate offset (offset is applied to the final
        // absolute position, not relative to parent)
        computedX += offsetX;
        computedY += offsetY;
    }

    private int[] anchorToPoint(Anchor a, int px, int py, int pw, int ph) {
        switch (a) {
            case TOP_LEFT:
                return new int[] { px, py };
            case TOP_CENTER:
                return new int[] { px + pw / 2, py };
            case TOP_RIGHT:
                return new int[] { px + pw, py };
            case CENTER_LEFT:
                return new int[] { px, py + ph / 2 };
            case CENTER:
                return new int[] { px + pw / 2, py + ph / 2 };
            case CENTER_RIGHT:
                return new int[] { px + pw, py + ph / 2 };
            case BOTTOM_LEFT:
                return new int[] { px, py + ph };
            case BOTTOM_CENTER:
                return new int[] { px + pw / 2, py + ph };
            case BOTTOM_RIGHT:
                return new int[] { px + pw, py + ph };
            default:
                return new int[] { px, py };
        }
    }

    private int[] anchorOffset(Anchor a, int w, int h) {
        switch (a) {
            case TOP_LEFT:
                return new int[] { 0, 0 };
            case TOP_CENTER:
                return new int[] { w / 2, 0 };
            case TOP_RIGHT:
                return new int[] { w, 0 };
            case CENTER_LEFT:
                return new int[] { 0, h / 2 };
            case CENTER:
                return new int[] { w / 2, h / 2 };
            case CENTER_RIGHT:
                return new int[] { w, h / 2 };
            case BOTTOM_LEFT:
                return new int[] { 0, h };
            case BOTTOM_CENTER:
                return new int[] { w / 2, h };
            case BOTTOM_RIGHT:
                return new int[] { w, h };
            default:
                return new int[] { 0, 0 };
        }
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
