package org.fish.uitoolkit.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;

/**
 * 描述纹理图集中的一个子区域（source rectangle）及可选的 atlas 大小与 alpha。
 */
public class TextureRegion {
    private final Identifier id;
    private final int u;
    private final int v;
    private final int w;
    private final int h;
    private int textureW;
    private int textureH;
    private float alpha = 1f;

    // 九宫格 insets（像素），默认 0 表示无九宫格
    private int insetLeft = 0;
    private int insetTop = 0;
    private int insetRight = 0;
    private int insetBottom = 0;

    public TextureRegion(Identifier id, int u, int v, int w, int h) {
        this(id, u, v, w, h, 0, 0);
        tryAutoFillTextureSize();
    }

    public TextureRegion(Identifier id, int u, int v, int w, int h, int textureW, int textureH) {
        this.id = id;
        this.u = u;
        this.v = v;
        this.w = w;
        this.h = h;
        this.textureW = textureW;
        this.textureH = textureH;
    }

    public Identifier getIdentifier() {
        return id;
    }

    public int getU() {
        return u;
    }

    public int getV() {
        return v;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public int getTextureWidth() {
        return textureW;
    }

    public int getTextureHeight() {
        return textureH;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float a) {
        this.alpha = Math.max(0f, Math.min(1f, a));
    }

    public boolean hasInsets() {
        return insetLeft > 0 || insetTop > 0 || insetRight > 0 || insetBottom > 0;
    }

    public int getInsetLeft() {
        return insetLeft;
    }

    public int getInsetTop() {
        return insetTop;
    }

    public int getInsetRight() {
        return insetRight;
    }

    public int getInsetBottom() {
        return insetBottom;
    }


    /**
     * 设置九宫格内边距（insets），用于九宫格拉伸。
     */
    public TextureRegion withInsets(int left, int top, int right, int bottom) {
        this.insetLeft = Math.max(0, left);
        this.insetTop = Math.max(0, top);
        this.insetRight = Math.max(0, right);
        this.insetBottom = Math.max(0, bottom);
        return this;
    }

    /**
     * 设置所有四个方向的九宫格内边距（insets）为相同值。
     */
    public TextureRegion withInsets(int all) {
        return withInsets(all, all, all, all);
    }

    /**
     * 尝试在运行时从资源管理器读取纹理文件以自动填充 atlas 大小（若尚未设置）。
     * 失败时静默返回，调用方应回退到默认值。
     */
    public void tryAutoFillTextureSize() {
        if (this.textureW > 0 && this.textureH > 0)
            return;
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null)
                return;
            ResourceManager rm = client.getResourceManager();
            java.util.Optional<Resource> opt = rm.getResource(this.id);
            if (!opt.isPresent())
                return;
            Resource r = opt.get();
            try (InputStream is = r.getInputStream()) {
                NativeImage img = NativeImage.read(is);
                if (img != null) {
                    this.textureW = img.getWidth();
                    this.textureH = img.getHeight();
                    img.close();
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
