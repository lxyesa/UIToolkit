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
    // 可选的平铺格子大小（如果不为 0 则覆盖 src 尺寸用于平铺）
    private int tileWidth = 0;
    private int tileHeight = 0;
    // 用户指定的额外缩放倍数（默认为 1.0），用于 SCALE 模式以在自动计算的基础上放大或缩小。
    private float userScale = 1.0f;

    // 裁剪比例（0.0 - 完全裁剪不可见, 1.0 - 不裁剪）。仅在 DrawMode.SCALE 模式下生效。
    // 语义：在缩放后的图像上以控件中心为锚点，保留该比例大小的中心区域，其余被裁剪。
    private float clip = 1.0f;

    // 裁剪轴，默认仅水平裁剪
    private ClipAxis clipAxis = ClipAxis.HORIZONTAL;

    // --- Tile layout cache (to avoid recomputing per-frame) ---
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

    /**
     * 在 SCALE 模式下，设置额外的缩放倍数（相对于自动计算的等比例尺寸）。
     * 例如 setScale(1.5f) 会在自动计算的基础上放大 1.5 倍。
     * 
     * @param scale 缩放倍数（<=0 将被视为 1.0）
     */
    public void setScale(float scale) {
        if (scale <= 0f)
            this.userScale = 1.0f;
        else
            this.userScale = scale;
    }

    /**
     * 设置裁剪比例（0.0 - 1.0）。仅在 DrawMode.SCALE 时生效。
     * 小于等于 0.0 将导致不绘制背景，大于等于 1.0 将等同于不裁剪。
     */
    public void setClip(float clip) {
        if (Float.isNaN(clip))
            clip = 1.0f;
        this.clip = Math.max(0f, Math.min(1f, clip));
    }

    /**
     * 设置裁剪轴（水平/垂直/两者）。若传入 null 则保持当前值。
     */
    public void setClipAxis(ClipAxis axis) {
        if (axis != null)
            this.clipAxis = axis;
    }

    public DrawMode getDrawMode() {
        return drawMode;
    }

    public Image(Object owner, TextureRegion texturePath) {
        super(owner);
        setBackground(texturePath);
    }

    /**
     * 设置Image的绘制模式：拉伸、平铺、填充、居中。
     * 
     * @param mode 绘制模式
     */
    public void setDrawMode(DrawMode mode) {
        this.drawMode = mode;
    }

    /**
     * 设置平铺时使用的格子尺寸（像素）。若为 0 则使用源区域大小。
     */
    public void setTileSize(int w, int h) {
        this.tileWidth = Math.max(0, w);
        this.tileHeight = Math.max(0, h);
        invalidateTileCache();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        invalidateTileCache();
    }

    @Override
    public void setBackground(TextureRegion region) {
        super.setBackground(region);
        invalidateTileCache();
    }

    @Override
    public void setBackground(Identifier id, int u, int v, int w, int h, int texW, int texH) {
        super.setBackground(id, u, v, w, h, texW, texH);
        invalidateTileCache();
    }

    private void invalidateTileCache() {
        this.tileCacheValid = false;
    }

    private void ensureTileCache() {
        if (this.tileCacheValid)
            return;
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
    protected void renderBackground(net.minecraft.client.gui.DrawContext context, int x, int y, int width,
            int height) {
        // If no background set or zero size, nothing to do
        if (this.bgRegion == null && this.background == null)
            return;

        // quick alpha early-out
        if (this.bgAlpha <= 0f)
            return;

        try {
            // cache commonly used fields into locals to avoid repeated field access
            final Identifier localBackground = this.background;
            final int localBgU = this.bgU;
            final int localBgV = this.bgV;
            final int localBgWidth = this.bgWidth;
            final int localBgHeight = this.bgHeight;
            final int localTextureW = this.textureWidth;
            final int localTextureH = this.textureHeight;

            int srcW = localBgWidth > 0 ? localBgWidth : width;
            int srcH = localBgHeight > 0 ? localBgHeight : height;
            if (srcW <= 0 || srcH <= 0)
                return;

            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, this.bgAlpha);

            switch (getDrawMode()) {
                case STRETCH:
                    context.drawTexture(this.background, x, y, width, height, this.bgU, this.bgV, srcW, srcH,
                            this.textureWidth, this.textureHeight);
                    break;
                case CENTER: {
                    int dx = x + (width - srcW) / 2;
                    int dy = y + (height - srcH) / 2;
                    context.drawTexture(this.background, dx, dy, srcW, srcH, this.bgU, this.bgV, srcW, srcH,
                            this.textureWidth, this.textureHeight);
                    break;
                }
                case SCALE: {
                    // 在目标尺寸内等比例缩放（fit），然后再乘以用户设置的 userScale
                    float scale = Math.min((float) width / (float) srcW, (float) height / (float) srcH);
                    scale *= this.userScale;
                    // drawW/drawH 由基类 helper 计算并绘制
                    // 使用控件的锚点作为缩放原点（将锚点视为控件本地 (0,0)）
                    float scaleToUse = scale;
                    // clip <= 0: 不绘制背景；0<clip<1: 使用裁剪；clip>=1: 不裁剪
                    if (this.clip <= 0f) {
                        // skip drawing background when fully clipped
                        break;
                    }

                    if (this.clip > 0f && this.clip < 1f) {
                        // 获取锚点整数坐标与锚点分数/绘制尺寸
                        int[] anchorPt = getAnchorPoint(srcW, srcH, scaleToUse);
                        int anchorX = anchorPt[0];
                        int anchorY = anchorPt[1];
                        float[] af = getAnchorWindowAndFrac(srcW, srcH, scaleToUse);
                        float anchorFracX = af[2];
                        float anchorFracY = af[3];
                        int drawW = Math.max(1, Math.round(af[4]));
                        int drawH = Math.max(1, Math.round(af[5]));

                        // 计算要保留的像素大小，使用基类 computeKeepSizes
                        int[] keep = computeKeepSizes(drawW, drawH, this.clip, this.clipAxis);
                        int keepW = keep[0];
                        int keepH = keep[1];

                        // 按锚点在保留区域内的相对位置定位裁剪框。
                        int clipLeft = anchorX - Math.round(anchorFracX * keepW);
                        int clipTop = anchorY - Math.round(anchorFracY * keepH);
                        int clipRight = clipLeft + keepW;
                        int clipBottom = clipTop + keepH;

                        // 启用裁剪，然后绘制已缩放的纹理，最后关闭裁剪
                        context.enableScissor(clipLeft, clipTop, clipRight, clipBottom);
                        try {
                            drawTextureAtAnchorScaled(context, this.background, this.bgU, this.bgV, srcW, srcH,
                                    scaleToUse, this.textureWidth, this.textureHeight);
                        } finally {
                            context.disableScissor();
                        }
                    } else {
                        drawTextureAtAnchorScaled(context, this.background, this.bgU, this.bgV, srcW, srcH, scaleToUse,
                                this.textureWidth, this.textureHeight);
                    }
                    break;
                }
                case FILL: {
                    float scale = Math.max((float) width / (float) srcW, (float) height / (float) srcH);
                    int drawW = Math.max(1, Math.round(srcW * scale));
                    int drawH = Math.max(1, Math.round(srcH * scale));
                    int dx = x + (width - drawW) / 2;
                    int dy = y + (height - drawH) / 2;
                    context.drawTexture(this.background, dx, dy, drawW, drawH, this.bgU, this.bgV, srcW, srcH,
                            this.textureWidth, this.textureHeight);
                    break;
                }
                case NINESLICE: {
                    super.renderBackground(context, x, y, width, height);
                    break;
                }
                case TILE: {
                    // ensure cached tile metadata (avoids recalculating tile cell size every frame)
                    ensureTileCache();
                    int cellW = this.cachedCellW > 0 ? this.cachedCellW : srcW;
                    int cellH = this.cachedCellH > 0 ? this.cachedCellH : srcH;
                    if (cellW <= 0 || cellH <= 0)
                        break;
                    for (int ox = 0; ox < width; ox += cellW) {
                        int tw = Math.min(cellW, width - ox);
                        for (int oy = 0; oy < height; oy += cellH) {
                            int th = Math.min(cellH, height - oy);
                            // 当请求的 tile 大小小于源宽度时，我们仅取源左上角的子区域
                            int srcSubW = Math.min(srcW, tw);
                            int srcSubH = Math.min(srcH, th);
                            // prefer cached values populated by ensureTileCache()
                            Identifier drawId = this.cachedBackgroundId != null ? this.cachedBackgroundId
                                    : localBackground;
                            int drawU = this.cachedBgU != 0 ? this.cachedBgU : localBgU;
                            int drawV = this.cachedBgV != 0 ? this.cachedBgV : localBgV;
                            int drawTexW = this.cachedTextureW != 0 ? this.cachedTextureW : localTextureW;
                            int drawTexH = this.cachedTextureH != 0 ? this.cachedTextureH : localTextureH;
                            context.drawTexture(drawId, x + ox, y + oy, tw, th, drawU, drawV,
                                    srcSubW, srcSubH, drawTexW, drawTexH);
                        }
                    }
                    break;
                }
                default:
                    context.drawTexture(this.background, x, y, width, height, this.bgU, this.bgV, srcW, srcH,
                            this.textureWidth, this.textureHeight);
            }

            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        } catch (Throwable ignored) {
        }
    }
}
