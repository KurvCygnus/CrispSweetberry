package kurvcygnus.crispsweetberry.common.features.coins;

import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinItem;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackBlock;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackItem;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.ICoinType;
import kurvcygnus.crispsweetberry.common.registries.CrispBlocks;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static kurvcygnus.crispsweetberry.common.registries.CrispItems.*;
import static net.minecraft.world.item.Items.GOLD_NUGGET;
import static net.minecraft.world.item.Items.IRON_NUGGET;

public record VanillaCoinTypes(
    String id,
    Supplier<? extends AbstractCoinStackBlock> blockSupplier,
    Supplier<? extends AbstractCoinStackItem> stackSupplier,
    Supplier<? extends AbstractCoinItem> coinSupplier,
    @Nullable Supplier<? extends Item> nuggetSupplier,
    int experience,
    float penaltyRate,
    Predicate<Supplier<? extends Item>> registerCondition
) implements ICoinType
{
    private static final Predicate<Supplier<? extends Item>> DEFAULT_CONDITION = Objects::nonNull;
    
    public static final VanillaCoinTypes COPPER = new VanillaCoinTypes(
        "copper",
        CrispBlocks.COPPER_COIN_STACK,
        COPPER_COIN_STACK,
        COPPER_COIN,
        IRON_NUGGET::asItem,//? TODO
        1, 0.7F,
        DEFAULT_CONDITION//? TODO
    );
    public static final VanillaCoinTypes IRON = new VanillaCoinTypes(
        "iron",
        CrispBlocks.IRON_COIN_STACK,
        IRON_COIN_STACK,
        IRON_COIN,
        IRON_NUGGET::asItem,
        3, 0.8F,
        DEFAULT_CONDITION
    );
    public static final VanillaCoinTypes GOLD = new VanillaCoinTypes(
        "gold",
        CrispBlocks.GOLD_COIN_STACK,
        GOLD_COIN_STACK,
        GOLD_COIN,
        GOLD_NUGGET::asItem,
        7, 0.85F,
        DEFAULT_CONDITION
    );
    public static final VanillaCoinTypes DIAMOND = new VanillaCoinTypes(
        "diamond",
        CrispBlocks.DIAMOND_COIN_STACK,
        DIAMOND_COIN_STACK,
        DIAMOND_COIN,
        IRON_NUGGET::asItem,//? TODO
        10, 0.9F,
        DEFAULT_CONDITION//? TODO
    );
    
    public static final VanillaCoinTypes[] VALUES = {IRON, GOLD};
    
    @Override
    public @NotNull ResourceLocation getId() { return CrispDefUtils.getModNamespacedLocation(id); }
    
    @Override
    public @NotNull AbstractCoinStackBlock stackBlock() { return this.blockSupplier.get(); }
    
    @Override
    public @NotNull AbstractCoinStackItem stackItem() { return this.stackSupplier.get(); }
    
    @Override
    public @NotNull AbstractCoinItem coinItem() { return this.coinSupplier.get(); }
    
    @Override
    public @NotNull Item nuggetItem() 
    {
        Objects.requireNonNull(nuggetSupplier, "Impossible case happen: field \"nuggetSupplier\" appears to be null.");
        return nuggetSupplier.get();
    }
    
    @Override
    public int getExperience() { return this.experience; }
    
    @Override
    public float getPenaltyRate() { return this.penaltyRate; }
    
    @Override
    public boolean shouldRegister() { return this.registerCondition.test(this.nuggetSupplier); }
}
