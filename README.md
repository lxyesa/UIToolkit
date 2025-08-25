# UIToolkit (v2) - API 文档与使用示例

本 README 总结了仓库中 `org.fish.uitoolkit.v2` 包及其子包的全部公开 API，并提供常用的使用示例。

## 目标

- 提供每个类 / 接口的简要说明与公开构造器、方法、字段列表。
- 提供可复制的使用示例，演示如何在 Minecraft 环境中构建简单 UI（创建 `UIManager`、添加 `Label`、`ProgressBar` 等）。

> 说明：示例代码基于项目中的类，渲染/更新入口使用 Minecraft 的 `DrawContext` 与游戏主循环（需在 mod 的客户端渲染回调中调用）。

## 包结构总览

- org.fish.uitoolkit.v2
	- `UIManager` - UI 管理器，保存 root 控件并负责 render/update
	- `RenderType` - 渲染模式常量（位标志）
	- `controls` - 控件集合：`ControlObject`, `Label`, `ProgressBar`
	- `components` - 组件集合：`PositionComponent`, `ScaleComponent`, `BackgroundComponent`, `PanelComponent`, `CommonComponent`
	- `interfaces` - 基础接口/抽象：`IComponent`, `IParentable`
- org.fish.uitoolkit.utils
	- `TextureRegion`, `Regions`, `Vector2d`

## API 详细说明

下面按文件列出公开 API（构造器 / 公共方法 / 常用字段），省略私有实现细节。

### UIManager

- 包: `org.fish.uitoolkit.v2`
- 用途: 管理 root `ControlObject` 并在每帧中更新与渲染。

公开成员：
- `UIManager()` - 构造器，初始化 root，默认 size 为 320x240 并将 root 放在 (0,0)。
- `ControlObject getRoot()` - 返回 root 控件。
- `void initDemo(Runnable r)` - 可选的 demo 初始化回调（如果传入非空 Runnable，将立即运行）。
- `void render(DrawContext context, float tickDelta)` - 每帧调用，调整 root 大小并调用 root.update / root.render。

### RenderType

- 包: `org.fish.uitoolkit.v2`
- 用途: 渲染标志位。

常量：
- `STRETCH` (1)
- `REPEAT` (1 << 1)
- `CLAMP` (1 << 2)
- `NINESLICE` (1 << 3)

可组合使用，例如：`RenderType.STRETCH | RenderType.REPEAT`。

### IComponent (抽象类)

- 包: `org.fish.uitoolkit.v2.interfaces`
- 用途: 所有组件的基类。组件可选保存对 owner (`ControlObject`) 的引用。

方法（公开 / 重要）：
- `ControlObject getOwner()`
- `void setOwner(ControlObject owner)`
- `abstract void update(ControlObject owner, float tickDelta)` - 每帧更新逻辑
- `void render(ControlObject owner, DrawContext context, float tickDelta)` - 可选绘制步骤（默认空实现）
- `int getPriority()` - 执行优先级（数值越小越先运行），默认 0。

### IParentable

- 接口：`ControlObject getParent(); void setParent(ControlObject p);`

### ControlObject

- 包: `org.fish.uitoolkit.v2.controls`
- 用途: 核心 UI 控件容器，持有组件、支持层级关系、update/render 调用。

主要公开 API：
- 构造器：`ControlObject()` - 默认自动添加 `PositionComponent` 与 `ScaleComponent`。
- 组件管理：
	- `IComponent addComponent(IComponent comp)`
	- `<T> T getComponent(Class<T> cls)`
	- `void removeComponent(Class<?> cls)`
- 生命周期：
	- `void update(float tickDelta)` - 更新组件（按 priority 排序）
	- `void render(DrawContext context, float tickDelta)` - 渲染组件
- 布局/属性：
	- `void setPosition(int x, int y)` / `void setLocalPosition(int lx, int ly)`
	- `void setSize(int w, int h)`
	- `int getX()` / `int getY()` / `int getWidth()` / `int getHeight()`
	- `int getCenterX()` / `int getCenterY()` / `int[] getCenter()`
	- 可通过 `addChild(ControlObject child)` 来使用 `PanelComponent` 进行子控件管理
	- `List<ControlObject> getChildren()` 返回子控件（若无 PanelComponent 则返回空列表）
	- 可见性：`void setVisible(boolean v)` / `boolean getVisible()`

