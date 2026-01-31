package kurvcygnus.crispsweetberry.common.features.coins.vanilla;

import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Kurv Cygnus
 * @apiNote Reference implementation for vanilla coin types.
 * <p>
 * This class exists mainly as a bridge implementation and
 * may be replaced by generated code in future versions.
 * @since 1.0 Release
 */
//@Deprecated(since = "1.1.0 Release", forRemoval = false)
public final class VanillaCoinStackBlock extends AbstractCoinStackBlock<VanillaCoinTypes>
{
    public VanillaCoinStackBlock(@NotNull Supplier<VanillaCoinTypes> coinTypeSupplier)
    {
        super(Objects.requireNonNull(coinTypeSupplier.get(), "Field \"coinTypeSupplier\" must not be null!"));
    }
}
