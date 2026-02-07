package kurvcygnus.crispsweetberry.datagen;

import com.google.common.collect.Iterables;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnRegistries;
import kurvcygnus.crispsweetberry.common.registries.CrispBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static kurvcygnus.crispsweetberry.datagen.utils.CrispLootUtils.initLootPool;

final class CrispBlockLootSubProvider extends BlockLootSubProvider
{
    CrispBlockLootSubProvider(@NotNull HolderLookup.Provider registries) { super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registries); }
    
    @Override
    protected void generate()
    {
        this.add(CrispBlocks.CARRY_CRATE.value(), block ->
            initLootPool(() -> LootPool.lootPool().
                setRolls(ConstantValue.exactly(1F)).
                add(AlternativesEntry.alternatives(
                    LootItem.
                        lootTableItem(block).
                        when(hasSilkTouch()),
                    
                    LootItem.
                        lootTableItem(Items.PAPER.asItem()).
                        apply(SetItemCountFunction.setCount(UniformGenerator.between(2F, 5F))).
                        apply(ApplyExplosionDecay.explosionDecay())
                ))
            )
        );
        
        this.dropSelf(KilnRegistries.KILN_BLOCK.value());
    }
    
    @Override
    protected @NotNull Iterable<Block> getKnownBlocks()
    {
        final List<Block> miscList = List.of(CrispBlocks.CARRY_CRATE.value());
        final List<Block> kilnList = List.of(KilnRegistries.KILN_BLOCK.value());
        
        return Iterables.concat(miscList, kilnList);
    }
}
