package org.fish.uitoolkit.v2;

/**
 * Bit flags for background rendering modes.
 * Usage example: RenderType.STRETCH | RenderType.REPEAT
 */
public final class RenderType {
    public static final int STRETCH = 1;
    public static final int REPEAT = 1 << 1;
    public static final int CLAMP = 1 << 2;
    public static final int NINESLICE = 1 << 3;

    private RenderType() {}
}
