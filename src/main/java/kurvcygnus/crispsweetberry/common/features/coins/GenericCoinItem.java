package kurvcygnus.crispsweetberry.common.features.coins;

import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public final class GenericCoinItem extends AbstractCoinItem
{
    private final @NotNull Supplier<CoinType> coinTypeSupplier;
    
    public GenericCoinItem(@NotNull Supplier<CoinType> coinTypeSupplier)
    {
        super();
        this.coinTypeSupplier = coinTypeSupplier;
        Objects.requireNonNull(coinTypeSupplier, "Field \"coinTypeSupplier\" must not be null!");
    }
    
    @Override
    protected @NotNull CoinType initCoinType() { return coinTypeSupplier.get(); }
}
