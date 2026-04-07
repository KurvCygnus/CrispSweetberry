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
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry.IBaseCarryAdapterFactory;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry.ICarryBlockAdapterFactory;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry.ICarryBlockEntityAdapterFactory;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry.ICarryEntityAdapterFactory;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableExtensions;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.components.AbstractCarryInteractHandler;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.components.AbstractCarryInteractHandler.HandleResult;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.OperationTask;
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
import net.minecraft.world.Containers;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData.CarryBlockEntityDataHolder;
import static kurvcygnus.crispsweetberry.common.features.carrycrate.core.components.AbstractCarryInteractHandler.OperationType;
import static kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryInteractContextCollection.*;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public enum CarryEngine
{
    INST;
    
    //region Fields
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")//! Its query usage is taken by LISTENER_MAPS.
    private static final HashMap<CarryID, ICarryBlockEntityAdapterFactory<?, ?>> BLOCK_ENTITY_CARRY_LISTENERS = new HashMap<>();
    private static final HashMap<CarryID, ICarryEntityAdapterFactory<?, ?>> ENTITY_CARRY_LISTENERS = new HashMap<>();
    private static final HashMap<CarryID, ICarryBlockAdapterFactory<?, ?>> BLOCK_CARRY_LISTENERS = new HashMap<>();
    
    private static final Map<CarryType, HashMap<CarryID, ? extends IBaseCarryAdapterFactory<?, ?>>> LISTENER_MAPS =
        DefinitionUtils.createImmutableEnumMap(
            CarryType.class,
            map ->
            {
                map.put(CarryType.BLOCK_ENTITY, BLOCK_CARRY_LISTENERS);
                map.put(CarryType.ENTITY, ENTITY_CARRY_LISTENERS);
                map.put(CarryType.BLOCK, BLOCK_CARRY_LISTENERS);
            }
        );
    
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "CARRY_ENGINE");
    //endregion
    
    //region Initialization Data & Engine Persistent Lifecycle
    /**
     * This is the definition of the instantized <u>{@link CarryEngine#LISTENER_MAPS}</u>.<br>
     * <b>It exists to make sure that boxed Carry Crate's data won't get lost</b>.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    private static final class CarryListenerSaveData extends SavedData
    {
        static final String UUID = "uuid";
        static final String ID = "id";
        static final String ENTRIES = "entries";
        static final String DATA = "crispsweetberry_carry_listeners";
        
        private ListTag entries = null;
        
        @Override public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
        {
            final ListTag entryList = new ListTag();
            
            final BiConsumer<CarryID, ICarryRegistry.IBaseCarryAdapterFactory<?, ?>> insertToData =
                (id, ignored) ->
                {
                    final CompoundTag entry = new CompoundTag();
                    entry.putString(ID, id.id());
                    entry.putString(UUID, id.uuid());
                    entryList.add(entry);
                    LOGGER.debug("Added UUID \"{}\", corresponded Adapter Object ID: \"{}\"", id.uuid(), id.id());
                };
            
            LISTENER_MAPS.values().forEach(map -> map.forEach(insertToData));
            
            tag.put(ENTRIES, entryList);
            return tag;
        }
        
        public static @NotNull CarryListenerSaveData get(@NotNull MinecraftServer server)
        {
            //! Explanation: Minecraft saves most world data by dimension.
            //! [[CarryEngine#LISTENER_MAPS]] is expected to be cross-dimensional,
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
                                
                                final Optional<? extends IBaseCarryAdapterFactory<?, ?>> optionalAdapter = CarryRegistryManager.INST.searchFactory(resourceLocation);
                                
                                if(optionalAdapter.isEmpty())
                                {
                                    LOGGER.error("Entry with ID \"{}\" doesn't have a corresponded factory!", resourceLocation);
                                    return;
                                }
                                
                                final ICarryRegistry.IBaseCarryAdapterFactory<?, ?> adapter = optionalAdapter.get();
                                ((HashMap<CarryID, IBaseCarryAdapterFactory<?, ?>>) LISTENER_MAPS.get(adapter.getType())).put(fullID, adapter);
                                
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
        assert data != null;
        
        final CarriableExtensions.TickingContext context = new CarriableExtensions.TickingContext(carryCrate, level, entity, data, carryID.uuid(), slotId);
        
        final int penaltyRate = data.unionData().getPenaltyRate();
        final AtomicReference<AbstractCarryAdapter<?>> adapter = new AtomicReference<>(null);
        
        final Consumer<HashMap<CarryID, ? extends ICarryRegistry.IBaseCarryAdapterFactory<?, ?>>> tickAction =
            map ->
            {
                final @Nullable IBaseCarryAdapterFactory<?, ?> factory = map.get(carryID);
                
                if(factory == null)
                    return;
                
                adapter.set(factory.create(null));
                adapter.get().carryingTick(context);
            };
        
        tickAction.accept(LISTENER_MAPS.get(data.carryType()));
        
        if(adapter.get() == null)//! Due to C/S sync, and also the competitive state between carry operation and map, returning at here can prevent potential NPE.
            return;
        
        final AbstractCarryAdapter<?> carryAdapter = adapter.get();
        
        //! ↓ This has already implicitly checked whether the environment is clientside.
        if(!(entity instanceof ServerPlayer player) || penaltyRate == 0)
            return;
        
        final int currentCounter = Objects.requireNonNullElse(carryCrate.get(CarryCrateRegistries.CARRY_TICK_COUNTER.get()), 0);
        
        if(currentCounter + 1 >= penaltyRate && !player.gameMode.isCreative())
        {
            LOGGER.debug("Tick counter is maxed out, try damaging Crate \"{}\".", carryID);
            
            if(carryCrateItem.hurtAndBreak(carryCrate, (ServerLevel) level, player))
            {
                final BlockPos pos = player.getOnPos();
                
                carryAdapter.onBreak(
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
                carryAdapter instanceof AbstractBlockEntityCarryAdapter<?> blockEntityCarryAdapter &&
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
    
    //? TODO Known issues:
    //? 1. Give crate always gives a new stack, instead of mut itself on single stack case.
    //? 2. Effect Update is weird.
    @SuppressWarnings("unchecked")//! Safe Casting.
    public static @Nullable InteractionResult interact(@NotNull ICarryInteractContext context)
    {
        final AtomicReference<InteractionResult> interactionResultRef = new AtomicReference<>(null);
        
        try(final var handle = LOGGER.pushMarker("INTERACT_START"))
        {
            //region Initialization
            //* Do not split initialization as an independent method, it increases the amount of Context data class, and doesn't have any obvious effect.
            final @Nullable BlockState targetState;
            final @Nullable BlockEntity targetBlockEntity;
            final @Nullable LivingEntity targetEntity;
            final @Nullable UseOnContext useOnContext;
            
            final Level level = context.getLevel();
            final BlockPos interactPos = context.getInteractPos();
            final Optional<Player> optionalPlayer = context.getPlayer();
            final ItemStack carryCrate = context.getCarryCrate();
            final Optional<CarryData> optionalData = context.getCarryData();
            
            LOGGER.debug("Start Interaction. Checking object action.");
            
            final @Nullable CarryType action = switch(context)
            {
                case CarryBlocklikeInteractContext blocklike ->
                {
                    targetState = level.getBlockState(interactPos);
                    targetEntity = null;
                    
                    if(optionalPlayer.isEmpty() || targetState.is(Blocks.VOID_AIR))
                    {
                        handle.changeMarker("UNEXPECTED_INTERACT");
                        LOGGER.debug(
                            "Interaction terminated as \"PASS\". Details: {}",
                            optionalPlayer.
                                map(player -> "Block happens to be null.").
                                orElse("This interaction isn't driven by player.")
                        );
                        
                        targetBlockEntity = null;
                        useOnContext = null;
                        yield null;
                    }
                    
                    useOnContext = blocklike.context();
                    
                    if(optionalData.isPresent())
                    {
                        final CarryData data = optionalData.get();
                        
                        targetBlockEntity = data.unionData() instanceof CarryBlockEntityDataHolder holder ?
                            holder.getType().create(interactPos, holder.getState()) :
                            null;
                        
                        yield data.unionData().getBoundType();
                    }
                    else
                    {
                        targetBlockEntity = context.getLevel().getBlockEntity(interactPos);
                        final CarryType result = targetBlockEntity != null ? CarryType.BLOCK_ENTITY : CarryType.BLOCK;
                        
                        //! As you can see, once the CarryType is BLOCK_ENTITY, "targetBlockEntity" won't be null.
                        assert targetBlockEntity != null;
                        yield validateBlocklikeAction(result, targetBlockEntity, targetState);
                    }
                }
                case CarryEntityInteractContext entity ->
                {
                    targetState = null;
                    targetBlockEntity = null;
                    useOnContext = null;
                    
                    targetEntity = entity.target();
                    yield optionalPlayer.map(player -> CarryType.ENTITY).orElse(null);
                }
            };
            
            if(action == null)
                return null;
            
            handle.changeMarker("ACTION_SELECT");
            LOGGER.debug("Picking {} as the current action.", action.name());
            //endregion
            
            //region Process Logics
            if(level.isClientSide)
            {
                handle.changeMarker("CLIENT_HANDLE");
                LOGGER.debug("Current is client side, returning result as \"SUCCESS\".");
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
            }
            
            final ServerLevel serverLevel = (ServerLevel) level;
            final ServerPlayer serverPlayer = (ServerPlayer) optionalPlayer.get();
            final ItemStack newCrate = copyCrate(carryCrate);
            
            handle.changeMarker("CARRY_ID_QUERY");
            LOGGER.debug("Trying to get the CarryID of this carry crate...");
            
            final @Nullable CarryID carryID = context.getCarryID();
            LOGGER.debug("Got CarryID: \"{}\"", Objects.requireNonNullElse(carryID, "N/A"));
            
            final AbstractCarryInteractHandler handler = action.createHandler(
                serverLevel,
                serverPlayer,
                carryCrate,
                interactPos,
                targetState,
                targetEntity,
                targetBlockEntity,
                useOnContext,
                carryID
            );
            
            final HandleResult result = handler.handle();
            interactionResultRef.set(result.result());
            //endregion
            
            //region Post-Process(Operation Dispatching)
            final var listenerPair = result.getListenerState();
            final var componentPair = result.getComponentState();
            final var targetPair = result.getTargetState();
            
            final List<OperationTask> operations = List.of(
                new OperationTask(
                    listenerPair.left(),
                    listenerPair.right(),
                    state ->
                    {
                        if(state.isDefault())
                            return;
                        
                        LOGGER.debug("Listener data changed. Mark saveData as dirtied.");
                        markDirty(serverLevel);
                    }
                ),
                new OperationTask(
                    componentPair.left(),
                    componentPair.right(),
                    state ->
                    {
                        if(!state.isTrue())
                            return;
                        
                        LOGGER.debug("Trying to insert data into carry crate.");
                        result.data().ifPresent(//* This is referenced by [[CarryOperationContext]] ↓ It is OK to use.
                            data -> giveCrateWithEffect(serverLevel, serverPlayer, data, state, newCrate)
                        );
                    }
                ),
                new OperationTask(
                    targetPair.left(),
                    targetPair.right(),
                    state ->
                    {
                        if(!state.isTrue() || carryCrate.getCount() == 1)
                            return;
                        
                        LOGGER.debug("CarryCrate's count has exceed 1({}). Do shrink action.", carryCrate.getCount());
                        carryCrate.shrink(1);
                    }
                )
            );
            
            handle.changeMarker("OPERATION_CONFIRM");
            for(final OperationTask operation: operations)
            {
                LOGGER.debug("Operation parsed: Type: {}, State: {}. Start executing.", operation.type().name(), operation.state().name());
                CarryOperationExecutor.execute(
                    operation.type(),
                    action,
                    operation.state(),
                    new CarryOperationExecutor.CarryOperationContext(
                        result.data(),
                        result.carryID(),
                        result.blockEntityType(),
                        Optional.ofNullable(targetEntity),
                        (operationType, triState) ->
                        {
                            if(!operationType.equals(OperationType.COMPONENT))
                                return carryCrate;
                            
                            return triState.isTrue() && carryCrate.getCount() > 1 ? newCrate : carryCrate;
                        },
                        (HashMap<CarryID, IBaseCarryAdapterFactory<?, ?>>) LISTENER_MAPS.get(action),
                        serverLevel,
                        interactPos,
                        blockState ->
                        {
                            //! This Function is used by blocklike cases,
                            //! in such a case, useOnContext won't be null.
                            assert useOnContext != null;
                            return new StatedBlockPlaceContext(useOnContext, blockState);
                        },
                        operation.callback()
                    ),
                    //! Here is safe, no value compete.
                    //! Among all operations, only TARGET uses this,
                    //! thus, changing the executions' return type signature is worse than passing a atomic ref.
                    operation.type().equals(OperationType.TARGET) ? interactionResultRef : null
                );
            }
            //endregion
        }
        
        return interactionResultRef.get();
    }
    //endregion
    
    //region Helpers
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
    
    private static void giveCrateWithEffect(@NotNull ServerLevel level, @NotNull ServerPlayer player, @NotNull CarryData data, @NotNull TriState state, @NotNull ItemStack newCrate)
    {
        OverweightEffect.updateFactorAndEffect(
            player,
            data,
            state,
            () ->
            {
                if(player.getInventory().add(newCrate))
                    return;
                
                final BlockPos pos = player.getOnPos();
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), newCrate);
            }
        );
    }
    
    private static void markDirty(@NotNull ServerLevel level) { CarryListenerSaveData.get(level.getServer()).setDirty(); }
    
    private static @NotNull ItemStack copyCrate(@NotNull ItemStack itemStack)
    {
        final ItemStack newCrate = new ItemStack(CarryCrateRegistries.CARRY_CRATE_ITEM.value());
        
        if(itemStack.has(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()))
            newCrate.set(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get(), itemStack.get(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()));
        
        return newCrate;
    }
    //endregion
}
