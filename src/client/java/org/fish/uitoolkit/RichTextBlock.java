package org.fish.uitoolkit;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 富文本块控件，支持追加多行文本、格式化追加、以及按文本查找并设置匹配片段颜色。
 * <p>
 * 用法示例：
 * RichTextBlock rt = new RichTextBlock(owner);
 * rt.append("line1");
 * rt.append("line2 %s", someValue);
 * rt.find("FishUI").setColor(0xFFFF0000);
 */
public class RichTextBlock extends Control {

    private final List<List<Segment>> lines = new ArrayList<>();
    private int defaultColor = 0xFFFFFFFF;
    private boolean centered = false;
    // x,y,anchors,margins inherited from Control
    private int lineSpacing = 2;

    public RichTextBlock(Object owner) {
        super(owner);
    }

    // 简单段片
    private static final class Segment {
        String text;
        int color;

        Segment(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }

    // find 返回的句柄
    public final class MatchHandle {
        private final List<Segment> matches;

        private MatchHandle(List<Segment> matches) {
            this.matches = matches;
        }

        public MatchHandle setColor(int hexRgb) {
            if (matches == null)
                return this;
            for (Segment s : matches) {
                s.color = hexRgb;
            }
            return this;
        }
    }

    // 追加一行，不带格式
    public RichTextBlock append(String text) {
        return append(text, (Object[]) null);
    }

    // 可变参数格式化追加，使用 String.format
    public RichTextBlock append(String fmt, Object... args) {
        String t;
        if (args == null || args.length == 0) {
            t = fmt != null ? fmt : "";
        } else {
            try {
                t = String.format(Locale.ROOT, fmt, args);
            } catch (Exception ex) {
                // 某些格式（例如 "%p"）不是 Java Formatter 的有效转换符，尝试安全回退策略
                try {
                    String safeFmt = fmt == null ? "" : fmt.replace("%p", "%s");
                    t = String.format(Locale.ROOT, safeFmt, args);
                } catch (Exception ex2) {
                    // 最后回退：把 fmt 与 args 串联，避免抛出异常
                    StringBuilder sb = new StringBuilder();
                    if (fmt != null)
                        sb.append(fmt);
                    for (Object a : args) {
                        sb.append(' ').append(String.valueOf(a));
                    }
                    t = sb.toString();
                }
            }
        }
        ArrayList<Segment> segs = new ArrayList<>();
        segs.add(new Segment(t, defaultColor));
        lines.add(segs);
        return this;
    }

    public MatchHandle find(String query) {
        if (query == null || query.isEmpty())
            return new MatchHandle(new ArrayList<>());
        List<Segment> found = new ArrayList<>();
        for (List<Segment> line : lines) {
            for (int i = 0; i < line.size(); i++) {
                Segment seg = line.get(i);
                String s = seg.text;
                int idx = s.indexOf(query);
                if (idx < 0)
                    continue;
                int offset = 0;
                while (true) {
                    int pos = s.indexOf(query, offset);
                    if (pos < 0)
                        break;
                    String before = s.substring(0, pos);
                    String match = s.substring(pos, pos + query.length());
                    String after = s.substring(pos + query.length());
                    List<Segment> toInsert = new ArrayList<>();
                    if (!before.isEmpty())
                        toInsert.add(new Segment(before, seg.color));
                    Segment matchSeg = new Segment(match, seg.color);
                    toInsert.add(matchSeg);
                    if (!after.isEmpty())
                        toInsert.add(new Segment(after, seg.color));
                    line.remove(i);
                    line.addAll(i, toInsert);

                    found.add(matchSeg);
                    i += toInsert.size() - 1;
                    s = after;
                    offset = 0;
                    if (s.isEmpty())
                        break;
                }
            }
        }
        return new MatchHandle(found);
    }

    public RichTextBlock clear() {
        lines.clear();
        return this;
    }

    public RichTextBlock setDefaultColor(int color) {
        this.defaultColor = color;
        return this;
    }

    public RichTextBlock setCentered(boolean c) {
        this.centered = c;
        return this;
    }

    public RichTextBlock setLineSpacing(int spacing) {
        this.lineSpacing = spacing;
        return this;
    }

    public void setPosition(int x, int y) {
        super.setPosition(x, y);
    }

    public void setHorizontalAnchor(UIElement.HAnchor a) {
        super.setHorizontalAnchor(a);
    }

    public void setVerticalAnchor(UIElement.VAnchor a) {
        super.setVerticalAnchor(a);
    }

    // setMargins removed; margins are not part of the control API

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null)
            return;
        TextRenderer tr = client.textRenderer;
        if (tr == null)
            return;

        super.render(context, mouseX, mouseY, delta);

        int fontH = tr.fontHeight;
        int drawY = getY();
        for (List<Segment> line : lines) {
            int drawX = getX();
            if (centered) {
                int width = computeLineWidth(tr, line);
                drawX = drawX - width / 2;
            }
            for (Segment seg : line) {
                if (seg.text == null || seg.text.isEmpty())
                    continue;
                context.drawText(tr, Text.literal(seg.text), drawX, drawY, seg.color, false);
                drawX += tr.getWidth(seg.text);
            }
            drawY += fontH + lineSpacing;
        }
    }

    private int computeLineWidth(TextRenderer tr, List<Segment> line) {
        int w = 0;
        for (Segment s : line)
            w += tr.getWidth(s.text);
        return w;
    }

    @Override
    public int getWidth() {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        if (tr == null)
            return 0;
        int max = 0;
        for (List<Segment> line : lines) {
            int w = computeLineWidth(tr, line);
            if (w > max)
                max = w;
        }
        return max;
    }

    @Override
    public int getHeight() {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        if (tr == null)
            return 0;
        int linesCount = Math.max(0, lines.size());
        if (linesCount == 0)
            return 0;
        return linesCount * tr.fontHeight + (linesCount - 1) * lineSpacing;
    }

    // 使用 Control/默认实现提供的行为（不再冗余覆写）
}
