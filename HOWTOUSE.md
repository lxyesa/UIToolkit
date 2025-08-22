## UIToolkit 使用说明

本说明文件包含当前库中三个核心类的目的说明与源码（或源码摘要）：

- `src/client/java/org/fish/uitoolkit/Control.java`：基础控件类，包含锚点、布局、背景绘制、九宫格与裁剪支持。
- `src/client/java/org/fish/uitoolkit/Container.java`：支持子控件管理的容器，负责子控件生命周期、布局缓存失效与内边距计算。
- `src/client/java/org/fish/uitoolkit/Image.java`：图像控件，支持多种绘制模式（拉伸、平铺、等比缩放、九宫格、填充、居中），并实现了平铺缓存与按锚点的裁剪绘制。

下面先给出每个类的简要说明与关键点，然后附上相应的源码（或源码摘要）。

---

## Control.java — 概要

`Control` 是控件层级的基类（实现 `UIElement`），负责：

- ownership 与层级引用（owner/child）。
- 锚点/缩放/布局缓存（避免每帧重复计算）。
- 本地坐标、锚点（水平/垂直）、边距、可见性、宽高。
- 背景纹理信息与九宫格、色相/饱和度/亮度/透明度等 tint 参数。
- 内容裁剪（scissor）与按需裁剪轴向控制。
- Nine-slice 缩放策略（NONE/PROPORTIONAL/MINIMUM/CLAMPED）以及 min/max 限制。
- 提供一组用于按锚点计算、缩放并按锚点绘制纹理的辅助方法（computeAnchorContext、drawTextureAtAnchorScaled 等）。
- 渲染流水线分阶段（位置/背景/内容），支持子类覆盖单个阶段（renderBackground/renderContent）。

下为在仓库中的源码摘要（部分方法以省略号表示，详见源码文件）：

