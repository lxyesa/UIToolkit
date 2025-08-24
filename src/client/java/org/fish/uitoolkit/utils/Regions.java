package org.fish.uitoolkit.utils;

import net.minecraft.util.Identifier;

/** 常用纹理区域常量集合（便于在代码中复用） */
public class Regions {
        private static final int TEXTURE_W = 1024;
        private static final int TEXTURE_H = 1024;
        // 示例：一个名为 uitoolkit 的资源域中 widgets.png 的一个 16x16 区域
        public static final TextureRegion WIDGET_PANEL = new TextureRegion(
                        new Identifier("uitoolkit", "textures/gui/widgets-sheet.png"), 0, 0, 16, 16, TEXTURE_W,
                        TEXTURE_H)
                        .withInsets(4);
        public static final TextureRegion WIDGET_PROGRESS_BORDER = new TextureRegion(
                        new Identifier("uitoolkit", "textures/gui/widgets-sheet.png"), 16, 0, 181, 5, TEXTURE_W,
                        TEXTURE_H);
        public static final TextureRegion WIDGET_PROGRESS_FILL = new TextureRegion(
                        new Identifier("uitoolkit", "textures/gui/widgets-sheet.png"), 16, 5, 181, 5, TEXTURE_W,
                        TEXTURE_H);
        public static final TextureRegion WIDGET_PROGRESS_BORDER_TILE_LEFT = new TextureRegion(
                        new Identifier("uitoolkit", "textures/gui/widgets-sheet.png"), 16, 30, 10, 5, TEXTURE_W,
                        TEXTURE_H);
        public static final TextureRegion WIDGET_PROGRESS_BORDER_TILE = new TextureRegion(
                        new Identifier("uitoolkit", "textures/gui/widgets-sheet.png"), 26, 30, 10, 5, TEXTURE_W,
                        TEXTURE_H);
        public static final TextureRegion WIDGET_PROGRESS_BORDER_TILE_RIGHT = new TextureRegion(
                        new Identifier("uitoolkit", "textures/gui/widgets-sheet.png"), 36, 30, 11, 5, TEXTURE_W,
                        TEXTURE_H);
        public static final TextureRegion WIDGET_PROGRESS_FILL_TILE_LEFT = new TextureRegion(
                        new Identifier("uitoolkit", "textures/gui/widgets-sheet.png"), 16, 35, 10, 5, TEXTURE_W,
                        TEXTURE_H);
        public static final TextureRegion WIDGET_PROGRESS_FILL_TILE = new TextureRegion(
                        new Identifier("uitoolkit", "textures/gui/widgets-sheet.png"), 26, 35, 10, 5, TEXTURE_W,
                        TEXTURE_H);
        public static final TextureRegion WIDGET_PROGRESS_FILL_TILE_RIGHT = new TextureRegion(
                        new Identifier("uitoolkit", "textures/gui/widgets-sheet.png"), 36, 35, 11, 5, TEXTURE_W,
                        TEXTURE_H);
}
