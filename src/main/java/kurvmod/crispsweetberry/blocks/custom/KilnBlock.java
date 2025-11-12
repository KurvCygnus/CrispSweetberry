package kurvmod.crispsweetberry.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KilnBlock extends AbstractFurnaceBlock
{
    public static final MapCodec<KilnBlock> CODEC = simpleCodec(KilnBlock::new);
    
    public KilnBlock(Properties properties) { super(properties); }
    
    @Override
    protected @NotNull MapCodec<? extends AbstractFurnaceBlock> codec()
    {
        return CODEC;
    }
    
    @Override
    protected void openContainer(Level level, BlockPos pos, Player player)
    {
    
    }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return null;
    }
}
