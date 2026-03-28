package kurvmod.freshingsweetberry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Blocks {
    public static DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.createBlocks(FreshingSweetberry.MOD_ID);

    public static Holder<Block> TEMPORARY_TORCH = BLOCK_REGISTER.register("temporary_torch", (resourceLocation) ->
            new Block(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, resourceLocation)).noLootTable()));
}
