package kurvcygnus.crispsweetberry.common.features.coins.abstracts;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public interface ICoinType
{
    @NotNull ResourceLocation getId();
    
    @NotNull AbstractCoinStackBlock stackBlock();
    @NotNull AbstractCoinStackItem stackItem();
    @NotNull AbstractCoinItem coinItem();
    @NotNull Item nuggetItem();
    
    int getExperience();
    float getPenaltyRate();
    
    default boolean shouldRegister() { return true; }
}