```java
package org.fish.uitoolkit;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import org.fish.uitoolkit.utils.TextureRegion;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Control: 基础控件类，保存 owner 引用并在构造时将自己注册到 owner（如果 owner 支持的话）。
 */
public abstract class Control implements UIElement {
    // ---------- ownership & hierarchy ----------
    protected Object owner;
    protected Object child;

    // ---------- anchor/scale cache & helper context ----------
    protected static final class AnchorContext {…}

    private AnchorContext cachedAnchorCtx = null;
    private int cachedSrcW = 0;
    private int cachedSrcH = 0;
    private float cachedScale = 0f;
    private HAnchor cachedHAnchor = null;
    private VAnchor cachedVAnchor = null;
    private int cachedParentX = Integer.MIN_VALUE;
    private int cachedParentY = Integer.MIN_VALUE;
    private int cachedParentW = Integer.MIN_VALUE;
    private int cachedParentH = Integer.MIN_VALUE;
    private boolean anchorCtxDirty = true;

    // ---------- layout: position, anchors, margins, visibility, size ----------
    protected int x = 0;
    protected int y = 0;
    protected HAnchor hAnchor = HAnchor.LEFT;
    protected VAnchor vAnchor = VAnchor.TOP;
    protected int marginLeft = 0;
    protected int marginRight = 0;
    protected int marginTop = 0;
    protected int marginBottom = 0;
    protected float marginLeftRel = Float.NaN;
    protected float marginRightRel = Float.NaN;
    protected float marginTopRel = Float.NaN;
    protected float marginBottomRel = Float.NaN;
    protected boolean visible = true;
    protected int width;
    protected int height;

    // ---------- background / nine-slice / tinting ----------
    protected Identifier background = null;
    protected int bgU = 0;
    protected int bgV = 0;
    protected int bgWidth = 0;
    protected int bgHeight = 0;
    protected int textureWidth = 256;
    protected int textureHeight = 256;
    protected float bgAlpha = 1.0f;
    protected org.fish.uitoolkit.utils.TextureRegion bgRegion = null;
    protected float bgTintR = 1f;
    protected float bgTintG = 1f;
    protected float bgTintB = 1f;
    protected float bgTintA = 1f;
    protected float bgSaturation = 1f;
    protected float bgBrightness = 1f;

    // ---------- content clipping (scissor) ----------
    protected boolean contentClipEnabled = false;
    protected float contentClipFraction = 1.0f;
    protected ClipAxis contentClipAxis = ClipAxis.HORIZONTAL;

    /**
     * 启用或禁用子内容裁剪，并设置裁剪比例与轴向。
     *
     * @param enabled  是否启用裁剪
     * @param fraction 裁剪比例（0..1）
     * @param axis     裁剪轴向
     */
    public void setContentClip(boolean enabled, float fraction, ClipAxis axis) {…}

    /** 清除内容裁剪（恢复不裁剪） */
    public void clearContentClip() {…}

    public boolean isContentClipEnabled() {…}

    public float getContentClipFraction() {…}

    public ClipAxis getContentClipAxis() {…}

    /**
     * 九宫格（9-slice）缩放策略。该枚举决定在控件目标尺寸与源图片（裁剪后）尺寸不一致时
     * 如何计算角与边在目标上的像素厚度（dst 大小）。
     */
    public enum NineSliceScaleMode { NONE, PROPORTIONAL, MINIMUM, CLAMPED }

    public enum ClipAxis { HORIZONTAL, VERTICAL, BOTH }

    protected int[] computeKeepSizes(int drawW, int drawH, float clip, ClipAxis axis) {…}

    private int[] computeClipRect(int absX, int absY) {…}

    private void withClip(DrawContext context, int absX, int absY, Runnable action) {…}

    private NineSliceScaleMode nineSliceMode = NineSliceScaleMode.MINIMUM;
    private int nineSliceMinPx = 1;
    private int nineSliceMaxPx = Integer.MAX_VALUE;
    protected boolean initialized = false;

    public Control() { this(null); }

    public Control(Object owner) {…}

    @Override public Object getOwner() {…}
    public void setOwner(Object owner) {…}
    protected void invalidateAnchorContext() {…}
    public void setPosition(int x, int y) {…}
    public void setSize(int width, int height) {…}
    public void setHorizontalAnchor(HAnchor a) {…}
    public void setVerticalAnchor(VAnchor a) {…}
    public void setMargins(int left, int top, int right, int bottom) {…}
    public void setMarginLeft(int px) {…}
    public void setMarginRight(int px) {…}
    public void setMarginTop(int px) {…}
    public void setMarginBottom(int px) {…}
    public void setMargins(int all) {…}
    public void setMarginsRelative(float leftPct, float topPct, float rightPct, float bottomPct) {…}
    public void setMarginLeftPercent(float pct) {…}
    public void setMarginRightPercent(float pct) {…}
    public void setMarginTopPercent(float pct) {…}
    public void setMarginBottomPercent(float pct) {…}
    public void setVisible(boolean v) {…}
    @Override public int getLocalX() {…}
    @Override public int getLocalY() {…}
    @Override public boolean isVisible() {…}
    @Override public HAnchor getHorizontalAnchor() {…}
    @Override public VAnchor getVerticalAnchor() {…}
    @Override public int getMarginLeft() {…}
    @Override public int getMarginRight() {…}
    @Override public int getMarginTop() {…}
    @Override public int getMarginBottom() {…}
    @Override public int getWidth() {…}
    @Override public int getHeight() {…}
    public int getCenterX() {…}
    public int getCenterY() {…}
    public int[] getCenter() {…}
    public int[] getAnchorBasedCenter(int srcW, int srcH, float scale) {…}
    protected AnchorContext getAnchorWindowAndFrac(int srcW, int srcH, float scale) {…}
    protected AnchorContext computeAnchorContext(int srcW, int srcH, float scale, HAnchor hAnchor, VAnchor vAnchor) {…}
    public int[] getAnchorPoint(int srcW, int srcH, float scale) {…}
    protected void drawTextureAtAnchorScaled(DrawContext context, Identifier id, int srcU, int srcV, int srcW, int srcH, float scale, int textureW, int textureH) {…}
    protected void drawTextureAtAnchorScaled(DrawContext context, Identifier id, int srcU, int srcV, int srcW, int srcH, float scale, int textureW, int textureH, HAnchor hAnchor, VAnchor vAnchor) {…}
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {…}
    public void render(DrawContext context, int absX, int absY, int mouseX, int mouseY, float delta) {…}
    protected void renderPosition(DrawContext context, int absX, int absY, int mouseX, int mouseY, float delta) {…}
    protected void renderWithClip(DrawContext context, int absX, int absY, int mouseX, int mouseY, float delta) {…}
+}
+```

