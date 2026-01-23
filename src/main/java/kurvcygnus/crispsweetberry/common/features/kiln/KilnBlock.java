package kurvcygnus.crispsweetberry.common.features.kiln;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnMenu;
import kurvcygnus.crispsweetberry.common.features.kiln.data.KilnContainerData;
import kurvcygnus.crispsweetberry.common.features.kiln.events.KilnRecipeCacheEvent;
import kurvcygnus.crispsweetberry.common.registries.CrispBlockEntities;
import kurvcygnus.crispsweetberry.utils.definitions.SoundConstants;
import kurvcygnus.crispsweetberry.utils.misc.CrispVisualUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static kurvcygnus.crispsweetberry.utils.definitions.ProjectileConstants.*;

//? TODO: Dye variants, water causes LIT property change impl

/**
 * The <b>physically interactable, seen</b> part of the Kiln Block.<br>
 * It mainly holds the <b>state</b>, <b>basic properties</b> and some <b>logical config</b> of an interactable block.
 * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity Functional Part
 * @see KilnContainerData Data Sync Part
 * @see KilnMenu Menu Part
 * @see KilnRecipeCacheEvent Recipe Initialization
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class KilnBlock extends BaseEntityBlock
{
    private static final double SOUND_HORIZONTICAL_OFFSET = 0.5D;
    
    private static final MapCodec<KilnBlock> CODEC = simpleCodec(KilnBlock::new);
    
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    //public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);
    
    /**
     * This constructor method has no exact usage, it is implemented as the super class requires.
     */
    private KilnBlock(@Nullable Properties properties){ this(); }
    
    /**
     * The construct method for <b>block registry</b>.
     */
    public KilnBlock()
    {
        super(BlockBehaviour.Properties.of().
            destroyTime(2.75F).
            requiresCorrectToolForDrops().
            explosionResistance(1.5F).
            sound(SoundType.STONE).
            lightLevel(bs -> bs.getValue(LIT) ? 10 : 0)
        );
        
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, true));
        //this.registerDefaultState(this.stateDefinition.any().setValue(COLOR, DyeColor.ORANGE));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, LIT); }
    
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        return this.defaultBlockState().
            setValue(FACING, context.getHorizontalDirection().getOpposite())//.
            /*setValue(COLOR, )*/;
    }
    
    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
    {
        super.onPlace(state, level, pos, oldState, isMoving);
        //? TODO
    }
    
    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> serverBlockEntityType)
    {
        if(level.isClientSide)//! Tick is handled by server, client shouldn't touch this.
            return null;
        else
            return createTickerHelper(serverBlockEntityType, CrispBlockEntities.KILN_BLOCK_ENTITY.get(), KilnBlockEntity::serverTick);
    }
    
    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    
    @Override @Contract("_, _ -> new")
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) { return new KilnBlockEntity(pos, state); }
    
    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }
    
    @Override
    protected @NotNull InteractionResult useWithoutItem
        (@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult)
            {
                if(level.isClientSide)
                    return InteractionResult.SUCCESS;
                else
                {
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if(blockEntity instanceof KilnBlockEntity kiln)
                        player.openMenu(kiln, pos);
                    
                    return InteractionResult.CONSUME;
                }
            }
    
    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        final double X_POS = (double) pos.getX() + SOUND_HORIZONTICAL_OFFSET;
        final double Y_POS = pos.getY();
        final double Z_POS = (double) pos.getZ() + SOUND_HORIZONTICAL_OFFSET;
        
        if(random.nextDouble() < 0.1)
            level.playLocalSound(X_POS, Y_POS, Z_POS,
                SoundEvents.FURNACE_FIRE_CRACKLE,
                SoundSource.BLOCKS,
                SoundConstants.NORMAL_SOUND_VOLUME,
                SoundConstants.NORMAL_SOUND_PITCH,
                false);
        
        Direction direction = state.getValue(FACING);
        Direction.Axis directionAxis = direction.getAxis();
        double PARTICLE_BASE_RANDOM_OFFSET = random.nextDouble() * 0.6 - 0.3;
        double PARTICLE_X_OFFSET = directionAxis == Direction.Axis.X ? (double) direction.getStepX() * 0.52 : PARTICLE_BASE_RANDOM_OFFSET;
        double PARTICLE_Y_OFFSET = random.nextDouble() * 6.0 / 16.0;
        double PARTICLE_Z_OFFSET = directionAxis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.52 : PARTICLE_BASE_RANDOM_OFFSET;
        
        CrispVisualUtils.addParticles(level,
            X_POS + PARTICLE_X_OFFSET, Y_POS + PARTICLE_Y_OFFSET, Z_POS + PARTICLE_Z_OFFSET, X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED,
            ParticleTypes.SMOKE, ParticleTypes.FLAME);
    }
}
