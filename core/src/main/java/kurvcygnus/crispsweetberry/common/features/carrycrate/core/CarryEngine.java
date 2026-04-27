//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.AbstractCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryType;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistryView;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistryView.IBaseCarryAdapterFactory;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistryView.ICarryBlockAdapterFactory;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistryView.ICarryBlockEntityAdapterFactory;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistryView.ICarryEntityAdapterFactory;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableExtensions;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.components.AbstractCarryInteractHandler;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.components.AbstractCarryInteractHandler.HandleResult;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryPipelineTask;
import kurvcygnus.crispsweetberry.common.features.carrycrate.self.CarryCrateItem;
import kurvcygnus.crispsweetberry.common.features.carrycrate.self.OverweightEffect;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import kurvcygnus.crispsweetberry.utils.base.extension.StatedBlockPlaceContext;
import kurvcygnus.crispsweetberry.utils.core.log.MarkLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import static kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData.CarryBlockEntityDataHolder;
import static kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryInteractContextCollection.*;

//? TODO: Take Middle Click Copy into account, that shit breaks the uniqueness of [[CarryID]].
//? FIX: Somehow, when capturing entity with one carryCrate, component persistent won't work.
//? According to debugging, this bug doesn't happen before the end of [[CarryCrateItem#interactLivingEntity]]. SHIT, Minecraft's code is awesome.
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public enum CarryEngine
{
    INST;
    
    //region Fields
    private static final HashMap<CarryID, ICarryBlockEntityAdapterFactory<?, ?>> BLOCK_ENTITY_CARRY_LISTENERS = new HashMap<>();
    private static final HashMap<CarryID, ICarryEntityAdapterFactory<?, ?>> ENTITY_CARRY_LISTENERS = new HashMap<>();
    private static final HashMap<CarryID, ICarryBlockAdapterFactory<?, ?>> BLOCK_CARRY_LISTENERS = new HashMap<>();
    
    private static final Map<CarryType, HashMap<CarryID, ? extends IBaseCarryAdapterFactory<?, ?>>> LISTENER_LOOKUP =
        DefinitionUtils.createImmutableEnumMapWithCheck(
            CarryType.class,
            map ->
            {
                map.put(CarryType.BLOCK_ENTITY, BLOCK_ENTITY_CARRY_LISTENERS);
                map.put(CarryType.ENTITY, ENTITY_CARRY_LISTENERS);
                map.put(CarryType.BLOCK, BLOCK_CARRY_LISTENERS);
            }
        );
    
    private static final ThreadLocal<Boolean> IS_INTERACTING_WITH_BE = ThreadLocal.withInitial(() -> false);
    
    private static final MarkLogger LOGGER = MarkLogger.marklessLogger(LogUtils.getLogger());
    //endregion
    
    //region Initialization Data & Engine Persistent Lifecycle
    /**
     * This is the definition of the instantized <u>{@link CarryEngine#LISTENER_LOOKUP}</u>.<br>
     * <b>It exists to make sure that boxed Carry Crate's data won't get lost</b>.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    static final class CarryListenerSaveData extends SavedData
    {
        static final String UUID = "uuid";
        static final String ID = "id";
        static final String ENTRIES = "entries";
        static final String DATA = "crispsweetberry_carry_listeners";
        
        private ListTag entries = null;
        
        @Override public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
        {
            try(final var ignored = LOGGER.pushMarker("PERSISTENT"))
            {
                final ListTag entryList = new ListTag();
                
                final BiConsumer<CarryID, ICarryRegistryView.IBaseCarryAdapterFactory<?, ?>> insertToData =
                    (id, $) ->
                    {
                        final CompoundTag entry = new CompoundTag();
                        entry.putString(ID, id.id());
                        entry.putString(UUID, id.uuid());
                        entryList.add(entry);
                        LOGGER.debug("Added UUID \"{}\", corresponded Adapter Object ID: \"{}\"", id.uuid(), id.id());
                    };
                
                LISTENER_LOOKUP.values().forEach(map -> map.forEach(insertToData));
                
                tag.put(ENTRIES, entryList);
            }
            return tag;
        }
        
        public static @NotNull CarryListenerSaveData get(@NotNull MinecraftServer server)
        {
            //! Explanation: Minecraft saves most world data by dimension.
            //! [[CarryEngine#LISTENER_LOOKUP]] is expected to be cross-dimensional,
            //! and in such a case, we choose to use [[Level#OVERWORLD]] as standard.
            final DimensionDataStorage storage = server.overworld().getDataStorage();
            
            final SavedData.Factory<CarryListenerSaveData> factory = new Factory<>(
                CarryListenerSaveData::new,
                CarryListenerSaveData::load
            );
            
            return storage.computeIfAbsent(factory, DATA);
        }
        
        private static @NotNull CarryListenerSaveData load(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
        {
            final CarryListenerSaveData data = new CarryListenerSaveData();
            data.entries = tag.getList(ENTRIES, 10);
            
            return data;
        }
        
        public @NotNull Optional<ListTag> getEntries() { return Optional.ofNullable(entries); }
    }
    
    @SuppressWarnings("unchecked")//! Unsafe casting, however, with the restrict of enum [[CarryType]], it is actually safe.
    @SubscribeEvent static void startEngine(@NotNull ServerStartedEvent event)
    {
        try(var handle = LOGGER.pushMarker("CARRY_INIT"))
        {
            LOGGER.debug("Cleaning listeners' cache...");
            BLOCK_ENTITY_CARRY_LISTENERS.clear();
            BLOCK_CARRY_LISTENERS.clear();
            ENTITY_CARRY_LISTENERS.clear();
            LOGGER.debug("Clean completed.");
            
            final CarryListenerSaveData data = CarryListenerSaveData.get(event.getServer());
            handle.changeMarker("CARRY_DATA_RECOVER");
            
            data.getEntries().ifPresent(
                listTag ->
                {
                    LOGGER.debug("SavedData acquired. Continue to recover listeners.");
                    listTag.stream().
                        filter(CompoundTag.class::isInstance).
                        map(CompoundTag.class::cast).
                        forEach(
                            entryTag ->
                            {
                                final String id = entryTag.getString(CarryListenerSaveData.ID);
                                final String uuid = entryTag.getString(CarryListenerSaveData.UUID);
                                final CarryID fullID = new CarryID(id, uuid);
                                LOGGER.debug("Got CarryID: [ResourceLocation: \"{}\", UUID: \"{}\"]", id, uuid);
                                
                                final ResourceLocation resourceLocation = ResourceLocation.parse(id);
                                final @Nullable var adapter = CarryRegistryManager.INST.searchFactory(resourceLocation);
                                
                                if(adapter == null)
                                {
                                    LOGGER.error("Entry with ID \"{}\" doesn't have a corresponded factory!", resourceLocation);
                                    return;
                                }
                                
                                ((HashMap<CarryID, IBaseCarryAdapterFactory<?, ?>>) LISTENER_LOOKUP.get(adapter.getType())).put(fullID, adapter);
                                
                                LOGGER.debug("Recovered a {} listener with ID: {}.", adapter.getType().name(), fullID);
                            }
                        );
                }
            );
            
            handle.changeMarker("CARRY_ENGINE_STARTED");
            LOGGER.debug("Listeners recovered. Carry engine, start!");
        }
    }
    //endregion
    
    //region Carry Core Logics
    public static void carryingTick(
        @NotNull CarryCrateItem carryCrateItem,
        @NotNull ItemStack carryCrate,
        @NotNull Level level,
        @NotNull Entity entity,
        int slotId
    )
    {
        if(
            !(carryCrate.getItem() instanceof CarryCrateItem) ||
            !carryCrate.has(CarryCrateRegistries.CARRY_ID.get()) ||
            !carryCrate.has(CarryCrateRegistries.CARRY_CRATE_DATA.get())
        ) return;
        
        final CarryID carryID = carryCrate.get(CarryCrateRegistries.CARRY_ID.get());
        final CarryData data = carryCrate.get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        assert carryID != null;//! [[DataComponentHolder#has()]] has granted the safety.
        assert data != null;//! `assert` doesn't work in non-debugging environment, it won't bring any extra performance penalty comparing to [[Objects#requireNonNull]].
        
        final var context = new CarriableExtensions.TickingContext(carryCrate, level, entity, data, carryID.uuid(), slotId);
        
        final int penaltyRate = data.unionData().getPenaltyRate();
        final var adapter = getCarryAdapter(LISTENER_LOOKUP.get(data.carryType()), carryID);
        
        if(adapter == null)//! Due to C/S sync, and also the competitive state between carry operation and map, returning at here can prevent potential NPE.
            return;
        
        adapter.carryingTick(context);
        
        //! ↓ This has already implicitly checked whether the environment is clientside.
        if(!(entity instanceof ServerPlayer player) || penaltyRate == 0)
            return;
        
        final int currentCounter = Objects.requireNonNullElse(carryCrate.get(CarryCrateRegistries.CARRY_TICK_COUNTER.get()), 0);
        
        if(currentCounter + 1 >= penaltyRate && !player.gameMode.isCreative())
        {
            if(carryCrateItem.hurtAndBreak(carryCrate, (ServerLevel) level, player))
            {
                final BlockPos pos = player.getOnPos();
                
                adapter.onBreak(
                    level,
                    pos,
                    data.unionData(),
                    level.getGameTime() - data.startTime()
                );
                
                OverweightEffect.updateFactorAndEffect(
                    player,
                    data,
                    TriState.FALSE
                );
                
                carryCrate.remove(CarryCrateRegistries.CARRY_CRATE_DATA.get());
                carryCrate.remove(CarryCrateRegistries.CARRY_ID.get());
            }
            
            if(
                data.carryType().equals(CarryType.BLOCK_ENTITY) &&
                adapter instanceof AbstractBlockEntityCarryAdapter<?> blockEntityCarryAdapter &&
                level.getRandom().nextFloat() < (float) carryCrate.getDamageValue() / carryCrate.getMaxDamage()
            )
                carryCrate.set(
                    CarryCrateRegistries.CARRY_CRATE_DATA.get(),
                    blockEntityCarryAdapter.onPenaltyDrop(context)
                );
            
            carryCrate.remove(CarryCrateRegistries.CARRY_TICK_COUNTER.get());
            return;
        }
        
        carryCrate.set(CarryCrateRegistries.CARRY_TICK_COUNTER.get(), currentCounter + 1);
    }
    
    @SuppressWarnings("unchecked")//! Safe Casting.
    public static @Nullable InteractionResult interact(@NotNull ICarryInteractContext context)
    {
        try(final var handle = LOGGER.pushMarker("INTERACT_INIT"))
        {
            //region Initialization
            //* Do not split initialization as an independent method, it increases the amount of Context data class, and doesn't have any obvious effect.
            final @Nullable BlockState targetBlockState;
            final @Nullable BlockEntity targetBlockEntity;
            final @Nullable LivingEntity targetEntity;
            final @Nullable UseOnContext useOnContext;
            
            final Level level = context.getLevel();
            final BlockPos interactPos = context.getInteractPos();
            final Optional<Player> optionalPlayer = context.getPlayer();
            final ItemStack carryCrate = context.getCarryCrate();
            final @Nullable CarryData carryData = context.getCarryData();
            
            LOGGER.when(!level.isClientSide).debug(
                "State of this interaction: Player: {}, Data: {}",
                optionalPlayer.map(player -> player.getDisplayName().getString()).orElse("N/A"),
                carryData != null ? carryData.toString() : "N/A"
            );
            
            final @Nullable CarryType action = switch(context)
            {
                case CarryBlocklikeInteractContext blocklike ->
                {
                    //? TODO: Edit needed.
                    targetBlockState = level.getBlockState(interactPos);
                    
                    if(optionalPlayer.isEmpty() || targetBlockState.is(Blocks.VOID_AIR))
                    {
                        handle.changeMarker("UNEXPECTED_INTERACT");
                        LOGGER.debug(
                            "Interaction terminated as \"PASS\". Details: {}",
                            optionalPlayer.
                                map(player -> "Block happens to be null.").
                                orElse("This interaction isn't driven by player.")
                        );
                        
                        targetEntity = null;
                        targetBlockEntity = null;
                        useOnContext = null;
                        yield null;
                    }
                    
                    useOnContext = blocklike.context();
                    
                    yield switch(carryData)
                    {
                        case null ->
                        {
                            targetEntity = null;
                            
                            targetBlockEntity = context.getLevel().getBlockEntity(interactPos);
                            final CarryType result = targetBlockEntity != null ? CarryType.BLOCK_ENTITY : CarryType.BLOCK;
                            
                            //! As you can see, once the [[CarryType]] is BLOCK_ENTITY, "targetBlockEntity" won't be null.
                            assert targetBlockEntity != null : "UwU";
                            yield validateBlocklikeAction(result, targetBlockEntity, targetBlockState);
                        }
                        case CarryData that when that.unionData() instanceof CarryData.CarryEntityDataHolder holder ->
                        {
                            targetEntity = (LivingEntity) holder.getType().create(level);
                            targetBlockEntity = null;
                            yield CarryType.ENTITY;
                        }
                        default ->
                        {
                            targetEntity = null;
                            
                            if(carryData.unionData() instanceof CarryBlockEntityDataHolder holder)
                            {
                                IS_INTERACTING_WITH_BE.set(true);
                                targetBlockEntity = holder.getType().create(interactPos, holder.getState());
                            }
                            else
                                targetBlockEntity = null;
                            
                            yield carryData.unionData().getBoundType();
                        }
                    };
                }
                case CarryEntityInteractContext entity ->
                {
                    targetBlockState = null;
                    targetBlockEntity = null;
                    useOnContext = null;
                    
                    targetEntity = entity.target();
                    //! If this interaction attempt doesn't have a owner, Engine should refuse to process the following logics.
                    yield optionalPlayer.map($ -> CarryType.ENTITY).orElse(null);
                }
            };
            
            switch(action)
            {
                case null -> { return null; }
                case BLOCK_ENTITY -> IS_INTERACTING_WITH_BE.set(true);
                default -> {}
            }
            
            handle.changeMarker("ACTION_SELECT");
            LOGGER.when(!level.isClientSide).debug("Current action: {}.", action.name());
            //endregion
            
            //region Process Logics
            if(level.isClientSide)
            {
                handle.changeMarker("CLIENT_HANDLE");
                LOGGER.debug("Current is client side, returning result as \"SUCCESS_NO_ITEM_USED\".");
                return InteractionResult.SUCCESS_NO_ITEM_USED;//! Using SUCCESS, or [[InteractionResult#sidedSuccess]] may lead to [[ItemStack#shrink]].
            }
            
            final ServerLevel serverLevel = (ServerLevel) level;
            final ServerPlayer serverPlayer = (ServerPlayer) optionalPlayer.get();
            
            handle.changeMarker("CARRY_ID_QUERY");
            final @Nullable CarryID carryID = context.getCarryID();
            LOGGER.debug("Got CarryID: \"{}\"", Objects.requireNonNullElse(carryID, "N/A"));
            
            final AbstractCarryInteractHandler handler = action.createHandler(
                serverLevel,
                serverPlayer,
                carryCrate,
                interactPos,
                targetBlockState,
                targetEntity,
                targetBlockEntity,
                carryID
            );
            
            final HandleResult result = handler.handle();
            //endregion
            
            //region Post-Process
            final var interactResult = CarryOperationExecutor.INST.execute(
                new CarryPipelineTask(
                    action,
                    result.getListenerState(),
                    result.getComponentState(),
                    result.getTargetState(),
                    carryCrate,
                    serverLevel,
                    serverPlayer,
                    interactPos,
                    result.data(),
                    Objects.requireNonNullElse(carryID, result.carryID()),//! If the crate has already own a UUID, we should use it, if not, use the newly generated one.
                    targetEntity,                                         //! Of course, if both are null, this will throw NPE, that's a bug.
                    result.blockEntityType(),
                    ((HashMap<CarryID, IBaseCarryAdapterFactory<?, ?>>) LISTENER_LOOKUP.get(action))::put,
                    LISTENER_LOOKUP.get(action)::remove,
                    useOnContext != null ? state -> new StatedBlockPlaceContext(useOnContext, state) : null,
                    InteractionResult.PASS
                )
            );
            
            if(action.equals(CarryType.BLOCK_ENTITY))
                IS_INTERACTING_WITH_BE.set(false);
            
            return interactResult;
            //endregion
        }
    }
    //endregion
    
    //region API & Helpers
    public boolean isInteracting() { return IS_INTERACTING_WITH_BE.get(); }
    
    private static @Nullable AbstractCarryAdapter<?> getCarryAdapter(
        @NotNull HashMap<CarryID, ? extends ICarryRegistryView.IBaseCarryAdapterFactory<?, ?>> map,
        @NotNull CarryID carryID
    )
    {
        final @Nullable var factory = map.get(carryID);
        return factory != null ? factory.create(null) : null;
    }
    
    private static @Nullable CarryType validateBlocklikeAction(@NotNull CarryType carryType, @NotNull BlockEntity blockEntity, @NotNull BlockState blockState)
    {
        if(CarryRegistryManager.INST.searchFactory(carryType, carryType.equals(CarryType.BLOCK_ENTITY) ? blockEntity.getType() : blockState.getBlock()).isEmpty())
        {
            if(carryType.equals(CarryType.BLOCK_ENTITY))
                return validateBlocklikeAction(CarryType.BLOCK, blockEntity, blockState);
            
            return null;
        }
        
        return carryType;
    }
    //endregion
}