---

## Container.java — 概要与源码

`Container` 继承自 `Control`，管理子元素（`UIElement`）列表。关键点：

- 子控件的添加/移除/清空与只读视图 `getChildren()`。
- 当子结构或容器属性（位置、尺寸、padding）变化时，递归通知子控件失效其锚点缓存。
- 支持基于子元素计算自动宽/高（computeAutoWidth/computeAutoHeight），考虑子项的 local 坐标与 margin。
- renderContent 中以容器内容区（减去 padding）为原点计算并渲染子控件。

源码（完整）：

```java
package org.fish.uitoolkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.DrawContext;

/**
 * Container：支持子控件管理的控件基类（继承自 Control）。
 */
public class Container extends Control {

    private final List<UIElement> children = new ArrayList<>();
    private final List<UIElement> childrenView = Collections.unmodifiableList(children);
    private int paddingLeft = 0;
    private int paddingRight = 0;
    private int paddingTop = 0;
    private int paddingBottom = 0;

    public Container() { super(); }

    public Container(Object owner) { super(owner); }

    public Container addChild(UIElement child) {
        if (child != null)
            children.add(child);
        if (child instanceof Control) {
            ((Control) child).invalidateAnchorContext();
            if (this instanceof Control && ((Control) this).isInitialized()) {
                ((UIElement) child).initialize();
            }
        }
        return this;
    }

    public boolean removeChild(UIElement child) {
        boolean r = children.remove(child);
        if (r) {
            if (child instanceof Control) {
                ((Control) child).invalidateAnchorContext();
            }
        }
        return r;
    }

    public void clearChildren() { children.clear(); }

    public List<UIElement> getChildren() { return childrenView; }

    public void setPadding(int left, int top, int right, int bottom) {
        this.paddingLeft = left;
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
        propagateInvalidateChildren();
    }

    public void setPadding(int pad) { setPadding(pad, pad, pad, pad); }

    protected void propagateInvalidateChildren() {
        for (UIElement child : children) {
            if (child instanceof Control) {
                ((Control) child).invalidateAnchorContext();
            }
            if (child instanceof Container) {
                ((Container) child).propagateInvalidateChildren();
            }
        }
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        propagateInvalidateChildren();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        propagateInvalidateChildren();
    }

    public int getPaddingLeft() { return paddingLeft; }
    public int getPaddingRight() { return paddingRight; }
    public int getPaddingTop() { return paddingTop; }
    public int getPaddingBottom() { return paddingBottom; }

    @Override
    public int getHeight() {
        if (height > 0) return height;
        return computeAutoHeight();
    }

    @Override
    public int getWidth() {
        if (width > 0) return width;
        return computeAutoWidth();
    }

    protected int computeAutoWidth() {
        List<UIElement> children = getChildren();
        if (children.isEmpty()) return paddingLeft + paddingRight;
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        boolean any = false;
        for (UIElement child : children) {
            if (child == null || !child.isVisible()) continue;
            any = true;
            int lx = child.getLocalX() - child.getMarginLeft();
            int rx = child.getLocalX() + child.getWidth() + child.getMarginRight();
            if (lx < minX) minX = lx;
            if (rx > maxX) maxX = rx;
        }
        if (!any) return paddingLeft + paddingRight;
        int contentWidth = maxX - minX;
        return contentWidth + paddingLeft + paddingRight;
    }

    protected int computeAutoHeight() {
        List<UIElement> children = getChildren();
        if (children.isEmpty()) return paddingTop + paddingBottom;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        boolean any = false;
        for (UIElement child : children) {
            if (child == null || !child.isVisible()) continue;
            any = true;
            int ty = child.getLocalY() - child.getMarginTop();
            int by = child.getLocalY() + child.getHeight() + child.getMarginBottom();
            if (ty < minY) minY = ty;
            if (by > maxY) maxY = by;
        }
        if (!any) return paddingTop + paddingBottom;
        int contentHeight = maxY - minY;
        return contentHeight + paddingTop + paddingBottom;
    }

    @Override
    public void setChild(UIElement child) { addChild(child); }

    @Override
    public void initialize() {
        super.initialize();
        for (UIElement child : children) {
            if (child != null) child.initialize();
        }
    }

    @Override
    protected void renderContent(DrawContext context, int absX, int absY, int mouseX, int mouseY, float delta) {
        int contentX = absX + this.paddingLeft;
        int contentY = absY + this.paddingTop;
        int contentW = Math.max(0, getWidth() - this.paddingLeft - this.paddingRight);
        int contentH = Math.max(0, getHeight() - this.paddingTop - this.paddingBottom);

        for (UIElement child : children) {
            if (child == null || !child.isVisible()) continue;
            int childAbsX = contentX;
            int childAbsY = contentY;
            if (child instanceof UIElement) {
                UIElement e = (UIElement) child;
                childAbsX = e.getAnchoredX(contentX, contentY, contentW, contentH) + e.getLocalX();
                childAbsY = e.getAnchoredY(contentX, contentY, contentW, contentH) + e.getLocalY();
            }
            child.render(context, childAbsX, childAbsY, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean containsPoint(int px, int py) {
        int gx = getX();
        int gy = getY();
        return px >= gx && py >= gy && px < gx + getWidth() && py < gy + getHeight();
    }

    @Override
    public void tick() {
        if (!isVisible()) return;
        for (UIElement child : children) {
            if (child != null) child.tick();
        }
    }

    @Override
    public void onRemoved() {
        for (UIElement child : children) {
            if (child != null) child.onRemoved();
        }
    }
}
```

