package net.veroxuniverse.verox_rpg_runestones.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;
import net.veroxuniverse.verox_rpg_runestones.item.RuneTabletItem;
import net.veroxuniverse.verox_rpg_runestones.item.RunestoneItem;
import net.veroxuniverse.verox_rpg_runestones.item.SoulAnchorItem;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RPGRunestones.MOD_ID);

    public static final DeferredItem<RunestoneItem> RUNESTONE = ITEMS.register("runestone",
            () -> new RunestoneItem(ModBlocks.RUNESTONE.get(), new Item.Properties()));

    public static final DeferredItem<Item> RUNE_DUST = ITEMS.registerSimpleItem("rune_dust");

    public static final DeferredItem<RuneTabletItem> RUNE_TABLET = ITEMS.register("rune_tablet",
            () -> new RuneTabletItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<SoulAnchorItem> SOUL_ANCHOR = ITEMS.register("soul_anchor",
            () -> new SoulAnchorItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    private ModItems() {}
}
