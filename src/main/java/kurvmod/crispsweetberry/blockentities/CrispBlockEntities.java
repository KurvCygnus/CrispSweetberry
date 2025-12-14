package kurvmod.crispsweetberry.blockentities;

import kurvmod.crispsweetberry.CrispSweetberry;
import kurvmod.crispsweetberry.blockentities.custom.KilnBlockEntity;
import kurvmod.crispsweetberry.blocks.CrispBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class CrispBlockEntities
{
    private CrispBlockEntities() {}
    
    public static final DeferredRegister<BlockEntityType<?>> CRISP_BLOCK_ENTITY_REGISTER =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CrispSweetberry.MOD_ID);
    
    @SuppressWarnings("ConstantConditions")//* https://docs.neoforged.net/docs/1.21.1/blockentities/ You can find the reason of suppression here.
    public static final Supplier<BlockEntityType<KilnBlockEntity>> KILN_BLOCK_ENTITY = CRISP_BLOCK_ENTITY_REGISTER.register("kiln_block_entity", () ->
        BlockEntityType.Builder.of(
            KilnBlockEntity::new,
            CrispBlocks.KILN.value()
        ).
        build(null)//* Build using null; vanilla does some datafixer with the parameter that we don't need.
    );
}
