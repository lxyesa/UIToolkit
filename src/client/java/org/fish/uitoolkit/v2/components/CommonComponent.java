package org.fish.uitoolkit.v2.components;

import org.fish.uitoolkit.v2.controls.ControlObject;
import org.fish.uitoolkit.v2.interfaces.IComponent;

/**
 * @deprecated replaced by Position/Scale components. Kept as noop for compatibility.
 */
@Deprecated
public final class CommonComponent extends IComponent {
    @Override
    public void update(ControlObject owner, float tickDelta) {
        // intentionally no-op
    }

    @Override
    public int getPriority() { return Integer.MAX_VALUE; }
}
