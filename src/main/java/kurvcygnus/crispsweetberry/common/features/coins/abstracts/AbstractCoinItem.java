package kurvcygnus.crispsweetberry.common.features.coins.abstracts;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import kurvcygnus.crispsweetberry.common.features.coins.CoinType;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This is the basic of coin series.
 * @implNote The reason we keep <u>{@link AbstractCoinItem}</u> and <u>{@link kurvcygnus.crispsweetberry.common.features.coins.GenericCoinItem GenericCoinItem}</u>
 * separated is that we are considering using annotation processors to solve boilerplate problems in future developing.
 * @see kurvcygnus.crispsweetberry.common.features.coins.GenericCoinItem Universal Implementation
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public abstract class AbstractCoinItem extends Item
{
    private CoinType coinType = null;
    private final @NotNull Supplier<CoinType> lazyCoinTypeSupplier = Suppliers.memoize(this::initCoinType);
    
    @SuppressWarnings("unused")//! Only for vanilla CODEC stuff.
    private AbstractCoinItem(@Nullable Properties properties) { this(); }
    
    public AbstractCoinItem() { super(new Properties()); }
    
    protected abstract @NotNull CoinType initCoinType();
    
    public final @NotNull CoinType getCoinType()
    {
        if(this.coinType == null)
        {
            this.coinType = lazyCoinTypeSupplier.get();
            return Objects.requireNonNull(coinType, "Field \"getCoinType\" is initialized as null!");
        }
        
        return this.coinType;
    }
}
