package kurvmod.crispsweetberry.blocks;

import kurvmod.crispsweetberry.CrispSweetberry;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Blocks {
    public static DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.MOD_ID);

    //aka throwable torch, I changed is since still called that name doesn't make any sense.
    public static Holder<Block> TEMPORARY_TORCH = BLOCK_REGISTER.register("temporary_torch", (resourceLocation) ->
            new Block(BlockBehaviour.Properties.of().noLootTable()));
    
    public static Holder<Block> PAPER_BOX = BLOCK_REGISTER.register("paper_box", (resourceLocation) ->
        new Block(BlockBehaviour.Properties.of()));
    
    public static Holder<Block> KILN = BLOCK_REGISTER.register("kiln", (resourceLocation) ->
        new Block(BlockBehaviour.Properties.of()));
}
