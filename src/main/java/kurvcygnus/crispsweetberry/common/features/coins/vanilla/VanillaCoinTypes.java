package kurvcygnus.crispsweetberry.common.features.coins.vanilla;

import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinItem;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackBlock;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackItem;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.ICoinType;
import kurvcygnus.crispsweetberry.common.features.coins.events.NuggetItemCheckEvent;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;
import static kurvcygnus.crispsweetberry.common.features.coins.CoinRegistries.*;
import static kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils.throwIf;
import static net.minecraft.world.item.Items.GOLD_NUGGET;
import static net.minecraft.world.item.Items.IRON_NUGGET;

/**
 * Enum-like coin type definitions for vanilla ores.<br>
 * We intentionally do NOT use {@link Enum} here to allow <u>{@link net.neoforged.neoforge.common.util.Lazy lazy suppliers}</u> and more flexible registration behavior.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see ICoinType Contract Interface
 */
public final class VanillaCoinTypes implements ICoinType<VanillaCoinTypes>
{
    private final @NotNull String id;
    private final @NotNull Supplier<? extends AbstractCoinStackBlock<VanillaCoinTypes>> blockSupplier;
    private final @NotNull Supplier<? extends AbstractCoinStackItem<VanillaCoinTypes>> stackSupplier;
    private final @NotNull Supplier<? extends AbstractCoinItem<VanillaCoinTypes>> coinSupplier;
    private final @Nullable Supplier<? extends Item> nuggetSupplier;
    private final int experience;
    private final float penaltyRate;
    private final float strength;
    private final @NotNull Predicate<Supplier<? extends Item>> enableCondition;
    
    private VanillaCoinTypes(@NotNull String id, @NotNull Supplier<? extends AbstractCoinStackBlock<VanillaCoinTypes>> blockSupplier,
        @NotNull Supplier<? extends AbstractCoinStackItem<VanillaCoinTypes>> stackSupplier, @NotNull Supplier<? extends AbstractCoinItem<VanillaCoinTypes>> coinSupplier,
        @Nullable Supplier<? extends Item> nuggetSupplier, int experience, float penaltyRate, float strength, @NotNull Predicate<Supplier<? extends Item>> enableCondition)
            {
                requireNonNull(id, "Param \"id\" must not be null!");
                requireNonNull(blockSupplier, "Param \"blockSupplier\" must not be null!");
                requireNonNull(stackSupplier, "Param \"stackSupplier\" must not be null!");
                requireNonNull(coinSupplier, "Param \"coinSupplier\" must not be null!");
                throwIf(experience <= 0, () -> new IllegalArgumentException("Param \"experience\" must be a positive integer!"));
                throwIf(penaltyRate <= 0F || penaltyRate > 1F, () ->
                    new IllegalArgumentException("Param \"penaltyRate\" must be in range of (0F, 1F]!"));
                throwIf(strength <= 0F, () -> new IllegalArgumentException("Param \"strength\" must be a positive float!"));
                
                this.id = id;
                this.blockSupplier = blockSupplier;
                this.stackSupplier = stackSupplier;
                this.coinSupplier = coinSupplier;
                this.nuggetSupplier = nuggetSupplier;
                this.experience = experience;
                this.penaltyRate = penaltyRate;
                this.strength = strength;
                this.enableCondition = enableCondition;
            }
    
    public static final Predicate<Supplier<? extends Item>> DEFAULT_CONDITION = Objects::nonNull;
    public static final Predicate<Supplier<? extends Item>> OPTIONAL = (supplier) -> supplier.get() != Items.AIR;
    
    public static final VanillaCoinTypes COPPER = new VanillaCoinTypes(
        "copper", COPPER_COIN_STACK_BLOCK, COPPER_COIN_STACK, COPPER_COIN,
        NuggetItemCheckEvent.copperNugget, 1, 0.7F, 0.5F, OPTIONAL
    );
    
    public static final VanillaCoinTypes IRON = new VanillaCoinTypes(
        "iron", IRON_COIN_STACK_BLOCK, IRON_COIN_STACK, IRON_COIN,
        IRON_NUGGET::asItem, 3, 0.8F, 1.0F, DEFAULT_CONDITION
    );
    
    public static final VanillaCoinTypes GOLD = new VanillaCoinTypes(
        "gold", GOLD_COIN_STACK_BLOCK, GOLD_COIN_STACK, GOLD_COIN,
        GOLD_NUGGET::asItem, 7, 0.85F, 0.8F, DEFAULT_CONDITION
    );
    
    public static final VanillaCoinTypes DIAMOND = new VanillaCoinTypes(
        "diamond", DIAMOND_COIN_STACK_BLOCK, DIAMOND_COIN_STACK, DIAMOND_COIN,
        NuggetItemCheckEvent.diamondNugget, 10, 0.9F, 1.5F, OPTIONAL
    );
    
    public static final VanillaCoinTypes[] VALUES = {COPPER, IRON, GOLD, DIAMOND};
    
    public @NotNull String id() { return id; }
    
    @Override public @NotNull ResourceLocation getId() { return CrispDefUtils.getModNamespacedLocation(id); }
    
    @Override public @NotNull AbstractCoinStackBlock<VanillaCoinTypes> stackBlock() { return this.blockSupplier.get(); }
    
    @Override public @NotNull AbstractCoinStackItem<VanillaCoinTypes> stackItem() { return this.stackSupplier.get(); }
    
    @Override public @NotNull AbstractCoinItem<VanillaCoinTypes> coinItem() { return this.coinSupplier.get(); }
    
    @Override
    public @NotNull Item nuggetItem()
    {
        requireNonNull(nuggetSupplier, "Impossible case: field \"nuggetSupplier\" is null.");
        return nuggetSupplier.get();
    }
    
    @Override public int getExperience() { return this.experience; }
    
    @Override public float getPenaltyRate() { return this.penaltyRate; }
    
    @Override public float getStrength() { return this.strength; }
    
    @Override public boolean shouldAppear() { return this.enableCondition.test(this.nuggetSupplier); }
    
    @Override
    public boolean equals(@Nullable Object object)
    {
        if(this == object) 
            return true;
        if(object == null || getClass() != object.getClass())
            return false;
        
        final VanillaCoinTypes that = (VanillaCoinTypes) object;
        
        return experience == that.experience &&
            Float.compare(that.penaltyRate, penaltyRate) == 0 &&
            id.equals(that.id);
    }
    
    @Override public int hashCode() { return hash(id, experience, penaltyRate); }
    
    @Override public @NotNull String toString() { return "VanillaCoinTypes[" + "id=" + id + ", experience=" + experience + "]"; }
}