---

## Image.java — 概要与源码

`Image` 继承自 `Control` 并提供丰富的图像绘制模式：

- DrawMode：STRETCH、TILE、SCALE、NINESLICE、FILL、CENTER。
- 平铺（TILE）支持可选 tileWidth/tileHeight 与缓存以避免每帧重复计算。
- SCALE 模式支持 userScale（额外缩放倍数）与 clip（裁剪比例）以及 clipAxis；支持按控件锚点进行缩放与裁剪。
- 支持 tint（颜色/饱和度/亮度/alpha）与开启/关闭混合。
- NINESLICE 模式委托给基类的九宫格渲染。

完整源码如下：

```java
package org.fish.uitoolkit;

import org.fish.uitoolkit.utils.TextureRegion;
import net.minecraft.util.Identifier;

public class Image extends Control {
    public enum DrawMode {
        STRETCH, // 拉伸
        TILE, // 平铺
        SCALE, // 等比例缩放（在指定 size 的基础上按比例缩放）
        NINESLICE, // 使用基类的九宫格渲染
        FILL, // 填充
        CENTER // 居中
    }

    private DrawMode drawMode = DrawMode.STRETCH;
    private int tileWidth = 0;
    private int tileHeight = 0;
    private float userScale = 1.0f;
    private float clip = 1.0f;
    private ClipAxis clipAxis = ClipAxis.HORIZONTAL;

    private boolean tileCacheValid = false;
    private int cachedCellW = 0;
    private int cachedCellH = 0;
    private Identifier cachedBackgroundId = null;
    private int cachedSrcW = 0;
    private int cachedSrcH = 0;
    private int cachedBgU = 0;
    private int cachedBgV = 0;
    private int cachedTextureW = 0;
    private int cachedTextureH = 0;

    public void setScale(float scale) { if (scale <= 0f) this.userScale = 1.0f; else this.userScale = scale; }
    public void setClip(float clip) { if (Float.isNaN(clip)) clip = 1.0f; this.clip = Math.max(0f, Math.min(1f, clip)); }
    public void setClipAxis(ClipAxis axis) { if (axis != null) this.clipAxis = axis; }
    public DrawMode getDrawMode() { return drawMode; }
    public Image(Object owner, TextureRegion texturePath) { super(owner); setBackground(texturePath); }
    public void setDrawMode(DrawMode mode) { this.drawMode = mode; }
    public void setTileSize(int w, int h) { this.tileWidth = Math.max(0, w); this.tileHeight = Math.max(0, h); invalidateTileCache(); }

    @Override public void setSize(int width, int height) { super.setSize(width, height); invalidateTileCache(); }
    @Override public void setBackground(TextureRegion region) { super.setBackground(region); invalidateTileCache(); }
    @Override public void setBackground(Identifier id, int u, int v, int w, int h, int texW, int texH) { super.setBackground(id, u, v, w, h, texW, texH); invalidateTileCache(); }

    private void invalidateTileCache() { this.tileCacheValid = false; }

    private void ensureTileCache() {
        if (this.tileCacheValid) return;
        this.cachedBackgroundId = this.background;
        this.cachedBgU = this.bgU;
        this.cachedBgV = this.bgV;
        this.cachedSrcW = this.bgWidth > 0 ? this.bgWidth : 0;
        this.cachedSrcH = this.bgHeight > 0 ? this.bgHeight : 0;
        this.cachedTextureW = this.textureWidth;
        this.cachedTextureH = this.textureHeight;
        this.cachedCellW = this.tileWidth > 0 ? this.tileWidth : (this.cachedSrcW > 0 ? this.cachedSrcW : 0);
        this.cachedCellH = this.tileHeight > 0 ? this.tileHeight : (this.cachedSrcH > 0 ? this.cachedSrcH : 0);
        this.tileCacheValid = true;
    }

    @Override
    protected void renderBackground(net.minecraft.client.gui.DrawContext context, int x, int y, int width, int height) {
        if (this.bgRegion == null && this.background == null) return;
        if (this.bgAlpha <= 0f) return;
        try {
            final Identifier localBackground = this.background;
            final int localBgU = this.bgU;
            final int localBgV = this.bgV;
            final int localBgWidth = this.bgWidth;
            final int localBgHeight = this.bgHeight;
            final int localTextureW = this.textureWidth;
            final int localTextureH = this.textureHeight;

            int srcW = localBgWidth > 0 ? localBgWidth : width;
            int srcH = localBgHeight > 0 ? localBgHeight : height;
            if (srcW <= 0 || srcH <= 0) return;

            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            float effectiveA = this.bgAlpha * this.bgTintA;
            float r = this.bgTintR; float g = this.bgTintG; float b = this.bgTintB;
            float[] hsv = rgbToHsv(r, g, b);
            hsv[1] = Math.max(0f, hsv[1] * this.bgSaturation);
            float[] rgb = hsvToRgb(hsv[0], hsv[1], hsv[2]);
            float br = Math.max(0f, this.bgBrightness);
            float outR = Math.min(1f, rgb[0] * br);
            float outG = Math.min(1f, rgb[1] * br);
            float outB = Math.min(1f, rgb[2] * br);
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(outR, outG, outB, effectiveA);

            switch (getDrawMode()) {
                case STRETCH:
                    context.drawTexture(this.background, x, y, width, height, this.bgU, this.bgV, srcW, srcH, this.textureWidth, this.textureHeight);
                    break;
                case CENTER: {
                    int dx = x + (width - srcW) / 2;
                    int dy = y + (height - srcH) / 2;
                    context.drawTexture(this.background, dx, dy, srcW, srcH, this.bgU, this.bgV, srcW, srcH, this.textureWidth, this.textureHeight);
                    break;
                }
                case SCALE: {
                    float scale = Math.min((float) width / (float) srcW, (float) height / (float) srcH);
                    scale *= this.userScale;
                    float scaleToUse = scale;
                    if (this.clip <= 0f) { break; }

                    if (this.clip > 0f && this.clip < 1f) {
                        org.fish.uitoolkit.Control.AnchorContext ctx = computeAnchorContext(srcW, srcH, scaleToUse, getHorizontalAnchor(), getVerticalAnchor());
                        int anchorX = Math.round(ctx.absX + ctx.pivotLocalX);
                        int anchorY = Math.round(ctx.absY + ctx.pivotLocalY);
                        float anchorFracX = ctx.anchorFracX;
                        float anchorFracY = ctx.anchorFracY;
                        int drawW = Math.max(1, ctx.drawW);
                        int drawH = Math.max(1, ctx.drawH);

                        int[] keep = computeKeepSizes(drawW, drawH, this.clip, this.clipAxis);
                        int keepW = keep[0];
                        int keepH = keep[1];

                        int clipLeft = anchorX - Math.round(anchorFracX * keepW);
                        int clipTop = anchorY - Math.round(anchorFracY * keepH);
                        int clipRight = clipLeft + keepW;
                        int clipBottom = clipTop + keepH;

                        context.enableScissor(clipLeft, clipTop, clipRight, clipBottom);
                        try {
                            drawTextureAtAnchorScaled(context, this.background, this.bgU, this.bgV, srcW, srcH, scaleToUse, this.textureWidth, this.textureHeight);
                        } finally {
                            context.disableScissor();
                        }
                    } else {
                        drawTextureAtAnchorScaled(context, this.background, this.bgU, this.bgV, srcW, srcH, scaleToUse, this.textureWidth, this.textureHeight);
                    }
                    break;
                }
                case FILL: {
                    float scale = Math.max((float) width / (float) srcW, (float) height / (float) srcH);
                    int drawW = Math.max(1, Math.round(srcW * scale));
                    int drawH = Math.max(1, Math.round(srcH * scale));
                    int dx = x + (width - drawW) / 2;
                    int dy = y + (height - drawH) / 2;
                    context.drawTexture(this.background, dx, dy, drawW, drawH, this.bgU, this.bgV, srcW, srcH, this.textureWidth, this.textureHeight);
                    break;
                }
                case NINESLICE: {
                    super.renderBackground(context, x, y, width, height);
                    break;
                }
                case TILE: {
                    ensureTileCache();
                    int cellW = this.cachedCellW > 0 ? this.cachedCellW : srcW;
                    int cellH = this.cachedCellH > 0 ? this.cachedCellH : srcH;
                    if (cellW <= 0 || cellH <= 0) break;
                    for (int ox = 0; ox < width; ox += cellW) {
                        int tw = Math.min(cellW, width - ox);
                        for (int oy = 0; oy < height; oy += cellH) {
                            int th = Math.min(cellH, height - oy);
                            int srcSubW = Math.min(srcW, tw);
                            int srcSubH = Math.min(srcH, th);
                            Identifier drawId = this.cachedBackgroundId != null ? this.cachedBackgroundId : localBackground;
                            int drawU = this.cachedBgU != 0 ? this.cachedBgU : localBgU;
                            int drawV = this.cachedBgV != 0 ? this.cachedBgV : localBgV;
                            int drawTexW = this.cachedTextureW != 0 ? this.cachedTextureW : localTextureW;
                            int drawTexH = this.cachedTextureH != 0 ? this.cachedTextureH : localTextureH;
                            context.drawTexture(drawId, x + ox, y + oy, tw, th, drawU, drawV, srcSubW, srcSubH, drawTexW, drawTexH);
                        }
                    }
                    break;
                }
                default:
                    context.drawTexture(this.background, x, y, width, height, this.bgU, this.bgV, srcW, srcH, this.textureWidth, this.textureHeight);
+            }
+
+            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
+            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
+        } catch (Throwable ignored) {
+        }
+    }
+}
+```

