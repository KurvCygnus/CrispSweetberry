package kurvcygnus.crispsweetberry.common.features.coins.abstracts;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import kurvcygnus.crispsweetberry.common.features.coins.CoinType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This is the basic of stacked coin items.
 *
 * @author Kurv Cygnus
 * @implNote The reason we keep <u>{@link AbstractCoinStackItem}</u> and <u>{@link kurvcygnus.crispsweetberry.common.features.coins.GenericCoinStackItem GenericCoinStackItem}</u>
 * separated is that we are considering using annotation processors to solve boilerplate problems in future developing.
 * @see kurvcygnus.crispsweetberry.common.features.coins.GenericCoinStackItem Universal Implementation
 * @since 1.0 Release
 */
public abstract class AbstractCoinStackItem extends BlockItem
{
    private CoinType coinType = null;
    private final @NotNull Supplier<CoinType> lazyCoinTypeSupplier = Suppliers.memoize(this::initCoinType);
    
    @SuppressWarnings("unused")//! Only for vanilla CODEC.
    private AbstractCoinStackItem(@Nullable Block block, @Nullable Properties properties) { this(); }
    
    @SuppressWarnings("DataFlowIssue")//! To avoid footgun, we use lazy load with overrides.
    protected AbstractCoinStackItem() { super(null, new Properties().stacksTo(16)); }
    
    
    /**
     * @implNote  This method is used by <u>{@link BlockItem}</u> to get the instance of block.<br>
     * We override this to implement lazy loading block to avoid footgun.
     */
    @Override
    public @NotNull Block getBlock() { return this.getCoinType().getBlockSupplier(); }
    
    protected abstract @NotNull CoinType initCoinType();
    
    public final @NotNull CoinType getCoinType() 
    {
        if(this.coinType == null)
        {
            this.coinType = lazyCoinTypeSupplier.get();
            return Objects.requireNonNull(coinType, "Field \"coinType\" was initialized as null!");
        }
        return this.coinType;
    }
}
