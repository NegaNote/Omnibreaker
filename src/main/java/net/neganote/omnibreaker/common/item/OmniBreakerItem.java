package net.neganote.omnibreaker.common.item;

import net.neganote.omnibreaker.Config;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.world.level.block.Block.getDrops;
import static net.neganote.omnibreaker.Config.ENERGY_PER_USE;
import static net.neganote.omnibreaker.Config.USE_FORGE_ENERGY;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OmniBreakerItem extends Item {

    public OmniBreakerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        IEnergyStorage storage = getEnergyStorage(stack);
        assert storage != null;

        return storage.getEnergyStored() >= ENERGY_PER_USE.get() || !USE_FORGE_ENERGY.get();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        IEnergyStorage storage = getEnergyStorage(stack);
        assert storage != null;

        return storage.getEnergyStored() >= ENERGY_PER_USE.get() || !USE_FORGE_ENERGY.get() ? 100_000f : 0f;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        IEnergyStorage storage = getEnergyStorage(stack);
        assert storage != null;

        var unbreaking = getAllEnchantments(stack).getOrDefault(Enchantments.UNBREAKING, 0);
        double chance = 1.0f / (unbreaking + 1);
        double rand = Math.random();

        if (rand <= chance && USE_FORGE_ENERGY.get()) {
            storage.extractEnergy(ENERGY_PER_USE.get(), false);
        }

        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        BlockPos pos = context.getClickedPos();
        BlockState blockState = level.getBlockState(pos);

        var itemStack = context.getItemInHand();

        float hardness = blockState.getBlock().defaultDestroyTime();
        if (!blockState.canHarvestBlock(level, pos, context.getPlayer()) || hardness < 0.0f) {
            return InteractionResult.PASS;
        }

        int unbreaking = context.getItemInHand().getItem().getAllEnchantments(itemStack)
                .getOrDefault(Enchantments.UNBREAKING, 0);
        double chance = 1.0 / (unbreaking + 1);
        double rand = Math.random();

        IEnergyStorage storage = getEnergyStorage(itemStack);
        assert storage != null;

        if (storage.getEnergyStored() < ENERGY_PER_USE.get() && USE_FORGE_ENERGY.get()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            List<ItemStack> drops = new ObjectArrayList<>(
                    getDrops(blockState, (ServerLevel) level, pos, level.getBlockEntity(pos)));
            var player = context.getPlayer();
            assert player != null;
            level.destroyBlock(pos, false);
            drops.removeIf(player::addItem);
            for (var drop : drops) {
                var center = pos.getCenter();
                var entity = new ItemEntity(level, center.x(), center.y(), center.z(), drop);
                level.addFreshEntity(entity);
            }
            if (rand <= chance && USE_FORGE_ENERGY.get()) {
                storage.extractEnergy(ENERGY_PER_USE.get(), false);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 22;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.UNBREAKING;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        IEnergyStorage storage = getEnergyStorage(stack);
        assert storage != null;

        tooltipComponents.add(Component.translatable("tooltip.omnibreaker.can_break_anything"));
        tooltipComponents.add(Component.translatable("tooltip.omnibreaker.right_click_function"));
        if (USE_FORGE_ENERGY.get()) {
            tooltipComponents.add(Component.translatable("tooltip.omnibreaker.energy", storage.getEnergyStored(),
                    storage.getMaxEnergyStored()).withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return USE_FORGE_ENERGY.get();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        IEnergyStorage storage = getEnergyStorage(stack);
        assert storage != null;
        return Math.round((float) storage.getEnergyStored() * 13.0F / (float) storage.getMaxEnergyStored());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.color(0.0f, 1.0f, 1.0f);
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
