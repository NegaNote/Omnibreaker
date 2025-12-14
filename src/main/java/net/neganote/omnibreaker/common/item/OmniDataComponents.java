package net.neganote.omnibreaker.common.item;

import net.neganote.omnibreaker.Omnibreaker;
import net.neganote.omnibreaker.common.capabilities.StoredEnergy;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class OmniDataComponents {

    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister
            .createDataComponents(Registries.DATA_COMPONENT_TYPE, Omnibreaker.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StoredEnergy>> STORED_ENERGY = REGISTRAR
            .registerComponentType("stored_energy", builder -> builder
                    .persistent(StoredEnergy.CODEC)
                    .networkSynchronized(StoredEnergy.STREAM_CODEC));
}
