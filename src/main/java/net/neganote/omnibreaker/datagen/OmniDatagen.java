package net.neganote.omnibreaker.datagen;

import net.neganote.omnibreaker.Omnibreaker;
import net.neganote.omnibreaker.datagen.lang.OmniLangHandler;

import com.tterrag.registrate.providers.ProviderType;

public class OmniDatagen {

    public static void init() {
        Omnibreaker.REGISTRATE.addDataGenerator(ProviderType.LANG, OmniLangHandler::init);
    }
}
