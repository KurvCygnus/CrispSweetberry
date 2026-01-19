package kurvcygnus.crispsweetberry.common.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnBlock;
import kurvcygnus.crispsweetberry.common.features.paperbox.PaperBoxBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.TemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.TemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.abstracts.ITemporaryTorchBehaviors;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CrispBlocks
{
    private CrispBlocks() { throw new IllegalAccessError(); }
    
    public static final DeferredRegister<Block> CRISP_BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.MOD_ID);
    
    public static final Holder<Block> TEMPORARY_TORCH = CRISP_BLOCK_REGISTER.register("temporary_torch", resourceLocation ->
            new TemporaryTorchBlock(ITemporaryTorchBehaviors.DEFAULT_TEMP_TORCH_PARTICLE));
    
    public static final Holder<Block> TEMPORARY_WALL_TORCH = CRISP_BLOCK_REGISTER.register("temporary_wall_torch", resourceLocation ->
        new TemporaryWallTorchBlock(ITemporaryTorchBehaviors.DEFAULT_TEMP_TORCH_PARTICLE));
    
    public static final Holder<Block> PAPER_BOX = CRISP_BLOCK_REGISTER.register("paper_box", resourceLocation -> new PaperBoxBlock());
    
    public static final Holder<Block> KILN = CRISP_BLOCK_REGISTER.register("kiln", resourceLocation -> new KilnBlock());
}
