## UIToolkit — API 文档

下面为仓库 `UIToolkit`（包 `org.fish.uitoolkit`）中常用的公共 API 汇总、说明与示例，方便在模组中引用和复用。

> 说明：文档基于仓库源码摘录整理，覆盖主要控件类、接口与示例。若需更详细的签名或未列出的成员，请告知我将进一步展开。

## 快速构建与运行

在 Windows PowerShell 下：

```powershell
./gradlew build
./gradlew runClient
Copy-Item -Path .\build\libs\*.jar -Destination "$env:APPDATA\.minecraft\mods\"
```

## 包概览

包名：`org.fish.uitoolkit`

主要类型（高层）：

- `UIElement` — 基础接口，定义渲染、输入与生命周期回调。
- `Control` — 抽象基类，提供位置、锚点、margin、背景绘制（9-slice 支持）等公共行为。
- `Container` — 支持子元素管理的容器控件（继承自 `Control`）。
- `Canvas` — 顶层渲染画布（继承自 `Container`），适合作为根节点，支持全屏自动尺寸与事件分发。
- `Panel` — 简单面板容器，方便创建固定位置/尺寸的容器。
- `Label` — 文本标签控件，基于 Minecraft 的 `TextRenderer` 绘制文本。
- `RichTextBlock` — 富文本块，支持多行、格式化追加、按文本查找并设置匹配片段颜色。

另外还有工具类和资源定义（例如 `org.fish.uitoolkit.utils.TextureRegion`、`Regions`）。

## 详细 API 摘要

注意：下列方法签名采用源码中出现的常用形式进行描述；某些重载或辅助方法在示例中未逐一列出。

### UIElement

概念：最小契约，任何可渲染 / 接收输入的元素都应实现该接口。

主要成员（默认/可覆写）：

- enum HAnchor { LEFT, CENTER, RIGHT }
- enum VAnchor { TOP, MIDDLE, BOTTOM }
- void render(DrawContext context, int mouseX, int mouseY, float delta)
- default void tick()
- default void onRemoved()
- 输入事件（可覆写）：
	- boolean mouseClicked(double mx, double my, int button)
	- boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
	- boolean mouseScrolled(double mouseX, double mouseY, double amount)
	- boolean keyPressed(int keyCode, int scanCode, int modifiers)
	- boolean charTyped(char codePoint, int modifiers)
- 位置 / 尺寸相关（默认实现可从 owner 推断）：
	- int getLocalX(), getLocalY()
	- int getWidth(), getHeight()
	- boolean isVisible()
	- Object getOwner()
	- int getParentX(), getParentY(), getParentWidth(), getParentHeight()

### Control (抽象)

用途：实现 `UIElement` 的基础控件，封装位置、锚点、margin、背景绘制与子控件链（部分控件使用单子控件）。

常用字段（实现细节）：

- protected Object owner
- protected int x, y, width, height
- protected HAnchor hAnchor; protected VAnchor vAnchor
- protected int marginLeft, marginTop, marginRight, marginBottom
- private Identifier background; // 纹理资源
- NineSliceScaleMode: NONE, PROPORTIONAL, MINIMUM, CLAMPED

常用方法：

- Control(Object owner) — 构造并（当 owner 支持时）注册到 owner。
- void setPosition(int x, int y)
- void setMargins(int left, int top, int right, int bottom)
- void setMargins(int all)
- void setVisible(boolean v)
- void setBackground(Identifier id, int u, int v, int w, int h, int texW, int texH)
	（源码中还有基于 TextureRegion / Regions 的便捷设定）
- void render(DrawContext context, int mouseX, int mouseY, float delta)
	- 默认实现会在可见时计算绝对位置、先绘制背景（renderBackground）再绘制内容（renderContent）。
- protected void renderContent(DrawContext context, int absX, int absY, int mouseX, int mouseY, float delta)
- protected void renderBackground(DrawContext context, int x, int y, int width, int height)

九宫格（9-slice）相关：

