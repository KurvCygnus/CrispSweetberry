package kurvcygnus.crispsweetberry.datagen;

import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackBlock;
import kurvcygnus.crispsweetberry.datagen.api.AbstractCoinLootTableProvider;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static kurvcygnus.crispsweetberry.common.features.coins.CoinRegistries.*;

final class VanillaCoinLootTableProvider extends AbstractCoinLootTableProvider
{
    VanillaCoinLootTableProvider(HolderLookup.Provider registries) { super(registries); }
    
    @Override
    protected @NotNull List<? extends AbstractCoinStackBlock<?>> initCoinBlockLists()
    {
        return List.of(
            COPPER_COIN_STACK_BLOCK.get(),
            IRON_COIN_STACK_BLOCK.get(),
            GOLD_COIN_STACK_BLOCK.get(),
            DIAMOND_COIN_STACK_BLOCK.get()
        );
    }
}
