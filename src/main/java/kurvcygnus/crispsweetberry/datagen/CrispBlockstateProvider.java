package kurvcygnus.crispsweetberry.datagen;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public final class CrispBlockstateProvider extends BlockStateProvider
{
    public CrispBlockstateProvider(@NotNull PackOutput output, @NotNull ExistingFileHelper exFileHelper)
    {
        super(output, CrispSweetberry.MOD_ID, exFileHelper);
    }
    
    @Override
    protected void registerStatesAndModels()
    {
        
    }
}
