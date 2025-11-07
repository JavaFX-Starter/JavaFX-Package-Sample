package com.icuxika.richtext;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
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

    private final List<Paragraph> paragraphs = new ArrayList<>();

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
        return paragraphs.get(index).toRichParagraph();
    }

    @Override
    protected void removeRange(TextPos start, TextPos end) {
        if (start.index() == end.index()) {
            // 删除操作发生在一行内
            Paragraph p = paragraphs.get(start.index());
            String text = p.getPlainText();
            if (!text.isEmpty()) {
                p.removeRangeInline(start, end);
            } else {
                paragraphs.remove(start.index());
            }
            return;
        }
        Paragraph startParagraph = paragraphs.get(start.index());
        Paragraph endParagraph = paragraphs.get(end.index());

        // 保存起始段落中删除点之前的内容
        String startText = startParagraph.getPlainText();
        if (start.offset() < startText.length()) {
            startParagraph.removeRangeInline(start, TextPos.ofLeading(start.index(), startText.length()));
        }

        // 保存结束段落中删除点之后的内容
        List<StyledSegment> endSegments = new ArrayList<>();
        if (end.offset() > 0) {
            int currentOffset = 0;
            for (StyledSegment segment : endParagraph.segments()) {
                int segmentLength = 0;
                if (segment.getType() == StyledSegment.Type.TEXT) {
                    segmentLength = segment.getText().length();
                } else if (segment.getType() == StyledSegment.Type.INLINE_NODE) {
                    segmentLength = "<image>".length();
                }

                int segmentStart = currentOffset;
                int segmentEnd = currentOffset + segmentLength;

                if (segmentStart >= end.offset()) {
                    endSegments.add(segment);
                } else if (segmentEnd > end.offset()) {
                    if (segment.getType() == StyledSegment.Type.TEXT) {
                        String currentText = segment.getText();
                        int localOffset = end.offset() - segmentStart;
                        if (localOffset < currentText.length()) {
                            String after = currentText.substring(localOffset);
                            if (!after.isEmpty()) {
                                endSegments.add(StyledSegment.of(after));
                            }
                        }
                    } else {
                        endSegments.add(segment);
                    }
                }

                currentOffset = segmentEnd;
            }
        } else {
            if (endParagraph.segments != null) {
                endSegments.addAll(endParagraph.segments);
            }
        }

        // 删除中间段落，包括结束段落
        if (end.index() >= start.index() + 1) {
            paragraphs.subList(start.index() + 1, end.index() + 1).clear();
        }

        // 将结束段落的剩余内容合并到起始段落
        if (!endSegments.isEmpty()) {
            for (StyledSegment segment : endSegments) {
                startParagraph.segments().add(segment);
            }
        }

        if (paragraphs.isEmpty()) {
            paragraphs.add(new Paragraph());
        }
    }

    @Override
    protected int insertTextSegment(int index, int offset, String text, StyleAttributeMap attrs) {
        Paragraph p = paragraphs.get(index);
        p.insertTextSegment(offset, text, attrs);
        return text.length();
    }

    @Override
    protected void insertLineBreak(int index, int offset) {
        Paragraph p = paragraphs.get(index);
        String text = p.getPlainText();

        // 段落开头
        if (offset == 0) {
            paragraphs.add(index, new Paragraph());
            return;
        }

        // 段落结尾
        if (offset >= text.length()) {
            paragraphs.add(index + 1, new Paragraph());
            return;
        }

        Paragraph newParagraph = p.splitAt(offset);
        paragraphs.add(index + 1, newParagraph);
    }

    @Override
    protected void insertParagraph(int index, Supplier<Region> generator) {
        System.out.println("insertParagraph");
    }

    @Override
    protected void setParagraphStyle(int index, StyleAttributeMap paragraphAttrs) {
        System.out.println("setParagraphStyle");
    }

    @Override
    protected void applyStyle(int index, int start, int end, StyleAttributeMap a, boolean merge) {
        System.out.println("applyStyle");
    }

    @Override
    public StyleAttributeMap getStyleAttributeMap(StyleResolver resolver, TextPos pos) {
        System.out.println("getStyleAttributeMap");
        return null;
    }

    public ReadWriteTextModel addSegment(String text) {
        Paragraph p = lastParagraph();
        p.addText(text);
        return this;
    }

    public ReadWriteTextModel addNodeSegment(Supplier<Node> generator) {
        Paragraph p = lastParagraph();
        p.addInlineNode(generator);
        return this;
    }

    public ReadWriteTextModel nl() {
        paragraphs.add(new Paragraph());
        return this;
    }

    private Paragraph lastParagraph() {
        int sz = paragraphs.size();
        if (sz == 0) {
            Paragraph p = new Paragraph();
            paragraphs.add(p);
            return p;
        }
        return paragraphs.get(sz - 1);
    }

    public void insertText(TextPos pos, String text) {
        insertTextSegment(pos.index(), pos.offset(), text, StyleAttributeMap.EMPTY);
        fireChangeEvent(TextPos.ofLeading(pos.index(), pos.offset()), TextPos.ofLeading(pos.index(), pos.offset() + text.length()), text.length(), 0, 0);
    }

    public void insertImage(TextPos pos, Image image) {
        Paragraph p = paragraphs.get(pos.index());
        int plainTextLength = p.getPlainText().length();
        p.insertNodeSegment(pos.offset(), () -> {
            ImageView imageView = new ImageView();
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(64);
            return imageView;
        });
        fireChangeEvent(TextPos.ofLeading(pos.index(), 0), TextPos.ofLeading(pos.index(), plainTextLength + "<image>".length()), plainTextLength + "<image>".length(), 0, 0);
    }

    static class Paragraph {
        private List<StyledSegment> segments;

        public String getPlainText() {
            if (segments == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (StyledSegment seg : segments) {
                if (seg.getType() == StyledSegment.Type.TEXT) {
                    sb.append(seg.getText());
                }
                if (seg.getType() == StyledSegment.Type.INLINE_NODE) {
                    sb.append("<image>");
                }
            }
            return sb.toString();
        }

        public RichParagraph toRichParagraph() {
            RichParagraph.Builder builder = RichParagraph.builder();
            segments().forEach(styledSegment -> {
                if (styledSegment.getType() == StyledSegment.Type.TEXT) {
                    builder.addSegment(styledSegment.getText());
                }
                if (styledSegment.getType() == StyledSegment.Type.INLINE_NODE) {
                    builder.addInlineNode(styledSegment.getInlineNodeGenerator());
                }
            });
            return builder.build();
        }

        private List<StyledSegment> segments() {
            if (segments == null) {
                segments = new ArrayList<>(8);
            }
            return segments;
        }

        void addText(String text) {
            segments().add(StyledSegment.of(text));
        }

        void addInlineNode(Supplier<Node> generator) {
            segments().add(StyledSegment.ofInlineNode(generator));
        }

        public void removeRangeInline(TextPos start, TextPos end) {
            // 空行先不考虑
            List<StyledSegment> newSegments = new ArrayList<>();
            int offset = 0;
            for (StyledSegment segment : segments()) {
                int segmentLength = 0;
                if (segment.getType() == StyledSegment.Type.TEXT) {
                    segmentLength = segment.getText().length();
                } else if (segment.getType() == StyledSegment.Type.INLINE_NODE) {
                    segmentLength = "<image>".length();
                }

                int segmentStart = offset;
                int segmentEnd = offset + segmentLength;

                if (segmentEnd <= start.offset()) {
                    // segment 完全在删除范围之前
                    newSegments.add(segment);
                } else if (segmentStart >= end.offset()) {
                    // segment 完全在删除范围之后
                    newSegments.add(segment);
                } else {
                    if (segment.getType() == StyledSegment.Type.TEXT) {
                        String text = segment.getText();
                        int deleteStart = Math.max(0, start.offset() - segmentStart);
                        int deleteEnd = Math.min(text.length(), end.offset() - segmentStart);

                        if (deleteStart > 0) {
                            String before = text.substring(0, deleteStart);
                            newSegments.add(StyledSegment.of(before));
                        }

                        if (deleteEnd < text.length()) {
                            String after = text.substring(deleteEnd);
                            newSegments.add(StyledSegment.of(after));
                        }
                    }
                }
                offset = segmentEnd;
            }
            segments = newSegments;
        }

        public void insertTextSegment(int offset, String text, StyleAttributeMap attrs) {
            if (segments().isEmpty()) {
                segments().add(StyledSegment.of(text));
                return;
            }
            if (offset == 0) {
                if (segments().getFirst().getType() == StyledSegment.Type.TEXT) {
                    String currentText = segments().getFirst().getText();
                    segments().set(0, StyledSegment.of(text + currentText));
                } else {
                    segments().addFirst(StyledSegment.of(text));
                }
                return;
            }

            int currentOffset = 0;
            for (int i = 0; i < segments().size(); i++) {
                StyledSegment segment = segments().get(i);
                int segmentLength = 0;
                if (segment.getType() == StyledSegment.Type.TEXT) {
                    segmentLength = segment.getText().length();
                } else if (segment.getType() == StyledSegment.Type.INLINE_NODE) {
                    segmentLength = "<image>".length();
                }

                int segmentStart = currentOffset;
                int segmentEnd = currentOffset + segmentLength;

                if (offset >= segmentStart && offset <= segmentEnd) {
                    if (segment.getType() == StyledSegment.Type.TEXT) {
                        String currentText = segment.getText();
                        int localOffset = offset - segmentStart;
                        String newText = currentText.substring(0, localOffset) + text + currentText.substring(localOffset);
                        segments().set(i, StyledSegment.of(newText));
                    } else {
                        if (offset == segmentStart) {
                            segments().add(i, StyledSegment.of(text));
                        } else {
                            segments().add(i + 1, StyledSegment.of(text));
                        }
                    }
                    return;
                }

                currentOffset = segmentEnd;
            }

            if (offset >= currentOffset) {
                StyledSegment lastSegment = segments().getLast();
                if (lastSegment.getType() == StyledSegment.Type.TEXT) {
                    String currentText = lastSegment.getText();
                    segments().set(segments().size() - 1, StyledSegment.of(currentText + text));
                } else {
                    segments().add(StyledSegment.of(text));
                }
            }
        }

        public void insertNodeSegment(int offset, Supplier<Node> generator) {
            if (segments().isEmpty()) {
                segments().add(StyledSegment.ofInlineNode(generator));
                return;
            }

            if (offset == 0) {
                segments().addFirst(StyledSegment.ofInlineNode(generator));
                return;
            }

            int currentOffset = 0;
            for (int i = 0; i < segments().size(); i++) {
                StyledSegment segment = segments().get(i);
                int segmentLength = 0;
                if (segment.getType() == StyledSegment.Type.TEXT) {
                    segmentLength = segment.getText().length();
                } else if (segment.getType() == StyledSegment.Type.INLINE_NODE) {
                    segmentLength = "<image>".length();
                }

                int segmentStart = currentOffset;
                int segmentEnd = currentOffset + segmentLength;

                if (offset >= segmentStart && offset <= segmentEnd) {
                    if (segment.getType() == StyledSegment.Type.TEXT) {
                        String currentText = segment.getText();
                        int localOffset = offset - segmentStart;

                        if (localOffset == 0) {
                            segments().add(i, StyledSegment.ofInlineNode(generator));
                        } else if (localOffset == currentText.length()) {
                            segments().add(i + 1, StyledSegment.ofInlineNode(generator));
                        } else {
                            String before = currentText.substring(0, localOffset);
                            String after = currentText.substring(localOffset);
                            segments().set(i, StyledSegment.of(before));
                            segments().add(i + 1, StyledSegment.ofInlineNode(generator));
                            segments().add(i + 2, StyledSegment.of(after));
                        }
                    } else {
                        if (offset == segmentStart) {
                            segments().add(i, StyledSegment.ofInlineNode(generator));
                        } else {
                            segments().add(i + 1, StyledSegment.ofInlineNode(generator));
                        }
                    }
                    return;
                }

                currentOffset = segmentEnd;
            }

            if (offset >= currentOffset) {
                segments().add(StyledSegment.ofInlineNode(generator));
            }
        }

        public Paragraph splitAt(int offset) {
            Paragraph paragraph = new Paragraph();

            List<StyledSegment> currentSegments = new ArrayList<>();
            List<StyledSegment> newSegments = new ArrayList<>();

            int currentOffset = 0;
            boolean splitDone = false;
            for (StyledSegment segment : segments()) {
                int segmentLength = 0;
                if (segment.getType() == StyledSegment.Type.TEXT) {
                    segmentLength = segment.getText().length();
                } else if (segment.getType() == StyledSegment.Type.INLINE_NODE) {
                    segmentLength = "<image>".length();
                }

                int segmentStart = currentOffset;
                int segmentEnd = currentOffset + segmentLength;

                if (splitDone) {
                    newSegments.add(segment);
                } else if (segmentEnd <= offset) {
                    currentSegments.add(segment);
                } else if (segmentStart >= offset) {
                    newSegments.add(segment);
                    splitDone = true;
                } else {
                    if (segment.getType() == StyledSegment.Type.TEXT) {
                        String currentText = segment.getText();
                        int localOffset = offset - segmentStart;
                        String before = currentText.substring(0, localOffset);
                        String after = currentText.substring(localOffset);

                        if (!before.isEmpty()) {
                            currentSegments.add(StyledSegment.of(before));
                        }

                        if (!after.isEmpty()) {
                            newSegments.add(StyledSegment.of(after));
                        }
                    } else {
                        if (offset == segmentStart) {
                            newSegments.add(segment);
                        } else {
                            currentSegments.add(segment);
                        }
                    }
                    splitDone = true;
                }

                currentOffset = segmentEnd;
            }

            this.segments = currentSegments;
            paragraph.segments = newSegments.isEmpty() ? null : newSegments;
            return paragraph;
        }
    }
}
