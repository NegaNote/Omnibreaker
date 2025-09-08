package net.neganote.omnibreaker.common.item;

import net.neganote.omnibreaker.Config;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OmniBreakerItem extends Item {

    public OmniBreakerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        IEnergyStorage storage = getEnergyStorage(stack);
        if (storage != null) {
            return 100_000.0f;
        }
        return 0.0f;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
                return ForgeCapabilities.ENERGY.orEmpty(capability, LazyOptional.of(() -> new EnergyStorage(stack)));
            }
        };
    }

    private static @Nullable IEnergyStorage getEnergyStorage(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY).resolve().orElse(null);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        IEnergyStorage storage = getEnergyStorage(stack);
        assert storage != null;
        return Math.round((float) storage.getEnergyStored() * 13.0F / (float) storage.getMaxEnergyStored());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.color(0.3F, 1.0f, 0.3f);
    }

    private record EnergyStorage(ItemStack stack) implements IEnergyStorage {

        private static final String ENERGY_TAG = "energy";

        private static final int CAPACITY = Config.CAPACITY.get();

        @Override
        public int receiveEnergy(int amount, boolean simulated) {
            int stored = getEnergyStored();
            int received = Math.min(amount, CAPACITY - stored);
            int ret = amount - received;

            if (!simulated) {
                stack.getOrCreateTag().putInt(ENERGY_TAG, stored + received);
            }

            return ret;
        }

        @Override
        public int extractEnergy(int amount, boolean simulated) {
            int stored = getEnergyStored();
            int extracted = Math.min(amount, stored);
            int ret = amount - extracted;

            if (!simulated) {
                stack.getOrCreateTag().putInt(ENERGY_TAG, stored - extracted);
            }

            return ret;
        }

        @Override
        public int getEnergyStored() {
            return stack.getOrCreateTag().getInt(ENERGY_TAG);
        }

        @Override
        public int getMaxEnergyStored() {
            return CAPACITY;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
