package kurvmod.crispsweetberry.datagen;

import kurvmod.crispsweetberry.CrispSweetberry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class CrispBlockstateProvider extends BlockStateProvider
{
    public CrispBlockstateProvider(PackOutput output, ExistingFileHelper exFileHelper)
    {
        super(output, CrispSweetberry.MOD_ID, exFileHelper);
    }
    
    @Override
    protected void registerStatesAndModels()
    {
    
    }
}
