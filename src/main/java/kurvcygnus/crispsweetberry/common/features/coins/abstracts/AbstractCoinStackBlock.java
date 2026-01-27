package kurvcygnus.crispsweetberry.common.features.coins.abstracts;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import kurvcygnus.crispsweetberry.common.features.coins.CoinType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

//? TODO: To lootTable, data driven.

/**
 * This is the basic of stacked coin block series.
 *
 * @author Kurv Cygnus
 * @implNote The reason we keep <u>{@link AbstractCoinStackBlock}</u> and <u>{@link kurvcygnus.crispsweetberry.common.features.coins.GenericCoinStackBlock GenericCoinStackBlock}</u>
 * separated is that we are considering using annotation processors to solve boilerplate problems in future developing.
 * @see kurvcygnus.crispsweetberry.common.features.coins.GenericCoinStackBlock Universal Implementation
 * @since 1.0 Release
 */
public abstract class AbstractCoinStackBlock extends SnowLayerBlock
{
    private static final int DROPS_COUNT_PER_LAYER = 9;
    
    private CoinType coinType = null;
    private final @NotNull Supplier<CoinType> lazyCoinTypeSupplier = Suppliers.memoize(this::initCoinType);
    
    @SuppressWarnings("unused")//! Only for vanilla CODEC.
    private AbstractCoinStackBlock(@Nullable Properties properties) { this(); }
    
    public AbstractCoinStackBlock()
    {
        super(Properties.of().
            strength(0.1F).
            sound(SoundType.DRIPSTONE_BLOCK).
            isViewBlocking(getViewBlockingCondition()).
            pushReaction(PushReaction.DESTROY)//* "pushAction" stands for piston pushing.
        );
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
                    final int totalExperience = layer * this.getCoinType().getExperience() * DROPS_COUNT_PER_LAYER;
                    
                    if(totalExperience > 0)
                        ExperienceOrb.award((ServerLevel) level, Vec3.atCenterOf(pos), totalExperience);
                }
                
                Block.popResource(level, pos, new ItemStack(getDropItem(isCrunchingForExp), DROPS_COUNT_PER_LAYER * layer));
            }
            else
                Block.popResource(level, pos, new ItemStack(this.getCoinType().getStackItemSupplier()));
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
        
        return new ItemStack(this.getCoinType().getCoinItemSupplier(), Math.max(0, count));
    }
    
    protected static @NotNull BlockBehaviour.StatePredicate getViewBlockingCondition()
        { return (state, level, pos) -> state.getValue(SnowLayerBlock.LAYERS) >= 8; }
    
    protected ItemLike getDropItem(boolean isCrunching) { return isCrunching ? this.getCoinType().getNuggetItemSupplier() : this.getCoinType().getCoinItemSupplier(); }
    
    protected abstract @NotNull CoinType initCoinType();
    
    public final @NotNull CoinType getCoinType() 
    {
        if(this.coinType == null)
        {
            this.coinType = this.lazyCoinTypeSupplier.get();
            return Objects.requireNonNull(this.coinType, "Field \"coinType\" is initialized as null!");
        }
        
        return this.coinType;
    }
}