---

### Control 渲染流水线与继承指南

下面针对 `Control` 的渲染流水线做一个清晰的分解，说明各阶段的职责、扩展点，以及如何继承实现自定义控件。

渲染流水线（概念分阶段）

1. 入口：render(context, mouseX, mouseY, delta)
   - 负责作为控件渲染的统一入口。通常由上层 UI 框架在每帧调用。
   - 入口会计算控件在父容器中的绝对位置（基于锚点/本地坐标/父尺寸与 margin），并最终调用下列阶段。

2. renderPosition(...)
   - 负责把计算好的绝对位置传入并准备渲染环境。它是分阶段渲染的调度器。
   - 在该阶段会先调用 `renderBackground(...)`，然后进入内容渲染阶段（带或不带裁剪）。

3. renderBackground(context, absX, absY, width, height)
   - 背景绘制阶段：绘制背景纹理、九宫格、tint 与 blend 等。子类可覆盖以实现自定义背景。
   - 如果调用 super.renderBackground(...)，则会执行 `Control`/`Image` 等基类的背景绘制逻辑。

4. renderWithClip / 内容阶段
   - 如果启用了 content-clip（裁剪），该阶段会先计算裁剪矩形并开启 scissor（context.enableScissor），然后调用实际的内容绘制逻辑，最后关闭 scissor。
   - 内容阶段通常由 `renderContent(context, absX, absY, mouseX, mouseY, delta)` 实现，子类重写该方法以绘制控件的前景/子元素。容器类也在此阶段递归渲染子元素。

