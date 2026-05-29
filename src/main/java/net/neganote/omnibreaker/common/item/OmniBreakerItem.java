package net.neganote.omnibreaker.common.item;

import net.neganote.omnibreaker.Config;
import net.neganote.omnibreaker.common.capabilities.StoredEnergy;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.world.level.block.Block.getDrops;
import static net.neganote.omnibreaker.Config.ENERGY_PER_USE;
import static net.neganote.omnibreaker.Config.USE_ENERGY;

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

        return storage.getEnergyStored() >= ENERGY_PER_USE.get() || !USE_ENERGY.get();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        IEnergyStorage storage = getEnergyStorage(stack);
        assert storage != null;

        return storage.getEnergyStored() >= ENERGY_PER_USE.get() || !USE_ENERGY.get() ? 100_000f : 0f;
    }

    private int getEnchantLevel(ItemStack stack, Level level, ResourceKey<Enchantment> enchantment) {
        return level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(enchantment)
                .map(holder -> EnchantmentHelper.getTagEnchantmentLevel(holder, stack))
                .orElse(0);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        IEnergyStorage storage = getEnergyStorage(stack);
        assert storage != null;

        var unbreaking = getEnchantLevel(stack, level, Enchantments.UNBREAKING);

        double chance = 1.0f / (unbreaking + 1);
        double rand = Math.random();

        if (rand <= chance && USE_ENERGY.get()) {
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
        assert context.getPlayer() != null;
        if (!blockState.canHarvestBlock(level, pos, context.getPlayer()) || hardness < 0.0f) {
            return InteractionResult.PASS;
        }

        int unbreaking = getEnchantLevel(context.getItemInHand(), level, Enchantments.UNBREAKING);
        double chance = 1.0 / (unbreaking + 1);
        double rand = Math.random();

        IEnergyStorage storage = getEnergyStorage(itemStack);
        assert storage != null;

        if (storage.getEnergyStored() < ENERGY_PER_USE.get() && USE_ENERGY.get()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            List<ItemStack> drops = new ObjectArrayList<>(
                    getDrops(blockState, (ServerLevel) level, pos, level.getBlockEntity(pos), null, itemStack));
            var player = context.getPlayer();
            assert player != null;
            level.destroyBlock(pos, false);
            drops.removeIf(player::addItem);
            for (var drop : drops) {
                var center = pos.getCenter();
                var entity = new ItemEntity(level, center.x(), center.y(), center.z(), drop);
                level.addFreshEntity(entity);
            }
            if (rand <= chance && USE_ENERGY.get()) {
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
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.UNBREAKING) || enchantment.is(Enchantments.SILK_TOUCH);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        var enchantments = book.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchantments == null) {
            return false;
        }
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            if (enchantment.is(Enchantments.UNBREAKING) || enchantment.is(Enchantments.SILK_TOUCH)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag) {
        IEnergyStorage storage = getEnergyStorage(stack);
        assert storage != null;

        tooltipComponents.add(Component.translatable("tooltip.omnibreaker.can_break_anything"));
        tooltipComponents.add(Component.translatable("tooltip.omnibreaker.right_click_function"));
        if (USE_ENERGY.get()) {
            tooltipComponents.add(Component.translatable("tooltip.omnibreaker.energy", storage.getEnergyStored(),
                    storage.getMaxEnergyStored()).withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return USE_ENERGY.get();
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

    private static @Nullable IEnergyStorage getEnergyStorage(ItemStack stack) {
        return stack.getCapability(Capabilities.EnergyStorage.ITEM);
    }

    public record EnergyStorage(ItemStack stack) implements IEnergyStorage {

        private static final int CAPACITY = Config.CAPACITY.get();

        @Override
        public int receiveEnergy(int amount, boolean simulated) {
            int stored = getEnergyStored();
            int received = Math.min(amount, CAPACITY - stored);

            if (!simulated) {
                stack.set(OmniDataComponents.STORED_ENERGY.value(), new StoredEnergy(stored + received));
            }

            return received;
        }

        @Override
        public int extractEnergy(int amount, boolean simulated) {
            int stored = getEnergyStored();
            int extracted = Math.min(amount, stored);

            if (!simulated) {
                stack.set(OmniDataComponents.STORED_ENERGY.value(), new StoredEnergy(stored - extracted));
            }

            return extracted;
        }

        @Override
        public int getEnergyStored() {
            var storedEnergy = stack.get(OmniDataComponents.STORED_ENERGY.value());
            return storedEnergy == null ? 0 : storedEnergy.energy();
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
