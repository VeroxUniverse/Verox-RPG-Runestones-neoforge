package net.veroxuniverse.verox_rpg_runestones.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.client.ClientFriends;
import net.veroxuniverse.verox_rpg_runestones.client.ClientSkinCache;
import net.veroxuniverse.verox_rpg_runestones.network.FriendActionPayload;
import net.veroxuniverse.verox_rpg_runestones.network.FriendInfo;
import net.veroxuniverse.verox_rpg_runestones.network.FriendsSyncPayload;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FriendsScreen extends Screen {

    private static final ResourceLocation BACKGROUND = RPGRunestones.id("textures/gui/friends_menu.png");
    private static final ResourceLocation WIDGETS = RPGRunestones.id("textures/gui/friends_widgets.png");
    private static final int BACKGROUND_SIZE = 256;
    private static final int WIDGETS_SIZE = 128;

    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 176;
    private static final int LIST_X = 8;
    private static final int LIST_Y = 22;
    private static final int LIST_WIDTH = 142;
    private static final int ROW_HEIGHT = 20;
    private static final int VISIBLE_ROWS = 7;
    private static final int TRACK_X = 152;
    private static final int TRACK_Y = 22;
    private static final int TRACK_HEIGHT = 146;
    private static final int SIDE_X = 166;
    private static final int FIELD_Y = 22;
    private static final int SEND_Y = 38;
    private static final int NOTICE_Y = 56;
    private static final int BACK_Y = 128;
    private static final int TOGGLE_Y = 152;
    private static final int SIDE_WIDTH = 66;
    private static final int FIELD_TEXT_INSET = 2;
    private static final int HEADER_Y = 8;
    private static final int HEAD_SIZE = 16;
    private static final int REMOVE_OFFSET = 128;
    private static final int ACCEPT_OFFSET = 114;

    private static final int DARK_TEXT = 0x2B2B2B;
    private static final int LIGHT_TEXT = 0xE6E6E6;
    private static final int MUTED_TEXT = 0x9A9A9A;
    private static final int OFFLINE_TEXT = 0x8A8A8A;

    private record Widget(int u, int v, int hoverU, int hoverV, int width, int height) {}

    private static final Widget REMOVE = new Widget(0, 0, 17, 0, 11, 11);
    private static final Widget ACCEPT = new Widget(34, 0, 51, 0, 11, 11);
    private static final Widget THUMB = new Widget(68, 0, 80, 0, 6, 15);
    private static final Widget WIDE = new Widget(0, 24, 0, 44, 66, 14);
    private static final Widget FIELD = new Widget(0, 64, 0, 84, 66, 14);
    private static final Widget BACK = new Widget(76, 24, 100, 24, 18, 17);
    private static final int BADGE_U = 79;
    private static final int BADGE_V = 51;
    private static final int BADGE_SIZE = 9;
    private static final int CELL_MARGIN = 3;

    private enum View { FRIENDS, REQUESTS }

    private enum RowType { FRIEND, INCOMING, OUTGOING }

    private record Row(FriendInfo info, RowType type) {}

    private final Screen parent;
    private View view = View.FRIENDS;
    private int scroll;
    private boolean draggingThumb;
    private int left;
    private int top;
    private EditBox nameField;
    private Component notice = Component.empty();

    public FriendsScreen(Screen parent) {
        super(Component.translatable("gui." + RPGRunestones.MOD_ID + ".friends.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_WIDTH) / 2;
        this.top = (this.height - PANEL_HEIGHT) / 2;

        String previous = this.nameField == null ? "" : this.nameField.getValue();
        this.nameField = new EditBox(this.font, this.left + SIDE_X + FIELD_TEXT_INSET, this.top + FIELD_Y + 3, SIDE_WIDTH - FIELD_TEXT_INSET * 2, 10,
                Component.translatable("gui." + RPGRunestones.MOD_ID + ".friends.name"));
        this.nameField.setBordered(false);
        this.nameField.setMaxLength(16);
        this.nameField.setTextColor(LIGHT_TEXT);
        this.nameField.setHint(Component.translatable("gui." + RPGRunestones.MOD_ID + ".friends.name").withStyle(ChatFormatting.DARK_GRAY));
        this.nameField.setValue(previous);
        this.addRenderableWidget(this.nameField);

        PacketDistributor.sendToServer(FriendActionPayload.of(FriendActionPayload.Action.SYNC, new UUID(0L, 0L)));
    }

    public void onSync(FriendsSyncPayload payload) {
        if (!payload.noticeKey().isEmpty()) {
            this.notice = Component.translatable(payload.noticeKey(), payload.noticeArg());
        }
        this.scroll = Mth.clamp(this.scroll, 0, this.maxScroll());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(BACKGROUND, this.left, this.top, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, BACKGROUND_SIZE, BACKGROUND_SIZE);
        this.drawWidget(graphics, FIELD, SIDE_X, FIELD_Y, this.nameField.isFocused());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int mx = mouseX - this.left;
        int my = mouseY - this.top;
        FriendsSyncPayload data = ClientFriends.get();

        Component header = this.view == View.FRIENDS
                ? Component.translatable("gui." + RPGRunestones.MOD_ID + ".friends.header", data.friends().size())
                : Component.translatable("gui." + RPGRunestones.MOD_ID + ".friends.requests_header");
        this.drawCentered(graphics, header, 8 + 74, HEADER_Y, DARK_TEXT);
        this.drawCentered(graphics, Component.translatable("gui." + RPGRunestones.MOD_ID + ".friends.add"), SIDE_X + SIDE_WIDTH / 2, HEADER_Y, DARK_TEXT);

        this.renderRows(graphics, mx, my);
        this.renderScrollbar(graphics, mx, my);

        this.drawButton(graphics, SEND_Y, Component.translatable("gui." + RPGRunestones.MOD_ID + ".friends.send"), mx, my);
        this.drawButton(graphics, TOGGLE_Y, Component.translatable("gui." + RPGRunestones.MOD_ID + "."
                + (this.view == View.FRIENDS ? "friends.show_requests" : "friends.show_friends")), mx, my);
        this.drawWidget(graphics, BACK, SIDE_X, BACK_Y, this.inside(mx, my, SIDE_X, BACK_Y, BACK.width(), BACK.height()));

        int pending = data.incoming().size();
        if (pending > 0 && this.view == View.FRIENDS) {
            int bx = this.left + SIDE_X + SIDE_WIDTH - 5;
            int by = this.top + TOGGLE_Y - 4;
            graphics.blit(WIDGETS, bx, by, BADGE_U, BADGE_V, BADGE_SIZE, BADGE_SIZE, WIDGETS_SIZE, WIDGETS_SIZE);
            String count = pending > 9 ? "9+" : String.valueOf(pending);
            graphics.pose().pushPose();
            graphics.pose().translate(bx + BADGE_SIZE / 2.0F, by + 2, 0.0F);
            graphics.pose().scale(0.75F, 0.75F, 1.0F);
            graphics.drawString(this.font, count, -this.font.width(count) / 2, 0, 0xFFFFFF, false);
            graphics.pose().popPose();
        }

        this.renderNotice(graphics);
    }

    private void renderRows(GuiGraphics graphics, int mx, int my) {
        List<Row> rows = this.rows();
        if (rows.isEmpty()) {
            String key = this.view == View.FRIENDS ? "friends.empty" : "friends.no_requests";
            this.drawCentered(graphics, Component.translatable("gui." + RPGRunestones.MOD_ID + "." + key), LIST_X + LIST_WIDTH / 2, LIST_Y + 60, MUTED_TEXT);
            return;
        }

        for (int i = 0; i < VISIBLE_ROWS && this.scroll + i < rows.size(); i++) {
            Row row = rows.get(this.scroll + i);
            int rx = LIST_X;
            int ry = LIST_Y + 1 + i * ROW_HEIGHT;

            if (this.inside(mx, my, rx, ry, LIST_WIDTH, ROW_HEIGHT)) {
                graphics.fill(this.left + rx + 1, this.top + ry, this.left + rx + LIST_WIDTH - 1, this.top + ry + ROW_HEIGHT, 0x22FFFFFF);
            }

            PlayerFaceRenderer.draw(graphics, this.skinOf(row.info()), this.left + rx + 2, this.top + ry + 2, HEAD_SIZE);

            int nameColor = row.info().online() ? LIGHT_TEXT : OFFLINE_TEXT;
            int nameLimit = (row.type() == RowType.INCOMING ? ACCEPT_OFFSET : REMOVE_OFFSET) - 24;
            String name = this.ellipsize(row.info().name(), nameLimit);
            graphics.drawString(this.font, name, this.left + rx + 22, this.top + ry + 3, nameColor, false);

            Component status = switch (row.type()) {
                case FRIEND -> Component.translatable("gui." + RPGRunestones.MOD_ID + ".friends."
                        + (row.info().online() ? "online" : "offline"));
                case INCOMING -> Component.translatable("gui." + RPGRunestones.MOD_ID + ".friends.incoming");
                case OUTGOING -> Component.translatable("gui." + RPGRunestones.MOD_ID + ".friends.outgoing");
            };
            graphics.pose().pushPose();
            graphics.pose().translate(this.left + rx + 22, this.top + ry + 12, 0.0F);
            graphics.pose().scale(0.75F, 0.75F, 1.0F);
            graphics.drawString(this.font, status, 0, 0, MUTED_TEXT, false);
            graphics.pose().popPose();

            int buttonY = ry + (ROW_HEIGHT - REMOVE.height()) / 2;
            this.drawWidget(graphics, REMOVE, rx + REMOVE_OFFSET, buttonY,
                    this.inside(mx, my, rx + REMOVE_OFFSET, buttonY, REMOVE.width(), REMOVE.height()));
            if (row.type() == RowType.INCOMING) {
                this.drawWidget(graphics, ACCEPT, rx + ACCEPT_OFFSET, buttonY,
                        this.inside(mx, my, rx + ACCEPT_OFFSET, buttonY, ACCEPT.width(), ACCEPT.height()));
            }
        }
    }

    private void renderScrollbar(GuiGraphics graphics, int mx, int my) {
        int max = this.maxScroll();
        if (max <= 0) return;
        int thumbY = TRACK_Y + Math.round((TRACK_HEIGHT - THUMB.height()) * (this.scroll / (float) max));
        boolean hovered = this.draggingThumb || this.inside(mx, my, TRACK_X, thumbY, THUMB.width(), THUMB.height());
        this.drawWidget(graphics, THUMB, TRACK_X, thumbY, hovered);
    }

    private void renderNotice(GuiGraphics graphics) {
        if (this.notice.getString().isEmpty()) return;
        List<FormattedCharSequence> lines = this.font.split(this.notice, (int) (SIDE_WIDTH / 0.75F));
        graphics.pose().pushPose();
        graphics.pose().translate(this.left + SIDE_X, this.top + NOTICE_Y, 0.0F);
        graphics.pose().scale(0.75F, 0.75F, 1.0F);
        for (int i = 0; i < Math.min(8, lines.size()); i++) {
            graphics.drawString(this.font, lines.get(i), 0, i * 10, DARK_TEXT, false);
        }
        graphics.pose().popPose();
    }

    private void drawButton(GuiGraphics graphics, int y, Component label, int mx, int my) {
        boolean hovered = this.inside(mx, my, SIDE_X, y, WIDE.width(), WIDE.height());
        this.drawWidget(graphics, WIDE, SIDE_X, y, hovered);
        this.drawCentered(graphics, label, SIDE_X + WIDE.width() / 2, y + 3, DARK_TEXT);
    }

    private void drawWidget(GuiGraphics graphics, Widget widget, int x, int y, boolean hovered) {
        int u = hovered ? widget.hoverU() : widget.u();
        int v = hovered ? widget.hoverV() : widget.v();
        graphics.blit(WIDGETS, this.left + x - CELL_MARGIN, this.top + y - CELL_MARGIN, u, v,
                widget.width() + CELL_MARGIN * 2, widget.height() + CELL_MARGIN * 2, WIDGETS_SIZE, WIDGETS_SIZE);
    }

    private void drawCentered(GuiGraphics graphics, Component text, int centerX, int y, int color) {
        graphics.drawString(this.font, text, this.left + centerX - this.font.width(text) / 2, this.top + y, color, false);
    }

    private String ellipsize(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) return text;
        return this.font.plainSubstrByWidth(text, maxWidth - this.font.width("...")) + "...";
    }

    private PlayerSkin skinOf(FriendInfo info) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            PlayerInfo playerInfo = connection.getPlayerInfo(info.id());
            if (playerInfo != null) return playerInfo.getSkin();
        }
        return ClientSkinCache.get(info.id());
    }

    private List<Row> rows() {
        FriendsSyncPayload data = ClientFriends.get();
        List<Row> rows = new ArrayList<>();
        if (this.view == View.FRIENDS) {
            data.friends().forEach(info -> rows.add(new Row(info, RowType.FRIEND)));
        } else {
            data.incoming().forEach(info -> rows.add(new Row(info, RowType.INCOMING)));
            data.outgoing().forEach(info -> rows.add(new Row(info, RowType.OUTGOING)));
        }
        return rows;
    }

    private int maxScroll() {
        return Math.max(0, this.rows().size() - VISIBLE_ROWS);
    }

    private boolean inside(int mx, int my, int x, int y, int width, int height) {
        return mx >= x && my >= y && mx < x + width && my < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int mx = Mth.floor(mouseX) - this.left;
            int my = Mth.floor(mouseY) - this.top;

            if (this.inside(mx, my, SIDE_X, SEND_Y, WIDE.width(), WIDE.height())) {
                this.click();
                this.sendRequest();
                return true;
            }
            if (this.inside(mx, my, SIDE_X, TOGGLE_Y, WIDE.width(), WIDE.height())) {
                this.view = this.view == View.FRIENDS ? View.REQUESTS : View.FRIENDS;
                this.scroll = 0;
                this.click();
                return true;
            }
            if (this.inside(mx, my, SIDE_X, BACK_Y, BACK.width(), BACK.height())) {
                this.click();
                this.onClose();
                return true;
            }
            if (this.inside(mx, my, TRACK_X, TRACK_Y, THUMB.width(), TRACK_HEIGHT) && this.maxScroll() > 0) {
                this.draggingThumb = true;
                this.scrollToMouse(my);
                return true;
            }
            if (this.handleRowClick(mx, my)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleRowClick(int mx, int my) {
        List<Row> rows = this.rows();
        for (int i = 0; i < VISIBLE_ROWS && this.scroll + i < rows.size(); i++) {
            Row row = rows.get(this.scroll + i);
            int ry = LIST_Y + 1 + i * ROW_HEIGHT;
            int buttonY = ry + (ROW_HEIGHT - REMOVE.height()) / 2;

            if (this.inside(mx, my, LIST_X + REMOVE_OFFSET, buttonY, REMOVE.width(), REMOVE.height())) {
                FriendActionPayload.Action action = switch (row.type()) {
                    case FRIEND -> FriendActionPayload.Action.REMOVE;
                    case INCOMING -> FriendActionPayload.Action.DECLINE;
                    case OUTGOING -> FriendActionPayload.Action.CANCEL;
                };
                PacketDistributor.sendToServer(FriendActionPayload.of(action, row.info().id()));
                this.click();
                return true;
            }
            if (row.type() == RowType.INCOMING && this.inside(mx, my, LIST_X + ACCEPT_OFFSET, buttonY, ACCEPT.width(), ACCEPT.height())) {
                PacketDistributor.sendToServer(FriendActionPayload.of(FriendActionPayload.Action.ACCEPT, row.info().id()));
                this.click();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingThumb) {
            this.scrollToMouse(Mth.floor(mouseY) - this.top);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingThumb = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            this.scroll = Mth.clamp(this.scroll + (scrollY > 0 ? -1 : 1), 0, this.maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void scrollToMouse(int my) {
        int max = this.maxScroll();
        float progress = (my - TRACK_Y - THUMB.height() / 2.0F) / (float) (TRACK_HEIGHT - THUMB.height());
        this.scroll = Mth.clamp(Math.round(progress * max), 0, max);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.nameField.isFocused() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            this.click();
            this.sendRequest();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void sendRequest() {
        String name = this.nameField.getValue().trim();
        if (name.isEmpty()) return;
        PacketDistributor.sendToServer(new FriendActionPayload(FriendActionPayload.Action.REQUEST, new UUID(0L, 0L), name));
        this.nameField.setValue("");
    }

    private void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
