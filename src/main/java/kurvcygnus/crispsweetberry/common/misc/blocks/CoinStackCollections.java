package kurvcygnus.crispsweetberry.common.misc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static kurvcygnus.crispsweetberry.common.misc.items.CoinCollections.*;
import static kurvcygnus.crispsweetberry.common.registries.CrispItems.*;

/**
 * This is the collection of all vanilla ores' coin stack blocks.
 * @since 1.0 Release
 * @author Kurv
 * @see kurvcygnus.crispsweetberry.common.misc.items.CoinCollections Coin Families
 * @apiNote Please remind that {@code CoinStacks} will never follow {@code loot_table}'s rules, 
 * if you want to edit drops, see <u>{@link AbstractCoinStackBlock#initCorrespondingItem() initCorrespondingItem()}</u>,
 * <u>{@link AbstractCoinStackBlock#initNuggetItem() initNuggetItem()}</u>
 * and <u>{@link AbstractCoinStackBlock#initStackItem() initStackItem()}</u>.
 */
public final class CoinStackCollections
{
    public abstract static class AbstractCoinStackBlock<C extends AbstractCoinItem> extends SnowLayerBlock
    {
        private static final int DROPS_COUNT_PER_LAYER = 9;
        
        private final C coinItem;
        private final ItemStack stackItem;
        private final int experienceStoredPerLayer;
        private final Item nuggetItem;
        
        @SuppressWarnings("unused")//! Only for vanilla CODEC.
        private AbstractCoinStackBlock(@Nullable Properties properties) { this(); }
        
        public AbstractCoinStackBlock()
        {
            super(BlockBehaviour.Properties.of().
                strength(0.1F).
                sound(SoundType.DRIPSTONE_BLOCK).
                isViewBlocking(getViewBlockingCondition()).
                pushReaction(PushReaction.DESTROY)//* "pushAction" stands for piston pushing.
            );
            
            this.coinItem = initCorrespondingItem();
            this.experienceStoredPerLayer = this.coinItem.getStoredExperience();
            this.nuggetItem = initNuggetItem();
            
            if(!this.nuggetItem.getDefaultInstance().is(Tags.Items.NUGGETS))
                throw new IllegalArgumentException("Field \"nuggetItem\" must be registered in tag \"%s\"!".formatted(Tags.Items.NUGGETS.toString()));
            
            this.stackItem = initStackItem().getDefaultInstance();
        }
        
        @Override
        public final @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player)
        {
            final boolean isServer = !level.isClientSide;
            final boolean isCreative = player.isCreative();
            final boolean isCrunchingForExp = player.isShiftKeyDown();
            
            if(isServer && !isCreative)
            {
                final int layer = state.getValue(SnowLayerBlock.LAYERS);
                final boolean hasSilkEnchantStuff = player.getItemInHand(InteractionHand.MAIN_HAND).
                    getEnchantmentLevel(level.registryAccess().
                        lookupOrThrow(Registries.ENCHANTMENT).
                        getOrThrow(Enchantments.SILK_TOUCH)
                    ) > 0;
                
                if(!hasSilkEnchantStuff)
                {
                    if(isCrunchingForExp)
                    {
                        final int totalExperience = layer * this.experienceStoredPerLayer;
                        
                        if(totalExperience > 0)
                            ExperienceOrb.award((ServerLevel) level, Vec3.atCenterOf(pos), totalExperience);
                    }
                    
                    Block.popResource(level, pos, new ItemStack(getDropItem(isCrunchingForExp), DROPS_COUNT_PER_LAYER * layer));
                }
                else
                    Block.popResource(level, pos, stackItem);
            }
            
            super.playerWillDestroy(level, pos, state, player);
            return state;
        }
        
        @Override
        public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder builder) { return Collections.emptyList(); }
        
        @Override
        public void onBlockExploded(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Explosion explosion)
        {
            if(!level.isClientSide)
                Block.popResource(level, pos, getExplodedDrop(state, level));
            
            super.onBlockExploded(state, level, pos, explosion);
        }
        
        protected @NotNull ItemStack getExplodedDrop(@NotNull BlockState state, @NotNull Level level)
        {
            final int count = state.getValue(LAYERS) * (DROPS_COUNT_PER_LAYER - level.getRandom().nextInt(2));
            
            return new ItemStack(this.coinItem, Math.max(0, count));
        }
        
        protected static @NotNull BlockBehaviour.StatePredicate getViewBlockingCondition()
            { return (state, level, pos) -> state.getValue(SnowLayerBlock.LAYERS) >= 8; }
        
        protected ItemLike getDropItem(boolean isCrunching) { return isCrunching ? this.nuggetItem : this.coinItem; }
        
        public final int getExperienceStoredPerLayer() { return this.experienceStoredPerLayer; }
        
        public final @NotNull Item getNuggetItem() { return nuggetItem; }
        
        protected abstract C initCorrespondingItem();
        
        protected abstract Item initNuggetItem();
        
        protected abstract BlockItem initStackItem();
    }
    
    public static final class CopperCoinStackBlock extends AbstractCoinStackBlock<CopperCoinItem>
    {
        @Override
        protected @NotNull CopperCoinItem initCorrespondingItem() { return COPPER_COIN.value(); }
        
        @Override//? TODO
        protected Item initNuggetItem() { return Items.AIR; }
        
        @Override
        protected @NotNull BlockItem initStackItem() { return COPPER_COIN_STACK.value(); }
    }
    
    public static final class IronCoinStackBlock extends AbstractCoinStackBlock<IronCoinItem>
    {
        @Override
        protected @NotNull IronCoinItem initCorrespondingItem() { return IRON_COIN.value(); }
        
        @Override
        protected @NotNull Item initNuggetItem() { return Items.IRON_NUGGET.asItem(); }
        
        @Override
        protected @NotNull BlockItem initStackItem() { return IRON_COIN_STACK.value(); }
    }
    
    public static final class GoldCoinStackBlock extends AbstractCoinStackBlock<GoldCoinItem>
    {
        @Override
        protected @NotNull GoldCoinItem initCorrespondingItem() { return GOLD_COIN.value(); }
        
        @Override
        protected @NotNull Item initNuggetItem() { return Items.GOLD_NUGGET.asItem(); }
        
        @Override
        protected @NotNull BlockItem initStackItem() { return GOLD_COIN_STACK.value(); }
    }
    
    public static final class DiamondCoinStackBlock extends AbstractCoinStackBlock<DiamondCoinItem>
    {
        @Override
        protected @NotNull DiamondCoinItem initCorrespondingItem() { return DIAMOND_COIN.value(); }
        
        @Override//? TODO
        protected Item initNuggetItem() { return Items.AIR; }
        
        @Override
        protected @NotNull BlockItem initStackItem() { return DIAMOND_COIN_STACK.value(); }
    }
}
