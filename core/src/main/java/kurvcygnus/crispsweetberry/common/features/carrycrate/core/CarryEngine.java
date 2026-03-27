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
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryBlockPlaceContext;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.common.features.carrycrate.self.CarryCrateItem;
import kurvcygnus.crispsweetberry.common.features.carrycrate.self.OverweightEffect;
import kurvcygnus.crispsweetberry.utils.data.Tuple;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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

import static kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData.*;
import static kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryInteractContextCollection.*;
import static kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils.throwIf;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public enum CarryEngine
{
    INST;
    
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")//! Its query usage is taken by LISTENER_MAPS.
    private static final HashMap<CarryID, ICarryBlockEntityAdapterFactory<?, ?>> BLOCK_ENTITY_CARRY_LISTENERS = new HashMap<>();
    private static final HashMap<CarryID, ICarryEntityAdapterFactory<?, ?>> ENTITY_CARRY_LISTENERS = new HashMap<>();
    private static final HashMap<CarryID, ICarryBlockAdapterFactory<?, ?>> BLOCK_CARRY_LISTENERS = new HashMap<>();
    
    private static final Map<CarryType, HashMap<CarryID, ? extends ICarryRegistry.IBaseCarryAdapterFactory<?, ?>>> LISTENER_MAPS =
        CrispDefUtils.createImmutableEnumMap(
            CarryType.class,
            map ->
            {
                map.put(CarryType.BLOCK_ENTITY, BLOCK_CARRY_LISTENERS);
                map.put(CarryType.ENTITY, ENTITY_CARRY_LISTENERS);
                map.put(CarryType.BLOCK, BLOCK_CARRY_LISTENERS);
            }
        );
    
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "CARRY_ENGINE");
    
    static final class CarryListenerSaveData extends SavedData
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
        
        public static @NotNull CarryListenerSaveData get(@NotNull ServerLevel level)
        {
            final DimensionDataStorage storage = level.getDataStorage();
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
    
    @SubscribeEvent static void startEngine(@NotNull ServerStartedEvent event)
    {
        try(var handle = LOGGER.pushMarker("CARRY_INIT"))
        {
            LOGGER.debug("Cleaning listeners' cache...");
            BLOCK_ENTITY_CARRY_LISTENERS.clear();
            BLOCK_CARRY_LISTENERS.clear();
            ENTITY_CARRY_LISTENERS.clear();
            LOGGER.debug("Clean completed.");
            
            final CarryListenerSaveData data = CarryListenerSaveData.get(event.getServer().overworld());
            handle.changeMarker("CARRY_DATA_RECOVER");
            
            LOGGER.when(data.getEntries().isPresent()).debug("SavedData acquired. Continue to recover listeners.");
                
            data.getEntries().ifPresent(
                listTag -> listTag.stream().
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
                            
                            final var optionalAdapter = CarryRegistryManager.INST.searchFactory(resourceLocation);
                            
                            if(optionalAdapter.isEmpty())
                            {
                                LOGGER.error("Entry with ID \"{}\" doesn't have a corresponded factory!", resourceLocation);
                                return;
                            }
                            
                            final ICarryRegistry.IBaseCarryAdapterFactory<?, ?> adapter = optionalAdapter.get();
                            
                            switch(adapter)
                            {
                                case ICarryRegistry.ICarryBlockAdapterFactory<?, ?> blockAdapterFactory ->
                                    BLOCK_CARRY_LISTENERS.put(fullID, blockAdapterFactory);
                                case ICarryRegistry.ICarryBlockEntityAdapterFactory<?, ?> blockEntityAdapterFactory ->
                                    BLOCK_ENTITY_CARRY_LISTENERS.put(
                                        fullID,
                                        blockEntityAdapterFactory
                                    );
                                case ICarryRegistry.ICarryEntityAdapterFactory<?, ?> entityAdapterFactory ->
                                    ENTITY_CARRY_LISTENERS.put(fullID, entityAdapterFactory);
                                case null, default -> throw new IllegalStateException(
                                    "This is an unachievable case: Adapter \"%s\" doesn't belongs to any type that exists!".
                                    formatted(optionalAdapter.get())
                                );
                            }
                            
                            LOGGER.debug("Recovered a {} listener with ID: {}.", adapter.getType(), fullID);
                        }
                    )
            );
            
            handle.changeMarker("CARRY_ENGINE_STARTED");
            LOGGER.debug("Listeners recovered. Carry engine, start!");
        }
    }
    
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
        assert carryID != null;//! carryCrate#has() has granted the safety.
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
                    false
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
    
    @SuppressWarnings("unchecked")//! Safe Casting.
    public static @Nullable InteractionResult interact(@NotNull ICarryInteractContext context)
    {
        final AtomicReference<InteractionResult> interactionResultRef = new AtomicReference<>(null);
        
        try(final var handle = LOGGER.pushMarker("INTERACT_START"))
        {
            final @Nullable BlockState targetState;
            final @Nullable BlockEntity targetBlockEntity;
            final @Nullable LivingEntity targetEntity;
            final @Nullable UseOnContext useOnContext;
            
            final Level level = context.getLevel();
            final BlockPos interactPos = context.getInteractPos();
            final var optionalPlayer = context.getPlayer();
            final ItemStack carryCrate = context.getCarryCrate();
            final Optional<CarryData> optionalData = context.getCarryData();
            
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
                        
                        if(data.unionData() instanceof CarryBlockEntityDataHolder holder)
                            targetBlockEntity = holder.getType().create(interactPos, holder.getState());
                        else
                            targetBlockEntity = null;
                        
                        yield data.unionData().getBoundType();
                    }
                    else
                    {
                        targetBlockEntity = context.getLevel().getBlockEntity(interactPos);
                        yield targetBlockEntity != null ? CarryType.BLOCK_ENTITY : CarryType.BLOCK;
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
                return InteractionResult.PASS;
            
            handle.changeMarker("ACTION_SELECT");
            LOGGER.debug("Picking {} as the current action.", action.name());
            
            final @Nullable Object factoryKey = switch(action)
            {
                case BLOCK_ENTITY ->
                {
                    //! Obviously, only when targetBlockEntity is not null, the type will be BLOCK_ENTITY.
                    assert targetBlockEntity != null;
                    yield targetBlockEntity.getType();
                }
                case BLOCK -> targetState.getBlock();
                case ENTITY -> null;
            };
            
            //? FIX: Override stuff.
            if(!action.equals(CarryType.ENTITY) && optionalData.isEmpty() && CarryRegistryManager.INST.searchFactory(action, factoryKey).isEmpty())
            {
                handle.changeMarker("INVALID_ATTEMPT");
                LOGGER.debug("{} \"{}\" is not supported by carry crate. Skipped.", action.name(), factoryKey.toString());
                return null;
            }
            
            if(level.isClientSide)
            {
                handle.changeMarker("CLIENT_HANDLE");
                LOGGER.debug("Current is client side, returning result as \"SUCCESS\".");
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
            }
            
            final ServerLevel serverLevel = (ServerLevel) level;
            final ServerPlayer serverPlayer = (ServerPlayer) optionalPlayer.get();
            
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
            
            final var listenerPair = result.getListenerState();
            final var componentPair = result.getComponentState();
            final var targetPair = result.getTargetState();
            
            final List<Tuple<HandleResult.OperationType, TriState, Consumer<TriState>>> operations = List.of(
                new Tuple<>(
                    listenerPair.left(),
                    listenerPair.right(),
                    state ->
                    {
                        if(state.isDefault())
                            return;
                        
                        markDirty(serverLevel);
                    }
                ),
                new Tuple<>(
                    componentPair.left(),
                    componentPair.right(),
                    state ->
                    {
                        if(state.isDefault())
                            return;
                        
                        //? TODO
                    }
                ),
                new Tuple<>(targetPair.left(), targetPair.right(), state -> {})
            );
            
            handle.changeMarker("OPERATION_CONFIRM");
            for(final Tuple<HandleResult.OperationType, TriState, Consumer<TriState>> operation: operations)
            {
                LOGGER.debug("Operation parsed: Type: {}, State: {}. Start executing.", operation.left().name(), operation.middle().name());
                CarryOperationExecutor.execute(
                    operation.left(),
                    action,
                    operation.middle(),
                    new CarryOperationExecutor.CarryOperationContext(
                        result.data(),
                        result.carryID(),
                        result.blockEntityType(),
                        Optional.ofNullable(targetEntity),
                        carryCrate,
                        (HashMap<CarryID, IBaseCarryAdapterFactory<?, ?>>) LISTENER_MAPS.get(action),
                        serverLevel,
                        interactPos,
                        blockState ->
                        {
                            //! This Function is used by blocklike cases,
                            //! in such a case, useOnContext won't be null.
                            assert useOnContext != null;
                            return new CarryBlockPlaceContext(useOnContext, blockState);
                        },
                        operation.right()
                    ),
                    //! Here is safe, no value compete.
                    //! Among all operations, only TARGET uses this,
                    //! thus, changing the executions' signature is worse than passing a atomic ref.
                    operation.left().equals(HandleResult.OperationType.TARGET) ? interactionResultRef : null
                );
            }
        }
        
        return interactionResultRef.get();
    }
    
    /**
     * @deprecated <span style="color: red">Too shit, and bloat.</span>
     */
    @SuppressWarnings("unchecked")//! See line 410.
    @Deprecated(forRemoval = true)
    public static @Nullable InteractionResult interactOnBlock(@NotNull UseOnContext context)
    {
        //region Refactored
        final CarryType action;
        
        try(final MarkLogger.MarkerHandle handle = LOGGER.pushMarker("BLOCK_INTERACT"))
        {
            final @Nullable Player player = context.getPlayer();
            final BlockPos pos = context.getClickedPos();
            final Level level = context.getLevel();
            final BlockState state = level.getBlockState(pos);
            
            if(player == null || state.is(Blocks.VOID_AIR))
            {
                handle.changeMarker("UNEXPECTED_INTERACT");
                LOGGER.debug(
                    "Interaction terminated as \"PASS\". Details: {}",
                    player == null ?
                        "This interaction isn't driven by player." :
                        "Block happens to be null."
                );
                
                return InteractionResult.PASS;
            }
            
            final ItemStack carryCrate = context.getItemInHand();
            final @Nullable CarryData data = carryCrate.get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
            final @Nullable BlockEntity blockEntity;
            
            if(data != null)
                switch(data.unionData())
                {
                    case CarryBlockEntityDataHolder blockEntityDataHolder ->
                    {
                        blockEntity = blockEntityDataHolder.getType().create(pos, blockEntityDataHolder.getState());
                        action = CarryType.BLOCK_ENTITY;
                    }
                    case CarryBlockDataHolder ignored ->
                    {
                        blockEntity = null;
                        action = CarryType.BLOCK;
                    }
                    case CarryEntityDataHolder ignored ->
                    {
                        blockEntity = null;
                        action = CarryType.ENTITY;
                    }
                }
            else
            {
                blockEntity = context.getLevel().getBlockEntity(pos);
                action = blockEntity != null ? CarryType.BLOCK_ENTITY : CarryType.BLOCK;
            }
            
            handle.changeMarker("ACTION_SELECT");
            LOGGER.debug("Picking {} as the current action.", action.name());
            
            if(
                action == CarryType.BLOCK_ENTITY &&
                //!                                                     ↓ Impossible to be null, granted by enum "CarryType".
                CarryRegistryManager.INST.getBlockEntityAdapter(Objects.requireNonNull(blockEntity).getType()).isEmpty() &&
                !carryCrate.has(CarryCrateRegistries.CARRY_CRATE_DATA.get())
            )
            {
                LOGGER.debug("BlockEntity \"{}\" is not supported by carry crate. Skipped.", blockEntity.toString());
                return null;
            }
            else if(
                action == CarryType.BLOCK &&
                CarryRegistryManager.INST.getBlockAdapter(state.getBlock()).isEmpty() &&
                !carryCrate.has(CarryCrateRegistries.CARRY_CRATE_DATA.get())
            )
            {
                LOGGER.debug("Block \"{}\" is not supported by carry crate. Skipped.", state.getBlock().getDescriptionId());
                return null;
            }
            
            if(level.isClientSide)
            {
                LOGGER.debug("Current is client side, returning result as \"SUCCESS\".");
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
            }
            
            final ServerLevel serverLevel = (ServerLevel) level;
            
            handle.changeMarker("CARRY_ID_QUERY");
            LOGGER.debug("Trying to get the CarryID of this carry crate...");
            
            final @Nullable CarryID carryID = carryCrate.get(CarryCrateRegistries.CARRY_ID.get());
            LOGGER.debug("Got CarryID: \"{}\"", Objects.requireNonNullElse(carryID, "N/A"));
            
            final AbstractCarryInteractHandler handler = action.createHandler(
                serverLevel,
                (ServerPlayer) context.getPlayer(),
                carryCrate,
                pos,
                state,
                null,
                blockEntity,
                context,
                carryID
            );
            
            final AbstractCarryInteractHandler.HandleResult result = handler.handle();
            
            LOGGER.debug("Received result: %s".formatted(result));
            
            final Optional<CarryData> optionalData = result.data();
            final Optional<CarryID> optionalCarryID = result.carryID();
            InteractionResult finalResult = result.result();
            final ItemStack newCrate = copyCrate(carryCrate);
            
            handle.changeMarker("ACTION_CONFIRM");
            LOGGER.debug(
                "Confirmed sequence actions: Listener: {}, Target: {}, Component: {}",
                result.getListenerState().left().name(),
                result.getTargetState().left().name(),
                result.getComponentState().left().name()
            );
            
            if(result.shouldTakeTarget() && !Objects.equals(action, CarryType.ENTITY))
            {
                serverLevel.setBlockAndUpdate(pos, Blocks.VOID_AIR.defaultBlockState());
                serverLevel.playSound(null, pos, SoundEvents.SCAFFOLDING_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
                carryCrate.shrink(1);
            }
            else if(result.shouldReleaseTarget() && optionalData.isPresent())
            {
                if(!action.equals(CarryType.ENTITY))
                {
                    final CarryBlockPlaceContext placeContext = getCarryBlockPlaceContext(context, action, optionalData.get());
                    finalResult = placeContext.performPlace();
                    
                    result.blockEntityType().ifPresent(
                        blockEntityType ->
                        {
                            final CarryBlockEntityDataHolder blockEntityDataHolder = optionalData.get().unionData();
                            
                            final BlockEntity blockEntityToPlace = Objects.requireNonNull(
                                blockEntityType.create(placeContext.getClickedPos(), blockEntityDataHolder.getState()),
                                """
                                    Fatal:
                                    Failed to create blockEntity "%s"'s adapter. This usually means the blockEntity's type registration itself has dataflow issue, or this
                                    method is called at improper time.
                                    
                                    %s
                                    """.
                                    formatted(
                                        blockEntityType.toString(),
                                        MiscConstants.FEEDBACK_MESSAGE
                                    )
                            );
                            
                            blockEntityToPlace.loadCustomOnly(blockEntityDataHolder.getTagData(), serverLevel.registryAccess());
                        }
                    );
                }
                else
                {
                    final CarryEntityDataHolder entityDataHolder = optionalData.get().unionData();
                    final Optional<Entity> optionalEntity = EntityType.create(entityDataHolder.getTagData(), serverLevel);
                    
                    if(optionalEntity.isPresent())
                    {
                        final Entity entity = optionalEntity.get();
                        entity.moveTo(pos.getX(), pos.getY(), pos.getZ());
                        
                        serverLevel.addFreshEntity(entity);
                        finalResult = InteractionResult.SUCCESS;
                    }
                }
            }
            
            optionalCarryID.ifPresent(
                id ->
                {
                    if(result.shouldAddListener() && optionalData.isPresent() && action != CarryType.ENTITY)
                    {
                        final BiConsumer<HashMap<CarryID, IBaseCarryAdapterFactory<?, ?>>, CarryData> insertAction =
                            (map, carryData) ->
                            {
                                final var factory = CarryRegistryManager.INST.
                                    searchFactory(action, carryData.unionData().getCreationData());
                                
                                if(factory.isEmpty())
                                {
                                    LOGGER.error(
                                        "Cannot find \"{}\"'s adapter factory!",
                                        carryData.unionData().getCreationData().toString()
                                    );
                                    
                                    return;
                                }
                                
                                map.put(id, factory.get());
                            };
                        
                        insertAction.accept(//! ↓ This is safe.
                            (HashMap<CarryID, IBaseCarryAdapterFactory<?, ?>>) LISTENER_MAPS.get(action),
                            optionalData.get()
                        );
                    }
                    else if(result.shouldRemoveListener())
                    {
                        markDirty(serverLevel);
                        LISTENER_MAPS.get(action).remove(id);
                    }
                }
            );
            
            tryInsertData(carryCrate, result, optionalData, newCrate, optionalCarryID);
            
            optionalData.ifPresent(carryData -> tryGiveCrate(result, player, carryData, newCrate, serverLevel));
            
            return finalResult;
        }
    }
    
    /**
     * @deprecated <span style="color: red">Too shit, and bloat.</span>
     */
    @Deprecated(forRemoval = true)
    public static @NotNull InteractionResult interactOnEntity(
        @NotNull ItemStack carryCrate,
        @NotNull Player player,
        @NotNull LivingEntity interactionTarget
    )
    {
        if(carryCrate.has(CarryCrateRegistries.CARRY_CRATE_DATA.get()) || CarryRegistryManager.INST.getEntityAdapter(interactionTarget.getType()).isEmpty())
        {
            LOGGER.debug("Entity \"{}\" is not supported by carry crate. Skipped.", interactionTarget.toString());
            return InteractionResult.PASS;
        }
        
        final Level level = player.level();
        
        if(level.isClientSide())
        {
            LOGGER.debug("Current is client side, returning result as \"SUCCESS\".");
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        
        final @Nullable CarryID carryID = carryCrate.get(CarryCrateRegistries.CARRY_ID.get());
        final ServerLevel serverLevel = (ServerLevel) level;
        
        final AbstractCarryInteractHandler handler = CarryType.ENTITY.createHandler(
            serverLevel,
            (ServerPlayer) player,
            carryCrate,
            null,
            null,
            interactionTarget,
            null,
            null,
            carryID
        );
        
        final AbstractCarryInteractHandler.HandleResult result = handler.handle();
        final Optional<CarryData> optionalData = result.data();
        final Optional<CarryID> optionalCarryID = result.carryID();
        InteractionResult finalResult = result.result();
        final ItemStack newCrate = copyCrate(carryCrate);
        
        if(result.shouldTakeTarget())
            interactionTarget.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        else if(result.shouldReleaseTarget() && optionalData.isPresent())
        {
            final CarryEntityDataHolder entityDataHolder = optionalData.get().unionData();
            final Optional<Entity> optionalEntity = EntityType.create(entityDataHolder.getTagData(), level);
            
            if(optionalEntity.isPresent())
            {
                final Entity entity = optionalEntity.get();
                final BlockPos pos = interactionTarget.getOnPos();
                
                entity.moveTo(pos.getX(), pos.getY(), pos.getZ());
                
                level.addFreshEntity(entity);
                finalResult = InteractionResult.SUCCESS;
            }
        }
        
        optionalCarryID.ifPresent(
            id ->
            {
                if(result.shouldAddListener() && optionalData.isPresent())
                {
                    final CarryEntityDataHolder entityDataHolder = optionalData.get().unionData();
                    final var optionalFactory =
                        CarryRegistryManager.INST.getEntityAdapter(entityDataHolder.getType());
                    
                    optionalFactory.ifPresent(
                        factory ->
                        {
                            ENTITY_CARRY_LISTENERS.put(id, factory);
                            markDirty((ServerLevel) level);
                        }
                    );
                    
                    LOGGER.when(optionalFactory.isEmpty()).error(
                        "Cannot find entity \"{}\"'s factory!",
                        interactionTarget.toString()
                    );
                }
                else if(result.shouldRemoveListener())
                {
                    markDirty((ServerLevel) level);
                    ENTITY_CARRY_LISTENERS.remove(id);
                }
            }
        );
        
        tryInsertData(carryCrate, result, optionalData, newCrate, optionalCarryID);
        
        optionalData.ifPresent(data -> tryGiveCrate(result, player, data, newCrate, serverLevel));
        
        return finalResult;
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")//! Optional makes logic clearer here.
    @Deprecated(forRemoval = true)
    private static void tryInsertData(
        @NotNull ItemStack carryCrateStack,
        @NotNull HandleResult result,
        Optional<CarryData> optionalData,
        @NotNull ItemStack newCrate,
        Optional<CarryID> optionalCarryID
    )
    {
        if(!result.shouldTakeTarget() && !result.shouldReleaseTarget())
            return;
        
        if(result.shouldInsertComponent())
        {
            optionalData.ifPresentOrElse(
                carryData -> insertData(newCrate, CarryCrateRegistries.CARRY_CRATE_DATA.get(), carryData),
                () -> LOGGER.warn("Can't insert CarryData into the item, because it is null!")
            );
            
            optionalCarryID.ifPresentOrElse(
                id -> insertData(newCrate, CarryCrateRegistries.CARRY_ID.get(), id),
                () -> LOGGER.warn("Can't insert CarryID into the item, because it is null!")
            );
        }
        else if(result.shouldRemoveComponent())
        {
            carryCrateStack.remove(CarryCrateRegistries.CARRY_CRATE_DATA.get());
            carryCrateStack.remove(CarryCrateRegistries.CARRY_ID.get());
            carryCrateStack.remove(CarryCrateRegistries.CARRY_TICK_COUNTER.get());
        }
    }
    
    private static void tryGiveCrate(
        @NotNull HandleResult result,
        @NotNull Player player,
        @NotNull CarryData data,
        @NotNull ItemStack newCrate,
        @NotNull ServerLevel serverLevel
    )
    {
        if((!result.shouldReleaseTarget() && !result.shouldTakeTarget()) || (!result.shouldInsertComponent() && !result.shouldRemoveComponent()))
            return;
        
        OverweightEffect.updateFactorAndEffect(
            player,
            data,
            result.shouldTakeTarget(),
            () ->
            {
                if(player.getInventory().add(newCrate))
                    return;
                
                final BlockPos pos = player.getOnPos();
                Containers.dropItemStack(serverLevel, pos.getX(), pos.getY(), pos.getZ(), newCrate);
            }
        );
    }
    
    @Deprecated(forRemoval = true)
    private static @NotNull CarryBlockPlaceContext getCarryBlockPlaceContext(@NotNull UseOnContext context, @NotNull CarryType action, @NotNull CarryData result)
    {
        final BlockState stateToPlace;
        
        switch(action)
        {
            case BLOCK_ENTITY ->
            {
                final CarryBlockEntityDataHolder blockEntityDataHolder = result.unionData();
                stateToPlace = blockEntityDataHolder.getState();
            }
            case BLOCK ->
            {
                final CarryBlockDataHolder blockDataHolder = result.unionData();
                stateToPlace = blockDataHolder.getState();
            }
            default -> throw new IllegalStateException(
                "Assertion error: Block interaction ended up getting unexpected action. %s".
                    formatted(MiscConstants.FEEDBACK_MESSAGE)
            );
        }
        
        return new CarryBlockPlaceContext(context, stateToPlace);
    }
    
    private static void markDirty(@NotNull ServerLevel level) { CarryListenerSaveData.get(level.getServer().overworld()).setDirty(); }
    
    private static @NotNull ItemStack copyCrate(@NotNull ItemStack itemStack)
    {
        final ItemStack newCrate = new ItemStack(CarryCrateRegistries.CARRY_CRATE_ITEM.value());
        
        if(itemStack.has(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()))
            newCrate.set(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get(), itemStack.get(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()));
        
        return newCrate;
    }
    
    @Deprecated(forRemoval = true)
    private static <T> void insertData(@NotNull ItemStack stack, @NotNull DataComponentType<T> type, @NotNull T data)
    {
        throwIf(
            stack.isEmpty() || !stack.is(CarryCrateRegistries.CARRY_CRATE_ITEM.value()),
            "Param \"stack\" must be \"%s:carry_crate\"!".formatted(CrispSweetberry.NAMESPACE),
            IllegalArgumentException::new
        );
        
        throwIf(
            stack.getCount() < 1,
            "Param \"stack\"'s count should be an positive integer!",
            IllegalArgumentException::new
        );
        
        if(stack.getCount() == 1)
        {
            stack.set(type, data);
            return;
        }
        
        stack.shrink(1);
        final ItemStack newStack = stack.copy();
        newStack.setCount(1);
        newStack.set(type, data);
    }
}
