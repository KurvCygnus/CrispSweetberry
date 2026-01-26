package kurvcygnus.crispsweetberry.common.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateBlock;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.TemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.TemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.abstracts.ITemporaryTorchBehaviors;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

import static kurvcygnus.crispsweetberry.common.misc.blocks.CoinStackCollections.*;


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
    
    public static final Holder<Block> COPPER_COIN_STACK = CRISP_BLOCK_REGISTER.register("copper_coin_stack", resourceLocation ->
        new CopperCoinStackBlock());
    
    public static final Holder<Block> IRON_COIN_STACK = CRISP_BLOCK_REGISTER.register("iron_coin_stack", resourceLocation ->
        new IronCoinStackBlock());
    
    public static final Holder<Block> GOLD_COIN_STACK = CRISP_BLOCK_REGISTER.register("gold_coin_stack", resourceLocation ->
        new GoldCoinStackBlock());
    
    public static final Holder<Block> DIAMOND_COIN_STACK = CRISP_BLOCK_REGISTER.register("diamond_coin_stack", resourceLocation ->
        new DiamondCoinStackBlock());
}
