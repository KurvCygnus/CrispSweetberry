package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractTemporaryTorchBlock<T extends AbstractTemporaryTorchBehavior> extends AbstractGenericTorchBlock<T>
{
    public AbstractTemporaryTorchBlock(@NotNull Properties properties, @NotNull T behavior) { super(properties, behavior, false); }
}
