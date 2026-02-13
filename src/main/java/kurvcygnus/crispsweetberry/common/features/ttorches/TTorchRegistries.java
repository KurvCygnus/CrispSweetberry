//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.FakeLightBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic.TemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic.TemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownRedstoneTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableRedstoneTorchItem;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableTorchItem;
import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.IWailaClientRegistration;

import java.util.List;

/**
 * This registers everything that relates to throwable torch series.<br>
 * <i>Since the first letter of this series' content are all {@code 'T'}, thus both registry and package are called {@code TTorch}.</i>
 * @since 1.0 Release
 */
public enum TTorchRegistries implements IRegistrant
{
    INSTANCE;
    
    @Override
    public void register(@NotNull IEventBus bus) { REGISTRIES.forEach(register ->  register.register(bus)); }
    
    @Override
    public boolean isFeature() { return true; }
    
    @Override
    public @NotNull String getJob() { return "T Torches"; }
    
    @Override
    public int getPriority() { return 6; }
    
    private static final DeferredRegister<Item> THROWABLE_TORCH_REGISTER = DeferredRegister.createItems(CrispSweetberry.NAMESPACE);
    private static final DeferredRegister<Block> TEMPORARY_TORCH_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.NAMESPACE);
    private static final DeferredRegister<EntityType<?>> THROWN_TORCH_REGISTER = DeferredRegister.create(Registries.ENTITY_TYPE, CrispSweetberry.NAMESPACE);
    
    public static final List<DeferredRegister<?>> REGISTRIES = List.of(
        TEMPORARY_TORCH_REGISTER,
        THROWABLE_TORCH_REGISTER,
        THROWN_TORCH_REGISTER
    );
    
    @AutoI18n(value = {
        "en_us = Thrown Torch",
        "lol_us = fullee lite stik",
        "zh_cn = 投掷火把"
        },
        group = "csb:temporary_torch"
    )
    public static final DeferredHolder<Block, TemporaryTorchBlock> TEMPORARY_TORCH = TEMPORARY_TORCH_REGISTER.register(
        "temporary_torch",
        resourceLocation -> new TemporaryTorchBlock()
    );
    
    @AutoI18n(group = "csb:temporary_torch")
    public static final DeferredHolder<Block, TemporaryWallTorchBlock> TEMPORARY_WALL_TORCH = TEMPORARY_TORCH_REGISTER.register(
        "temporary_wall_torch", 
        resourceLocation -> new TemporaryWallTorchBlock()
    );
    
    /**
     * @implNote This is not supposed to be appeared in game.<br>
     * <u>{@link kurvcygnus.crispsweetberry.integrations.JadeEntrypoint Jade}</u>'s block display implementation relies on 
     * <u>{@link BlockItem}</u>, that's why we still implemented this.
     */
    @AutoI18n(group = "csb:temporary_torch")
    public static final Holder<Item> TEMPORARY_TORCH_BLOCK_ITEM = THROWABLE_TORCH_REGISTER.register("temporary_torch", 
        () -> new BlockItem(TEMPORARY_TORCH.value(), new Item.Properties())
    );
    
    /**
     * @implNote This is not supposed to be appeared in game.<br>
     * <u>{@link kurvcygnus.crispsweetberry.integrations.JadeEntrypoint Jade}</u>'s block display implementation relies on
     * <u>{@link BlockItem}</u>, that's why we still implemented this.
     */
    @AutoI18n(group = "csb:temporary_torch")
    public static final Holder<Item> TEMPORARY_WALL_TORCH_BLOCK_ITEM = THROWABLE_TORCH_REGISTER.register("temporary_wall_torch", 
        () -> new BlockItem(TEMPORARY_WALL_TORCH.value(), new Item.Properties())
    );
    
    /**
     * This block is hidden in jade's display.
     * @see kurvcygnus.crispsweetberry.integrations.JadeEntrypoint#registerClient(IWailaClientRegistration) Hidden Implementation
     */
    @AutoI18n(value = {
        "en_us = UwU",
        "lol_us = OwO",
        "zh_cn = QAQ"
        },
        group = "csb:lol"
    )
    public static final Holder<Block> FAKE_LIGHT_BLOCK = TEMPORARY_TORCH_REGISTER.register(
        "fake_light_block",
        resourceLocation -> new FakeLightBlock()
    );
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Throwable Torch",
        "lol_us = chuk da lite stik",
        "zh_cn = 投掷火把"
    })
    public static final Holder<Item> THROWABLE_TORCH = THROWABLE_TORCH_REGISTER.register(
        "throwable_torch",
        resourceLocation -> new ThrowableTorchItem()
    );
    
    @AutoI18n({
        "en_us = Throwable Redstone Torch",
        "lol_us = lite stewberi stik",
        "zh_cn = 红石投掷火把"
    })
    public static final Holder<Item> THROWABLE_REDSTONE_TORCH = THROWABLE_TORCH_REGISTER.register(
        "throwable_redstone_torch",
        resourceLocation -> new ThrowableRedstoneTorchItem()
    );
    
    @AutoI18n({
        "en_us = Thrown Torch",
        "lol_us = Spinn' Stik",
        "zh_cn = 投掷火把"
    })
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownTorchEntity>> THROWN_TORCH = getTypeHolder("thrown_torch", ThrownTorchEntity::new);
    
    @AutoI18n({
        "en_us = Thrown Redstone Torch",
        "lol_us = Spinn' Stewberi Stik",
        "zh_cn = 红石投掷火把"
    })
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownRedstoneTorchEntity>> THROWN_REDSTONE_TORCH = 
        getTypeHolder("thrown_redstone_torch", ThrownRedstoneTorchEntity::new);
    
    private static <T extends AbstractThrownTorchEntity> @NotNull DeferredHolder<EntityType<?>, EntityType<T>>
    getTypeHolder(@NotNull String id, @NotNull EntityType.EntityFactory<T> factory)
    {
        return THROWN_TORCH_REGISTER.register(id, () -> EntityType.Builder.of(factory, MobCategory.MISC).
                sized(0.25F, 0.25F).
                updateInterval(10).
                noSummon().
                build(id)
        );
    }
}
