package org.fish.uitoolkit;

import org.fish.uitoolkit.utils.Regions;
import org.fish.uitoolkit.utils.TextureRegion;

import net.minecraft.client.gui.DrawContext;

public class Progress extends Control {
    protected float value = 0.0f;
    protected float maxValue = 1.0f;
    protected final Image borderImage;
    protected final Image fillImage;

    /**
     * 创建一个新的进度条控件。
     * 
     * @param owner 控件的所有者，通常是一个 {@link Canvas} 或 {@link Panel}。
     */
    public Progress(Object owner) {
        super(owner);
        borderImage = new Image(this, Regions.WIDGET_PROGRESS_BORDER);
        borderImage.setDrawMode(Image.DrawMode.SCALE);
        fillImage = new Image(this, Regions.WIDGET_PROGRESS_FILL);
        fillImage.setDrawMode(Image.DrawMode.SCALE);
    }

    /**
     * 创建一个新的进度条控件，并设置初始值。
     * 
     * @param owner 控件的所有者，通常是一个 {@link Canvas} 或 {@link Panel}。
     * @param value 初始值，范围应在 0.0f 到 maxValue 之间，默认为 1.0f。
     */
    public Progress(Object owner, float value) {
        this(owner);
        setValue(value);
    }

    /**
     * 创建一个新的进度条控件，并设置初始值和最大值。
     * 
     * @param owner    控件的所有者，通常是一个 {@link Canvas} 或 {@link Panel}。
     * @param value    初始值，范围应在 0.0f 到 maxValue 之间，默认为 1.0f。
     * @param maxValue 最大值，默认为 1.0f。
     */
    public Progress(Object owner, float value, float maxValue) {
        this(owner);
        setValue(value);
        setMaxValue(maxValue);
    }

    /**
     * 创建一个新的进度条控件，并设置初始值、最大值和缩放比例。
     * 
     * @param owner    控件的所有者，通常是一个 {@link Canvas} 或 {@link Panel}。
     * @param value    初始值，范围应在 0.0f 到 maxValue 之间，默认为 1.0f。
     * @param maxValue 最大值，默认为 1.0f。
     * @param scale    缩放比例，默认为 1.0f。
     */
    public Progress(Object owner, float value, float maxValue, float scale) {
        this(owner, value, maxValue);
        setScale(scale);
    }

    /**
     * 设置进度条的当前值。
     * 
     * @param value 当前值，范围应在 0.0f 到 maxValue 之间，默认为 1.0f。
     */
    public void setValue(float value) {
        this.value = Math.max(0.0f, Math.min(value, maxValue));
        updateFillImage();
    }

    /**
     * 更新填充图像的剪裁比例。
     */
    protected void updateFillImage() {
        float clip = maxValue > 0 ? value / maxValue : 0.0f;
        fillImage.setClip(clip);
    }

    /**
     * 获取当前进度条的值。
     */
    public float getValue() {
        return value;
    }

    /**
     * 设置进度条的最大值。
     * 
     * @param maxValue 最大值，默认为 1.0f。
     */
    public void setMaxValue(float maxValue) {
        this.maxValue = Math.max(0.0f, maxValue);
        updateFillImage();
    }

    /**
     * 获取进度条的最大值。
     * 
     * @return 最大值，默认为 1.0f。
     */
    public float getMaxValue() {
        return maxValue;
    }

    /**
     * 设置进度条的边框和填充图像的缩放比例。
     * 
     * @param scale 缩放比例，默认为 1.0f。
     */
    public void setScale(float scale) {
        borderImage.setScale(scale);
        fillImage.setScale(scale);
    }

    /**
     * 设置进度条的填充图像。
     * 
     * @param region 填充图像的纹理区域。
     */
    public void setProgressFillImage(TextureRegion region) {
        fillImage.setBackground(region);
        updateFillImage();
    }

    /**
     * 设置进度条的边框图像。
     * 
     * @param region 边框图像的纹理区域。
     */
    public void setProgressBorderImage(TextureRegion region) {
        borderImage.setBackground(region);
    }

    @Override
    public void setMargins(int left, int top, int right, int bottom) {
        super.setMargins(left, top, right, bottom);
        borderImage.setMargins(left, top, right, bottom);
        fillImage.setMargins(left, top, right, bottom);
    }

    @Override
    protected void renderContent(DrawContext context, int absX, int absY, int mouseX, int mouseY, float delta) {
        super.renderContent(context, absX, absY, mouseX, mouseY, delta);
        borderImage.render(context, mouseX, mouseY, delta);
        fillImage.render(context, mouseX, mouseY, delta);
    }
}
