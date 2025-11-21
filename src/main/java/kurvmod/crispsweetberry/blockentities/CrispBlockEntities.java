package kurvmod.crispsweetberry.blockentities;

import kurvmod.crispsweetberry.CrispSweetberry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

//PLACEHOLDER
public class CrispBlockEntities
{
    private CrispBlockEntities() {}
    
    public static final DeferredRegister<BlockEntityType<?>> CRISP_BLOCK_ENTITY_REGISTER =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CrispSweetberry.MOD_ID);
}