- 便捷位置/锚点映射（委托到 `PositionComponent`）：
	- `PositionComponent getPositionComponent()`
	- `void setAnchor(PositionComponent.Anchor a)`
	- `void setParentAnchor(PositionComponent.Anchor a)`
	- `void setParentAnchorNormalized(float ax, float ay)` / `void clearParentAnchorNormalized()`
	- `void setPivotNormalized(float px, float py)` / `void clearPivotNormalized()`
	- `void setPivotPreset(PositionComponent.Anchor a)`
	- `void setParentAnchorPreset(PositionComponent.Anchor a)`
	- `void setAlignment(PositionComponent.Anchor parentPreset, PositionComponent.Anchor pivotPreset)`


备注：`ControlObject` 使用 `PositionComponent` / `ScaleComponent` 提供位置与尺寸行为。

### components.PositionComponent

- 作用：管理控件的位置、锚点（preset 或 normalized）、pivot（基点）与最终绝对位置计算。

主要方法/字段：
- 枚举 `Anchor`：TOP_LEFT, TOP_CENTER, ..., BOTTOM_RIGHT
- `void setPosition(int x, int y)`
- `void setLocalPosition(int lx, int ly)`
- `void setAnchor(Anchor a)` / `Anchor getAnchor()`
- `void setParentAnchor(Anchor a)` / `Anchor getParentAnchor()`
- `void setParentAnchorNormalized(float ax, float ay)` / `void clearParentAnchorNormalized()`
- `void setPivotNormalized(float px, float py)` / `void clearPivotNormalized()`
- 便捷 preset：`setPivotPreset(Anchor a)`, `setParentAnchorPreset(Anchor a)`, `setAlignment(Anchor parentPreset, Anchor pivotPreset)`
- 读取计算结果：`int getAbsX()`, `int getAbsY()`

备注：`update` 会在组件排序时根据父控件与尺寸计算最终 absolute 坐标。

新概念：坐标偏移（offset）

为了支持在最终绝对坐标基础上做小幅微调（例如动画位移或像素级偏移），`PositionComponent` 新增了“坐标偏移（offset）”字段。

- offset 与 `localPosition` 的区别：
	- `localPosition`（localX/localY）是在相对于父锚点的位置计算中被纳入 base 偏移的一部分，通常由布局（Panel）设置。
	- `offset` 是在完成对齐与基准位置计算后的最终坐标上直接相加的额外偏移（全局绝对偏移），适合小幅 nudge 或动画。

API：
- `void setOffset(int ox, int oy)` - 设置最终坐标偏移
- `void addOffset(int dx, int dy)` - 在当前偏移上叠加（方便做渐变/动画）
- `void clearOffset()` - 清除偏移
- `int getOffsetX()` / `int getOffsetY()` - 读取当前偏移值

示例：

```java
// 将控件在计算后的绝对坐标上向右下各偏移 2 像素
lbl.getComponent(PositionComponent.class).addOffset(2, 2);

// 持续动画示例（每帧在 render/update 中调用）
float anim = (float)Math.sin(time) * 2f; // -2..2
lbl.getComponent(PositionComponent.class).setOffset(Math.round(anim), 0);
```

注意：offset 会在 `PositionComponent.update` 的末尾被加到 `computedX/computedY`，因此 `ControlObject.getX()/getY()` 将包含 offset 的效果。

### components.ScaleComponent

- 作用：拥有控件声明的宽高与缩放因子。

主要方法：
- `void setScale(float s)` / `float getScaleFactor()`
- `void setSize(int w, int h)` / `int getWidth()` / `int getHeight()`
- `int[] getActualControlSize()` - 返回未缩放的声明尺寸
- `int[] getScaledControlSize()` - 返回经过 scale 之后的尺寸

### components.BackgroundComponent

