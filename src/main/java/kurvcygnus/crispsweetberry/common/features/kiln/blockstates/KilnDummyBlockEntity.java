package kurvcygnus.crispsweetberry.common.features.kiln.blockstates;

import kurvcygnus.crispsweetberry.common.features.kiln.KilnRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This dummy block entity is specially used for 
 * <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnMenu#KilnMenu(int, Inventory) Menu's Client Constructor Method}</u>,
 * as the constructor of <u>{@link KilnBlockEntity Normal One}</u> can't be used due to its args, and the fact that the actual block entity that impacts
 * behavior is defined in the 
 * <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnMenu#KilnMenu(int, Inventory, KilnBlockEntity) Server Constructor}</u>.
 * @see KilnBlockEntity Normal Block Entity
 * @see kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnMenu Menu
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class KilnDummyBlockEntity extends KilnBlockEntity
{
    public KilnDummyBlockEntity() { super(BlockPos.ZERO, KilnRegistries.KILN_BLOCK.value().defaultBlockState()); }
    
    @Override
    public boolean stillValid(@NotNull Player player) { return true; }
}
