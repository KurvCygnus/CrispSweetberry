package kurvmod.crispsweetberry.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import org.jetbrains.annotations.NotNull;

public class KilnBlock extends FurnaceBlock
{
    public KilnBlock(Properties properties) { super(properties); }
    
    @Override
    protected void openContainer(@NotNull Level level, @NotNull BlockPos pos, @NotNull Player player)
    {
        super.openContainer(level, pos, player);
    }
}
