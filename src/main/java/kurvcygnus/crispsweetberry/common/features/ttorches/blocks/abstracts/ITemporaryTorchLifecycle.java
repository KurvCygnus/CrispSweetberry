package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public interface ITemporaryTorchLifecycle
{
    int getStateLength();
    boolean isStillBright(@NotNull BlockState state);
}
