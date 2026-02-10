package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface ITemporaryTorchLifecycle
{
    @Range(from = 0, to = Integer.MAX_VALUE) int getStateLength();
    boolean isStillBright(@NotNull BlockState state);
}
