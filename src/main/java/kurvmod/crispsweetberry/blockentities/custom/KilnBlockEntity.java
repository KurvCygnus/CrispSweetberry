package kurvmod.crispsweetberry.blockentities.custom;

import kurvmod.crispsweetberry.blockentities.CrispBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

//TODO: FurnaceBlockEntity -> BlockEntity
//WIP
//DOC
public class KilnBlockEntity extends AbstractFurnaceBlockEntity implements MenuProvider
{
    public KilnBlockEntity(BlockPos pos, BlockState blockState) { super(CrispBlockEntities.KILN_BLOCK_ENTITY.get(), pos, blockState, RecipeType.SMELTING); }
    
    @Override
    protected @NotNull Component getDefaultName() { return Component.translatable("container.kiln"); }
    
    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory)
        { return new FurnaceMenu(containerId, inventory, this, this.dataAccess); }
}
