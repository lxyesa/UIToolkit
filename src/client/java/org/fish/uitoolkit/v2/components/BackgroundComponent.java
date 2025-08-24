package org.fish.uitoolkit.v2.components;

import net.minecraft.client.gui.DrawContext;
import com.mojang.blaze3d.systems.RenderSystem;
import org.fish.uitoolkit.utils.TextureRegion;
import org.fish.uitoolkit.v2.RenderType;
import org.fish.uitoolkit.v2.controls.ControlObject;
import org.fish.uitoolkit.v2.interfaces.IComponent;

/** Minimal background component that can draw a TextureRegion. */
public class BackgroundComponent extends IComponent {
    private TextureRegion region;
    private int renderFlags = RenderType.STRETCH;
    private int tintColor = 0xFFFFFF;
    // clip state
    private boolean clipEnabled = false;
    private float clipXPercent = 0f;
    private float clipYPercent = 0f;
    private ClipType clipType = ClipType.FORWARD;

    public enum ClipType {
        FORWARD, BACKWARD
    }

    public BackgroundComponent(ControlObject owner) {
        this.setOwner(owner);
    }

    public IComponent setTexture(TextureRegion region, int renderFlags) {
        this.region = region;
        this.renderFlags = renderFlags;
        return this;
    }

    public IComponent setTexture(TextureRegion region) {
        setTexture(region, RenderType.STRETCH);
        return this;
    }

    /** ARGB color (0xRRGGBB) */
    public IComponent setColor(int color) {
        this.tintColor = color & 0xFFFFFF;
        return this;
    }

    public IComponent setAlpha(float a) {
        if (region != null)
            region.setAlpha(a);
        return this;
    }

    /**
     * 按百分比沿轴裁剪背景渲染。最小实现：仅记录裁剪，实际的剪裁操作由调用者或后续改进处理。目前仅根据百分比简单缩小尺寸。
     */
    public IComponent clip(float xPercent, float yPercent, ClipType clipType) {
        // 存储裁剪信息，renderBackground 在渲染时应用 scissor
        this.clipEnabled = true;
        this.clipXPercent = Math.max(0f, Math.min(1f, xPercent));
        this.clipYPercent = Math.max(0f, Math.min(1f, yPercent));
        this.clipType = clipType == null ? ClipType.FORWARD : clipType;
        return this;
    }

    /**
     * 更新背景组件的拥有者大小
     */
    public void updateOwnerSize() {
        if (region == null)
            return;
        ControlObject o = getOwner();
        if (o == null)
            throw new IllegalStateException("Owner is not set");
        o.setSize(region.getW(), region.getH());
    }

