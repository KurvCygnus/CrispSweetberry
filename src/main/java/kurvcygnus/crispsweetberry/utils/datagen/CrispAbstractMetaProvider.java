package kurvcygnus.crispsweetberry.utils.datagen;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class CrispAbstractMetaProvider implements DataProvider
{
    protected final PackOutput.PathProvider pathProvider;
    private final @NotNull String namespace;
    
    protected CrispAbstractMetaProvider(@NotNull PackOutput output, @NotNull String namespace) 
    {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "");
        this.namespace = namespace;
    }
    
    public abstract @NotNull CompletableFuture<?> run(@NotNull CachedOutput output);
    
    protected abstract void addMetas();
    
    public @NotNull String getName() { return "%s: MetaProvider".formatted(this.namespace); }
}
