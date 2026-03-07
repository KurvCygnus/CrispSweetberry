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
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.block.AbstractBlockCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.entity.AbstractEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.AbstractCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableExtensions;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.components.AbstractCarryInteractHandler;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryBlockPlaceContext;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryType;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

//? TODO: Oh shit, I forgot to process container issues, ass
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public enum CarryEngine
{
    INSTANCE;
    
    private static final HashMap<String, ICarryRegistry.ICarryBlockEntityAdapterFactory<? extends BlockEntity, ? extends AbstractBlockEntityCarryAdapter<?>>>
        BLOCK_ENTITY_CARRY_LISTENERS = new HashMap<>();
    private static final HashMap<String, ICarryRegistry.ICarryEntityAdapterFactory<? extends LivingEntity, ? extends AbstractEntityCarryAdapter<?>>>
        ENTITY_CARRY_LISTENERS = new HashMap<>();
    private static final HashMap<String, ICarryRegistry.ICarryBlockAdapterFactory<? extends Block, ? extends AbstractBlockCarryAdapter<?>>>
        BLOCK_CARRY_LISTENERS = new HashMap<>();
    
    private static final int PENALTY_QUANTITY = 10;
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "CARRY_ENGINE");
    
    static final class CarryListenerSaveData extends SavedData
    {
        static final String UUID = "carryID";
        static final String ID = "id";
        static final String ENTRIES = "entries";
        static final String DATA = "csb_carry_listeners";
        
        private ListTag entries = null;
        
        @Override public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
        {
            final ListTag entryList = new ListTag();
            
            final BiConsumer<String, ICarryRegistry.IBaseCarryAdapterFactory<?, ?>> insertToData = 
                (id, $uwu$) ->
                {
                    final String[] keys = id.split("§");
                    
                    if(keys.length != 2)
                    {
                        LOGGER.error("Invalid CarryID: {}", id);
                        return;
                    }
                    
                    final CompoundTag entry = new CompoundTag();
                    entry.putString(ID, keys[0]);
                    entry.putString(UUID, keys[1]);
                    entryList.add(entry);
                };
            
            BLOCK_ENTITY_CARRY_LISTENERS.forEach(insertToData);
            ENTITY_CARRY_LISTENERS.forEach(insertToData);
            BLOCK_CARRY_LISTENERS.forEach(insertToData);
            
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
    
    @SubscribeEvent static void startEngine(@NotNull ServerAboutToStartEvent event)
    {
        try(var handle = LOGGER.pushMarker("CARRY_INIT"))
        {
            LOGGER.debug("Cleaning listeners' cache...");
            BLOCK_ENTITY_CARRY_LISTENERS.clear();
            BLOCK_CARRY_LISTENERS.clear();
            ENTITY_CARRY_LISTENERS.clear();
            
            final CarryListenerSaveData data = CarryListenerSaveData.get(event.getServer().overworld());
            handle.changeMarker("CARRY_DATA_RECOVER");
            
            LOGGER.debug("SavedData acquired. Continue to recover listeners.");
                
            data.getEntries().ifPresent(
                listTag -> listTag.stream().
                    filter(CompoundTag.class::isInstance).
                    map(CompoundTag.class::cast).
                    forEach(
                        entryTag ->
                        {
                            final String id = entryTag.getString(CarryListenerSaveData.ID);
                            final String uuid = entryTag.getString(CarryListenerSaveData.UUID);
                            final String fullID = "%s§%s".formatted(id, uuid);
                            LOGGER.debug("Got CarryID: [ResourceLocation: \"{}\", UUID: \"{}\"]", id, uuid);
                            
                            final ResourceLocation resourceLocation = ResourceLocation.parse(id);
                            
                            final CarryRegistryManager manager = CarryRegistryManager.INSTANCE;
                            final var blockAdapter = manager.getBlockAdapter(resourceLocation);
                            final var blockEntityAdapter = manager.getBlockEntityAdapter(resourceLocation);
                            final var entityAdapter = manager.getEntityAdapter(resourceLocation);
                            
                            if(blockAdapter.isPresent())
                            {
                                LOGGER.debug("Recovered a block listener with ID: {}.", fullID);
                                BLOCK_CARRY_LISTENERS.put(fullID, blockAdapter.get());
                            }
                            else if(blockEntityAdapter.isPresent())
                            {
                                LOGGER.debug("Recovered a blockEntity listener with ID: {}.", fullID);
                                BLOCK_ENTITY_CARRY_LISTENERS.put(fullID, blockEntityAdapter.get());
                            }
                            else if(entityAdapter.isPresent())
                            {
                                LOGGER.debug("Recovered a entity listener with ID: {}.", fullID);
                                ENTITY_CARRY_LISTENERS.put(fullID, entityAdapter.get());
                            }
                        }
                    )
            );
            
            handle.changeMarker("CARRY_ENGINE_STARTED");
            LOGGER.debug("Listeners recovered. Carry engine, start!");
        }
    }
    
    //? TODO: Overweight effect.
    public static void carryingTick(@NotNull ItemStack carryCrate, @NotNull Level level, @NotNull Entity entity, int slotId)
    {
        if(!carryCrate.has(CarryCrateRegistries.CARRY_ID.get()) && !carryCrate.has(CarryCrateRegistries.CARRY_CRATE_DATA.get()))
            return;
        
        final CarriableExtensions.ICarryTickable.TickingContext context = new CarriableExtensions.ICarryTickable.TickingContext(carryCrate, level, entity, slotId);
        final String carryID = carryCrate.get(CarryCrateRegistries.CARRY_ID.get());
        final CarryData data = carryCrate.get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        assert carryID != null;//! carryCrate#has() has granted the safety.
        assert data != null;
        
        final int penaltyRate = data.data().getPenaltyRate();
        
        final Consumer<HashMap<String, ? extends ICarryRegistry.IBaseCarryAdapterFactory<?, ?>>> tickAction = 
            map ->
            {
                final AbstractCarryAdapter adapter = map.get(carryID).create(null);
                adapter.carryingTick(context);//? TODO: #carryingTick() should have a return value, like Tag or something else.
            }; 
        
        switch(data.carryType())
        {
            case BLOCK_ENTITY -> tickAction.accept(BLOCK_ENTITY_CARRY_LISTENERS);
            case ENTITY -> tickAction.accept(ENTITY_CARRY_LISTENERS);
            case BLOCK -> tickAction.accept(BLOCK_CARRY_LISTENERS);
        }
        
        //! This has already implicitly checked whether it is clientside.
        if(!(entity instanceof ServerPlayer player) || penaltyRate == 0)
            return;
        
        final int currentCounter = Objects.requireNonNullElse(carryCrate.get(CarryCrateRegistries.CARRY_TICK_COUNTER.get()), 0);
        
        if(currentCounter + 1 >= penaltyRate)
        {
            carryCrate.hurtAndBreak(PENALTY_QUANTITY, (ServerLevel) level, player, i -> 
                {
                    //? TODO UwU
                }
            );
            
            carryCrate.remove(CarryCrateRegistries.CARRY_TICK_COUNTER.get());
            return;
        }
        
        carryCrate.set(CarryCrateRegistries.CARRY_TICK_COUNTER.get(), currentCounter + 1);
    }
    
    //? TODO: BE Handler Logic refactor
    public static @NotNull InteractionResult interactOnBlock(@NotNull UseOnContext context)
    {
        final CarryType action;
        
        try(final MarkLogger.MarkerHandle handle = LOGGER.pushMarker("BLOCK_INTERACT"))
        {
            final @Nullable Player player = context.getPlayer();
            final BlockState state = context.getLevel().getBlockState(context.getClickedPos());
            final @Nullable BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
            
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
            
            if(context.getLevel().isClientSide)
            {
                LOGGER.debug("Current is client side, returning result as \"SUCCESS\".");
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
            }
            
            final ServerLevel level = (ServerLevel) context.getLevel();
            final CarryData data = context.getItemInHand().get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
            
            if(data != null && data.data() instanceof CarryData.CarryEntityDataHolder)
                action = CarryType.ENTITY;
            else
                action = blockEntity != null ? CarryType.BLOCK_ENTITY : CarryType.BLOCK;
            
            handle.changeMarker("ACTION_SELECT");
            LOGGER.debug("Picking {} as the current action.", action.name());
            
            if(action == CarryType.BLOCK_ENTITY && CarryRegistryManager.INSTANCE.getBlockEntityAdapter(blockEntity.getType()).isEmpty())
            {
                LOGGER.debug("BlockEntity \"{}\" is not supported by carry crate. Skipped.", blockEntity.toString());
                return InteractionResult.PASS;
            }
            else if(action == CarryType.BLOCK && CarryRegistryManager.INSTANCE.getBlockAdapter(state.getBlock()).isEmpty())
            {
                LOGGER.debug("Block \"{}\" is not supported by carry crate. Skipped.", state.getBlock().getDescriptionId());
                return InteractionResult.PASS;
            }
            
            handle.changeMarker("CARRY_ID_QUERY");
            LOGGER.debug("Trying to get the CarryID of this carry crate...");
            
            final @Nullable String carryID = context.getItemInHand().get(CarryCrateRegistries.CARRY_ID.get());
            LOGGER.debug("Got CarryID: \"{}\"", Objects.requireNonNullElse(carryID, "N/A"));
            
            final AbstractCarryInteractHandler handler = action.createHandler(
                (ServerLevel) context.getLevel(),
                (ServerPlayer) context.getPlayer(),
                context.getItemInHand(),
                context.getClickedPos(),
                context.getLevel().getBlockState(context.getClickedPos()),
                null,
                carryID
            );
            
            final AbstractCarryInteractHandler.HandleResult result = handler.handle();
            final Optional<CarryData> optionalData = result.data();
            final Optional<String> optionalCarryID = result.carryID();
            InteractionResult finalResult = result.result();
            
            if(result.shouldTakeTarget() && !action.equals(CarryType.ENTITY))
            {
                level.setBlockAndUpdate(context.getClickedPos(), Blocks.VOID_AIR.defaultBlockState());
                level.playSound(null, context.getClickedPos(), SoundEvents.SCAFFOLDING_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            else if(result.shouldReleaseTarget() && optionalData.isPresent())
            {
                if(!action.equals(CarryType.ENTITY))
                {
                    final CarryBlockPlaceContext placeContext = getCarryBlockPlaceContext(context, action, optionalData.get());
                    finalResult = placeContext.performPlace();
                }
                else
                {
                    final CarryData.CarryEntityDataHolder entityDataHolder = optionalData.get().data();
                    final Optional<Entity> optionalEntity = EntityType.create(entityDataHolder.getTagData(), level);
                    
                    if(optionalEntity.isPresent())
                    {
                        final Entity entity = optionalEntity.get();
                        final BlockPos pos = context.getClickedPos();
                        entity.moveTo(pos.getX(), pos.getY(), pos.getZ());
                        
                        level.addFreshEntity(entity);
                        finalResult = InteractionResult.SUCCESS;
                    }
                }
            }
            
            optionalCarryID.ifPresent(
                id ->
                {
                    if(result.shouldAddListener() && optionalData.isPresent())
                    {
                        switch(action)
                        {
                            case BLOCK_ENTITY ->
                            {
                                final CarryData.CarryBlockEntityDataHolder blockEntityDataHolder = optionalData.get().data();
                                
                                final var optionalFactory = 
                                    CarryRegistryManager.INSTANCE.getBlockEntityAdapter(blockEntityDataHolder.getType());
                                
                                optionalFactory.ifPresent(
                                    factory ->
                                    {
                                        BLOCK_ENTITY_CARRY_LISTENERS.put(id, factory);
                                        markDirty(level);
                                    }
                                );
                                
                                LOGGER.when(optionalFactory.isEmpty()).error(
                                    "Cannot find blockEntity \"{}\"'s adapter factory!",
                                    blockEntityDataHolder.getType().toString()
                                );
                            }
                            case BLOCK ->
                            {
                                final CarryData.CarryBlockDataHolder blockDataHolder = optionalData.get().data();
                                final var optionalFactory = 
                                    CarryRegistryManager.INSTANCE.getBlockAdapter(blockDataHolder.getState().getBlock());
                                
                                optionalFactory.ifPresent(
                                    factory ->
                                    {
                                        BLOCK_CARRY_LISTENERS.put(id, factory);
                                        markDirty(level);
                                    }
                                );
                                
                                LOGGER.when(optionalFactory.isEmpty()).error(
                                    "Cannot find block \"{}\"'s adapter factory!",
                                    blockDataHolder.getState().getBlock().getDescriptionId()
                                );
                            }
                            case ENTITY -> {}
                        }
                    }
                    else if(result.shouldRemoveListener())
                    {
                        markDirty(level);
                        switch(action)
                        {
                            case BLOCK_ENTITY -> BLOCK_ENTITY_CARRY_LISTENERS.remove(id);
                            case BLOCK -> BLOCK_CARRY_LISTENERS.remove(id);
                            case ENTITY -> ENTITY_CARRY_LISTENERS.remove(id);
                        }
                    }
                }
            );
            
            if(result.shouldInsertComponent() && optionalData.isPresent())
                insertData(context.getItemInHand(), player, CarryCrateRegistries.CARRY_CRATE_DATA.get(), optionalData.get());
            else if(result.shouldRemoveComponent())
                context.getItemInHand().remove(CarryCrateRegistries.CARRY_CRATE_DATA.get());
            
            return finalResult;
        }
    }
    
    public static @NotNull InteractionResult interactOnEntity(
        @NotNull ItemStack carryCrateStack,
        @NotNull Player player,
        @NotNull LivingEntity interactionTarget
    )
    {
        if(CarryRegistryManager.INSTANCE.getEntityAdapter(interactionTarget.getType()).isEmpty())
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
        
        final @Nullable String carryID = carryCrateStack.get(CarryCrateRegistries.CARRY_ID.get());
        
        final AbstractCarryInteractHandler handler = CarryType.ENTITY.createHandler(
            (ServerLevel) level,
            (ServerPlayer) player,
            carryCrateStack,
            null,
            null,
            interactionTarget,
            carryID
        );
        
        final AbstractCarryInteractHandler.HandleResult result = handler.handle();
        final Optional<CarryData> optionalData = result.data();
        final Optional<String> optionalCarryID = result.carryID();
        InteractionResult finalResult = result.result();
        
        if(result.shouldTakeTarget())
            interactionTarget.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        else if(result.shouldReleaseTarget() && optionalData.isPresent())
        {
            final CarryData.CarryEntityDataHolder entityDataHolder = optionalData.get().data();
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
        
        if(optionalCarryID.isPresent())
        {
            if(result.shouldAddListener() && optionalData.isPresent())
            {
                final CarryData.CarryEntityDataHolder entityDataHolder = optionalData.get().data();
                final var optionalFactory = 
                    CarryRegistryManager.INSTANCE.getEntityAdapter(entityDataHolder.getType());
                
                optionalFactory.ifPresent(
                    factory ->
                    {
                        ENTITY_CARRY_LISTENERS.put(optionalCarryID.get(), factory);
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
                ENTITY_CARRY_LISTENERS.remove(optionalCarryID.get());
            }
        }
        
        if(result.shouldInsertComponent() && optionalData.isPresent())
            insertData(carryCrateStack, player, CarryCrateRegistries.CARRY_CRATE_DATA.get(), optionalData.get());
        else if(result.shouldRemoveComponent())
            carryCrateStack.remove(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        
        return finalResult;
    }
    
    private static @NotNull CarryBlockPlaceContext getCarryBlockPlaceContext(@NotNull UseOnContext context, @NotNull CarryType action, @NotNull CarryData result)
    {
        final BlockState stateToPlace;
        
        switch(action)
        {
            case BLOCK_ENTITY ->
            {
                final CarryData.CarryBlockEntityDataHolder blockEntityDataHolder = result.data();
                stateToPlace = blockEntityDataHolder.getState();
            }
            case BLOCK ->
            {
                final CarryData.CarryBlockDataHolder blockDataHolder = result.data();
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
    
    private static <T> void insertData(@NotNull ItemStack stack, @NotNull Player player, @NotNull DataComponentType<T> type, @NotNull T data)
    {
        if(stack.isEmpty())
            return;
        
        if(stack.getCount() <= 1)
        {
            stack.set(type, data);
            return;
        }
        
        stack.shrink(1);
        final ItemStack newStack = stack.copy();
        newStack.setCount(1);
        newStack.set(type, data);
        
        if(player.getInventory().add(newStack))
            return;
        
        Containers.dropItemStack(player.level(), player.getX(), player.getY(), player.getZ(), newStack);
    }
}
