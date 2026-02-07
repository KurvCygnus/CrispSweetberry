package kurvcygnus.crispsweetberry.common.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.TemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.TemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.ITemporaryTorchBehaviors;
import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public enum CrispBlocks implements IRegistrant
{
    INSTANCE;
    
    @Override public void register(@NotNull IEventBus bus) { CRISP_BLOCK_REGISTER.register(bus); }
    
    @Override
    public boolean isFeature() { return false; }
    
    @Override
    public @NotNull String getJob() { return "Misc Blocks"; }
    
    @Override
    public int getPriority() { return 0; }
    
    public static final DeferredRegister<Block> CRISP_BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.NAMESPACE);
    
    @AutoI18n({
        "en_us -> Thrown Torch",
        "lol_us -> fullee lite stik",
        "zh_cn -> 投掷火把"
    })
    public static final Holder<Block> TEMPORARY_TORCH = CRISP_BLOCK_REGISTER.register("temporary_torch", resourceLocation ->
            new TemporaryTorchBlock(ITemporaryTorchBehaviors.DEFAULT_TEMP_TORCH_PARTICLE));
    
    @AutoI18n({
        "en_us -> Thrown Torch",
        "lol_us -> fullee lite stik",
        "zh_cn -> 投掷火把"
    })
    public static final Holder<Block> TEMPORARY_WALL_TORCH = CRISP_BLOCK_REGISTER.register("temporary_wall_torch", resourceLocation ->
        new TemporaryWallTorchBlock(ITemporaryTorchBehaviors.DEFAULT_TEMP_TORCH_PARTICLE));
    
    @AutoI18n({
        "en_us -> Carry Crate",
        "lol_us -> hoom",
        "zh_cn -> 搬运箱"
    })
    public static final Holder<Block> CARRY_CRATE = CRISP_BLOCK_REGISTER.register("carry_crate", resourceLocation -> new CarryCrateBlock());
}
