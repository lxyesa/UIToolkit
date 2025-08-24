package org.fish.uitoolkit.v2.controls;

import net.minecraft.client.gui.DrawContext;
import org.fish.uitoolkit.utils.TextureRegion;
import org.fish.uitoolkit.v2.Background;
import org.fish.uitoolkit.v2.components.PanelComponent;
import org.fish.uitoolkit.v2.components.PositionComponent;
import org.fish.uitoolkit.v2.components.ScaleComponent;
import org.fish.uitoolkit.v2.interfaces.IComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal ControlObject: can hold components and basic layout properties.
 */
public class ControlObject {
    private final Map<Class<?>, IComponent> components = new HashMap<>();

    protected boolean visible = true;
    // parent reference for anchor calculations
    private ControlObject parent = null;

    public ControlObject() {
        // ensure base components exist
        this.addComponent(new PositionComponent());
        this.addComponent(new ScaleComponent());
    }

    public <T extends IComponent> T addComponent(T comp) {
        components.put(comp.getClass(), comp);
        return comp;
    }

    @SuppressWarnings("unchecked")
    public <T extends IComponent> T getComponent(Class<T> cls) {
        return (T) components.get(cls);
    }

    public void removeComponent(Class<?> cls) {
        components.remove(cls);
    }

    public void update(float tickDelta) {
        // execute components in order of increasing priority
        components.values().stream()
                .sorted((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                .forEach(c -> {
                    try {
                        c.update(this, tickDelta);
                    } catch (Throwable ignored) {
                    }
                });
    }

    public void render(DrawContext context, float tickDelta) {
        if (!visible)
            return;
        // render components that have render implementations in priority order
        components.values().stream()
                .sorted((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                .forEach(c -> {
                    try {
                        c.render(this, context, tickDelta);
                    } catch (Throwable ignored) {
                    }
                });
    }

    // basic properties
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

    public void addChild(ControlObject child) {
        PanelComponent p = getComponent(PanelComponent.class);
        if (p == null)
            p = addComponent(new PanelComponent());
        p.addChild(child);
        if (child != null)
            child.setParent(this);
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

    public void setBackground(TextureRegion region) {
        Background bg = getComponent(Background.class);
        if (bg == null)
            bg = addComponent(new Background());
        bg.setTexture(region);
    }
}