- NineSliceScaleMode 枚举控制角/边的缩放策略。Control 提供最小/最大像素限制与模式选择。

注意：Control 实现中对异常进行了防护（绘制时捕获 Throwable），以提高在不完整资源时的鲁棒性。

### Container

功能：支持子元素集合管理与按顺序绘制。

主要方法：

- void setPadding(int left, int top, int right, int bottom)
- void setPadding(int pad)
- int getPaddingLeft()/getPaddingRight()/getPaddingTop()/getPaddingBottom()
- public boolean removeChild(UIElement child)
- public void clearChildren()
- public List<UIElement> getChildren() — 返回只读视图
- protected void renderContent(...) — 遍历 children 并调用它们的 render

Container 在宽高未手动设置时，会使用内部的 computeAutoWidth/computeAutoHeight 策略自动计算尺寸（源码实现可查阅 `Container.java`）。

### Canvas

用途：顶层画布（继承自 Container），常用于注册到 HUD 渲染回调并绘制 UI。

关键方法：

- Canvas() — 构造时会根据窗口设置初始尺寸
- void updateSizeFromWindow() — 将画布大小更新为 Minecraft 窗口的 scaled 大小（适配 DPI）
- int getContentX(), getContentY(), getContentWidth(), getContentHeight()
- Canvas addChild(UIElement child) — 连式 API，返回 this
- void render(DrawContext context, int mouseX, int mouseY, float delta) — 渲染所有顶层子元素
- 事件分发与生命周期：mouseDragged / mouseScrolled / keyPressed / charTyped / tick / onRemoved

示例（在客户端初始化时注册 HUD 回调并渲染）：

```java
Canvas canvas = new Canvas();
Panel panel = new Panel(canvas);
Label label = new Label(panel, "Hello, UIToolkit!");
label.setColor(0xFF00FF00);
panel.addChild(label);
// 在 HudRenderCallback 中：
// canvas.updateSizeFromWindow();
// canvas.render(context, mouseX, mouseY, tickDelta);
```

### Panel

简单容器。构造器：

- Panel(Object owner)
- Panel(Object owner, int x, int y, int width, int height)

Panel 默认按添加顺序渲染子元素，事件分发从后向前（后添加的元素优先接收事件）。

### Label

文本标签控件，使用 Minecraft 的 TextRenderer 绘制文本。

构造与常用 API：

- Label(Object owner, String text)
- Label(Object owner, Text text)
- void setText(String)
- void setText(Text)
- Text getText()
- void setColor(int hexARGB) // 例如 0xFFFFFFFF
- void setShadow(boolean)
- void setCentered(boolean)
- void setPosition(int x, int y)
- 覆写的尺寸/位置方法：getLocalX/Y(), getWidth(), getHeight(), getMargin*(), isVisible(), getHorizontalAnchor()/getVerticalAnchor()

渲染：Label 在 renderContent 中调用 DrawContext.drawText，支持水平居中（centered）与阴影选项。

### RichTextBlock

富文本块，支持多行追加、可变参数格式追加、按文本查找并对匹配段落设置颜色。

主要 API：

- RichTextBlock(Object owner)
- RichTextBlock append(String text)
- RichTextBlock append(String fmt, Object... args) — 使用 String.format（包含安全回退策略）
- MatchHandle find(String query) — 返回可操作匹配段的句柄
	- MatchHandle.setColor(int hexRgb)
- RichTextBlock clear()
- RichTextBlock setDefaultColor(int color)
- RichTextBlock setCentered(boolean c)
- RichTextBlock setLineSpacing(int spacing)
- setPosition / setMargins / setHorizontalAnchor / setVerticalAnchor（继承 Control）
- getWidth(), getHeight(), getLocalX/Y(), getMargin*

渲染：基于 TextRenderer，按行绘制每个段（Segment），每个段可单独设置颜色。

示例用法：

```java
RichTextBlock rt = new RichTextBlock(panel);
rt.append("Hello %s", playerName);
rt.append("This contains UIToolkit");
rt.find("UIToolkit").setColor(0xFFFF0000);
panel.addChild(rt);
```