5. 清理与后处理
   - 渲染结束后需要恢复 OpenGL 状态（例如 shader color、blend、scissor 等），基类通常会在合适位置做恢复。自定义绘制应保证在异常时也不破坏全局状态。

如何正确继承并实现自定义控件（步骤与注意事项）

- 选择覆盖点：
  - 仅想改变背景：覆写 `renderBackground(...)`。
  - 仅想在背景上绘制额外内容或前景：覆写 `renderContent(...)`。
  - 需要完全控制渲染顺序与坐标：覆写 `renderPosition(...)` 或 `render(...)`（谨慎使用，确保调用必要的父级逻辑或维护状态）。

- 在覆写方法内部的良好实践：
  - 小心处理 null / 0 大小：在绘制前检查宽高和资源是否可用，避免触发异常。
  - 保持 OpenGL 状态的一致性：若你修改 shader color、enableBlend、scissor 等，请在 finally 或结束处恢复初始状态（或直接调用基类的恢复逻辑）。
  - 对于需要裁剪的子绘制，使用 `context.enableScissor(...)` / `context.disableScissor()` 与基类的剪辑计算逻辑保持一致。
  - 当控件的布局相关属性发生变化（例如 size、margin、anchors、padding），调用 `invalidateAnchorContext()`（或容器的 `propagateInvalidateChildren()`）以确保后续渲染使用正确的缓存数据。
  - 在构造函数中尽量不要依赖父容器的尺寸/状态，延迟到 `initialize()` 或首次 `render` 时读取。

