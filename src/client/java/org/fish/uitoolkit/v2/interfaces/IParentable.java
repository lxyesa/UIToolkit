package org.fish.uitoolkit.v2.interfaces;

import org.fish.uitoolkit.v2.controls.ControlObject;

public interface IParentable {
    ControlObject getParent();
    void setParent(ControlObject p);
}
