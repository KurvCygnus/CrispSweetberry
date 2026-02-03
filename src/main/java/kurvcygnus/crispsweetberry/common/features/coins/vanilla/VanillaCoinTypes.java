package kurvcygnus.crispsweetberry.common.features.coins.vanilla;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.*;
import kurvcygnus.crispsweetberry.common.features.coins.events.NuggetItemCheckEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static kurvcygnus.crispsweetberry.common.features.coins.CoinRegistries.*;
import static net.minecraft.world.item.Items.GOLD_NUGGET;
import static net.minecraft.world.item.Items.IRON_NUGGET;

/**
 * Enum-like coin type definitions for vanilla ores.<br>
 * We intentionally do NOT use {@link Enum} here to allow <u>{@link net.neoforged.neoforge.common.util.Lazy lazy suppliers}</u> and more flexible registration behavior.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see ICoinType Contract Interface
 * @see BaseCoinType Basic Implementation
 */
public final class VanillaCoinTypes extends BaseCoinType<VanillaCoinTypes>
{
    private final boolean isOptional;
    
    private VanillaCoinTypes(@NotNull String id, @NotNull Supplier<? extends AbstractCoinStackBlock<VanillaCoinTypes>> blockSupplier,
        @NotNull Supplier<? extends AbstractCoinStackItem<VanillaCoinTypes>> stackSupplier, @NotNull Supplier<? extends AbstractCoinItem<VanillaCoinTypes>> coinSupplier,
        @NotNull Supplier<? extends Item> nuggetSupplier, int experience, float penaltyRate, float strength, boolean isOptional)
            {
                super(id, blockSupplier, stackSupplier, coinSupplier, nuggetSupplier, experience, penaltyRate, strength);
                this.isOptional = isOptional;
            }
    
    private VanillaCoinTypes(@NotNull String id, @NotNull Supplier<? extends AbstractCoinStackBlock<VanillaCoinTypes>> blockSupplier,
        @NotNull Supplier<? extends AbstractCoinStackItem<VanillaCoinTypes>> stackSupplier, @NotNull Supplier<? extends AbstractCoinItem<VanillaCoinTypes>> coinSupplier,
        @NotNull Supplier<? extends Item> nuggetSupplier, int experience, float penaltyRate, float strength)
            {
                super(id, blockSupplier, stackSupplier, coinSupplier, nuggetSupplier, experience, penaltyRate, strength);
                this.isOptional = false;
            }
    
    public static final Predicate<Supplier<? extends Item>> DEFAULT_CONDITION = Objects::nonNull;
    public static final Predicate<Supplier<? extends Item>> OPTIONAL = (supplier) -> supplier.get() != Items.AIR;
    
    public static final VanillaCoinTypes COPPER = new VanillaCoinTypes(
        "copper", COPPER_COIN_STACK_BLOCK, COPPER_COIN_STACK, COPPER_COIN,
        NuggetItemCheckEvent.copperNuggetSupplier, 1, 0.7F, 0.5F, true
    );
    
    public static final VanillaCoinTypes IRON = new VanillaCoinTypes(
        "iron", IRON_COIN_STACK_BLOCK, IRON_COIN_STACK, IRON_COIN,
        IRON_NUGGET::asItem, 3, 0.8F, 1.0F
    );
    
    public static final VanillaCoinTypes GOLD = new VanillaCoinTypes(
        "gold", GOLD_COIN_STACK_BLOCK, GOLD_COIN_STACK, GOLD_COIN,
        GOLD_NUGGET::asItem, 7, 0.85F, 0.8F
    );
    
    public static final VanillaCoinTypes DIAMOND = new VanillaCoinTypes(
        "diamond", DIAMOND_COIN_STACK_BLOCK, DIAMOND_COIN_STACK, DIAMOND_COIN,
        NuggetItemCheckEvent.diamondNuggetSupplier, 10, 0.9F, 1.5F
    );
    
    public static final VanillaCoinTypes[] VALUES = {COPPER, IRON, GOLD, DIAMOND};
    
    @Override protected @NotNull String initNamespace() { return CrispSweetberry.ID; }
    
    protected @NotNull Predicate<Supplier<? extends Item>> initEnableCondition() { return this.isOptional ? DEFAULT_CONDITION : OPTIONAL; }
    
    @Override public @NotNull String toString() { return "VanillaCoinTypes[id=%s, experience=%d, strength=%f]".formatted(id, experience, strength); }
}