- 作用：为控件绘制背景纹理（`TextureRegion`），支持 tint、alpha、剪裁与九宫格绘制。

构造器：`BackgroundComponent(ControlObject owner)`

主要方法（链式）：
- `IComponent setTexture(TextureRegion region, int renderFlags)`
- `IComponent setTexture(TextureRegion region)` (默认 STRETCH)
- `IComponent setColor(int color)` - 只保留 RGB 部分（0xRRGGBB）
- `IComponent setAlpha(float a)` - 设置纹理 alpha
- `IComponent clip(float xPercent, float yPercent, ClipType clipType)` - 设置按百分比裁剪
- `void updateOwnerSize()` - 读取纹理尺寸并将 owner.setSize(textureW, textureH)

枚举 `ClipType`：FORWARD / BACKWARD。

渲染选项依赖 `RenderType` 常量（STRETCH、REPEAT、NINESLICE 等）。

### components.PanelComponent

- 作用：管理一组子 `ControlObject` 并进行简易布局（垂直/水平），支持 padding、spacing 与自动尺寸（autosize）。

主要 API：
- `void addChild(ControlObject c)` / `void removeChild(ControlObject c)`
- `List<ControlObject> getChildren()`
- 布局设置：`setOrientation(Orientation)`, `setCrossAlign(Align)`, `setSpacing(int)`, `setPadding(int left, int top, int right, int bottom)`
- autosize：`setAutoSizeWidth(boolean)`, `setAutoSizeHeight(boolean)`

在 `update` 中会计算子控件位置并调用其 update；在 `render` 中按顺序渲染子控件。

### components.CommonComponent

- 已标记为 @Deprecated。保留为兼容的 no-op 组件。

### controls.Label

- 作用：展示多行文本，自动根据 Minecraft 的 `textRenderer` 计算尺寸。

构造器：`Label(String text)`

主要方法：
- `void setText(String text)`
- `void setTextScale(float s)` / `float getTextScale()`
- `void setFontSizePx(int px)` / `int getFontSizePx()`
- `void setTextColor(int color)` / `int getTextColor()`
- 水平/垂直对齐：`setHorizontalAlign(HAlign)` / `setVerticalAlign(VAlign)`
- padding：`setPadding(...)` 系列方法，和对应的 getters

渲染/尺寸：在 `update` 中测量文本并设置控件尺寸，在 `render` 中按照对齐与 scale 绘制文本（使用 `DrawContext.drawTextWithShadow`）。

### controls.ProgressBar

- 作用：基于若干个 tile 构建的进度条，支持平滑动画显示进度和对每个片段进行裁剪。

构造器：`ProgressBar(int tileCount)` - tileCount 指中间平铺片段数量。

主要方法：
- `void setProgress(float p)` / `float getProgress()`
- `void setAnimationSpeed(float speed)` / `float getAnimationSpeed()`
- `void setAnimateEnabled(boolean v)` / `boolean isAnimateEnabled()`
- `float getAnimatedProgress()` - 当前动画插值进度
- `void setColor(int color)` - 给组成片段设置 tint 色值

实现细节：内部以两个 `PanelComponent` 管理背景与填充片段，并用 `BackgroundComponent.clip(...)` 来裁剪显示。

### utils.TextureRegion

- 作用：表示纹理图集中的一个子矩形区域（u,v,w,h）以及可选的 atlas 大小与 9-slice inset。

构造器：
- `TextureRegion(Identifier id, int u, int v, int w, int h)`
- `TextureRegion(Identifier id, int u, int v, int w, int h, int textureW, int textureH)`

方法：
- `Identifier getIdentifier()` / `int getU()` / `int getV()` / `int getW()` / `int getH()`
- `int getTextureWidth()` / `int getTextureHeight()`
- `float getAlpha()` / `void setAlpha(float a)`
- `TextureRegion withInsets(int left, int top, int right, int bottom)` / `withInsets(int all)`
- `boolean hasInsets()` / `getInsetLeft()` ... `getInsetBottom()`
- `void tryAutoFillTextureSize()` - 尝试读取资源并填充 atlas 尺寸（在客户端有 ResourceManager 时有效）。

