package net.neganote.omnibreaker.common.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import org.jetbrains.annotations.Nullable;

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
        var storage = stack.getCapability(ForgeCapabilities.ENERGY).resolve();
        if (storage.isPresent()) {
            return 100_000.0f;
        }
        return 0.0f;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
                if (capability == ForgeCapabilities.ENERGY) {
                    return LazyOptional.of(() -> new EnergyStorage(stack)).cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    private record EnergyStorage(ItemStack stack) implements IEnergyStorage {

        private static final String ENERGY_TAG = "energy";

        private static final int CAPACITY = 4_000_000;

        @Override
        public int receiveEnergy(int amount, boolean simulated) {
            stack.getOrCreateTag();
            return 0;
        }

        @Override
        public int extractEnergy(int amount, boolean simulated) {
            return 0;
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
