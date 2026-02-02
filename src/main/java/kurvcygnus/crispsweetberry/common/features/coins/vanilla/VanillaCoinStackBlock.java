package kurvcygnus.crispsweetberry.common.features.coins.vanilla;

import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackBlock;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
    public VanillaCoinStackBlock(@NotNull Lazy<VanillaCoinTypes> lazyCoinTypeSupplier)
    {
        super(Objects.requireNonNull(lazyCoinTypeSupplier.get(), "Field \"coinTypeSupplier\" must not be null!"));
    }
}
