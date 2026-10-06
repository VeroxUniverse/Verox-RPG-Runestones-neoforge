package net.veroxuniverse.verox_rpg_runestones.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RPGRunestones.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + RPGRunestones.MOD_ID))
                    .icon(() -> new ItemStack(ModItems.RUNESTONE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.RUNESTONE.get());
                        output.accept(ModItems.RUNE_DUST.get());
                        output.accept(ModItems.RUNE_TABLET.get());
                        output.accept(ModItems.SOUL_ANCHOR.get());
                    })
                    .build());

    private ModCreativeTabs() {}
}
