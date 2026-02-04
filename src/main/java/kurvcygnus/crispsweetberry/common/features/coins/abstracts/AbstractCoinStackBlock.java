package kurvcygnus.crispsweetberry.common.features.coins.abstracts;

import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinStackBlock;
import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This is the basic of stacked coin blocks, which behaves like <u>{@link SnowLayerBlock}</u>, and can be broke to getMarkedLogger 
 * <u>{@link AbstractCoinItem coins}</u>, or <u>{@link VanillaCoinTypes#nuggetItem() nuggets}</u> with <u>{@link ExperienceOrb experiences}</u> on crunching case 
 * in survivor mode.
 *
 * @author Kurv Cygnus
 * @apiNote Loot logic is fully data-driven via <u>{@link kurvcygnus.crispsweetberry.datagen.api.AbstractCoinLootTableProvider Coin LootTables}</u>.
 * Block only handles experience awarding.
 * @implNote The reason we keep <u>{@link AbstractCoinStackBlock}</u> and <u>{@link VanillaCoinStackBlock VanillaCoinStackBlock}</u>
 * separated is that we are considering using annotation processors to solve boilerplate problems in future developing.<br><br>
 * Also, you may have noticed that <u>{@link AbstractCoinStackBlock}</u> doesn't use <u>{@link net.neoforged.neoforge.common.util.Lazy lazy loading}</u>, 
 * that's because <u>{@link Properties#strength}</u> is the only compatible entrance for block strength, which can't be solved by 
 * <u>{@link net.neoforged.neoforge.common.util.Lazy lazy loading}</u>, thus, we compromised.
 * @see VanillaCoinStackBlock Vanilla Implementation
 * @since 1.0 Release
 */
public abstract class AbstractCoinStackBlock<C extends ICoinType<C>> extends SnowLayerBlock
{
    private static final int DROPS_COUNT_PER_LAYER = 9;
    
    private final C coinType;
    
    @SuppressWarnings({"unused", "DataFlowIssue"})//! Only for vanilla CODEC.
    private AbstractCoinStackBlock(@Nullable Properties properties, @Nullable C coinType) { this(coinType); }
    
    public AbstractCoinStackBlock(@NotNull C coinType)//! This is safe since the base class uses lazy to pass value.
    {
        super(Properties.of().
            strength(coinType.getStrength()).//! Oh my god, Minecraft API is terrible.
            sound(initSound()).
            isViewBlocking(getViewBlockingCondition()).
            pushReaction(PushReaction.DESTROY).//* "pushAction" stands for piston pushing.
            noOcclusion()//* This makes sure that the unfilled area is still transparent.
        );
        Objects.requireNonNull(coinType, "Param \"coinType\" must not be null!");
        this.coinType = coinType;
    }
    
    @Override
    public final @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player)
    {
        if(level.isClientSide || player.isCreative() || !player.isShiftKeyDown())
        {
            super.playerWillDestroy(level, pos, state, player);
            return state;
        }
        
        final int layer = state.getValue(SnowLayerBlock.LAYERS);
        
        final int totalExperience = (int) (layer * this.getCoinType().getExperience() * DROPS_COUNT_PER_LAYER * this.getCoinType().getPenaltyRate());
        
        if(totalExperience > 0)
            ExperienceOrb.award((ServerLevel) level, Vec3.atCenterOf(pos), totalExperience);
        
        super.playerWillDestroy(level, pos, state, player);
        return state;
    }
    
    protected static @NotNull BlockBehaviour.StatePredicate getViewBlockingCondition()
        { return (state, level, pos) -> state.getValue(SnowLayerBlock.LAYERS) >= 8; }
    
    protected static SoundType initSound() { return SoundType.DRIPSTONE_BLOCK; }
    
    public final @NotNull C getCoinType() { return this.coinType; }
}