小合同（Contract） — 自定义渲染方法期望与保证（2~4 条）

- 输入：DrawContext、像素坐标（absX/absY）、控件宽高、鼠标坐标、帧间插值 delta。
- 输出：在传入的绘制矩形内提交绘制命令；不应修改父控件状态或其它控件的几何数据。
- 错误模式：若资源缺失或尺寸非法，应安全地跳过绘制并保证不抛出未捕获异常；不要吞掉严重错误（可记录或向上抛出）。
- 成功标准：绘制完成且恢复全局渲染状态（shader color = 1,1,1,1；blend/scissor 恢复到调用前状态）。

常见边界/异常情况（及推荐应对）

- 背景资源为 null：跳过背景绘制。
- 宽高为 0 或负：跳过绘制。
- bgAlpha <= 0：提前返回。
- 裁剪区域计算产生超出窗口的坐标：在调用 `enableScissor` 前 clamp 到窗口范围，或依赖框架剪裁函数。
- 多线程或初始化顺序问题：避免在构造器中访问尚未初始化的父/资源，使用 `initialize()` 钩子。

示例：实现一个简单的带前景条的自定义控件（伪代码，展示覆写要点）

```java
public class ProgressBar extends Control {
    private float progress = 0f; // 0..1

    public ProgressBar(Object owner) {
        super(owner);
        // 不在构造器中依赖父尺寸
    }

    public void setProgress(float p) {
        this.progress = Math.max(0f, Math.min(1f, p));
    }

    @Override
    protected void renderBackground(DrawContext context, int absX, int absY, int width, int height) {
        // 使用基类背景（如果有）然后绘制进度条背景块
        super.renderBackground(context, absX, absY, width, height);
        if (width <= 0 || height <= 0) return;
        // 绘制一个暗色背景条（示例：假设存在一个白色 1x1 纹理可以拉伸）
        // 注意：实际绘制 API 取决于 DrawContext，可用 drawTexture 或项目已有的填充工具。
        int barW = Math.round(width * 0.9f);
        int barH = Math.max(2, height / 4);
        int bx = absX + (width - barW) / 2;
        int by = absY + (height - barH) / 2;
        // 伪代码：绘制背景槽
        // context.drawTexture(WHITE_TEX, bx, by, barW, barH, ...);
    }

    @Override
    protected void renderContent(DrawContext context, int absX, int absY, int mouseX, int mouseY, float delta) {
        // 在背景之上绘制进度填充
        int barW = Math.round(getWidth() * 0.9f);
        int barH = Math.max(2, getHeight() / 4);
        int bx = absX + (getWidth() - barW) / 2;
        int by = absY + (getHeight() - barH) / 2;
        int fillW = Math.max(0, Math.round(barW * this.progress));
        if (fillW > 0) {
            // 伪代码：绘制填充部分
            // context.drawTexture(WHITE_TEX, bx, by, fillW, barH, ...);
        }
    }
}
```

