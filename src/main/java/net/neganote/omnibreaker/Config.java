package net.neganote.omnibreaker;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue USE_ENERGY = BUILDER
            .comment("Whether the Omni-breaker needs energy to be used.",
                    "(Will make the other configs do nothing if false.)")
            .define("use_energy", true);

    public static final ModConfigSpec.IntValue CAPACITY = BUILDER
            .comment("Amount of FE the Omni-breaker can store.")
            .defineInRange("capacity", 50_000_000, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ENERGY_PER_USE = BUILDER
            .comment("Amount of FE used every time the Omni-breaker breaks a block.")
            .defineInRange("energy_per_use", 10_000, 0, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();
}
