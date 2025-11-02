package kurvmod.crispsweetberry.blocks;

import kurvmod.crispsweetberry.CrispSweetberry;
import kurvmod.crispsweetberry.blocks.custom.PaperBoxBlock;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Blocks {
    public static DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.MOD_ID);

    //aka throwable torch, I changed is since still called that name doesn't make any sense.
    public static Holder<Block> TEMPORARY_TORCH = BLOCK_REGISTER.register("temporary_torch", resourceLocation ->
            new Block(BlockBehaviour.Properties.of().
                noLootTable().
                sound(SoundType.WOOD).
                noCollission().
                ignitedByLava().
                lightLevel(state -> 8))
    );
    
    public static Holder<Block> PAPER_BOX = BLOCK_REGISTER.register("paper_box", resourceLocation ->
        new PaperBoxBlock(BlockBehaviour.Properties.of().
            destroyTime(0.1F).
            explosionResistance(0.1F).
            sound(SoundType.SCAFFOLDING))
    );
    
    public static Holder<Block> KILN = BLOCK_REGISTER.register("kiln", resourceLocation ->
        new Block(BlockBehaviour.Properties.of().
            destroyTime(2.75F).
            requiresCorrectToolForDrops().
            explosionResistance(1.5F).
            sound(SoundType.STONE).
            lightLevel(state -> 10))
    );
}
