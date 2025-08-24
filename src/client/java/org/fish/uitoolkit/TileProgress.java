package org.fish.uitoolkit;

import org.fish.uitoolkit.Image.DrawMode;
import org.fish.uitoolkit.Panel.PanelType;
import org.fish.uitoolkit.utils.Regions;

public class TileProgress extends Container {
    protected float value = 0.0f;
    protected float maxValue = 1.0f;
    protected int tileCount = 1;
    protected Panel borderPanel = new Panel(this);
    protected Panel fillPanel = new Panel(this);

    public TileProgress(UIElement parent, int tileCount) {
        super(parent);

        if (tileCount < 1) {
            throw new IllegalArgumentException("Tile count must be at least 1");
        }

        this.tileCount = tileCount;
        this.borderPanel.clearBackground();
        this.borderPanel.setPanelType(PanelType.HORIZONTAL);
        this.fillPanel.clearBackground();
        this.fillPanel.setPanelType(PanelType.HORIZONTAL);

        Image left_tile = new Image(this.borderPanel, Regions.WIDGET_PROGRESS_BORDER_TILE_LEFT);
        left_tile.setDrawMode(DrawMode.STRETCH);
        Image left_tile_fill = new Image(this.fillPanel, Regions.WIDGET_PROGRESS_FILL_TILE_LEFT);
        left_tile_fill.setDrawMode(DrawMode.STRETCH);

        for (int i = 0; i < tileCount; i++) {
            Image borderImage = new Image(this.borderPanel, Regions.WIDGET_PROGRESS_BORDER_TILE);
            borderImage.setDrawMode(DrawMode.STRETCH);
            borderImage.setPosition((int) borderPanel.getNextControlPosition().x, borderImage.getLocalY());

            Image fillImage = new Image(this.fillPanel, Regions.WIDGET_PROGRESS_FILL_TILE);
            fillImage.setDrawMode(DrawMode.STRETCH);
            fillImage.setPosition((int) fillPanel.getNextControlPosition().x, fillImage.getLocalY());
        }

        Image right_tile = new Image(this.borderPanel, Regions.WIDGET_PROGRESS_BORDER_TILE_RIGHT);
        right_tile.setDrawMode(DrawMode.STRETCH);
        right_tile.setPosition((int) borderPanel.getNextControlPosition().x, getLocalY());

        Image right_tile_fill = new Image(this.fillPanel, Regions.WIDGET_PROGRESS_FILL_TILE_RIGHT);
        right_tile_fill.setDrawMode(DrawMode.STRETCH);
        right_tile_fill.setPosition((int) fillPanel.getNextControlPosition().x, right_tile_fill.getLocalY());

    }

    @Override
    public void setScale(float scale) {
        this.borderPanel.setScale(scale);
        this.fillPanel.setScale(scale);
    }

    /**
     * 设置边框面板内所有子项的颜色（RGB, alpha 默认为 255）。
     */
    public void setBorderColor(int r, int g, int b) {
        setBorderColor(r, g, b, 255);
    }

    /**
     * 设置边框面板内所有子项的颜色（RGBA）。
     */
    public void setBorderColor(int r, int g, int b, int a) {
        applyColorToPanel(this.borderPanel, r, g, b, a);
    }

    /**
     * 设置填充面板内所有子项的颜色（RGB, alpha 默认为 255）。
     */
    public void setFillColor(int r, int g, int b) {
        setFillColor(r, g, b, 255);
    }

    /**
     * 设置填充面板内所有子项的颜色（RGBA）。
     */
    public void setFillColor(int r, int g, int b, int a) {
        applyColorToPanel(this.fillPanel, r, g, b, a);
    }

    private void applyColorToPanel(Panel panel, int r, int g, int b, int a) {
        if (panel == null)
            return;
        for (UIElement child : panel.getChildren()) {
            if (child == null)
                continue;
            if (child instanceof Control) {
                ((Control) child).setBackgroundColor(r, g, b, a);
            }
        }
    }

    @Override
    public void setHorizontalAnchor(HAnchor a) {
        super.setHorizontalAnchor(a);
        this.borderPanel.setHorizontalAnchor(a);
        this.fillPanel.setHorizontalAnchor(a);
    }

    @Override
    public void setVerticalAnchor(VAnchor a) {
        super.setVerticalAnchor(a);
        this.borderPanel.setVerticalAnchor(a);
        this.fillPanel.setVerticalAnchor(a);
    }
}
