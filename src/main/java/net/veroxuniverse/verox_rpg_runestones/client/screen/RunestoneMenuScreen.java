package net.veroxuniverse.verox_rpg_runestones.client.screen;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.client.ClientFriends;
import net.veroxuniverse.verox_rpg_runestones.config.RunestonesConfig;
import net.veroxuniverse.verox_rpg_runestones.network.MenuEntry;
import net.veroxuniverse.verox_rpg_runestones.network.OpenRunestoneMenuPayload;
import net.veroxuniverse.verox_rpg_runestones.network.ReorderRunestonesPayload;
import net.veroxuniverse.verox_rpg_runestones.network.TeleportRequestPayload;
import net.veroxuniverse.verox_rpg_runestones.teleport.MenuSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class RunestoneMenuScreen extends Screen {

    private static final ResourceLocation NORMAL_TEXTURE = RPGRunestones.id("textures/gui/radial_menu.png");
    private static final ResourceLocation HOVER_TEXTURE = RPGRunestones.id("textures/gui/radial_menu_hover.png");
    private static final ResourceLocation DRAG_TEXTURE = RPGRunestones.id("textures/gui/radial_menu_drag_hover.png");
    private static final float DRAGGED_TINT = 0.5F;

    private static final int SLOTS_PER_PAGE = 8;
    private static final int CONTENT_CENTER_X = 112;
    private static final int CONTENT_CENTER_Y = 129;
    private static final float FLOAT_AMPLITUDE = 2.0F;
    private static final float FLOAT_SPEED = 1.6F;
    private static final float FLOAT_PHASE_STEP = 0.785F;
    private static final float EMPTY_TINT = 0.45F;
    private static final float DISABLED_TINT = 0.6F;

    private static final float TEXT_SCALE = 0.75F;
    private static final int TEXT_HEIGHT = 6;
    private static final int LINE_STEP = 8;
    private static final int TEXT_INSET = 1;
    private static final float MARQUEE_SPEED = 18.0F;
    private static final float MARQUEE_PAUSE = 1.0F;

    private static final int PLATE_TEXT_COLOR = 0x2B2B2B;
    private static final int DISPLAY_TEXT_COLOR = 0xE6E6E6;
    private static final int ANCHOR_TEXT_COLOR = 0xFFD37A;
    private static final int DISPLAY_MUTED_COLOR = 0xAAAAAA;
    private static final int COST_OK_COLOR = 0x9BE89B;
    private static final int COST_MISSING_COLOR = 0xFF7070;
    private static final int INITIALS_COLOR = 0x8FE3FF;
    private static final int INITIALS_HOVER_COLOR = 0xD2F7FF;
    private static final int GLOBAL_INITIALS_COLOR = 0xC08CFF;
    private static final int GLOBAL_INITIALS_HOVER_COLOR = 0xE4CCFF;
    private static final int GLOBAL_TEXT_COLOR = 0xCDA6FF;
    private static final int FRIENDS_TEXT_COLOR = 0x2B2B2B;
    private static final int FRIENDS_HOVER_COLOR = 0x1E9FD0;
    private static final float INITIALS_SCALE = 1.5F;
    private static final Set<String> SKIPPED_WORDS = Set.of("of", "the", "a", "an", "and", "to");

    private record Line(String text, int color, boolean marquee) {}

    private final OpenRunestoneMenuPayload payload;
    private final List<MenuEntry> entries;
    private int dragIndex = -1;
    private RadialMenuLayout.Element dragElement;
    private double dragStartX;
    private double dragStartY;
    private RadialMenuLayout layout;
    private int page;
    private int left;
    private int top;
    private RadialMenuLayout.Element lastHovered;
    private long hoverStart;
    private boolean plateHovered;
    private long plateHoverStart;

    public RunestoneMenuScreen(OpenRunestoneMenuPayload payload) {
        super(Component.translatable("gui." + RPGRunestones.MOD_ID + ".menu.title"));
        this.payload = payload;
        this.entries = new ArrayList<>(payload.entries());
    }

    @Override
    protected void init() {
        if (this.layout == null) {
            this.layout = RadialMenuLayout.load(NORMAL_TEXTURE, HOVER_TEXTURE, DRAG_TEXTURE);
        }
        this.left = this.width / 2 - CONTENT_CENTER_X;
        this.top = this.height / 2 - CONTENT_CENTER_Y;
        this.page = Mth.clamp(this.page, 0, this.pageCount() - 1);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        long now = Util.getMillis();
        RadialMenuLayout.Element hovered = this.interactiveElementAt(mouseX, mouseY);
        if (hovered != this.lastHovered) {
            this.lastHovered = hovered;
            this.hoverStart = now;
        }
        RadialMenuLayout.Element underMouse = this.layout.elementAt(Mth.floor(mouseX - this.left), Mth.floor(mouseY - this.top));
        boolean overPlate = underMouse != null && underMouse.kind() == RadialMenuLayout.Kind.NAME_PLATE;
        if (overPlate != this.plateHovered) {
            this.plateHovered = overPlate;
            this.plateHoverStart = now;
        }

        float time = (now % 3_600_000L) / 1000.0F;
        for (RadialMenuLayout.Element element : this.layout.elements()) {
            boolean isHovered = element == hovered;
            switch (element.kind()) {
                case STONE -> this.renderStone(graphics, element, isHovered, time, mouseX, mouseY);
                case ARROW_LEFT -> this.renderButton(graphics, element, isHovered, this.page > 0);
                case ARROW_RIGHT -> this.renderButton(graphics, element, isHovered, this.page < this.pageCount() - 1);
                default -> this.drawElement(graphics, element, isHovered);
            }
        }

        this.renderPlateText(graphics, now);
        this.renderFriendsText(graphics, hovered != null && hovered.kind() == RadialMenuLayout.Kind.FRIENDS);
        this.renderDisplayText(graphics, hovered, now);
        this.renderDragGhost(graphics, mouseX, mouseY);

        if (hovered != null && hovered.kind() == RadialMenuLayout.Kind.FRIENDS) {
            int pending = ClientFriends.get().incoming().size();
            Component tooltip = pending > 0
                    ? Component.translatable("gui." + RPGRunestones.MOD_ID + ".menu.friends_pending", pending)
                    : Component.translatable("gui." + RPGRunestones.MOD_ID + ".menu.friends_tooltip");
            graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
        } else if (hovered != null && hovered.kind() == RadialMenuLayout.Kind.NAME_PLATE) {
            graphics.renderTooltip(this.font, Component.translatable("gui." + RPGRunestones.MOD_ID + ".menu.rename"), mouseX, mouseY);
        }
    }

    private void renderStone(GuiGraphics graphics, RadialMenuLayout.Element element, boolean hovered, float time, int mouseX, int mouseY) {
        MenuEntry entry = this.entryForSlot(element.slot());
        int index = this.page * SLOTS_PER_PAGE + element.slot();
        boolean dragging = this.dragIndex >= 0;
        boolean isDragged = dragging && index == this.dragIndex;
        boolean isDropTarget = dragging && !isDragged && element == this.stoneAt(mouseX, mouseY) && this.canDropOn(index);
        float offset = entry == null ? 0.0F : this.snapToScreenPixel(Mth.sin(time * FLOAT_SPEED + element.slot() * FLOAT_PHASE_STEP) * FLOAT_AMPLITUDE);

        RadialMenuLayout.State state = RadialMenuLayout.State.NORMAL;
        if (isDropTarget) {
            state = RadialMenuLayout.State.DRAG;
        } else if (hovered && !dragging) {
            state = hasShiftDown() && this.canDrag(entry) ? RadialMenuLayout.State.DRAG : RadialMenuLayout.State.HOVER;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, offset, 0.0F);
        if (entry == null) {
            graphics.setColor(EMPTY_TINT, EMPTY_TINT, EMPTY_TINT, 1.0F);
        } else if (isDragged) {
            graphics.setColor(DRAGGED_TINT, DRAGGED_TINT, DRAGGED_TINT, 1.0F);
        }
        this.drawElement(graphics, element, state);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (entry != null && !isDragged) {
            this.renderInitials(graphics, element, entry, state != RadialMenuLayout.State.NORMAL);
        }
        graphics.pose().popPose();
    }

    private void renderDragGhost(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.dragIndex < 0 || this.dragElement == null || this.dragIndex >= this.entries.size()) return;

        graphics.pose().pushPose();
        graphics.pose().translate((float) (mouseX - this.dragStartX), (float) (mouseY - this.dragStartY), 50.0F);
        this.drawElement(graphics, this.dragElement, RadialMenuLayout.State.DRAG);
        this.renderInitials(graphics, this.dragElement, this.entries.get(this.dragIndex), true);
        graphics.pose().popPose();
    }

    private float snapToScreenPixel(float guiOffset) {
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        return (float) (Math.round(guiOffset * guiScale) / guiScale);
    }

    private void renderInitials(GuiGraphics graphics, RadialMenuLayout.Element element, MenuEntry entry, boolean hovered) {
        String initials = initials(entry.name());
        if (initials.isEmpty()) return;

        int color;
        if (entry.soulAnchor()) {
            color = ANCHOR_TEXT_COLOR;
        } else if (entry.global()) {
            color = hovered ? GLOBAL_INITIALS_HOVER_COLOR : GLOBAL_INITIALS_COLOR;
        } else {
            color = hovered ? INITIALS_HOVER_COLOR : INITIALS_COLOR;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(this.left + Math.round(element.centroidX()), this.top + Math.round(element.centroidY()), 0.0F);
        graphics.pose().scale(INITIALS_SCALE, INITIALS_SCALE, 1.0F);
        graphics.drawString(this.font, initials, -this.font.width(initials) / 2, -this.font.lineHeight / 2 + 1, color, true);
        graphics.pose().popPose();
    }

    private static String initials(String name) {
        StringBuilder builder = new StringBuilder();
        for (String word : name.trim().split("\\s+")) {
            if (word.isEmpty() || SKIPPED_WORDS.contains(word.toLowerCase(Locale.ROOT))) continue;
            builder.appendCodePoint(Character.toUpperCase(word.codePointAt(0)));
            if (builder.length() >= 2) break;
        }
        if (builder.isEmpty() && !name.isBlank()) {
            builder.appendCodePoint(Character.toUpperCase(name.trim().codePointAt(0)));
        }
        return builder.toString();
    }

    private void renderFriendsText(GuiGraphics graphics, boolean hovered) {
        RadialMenuLayout.Element friends = this.layout.find(RadialMenuLayout.Kind.FRIENDS);
        if (friends == null) return;
        String label = Component.translatable("gui." + RPGRunestones.MOD_ID + ".menu.friends").getString();
        this.renderLines(graphics, friends, List.of(new Line(label, hovered ? FRIENDS_HOVER_COLOR : FRIENDS_TEXT_COLOR, false)), -1.0F);
    }

    private void renderButton(GuiGraphics graphics, RadialMenuLayout.Element element, boolean hovered, boolean enabled) {
        if (!enabled) {
            graphics.setColor(DISABLED_TINT, DISABLED_TINT, DISABLED_TINT, 1.0F);
        }
        this.drawElement(graphics, element, hovered && enabled ? RadialMenuLayout.State.HOVER : RadialMenuLayout.State.NORMAL);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawElement(GuiGraphics graphics, RadialMenuLayout.Element element, boolean hovered) {
        this.drawElement(graphics, element, hovered ? RadialMenuLayout.State.HOVER : RadialMenuLayout.State.NORMAL);
    }

    private void drawElement(GuiGraphics graphics, RadialMenuLayout.Element element, RadialMenuLayout.State state) {
        ResourceLocation texture = switch (state) {
            case NORMAL -> NORMAL_TEXTURE;
            case HOVER -> HOVER_TEXTURE;
            case DRAG -> DRAG_TEXTURE;
        };
        for (RadialMenuLayout.Span span : element.spans(state)) {
            graphics.blit(texture, this.left + span.x(), this.top + span.y(), span.x(), span.y(), span.width(), 1,
                    RadialMenuLayout.SIZE, RadialMenuLayout.SIZE);
        }
    }

    private void renderPlateText(GuiGraphics graphics, long now) {
        RadialMenuLayout.Element plate = this.layout.find(RadialMenuLayout.Kind.NAME_PLATE);
        if (plate == null) return;

        String text = this.payload.source() == MenuSource.TABLET
                ? Component.translatable("gui." + RPGRunestones.MOD_ID + ".menu.tablet").getString()
                : this.payload.currentName();
        float marqueeTime = this.plateHovered ? (now - this.plateHoverStart) / 1000.0F : -1.0F;
        this.renderLines(graphics, plate, List.of(new Line(text, PLATE_TEXT_COLOR, true)), marqueeTime);
    }

    private void renderDisplayText(GuiGraphics graphics, RadialMenuLayout.Element hovered, long now) {
        RadialMenuLayout.Element display = this.layout.find(RadialMenuLayout.Kind.DISPLAY);
        if (display == null) return;

        MenuEntry entry = hovered != null && hovered.kind() == RadialMenuLayout.Kind.STONE ? this.entryForSlot(hovered.slot()) : null;
        List<Line> lines = new ArrayList<>();
        if (entry != null) {
            int nameColor = entry.soulAnchor() ? ANCHOR_TEXT_COLOR : (entry.global() ? GLOBAL_TEXT_COLOR : DISPLAY_TEXT_COLOR);
            lines.add(new Line(entry.name(), nameColor, true));
            lines.add(new Line(this.locationText(entry), DISPLAY_MUTED_COLOR, false));
            lines.add(new Line(this.costText(entry.cost()), entry.affordable() ? COST_OK_COLOR : COST_MISSING_COLOR, false));
            this.renderLines(graphics, display, lines, (now - this.hoverStart) / 1000.0F);
            return;
        }

        String key = this.entries.isEmpty() ? "menu.empty" : (hasShiftDown() || this.dragIndex >= 0 ? "menu.reorder" : "menu.choose");
        Component message = Component.translatable("gui." + RPGRunestones.MOD_ID + "." + key);
        int maxWidth = (int) ((display.innerMaxX() - display.innerMinX() + 1 - TEXT_INSET * 2) / TEXT_SCALE);
        List<FormattedCharSequence> wrapped = this.font.split(message, maxWidth);
        for (int i = 0; i < Math.min(2, wrapped.size()); i++) {
            lines.add(new Line(toPlain(wrapped.get(i)), DISPLAY_TEXT_COLOR, false));
        }
        if (!this.entries.isEmpty()) {
            lines.add(new Line(Component.translatable("gui." + RPGRunestones.MOD_ID + ".menu.page", this.page + 1, this.pageCount()).getString(),
                    DISPLAY_MUTED_COLOR, false));
        }
        this.renderLines(graphics, display, lines, -1.0F);
    }

    private void renderLines(GuiGraphics graphics, RadialMenuLayout.Element area, List<Line> lines, float marqueeTime) {
        int x0 = this.left + area.innerMinX() + TEXT_INSET;
        int x1 = this.left + area.innerMaxX() + 1 - TEXT_INSET;
        int y0 = this.top + area.innerMinY();
        int y1 = this.top + area.innerMaxY() + 1;
        int areaWidth = x1 - x0;
        int maxWidth = (int) (areaWidth / TEXT_SCALE);

        int totalHeight = (lines.size() - 1) * LINE_STEP + TEXT_HEIGHT;
        int y = y0 + (y1 - y0 - totalHeight) / 2;

        graphics.enableScissor(x0, y0, x1, y1);
        for (Line line : lines) {
            int textWidth = this.font.width(line.text());
            String text = line.text();
            float offset = 0.0F;
            boolean scrolling = false;

            if (textWidth > maxWidth) {
                if (line.marquee() && marqueeTime >= 0.0F) {
                    offset = marqueeOffset(marqueeTime, textWidth - maxWidth);
                    scrolling = true;
                } else {
                    text = this.ellipsize(text, maxWidth);
                    textWidth = this.font.width(text);
                }
            }

            graphics.pose().pushPose();
            if (scrolling) {
                graphics.pose().translate(x0, y, 0.0F);
                graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
                graphics.drawString(this.font, text, Math.round(-offset), 0, line.color(), false);
            } else {
                graphics.pose().translate(x0 + areaWidth / 2.0F, y, 0.0F);
                graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
                graphics.drawString(this.font, text, -textWidth / 2, 0, line.color(), false);
            }
            graphics.pose().popPose();
            y += LINE_STEP;
        }
        graphics.disableScissor();
    }

    private static float marqueeOffset(float time, int overflow) {
        float travel = overflow / MARQUEE_SPEED;
        float cycle = (MARQUEE_PAUSE + travel) * 2.0F;
        float t = time % cycle;
        if (t < MARQUEE_PAUSE) return 0.0F;
        if (t < MARQUEE_PAUSE + travel) return (t - MARQUEE_PAUSE) * MARQUEE_SPEED;
        if (t < MARQUEE_PAUSE * 2.0F + travel) return overflow;
        return overflow - (t - MARQUEE_PAUSE * 2.0F - travel) * MARQUEE_SPEED;
    }

    private String ellipsize(String text, int maxWidth) {
        String ellipsis = "...";
        return this.font.plainSubstrByWidth(text, maxWidth - this.font.width(ellipsis)) + ellipsis;
    }

    private static String toPlain(FormattedCharSequence sequence) {
        StringBuilder builder = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });
        return builder.toString();
    }

    private String locationText(MenuEntry entry) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.level().dimension().location().toString().equals(entry.dimension())) {
            return formatDistance((int) Math.sqrt(player.blockPosition().distSqr(entry.pos())));
        }
        return prettify(entry.dimension());
    }

    private static String formatDistance(int meters) {
        if (meters < 1000) return meters + "m";
        if (meters < 10000) return String.format(Locale.ROOT, "%.1fkm", meters / 1000.0);
        return (meters / 1000) + "km";
    }

    private String costText(int cost) {
        if (cost <= 0) {
            return Component.translatable("gui." + RPGRunestones.MOD_ID + ".menu.free").getString();
        }
        String key = this.payload.costMode() == RunestonesConfig.CostMode.XP.ordinal() ? "cost.xp" : "cost.dust";
        return Component.translatable("gui." + RPGRunestones.MOD_ID + ".menu." + key, cost).getString();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hasShiftDown()) {
            RadialMenuLayout.Element stone = this.stoneAt(mouseX, mouseY);
            if (stone != null && this.canDrag(this.entryForSlot(stone.slot()))) {
                this.dragIndex = this.page * SLOTS_PER_PAGE + stone.slot();
                this.dragElement = stone;
                this.dragStartX = mouseX;
                this.dragStartY = mouseY;
                return true;
            }
        }
        if (button == 0) {
            RadialMenuLayout.Element element = this.interactiveElementAt(mouseX, mouseY);
            if (element != null) {
                switch (element.kind()) {
                    case STONE -> {
                        this.click();
                        MenuEntry entry = this.entryForSlot(element.slot());
                        PacketDistributor.sendToServer(new TeleportRequestPayload(entry.id(), this.payload.source(), this.payload.currentId()));
                        this.onClose();
                    }
                    case ARROW_LEFT -> this.changePage(-1);
                    case ARROW_RIGHT -> this.changePage(1);
                    case FRIENDS -> {
                        this.click();
                        Minecraft.getInstance().setScreen(new FriendsScreen(this));
                    }
                    case NAME_PLATE -> {
                        this.click();
                        this.payload.currentId().ifPresent(id ->
                                Minecraft.getInstance().setScreen(new RunestoneEditScreen(id, this.payload.currentName(),
                                        this.payload.global(), this.payload.canSetGlobal(), true)));
                    }
                    default -> {
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.dragIndex >= 0) return true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.dragIndex < 0) return super.mouseReleased(mouseX, mouseY, button);

        RadialMenuLayout.Element target = this.stoneAt(mouseX, mouseY);
        if (target != null) {
            int targetIndex = this.page * SLOTS_PER_PAGE + target.slot();
            if (targetIndex != this.dragIndex && this.canDropOn(targetIndex)) {
                this.moveEntry(this.dragIndex, targetIndex);
                this.click();
            }
        }
        this.dragIndex = -1;
        this.dragElement = null;
        return true;
    }

    private void moveEntry(int from, int to) {
        MenuEntry moved = this.entries.get(from);
        if (to < this.entries.size()) {
            MenuEntry other = this.entries.get(to);
            this.entries.set(to, moved);
            this.entries.set(from, other);
            PacketDistributor.sendToServer(new ReorderRunestonesPayload(moved.id(), Optional.of(other.id())));
        } else {
            this.entries.remove(from);
            this.entries.add(moved);
            PacketDistributor.sendToServer(new ReorderRunestonesPayload(moved.id(), Optional.empty()));
        }
    }

    private boolean canDrag(MenuEntry entry) {
        return entry != null && !entry.soulAnchor();
    }

    private boolean canDropOn(int index) {
        if (index >= this.entries.size()) return true;
        return !this.entries.get(index).soulAnchor();
    }

    private RadialMenuLayout.Element stoneAt(double mouseX, double mouseY) {
        RadialMenuLayout.Element element = this.layout.elementAt(Mth.floor(mouseX - this.left), Mth.floor(mouseY - this.top));
        return element != null && element.kind() == RadialMenuLayout.Kind.STONE ? element : null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            this.changePage(scrollY > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void changePage(int direction) {
        int target = Mth.clamp(this.page + direction, 0, this.pageCount() - 1);
        if (target == this.page) return;
        this.page = target;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }

    private void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private RadialMenuLayout.Element interactiveElementAt(double mouseX, double mouseY) {
        RadialMenuLayout.Element element = this.layout.elementAt(Mth.floor(mouseX - this.left), Mth.floor(mouseY - this.top));
        if (element == null) return null;

        return switch (element.kind()) {
            case STONE -> this.entryForSlot(element.slot()) != null ? element : null;
            case ARROW_LEFT -> this.page > 0 ? element : null;
            case ARROW_RIGHT -> this.page < this.pageCount() - 1 ? element : null;
            case NAME_PLATE -> this.payload.canEdit() && this.payload.currentId().isPresent() ? element : null;
            case FRIENDS -> element;
            case DISPLAY -> null;
        };
    }

    private MenuEntry entryForSlot(int slot) {
        int index = this.page * SLOTS_PER_PAGE + slot;
        return index >= 0 && index < this.entries.size() ? this.entries.get(index) : null;
    }

    private int pageCount() {
        return Math.max(1, (this.entries.size() + SLOTS_PER_PAGE - 1) / SLOTS_PER_PAGE);
    }

    private static String prettify(String id) {
        String path = id.substring(id.indexOf(':') + 1);
        StringBuilder builder = new StringBuilder();
        for (String word : path.split("_")) {
            if (word.isEmpty()) continue;
            if (!builder.isEmpty()) builder.append(' ');
            builder.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        return builder.toString();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}