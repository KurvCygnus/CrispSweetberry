package kurvcygnus.crispsweetberry.common.features.coins.datagen;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.coins.CoinRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

/**
 * This is a custom <u>{@link LootItemCondition}</u> for coin's item loot drop logics.
 * @since Release 1.0
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.datagen.api.AbstractCoinLootTableProvider Usage
 */
public record IsCrunchingCondition() implements LootItemCondition
{
    public static final MapCodec<IsCrunchingCondition> CODEC = MapCodec.unit(IsCrunchingCondition::new);
    
    @Override
    public @NotNull LootItemConditionType getType() { return CoinRegistries.IS_CRUNCHING_CONDITION.value(); }
    
    @Override
    public boolean test(@NotNull LootContext lootContext)
    {
        final Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        
        if(entity == null)
            return false;
        
        if(entity instanceof Player player)
            return player.isShiftKeyDown();
        
        return false;
    }
}
