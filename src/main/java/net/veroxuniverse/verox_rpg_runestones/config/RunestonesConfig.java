package net.veroxuniverse.verox_rpg_runestones.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class RunestonesConfig {

    public enum CostMode { NONE, XP, DUST }

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<CostMode> COST_MODE;
    public static final ModConfigSpec.IntValue BASE_COST;
    public static final ModConfigSpec.IntValue BLOCKS_PER_EXTRA_COST;
    public static final ModConfigSpec.IntValue CROSS_DIMENSION_EXTRA_COST;
    public static final ModConfigSpec.BooleanValue ALLOW_CROSS_DIMENSION;
    public static final ModConfigSpec.BooleanValue PLAYERS_CAN_SET_GLOBAL;
    public static final ModConfigSpec.BooleanValue PROTECT_RUNESTONES;
    public static final ModConfigSpec.IntValue TABLET_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue TABLET_BASE_COST;
    public static final ModConfigSpec.IntValue BUBBLE_CHARGE_TICKS;
    public static final ModConfigSpec.BooleanValue BUBBLE_TAKES_FRIENDS;
    public static final ModConfigSpec.BooleanValue BUBBLE_TAKES_CREATURES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("cost");
        COST_MODE = builder
                .comment("What a teleport costs: NONE (free), XP (experience levels) or DUST (Rune Dust from the inventory).")
                .defineEnum("mode", CostMode.DUST);
        BASE_COST = builder
                .comment("Cost of every teleport, in levels or Rune Dust.")
                .defineInRange("base", 1, 0, 1000);
        BLOCKS_PER_EXTRA_COST = builder
                .comment("Every this many blocks of distance add 1 to the cost. 0 disables distance costs.")
                .defineInRange("blocks_per_extra", 5000, 0, 10000000);
        CROSS_DIMENSION_EXTRA_COST = builder
                .comment("Extra cost when travelling to another dimension, added to the base cost.")
                .defineInRange("cross_dimension_extra", 2, 0, 1000);
        builder.pop();

        builder.push("teleport");
        ALLOW_CROSS_DIMENSION = builder
                .comment("If false, runestones only lead to runestones in the same dimension.")
                .define("allow_cross_dimension", true);
        PLAYERS_CAN_SET_GLOBAL = builder
                .comment("If true, every player can make their own runestones global. Otherwise only creative players and operators can.")
                .define("players_can_set_global", false);
        PROTECT_RUNESTONES = builder
                .comment("If true, only the owner, operators and creative players can break a runestone, and explosions can't destroy it.",
                        "Global runestones can only be broken by operators and creative players - or by their owner when players_can_set_global is true.")
                .define("protect_runestones", true);
        builder.pop();

        builder.push("rune_tablet");
        TABLET_COOLDOWN_SECONDS = builder
                .comment("Seconds a Rune Tablet needs to recharge after a teleport.")
                .defineInRange("cooldown_seconds", 300, 0, 86400);
        TABLET_BASE_COST = builder
                .comment("Base cost of a teleport with the Rune Tablet. Replaces cost.base, distance and dimension costs still apply on top.")
                .defineInRange("base_cost", 3, 0, 1000);
        builder.pop();

        builder.push("bubble");
        BUBBLE_CHARGE_TICKS = builder
                .comment("Ticks the teleport bubble charges before everyone inside travels. 20 ticks = 1 second.")
                .defineInRange("charge_ticks", 50, 0, 400);
        BUBBLE_TAKES_FRIENDS = builder
                .comment("If true, friends inside the bubble travel along.")
                .define("takes_friends", true);
        BUBBLE_TAKES_CREATURES = builder
                .comment("If true, non-hostile creatures inside the bubble travel along - pets, villagers, animals, familiars.")
                .define("takes_creatures", true);
        builder.pop();

        SPEC = builder.build();
    }

    private RunestonesConfig() {}
}