说明：示例中用到了 `super.renderBackground(...)` 来复用基类背景逻辑，并在 `renderContent(...)` 中仅负责绘制前景（不影响基类的裁剪/恢复逻辑）。真实绘制命令应使用项目中的纹理或绘制工具（示例中的 `WHITE_TEX` 只是占位）。

快速检查清单（继承控件时）

- [ ] 覆写点选定（background/content/position）并限制影响范围。
- [ ] 在状态变更（size/anchor/margin）时调用 `invalidateAnchorContext()`。
- [ ] 检查 null/0 尺寸并提前返回以避免异常。
- [ ] 保证在异常路径中也恢复 OpenGL/渲染状态（blend/scissor/shader color）。

---

## 使用说明要点

- 若修改 `Control`、`Container` 或 `Image` 中影响布局/锚点计算的字段（位置、padding、margin、size），请调用相应的缓存失效方法（如 `invalidateAnchorContext()` 或容器的 `propagateInvalidateChildren()`）。
- 对于 `Image` 的平铺，使用 `setTileSize(w,h)` 并在更新背景或控件尺寸后留意缓存失效（库已处理）。
- SCALE 模式下可通过 `setScale()` 与 `setClip()` 调整缩放/裁剪行为，裁剪框以锚点为参考定位。

---