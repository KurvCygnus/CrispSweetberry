package kurvcygnus.crispsweetberry.datagen.api;

import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackBlock;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.ICoinType;
import kurvcygnus.crispsweetberry.common.features.coins.datagen.IsCrunchingCondition;
import kurvcygnus.crispsweetberry.common.features.coins.datagen.SetCoinCountFunction;
import kurvcygnus.crispsweetberry.datagen.utils.CrispLootUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractCoinLootTableProvider extends BlockLootSubProvider
{
    public AbstractCoinLootTableProvider(HolderLookup.Provider registries) { super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registries); }
    
    protected abstract List<? extends AbstractCoinStackBlock<?>> initCoinBlockLists();
    
    @Override
    protected final void generate()
    {
        for(final Block block: getKnownBlocks())
        {
            final AbstractCoinStackBlock<?> coinStackBlock = (AbstractCoinStackBlock<?>) block;
             
            final ICoinType<?> coinType = coinStackBlock.getCoinType();
            
            this.add(coinStackBlock, blockAttributes ->
                CrispLootUtils.initLootPool(() -> LootPool.lootPool().
                    setRolls(ConstantValue.exactly(1F)).
                    add(AlternativesEntry.alternatives(
                        LootItem.
                            lootTableItem(coinType.stackItem()).
                            when(hasSilkTouch()),
                        
                        LootItem.
                            lootTableItem(coinType.nuggetItem()).
                            when(IsCrunchingCondition::new).
                            apply(SetCoinCountFunction::new),//! Experience award is processed in Block itself, not lootTable.
                        
                        LootItem.
                            lootTableItem(coinType.coinItem()).
                            when(InvertedLootItemCondition.invert(IsCrunchingCondition::new)).
                            apply(SetCoinCountFunction::new).
                            apply(ApplyExplosionDecay.explosionDecay())
                    ))
                )
            );
        }
    }
    
    @Override
    protected final @NotNull Iterable<Block> getKnownBlocks() { return initCoinBlockLists().stream().collect(Collectors.toUnmodifiableList()); }
}
