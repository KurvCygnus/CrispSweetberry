package kurvcygnus.crispsweetberry.common.features.coins;

import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public final class GenericCoinStackBlock extends AbstractCoinStackBlock
{
    private final @NotNull Supplier<CoinType> coinTypeSupplier;
    
    public GenericCoinStackBlock(@NotNull Supplier<CoinType> coinTypeSupplier)
    {
        super();
        this.coinTypeSupplier = coinTypeSupplier;
        Objects.requireNonNull(this.coinTypeSupplier, "Field \"coinTypeSupplier\" must not be null!");
    }
    
    @Override
    protected @NotNull CoinType initCoinType() { return coinTypeSupplier.get(); }
}