## 示例：在 Fabric 客户端中初始化 UI

参考 `src/client/java/org/examplea/uitoolkit/client/UitoolkitClient.java`：

核心流程：

1. 在客户端初始化回调中创建 `Canvas`、`Panel`、`Label` 等控件并设置属性。
2. 在 `HudRenderCallback.EVENT.register` 回调里调用 `canvas.updateSizeFromWindow()` 与 `canvas.render(...)`。

## 使用建议与注意事项

- 纹理资源与九宫格：若使用带 inset 的 TextureRegion，请确保提供正确的源矩形与纹理大小（texW/texH），否则绘制可能出错。
- 尽量在主线程（Minecraft 客户端线程）中修改 UI 状态，避免并发问题。
- Control 的渲染会捕获异常以避免模块间渲染互相影响，但仍应在开发时确保资源路径与参数正确。


## 逐方法 API 参考（按类）

下面给出项目中主要类的每个 public 方法的签名、参数说明、返回值与要点，便于直接查阅与复制示例。

### org.fish.uitoolkit.UIElement

- Object getOwner()
	- 返回：元素所属的 owner（通常为 `Canvas` 或 `Panel`），可能为 null。

- void render(DrawContext context, int mouseX, int mouseY, float delta)
	- 描述：必须实现的渲染方法，带当前缩放后的鼠标坐标与帧 delta。

- default void render(DrawContext context, float delta)
	- 描述：默认实现会以 getX()/getY() 作为 mouse 坐标调用上面的重载。

- default boolean mouseClicked(double mouseX, double mouseY, int button)
- default boolean mouseReleased(double mouseX, double mouseY, int button)
- default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
- default boolean mouseScrolled(double mouseX, double mouseY, double amount)
- default boolean keyPressed(int keyCode, int scanCode, int modifiers)
- default boolean charTyped(char codePoint, int modifiers)
	- 描述：输入事件的钩子。默认返回 false，子类根据需要覆盖并在处理后返回 true 以停止事件冒泡。

- default int getX(), getY(), getLocalX(), getLocalY()
	- 描述：位置相关访问器。`getX()/getY()` 返回绝对坐标（基于 owner 的锚点计算）；`getLocalX()/getLocalY()` 返回相对于 owner 的局部偏移，默认 0，控件通常覆盖为实际字段。

- default int getWidth(), getHeight()
	- 描述：尺寸访问器，默认返回 0，常由控件覆盖以反映背景或内容尺寸。

- default HAnchor getHorizontalAnchor(), default VAnchor getVerticalAnchor()
	- 描述：锚点（LEFT/CENTER/RIGHT / TOP/MIDDLE/BOTTOM），用于相对于父容器定位。

- default int getMarginLeft()/getMarginRight()/getMarginTop()/getMarginBottom()
	- 描述：边距（默认 0）。

- default boolean containsPoint(int x, int y)
	- 描述：判断点是否在元素包括 margin 的区域内（左闭右开）。

### org.fish.uitoolkit.Control

构造器与基本属性：

- Control()
- Control(Object owner)
	- 描述：构造时若 `owner` 为 `Container`/`Control`，会尝试将自身注册为子控件（调用 `addChild` / `setChild`），注册异常被吞掉以保证鲁棒性。

位置、尺寸与可见性：

- void setPosition(int x, int y)
	- 描述：设置局部偏移（getLocalX/Y 返回值）。

- void setSize(int width, int height)
	- 描述：显式设置控件大小（覆盖自动尺寸）。

- void setHorizontalAnchor(HAnchor a)
- void setVerticalAnchor(VAnchor a)
	- 描述：设置锚点，影响 `getX()/getY()` 的计算。

- void setMargins(int left, int top, int right, int bottom)
- void setMargins(int all)
	- 描述：设置四个方向的 margin。若 owner 提供 `invalidateLayout()` 方法，会尝试通过反射调用以触发布局刷新（静默失败）。

