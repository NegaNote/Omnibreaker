package net.neganote.omnibreaker;

import net.neganote.omnibreaker.common.OmniItems;
import net.neganote.omnibreaker.common.item.OmniBreakerItem;
import net.neganote.omnibreaker.common.item.OmniDataComponents;
import net.neganote.omnibreaker.datagen.OmniDatagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import com.mojang.logging.LogUtils;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Omnibreaker.MOD_ID)
@ParametersAreNonnullByDefault
public class Omnibreaker {

    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "omnibreaker";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Registrate REGISTRATE = Registrate.create(MOD_ID);

    @SuppressWarnings("unused")
    public static RegistryEntry<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = REGISTRATE.defaultCreativeTab(MOD_ID,
            builder -> builder
                    .title(REGISTRATE.addLang("itemGroup", Omnibreaker.id("creative_tab"), "Omni-breaker"))
                    .icon(() -> new ItemStack(OmniItems.OMNIBREAKER.get()))
                    .displayItems((itemDisplayParameters, output) -> {

                        if (Config.USE_ENERGY.get()) {
                            var tab = REGISTRATE.get("omnibreaker", Registries.CREATIVE_MODE_TAB);
                            ItemStack fullOmnibreaker = new ItemStack(OmniItems.OMNIBREAKER.get());
                            var energyStored = fullOmnibreaker.getCapability(Capabilities.EnergyStorage.ITEM);
                            assert energyStored != null;
                            energyStored.receiveEnergy(energyStored.getMaxEnergyStored(), false);
                            output.accept(fullOmnibreaker);
                        }

                    })
                    .build())
            .register();

    public Omnibreaker(IEventBus modEventBus, ModContainer modContainer) {
        OmniDataComponents.REGISTRAR.register(modEventBus);

        OmniItems.init();
        OmniDatagen.init();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(this::registerCapabilities);

        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that Forge can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "omnibreaker.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("Omni-breaker capacity is {}", Config.CAPACITY.get());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with
    // @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {}
    }

    public static ResourceLocation id(String location) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, location);
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.EnergyStorage.ITEM,
                (ItemStack stack, Void unused) -> new OmniBreakerItem.EnergyStorage(stack),
                OmniItems.OMNIBREAKER.get());
    }
}
