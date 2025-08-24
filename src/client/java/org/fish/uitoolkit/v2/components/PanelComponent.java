package org.fish.uitoolkit.v2.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fish.uitoolkit.v2.controls.ControlObject;
import org.fish.uitoolkit.v2.interfaces.IComponent;

public class PanelComponent implements IComponent {
    private final List<ControlObject> children = new ArrayList<>();

    public PanelComponent() {}

    public void addChild(ControlObject c) {
        if (c == null) return;
        children.add(c);
    }

    public void removeChild(ControlObject c) {
        if (children.remove(c)) {
            if (c != null) c.setParent(null);
        }
    }

    public List<ControlObject> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void update(ControlObject owner, float tickDelta) {
        for (ControlObject c : children) {
            try { c.update(tickDelta); } catch (Throwable ignored) {}
        }
    }

    @Override
    public void render(ControlObject owner, net.minecraft.client.gui.DrawContext context, float tickDelta) {
        for (ControlObject c : children) {
            try { c.render(context, tickDelta); } catch (Throwable ignored) {}
        }
    }

    @Override
    public int getPriority() { return 50; } // render after background (background default priority 0)
}
