package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic;

import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBehavior;
import org.jetbrains.annotations.NotNull;

public final class TemporaryTorchBehavior extends AbstractTemporaryTorchBehavior
{
    public <T extends AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior>> TemporaryTorchBehavior(@NotNull T torchBlock) { super(torchBlock); }
    
    @Override
    protected boolean isRelitable() { return true; }
}
