帮我规划一套UI框架，基于Minecraft Fabric的 net.minecraft.client.gui.DrawContext

要求包含一个UI组件系统，支持布局管理，事件处理，锚点，缩放，层级关系，可见性管理，背景绘制（使用 utils/TextureRegion）

使用示例：
ControlObject canvas = new ControlObject(); // 创建UI对象，UI对象不是抽象类，它可以直接被创建，UI控件相当于“空壳子”只提供基础的坐标、尺寸等属性
/* 基带属性 */
canvas.setPosition(100, 100);   // 原始坐标
canvas.setLocalPosition(100, 100);   // 本地坐标(相当于在原始坐标系的偏移)，举例(原始坐标100,100)：canvas.setLocalPosition(10, 10) -> (110, 110)
canvas.setSize(200, 150);
canvas.setVisible(true);
/* 组件管理（以及组件的附加属性） */
canvas.addComponent<Scale>().scale(float);
canvas.getComponent<Scale>().getActualControlSize();    // 获取缩放前的控件尺寸
canvas.getComponent<Scale>().getScaleFactor();  // 获取缩放比例
canvas.getComponent<Scale>().getScaledControlSize();  // 获取缩放后的控件尺寸，举例(0.25缩放比例)：100 x 100 -> 25 x 25
canvas.addComponent<Panel>().addChild(ControlObject child);
canvas.addComponent<Panel>().getChildren(); -> List<ControlObject>
canvas.getComponent<Panel>().removeChild(ControlObject child);
canvas.addComponent<Background>().setTexture(new TextureRegion(/* ... */), RenderType.STRETCH|RenderType.REPEAT|RenderType.CLAMP|RenderType.NINESLICE);
canvas.getComponent<Background>().setColor(int color); // 设置背景颜色
canvas.getComponent<Background>().setAlpha(float alpha); // 设置背景透明度
canvas.getComponent<Background>().clip(float xPercent, float yPercent, ClipType clipType); // 设置背景裁剪，例如：clip(0.5f,0,ClipType.FORWARD) -> 背景沿轴体x(正向)裁剪，使其只剩下一半，裁剪不会影响控件的尺寸大小。
...

组件系统：
组件系统是一个可扩展的架构，允许开发者为UI控件添加各种功能和行为。每个控件都可以拥有多个组件，这些组件可以独立管理自己的状态和逻辑，从而实现更高的灵活性和可重用性。
例如，Scale组件可以用于控制控件的缩放行为，开发者可以通过设置缩放比例来实现不同的视觉效果。
Panel组件允许开发者将多个子控件组织在一起，并提供更复杂的布局和交互功能。
Background组件允许控件设置背景纹理，并支持多种渲染类型。
多种组件可以组合使用，且应该互相不干扰，最终的结果是，将组件的行为和状态进行组合，相应到控件自身的属性中，这样，例如当使用缩放后，控件的实际尺寸和位置都会受到影响，从而也会影响到控件背景渲染（背景渲染根据控件的实际尺寸和位置进行调整）。

组件结构：
一个组件接口存在 update() 方法，用于管理组件的状态和行为。

每个组件都可以通过实现该接口来定义自己的更新逻辑，从而在每一帧中被调用。

组件约束：
有些组件，可能会与其他组件产生依赖、冲突，例如，Scale组件可能与Background产生依赖性，因此，在使用Background组件时，必须确保Scale组件已经被添加到控件中，否则，程序会抛出异常。（提供一个API，用于告诉系统这个组件与其他组件冲突或依赖）

组件管理：
组件管理是一个复杂的系统，负责协调和管理所有组件的生命周期、状态和交互。它确保组件之间的依赖关系得到满足，并处理组件之间的冲突。
它应该在 ControlObject 中实现，提供添加、移除和获取组件的方法。

基带组件：
基带组件是最基本的组件，提供了最基本的功能和行为。所有其他组件都可以基于基带组件进行扩展和定制。
如，setPosition, setSize, setVisible, setLocalPosition都被一个叫做 CommonComponent的组件所管理，这代表，ControlObject 的 setPosition 方法实际上是调用了 CommonComponent 的相应方法。
基带组件还有一种存在形式，那就是继承自 ControlObject，成为一个特殊的 ControlObject，这时，开发者可以使用 addBaseComponent 为该自定义控件添加基带组件。Protected 是它的方法签名，这代表它只能被子类访问。

HudRenderCallback.EVENT.register((context, tickDelta) -> {
    canvas.render(context, tickDelta);
});