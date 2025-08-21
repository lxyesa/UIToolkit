package org.fish.uitoolkit.utils;

import net.minecraft.util.Identifier;

/** 常用纹理区域常量集合（便于在代码中复用） */
public class Regions {
    // 示例：一个名为 uitoolkit 的资源域中 widgets.png 的一个 16x16 区域
    public static final TextureRegion WIDGET_PANEL = new TextureRegion(
            new Identifier("uitoolkit", "textures/gui/widgets-sheet.png"), 0, 0, 16, 16, 32, 16).withInsets(4);
}
