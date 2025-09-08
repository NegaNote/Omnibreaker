package net.neganote.omnibreaker.common;

import net.neganote.omnibreaker.Omnibreaker;
import net.neganote.omnibreaker.common.item.OmniBreakerItem;

import net.minecraft.world.item.Rarity;

import com.tterrag.registrate.util.entry.RegistryEntry;

public class OmniItems {

    public static RegistryEntry<OmniBreakerItem> OMNIBREAKER = Omnibreaker.REGISTRATE
            .item("omnibreaker", OmniBreakerItem::new)
            .properties(p -> p.stacksTo(1).durability(0).rarity(Rarity.RARE))
            .model((ctx, prov) -> prov.handheld(ctx)
                    .texture("layer0", Omnibreaker.id("item/omnibreaker")))

            .register();

    public static void init() {}
}