- void setVisible(boolean v)
	- 描述：控制可见性，render 时会检查 `isVisible()`。

背景及九宫格：

- void setBackground(Identifier id, int u, int v, int w, int h, int texW, int texH)
	- 参数：纹理标识符与源矩形及纹理总大小（用于 UV 计算）。
	- 要点：当提供完整 atlas 大小时可以正确计算 drawTexture 的 UV；若未提供，部分路径会尝试依赖 `TextureRegion` 自动填充。

- void setBackground(TextureRegion region)
	- 参数：`TextureRegion`（可包含 insets 与 atlas 大小与 alpha）。

- void clearBackground()
	- 描述：清除背景设置。

- void setBackgroundAlpha(float alpha)
	- 描述：设置背景绘制透明度（0.0–1.0）。

九宫格控制：

- void setNineSliceMode(NineSliceScaleMode mode)
- void setNineSliceMinPx(int minPx)
- void setNineSliceMaxPx(int maxPx)
	- 描述：控制九宫格（9-slice）边角在目标区域的缩放策略与限制（枚举：NONE, PROPORTIONAL, MINIMUM, CLAMPED）。

子控件与渲染钩子：

- void setChild(UIElement child)
	- 描述：为支持单子控件的 Control 设置子元素（某些 Control 用单一 child 表示内容）。

- void render(DrawContext context, int mouseX, int mouseY, float delta)
	- 描述：默认实现会在 visible 时计算绝对位置，先调用 `renderBackground` 再调用 `renderContent`。

- protected void renderContent(DrawContext context, int absX, int absY, int mouseX, int mouseY, float delta)
	- 描述：默认实现若存在单 child 则转发到 child；子类覆盖以实现自定义绘制逻辑。

- protected void renderBackground(DrawContext context, int x, int y, int width, int height)
	- 描述：负责将背景纹理（单图或带 insets 的 9-slice）绘制到目标矩形。实现中对绘制过程做了异常捕获以避免渲染期间抛出异常导致整个 HUD 崩溃。

### org.fish.uitoolkit.Container

- Container(), Container(Object owner)

- Container addChild(UIElement child)
	- 返回：this（链式调用）。
	- 描述：将 child 添加到内部列表，保留添加顺序用于渲染。

- boolean removeChild(UIElement child)
- void clearChildren()
- List<UIElement> getChildren()
	- 描述：管理子元素的标准 API。`getChildren()` 返回不可变视图。

- void setPadding(int left, int top, int right, int bottom)
- void setPadding(int pad)
- int getPaddingLeft()/getPaddingRight()/getPaddingTop()/getPaddingBottom()

- protected int computeAutoWidth()
- protected int computeAutoHeight()
	- 描述：当未显式设置 width/height 时，Container 会基于子元素的 localX/Width 与 padding/margins 计算自动尺寸。

- protected void renderContent(DrawContext context, int absX, int absY, int mouseX, int mouseY, float delta)
	- 描述：按添加顺序遍历并 render 每个可见子元素。

- void tick()
- void onRemoved()
	- 描述：生命周期钩子会遍历并转发到子元素。

### org.fish.uitoolkit.Canvas

- Canvas()
	- 描述：构造时会调用 `updateSizeFromWindow()` 使画布初始尺寸与 Minecraft 窗口 scaled 尺寸一致。

- void updateSizeFromWindow()
	- 描述：读取 `MinecraftClient.getInstance().getWindow()` 的 scaled 尺寸并写入 Canvas 的 width/height。

- int getWidth(), int getHeight()

- void setPadding(int left, int top, int right, int bottom)

- int getContentX(), getContentY(), getContentWidth(), getContentHeight()
	- 描述：content 区域会扣除 padding，用于放置子元素的参照区域。

- Canvas addChild(UIElement child)
	- 描述：返回 Canvas 本身以便链式添加。

