package kurvmod.crispsweetberry.blockentities;

import kurvmod.crispsweetberry.CrispSweetberry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CrispBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CrispSweetberry.MOD_ID);
    
    //Nothing here. For now.
}
