package org.fish.uitoolkit.v2.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fish.uitoolkit.v2.controls.ControlObject;
import org.fish.uitoolkit.v2.interfaces.IComponent;

public class PanelComponent extends IComponent {
    private final List<ControlObject> children = new ArrayList<>();

    public enum Orientation {
        VERTICAL, HORIZONTAL
    }

    public enum Align {
        START, CENTER, END
    }

    private Orientation orientation = Orientation.VERTICAL;
    private Align crossAlign = Align.START; // alignment on the cross axis
    private int spacing = 2; // pixels between children
    private int paddingLeft = 0, paddingTop = 0, paddingRight = 0, paddingBottom = 0;
    private boolean autoSizeWidth = false;
    private boolean autoSizeHeight = false;

    public PanelComponent() {
    }

    public void addChild(ControlObject c) {
        if (c == null)
            return;
        children.add(c);
        // if this panel is already attached to a ControlObject, set the child's parent
        ControlObject owner = getOwner();
        if (owner != null) {
            c.setParent(owner);
        }
    }

    public void removeChild(ControlObject c) {
        if (children.remove(c)) {
            if (c != null)
                c.setParent(null);
        }
    }

    @Override
    public void setOwner(ControlObject owner) {
        super.setOwner(owner);
        // ensure existing children have their parent set to the new owner
        if (owner != null) {
            for (ControlObject c : children) {
                if (c != null)
                    c.setParent(owner);
            }
        }
    }

    public List<ControlObject> getChildren() {
        return Collections.unmodifiableList(children);
    }

    // layout configuration
    public void setOrientation(Orientation o) {
        if (o != null)
            this.orientation = o;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setCrossAlign(Align a) {
        if (a != null)
            this.crossAlign = a;
    }

    public Align getCrossAlign() {
        return crossAlign;
    }

    public void setSpacing(int s) {
        this.spacing = Math.max(0, s);
    }

    public int getSpacing() {
        return spacing;
    }

    public void setPadding(int left, int top, int right, int bottom) {
        this.paddingLeft = left;
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
    }

    public void setAutoSizeWidth(boolean v) {
        this.autoSizeWidth = v;
    }

    public boolean isAutoSizeWidth() {
        return this.autoSizeWidth;
    }

    public void setAutoSizeHeight(boolean v) {
        this.autoSizeHeight = v;
    }

    public boolean isAutoSizeHeight() {
        return this.autoSizeHeight;
    }

    @Override
    public void update(ControlObject owner, float tickDelta) {
        // perform simple layout of children relative to the owner control, then update
        // them
        if (owner != null) {
            // compute required size based on children when autosize is enabled
            int requiredW = owner.getWidth();
            int requiredH = owner.getHeight();
            if (autoSizeWidth || autoSizeHeight) {
                if (orientation == Orientation.VERTICAL) {
                    int totalH = paddingTop + paddingBottom;
                    int maxW = 0;
                    int count = 0;
                    for (ControlObject c : children) {
                        if (c == null)
                            continue;
                        totalH += c.getHeight();
                        maxW = Math.max(maxW, c.getWidth());
                        count++;
                    }
                    if (count > 1)
                        totalH += spacing * (count - 1);
                    if (autoSizeWidth)
                        requiredW = paddingLeft + paddingRight + maxW;
                    if (autoSizeHeight)
                        requiredH = totalH;
                } else { // HORIZONTAL
                    int totalW = paddingLeft + paddingRight;
                    int maxH = 0;
                    int count = 0;
                    for (ControlObject c : children) {
                        if (c == null)
                            continue;
                        totalW += c.getWidth();
                        maxH = Math.max(maxH, c.getHeight());
                        count++;
                    }
                    if (count > 1)
                        totalW += spacing * (count - 1);
                    if (autoSizeWidth)
                        requiredW = totalW;
                    if (autoSizeHeight)
                        requiredH = paddingTop + paddingBottom + maxH;
                }
                // apply the computed size to owner
                try {
                    owner.setSize(requiredW, requiredH);
                } catch (Throwable ignored) {
                }
            }
            if (orientation == Orientation.VERTICAL) {
                int y = paddingTop;
                int availW = owner.getWidth() - paddingLeft - paddingRight;
                for (ControlObject c : children) {
                    if (c == null)
                        continue;
                    int cw = c.getWidth();
                    int cx;
                    switch (crossAlign) {
                        case CENTER:
                            cx = paddingLeft + (availW - cw) / 2;
                            break;
                        case END:
                            cx = paddingLeft + Math.max(0, availW - cw);
                            break;
                        default:
                            cx = paddingLeft;
                            break;
                    }
                    c.setLocalPosition(cx, y);
                    y += c.getHeight() + spacing;
                }
            } else { // HORIZONTAL
                int x = paddingLeft;
                int availH = owner.getHeight() - paddingTop - paddingBottom;
                for (ControlObject c : children) {
                    if (c == null)
                        continue;
                    int ch = c.getHeight();
                    int cy;
                    switch (crossAlign) {
                        case CENTER:
                            cy = paddingTop + (availH - ch) / 2;
                            break;
                        case END:
                            cy = paddingTop + Math.max(0, availH - ch);
                            break;
                        default:
                            cy = paddingTop;
                            break;
                    }
                    c.setLocalPosition(x, cy);
                    x += c.getWidth() + spacing;
                }
            }
        }

        for (ControlObject c : children) {
            try {
                if (c != null)
                    c.update(tickDelta);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void render(ControlObject owner, net.minecraft.client.gui.DrawContext context, float tickDelta) {
        for (ControlObject c : children) {
            try {
                if (c != null)
                    c.render(context, tickDelta);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public int getPriority() {
        return 50;
    }
}
