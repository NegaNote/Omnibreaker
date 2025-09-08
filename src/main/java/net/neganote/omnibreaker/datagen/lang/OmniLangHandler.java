package net.neganote.omnibreaker.datagen.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class OmniLangHandler {

    public static void init(RegistrateLangProvider provider) {
        provider.add("tooltip.omnibreaker.energy", "%d/%d FE");
        provider.add("tooltip.omnibreaker.can_break_anything", "The Omni-breaker can insta-mine ANYTHING!");
        provider.add("tooltip.omnibreaker.right_click_function", "Break individual blocks with right-click!");
    }
}
