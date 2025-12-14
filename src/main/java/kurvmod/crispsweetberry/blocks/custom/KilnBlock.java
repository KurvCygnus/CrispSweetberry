package kurvmod.crispsweetberry.blocks.custom;

import com.mojang.serialization.MapCodec;
import kurvmod.crispsweetberry.blockentities.CrispBlockEntities;
import kurvmod.crispsweetberry.blockentities.custom.KilnBlockEntity;
import kurvmod.crispsweetberry.misc.CrispStats;
import kurvmod.crispsweetberry.utils.CrispUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static kurvmod.crispsweetberry.utils.CrispConstants.ProjectileConstants.*;
import static kurvmod.crispsweetberry.utils.CrispConstants.SoundConstants;

//WIP
//DOC
//TODO
public class KilnBlock extends AbstractFurnaceBlock
{
    public static final MapCodec<KilnBlock> CODEC = simpleCodec(KilnBlock::new);
    public static final double SOUND_HORIZONTICAL_OFFSET = 0.5;
    
    /**
     * A <b>placeholder construct method, it has to be implemented for its superclass demands</b>.
     */
    public KilnBlock(Properties properties) { super(properties); }
    
    /**
     * This is the actual construct method for <b>block registry</b>.
     */
    public KilnBlock()
    {
        //PLACEHOLDER, kinda of
        super(BlockBehaviour.Properties.of().
            destroyTime(2.75F).
            requiresCorrectToolForDrops().
            explosionResistance(1.5F).
            sound(SoundType.STONE).
            lightLevel(bs -> 6)
        );
    }
    
    @Override
    protected @NotNull MapCodec<? extends AbstractFurnaceBlock> codec() { return CODEC; }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) { return new KilnBlockEntity(pos, state); }
    
    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType)
        { return createFurnaceTicker(level, blockEntityType, CrispBlockEntities.KILN_BLOCK_ENTITY.get()); }
    
    @Override
    protected void openContainer(Level level, @NotNull BlockPos pos, @NotNull Player player)
    {
        BlockEntity blockentity = level.getBlockEntity(pos);
        
        if(!(blockentity instanceof KilnBlockEntity))
            return;
        
        player.openMenu((MenuProvider) blockentity);
        player.awardStat(CrispStats.INTERACT_WITH_KILN);
    }
    
    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, BlockPos pos, RandomSource random)
    {
        double X_POS = (double) pos.getX() + SOUND_HORIZONTICAL_OFFSET;
        double Y_POS = pos.getY();
        double Z_POS = (double) pos.getZ() + SOUND_HORIZONTICAL_OFFSET;
        
        if(random.nextDouble() < 0.1)
            level.playLocalSound(X_POS, Y_POS, Z_POS,
                SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, SoundConstants.NORMAL_SOUND_VOLUME, SoundConstants.NORMAL_SOUND_PITCH,
                false);
        
        Direction direction = state.getValue(FACING);
        Direction.Axis direction$axis = direction.getAxis();
        double PARTICLE_BASE_RANDOM_OFFSET = random.nextDouble() * 0.6 - 0.3;
        double PARTICLE_X_OFFSET = direction$axis == Direction.Axis.X ? (double) direction.getStepX() * 0.52 : PARTICLE_BASE_RANDOM_OFFSET;
        double PARTICLE_Y_OFFSET = random.nextDouble() * 6.0 / 16.0;
        double PARTICLE_Z_OFFSET = direction$axis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.52 : PARTICLE_BASE_RANDOM_OFFSET;
        
        CrispUtils.addParticles(level,
            X_POS + PARTICLE_X_OFFSET, Y_POS + PARTICLE_Y_OFFSET, Z_POS + PARTICLE_Z_OFFSET, X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED,
            ParticleTypes.SMOKE, ParticleTypes.FLAME);
    }
}