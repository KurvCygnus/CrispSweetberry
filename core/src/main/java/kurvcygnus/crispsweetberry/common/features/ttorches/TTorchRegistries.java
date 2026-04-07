//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.FakeLightBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic.TemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic.TemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.glowstick.GlowStickBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone.TemporaryRedstoneTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone.TemporaryRedstoneWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.soul.TemporarySoulTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.soul.TemporarySoulWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.GlowStickEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownRedstoneTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownSoulTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.GlowStickItem;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableRedstoneTorchItem;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableSoulTorchItem;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableTorchItem;
import kurvcygnus.crispsweetberry.common.features.ttorches.sync.SoulFireTagPayloads;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import kurvcygnus.crispsweetberry.utils.core.registry.IRegistrant;
import kurvcygnus.crispsweetberry.utils.core.registry.RegisterToTab;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.IWailaClientRegistration;

import java.util.List;
import java.util.Map;

import static kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone.ITRedstoneTorchExtensions.*;

/**
 * This registers everything that relates to throwable torch series.<br>
 * <i>Since the first letter of this series' content are all {@code 'T'}, thus both registry and package are called {@code TTorch}.</i>
 * @since 1.0 Release
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public enum TTorchRegistries implements IRegistrant
{
    //  region Registry Basics
    INST;
    
    @Override public void register(@NotNull IEventBus bus) { REGISTRIES.forEach(registry -> registry.register(bus)); }
    
    @Override public @NotNull String getJob() { return "T Torches"; }
    
    @Override public @NotNull PriorityPair getPriority() { return new PriorityPair(PriorityRange.FEATURE, 2); }
    
    private static final DeferredRegister<Item> THROWABLE_TORCH_REGISTER = DeferredRegister.createItems(CrispSweetberry.NAMESPACE);
    private static final DeferredRegister<Block> TEMPORARY_TORCH_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.NAMESPACE);
    private static final DeferredRegister<EntityType<?>> THROWN_TORCH_REGISTER = DeferredRegister.create(Registries.ENTITY_TYPE, CrispSweetberry.NAMESPACE);
    
    public static final List<DeferredRegister<?>> REGISTRIES = List.of(
        TEMPORARY_TORCH_REGISTER,
        THROWABLE_TORCH_REGISTER,
        THROWN_TORCH_REGISTER
    );
    //endregion
    
    //  region Sync Packet Registry
    @SubscribeEvent
    static void registerPacket(@NotNull RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar("1.0 Release");
        
        registrar.playToClient(
            SoulFireTagPayloads.SoulFireTagPayload.TYPE,
            SoulFireTagPayloads.SoulFireTagPayload.CODEC,
            SoulFireTagPayloads.SoulFireTagPayloadHandler::attachTag
        );
    }
    //endregion
    
    //  region Item Registries
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
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Throwable Soul Torch",
        "lol_us = COOL GATLING BARREL",
        "zh_cn = 灵魂投掷火把"
    })
    public static final Holder<Item> THROWABLE_SOUL_TORCH = THROWABLE_TORCH_REGISTER.register(
        "throwable_soul_torch",
        resourceLocation -> new ThrowableSoulTorchItem()
    );
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Throwable Redstone Torch",
        "lol_us = lite stewberi stik",
        "zh_cn = 红石投掷火把"
    })
    public static final DeferredHolder<Item, ThrowableRedstoneTorchItem> THROWABLE_REDSTONE_TORCH = 
        getThrowableRedstoneTorch(OxidizeState.NORMAL, false);
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Waxed Throwable Redstone Torch",
        "lol_us = shyni lite stewberi stik",
        "zh_cn = 涂蜡红石投掷火把"
    })
    public static final DeferredHolder<Item, ThrowableRedstoneTorchItem> WAXED_THROWABLE_REDSTONE_TORCH =
        getThrowableRedstoneTorch(OxidizeState.NORMAL, true);
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Exposed Throwable Redstone Torch",
        "lol_us = od lite stewberi stik",
        "zh_cn = 斑驳红石投掷火把"
    })
    public static final DeferredHolder<Item, ThrowableRedstoneTorchItem> EXPOSED_THROWABLE_REDSTONE_TORCH = 
        getThrowableRedstoneTorch(OxidizeState.EXPOSED, false);
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Waxed Exposed Throwable Redstone Torch",
        "lol_us = od butt shyni lite stewberi stik",
        "zh_cn = 涂蜡斑驳红石投掷火把"
    })
    public static final DeferredHolder<Item, ThrowableRedstoneTorchItem> WAXED_EXPOSED_THROWABLE_REDSTONE_TORCH =
        getThrowableRedstoneTorch(OxidizeState.EXPOSED, true);
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Exposed Throwable Redstone Torch",
        "lol_us = ord lite stewberi stik",
        "zh_cn = 锈蚀红石投掷火把"
    })
    public static final DeferredHolder<Item, ThrowableRedstoneTorchItem> WEATHERED_THROWABLE_REDSTONE_TORCH = 
        getThrowableRedstoneTorch(OxidizeState.WEATHERED, false);
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Waxed Exposed Throwable Redstone Torch",
        "lol_us = ord butt shyni lite stewberi stik",
        "zh_cn = 涂蜡锈蚀红石投掷火把"
    })
    public static final DeferredHolder<Item, ThrowableRedstoneTorchItem> WAXED_WEATHERED_THROWABLE_REDSTONE_TORCH =
        getThrowableRedstoneTorch(OxidizeState.WEATHERED, true);
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Oxidized Throwable Redstone Torch",
        "lol_us = eww kat hite this stik",
        "zh_cn = 氧化红石投掷火把"
    })
    public static final DeferredHolder<Item, ThrowableRedstoneTorchItem> OXIDIZED_THROWABLE_REDSTONE_TORCH = 
        getThrowableRedstoneTorch(OxidizeState.OXIDIZED, false);
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Waxed Oxidized Throwable Redstone Torch",
        "lol_us = shyni butt eww kat hite this stik",
        "zh_cn = 涂蜡氧化红石投掷火把"
    })
    public static final DeferredHolder<Item, ThrowableRedstoneTorchItem> WAXED_OXIDIZED_THROWABLE_REDSTONE_TORCH =
        getThrowableRedstoneTorch(OxidizeState.OXIDIZED, true);
    
    @RegisterToTab
    @AutoI18n(value = {
        "en_us = Glowstick",
        "lol_us = Bluberi stik",
        "zh_cn = 荧光棒"
        },
        group = "glowstick",
        key = "glowstick"
    )
    public static final Holder<Item> GLOWSTICK_ITEM = THROWABLE_TORCH_REGISTER.register(
        "glowstick",
        resourceLocation -> new GlowStickItem()
    );
    //endregion
    
    //  region Block Registries
    @AutoI18n(value = {
            "en_us = Thrown Torch",
            "lol_us = fullee lite stik",
            "zh_cn = 投掷火把"
        },
        group = "temporary_torch"
    )
    public static final DeferredHolder<Block, TemporaryTorchBlock> TEMPORARY_TORCH = TEMPORARY_TORCH_REGISTER.register(
        "temporary_torch",
        resourceLocation -> new TemporaryTorchBlock()
    );
    
    @AutoI18n(group = "temporary_torch")
    public static final DeferredHolder<Block, TemporaryWallTorchBlock> TEMPORARY_WALL_TORCH = TEMPORARY_TORCH_REGISTER.register(
        "temporary_wall_torch",
        resourceLocation -> new TemporaryWallTorchBlock()
    );
    
    @AutoI18n(value = {
        "en_us = Thrown Soul Torch",
        "lol_us = Gatling Barrel",
        "zh_cn = 投掷灵魂火把"
    },
        group = "temporary_soul_torch"
    )
    public static final DeferredHolder<Block, TemporarySoulTorchBlock> TEMPORARY_SOUL_TORCH = TEMPORARY_TORCH_REGISTER.register(
        "temporary_soul_torch",
        resourceLocation -> new TemporarySoulTorchBlock()
    );
    
    @AutoI18n(group = "temporary_soul_torch")
    public static final DeferredHolder<Block, TemporarySoulWallTorchBlock> TEMPORARY_SOUL_WALL_TORCH = TEMPORARY_TORCH_REGISTER.register(
        "temporary_soul_wall_torch",
        resourceLocation -> new TemporarySoulWallTorchBlock()
    );
    
    @AutoI18n(value = {
            "en_us = Temporary Redstone Torch",
            "lol_us = fullee stewberi stik",
            "zh_cn = 投掷红石火把"
        },
        group = "temporary_redstone_torch"
    )
    public static final DeferredHolder<Block, TemporaryRedstoneTorchBlock> TEMPORARY_REDSTONE_TORCH = getRedstoneTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.NORMAL,
        false,
        THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(group = "temporary_redstone_torch")
    public static final DeferredHolder<Block, TemporaryRedstoneWallTorchBlock> TEMPORARY_REDSTONE_WALL_TORCH = getRedstoneWallTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.NORMAL,
        false,
        THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(value = {
            "en_us = Waxed Temporary Redstone Torch",
            "lol_us = shyni fullee stewberi stik",
            "zh_cn = 涂蜡投掷红石火把"
        },
        group = "waxed_temporary_redstone_torch"
    )
    public static final DeferredHolder<Block, TemporaryRedstoneTorchBlock> WAXED_TEMPORARY_REDSTONE_TORCH = getRedstoneTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.NORMAL,
        true,
        WAXED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(group = "waxed_temporary_redstone_torch")
    public static final DeferredHolder<Block, TemporaryRedstoneWallTorchBlock> WAXED_TEMPORARY_REDSTONE_WALL_TORCH = getRedstoneWallTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.NORMAL,
        true,
        WAXED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(value = {
            "en_us = Temporary Redstone Torch",
            "lol_us = od fullee stewberi stik",
            "zh_cn = 斑驳投掷红石火把"
        },
        group = "exposed_temporary_redstone_torch"
    )
    public static final DeferredHolder<Block, TemporaryRedstoneTorchBlock> EXPOSED_TEMPORARY_REDSTONE_TORCH = getRedstoneTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.EXPOSED,
        false,
        EXPOSED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(group = "exposed_temporary_redstone_torch")
    public static final DeferredHolder<Block, TemporaryRedstoneWallTorchBlock> EXPOSED_TEMPORARY_REDSTONE_WALL_TORCH = getRedstoneWallTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.EXPOSED,
        false,
        EXPOSED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(value = {
            "en_us = Waxed Exposed Temporary Redstone Torch",
            "lol_us = od butt shyni fullee stewberi stik",
            "zh_cn = 涂蜡斑驳投掷红石火把"
        },
        group = "waxed_exposed_temporary_redstone_torch"
    )
    public static final DeferredHolder<Block, TemporaryRedstoneTorchBlock> WAXED_EXPOSED_TEMPORARY_REDSTONE_TORCH = getRedstoneTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.EXPOSED,
        true,
        WAXED_EXPOSED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(group = "waxed_exposed_temporary_redstone_torch")
    public static final DeferredHolder<Block, TemporaryRedstoneWallTorchBlock> WAXED_EXPOSED_TEMPORARY_REDSTONE_WALL_TORCH = getRedstoneWallTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.EXPOSED,
        true,
        WAXED_EXPOSED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(value = {
            "en_us = Temporary Redstone Torch",
            "lol_us = ord fullee stewberi stik",
            "zh_cn = 锈蚀投掷红石火把"
        },
        group = "weathered_temporary_redstone_torch"
    )
    public static final DeferredHolder<Block, TemporaryRedstoneTorchBlock> WEATHERED_TEMPORARY_REDSTONE_TORCH = getRedstoneTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.WEATHERED,
        false,
        WEATHERED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(group = "weathered_temporary_redstone_torch")
    public static final DeferredHolder<Block, TemporaryRedstoneWallTorchBlock> WEATHERED_TEMPORARY_REDSTONE_WALL_TORCH = getRedstoneWallTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.WEATHERED,
        false,
        WEATHERED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(value = {
            "en_us = Waxed Weathered Temporary Redstone Torch",
            "lol_us = ord butt shyni fullee stewberi stik",
            "zh_cn = 涂蜡锈蚀投掷红石火把"
        },
        group = "waxed_weathered_temporary_redstone_torch"
    )
    public static final DeferredHolder<Block, TemporaryRedstoneTorchBlock> WAXED_WEATHERED_TEMPORARY_REDSTONE_TORCH = getRedstoneTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.WEATHERED,
        true,
        WAXED_WEATHERED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(group = "waxed_weathered_temporary_redstone_torch")
    public static final DeferredHolder<Block, TemporaryRedstoneWallTorchBlock> WAXED_WEATHERED_TEMPORARY_REDSTONE_WALL_TORCH = getRedstoneWallTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.WEATHERED,
        true,
        WAXED_WEATHERED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(value = {
            "en_us = Temporary Redstone Torch",
            "lol_us = eww kat hite this stik",
            "zh_cn = 氧化投掷红石火把"
        },
        group = "oxidized_temporary_redstone_torch"
    )
    public static final DeferredHolder<Block, TemporaryRedstoneTorchBlock> OXIDIZED_TEMPORARY_REDSTONE_TORCH = getRedstoneTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.OXIDIZED,
        false,
        OXIDIZED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(group = "oxidized_temporary_redstone_torch")
    public static final DeferredHolder<Block, TemporaryRedstoneWallTorchBlock> OXIDIZED_TEMPORARY_REDSTONE_WALL_TORCH = getRedstoneWallTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.OXIDIZED,
        false,
        OXIDIZED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(value = {
            "en_us = Waxed Oxidized Temporary Redstone Torch",
            "lol_us = shyni butt eww kat hite this stik",
            "zh_cn = 涂蜡氧化投掷红石火把"
        },
        group = "waxed_oxidized_temporary_redstone_torch"
    )
    public static final DeferredHolder<Block, TemporaryRedstoneTorchBlock> WAXED_OXIDIZED_TEMPORARY_REDSTONE_TORCH = getRedstoneTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.OXIDIZED,
        true,
        WAXED_OXIDIZED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(group = "waxed_oxidized_temporary_redstone_torch")
    public static final DeferredHolder<Block, TemporaryRedstoneWallTorchBlock> WAXED_OXIDIZED_TEMPORARY_REDSTONE_WALL_TORCH = getRedstoneWallTorchBlock(
        TEMPORARY_TORCH_REGISTER,
        OxidizeState.OXIDIZED,
        true,
        WAXED_OXIDIZED_THROWABLE_REDSTONE_TORCH
    );
    
    @AutoI18n(group = "glowstick", key = "glowstick")
    public static final DeferredHolder<Block, GlowStickBlock> GLOW_STICK_BLOCK = TEMPORARY_TORCH_REGISTER.register(
        "glow_stick",
        resourceLocation -> new GlowStickBlock()
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
        group = "lol"
    )
    public static final Holder<Block> FAKE_LIGHT_BLOCK = TEMPORARY_TORCH_REGISTER.register(
        "fake_light_block",
        resourceLocation -> new FakeLightBlock()
    );
    //endregion
    
    //  region Entity Registries
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
    
    @AutoI18n({
        "en_us = Throwable Soul Torch",
        "lol_us = Spinn' Gatling Barrel",
        "zh_cn = 灵魂投掷火把"
    })
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownSoulTorchEntity>> THROWN_SOUL_TORCH = 
        getTypeHolder("thrown_soul_torch", ThrownSoulTorchEntity::new);
    
    @AutoI18n(group = "glowstick", key = "glowstick")
    public static final DeferredHolder<EntityType<?>, EntityType<GlowStickEntity>> GLOW_STICK_ENTITY = getTypeHolder("glow_stick", GlowStickEntity::new);
    //endregion
    
    //  region Misc & helpers
    //*:=== Jade Compat
    public static final List<DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractThrownTorchEntity>>> ENTITY_HIDE_LIST = List.of(
        THROWN_TORCH,
        THROWN_REDSTONE_TORCH,
        THROWN_SOUL_TORCH,
        GLOW_STICK_ENTITY
    );
    
    //*:=== Redstone TTorch Dispatchers
    private static final Map<OxidizeState, DeferredHolder<Block, TemporaryRedstoneTorchBlock>> UNWAXED_REDSTONE_TTORCH_LOOKUP =
        DefinitionUtils.createImmutableEnumMap(OxidizeState.class, map ->
            {
                map.put(OxidizeState.NORMAL, TEMPORARY_REDSTONE_TORCH);
                map.put(OxidizeState.EXPOSED, EXPOSED_TEMPORARY_REDSTONE_TORCH);
                map.put(OxidizeState.WEATHERED, WEATHERED_TEMPORARY_REDSTONE_TORCH);
                map.put(OxidizeState.OXIDIZED, OXIDIZED_TEMPORARY_REDSTONE_TORCH);
            }
        );
    
    private static final Map<OxidizeState, DeferredHolder<Block, TemporaryRedstoneWallTorchBlock>> UNWAXED_WALL_REDSTONE_TTORCH_LOOKUP =
        DefinitionUtils.createImmutableEnumMap(OxidizeState.class, map ->
            {
                map.put(OxidizeState.NORMAL, TEMPORARY_REDSTONE_WALL_TORCH);
                map.put(OxidizeState.EXPOSED, EXPOSED_TEMPORARY_REDSTONE_WALL_TORCH);
                map.put(OxidizeState.WEATHERED, WEATHERED_TEMPORARY_REDSTONE_WALL_TORCH);
                map.put(OxidizeState.OXIDIZED, OXIDIZED_TEMPORARY_REDSTONE_WALL_TORCH);
            }
        );
    
    private static final Map<OxidizeState, DeferredHolder<Block, TemporaryRedstoneTorchBlock>> WAXED_REDSTONE_TTORCH_LOOKUP = 
        DefinitionUtils.createImmutableEnumMap(OxidizeState.class, map ->
            {
                map.put(OxidizeState.NORMAL, WAXED_TEMPORARY_REDSTONE_TORCH);
                map.put(OxidizeState.EXPOSED, WAXED_EXPOSED_TEMPORARY_REDSTONE_TORCH);
                map.put(OxidizeState.WEATHERED, WAXED_WEATHERED_TEMPORARY_REDSTONE_TORCH);
                map.put(OxidizeState.OXIDIZED, WAXED_OXIDIZED_TEMPORARY_REDSTONE_TORCH);
            }
        );
    
    private static final Map<OxidizeState, DeferredHolder<Block, TemporaryRedstoneWallTorchBlock>> WAXED_WALL_REDSTONE_TTORCH_LOOKUP = 
        DefinitionUtils.createImmutableEnumMap(OxidizeState.class, map ->
            {
                map.put(OxidizeState.NORMAL, WAXED_TEMPORARY_REDSTONE_WALL_TORCH);
                map.put(OxidizeState.EXPOSED, WAXED_EXPOSED_TEMPORARY_REDSTONE_WALL_TORCH);
                map.put(OxidizeState.WEATHERED, WAXED_WEATHERED_TEMPORARY_REDSTONE_WALL_TORCH);
                map.put(OxidizeState.OXIDIZED, WAXED_OXIDIZED_TEMPORARY_REDSTONE_WALL_TORCH);
            }
        );
    
    public static final Map<Boolean, Map<OxidizeState, DeferredHolder<Block, TemporaryRedstoneTorchBlock>>> REDSTONE_TTORCH_LOOKUP = Map.of(
        true, WAXED_REDSTONE_TTORCH_LOOKUP,
        false, UNWAXED_REDSTONE_TTORCH_LOOKUP
    );
    
    public static final Map<Boolean, Map<OxidizeState, DeferredHolder<Block, TemporaryRedstoneWallTorchBlock>>> WALL_REDSTONE_TTORCH_LOOKUP = Map.of(
        true, WAXED_WALL_REDSTONE_TTORCH_LOOKUP,
        false, UNWAXED_WALL_REDSTONE_TTORCH_LOOKUP
    );
    
    //*:=== Helpers
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
    
    private static @NotNull DeferredHolder<Item, ThrowableRedstoneTorchItem> getThrowableRedstoneTorch(@NotNull OxidizeState state, boolean waxed)
    {
        return THROWABLE_TORCH_REGISTER.register(
            "%s%sthrowable_redstone_torch".formatted(waxed ? 
                "waxed_" : "",
                state.equals(OxidizeState.NORMAL) ?
                    "" : "%s_".
                    formatted(state.name().toLowerCase())
            ),
            resourceLocation -> new ThrowableRedstoneTorchItem(state, waxed)
        );
    }
    //endregion
}
