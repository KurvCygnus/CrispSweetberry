package kurvcygnus.crispsweetberry.common.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateBlock;
import kurvcygnus.crispsweetberry.common.features.coins.CoinType;
import kurvcygnus.crispsweetberry.common.features.coins.GenericCoinStackBlock;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.TemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.TemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.abstracts.ITemporaryTorchBehaviors;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public final class CrispBlocks
{
    private CrispBlocks() { throw new IllegalAccessError(); }
    
    public static final DeferredRegister<Block> CRISP_BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.MOD_ID);
    
    public static final Holder<Block> TEMPORARY_TORCH = CRISP_BLOCK_REGISTER.register("temporary_torch", resourceLocation ->
            new TemporaryTorchBlock(ITemporaryTorchBehaviors.DEFAULT_TEMP_TORCH_PARTICLE));
    
    public static final Holder<Block> TEMPORARY_WALL_TORCH = CRISP_BLOCK_REGISTER.register("temporary_wall_torch", resourceLocation ->
        new TemporaryWallTorchBlock(ITemporaryTorchBehaviors.DEFAULT_TEMP_TORCH_PARTICLE));
    
    public static final Holder<Block> CARRY_CRATE = CRISP_BLOCK_REGISTER.register("carry_crate", resourceLocation -> new CarryCrateBlock());
    
    public static final Holder<Block> KILN = CRISP_BLOCK_REGISTER.register("kiln", resourceLocation -> new KilnBlock());
    
    public static final DeferredHolder<Block, GenericCoinStackBlock> COPPER_COIN_STACK = CRISP_BLOCK_REGISTER.register("copper_coin_stack", resourceLocation ->
        new GenericCoinStackBlock(() -> CoinType.COPPER));
    
    public static final DeferredHolder<Block, GenericCoinStackBlock> IRON_COIN_STACK = CRISP_BLOCK_REGISTER.register("iron_coin_stack", resourceLocation ->
        new GenericCoinStackBlock(() -> CoinType.IRON));
    
    public static final DeferredHolder<Block, GenericCoinStackBlock> GOLD_COIN_STACK = CRISP_BLOCK_REGISTER.register("gold_coin_stack", resourceLocation ->
        new GenericCoinStackBlock(() -> CoinType.GOLD));
    
    public static final DeferredHolder<Block, GenericCoinStackBlock> DIAMOND_COIN_STACK = CRISP_BLOCK_REGISTER.register("diamond_coin_stack", resourceLocation ->
        new GenericCoinStackBlock(() -> CoinType.DIAMOND));
}
