package kurvmod.crispsweetberry.blocks;

import kurvmod.crispsweetberry.CrispSweetberry;
import kurvmod.crispsweetberry.blocks.custom.KilnBlock;
import kurvmod.crispsweetberry.blocks.custom.PaperBoxBlock;
import kurvmod.crispsweetberry.blocks.custom.temporarytorch.TemporaryTorchBlock;
import kurvmod.crispsweetberry.blocks.custom.temporarytorch.TemporaryWallTorchBlock;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CrispBlocks
{
    public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.MOD_ID);
    
    /**
     * The block form of item throwable torch. Its entity form is thrown torch.
     */
    public static Holder<Block> TEMPORARY_TORCH = BLOCK_REGISTER.register("temporary_torch", resourceLocation ->
            new TemporaryTorchBlock());
    
    public static Holder<Block> TEMPORARY_WALL_TORCH = BLOCK_REGISTER.register("temporary_wall_torch", resourceLocation ->
        new TemporaryWallTorchBlock());
    
    public static Holder<Block> PAPER_BOX = BLOCK_REGISTER.register("paper_box", resourceLocation -> new PaperBoxBlock());
    
    public static Holder<Block> KILN = BLOCK_REGISTER.register("kiln", resourceLocation -> new KilnBlock());
}
