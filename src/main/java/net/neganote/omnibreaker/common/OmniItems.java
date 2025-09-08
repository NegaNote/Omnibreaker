package net.neganote.omnibreaker.common;

import net.neganote.omnibreaker.Omnibreaker;
import net.neganote.omnibreaker.common.item.OmniBreakerItem;

import com.tterrag.registrate.util.entry.RegistryEntry;

public class OmniItems {

    public static RegistryEntry<OmniBreakerItem> OMNIBREAKER = Omnibreaker.REGISTRATE
            .item("omnibreaker", OmniBreakerItem::new)
            .register();

    public static void init() {}
}
