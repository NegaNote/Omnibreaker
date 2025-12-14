package net.neganote.omnibreaker.common;

import net.neganote.omnibreaker.Omnibreaker;
import net.neganote.omnibreaker.common.capabilities.StoredEnergy;
import net.neganote.omnibreaker.common.item.OmniBreakerItem;
import net.neganote.omnibreaker.common.item.OmniDataComponents;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import com.tterrag.registrate.util.entry.RegistryEntry;

public class OmniItems {

    public static RegistryEntry<Item, OmniBreakerItem> OMNIBREAKER = Omnibreaker.REGISTRATE
            .item("omnibreaker", OmniBreakerItem::new)
            .properties(p -> p.stacksTo(1).durability(0).rarity(Rarity.RARE)
                    .component(OmniDataComponents.STORED_ENERGY.get(), new StoredEnergy(0)))
            .model((ctx, prov) -> prov.handheld(ctx)
                    .texture("layer0", Omnibreaker.id("item/omnibreaker")))
            .lang("Omni-breaker")
            .register();

    public static void init() {}
}
