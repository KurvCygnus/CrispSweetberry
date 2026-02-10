package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone;

import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBehavior;
import org.jetbrains.annotations.NotNull;

public final class TemporaryRedstoneTorchBehavior extends AbstractTemporaryTorchBehavior
{
    private boolean signalSent = false;
    
    public <T extends AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior>> TemporaryRedstoneTorchBehavior(@NotNull T torchBlock) { super(torchBlock); }
    
    @Override
    protected boolean isRelitable() { return false; }
}
