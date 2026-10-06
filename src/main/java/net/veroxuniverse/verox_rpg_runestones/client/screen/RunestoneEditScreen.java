package net.veroxuniverse.verox_rpg_runestones.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.network.RenameRunestonePayload;
import net.veroxuniverse.verox_rpg_runestones.teleport.RunestoneService;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class RunestoneEditScreen extends Screen {

    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 98;
    private static final int ROW_SPACING = 24;

    private final UUID runestoneId;
    private final String initialName;
    private final boolean initialGlobal;
    private final boolean canSetGlobal;
    private final boolean reopenMenu;
    private boolean global;
    private EditBox nameField;
    private Button modeButton;

    public RunestoneEditScreen(UUID runestoneId, String initialName, boolean global, boolean canSetGlobal, boolean reopenMenu) {
        super(Component.translatable("gui." + RPGRunestones.MOD_ID + (reopenMenu ? ".edit.title" : ".naming.title")));
        this.runestoneId = runestoneId;
        this.initialName = initialName;
        this.initialGlobal = global;
        this.global = global;
        this.canSetGlobal = canSetGlobal;
        this.reopenMenu = reopenMenu;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - FIELD_HEIGHT;

        String value = this.nameField == null ? this.initialName : this.nameField.getValue();
        this.nameField = new EditBox(this.font, centerX - FIELD_WIDTH / 2, y, FIELD_WIDTH, FIELD_HEIGHT, this.title);
        this.nameField.setMaxLength(RunestoneService.MAX_NAME_LENGTH);
        this.nameField.setValue(value);
        this.addRenderableWidget(this.nameField);
        this.setInitialFocus(this.nameField);
        y += ROW_SPACING;

        if (this.canSetGlobal) {
            this.modeButton = this.addRenderableWidget(Button.builder(this.modeLabel(), button -> this.toggleMode())
                    .bounds(centerX - FIELD_WIDTH / 2, y, FIELD_WIDTH, FIELD_HEIGHT)
                    .tooltip(this.modeTooltip())
                    .build());
            y += ROW_SPACING;
        }

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.confirm())
                .bounds(centerX - FIELD_WIDTH / 2, y, BUTTON_WIDTH, FIELD_HEIGHT).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose())
                .bounds(centerX + 2, y, BUTTON_WIDTH, FIELD_HEIGHT).build());
    }

    private void toggleMode() {
        this.global = !this.global;
        this.modeButton.setMessage(this.modeLabel());
        this.modeButton.setTooltip(this.modeTooltip());
    }

    private Component modeLabel() {
        return Component.translatable("gui." + RPGRunestones.MOD_ID + ".edit.mode." + (this.global ? "global" : "discovery"));
    }

    private Tooltip modeTooltip() {
        return Tooltip.create(Component.translatable("gui." + RPGRunestones.MOD_ID + ".edit.mode." + (this.global ? "global" : "discovery") + ".tooltip"));
    }

    private void confirm() {
        String name = this.nameField.getValue().trim();
        boolean changed = !name.equals(this.initialName) || this.global != this.initialGlobal;
        if (!name.isEmpty() && changed) {
            PacketDistributor.sendToServer(new RenameRunestonePayload(this.runestoneId, name, this.global, this.reopenMenu));
        }
        this.onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.confirm();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - FIELD_HEIGHT - 16, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
