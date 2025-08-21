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
    private Identifier background = null;
    private int bgU = 0; // source x in texture atlas
    private int bgV = 0; // source y in texture atlas
    private int bgWidth = 0; // source width
    private int bgHeight = 0; // source height
    private int textureWidth = 256; // atlas width (default common size)
    private int textureHeight = 256; // atlas height
    private float bgAlpha = 1.0f; // background alpha (0..1)
    private org.fish.uitoolkit.utils.TextureRegion bgRegion = null;

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

    /** 当前使用的九宫格缩放模式（默认：PROPORTIONAL）。 */
    private NineSliceScaleMode nineSliceMode = NineSliceScaleMode.PROPORTIONAL;

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
    }

    /**
     * 设置水平锚点（HAnchor），用于相对于父容器定位。
     * 
     * @param a
     */
    public void setHorizontalAnchor(HAnchor a) {
        this.hAnchor = a;
    }

    /**
     * 设置垂直锚点（VAnchor），用于相对于父容器定位。
     *
     * @param a 垂直锚点值
     */
    public void setVerticalAnchor(VAnchor a) {
        this.vAnchor = a;
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
        // 如果 owner 有 invalidateLayout 方法，则通知其重新布局（例如 StackPanel）
        if (this.owner != null) {
            try {
                java.lang.reflect.Method m = this.owner.getClass().getMethod("invalidateLayout");
                if (m != null) {
                    m.setAccessible(true);
                    m.invoke(this.owner);
                }
            } catch (Throwable ignored) {
            }
        }
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
     * 设置控件外边距。
     *
     * @param left   左边距（像素）
     * @param top    上边距（像素）
     * @param right  右边距（像素）
     * @param bottom 下边距（像素）
     */

    public void setVisible(boolean v) {
        this.visible = v;
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
        return this.bgWidth > 0 ? this.bgWidth : 0;
    }

    /**
     * 默认高度回退：若子类未覆盖，则使用背景高度作为控件高度回退值。
     */
    @Override
    public int getHeight() {
        return this.bgHeight > 0 ? this.bgHeight : 0;
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
            int drawW = width > 0 ? width : this.bgWidth;
            int drawH = height > 0 ? height : this.bgHeight;
            if (drawW <= 0 || drawH <= 0)
                return;

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, this.bgAlpha);

            int srcW = this.bgWidth > 0 ? this.bgWidth : drawW;
            int srcH = this.bgHeight > 0 ? this.bgHeight : drawH;

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

                java.util.function.BiFunction<Integer, Integer, Integer> compute = (inset, totalSrc) -> {
                    if (inset <= 0)
                        return 0;
                    switch (this.nineSliceMode) {
                        case NONE:
                            return Math.min(inset, totalSrc);
                        case PROPORTIONAL: {
                            float scaleX = (float) drawW / (float) srcTotalW;
                            int v = Math.max(1, Math.round(inset * scaleX));
                            return v;
                        }
                        case MINIMUM:
                            return Math.max(this.nineSliceMinPx, Math.min(inset, totalSrc));
                        case CLAMPED: {
                            float scale = (float) drawW / (float) srcTotalW;
                            int v = Math.round(inset * scale);
                            v = Math.max(this.nineSliceMinPx, v);
                            v = Math.min(this.nineSliceMaxPx, Math.min(v, totalSrc));
                            return v;
                        }
                        default:
                            return Math.min(inset, totalSrc);
                    }
                };

                dstLeft = Math.min(compute.apply(left, srcTotalW), drawW);
                dstTop = Math.min(compute.apply(top, srcTotalH), drawH);
                dstRight = Math.min(compute.apply(right, srcTotalW), drawW - dstLeft);
                dstBottom = Math.min(compute.apply(bottom, srcTotalH), drawH - dstTop);

                int dstCenterW = Math.max(0, drawW - dstLeft - dstRight);
                int dstCenterH = Math.max(0, drawH - dstTop - dstBottom);

                int sx0 = this.bgU;
                int sy0 = this.bgV;
                int sx1 = sx0 + left;
                int sx2 = sx0 + srcTotalW - right;
                int sy1 = sy0 + top;
                int sy2 = sy0 + srcTotalH - bottom;

                context.drawTexture(this.background, x, y, dstLeft, dstTop, sx0, sy0, left, top, this.textureWidth,
                        this.textureHeight);
                context.drawTexture(this.background, x + dstLeft + dstCenterW, y, dstRight, dstTop, sx2, sy0, right,
                        top, this.textureWidth, this.textureHeight);
                context.drawTexture(this.background, x, y + dstTop + dstCenterH, dstLeft, dstBottom, sx0, sy2, left,
                        bottom, this.textureWidth, this.textureHeight);
                context.drawTexture(this.background, x + dstLeft + dstCenterW, y + dstTop + dstCenterH, dstRight,
                        dstBottom, sx2, sy2, right, bottom, this.textureWidth, this.textureHeight);

                if (dstCenterW > 0)
                    context.drawTexture(this.background, x + dstLeft, y, dstCenterW, dstTop, sx1, sy0,
                            srcTotalW - left - right, top, this.textureWidth, this.textureHeight);
                if (dstCenterW > 0)
                    context.drawTexture(this.background, x + dstLeft, y + dstTop + dstCenterH, dstCenterW, dstBottom,
                            sx1, sy2, srcTotalW - left - right, bottom, this.textureWidth, this.textureHeight);
                if (dstCenterH > 0)
                    context.drawTexture(this.background, x, y + dstTop, dstLeft, dstCenterH, sx0, sy1, left,
                            srcTotalH - top - bottom, this.textureWidth, this.textureHeight);
                if (dstCenterH > 0)
                    context.drawTexture(this.background, x + dstLeft + dstCenterW, y + dstTop, dstRight, dstCenterH,
                            sx2, sy1, right, srcTotalH - top - bottom, this.textureWidth, this.textureHeight);
                if (dstCenterW > 0 && dstCenterH > 0)
                    context.drawTexture(this.background, x + dstLeft, y + dstTop, dstCenterW, dstCenterH, sx1, sy1,
                            srcTotalW - left - right, srcTotalH - top - bottom, this.textureWidth, this.textureHeight);
            } else {
                context.drawTexture(this.background, x, y, drawW, drawH, this.bgU, this.bgV, srcW, srcH,
                        this.textureWidth, this.textureHeight);
            }

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        } catch (Throwable ignored) {
        }
    }

    /**
     * 设置子控件（通常由容器或其他控件调用）。
     * 
     * @param child 子控件实例
     */
    public void setChild(UIElement child) {
        this.child = child;
    }
}