package net.veroxuniverse.verox_rpg_runestones.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

public final class CuriosCompat {

    private CuriosCompat() {}

    public static boolean isEquipped(LivingEntity entity, Item item) {
        if (!ModList.get().isLoaded("curios")) return false;
        return CuriosApi.getCuriosInventory(entity).map(inventory -> inventory.isEquipped(item)).orElse(false);
    }
}
