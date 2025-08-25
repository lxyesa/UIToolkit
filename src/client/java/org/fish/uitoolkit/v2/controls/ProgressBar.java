package org.fish.uitoolkit.v2.controls;

import org.fish.uitoolkit.utils.Regions;
import org.fish.uitoolkit.v2.RenderType;
import org.fish.uitoolkit.v2.components.BackgroundComponent;
import org.fish.uitoolkit.v2.components.PanelComponent;

import net.minecraft.client.gui.DrawContext;

public class ProgressBar extends ControlObject {
    private float progress;
    // animated displayed progress (for smooth transitions)
    private float animatedProgress = 0f;
    // units: fraction per second. If <= 0 then animation is disabled (instant).
    private float animationSpeed = 6f;
    private boolean animateEnabled = true;
    private final PanelComponent background_panel;
    private final PanelComponent fill_panel;

    /**
     * @param tileCount tile数量
     */
    public ProgressBar(int tileCount) {
        this.progress = 0;
        background_panel = (PanelComponent) this.addComponent(new PanelComponent());
        fill_panel = (PanelComponent) this.addComponent(new PanelComponent());

        // arrange children horizontally with no extra spacing; they will be placed
        // left-to-right
        background_panel.setOrientation(PanelComponent.Orientation.HORIZONTAL);
        background_panel.setSpacing(0);
        background_panel.setPadding(0, 0, 0, 0);
        background_panel.setAutoSizeHeight(visible);
        background_panel.setAutoSizeWidth(visible);

        fill_panel.setOrientation(PanelComponent.Orientation.HORIZONTAL);
        fill_panel.setSpacing(0);
        fill_panel.setPadding(0, 0, 0, 0);

        ControlObject left_bg = new ControlObject();
        left_bg.addComponent(new BackgroundComponent(left_bg)
                .setTexture(Regions.WIDGET_PROGRESS_BORDER_TILE_LEFT, RenderType.STRETCH));
        left_bg.getComponent(BackgroundComponent.class).updateOwnerSize();

        ControlObject left_fill = new ControlObject();
        left_fill.addComponent(new BackgroundComponent(left_fill)
                .setTexture(Regions.WIDGET_PROGRESS_FILL_TILE_LEFT, RenderType.STRETCH));
        left_fill.getComponent(BackgroundComponent.class).updateOwnerSize();

        ControlObject right_bg = new ControlObject();
        right_bg.addComponent(new BackgroundComponent(right_bg)
                .setTexture(Regions.WIDGET_PROGRESS_BORDER_TILE_RIGHT, RenderType.STRETCH));
        right_bg.getComponent(BackgroundComponent.class).updateOwnerSize();

        ControlObject right_fill = new ControlObject();
        right_fill.addComponent(new BackgroundComponent(right_fill)
                .setTexture(Regions.WIDGET_PROGRESS_FILL_TILE_RIGHT, RenderType.STRETCH));
        right_fill.getComponent(BackgroundComponent.class).updateOwnerSize();

        background_panel.addChild(left_bg);
        fill_panel.addChild(left_fill);

        for (int i = 0; i < tileCount; i++) {
            ControlObject center_bg = new ControlObject();
            center_bg.addComponent(new BackgroundComponent(center_bg)
                    .setTexture(Regions.WIDGET_PROGRESS_BORDER_TILE, RenderType.STRETCH));
            center_bg.getComponent(BackgroundComponent.class).updateOwnerSize();

            ControlObject center_fill = new ControlObject();
            center_fill.addComponent(new BackgroundComponent(center_fill)
                    .setTexture(Regions.WIDGET_PROGRESS_FILL_TILE, RenderType.STRETCH));
            center_fill.getComponent(BackgroundComponent.class).updateOwnerSize();

            background_panel.addChild(center_bg);
            fill_panel.addChild(center_fill);
        }

        background_panel.addChild(right_bg);
        fill_panel.addChild(right_fill);
    }

    public void setColor(int color) {
        for (ControlObject child : background_panel.getChildren()) {
            if (child == null)
                continue;
            BackgroundComponent bg = child.getComponent(BackgroundComponent.class);
            if (bg != null)
                bg.setColor(color);
        }

        for (ControlObject child : fill_panel.getChildren()) {
            if (child == null)
                continue;
            BackgroundComponent bg = child.getComponent(BackgroundComponent.class);
            if (bg != null)
                bg.setColor(color);
        }
    }

    @Override
    public void update(float tickDelta) {
        super.update(tickDelta);
        background_panel.update(this, tickDelta);
        fill_panel.update(this, tickDelta);
        // update animated progress towards target
        float target = Math.max(0f, Math.min(1f, this.progress));
        if (!animateEnabled || animationSpeed <= 0f) {
            animatedProgress = target;
        } else {
            // exponential smoothing: animated += (target - animated) * alpha
            // alpha = 1 - exp(-speed * dt) gives smooth non-linear easing (fast then slow)
            float alpha = 1f - (float) Math.exp(-animationSpeed * tickDelta);
            animatedProgress += (target - animatedProgress) * alpha;
            // clamp to [0,1] to avoid tiny numerical drift
            if (animatedProgress < 0f)
                animatedProgress = 0f;
            if (animatedProgress > 1f)
                animatedProgress = 1f;
        }
        float p = animatedProgress;
        int totalW = 0;
        for (ControlObject child : fill_panel.getChildren())
            totalW += child.getWidth();
        int filled = Math.round(totalW * p);
        int remaining = filled;
        for (ControlObject child : fill_panel.getChildren()) {
            if (child == null)
                continue;
            int cw = child.getWidth();
            BackgroundComponent bg = child.getComponent(BackgroundComponent.class);
            if (bg == null)
                continue;
            if (remaining <= 0) {
                // hide fully
                bg.clip(0f, 1f, BackgroundComponent.ClipType.FORWARD);
                bg.getOwner().setVisible(false);
            } else if (remaining >= cw) {
                // fully visible
                bg.clip(1f, 1f, BackgroundComponent.ClipType.FORWARD);
                remaining -= cw;
                bg.getOwner().setVisible(true);
            } else {
                float xPct = (float) remaining / (float) Math.max(1, cw);
                bg.clip(xPct, 1f, BackgroundComponent.ClipType.FORWARD);
                remaining = 0;
                // partially visible
                bg.getOwner().setVisible(true);
            }
        }
    }

    public void setProgress(float p) {
        this.progress = Math.max(0f, Math.min(1f, p));
    }

    public float getProgress() {
        return this.progress;
    }

    public void setAnimationSpeed(float speed) {
        this.animationSpeed = speed;
    }

    public float getAnimationSpeed() {
        return this.animationSpeed;
    }

    public void setAnimateEnabled(boolean v) {
        this.animateEnabled = v;
    }

    public boolean isAnimateEnabled() {
        return this.animateEnabled;
    }

    public float getAnimatedProgress() {
        return this.animatedProgress;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        super.render(context, tickDelta);
        background_panel.render(this, context, tickDelta);
        fill_panel.render(this, context, tickDelta);
    }
}
