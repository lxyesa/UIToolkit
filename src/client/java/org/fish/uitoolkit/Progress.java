package org.fish.uitoolkit;

import org.fish.uitoolkit.utils.Regions;
import org.fish.uitoolkit.utils.TextureRegion;

import net.minecraft.text.Text;

public class Progress extends Container {
    protected float value = 0.0f;
    protected float maxValue = 1.0f;
    protected Image borderImage;
    protected Image fillImage;
    protected Label label;

    /**
     * 创建一个新的进度条控件。
     * 
     * @param owner 控件的所有者，通常是一个 {@link Canvas} 或 {@link Panel}。
     */
    public Progress(Object owner) {
        super(owner);
        this.owner = owner;

        borderImage = new Image(this, Regions.WIDGET_PROGRESS_BORDER);
        borderImage.setDrawMode(Image.DrawMode.SCALE);
        fillImage = new Image(this, Regions.WIDGET_PROGRESS_FILL);
        fillImage.setDrawMode(Image.DrawMode.SCALE);
        label = new Label(this, "");
        label.setFontSize(5);
        label.setVisible(false);

        this.clearBackground();
        this.propagateInvalidateChildren();
    }

    /**
     * 创建一个新的进度条控件，并设置初始值。
     * 
     * @param owner 控件的所有者，通常是一个 {@link Canvas} 或 {@link Panel}。
     * @param value 初始值，范围应在 0.0f 到 maxValue 之间，默认为 1.0f。
     */
    public Progress(Object owner, float value) {
        this(owner);
        this.value = Math.max(0.0f, Math.min(value, maxValue));
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
        this.value = Math.max(0.0f, Math.min(value, maxValue));
        this.maxValue = Math.max(0.0f, maxValue);
        updateFillImage();
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
        this(owner);
        this.value = Math.max(0.0f, Math.min(value, maxValue));
        this.maxValue = Math.max(0.0f, maxValue);
        updateFillImage();
        this.setScale(scale);
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
     * 设置进度条的标题文本。
     * 
     * @param header 标题文本，若为 null 或空字符串则隐藏标题。
     */
    public void setHeader(String header) {
        if (label != null) {
            label.setText(header);
            label.setVerticalAnchor(VAnchor.MIDDLE);
            label.setHorizontalAnchor(HAnchor.CENTER);
            label.setVisible(header != null && !header.isEmpty());

            this.propagateInvalidateChildren();
        }
    }

    /**
     * 设置进度条的标题文本。
     * 
     * @param header 标题文本，若为 null 或空字符串则隐藏标题。
     */
    public void setHeader(Text header) {
        if (label != null) {
            label.setText(header);
            label.setVisible(header != null && !header.getString().isEmpty());
        }
    }

    public void setFontSize(int size) {
        if (label != null) {
            label.setFontSize(size);
            this.propagateInvalidateChildren();
        }
    }

    /**
     * 更新填充图像的剪裁比例。
     */
    protected void updateFillImage() {
        if (fillImage == null)
            return;
        float clip = maxValue > 0 ? value / maxValue : 0.0f;
        try {
            fillImage.setClip(clip);
        } catch (Throwable ignored) {
        }
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
        if (borderImage != null) {
            borderImage.setScale(scale);
        }
        if (fillImage != null) {
            fillImage.setScale(scale);
        }
    }

    /**
     * 设置进度条的填充图像。
     * 
     * @param region 填充图像的纹理区域。
     */
    public void setProgressFillImage(TextureRegion region) {
        if (fillImage != null) {
            fillImage.setBackground(region);
            updateFillImage();
        }
    }

    /**
     * 设置进度条的边框图像。
     * 
     * @param region 边框图像的纹理区域。
     */
    public void setProgressBorderImage(TextureRegion region) {
        if (borderImage != null) {
            borderImage.setBackground(region);
        }
    }

    @Override
    public void setBackgroundColor(int r, int g, int b, int a) {
        fillImage.setBackgroundColor(r, g, b, a);
    }
}