### utils.Regions

- 作用：静态常量集合，预定义了常用 `TextureRegion`（如 widgets-sheet 的不同子区域）。

示例常量：`WIDGET_PANEL`, `WIDGET_PROGRESS_BORDER`, `WIDGET_PROGRESS_FILL`, `WIDGET_PROGRESS_BORDER_TILE_LEFT`, 等。

### utils.Vector2d

- 简单的 double 精度二维向量，字段 `x`, `y` 公共可访问。

方法：
- 构造器 `Vector2d()` / `Vector2d(double x, double y)`
- `Vector2d copy()`
- `Vector2d add(Vector2d other)` - 原地相加并返回 this（链式）
- `void set(Vector2d other)`


## 使用示例

下面给出若干常见场景的代码片段：

1) 创建 UIManager 并添加一个 Label 与 ProgressBar（伪代码，需在 mod 的客户端渲染/初始化回调中运行）：

```java
// 创建 UI 管理器（通常在客户端初始化时）
UIManager ui = new UIManager();

// 在 init 或者 UI 构建阶段添加控件
ui.initDemo(() -> {
		ControlObject root = ui.getRoot();

		// 创建标签
		Label lbl = new Label("Hello UIToolkit v2!");
		lbl.setLocalPosition(10, 10); // 本地偏移
		lbl.setTextScale(1.2f);
		lbl.setTextColor(0xFFFFAA);
		root.addChild(lbl);

		// 创建进度条 (中间平铺片段数量为 6)
		ProgressBar pb = new ProgressBar(6);
		pb.setLocalPosition(10, 40);
		pb.setSize(120, 12); // 使用 ScaleComponent.setSize
		pb.setProgress(0.25f);
		pb.setAnimationSpeed(8f);
		root.addChild(pb);
});

// 每帧渲染（在游戏客户端的 render 回调中传入 DrawContext）
// ui.render(context, tickDelta);
```

2) 使用 `BackgroundComponent` 与 `TextureRegion`：

```java
ControlObject panel = new ControlObject();
BackgroundComponent bg = new BackgroundComponent(panel)
		.setTexture(Regions.WIDGET_PANEL, RenderType.NINESLICE)
		.setColor(0xFFFFFF);
panel.addComponent(bg);
bg.updateOwnerSize(); // 将控件大小设置为纹理大小（必要时）

// 添加到 root 或其他容器
ui.getRoot().addChild(panel);
```

3) Label 的多行与自动尺寸：

```java
Label multi = new Label("Line1\nLine2\nLine3");
multi.setPadding(4);
multi.setTextScale(1.0f);
ui.getRoot().addChild(multi);
// update 会根据字体度量调整 multi 的尺寸
```

4) ProgressBar 动画控制：

```java
ProgressBar pb = new ProgressBar(8);
pb.setAnimateEnabled(true);
pb.setAnimationSpeed(4f); // 更大更快
pb.setProgress(0.75f);
ui.getRoot().addChild(pb);
```

## 注意事项与边界情况

- 所有渲染方法都依赖 Minecraft 的客户端环境（例如 `DrawContext`、`MinecraftClient`、`ResourceManager` 等），在非客户端环境下某些自动化方法（例如 `TextureRegion.tryAutoFillTextureSize()`）将静默失败。
- `PositionComponent` 支持两套锚点 API（枚举 preset 与 0..1 normalized），使用时注意启用/清除对应模式的方法。
- `BackgroundComponent` 的九宫格渲染依赖于 `TextureRegion` 的 insets，使用 `withInsets(...)` 进行设置。
- `PanelComponent` 的 autosize 会在 update 中调整 owner 的尺寸，请在添加子控件后留意布局影响。

## 小结

本文档覆盖了仓库中 `org.fish.uitoolkit.v2` 的主要公开 API，并给出若干常见使用场景的代码片段。若需更多示例（如交互控件、事件处理、复杂布局示例），我可以基于本 README 再补充 demo 示例或单元测试用例。

