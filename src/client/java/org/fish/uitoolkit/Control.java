package org.fish.uitoolkit;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import org.fish.uitoolkit.utils.TextureRegion;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Control: 基础控件类，保存 owner 引用并在构造时将自己注册到 owner（如果 owner 支持的话）。
 */
public abstract class Control implements UIElement {

    protected Object owner;
    protected Object child; // 用于存储单个子控件（如果有）

    // 缓存用于锚点/缩放位置计算的上下文，和失效标志
    private float[] cachedAnchorCtx = null; // { absX, absY, drawW, drawH, pivotLocalX, pivotLocalY, anchorFracX,
                                            // anchorFracY }
    private int cachedSrcW = 0;
    private int cachedSrcH = 0;
    private float cachedScale = 0f;
    private HAnchor cachedHAnchor = null;
    private VAnchor cachedVAnchor = null;
    private boolean anchorCtxDirty = true;

    // 基础控件属性：位置、锚点、margin、可见性
    protected int x = 0;
    protected int y = 0;
    protected HAnchor hAnchor = HAnchor.LEFT;
    protected VAnchor vAnchor = VAnchor.TOP;
    protected int marginLeft = 0;
    protected int marginRight = 0;
    protected int marginTop = 0;
    protected int marginBottom = 0;
    protected boolean visible = true;
    protected int width;
    protected int height;

    // Background texture (from a texture atlas) and source rectangle in the atlas
    protected Identifier background = null;
    protected int bgU = 0; // source x in texture atlas
    protected int bgV = 0; // source y in texture atlas
    protected int bgWidth = 0; // source width
    protected int bgHeight = 0; // source height
    protected int textureWidth = 256; // atlas width (default common size)
    protected int textureHeight = 256; // atlas height
    protected float bgAlpha = 1.0f; // background alpha (0..1)
    protected org.fish.uitoolkit.utils.TextureRegion bgRegion = null;

    /**
     * 九宫格（9-slice）缩放策略。该枚举决定在控件目标尺寸与源图片（裁剪后）尺寸不一致时
     * 如何计算角与边在目标上的像素厚度（dst 大小）。
     *
     * <ul>
     * <li>NONE — 不缩放角：角区在目标上保持与源 inset 相同的像素大小（受目标尺寸限制）。</li>
     * <li>PROPORTIONAL — 按比例缩放：角的目标大小按目标宽/源宽（或高）比例放大或缩小。</li>
     * <li>MINIMUM — 最小阈值：角至少保证为指定的最小像素厚度（由 {@link #setNineSliceMinPx} 设置）。</li>
     * <li>CLAMPED — 按比例但夹取：先按比例缩放，再 clamp 到由 {@link #setNineSliceMinPx} 与
     * {@link #setNineSliceMaxPx} 定义的范围内。</li>
     * </ul>
     */
    public enum NineSliceScaleMode {
        NONE,
        PROPORTIONAL,
        MINIMUM,
        CLAMPED
    }

    /**
     * 控制裁剪轴向：仅水平、仅垂直或同时裁剪。
     */
    public enum ClipAxis {
        HORIZONTAL,
        VERTICAL,
        BOTH
    }

    /**
     * 计算保留区域的像素尺寸（keepW, keepH），根据 drawW/drawH、裁剪比例 clip 和裁剪轴。
     * 返回 int[]{ keepW, keepH }。
     */
    protected int[] computeKeepSizes(int drawW, int drawH, float clip, ClipAxis axis) {
        int keepW = drawW;
        int keepH = drawH;
        if (clip <= 0f) {
            keepW = 0;
            keepH = 0;
            return new int[] { keepW, keepH };
        }
        switch (axis) {
            case HORIZONTAL:
                keepW = Math.max(1, Math.round(drawW * clip));
                break;
            case VERTICAL:
                keepH = Math.max(1, Math.round(drawH * clip));
                break;
            case BOTH:
                keepW = Math.max(1, Math.round(drawW * clip));
                keepH = Math.max(1, Math.round(drawH * clip));
                break;
            default:
                keepW = Math.max(1, Math.round(drawW * clip));
                keepH = Math.max(1, Math.round(drawH * clip));
        }
        return new int[] { keepW, keepH };
    }