    private void renderBackground(DrawContext context, ControlObject owner) {
        if (region == null)
            return;
        if (!owner.getVisible())
            return;
        int x = owner.getX();
        int y = owner.getY();
        int w = owner.getWidth();
        int h = owner.getHeight();
        boolean scissored = false;
        try {
            // apply tint by setting shader color on context and ensure blending is enabled
            float r = ((tintColor >> 16) & 0xFF) / 255f;
            float g = ((tintColor >> 8) & 0xFF) / 255f;
            float b = (tintColor & 0xFF) / 255f;
            context.setShaderColor(r, g, b, region.getAlpha());
            // ensure correct blending state so semi-transparent textures render properly
            try {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            } catch (Throwable ignored) {
            }
            // apply scissor if requested
            if (this.clipEnabled && (this.clipXPercent > 0f || this.clipYPercent > 0f)) {
                int clipW = Math.max(0, Math.round(w * this.clipXPercent));
                int clipH = Math.max(0, Math.round(h * this.clipYPercent));
                int clipX = x;
                int clipY = y;
                // horizontal handling
                if (this.clipXPercent > 0f) {
                    if (this.clipType == ClipType.FORWARD) {
                        clipX = x;
                    } else {
                        clipX = x + Math.max(0, w - clipW);
                    }
                }
                // vertical handling
                if (this.clipYPercent > 0f) {
                    if (this.clipType == ClipType.FORWARD) {
                        clipY = y;
                    } else {
                        clipY = y + Math.max(0, h - clipH);
                    }
                }
                int clipRectW = this.clipXPercent > 0f ? clipW : w;
                int clipRectH = this.clipYPercent > 0f ? clipH : h;
                // enable scissor (DrawContext expects x1,y1,x2,y2)
                context.enableScissor(clipX, clipY, clipX + clipRectW, clipY + clipRectH);
                scissored = true;
            }

            if ((renderFlags & RenderType.NINESLICE) != 0 && region.hasInsets()) {
                // Fallback to manual nine-slice rendering using drawTexture so we don't
                // depend on a specific DrawContext.drawNineSlicedTexture signature.
                int left = region.getInsetLeft();
                int top = region.getInsetTop();
                int right = region.getInsetRight();
                int bottom = region.getInsetBottom();

                int srcTotalW = region.getW();
                int srcTotalH = region.getH();

                // destination (on-screen) sizes for corners/edges, clamp to available size
                int dstLeft = Math.min(left, w);
                int dstTop = Math.min(top, h);
                int dstRight = Math.min(right, Math.max(0, w - dstLeft));
                int dstBottom = Math.min(bottom, Math.max(0, h - dstTop));

                int dstCenterW = Math.max(0, w - dstLeft - dstRight);
                int dstCenterH = Math.max(0, h - dstTop - dstBottom);

                int sx0 = region.getU();
                int sy0 = region.getV();
                int sx1 = sx0 + left;
                int sx2 = sx0 + srcTotalW - right;
                int sy1 = sy0 + top;
                int sy2 = sy0 + srcTotalH - bottom;

                // corners
                if (dstLeft > 0 && dstTop > 0)
                    context.drawTexture(region.getIdentifier(), x, y, dstLeft, dstTop, sx0, sy0, left, top,
                            region.getTextureWidth(), region.getTextureHeight());
                if (dstRight > 0 && dstTop > 0)
                    context.drawTexture(region.getIdentifier(), x + dstLeft + dstCenterW, y, dstRight, dstTop, sx2, sy0,
                            right, top, region.getTextureWidth(), region.getTextureHeight());
                if (dstLeft > 0 && dstBottom > 0)
                    context.drawTexture(region.getIdentifier(), x, y + dstTop + dstCenterH, dstLeft, dstBottom, sx0,
                            sy2,
                            left, bottom, region.getTextureWidth(), region.getTextureHeight());
                if (dstRight > 0 && dstBottom > 0)
                    context.drawTexture(region.getIdentifier(), x + dstLeft + dstCenterW, y + dstTop + dstCenterH,
                            dstRight,
                            dstBottom, sx2, sy2, right, bottom, region.getTextureWidth(), region.getTextureHeight());

                // edges and center
                if (dstCenterW > 0 && dstTop > 0)
                    context.drawTexture(region.getIdentifier(), x + dstLeft, y, dstCenterW, dstTop, sx1, sy0,
                            srcTotalW - left - right, top, region.getTextureWidth(), region.getTextureHeight());
                if (dstCenterW > 0 && dstBottom > 0)
                    context.drawTexture(region.getIdentifier(), x + dstLeft, y + dstTop + dstCenterH, dstCenterW,
                            dstBottom,
                            sx1, sy2, srcTotalW - left - right, bottom, region.getTextureWidth(),
                            region.getTextureHeight());
                if (dstCenterH > 0 && dstLeft > 0)
                    context.drawTexture(region.getIdentifier(), x, y + dstTop, dstLeft, dstCenterH, sx0, sy1, left,
                            srcTotalH - top - bottom, region.getTextureWidth(), region.getTextureHeight());
                if (dstCenterH > 0 && dstRight > 0)
                    context.drawTexture(region.getIdentifier(), x + dstLeft + dstCenterW, y + dstTop, dstRight,
                            dstCenterH,
                            sx2, sy1, right, srcTotalH - top - bottom, region.getTextureWidth(),
                            region.getTextureHeight());
                if (dstCenterW > 0 && dstCenterH > 0)
                    context.drawTexture(region.getIdentifier(), x + dstLeft, y + dstTop, dstCenterW, dstCenterH, sx1,
                            sy1,
                            srcTotalW - left - right, srcTotalH - top - bottom, region.getTextureWidth(),
                            region.getTextureHeight());
            } else if ((renderFlags & RenderType.REPEAT) != 0) {
                context.drawRepeatingTexture(region.getIdentifier(), x, y, w, h, region.getU(), region.getV(),
                        region.getTextureWidth(), region.getTextureHeight());
            } else {
                // default: stretch
                context.drawTexture(region.getIdentifier(), x, y, w, h, region.getU(), region.getV(), region.getW(),
                        region.getH(), region.getTextureWidth(), region.getTextureHeight());
            }
            context.setShaderColor(1f, 1f, 1f, 1f);
        } catch (Throwable ignored) {
        } finally {
            if (scissored) {
                try {
                    context.disableScissor();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    @Override
    public void update(ControlObject owner, float tickDelta) {
        // background does not need per-frame logic for now
    }

    @Override
    public void render(ControlObject owner, net.minecraft.client.gui.DrawContext context, float tickDelta) {
        try {
            renderBackground(context, owner);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
