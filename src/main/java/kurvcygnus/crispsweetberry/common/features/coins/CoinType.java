package kurvcygnus.crispsweetberry.common.features.coins;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinItem;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackBlock;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackItem;
import kurvcygnus.crispsweetberry.common.registries.CrispItems;
import kurvcygnus.crispsweetberry.utils.misc.CrispLogUtils;
import kurvcygnus.crispsweetberry.utils.misc.CrispMiscUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.function.Supplier;

import static kurvcygnus.crispsweetberry.common.registries.CrispBlocks.*;

//? TODO: Interface ass shit stuff

public enum CoinType
{
    COPPER(
        COPPER_COIN_STACK,
        CrispItems.COPPER_COIN_STACK,
        CrispItems.COPPER_COIN,
        Items.IRON_NUGGET::asItem,
        1, 0.7F
    ),
    IRON(
        IRON_COIN_STACK,
        CrispItems.IRON_COIN_STACK,
        CrispItems.IRON_COIN,
        Items.IRON_NUGGET::asItem,
        3, 0.8F
    ),
    GOLD(
        GOLD_COIN_STACK,
        CrispItems.GOLD_COIN_STACK,
        CrispItems.GOLD_COIN,
        Items.GOLD_NUGGET::asItem,
        7, 0.85F
    ),
    DIAMOND(
        DIAMOND_COIN_STACK,
        CrispItems.DIAMOND_COIN_STACK,
        CrispItems.DIAMOND_COIN,
        Items.IRON_NUGGET::asItem,
        10, 0.9F
    );
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private final Supplier<? extends AbstractCoinStackBlock> blockSupplier;
    private final Supplier<? extends AbstractCoinStackItem> stackItemSupplier;
    private final Supplier<? extends AbstractCoinItem> coinItemSupplier;
    private final Supplier<? extends Item> nuggetItemSupplier;
    private final int experience;
    private final float penaltyRate;

    CoinType(@NotNull Supplier<? extends AbstractCoinStackBlock> blockSupplier, @NotNull Supplier<? extends AbstractCoinStackItem> stackItemSupplier,
        @NotNull Supplier<? extends AbstractCoinItem> coinItemSupplier, @NotNull Supplier<? extends Item> nuggetItemSupplier, int experience, float penaltyRate)
            {
                Objects.requireNonNull(blockSupplier, "Param \"blockSupplier\" must not be null!");
                Objects.requireNonNull(stackItemSupplier, "Param \"stackItemSupplier\" must not be null!");
                Objects.requireNonNull(nuggetItemSupplier, "Param \"nuggetItemSupplier\" must not be null!");
                Objects.requireNonNull(coinItemSupplier, "Param \"coinItemSupplier\" must not be null!");
                CrispMiscUtils.throwIf(experience <= 0, () -> new IllegalArgumentException("Param \"experience\" must be a positive integer!"));
                CrispMiscUtils.throwIf(penaltyRate <= 0F || penaltyRate > 1.0F, () -> new IllegalArgumentException("Param \"penaltyRate\" must be in range (0F, 1.0F]!"));
                
                this.blockSupplier = blockSupplier;
                this.stackItemSupplier = stackItemSupplier;
                this.coinItemSupplier = coinItemSupplier;
                this.nuggetItemSupplier = nuggetItemSupplier;
                this.experience = experience;
                this.penaltyRate = penaltyRate;
            }
    
    public static void validateTags()
    {
        for(CoinType type: values())
        {
            ItemStack itemStack = type.getNuggetItemSupplier().getDefaultInstance();
            
            CrispLogUtils.logIf(!itemStack.is(Tags.Items.NUGGETS), () ->
                LOGGER.warn("Invalid definition for {}: Item {} is not in the Nuggets tag!",
                    type.name(), itemStack.getItemHolder().getRegisteredName()
                )
            );
        }
    }
    
    public @NotNull AbstractCoinStackBlock getBlockSupplier() { return blockSupplier.get(); }
    
    public @NotNull AbstractCoinStackItem getStackItemSupplier() { return stackItemSupplier.get(); }
    
    public @NotNull AbstractCoinItem getCoinItemSupplier() { return coinItemSupplier.get(); }
    
    public @NotNull Item getNuggetItemSupplier() { return nuggetItemSupplier.get(); }
    
    public int getExperience() { return experience; }
    
    public float getPenaltyRate() { return penaltyRate; }
}
