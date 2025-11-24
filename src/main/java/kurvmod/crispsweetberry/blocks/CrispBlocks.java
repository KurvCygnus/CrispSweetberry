package kurvmod.crispsweetberry.blocks;

import kurvmod.crispsweetberry.CrispSweetberry;
import kurvmod.crispsweetberry.blocks.custom.KilnBlock;
import kurvmod.crispsweetberry.blocks.custom.PaperBoxBlock;
import kurvmod.crispsweetberry.blocks.custom.temporarytorch.ITemporaryTorch;
import kurvmod.crispsweetberry.blocks.custom.temporarytorch.TemporaryTorchBlock;
import kurvmod.crispsweetberry.blocks.custom.temporarytorch.TemporaryWallTorchBlock;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CrispBlocks
{
    private CrispBlocks() {}
    
    public static final DeferredRegister<Block> CRISP_BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.MOD_ID);

    public static Holder<Block> TEMPORARY_TORCH = CRISP_BLOCK_REGISTER.register("temporary_torch", resourceLocation ->
            new TemporaryTorchBlock(ITemporaryTorch.TEMP_TORCH_DEFAULT_PARTICLE));

    public static Holder<Block> TEMPORARY_WALL_TORCH = CRISP_BLOCK_REGISTER.register("temporary_wall_torch", resourceLocation ->
        new TemporaryWallTorchBlock(ITemporaryTorch.TEMP_TORCH_DEFAULT_PARTICLE));

    public static Holder<Block> PAPER_BOX = CRISP_BLOCK_REGISTER.register("paper_box", resourceLocation -> new PaperBoxBlock());

    public static Holder<Block> KILN = CRISP_BLOCK_REGISTER.register("kiln", resourceLocation -> new KilnBlock());
}
