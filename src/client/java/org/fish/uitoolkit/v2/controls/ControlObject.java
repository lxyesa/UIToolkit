package org.fish.uitoolkit.v2.controls;

import net.minecraft.client.gui.DrawContext;
import org.fish.uitoolkit.v2.components.PanelComponent;
import org.fish.uitoolkit.v2.components.PositionComponent;
import org.fish.uitoolkit.v2.components.ScaleComponent;
import org.fish.uitoolkit.v2.interfaces.IComponent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Minimal ControlObject: can hold components and basic layout properties.
 */
public class ControlObject {
    private final Map<Class<?>, IComponent> components = new LinkedHashMap<>();
    private final List<IComponent> cachedComponents = new ArrayList<>();
    private boolean componentsDirty = true;
    protected boolean visible = true;
    private ControlObject parent = null;

    public ControlObject() {
        this.addComponent(new PositionComponent());
        this.addComponent(new ScaleComponent());
    }

    public IComponent addComponent(IComponent comp) {
        if (comp != null)
            comp.setOwner(this);
        components.put(comp.getClass(), comp);
        componentsDirty = true;
        return comp;
    }

    @SuppressWarnings("unchecked")
    public <T> T getComponent(Class<T> cls) {
        return (T) components.get(cls);
    }

    public void removeComponent(Class<?> cls) {
        components.remove(cls);
        componentsDirty = true;
    }

    /**
     * 组件更新缓存
     */
    private void ensureCachedComponents() {
        if (!componentsDirty)
            return;
        cachedComponents.clear();
        cachedComponents.addAll(components.values());
        cachedComponents.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
        componentsDirty = false;
    }

    public void update(float tickDelta) {
        ensureCachedComponents();
        for (int i = 0, n = cachedComponents.size(); i < n; i++) {
            IComponent c = cachedComponents.get(i);
            try {
                c.update(this, tickDelta);
            } catch (Throwable ignored) {
            }
        }
    }

    public void render(DrawContext context, float tickDelta) {
        if (!visible)
            return;
        ensureCachedComponents();
        for (int i = 0, n = cachedComponents.size(); i < n; i++) {
            IComponent c = cachedComponents.get(i);
            try {
                c.render(this, context, tickDelta);
            } catch (Throwable ignored) {
            }
        }
    }

    public boolean getVisible() {
        return this.visible;
    }

    public void setPosition(int x, int y) {
        PositionComponent p = getComponent(PositionComponent.class);
        if (p != null)
            p.setPosition(x, y);
    }

    public void setLocalPosition(int lx, int ly) {
        PositionComponent p = getComponent(PositionComponent.class);
        if (p != null)
            p.setLocalPosition(lx, ly);
    }

    /** Convenience: get the PositionComponent instance for this control. */
    public PositionComponent getPositionComponent() {
        return getComponent(PositionComponent.class);
    }

    // --- Shortcut mapping methods that delegate to PositionComponent ---
    public void setAnchor(PositionComponent.Anchor a) {
        PositionComponent p = getPositionComponent();
        if (p != null)
            p.setAnchor(a);
    }

    public void setParentAnchor(PositionComponent.Anchor a) {
        PositionComponent p = getPositionComponent();
        if (p != null)
            p.setParentAnchor(a);
    }

    public void setParentAnchorNormalized(float ax, float ay) {
        PositionComponent p = getPositionComponent();
        if (p != null)
            p.setParentAnchorNormalized(ax, ay);
    }

    public void clearParentAnchorNormalized() {
        PositionComponent p = getPositionComponent();
        if (p != null)
            p.clearParentAnchorNormalized();
    }

    public void setPivotNormalized(float px, float py) {
        PositionComponent p = getPositionComponent();
        if (p != null)
            p.setPivotNormalized(px, py);
    }

    public void clearPivotNormalized() {
        PositionComponent p = getPositionComponent();
        if (p != null)
            p.clearPivotNormalized();
    }

    public void setPivotPreset(PositionComponent.Anchor a) {
        PositionComponent p = getPositionComponent();
        if (p != null)
            p.setPivotPreset(a);
    }

    public void setParentAnchorPreset(PositionComponent.Anchor a) {
        PositionComponent p = getPositionComponent();
        if (p != null)
            p.setParentAnchorPreset(a);
    }

    public void setAlignment(PositionComponent.Anchor parentPreset, PositionComponent.Anchor pivotPreset) {
        PositionComponent p = getPositionComponent();
        if (p != null)
            p.setAlignment(parentPreset, pivotPreset);
    }

    public void setSize(int w, int h) {
        ScaleComponent s = getComponent(ScaleComponent.class);
        if (s != null)
            s.setSize(w, h);
    }

    public void setVisible(boolean v) {
        this.visible = v;
    }

    public int getX() {
        PositionComponent p = getComponent(PositionComponent.class);
        return p != null ? p.getAbsX() : 0;
    }

    public int getY() {
        PositionComponent p = getComponent(PositionComponent.class);
        return p != null ? p.getAbsY() : 0;
    }

    public int getWidth() {
        ScaleComponent s = getComponent(ScaleComponent.class);
        return s != null ? s.getWidth() : 0;
    }

    public int getHeight() {
        ScaleComponent s = getComponent(ScaleComponent.class);
        return s != null ? s.getHeight() : 0;
    }

    public void setOffset(int ox, int oy) {
        PositionComponent p = getComponent(PositionComponent.class);
        if (p != null)
            p.setOffset(ox, oy);
    }

    public void addOffset(int dx, int dy) {
        PositionComponent p = getComponent(PositionComponent.class);
        if (p != null)
            p.addOffset(dx, dy);
    }

    public void clearOffset() {
        PositionComponent p = getComponent(PositionComponent.class);
        if (p != null)
            p.clearOffset();
    }

    public ControlObject addChild(ControlObject child) {
        PanelComponent p = getComponent(PanelComponent.class);
        if (p == null)
            p = (PanelComponent) addComponent(new PanelComponent());
        p.addChild(child);
        if (child != null)
            child.setParent(this);

        return this;
    }

    public List<ControlObject> getChildren() {
        PanelComponent p = getComponent(PanelComponent.class);
        if (p == null)
            return List.of();
        return p.getChildren();
    }

    public ControlObject getParent() {
        return parent;
    }

    public void setParent(ControlObject p) {
        this.parent = p;
    }

    /**
     * 返回控件在屏幕坐标系中的中心 X（整型，使用当前绝对 X 和宽度计算）。
     */
    public int getCenterX() {
        return getX() + getWidth() / 2;
    }

    /**
     * 返回控件在屏幕坐标系中的中心 Y（整型，使用当前绝对 Y 和高度计算）。
     */
    public int getCenterY() {
        return getY() + getHeight() / 2;
    }

    /**
     * 返回中心坐标数组 [centerX, centerY]（方便一次性获取）。
     */
    public int[] getCenter() {
        return new int[] { getCenterX(), getCenterY() };
    }
}
