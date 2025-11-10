package kurvmod.crispsweetberry.blocks;

import kurvmod.crispsweetberry.CrispSweetberry;
import kurvmod.crispsweetberry.blocks.custom.KilnBlock;
import kurvmod.crispsweetberry.blocks.custom.PaperBoxBlock;
import kurvmod.crispsweetberry.blocks.custom.TemporaryTorchBlock;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

//TODO
// 1.完成投掷火把的墙形态
// 2.完成投掷火把
// 3.完成窑炉
// 4.没想好
public class CrispBlocks
{
    public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.MOD_ID);
    
    /**
     * The block form of item throwable torch. Its entity form is thrown torch.
     */
    public static Holder<Block> TEMPORARY_TORCH = BLOCK_REGISTER.register("temporary_torch", resourceLocation ->
            new TemporaryTorchBlock(ParticleTypes.SMALL_FLAME, BlockBehaviour.Properties.of().
                noLootTable().
                sound(SoundType.WOOD).
                ignitedByLava().
                lightLevel(state -> 12).
                noCollission().
                instabreak())
    );
    
    public static Holder<Block> PAPER_BOX = BLOCK_REGISTER.register("paper_box", resourceLocation ->
        new PaperBoxBlock(BlockBehaviour.Properties.of().
            destroyTime(0.1F).
            explosionResistance(0.1F).
            ignitedByLava().
            sound(SoundType.SCAFFOLDING))
    );
    
    public static Holder<Block> KILN = BLOCK_REGISTER.register("kiln", resourceLocation ->
        new KilnBlock(BlockBehaviour.Properties.of().
            destroyTime(2.75F).
            requiresCorrectToolForDrops().
            explosionResistance(1.5F).
            sound(SoundType.STONE).
            lightLevel(state -> 10))
    );
}
