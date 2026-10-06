package net.veroxuniverse.verox_rpg_runestones.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class RadialMenuLayout {

    public static final int SIZE = 256;
    private static final float RING_CENTER = 111.5F;
    private static final float STONE_MIN_DISTANCE = 60.0F;
    private static final int BOTTOM_ROW_Y = 215;
    private static final int PLATE_MAX_Y = 88;

    public enum Kind { STONE, NAME_PLATE, DISPLAY, ARROW_LEFT, FRIENDS, ARROW_RIGHT }

    public enum State { NORMAL, HOVER, DRAG }

    public record Span(int y, int x, int width) {}

    public static final class Element {
        private final Kind kind;
        private final int slot;
        private final int minX;
        private final int minY;
        private final int maxX;
        private final int maxY;
        private final float centroidX;
        private final float centroidY;
        private final List<Span> normalSpans;
        private List<Span> hoverSpans;
        private List<Span> dragSpans;
        private int innerMinX;
        private int innerMinY;
        private int innerMaxX;
        private int innerMaxY;

        private Element(Kind kind, int slot, int[] bounds, float centroidX, float centroidY, List<Span> normalSpans) {
            this.kind = kind;
            this.slot = slot;
            this.centroidX = centroidX;
            this.centroidY = centroidY;
            this.minX = bounds[0];
            this.minY = bounds[1];
            this.maxX = bounds[2];
            this.maxY = bounds[3];
            this.normalSpans = normalSpans;
            this.hoverSpans = normalSpans;
            this.dragSpans = normalSpans;
            this.innerMinX = this.minX;
            this.innerMinY = this.minY;
            this.innerMaxX = this.maxX;
            this.innerMaxY = this.maxY;
        }

        public Kind kind() { return this.kind; }
        public int slot() { return this.slot; }
        public int minX() { return this.minX; }
        public int minY() { return this.minY; }
        public int maxX() { return this.maxX; }
        public int maxY() { return this.maxY; }
        public int centerX() { return (this.minX + this.maxX) / 2; }
        public int centerY() { return (this.minY + this.maxY) / 2; }
        public float centroidX() { return this.centroidX; }
        public float centroidY() { return this.centroidY; }
        public List<Span> spans(State state) {
            return switch (state) {
                case NORMAL -> this.normalSpans;
                case HOVER -> this.hoverSpans;
                case DRAG -> this.dragSpans;
            };
        }
        public int innerMinX() { return this.innerMinX; }
        public int innerMinY() { return this.innerMinY; }
        public int innerMaxX() { return this.innerMaxX; }
        public int innerMaxY() { return this.innerMaxY; }
    }

    private final List<Element> elements = new ArrayList<>();
    private final int[] hitMap = new int[SIZE * SIZE];

    private RadialMenuLayout() {
        Arrays.fill(this.hitMap, -1);
    }

    public List<Element> elements() {
        return this.elements;
    }

    public Element elementAt(int x, int y) {
        if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) return null;
        int index = this.hitMap[y * SIZE + x];
        return index < 0 ? null : this.elements.get(index);
    }

    public Element find(Kind kind) {
        for (Element element : this.elements) {
            if (element.kind == kind) return element;
        }
        return null;
    }

    public static RadialMenuLayout load(ResourceLocation normalTexture, ResourceLocation hoverTexture, ResourceLocation dragTexture) {
        RadialMenuLayout layout = new RadialMenuLayout();
        try (NativeImage normal = read(normalTexture); NativeImage hover = read(hoverTexture); NativeImage drag = read(dragTexture)) {
            List<int[]> normalComponents = components(normal);
            for (int[] pixels : normalComponents) {
                layout.elements.add(classify(pixels));
            }
            for (int i = 0; i < layout.elements.size(); i++) {
                for (int pixel : normalComponents.get(i)) {
                    layout.hitMap[pixel] = i;
                }
                Element element = layout.elements.get(i);
                if (element.kind == Kind.NAME_PLATE || element.kind == Kind.DISPLAY || element.kind == Kind.FRIENDS) {
                    measureInnerArea(normal, element);
                }
            }

            layout.assignOverlay(components(hover), State.HOVER);
            layout.assignOverlay(components(drag), State.DRAG);
        } catch (Exception exception) {
            RPGRunestones.LOGGER.error("Failed to read radial menu textures", exception);
        }
        return layout;
    }

    private void assignOverlay(List<int[]> overlayComponents, State state) {
        for (int[] pixels : overlayComponents) {
            int owner = -1;
            for (int pixel : pixels) {
                if (this.hitMap[pixel] >= 0) {
                    owner = this.hitMap[pixel];
                    break;
                }
            }
            if (owner < 0) continue;

            Element element = this.elements.get(owner);
            if (state == State.HOVER) {
                element.hoverSpans = spans(pixels);
                for (int pixel : pixels) {
                    if (this.hitMap[pixel] < 0) this.hitMap[pixel] = owner;
                }
            } else {
                element.dragSpans = spans(pixels);
            }
        }
    }

    private static NativeImage read(ResourceLocation location) throws Exception {
        try (InputStream stream = Minecraft.getInstance().getResourceManager().open(location)) {
            return NativeImage.read(stream);
        }
    }

    private static List<int[]> components(NativeImage image) {
        int width = Math.min(image.getWidth(), SIZE);
        int height = Math.min(image.getHeight(), SIZE);
        boolean[] seen = new boolean[SIZE * SIZE];
        List<int[]> result = new ArrayList<>();
        IntArrayList stack = new IntArrayList();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int start = y * SIZE + x;
                if (seen[start] || alpha(image, x, y) == 0) continue;

                IntArrayList pixels = new IntArrayList();
                stack.clear();
                stack.add(start);
                seen[start] = true;
                while (!stack.isEmpty()) {
                    int current = stack.removeInt(stack.size() - 1);
                    pixels.add(current);
                    int cx = current % SIZE;
                    int cy = current / SIZE;
                    int[][] neighbours = {{cx + 1, cy}, {cx - 1, cy}, {cx, cy + 1}, {cx, cy - 1}};
                    for (int[] n : neighbours) {
                        if (n[0] < 0 || n[1] < 0 || n[0] >= width || n[1] >= height) continue;
                        int index = n[1] * SIZE + n[0];
                        if (seen[index] || alpha(image, n[0], n[1]) == 0) continue;
                        seen[index] = true;
                        stack.add(index);
                    }
                }
                result.add(pixels.toIntArray());
            }
        }
        return result;
    }

    private static void measureInnerArea(NativeImage image, Element element) {
        int cx = element.centerX();
        int cy = element.centerY();
        int fill = image.getPixelRGBA(cx, cy);

        int left = cx;
        while (left - 1 > element.minX && image.getPixelRGBA(left - 1, cy) == fill) left--;
        int right = cx;
        while (right + 1 < element.maxX && image.getPixelRGBA(right + 1, cy) == fill) right++;
        int topEdge = cy;
        while (topEdge - 1 > element.minY && image.getPixelRGBA(cx, topEdge - 1) == fill) topEdge--;
        int bottom = cy;
        while (bottom + 1 < element.maxY && image.getPixelRGBA(cx, bottom + 1) == fill) bottom++;

        if (right - left >= 8 && bottom - topEdge >= 4) {
            element.innerMinX = left;
            element.innerMaxX = right;
            element.innerMinY = topEdge;
            element.innerMaxY = bottom;
        }
    }

    private static int alpha(NativeImage image, int x, int y) {
        return (image.getPixelRGBA(x, y) >>> 24) & 0xFF;
    }

    private static Element classify(int[] pixels) {
        int[] bounds = {SIZE, SIZE, 0, 0};
        double sumX = 0;
        double sumY = 0;
        for (int pixel : pixels) {
            int x = pixel % SIZE;
            int y = pixel / SIZE;
            bounds[0] = Math.min(bounds[0], x);
            bounds[1] = Math.min(bounds[1], y);
            bounds[2] = Math.max(bounds[2], x);
            bounds[3] = Math.max(bounds[3], y);
            sumX += x;
            sumY += y;
        }
        double centerX = sumX / pixels.length;
        double centerY = sumY / pixels.length;
        List<Span> spans = spans(pixels);

        if (centerY > BOTTOM_ROW_Y) {
            Kind kind = centerX < RING_CENTER - 20 ? Kind.ARROW_LEFT : centerX > RING_CENTER + 20 ? Kind.ARROW_RIGHT : Kind.FRIENDS;
            return new Element(kind, -1, bounds, (float) centerX, (float) centerY, spans);
        }

        double dx = centerX - RING_CENTER;
        double dy = centerY - RING_CENTER;
        if (Math.sqrt(dx * dx + dy * dy) > STONE_MIN_DISTANCE) {
            double angle = Math.atan2(dy, dx) + Math.PI / 2.0;
            int slot = Math.floorMod((int) Math.round(angle / (Math.PI / 4.0)), 8);
            return new Element(Kind.STONE, slot, bounds, (float) centerX, (float) centerY, spans);
        }

        return new Element(centerY < PLATE_MAX_Y ? Kind.NAME_PLATE : Kind.DISPLAY, -1, bounds, (float) centerX, (float) centerY, spans);
    }

    private static List<Span> spans(int[] pixels) {
        int[] sorted = pixels.clone();
        Arrays.sort(sorted);
        List<Span> result = new ArrayList<>();
        int i = 0;
        while (i < sorted.length) {
            int start = sorted[i];
            int y = start / SIZE;
            int x = start % SIZE;
            int width = 1;
            while (i + width < sorted.length && sorted[i + width] == start + width && (start + width) / SIZE == y) {
                width++;
            }
            result.add(new Span(y, x, width));
            i += width;
        }
        result.sort(Comparator.comparingInt(Span::y).thenComparingInt(Span::x));
        return result;
    }
}
