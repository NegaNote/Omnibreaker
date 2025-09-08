package net.neganote.omnibreaker;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Omnibreaker.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue CAPACITY = BUILDER
            .comment("Amount of FE the Omni-breaker can store.")
            .defineInRange("capacity", 50_000_000, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.IntValue ENERGY_PER_USE = BUILDER
            .comment("Amount of FE used every time the Omni-breaker breaks a block.")
            .defineInRange("energyPerUse", 10_000, 0, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();
}