    /** 当前使用的九宫格缩放模式（默认：MINIMUM）。 */
    private NineSliceScaleMode nineSliceMode = NineSliceScaleMode.MINIMUM;

    /**
     * 当使用 MINIMUM 或 CLAMPED 模式时，角/边在目标上的最小像素厚度。
     * 该值仅在计算目标 dst 边界时被用于约束结果，单位为像素。
     */
    private int nineSliceMinPx = 1;

    /**
     * 当使用 CLAMPED 模式时，角/边在目标上的最大像素厚度上限。
     * 若不希望上限，可保留 Integer.MAX_VALUE。
     */
    private int nineSliceMaxPx = Integer.MAX_VALUE;

    public Control() {
        this(null);
    }

    /**
     * 构造一个控件并可选地将其注册到指定的 owner（若 owner 为 Container 或 Control）。
     *
     * @param owner 所属对象，通常为容器或另一个 Control；可为 null
     */
    public Control(Object owner) {
        this.owner = owner;
        if (owner instanceof Control) {
            try {
                ((Control) owner).setChild(this);
            } catch (Throwable ignored) {
            }
        }

        if (owner instanceof Container) {
            try {
                ((Container) owner).addChild(this);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public Object getOwner() {
        return owner;
    }

    /**
     * 更改控件的 owner 引用（不会自动修改 owner 的子控件集合）。
     *
     * @param owner 新的 owner 对象
     */
    public void setOwner(Object owner) {
        this.owner = owner;
        this.anchorCtxDirty = true;
    }

    /**
     * 设置控件的本地坐标。
     *
     * @param x 本地 x 坐标（像素）
     * @param y 本地 y 坐标（像素）
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        this.anchorCtxDirty = true;
    }

    /**
     * 设置控件的本地坐标。
     * 
     * @param width  控件宽度（像素）
     * @param height 控件高度（像素）
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        this.anchorCtxDirty = true;
    }

    /**
     * 设置水平锚点（HAnchor），用于相对于父容器定位。
     * 
     * @param a
     */
    public void setHorizontalAnchor(HAnchor a) {
        this.hAnchor = a;
        this.anchorCtxDirty = true;
    }

    /**
     * 设置垂直锚点（VAnchor），用于相对于父容器定位。
     *
     * @param a 垂直锚点值
     */
    public void setVerticalAnchor(VAnchor a) {
        this.vAnchor = a;
        this.anchorCtxDirty = true;
    }

    /**
     * 设置 margin（边距），用于在控件内容与其边界之间添加空白区域。
     * 
     * @param left   左边距
     * @param top    上边距
     * @param right  右边距
     * @param bottom 下边距
     */
    public void setMargins(int left, int top, int right, int bottom) {
        this.marginLeft = left;
        this.marginTop = top;
        this.marginRight = right;
        this.marginBottom = bottom;
        this.anchorCtxDirty = true;
    }

    /**
     * 设置统一的 margin（边距）值。
     * 
     * @param all 统一的边距值（左、上、右、下均相同）
     */
    public void setMargins(int all) {
        setMargins(all, all, all, all);
    }

    /**
     * 设置控件的可见性。
     * 
     * @param width 控件宽度（像素）
     */
    public void setWidth(int width) {
        this.width = width;
        this.anchorCtxDirty = true;
    }

    /**
     * 设置控件的高度。
     * 
     * @param height 控件高度（像素）
     */
    public void setHeight(int height) {
        this.height = height;
        this.anchorCtxDirty = true;
    }

    /**
     * 设置控件外边距。
     *
     * @param left   左边距（像素）
     * @param top    上边距（像素）
     * @param right  右边距（像素）
     * @param bottom 下边距（像素）
     */

    public void setVisible(boolean v) {
        this.visible = v;
        this.anchorCtxDirty = true;
    }

    /**
     * 显示或隐藏控件。
     *
     * @param v true 表示显示，false 表示隐藏
     */

    @Override
    public int getLocalX() {
        return x;
    }

    @Override
    public int getLocalY() {
        return y;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public HAnchor getHorizontalAnchor() {
        return hAnchor;
    }

    @Override
    public VAnchor getVerticalAnchor() {
        return vAnchor;
    }

    @Override
    public int getMarginLeft() {
        return marginLeft;
    }

    @Override
    public int getMarginRight() {
        return marginRight;
    }

    @Override
    public int getMarginTop() {
        return marginTop;
    }

    @Override
    public int getMarginBottom() {
        return marginBottom;
    }

    /**
     * 默认宽度回退：若子类未覆盖，则使用背景宽度作为控件宽度回退值。
     */
    @Override
    public int getWidth() {
        if (this.width > 0)
            return this.width;
        return this.bgWidth > 0 ? this.bgWidth : 0;
    }

    /**
     * 默认高度回退：若子类未覆盖，则使用背景高度作为控件高度回退值。
     */
    @Override
    public int getHeight() {
        if (this.height > 0)
            return this.height;
        return this.bgHeight > 0 ? this.bgHeight : 0;
    }

    /**
     * 返回控件相对于窗口/画布的中心 X 坐标（像素），基于锚点和本地坐标计算。
     * 该方法使用父容器的定位信息（getParentX/Y/Width/Height）和锚点偏移
     * （通过 getAnchoredX/getAnchoredY 计算绝对位置），然后再加上控件宽度的一半。
     *
     * @return 绝对中心 X（像素）
     */
    public int getCenterX() {
        // 使用通用的 getX() 来避免重复计算父容器/锚点逻辑
        int absX = getX();
        return absX + (getWidth() / 2);
    }

    /**
     * 返回控件相对于窗口/画布的中心 Y 坐标（像素）。
     * 
     * @return 绝对中心 Y（像素）
     */
    public int getCenterY() {
        int absY = getY();
        return absY + (getHeight() / 2);
    }

    /**
     * 返回 {centerX, centerY} 的整型数组，便于一次性获取中心点。
     */
    public int[] getCenter() {
        return new int[] { getCenterX(), getCenterY() };
    }

    /**
     * 返回一个基于控件锚点并考虑缩放后的图像中心（绝对坐标）的整型数组 {centerX, centerY}。
     *
     * 该方法用于在不直接绘制图像的情况下计算当使用 drawTextureAtAnchorScaled
     * （或等价计算）并传入给定源宽高与缩放倍数时，图像在窗口坐标系下的中心点位置。
     *
     * @param srcW  源图像宽度（像素）
     * @param srcH  源图像高度（像素）
     * @param scale 缩放倍数（1.0 = 原始大小）
     * @return 长度为2的整型数组，索引0为 centerX，索引1为 centerY（窗口坐标）
     */
    public int[] getAnchorBasedCenter(int srcW, int srcH, float scale) {
        float[] ctx = computeAnchorContext(srcW, srcH, scale, getHorizontalAnchor(), getVerticalAnchor());
        float absX = ctx[0];
        float absY = ctx[1];
        int drawW = Math.round(ctx[2]);
        int drawH = Math.round(ctx[3]);
        float pivotLocalX = ctx[4];
        float pivotLocalY = ctx[5];
        float anchorFracX = ctx[6];
        float anchorFracY = ctx[7];

        int dx = Math.round(absX + pivotLocalX - drawW * anchorFracX);
        int dy = Math.round(absY + pivotLocalY - drawH * anchorFracY);

        int centerX = dx + (drawW / 2);
        int centerY = dy + (drawH / 2);
        return new int[] { centerX, centerY };
    }

    /**
     * 返回用于锚点裁剪/布局的若干数值：
     * { anchorWindowX, anchorWindowY, anchorFracX, anchorFracY, drawW, drawH }
     * - anchorWindowX/Y: 控件内锚点在窗口坐标下的位置（像素）
     * - anchorFracX/Y: 缩放后图像中被对齐点在图像内的相对位置（0..1）
     * - drawW/drawH: 缩放后的图像像素尺寸
     *
     * 该方法为子类（例如 Image）提供按锚点计算裁剪区域所需的基础数据。
     */
    protected float[] getAnchorWindowAndFrac(int srcW, int srcH, float scale) {
        float[] ctx = computeAnchorContext(srcW, srcH, scale, getHorizontalAnchor(), getVerticalAnchor());
        float absX = ctx[0];
        float absY = ctx[1];
        float drawW = ctx[2];
        float drawH = ctx[3];
        float pivotLocalX = ctx[4];
        float pivotLocalY = ctx[5];
        float anchorFracX = ctx[6];
        float anchorFracY = ctx[7];

        float anchorWindowX = absX + pivotLocalX;
        float anchorWindowY = absY + pivotLocalY;

        return new float[] { anchorWindowX, anchorWindowY, anchorFracX, anchorFracY, drawW, drawH };
    }

    /**
     * 统一计算与锚点/缩放相关的上下文值，减少重复计算。
     * 返回数组：{ absX, absY, drawW, drawH, pivotLocalX, pivotLocalY, anchorFracX,
     * anchorFracY }
     */
    protected float[] computeAnchorContext(int srcW, int srcH, float scale, HAnchor hAnchor, VAnchor vAnchor) {
        // 如果缓存有效且参数未改变，则直接返回缓存
        if (!anchorCtxDirty && cachedAnchorCtx != null && cachedSrcW == srcW && cachedSrcH == srcH
                && Float.compare(cachedScale, scale) == 0 && cachedHAnchor == hAnchor && cachedVAnchor == vAnchor) {
            return cachedAnchorCtx;
        }

        int parentX = getParentX();
        int parentY = getParentY();
        int parentW = getParentWidth();
        int parentH = getParentHeight();
        int absX = getAnchoredX(parentX, parentY, parentW, parentH) + getLocalX();
        int absY = getAnchoredY(parentX, parentY, parentW, parentH) + getLocalY();

        int drawW = Math.max(1, Math.round(srcW * scale));
        int drawH = Math.max(1, Math.round(srcH * scale));

        float[] pf = computePivotAndFrac(hAnchor, vAnchor);
        float pivotLocalX = pf[0];
        float pivotLocalY = pf[1];
        float anchorFracX = pf[2];
        float anchorFracY = pf[3];

        float[] result = new float[] { (float) absX, (float) absY, (float) drawW, (float) drawH, pivotLocalX,
                pivotLocalY, anchorFracX, anchorFracY };

        // 更新缓存
        this.cachedAnchorCtx = result;
        this.cachedSrcW = srcW;
        this.cachedSrcH = srcH;
        this.cachedScale = scale;
        this.cachedHAnchor = hAnchor;
        this.cachedVAnchor = vAnchor;
        this.anchorCtxDirty = false;

        return result;
    }

    /**
     * 返回控件的锚点在窗口坐标系下的整型坐标 {anchorX, anchorY}，便于上层直接使用整数像素值。
     */
    public int[] getAnchorPoint(int srcW, int srcH, float scale) {
        float[] ctx = computeAnchorContext(srcW, srcH, scale, getHorizontalAnchor(), getVerticalAnchor());
        int ax = Math.round(ctx[0] + ctx[4]);
        int ay = Math.round(ctx[1] + ctx[5]);
        return new int[] { ax, ay };
    }

    /**
     * 在控件的锚点位置（即控件的绝对左上角）绘制按比例缩放的纹理。
     *
     * @param context  渲染上下文
     * @param id       纹理标识
     * @param srcU     源区域在图集中的 X
     * @param srcV     源区域在图集中的 Y
     * @param srcW     源区域宽度
     * @param srcH     源区域高度
     * @param scale    缩放倍数（1.0 = 原始大小）
     * @param textureW 纹理图集宽度
     * @param textureH 纹理图集高度
     */
    protected void drawTextureAtAnchorScaled(DrawContext context, Identifier id, int srcU, int srcV, int srcW,
            int srcH, float scale, int textureW, int textureH) {
        drawTextureAtAnchorScaled(context, id, srcU, srcV, srcW, srcH, scale, textureW, textureH, getHorizontalAnchor(),
                getVerticalAnchor());
    }

    /**
     * 在控件的锚点位置绘制按比例缩放的纹理，并允许指定用于缩放的锚点（水平/垂直）。
     * 锚点决定缩放后的图像相对于控件本地坐标的对齐点（例如 CENTER 会以控件中心对齐）。
     */
    protected void drawTextureAtAnchorScaled(DrawContext context, Identifier id, int srcU, int srcV, int srcW,
            int srcH, float scale, int textureW, int textureH, HAnchor hAnchor, VAnchor vAnchor) {
        if (id == null || srcW <= 0 || srcH <= 0)
            return;
        float[] ctx = computeAnchorContext(srcW, srcH, scale, hAnchor, vAnchor);
        float absX = ctx[0];
        float absY = ctx[1];
        int drawW = Math.round(ctx[2]);
        int drawH = Math.round(ctx[3]);
        float pivotLocalX = ctx[4];
        float pivotLocalY = ctx[5];
        float anchorFracX = ctx[6];
        float anchorFracY = ctx[7];

        int dx = Math.round(absX + pivotLocalX - drawW * anchorFracX);
        int dy = Math.round(absY + pivotLocalY - drawH * anchorFracY);

        context.drawTexture(id, dx, dy, drawW, drawH, srcU, srcV, srcW, srcH, textureW, textureH);
    }

    /**
     * 渲染控件（默认会先绘制背景）。
     *
     * @param context 渲染上下文
     * @param mouseX  鼠标 X
     * @param mouseY  鼠标 Y
     * @param delta   渲染插值
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!isVisible())
            return;
        int parentX = getParentX();
        int parentY = getParentY();
        int parentW = getParentWidth();
        int parentH = getParentHeight();
        int absX = getAnchoredX(parentX, parentY, parentW, parentH) + getLocalX();
        int absY = getAnchoredY(parentX, parentY, parentW, parentH) + getLocalY();

        renderBackground(context, absX, absY, getWidth(), getHeight());
        renderContent(context, absX, absY, mouseX, mouseY, delta);
    }

    /**
     * 渲染控件内容，子类可覆盖以实现自定义渲染逻辑。
     * 
     * @param context 渲染上下文
     * @param absX    绝对 X 坐标（像素）
     * @param absY    绝对 Y 坐标（像素）
     * @param mouseX  鼠标 X 坐标（像素）
     * @param mouseY  鼠标 Y 坐标（像素）
     * @param delta   渲染插值（通常为 1.0f）
     */
    protected void renderContent(DrawContext context, int absX, int absY, int mouseX, int mouseY, float delta) {
        if (this.child == null)
            return;
        ((Control) this.child).renderContent(context, absX, absY, mouseX, mouseY, delta);
    }

    /**
     * 设置背景纹理以及在纹理图集中的区域。
     * 
     * @param id   纹理标识符（通常是 new Identifier(modid, "textures/gui/atlas.png") 或资源条目）
     * @param u    在图集中的 x 偏移（像素）
     * @param v    在图集中的 y 偏移（像素）
     * @param w    背景绘制宽度（像素）
     * @param h    背景绘制高度（像素）
     * @param texW 纹理图集宽度（像素）——用于正确计算 UV
     * @param texH 纹理图集高度（像素）
     */
    public void setBackground(Identifier id, int u, int v, int w, int h, int texW, int texH) {
        this.background = id;
        this.bgRegion = null;
        this.bgU = u;
        this.bgV = v;
        this.bgWidth = w;
        this.bgHeight = h;
        this.textureWidth = texW;
        this.textureHeight = texH;
    }

    /**
     * 将背景设置为指定的 {@link TextureRegion}。
     *
     * @param region 背景纹理区域（可为 null，不会改变当前背景）
     */
    public void setBackground(TextureRegion region) {
        if (region == null)
            return;
        this.background = region.getIdentifier();
        this.bgRegion = region;
        this.bgU = region.getU();
        this.bgV = region.getV();
        this.bgWidth = region.getW();
        this.bgHeight = region.getH();
        if (region.getTextureWidth() > 0)
            this.textureWidth = region.getTextureWidth();
        if (region.getTextureHeight() > 0)
            this.textureHeight = region.getTextureHeight();
        this.bgAlpha = region.getAlpha();
    }

    /**
     * 设置九宫格缩放模式（影响在控件尺寸与源尺寸不一致时角/边的目标像素计算）。
     * 
     * @param mode 九宫格缩放模式
     */
    public void setNineSliceMode(NineSliceScaleMode mode) {
        if (mode != null)
            this.nineSliceMode = mode;
    }

    /** 设置九宫格的最小像素厚度（用于 MINIMUM 或 CLAMPED 模式） */
    public void setNineSliceMinPx(int minPx) {
        this.nineSliceMinPx = Math.max(0, minPx);
    }

    /**
     * 设置九宫格的最大像素厚度（用于 CLAMPED 模式）。
     * 
     * @param maxPx 最大像素厚度（0 表示不限制）
     */
    public void setNineSliceMaxPx(int maxPx) {
        this.nineSliceMaxPx = Math.max(0, maxPx);
    }

    /**
     * 设置背景透明度（0.0 - 完全透明, 1.0 - 完全不透明）。
     * 
     * @param alpha 透明度值（0.0 - 1.0）
     */
    public void setBackgroundAlpha(float alpha) {
        this.bgAlpha = Math.max(0f, Math.min(1f, alpha));
    }

    /**
     * 清除背景图
     */
    public void clearBackground() {
        this.background = null;
    }

    /**
     * 渲染背景到指定位置与大小（会拉伸/裁切到目标宽高）。
     * 
     * @param context 渲染上下文
     * @param x       背景绘制位置 X（像素）
     * @param y       背景绘制位置 Y（像素）
     * @param width   背景绘制宽度（像素）
     * @param height  背景绘制高度（像素）
     */
    protected void renderBackground(DrawContext context, int x, int y, int width, int height) {
        if (this.background == null)
            return;
        try {
            // 缓存常用字段到局部变量，减少多次字段访问
            final Identifier localBackground = this.background;
            final int localBgU = this.bgU;
            final int localBgV = this.bgV;
            final int localBgWidth = this.bgWidth;
            final int localBgHeight = this.bgHeight;
            final int localTextureW = this.textureWidth;
            final int localTextureH = this.textureHeight;
            final float localBgAlpha = this.bgAlpha;

            int drawW = width > 0 ? width : localBgWidth;
            int drawH = height > 0 ? height : localBgHeight;
            if (drawW <= 0 || drawH <= 0)
                return;
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, localBgAlpha);

            int srcW = localBgWidth > 0 ? localBgWidth : drawW;
            int srcH = localBgHeight > 0 ? localBgHeight : drawH;

            if (this.bgRegion != null && this.bgRegion.hasInsets()) {
                int left = this.bgRegion.getInsetLeft();
                int top = this.bgRegion.getInsetTop();
                int right = this.bgRegion.getInsetRight();
                int bottom = this.bgRegion.getInsetBottom();

                int srcTotalW = srcW;
                int srcTotalH = srcH;

                int dstLeft;
                int dstTop;
                int dstRight;
                int dstBottom;

                dstLeft = Math.min(computeInsetForMode(left, srcTotalW, drawW, srcTotalW), drawW);
                dstTop = Math.min(computeInsetForMode(top, srcTotalH, drawH, srcTotalH), drawH);
                dstRight = Math.min(computeInsetForMode(right, srcTotalW, drawW - dstLeft, srcTotalW),
                        drawW - dstLeft);
                dstBottom = Math.min(computeInsetForMode(bottom, srcTotalH, drawH - dstTop, srcTotalH),
                        drawH - dstTop);

                int dstCenterW = Math.max(0, drawW - dstLeft - dstRight);
                int dstCenterH = Math.max(0, drawH - dstTop - dstBottom);

                int sx0 = this.bgU;
                int sy0 = this.bgV;
                int sx1 = sx0 + left;
                int sx2 = sx0 + srcTotalW - right;
                int sy1 = sy0 + top;
                int sy2 = sy0 + srcTotalH - bottom;

                context.drawTexture(localBackground, x, y, dstLeft, dstTop, sx0, sy0, left, top, localTextureW,
                        localTextureH);
                context.drawTexture(localBackground, x + dstLeft + dstCenterW, y, dstRight, dstTop, sx2, sy0, right,
                        top, localTextureW, localTextureH);
                context.drawTexture(localBackground, x, y + dstTop + dstCenterH, dstLeft, dstBottom, sx0, sy2, left,
                        bottom, localTextureW, localTextureH);
                context.drawTexture(localBackground, x + dstLeft + dstCenterW, y + dstTop + dstCenterH, dstRight,
                        dstBottom, sx2, sy2, right, bottom, localTextureW, localTextureH);

                if (dstCenterW > 0)
                    context.drawTexture(localBackground, x + dstLeft, y, dstCenterW, dstTop, sx1, sy0,
                            srcTotalW - left - right, top, localTextureW, localTextureH);
                if (dstCenterW > 0)
                    context.drawTexture(localBackground, x + dstLeft, y + dstTop + dstCenterH, dstCenterW, dstBottom,
                            sx1, sy2, srcTotalW - left - right, bottom, localTextureW, localTextureH);
                if (dstCenterH > 0)
                    context.drawTexture(localBackground, x, y + dstTop, dstLeft, dstCenterH, sx0, sy1, left,
                            srcTotalH - top - bottom, localTextureW, localTextureH);
                if (dstCenterH > 0)
                    context.drawTexture(localBackground, x + dstLeft + dstCenterW, y + dstTop, dstRight, dstCenterH,
                            sx2, sy1, right, srcTotalH - top - bottom, localTextureW, localTextureH);
                if (dstCenterW > 0 && dstCenterH > 0)
                    context.drawTexture(localBackground, x + dstLeft, y + dstTop, dstCenterW, dstCenterH, sx1, sy1,
                            srcTotalW - left - right, srcTotalH - top - bottom, localTextureW, localTextureH);
            } else {
                context.drawTexture(localBackground, x, y, drawW, drawH, localBgU, localBgV, srcW, srcH,
                        localTextureW, localTextureH);
            }

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        } catch (Throwable ignored) {
        }
    }

    /**
     * 计算九宫格缩放时目标像素的 inset（替代原来的 BiFunction 分配）。
     *
     * @param inset     源 inset
     * @param totalSrc  源总宽/高
     * @param drawSize  目标 draw 大小（可用来按比例缩放）
     * @param srcTotalW 源总宽度（用于比例计算）
     * @return 目标像素厚度
     */
    private int computeInsetForMode(int inset, int totalSrc, int drawSize, int srcTotalW) {
        if (inset <= 0)
            return 0;
        switch (this.nineSliceMode) {
            case NONE:
                return Math.min(inset, totalSrc);
            case PROPORTIONAL: {
                float scaleX = (float) drawSize / (float) srcTotalW;
                int v = Math.max(1, Math.round(inset * scaleX));
                return v;
            }
            case MINIMUM:
                return Math.max(this.nineSliceMinPx, Math.min(inset, totalSrc));
            case CLAMPED: {
                float scale = (float) drawSize / (float) srcTotalW;
                int v = Math.round(inset * scale);
                v = Math.max(this.nineSliceMinPx, v);
                v = Math.min(this.nineSliceMaxPx, Math.min(v, totalSrc));
                return v;
            }
            default:
                return Math.min(inset, totalSrc);
        }
    }

    /**
     * 设置子控件（通常由容器或其他控件调用）。
     * 
     * @param child 子控件实例
     */
    public void setChild(UIElement child) {
        this.child = child;
        this.anchorCtxDirty = true;
    }

    /**
     * 私有辅助：基于水平/垂直锚点计算 pivotLocalX/pivotLocalY 与 anchor fraction X/Y。
     * 返回数组：{ pivotLocalX, pivotLocalY, anchorFracX, anchorFracY }
     */
    private float[] computePivotAndFrac(HAnchor hAnchor, VAnchor vAnchor) {
        float pivotLocalX;
        switch (hAnchor) {
            case RIGHT:
                pivotLocalX = getWidth();
                break;
            case CENTER:
                pivotLocalX = getWidth() / 2f;
                break;
            case LEFT:
            default:
                pivotLocalX = 0f;
                break;
        }

        float pivotLocalY;
        switch (vAnchor) {
            case BOTTOM:
                pivotLocalY = getHeight();
                break;
            case MIDDLE:
                pivotLocalY = getHeight() / 2f;
                break;
            case TOP:
            default:
                pivotLocalY = 0f;
                break;
        }

        float anchorFracX = (hAnchor == HAnchor.CENTER) ? 0.5f : (hAnchor == HAnchor.RIGHT ? 1f : 0f);
        float anchorFracY = (vAnchor == VAnchor.MIDDLE) ? 0.5f : (vAnchor == VAnchor.BOTTOM ? 1f : 0f);

        return new float[] { pivotLocalX, pivotLocalY, anchorFracX, anchorFracY };
    }
}