- void render(DrawContext context, int mouseX, int mouseY, float delta)
- void render(DrawContext context, float delta)
	- 描述：二者分别接收缩放后的鼠标坐标或从 MinecraftClient 获取并转换原始鼠标坐标后再渲染。

事件分发（按 Z 轴从上到下，后添加的子元素先接收）：

- boolean mouseClicked(double mouseX, double mouseY, int button)
- boolean mouseReleased(double mouseX, double mouseY, int button)
- boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
- boolean mouseScrolled(double mouseX, double mouseY, double amount)
- boolean keyPressed(int keyCode, int scanCode, int modifiers)
- boolean charTyped(char codePoint, int modifiers)
	- 描述：这些方法会按子元素逆序查找第一个包含坐标的元素并调用对应的事件处理，若子元素返回 true 则事件停止传播。

- void tick(), void onRemoved()
	- 描述：转发到子元素。

### org.fish.uitoolkit.Panel

- Panel(Object owner)
- Panel(Object owner, int x, int y, int width, int height)
	- 描述：简单容器，常用于固定位置的分组；构造器会设置 owner 与显式位置/尺寸。

### org.fish.uitoolkit.Label

- Label(Object owner, String text)
- Label(Object owner, Text text)
	- 描述：文本标签控件，内部使用 Minecraft 的 `TextRenderer` 绘制文本。

- void setText(String) / void setText(Text)
- Text getText()
- void setColor(int hexARGB)
- void setShadow(boolean)
- void setCentered(boolean)
	- 描述：常规文本属性设置。`setCentered(true)` 会在绘制时将文本 X 向左移动半个文本宽度以实现水平居中。

- getWidth(), getHeight()
	- 描述：基于 `TextRenderer` 的测量结果返回文本尺寸（在 `getHeight()` 中若 `textRenderer` 为 null 会返回默认 9）。

### org.fish.uitoolkit.RichTextBlock

- RichTextBlock(Object owner)

- RichTextBlock append(String text)
- RichTextBlock append(String fmt, Object... args)
	- 描述：向末尾追加一行文本；对带格式化参数的重载采用 `String.format(Locale.ROOT, ...)`，对异常情况提供多个安全回退策略，尽可能避免抛出异常。

- MatchHandle find(String query)
	- 返回：`MatchHandle`，用以对查找出的匹配片段进行操作（例如 `setColor(int)`）。实现细节：会在匹配处拆分 segment 并返回 match segment 的引用列表。

- RichTextBlock clear(), setDefaultColor(int), setCentered(boolean), setLineSpacing(int)

- getWidth(), getHeight()
	- 描述：基于包含的行与每行的段测量字体宽高并加上 margin 得到最终尺寸。

### org.fish.uitoolkit.utils.TextureRegion

- TextureRegion(Identifier id, int u, int v, int w, int h)
- TextureRegion(Identifier id, int u, int v, int w, int h, int textureW, int textureH)
	- 描述：表示纹理图集中的子矩形；可选指定 atlas 尺寸以便准确计算 UV。

- TextureRegion withInsets(int left, int top, int right, int bottom)
- TextureRegion withInsets(int all)
	- 描述：设置 9-slice 的 insets（以像素为单位），返回自身以便链式调用。

- void tryAutoFillTextureSize()
	- 描述：尝试通过 `MinecraftClient.getInstance().getResourceManager()` 读取资源图片并自动填充 `textureW/textureH`；静默失败时不会抛出异常。

- getter: getIdentifier(), getU(), getV(), getW(), getH(), getTextureWidth(), getTextureHeight(), getAlpha(), setAlpha(float)

### org.fish.uitoolkit.utils.Regions

- 常量集合，例如：
	- public static final TextureRegion WIDGET_PANEL = new TextureRegion(new Identifier("uitoolkit","textures/gui/widgets-sheet.png"),0,0,16,16,32,16).withInsets(4);
	- 描述：为常用控件背景与图块提供预定义 `TextureRegion` 实例以便在 `Control.setBackground(TextureRegion)` 中直接使用。