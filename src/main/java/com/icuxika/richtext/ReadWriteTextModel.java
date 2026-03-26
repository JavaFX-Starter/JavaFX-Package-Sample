package com.icuxika.richtext;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.StyleResolver;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import jfx.incubator.scene.control.richtext.model.StyledSegment;
import jfx.incubator.scene.control.richtext.model.StyledTextModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ReadWriteTextModel extends StyledTextModel {

    // -------------------------------------------------------------------------
    // 字段与构造

    private Color textColor = Color.BLACK;
    private final List<Paragraph> paragraphs = new ArrayList<>();

    public ReadWriteTextModel() {
        paragraphs.add(new Paragraph());
    }

    // -------------------------------------------------------------------------
    // StyledTextModel 查询接口

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public int size() {
        return paragraphs.size();
    }

    @Override
    public String getPlainText(int index) {
        return paragraphs.get(index).getPlainText();
    }

    @Override
    public RichParagraph getParagraph(int index) {
        return paragraphs.get(index).toRichParagraph(currentStyle());
    }

    @Override
    public StyleAttributeMap getStyleAttributeMap(StyleResolver resolver, TextPos pos) {
        return currentStyle();
    }

    // -------------------------------------------------------------------------
    // StyledTextModel 变更接口

    @Override
    protected void removeRange(TextPos start, TextPos end) {
        if (start.index() == end.index()) {
            removeSingleParagraphRange(start, end);
        } else {
            removeCrossParagraphRange(start, end);
        }
    }

    @Override
    protected int insertTextSegment(int index, int offset, String text, StyleAttributeMap attrs) {
        paragraphs.get(index).insertTextSegment(offset, text, attrs);
        return text.length();
    }

    @Override
    protected void insertLineBreak(int index, int offset) {
        Paragraph p = paragraphs.get(index);
        String text = p.getPlainText();
        if (offset == 0) {
            paragraphs.add(index, new Paragraph());
        } else if (offset >= text.length()) {
            paragraphs.add(index + 1, new Paragraph());
        } else {
            paragraphs.add(index + 1, p.splitAt(offset));
        }
    }

    @Override
    protected void insertParagraph(int index, Supplier<Region> generator) {
        System.out.println("insertParagraph");
    }

    @Override
    protected void setParagraphStyle(int index, StyleAttributeMap paragraphAttrs) {
        paragraphs.get(index).setParagraphAttributes(paragraphAttrs);
    }

    @Override
    protected void applyStyle(int index, int start, int end, StyleAttributeMap a, boolean merge) {
        System.out.println("applyStyle");
    }

    // -------------------------------------------------------------------------
    // 公开的内容构建 API

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public ReadWriteTextModel addSegment(String text) {
        lastParagraph().addText(text);
        return this;
    }

    public ReadWriteTextModel addNodeSegment(Supplier<Node> generator) {
        lastParagraph().addInlineNode(generator);
        return this;
    }

    public ReadWriteTextModel nl() {
        paragraphs.add(new Paragraph());
        return this;
    }

    /**
     * 在指定位置插入文字并通知控件刷新。
     */
    public void insertText(TextPos pos, String text) {
        insertTextSegment(pos.index(), pos.offset(), text, StyleAttributeMap.EMPTY);
        fireChangeEvent(
                TextPos.ofLeading(pos.index(), pos.offset()),
                TextPos.ofLeading(pos.index(), pos.offset() + text.length()),
                text.length(), 0, 0
        );
    }

    /**
     * 在指定位置插入内联节点，节点的具体构建由调用方通过 nodeSupplier 提供，模型不感知展示细节。
     * 插入后逻辑长度为 {@link Paragraph#INLINE_NODE_LOGICAL_LENGTH}。
     */
    public void insertInlineNode(TextPos pos, Supplier<Node> nodeSupplier) {
        paragraphs.get(pos.index()).insertNodeSegment(pos.offset(), nodeSupplier);
        fireChangeEvent(
                TextPos.ofLeading(pos.index(), pos.offset()),
                TextPos.ofLeading(pos.index(), pos.offset() + Paragraph.INLINE_NODE_LOGICAL_LENGTH),
                Paragraph.INLINE_NODE_LOGICAL_LENGTH, 0, 0
        );
    }

    /**
     * 便捷方法：插入图片，由调用方指定渲染高度。
     */
    public void insertImage(TextPos pos, Image image, double fitHeight) {
        insertInlineNode(pos, () -> {
            ImageView iv = new ImageView(image);
            iv.setPreserveRatio(true);
            iv.setFitHeight(fitHeight);
            return iv;
        });
    }

    /**
     * 收集当前编辑器的所有内容，按段落顺序返回文字和图片的混合列表。
     * 相邻的文字 item 会被合并；段落之间以 "\n" 连接。
     */
    public List<ChatInputItem> getChatInputItems() {
        List<ChatInputItem> items = new ArrayList<>();
        for (int i = 0; i < paragraphs.size(); i++) {
            paragraphs.get(i).collectItems(items);
            if (i < paragraphs.size() - 1) {
                appendNewline(items);
            }
        }
        return items;
    }

    // -------------------------------------------------------------------------
    // removeRange 私有拆分

    private void removeSingleParagraphRange(TextPos start, TextPos end) {
        Paragraph p = paragraphs.get(start.index());
        if (!p.getPlainText().isEmpty()) {
            p.removeRangeInline(start, end);
        } else {
            // 段落本身已是空段，用户主动删除空行
            paragraphs.remove(start.index());
        }
    }

    private void removeCrossParagraphRange(TextPos start, TextPos end) {
        Paragraph startParagraph = paragraphs.get(start.index());
        Paragraph endParagraph = paragraphs.get(end.index());

        // 1. 裁剪起始段：保留 [0, start.offset()) 的内容
        startParagraph.truncateFrom(start.offset());

        // 2. 收集结束段：保留 [end.offset(), 末尾) 的内容
        List<StyledSegment> tail = endParagraph.collectTailFrom(end.offset());

        // 3. 删除中间段落（含结束段落）
        paragraphs.subList(start.index() + 1, end.index() + 1).clear();

        // 4. 将结束段的剩余内容追加到起始段
        if (!tail.isEmpty()) {
            startParagraph.addAll(tail);
        }

        if (paragraphs.isEmpty()) {
            paragraphs.add(new Paragraph());
        }
    }

    // -------------------------------------------------------------------------
    // 私有工具方法

    private StyleAttributeMap currentStyle() {
        return StyleAttributeMap.builder()
                .set(StyleAttributeMap.TEXT_COLOR, textColor)
                .build();
    }

    private Paragraph lastParagraph() {
        if (paragraphs.isEmpty()) {
            Paragraph p = new Paragraph();
            paragraphs.add(p);
            return p;
        }
        return paragraphs.getLast();
    }

    private static void appendNewline(List<ChatInputItem> items) {
        if (!items.isEmpty() && items.getLast() instanceof ChatInputItem.Text(String prev)) {
            items.set(items.size() - 1, new ChatInputItem.Text(prev + "\n"));
        } else {
            items.add(new ChatInputItem.Text("\n"));
        }
    }

    // =========================================================================
    // Paragraph — 段落内容与操作
    // =========================================================================

    static class Paragraph {

        /**
         * 所有内联节点在逻辑 offset 空间中统一占 1 个单位，与 RichTextArea 的 hit-test 行为对齐。
         * fireChangeEvent 和外部所有涉及节点长度的计算都应引用此常量，而不是硬编码 1。
         */
        static final int INLINE_NODE_LOGICAL_LENGTH = 1;

        /**
         * Unicode Object Replacement Character，用于在 getPlainText() 中作为内联节点的占位符。
         * 其 UTF-16 长度恰好为 1，与 INLINE_NODE_LOGICAL_LENGTH 严格对应。
         */
        private static final char INLINE_NODE_PLACEHOLDER = '\uFFFC';

        private List<StyledSegment> segments;
        private String cachedPlainText = null;
        private StyleAttributeMap paragraphAttrs;

        // -------------------------------------------------------------------------
        // 属性访问

        public StyleAttributeMap getParagraphAttributes() {
            return paragraphAttrs;
        }

        public void setParagraphAttributes(StyleAttributeMap a) {
            paragraphAttrs = a;
        }

        /**
         * 返回给控件使用的逻辑字符串。
         * TEXT segment 直接追加文本；INLINE_NODE 追加单字符占位符。
         * 字符串的 length() 与各 segment 的 logicalLength() 累加值严格一致，
         * 保证控件 hit-test 得到的 offset 可以直接用于模型的增删操作。
         */
        public String getPlainText() {
            if (cachedPlainText != null) return cachedPlainText;
            if (segments == null) {
                cachedPlainText = "";
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (StyledSegment seg : segments) {
                if (seg.getType() == StyledSegment.Type.TEXT) {
                    sb.append(seg.getText());
                } else if (seg.getType() == StyledSegment.Type.INLINE_NODE) {
                    sb.append(INLINE_NODE_PLACEHOLDER);
                }
            }
            cachedPlainText = sb.toString();
            return cachedPlainText;
        }

        public RichParagraph toRichParagraph(StyleAttributeMap style) {
            RichParagraph.Builder builder = RichParagraph.builder();
            for (StyledSegment seg : segments()) {
                if (seg.getType() == StyledSegment.Type.TEXT) {
                    builder.addSegment(seg.getText(), style);
                } else if (seg.getType() == StyledSegment.Type.INLINE_NODE) {
                    builder.addInlineNode(seg.getInlineNodeGenerator());
                }
            }
            return builder.build();
        }

        // -------------------------------------------------------------------------
        // 内容追加

        void addText(String text) {
            segments().add(StyledSegment.of(text));
            cachedPlainText = null;
        }

        void addInlineNode(Supplier<Node> generator) {
            segments().add(StyledSegment.ofInlineNode(generator));
            cachedPlainText = null;
        }

        /**
         * 将外部 segments 追加到本段末尾。
         * 相邻无样式 TEXT segment 自动合并，避免跨段删除后列表持续碎片化。
         */
        void addAll(List<StyledSegment> incoming) {
            if (incoming.isEmpty()) return;
            List<StyledSegment> current = segments();
            for (StyledSegment seg : incoming) {
                if (seg.getType() == StyledSegment.Type.TEXT
                        && !current.isEmpty()
                        && current.getLast().getType() == StyledSegment.Type.TEXT) {
                    StyledSegment last = current.getLast();
                    current.set(current.size() - 1, StyledSegment.of(last.getText() + seg.getText()));
                } else {
                    current.add(seg);
                }
            }
            cachedPlainText = null;
        }

        // -------------------------------------------------------------------------
        // 插入操作

        void insertTextSegment(int offset, String text, StyleAttributeMap attrs) {
            cachedPlainText = null;
            if (segments().isEmpty() || offset == 0) {
                segments().addFirst(StyledSegment.of(text, attrs));
                return;
            }
            int currentOffset = 0;
            for (int i = 0; i < segments().size(); i++) {
                StyledSegment seg = segments().get(i);
                int segEnd = currentOffset + logicalLength(seg);
                if (offset >= currentOffset && offset <= segEnd) {
                    if (seg.getType() == StyledSegment.Type.TEXT) {
                        int local = offset - currentOffset;
                        String merged = seg.getText().substring(0, local) + text + seg.getText().substring(local);
                        segments().set(i, StyledSegment.of(merged, attrs));
                    } else {
                        segments().add(offset == currentOffset ? i : i + 1, StyledSegment.of(text, attrs));
                    }
                    return;
                }
                currentOffset = segEnd;
            }
            // offset 超出末尾，直接追加，不与最后一段合并以保留各自样式
            segments().add(StyledSegment.of(text, attrs));
        }

        void insertNodeSegment(int offset, Supplier<Node> generator) {
            cachedPlainText = null;
            if (segments().isEmpty() || offset == 0) {
                segments().addFirst(StyledSegment.ofInlineNode(generator));
                return;
            }
            int currentOffset = 0;
            for (int i = 0; i < segments().size(); i++) {
                StyledSegment seg = segments().get(i);
                int segEnd = currentOffset + logicalLength(seg);
                if (offset >= currentOffset && offset <= segEnd) {
                    if (seg.getType() == StyledSegment.Type.TEXT) {
                        int local = offset - currentOffset;
                        if (local == 0) {
                            segments().add(i, StyledSegment.ofInlineNode(generator));
                        } else if (local == seg.getText().length()) {
                            segments().add(i + 1, StyledSegment.ofInlineNode(generator));
                        } else {
                            // 在文字中间插入节点，将文字段一分为二
                            segments().set(i, StyledSegment.of(seg.getText().substring(0, local)));
                            segments().add(i + 1, StyledSegment.ofInlineNode(generator));
                            segments().add(i + 2, StyledSegment.of(seg.getText().substring(local)));
                        }
                    } else {
                        segments().add(offset == currentOffset ? i : i + 1, StyledSegment.ofInlineNode(generator));
                    }
                    return;
                }
                currentOffset = segEnd;
            }
            segments().add(StyledSegment.ofInlineNode(generator));
        }

        // -------------------------------------------------------------------------
        // 删除与分割操作

        void removeRangeInline(TextPos start, TextPos end) {
            List<StyledSegment> result = new ArrayList<>();
            int offset = 0;
            for (StyledSegment seg : segments()) {
                int segEnd = offset + logicalLength(seg);
                if (segEnd <= start.offset()) {
                    result.add(seg);
                } else if (offset >= end.offset()) {
                    result.add(seg);
                } else if (seg.getType() == StyledSegment.Type.TEXT) {
                    int delStart = Math.max(0, start.offset() - offset);
                    int delEnd = Math.min(seg.getText().length(), end.offset() - offset);
                    if (delStart > 0) result.add(StyledSegment.of(seg.getText().substring(0, delStart)));
                    if (delEnd < seg.getText().length()) result.add(StyledSegment.of(seg.getText().substring(delEnd)));
                }
                // INLINE_NODE 落在删除范围内：直接丢弃
                offset = segEnd;
            }
            segments = result;
            cachedPlainText = null;
        }

        /**
         * 清除从 offset（含）到末尾的全部内容。
         */
        void truncateFrom(int offset) {
            if (offset == 0) {
                segments = null;
                cachedPlainText = null;
                return;
            }
            String plain = getPlainText();
            if (offset < plain.length()) {
                removeRangeInline(
                        TextPos.ofLeading(0, offset),
                        TextPos.ofLeading(0, plain.length())
                );
            }
        }

        /**
         * 收集从 offset（含）到末尾的 segments，用于跨段删除后保留尾部内容。
         * 不修改当前段落自身的内容。
         */
        List<StyledSegment> collectTailFrom(int offset) {
            List<StyledSegment> tail = new ArrayList<>();
            if (offset == 0) {
                if (segments != null) tail.addAll(segments);
                return tail;
            }
            int currentOffset = 0;
            for (StyledSegment seg : segments()) {
                int segEnd = currentOffset + logicalLength(seg);
                if (currentOffset >= offset) {
                    tail.add(seg);
                } else if (segEnd > offset) {
                    if (seg.getType() == StyledSegment.Type.TEXT) {
                        String after = seg.getText().substring(offset - currentOffset);
                        if (!after.isEmpty()) tail.add(StyledSegment.of(after));
                    } else {
                        // INLINE_NODE 跨越边界：整体保留
                        tail.add(seg);
                    }
                }
                currentOffset = segEnd;
            }
            return tail;
        }

        /**
         * 从 offset 处将段落一分为二，返回后半部分作为新段落，当前段落保留前半部分。
         */
        Paragraph splitAt(int offset) {
            Paragraph next = new Paragraph();
            List<StyledSegment> before = new ArrayList<>();
            List<StyledSegment> after = new ArrayList<>();
            int currentOffset = 0;
            boolean split = false;
            for (StyledSegment seg : segments()) {
                int segEnd = currentOffset + logicalLength(seg);
                if (split) {
                    after.add(seg);
                } else if (segEnd <= offset) {
                    before.add(seg);
                } else if (currentOffset >= offset) {
                    after.add(seg);
                    split = true;
                } else if (seg.getType() == StyledSegment.Type.TEXT) {
                    int local = offset - currentOffset;
                    String b = seg.getText().substring(0, local);
                    String a = seg.getText().substring(local);
                    if (!b.isEmpty()) before.add(StyledSegment.of(b));
                    if (!a.isEmpty()) after.add(StyledSegment.of(a));
                    split = true;
                } else {
                    // INLINE_NODE 不可分割，按边界决定归属
                    (offset == currentOffset ? after : before).add(seg);
                    split = true;
                }
                currentOffset = segEnd;
            }
            this.segments = before;
            next.segments = after.isEmpty() ? null : after;
            cachedPlainText = null;
            return next;
        }

        // -------------------------------------------------------------------------
        // ChatInputItem 收集

        /**
         * 将本段落内容追加到 items，相邻文字 item 自动合并。
         */
        void collectItems(List<ChatInputItem> items) {
            for (StyledSegment seg : segments()) {
                if (seg.getType() == StyledSegment.Type.TEXT) {
                    appendText(items, seg.getText());
                } else if (seg.getType() == StyledSegment.Type.INLINE_NODE) {
                    // generator 每次调用都创建新节点，Image 由闭包持有，开销可接受。
                    // 若后续节点类型增多，应改为插入时单独存储 metadata，而非通过实例化反查。
                    Node node = seg.getInlineNodeGenerator().get();
                    if (node instanceof ImageView iv) {
                        items.add(new ChatInputItem.ImageItem(iv.getImage()));
                    }
                }
            }
        }

        // -------------------------------------------------------------------------
        // 私有工具

        /**
         * 返回 segment 在逻辑 offset 空间中占用的长度，是段落内所有位置计算的唯一入口。
         * 新增节点类型时必须在此处显式添加 case，否则运行时立即报错。
         */
        private static int logicalLength(StyledSegment segment) {
            return switch (segment.getType()) {
                case TEXT -> segment.getText().length();
                case INLINE_NODE -> INLINE_NODE_LOGICAL_LENGTH;
                default -> throw new IllegalStateException(
                        "未处理的 StyledSegment 类型: " + segment.getType());
            };
        }

        private static void appendText(List<ChatInputItem> items, String text) {
            if (!items.isEmpty() && items.getLast() instanceof ChatInputItem.Text(String prev)) {
                items.set(items.size() - 1, new ChatInputItem.Text(prev + text));
            } else {
                items.add(new ChatInputItem.Text(text));
            }
        }

        private List<StyledSegment> segments() {
            if (segments == null) segments = new ArrayList<>(8);
            return segments;
        }
    }
}