package kurvcygnus.crispsweetberry.datagen;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnBlock;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnRegistries;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public final class CrispBlockstateProvider extends BlockStateProvider
{
    public CrispBlockstateProvider(@NotNull PackOutput output, @NotNull ExistingFileHelper exFileHelper)
        { super(output, CrispSweetberry.NAMESPACE, exFileHelper); }
    
    @Override
    protected void registerStatesAndModels()
    {
        final ModelFile kilnOff = models().orientableWithBottom("kiln",
            modLoc("block/kiln_side"),
            modLoc("block/kiln_front_off"),
            modLoc("block/kiln_bottom"),
            modLoc("block/kiln_top")
        );
        
        final ModelFile kilnOn = models().orientableWithBottom("kiln_on",
            modLoc("block/kiln_side"),
            modLoc("block/kiln_front"),
            modLoc("block/kiln_bottom"),
            modLoc("block/kiln_top")
        );
        
        getVariantBuilder(KilnRegistries.KILN_BLOCK.value()).forAllStates(
            state ->
                {
                    final Direction facing = state.getValue(KilnBlock.FACING);
                    final boolean isLit = state.getValue(KilnBlock.LIT);
                    
                    return ConfiguredModel.builder().
                        modelFile(isLit ? kilnOn : kilnOff).
                        rotationY(((int) facing.toYRot() + 180) % 360).
                        build();
                }
            );
        
        assert KilnRegistries.KILN.getKey() != null;
        
        itemModels().getBuilder(KilnRegistries.KILN.getKey().location().getPath()).
            parent(kilnOn).
                override().
                predicate(modLoc("lit"), 0F).
                model(kilnOff).
            end();
    }
}
