//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core;

import com.google.errorprone.annotations.DoNotCall;
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
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryInteractContext;
import kurvcygnus.crispsweetberry.common.features.carrycrate.events.CarryCrateCopyProcessor;
import kurvcygnus.crispsweetberry.common.features.carrycrate.products.CarryCrateItem;
import kurvcygnus.crispsweetberry.common.features.carrycrate.products.OverweightEffect;
import kurvcygnus.crispsweetberry.lib.base.extensions.StatedBlockPlaceContext;
import kurvcygnus.crispsweetberry.lib.base.functions.ITriConsumer;
import kurvcygnus.crispsweetberry.lib.base.lang.IVault;
import kurvcygnus.crispsweetberry.lib.core.log.IMarkLogger;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import kurvcygnus.crispsweetberry.utils.constants.FunctionalDummies;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData.CarryBlockEntityDataHolder;
import static kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryInteractContextCollection.*;

//? FIX: Somehow, when capturing entity with one carryCrate, component persistent won't work.
//? According to debugging, this bug doesn't happen before the end of [[CarryCrateItem#interactLivingEntity]].

/**
 * The core engine of the whole carry system. As you can could see, it is complex enough to be an independent class.<br>
 * It is capable of doing these things:
 * <ul>
 *     <li>
 *         Handling <u>{@link CarryCrateItem}</u>'s interaction(<i>
 *             both <u>{@link CarryCrateItem#useOn(UseOnContext) blocklike interact}</u> and
 *             <u>{@link CarryCrateItem#interactLivingEntity(ItemStack, Player, LivingEntity, InteractionHand) entity interact}</u>
 *         </i>).
 *     </li>
 *     <li>Owning the <u>{@link #LISTENER_LOOKUP Lookup}</u> <b>that holds contented CarryCrates</b>, whose are needed to be monitored.</li>
 *     <li>Dispatching the carrying behaviors of CarryCrates that has data.(See <u>{@link #carryingTick(CarryCrateItem, ItemStack, Level, Entity, int)}</u>)</li>
 *     <li>
 *         Saving/Restoring the <u>{@link #LISTENER_LOOKUP listner lookup}</u> on game start/end(See <u>{@link CarryListenerSaveData}</u>, <u>{@link #startEngine(ServerStartedEvent)}</u>).
 *     </li>
 * </ul>
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see CarryRegistryManager
 * @see CarryOperationExecutor
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public enum CarryEngine
{
    INST;
    
    //region Fields
    private static final Map<CarryID, ICarryBlockEntityAdapterFactory<?, ?>> BLOCK_ENTITY_CARRY_LISTENERS = new HashMap<>();
    private static final Map<CarryID, ICarryEntityAdapterFactory<?, ?>> ENTITY_CARRY_LISTENERS = new HashMap<>();
    private static final Map<CarryID, ICarryBlockAdapterFactory<?, ?>> BLOCK_CARRY_LISTENERS = new HashMap<>();
    
    private static final Map<CarryType, Map<CarryID, ? extends IBaseCarryAdapterFactory<?, ?>>> LISTENER_LOOKUP =
        DefinitionUtils.createImmutableEnumMapWithCheck(
            CarryType.class,
            map ->
            {
                map.put(CarryType.BLOCK_ENTITY, BLOCK_ENTITY_CARRY_LISTENERS);
                map.put(CarryType.ENTITY, ENTITY_CARRY_LISTENERS);
                map.put(CarryType.BLOCK, BLOCK_CARRY_LISTENERS);
            }
        );
    
    @ApiStatus.Internal public static final IVault<ITriConsumer<CarryType, CarryID, CarryID>, Optional<?>> INSERT_ACCESS = IVault.ofAccessLimited(
        (type, original, newID) ->
        {
            @SuppressWarnings("unchecked")//! Safe Casting. Internal Map Mutation is always legal, no [[ClassCastException]], granted by [[CarryType]].
            final var subLookup = (Map<CarryID, IBaseCarryAdapterFactory<?, ?>>) LISTENER_LOOKUP.get(type);
            subLookup.put(newID, subLookup.get(original));
        },
        CarryCrateCopyProcessor.class
    );
    
    private static final ThreadLocal<Boolean> IS_INTERACTING_WITH_BE = ThreadLocal.withInitial(FunctionalDummies::alwaysFalse);
    
    private static final IMarkLogger LOGGER = IMarkLogger.marklessLogger();
    //endregion
    
    //region Initialization Data & Engine Persistent Lifecycle
    /**
     * This is the definition of the instantized <u>{@link CarryEngine#LISTENER_LOOKUP}</u>.<br>
     * <b>It exists to make sure that boxed Carry Crate's data won't get lost</b>.
     * @since 1.0 Release
     * @author Kurv Cygnus
     * @implNote This class only contains the data field and <u>{@link #save(CompoundTag, HolderLookup.Provider) save logic}</u>,
     * <b>The data shall be saved once the data is marked as dirty(At <u>{@link CarryOperationExecutor}</u>).</b>
     * <hr>
     * Also, you may ask:
     * <h3><b>"Why this persistent operation only involves <u>{@link CarryID}</u>? Where is <u>{@link CarryData}</u>???"</b></h3>
     * <br>
     * The answer is, <b><i><span style="color: 95cc6d">they are ignored by design, since there's no need to.</span></i></b>
     * <br>
     * Explanations: Both <u>{@link CarryID}</u> and <u>{@link CarryData}</u> are a type of <u>{@link net.minecraft.core.component.DataComponentType DataComponentType}</u>,
     * which means, they can and only exists on <u>{@link net.minecraft.world.item.Item Item}</u> as attachments, <b>and their persistent are handled by Minecraft itself.</b>
     * <hr>
     * <h3><i>Then, why save <u>{@link CarryID}</u> again?</i></h3>
     * The answer is <u>{@link CarryEngine}</u> have to monitor contented <u>{@link CarryCrateItem CarryCrates}</u>,
     * since we have to make <u>{@link #carryingTick(CarryCrateItem, ItemStack, Level, Entity, int)}</u> work.<br>
     * Thus, iterating all <u>{@link ItemStack item intsnaces}</u> to rebuild <u>{@link #LISTENER_LOOKUP}</u> is stupid, because:
     * <ul>
     *     <li><span style="color: f84b4b">Slow.</span> Iterating needs to scan a number of chunks, which is a disaster.</li>
     *     <li>
     *         <span style="color: f84b4b">Edgy.</span> Minecraft won't load all chunks to grantee performance.
     *         <span style="color: f84b4b">Iterating CANNOT get all <u>{@link ItemStack item instances}</u> at ALL.</span><br>
     *         Also, <b>how about backpack mod's contents, <u>{@link BlockEntity}</u> contents and so on?</b>
     *     </li>
     *     <li>
     *         <span style="color: f84b4b">Capable of causing side effects.</span>
     *         Visiting these data too early can lead to unexpected firing of <u>{@link net.neoforged.bus.api.Event Event}</u>, or something else,
     *         despite it won't always happen.
     *     </li>
     * </ul>
     * , and we'll lose the info of corresponded
     * <u>{@link AbstractCarryAdapter adapters}</u>, so we have to save these info into a file to recover <u>{@link #LISTENER_LOOKUP}</u>.
     */
    @ApiStatus.Internal
    static final class CarryListenerSaveData extends SavedData
    {
        static final String UUID = "uuid";
        static final String ID = "id";
        static final String ENTRIES = "entries";
        static final String DATA = DefinitionUtils.namespacedUnderscoreId("persistent_carry_listeners");
        
        private ListTag entries = null;
        
        private CarryListenerSaveData() {}
        
        static @NotNull CarryListenerSaveData create() { return new CarryListenerSaveData(); }
        
        @Override public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
        {
            try(final var ignored = LOGGER.pushMarker("PERSISTENT"))
            {
                final var entryList = new ListTag();
                
                for(final var lookup: LISTENER_LOOKUP.values())
                    lookup.forEach(
                        (id, $) ->
                        {
                            final CompoundTag entry = new CompoundTag();
                            entry.putString(ID, id.id);
                            entry.putString(UUID, id.uuid);
                            entryList.add(entry);
                            LOGGER.debug("Added UUID \"{}\", corresponded Adapter Object ID: \"{}\"", id.uuid, id.id);
                        }
                    );
                
                tag.put(ENTRIES, entryList);
            }
            return tag;
        }
        
        static @NotNull CarryListenerSaveData get(@NotNull MinecraftServer server)
        {
            //! Explanation: Minecraft saves most world data by dimension.
            //! [[CarryEngine#LISTENER_LOOKUP]] is expected to be cross-dimensional,
            //! and in such a case, we choose to use [[Level#OVERWORLD]] as standard.
            final var storage = server.overworld().getDataStorage();
            
            //* Notes that [[net.minecraft.world.level.saveddata.SavedData.Factory]] is actually not a factory.
            //* More precisely, it is a file IO Pair, with [[Supplier]] as output(by calling instance method [[SavedData#save]]),
            //* [[BiFunction]] as input(loading file, here equals to method [[CarryListenerSaveData#load]]).
            //* We need to send this pair into server by [[DimensionDataStorage#computeIfAbsent]],
            //* only then the file shall be processed.
            final SavedData.Factory<CarryListenerSaveData> factory = new Factory<>(
                CarryListenerSaveData::create,
                CarryListenerSaveData::load
            );
            
            return storage.computeIfAbsent(factory, DATA);
        }
        
        private static @NotNull CarryListenerSaveData load(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
        {
            final CarryListenerSaveData data = CarryListenerSaveData.create();
            data.entries = tag.getList(ENTRIES, CompoundTag.TAG_COMPOUND);
            
            return data;
        }
        
        @NotNull Optional<ListTag> getEntries() { return Optional.ofNullable(entries); }
    }
    
    /**
     * Deserialize the level's Save Data, restoring <u>{@link #LISTENER_LOOKUP}</u>.
     */
    @SuppressWarnings("unchecked")//! Unsafe casting, however, with the restrict of enum [[CarryType]], it is actually safe.
    @SubscribeEvent @DoNotCall static void startEngine(@NotNull ServerStartedEvent event)
    {
        BLOCK_ENTITY_CARRY_LISTENERS.clear();
        BLOCK_CARRY_LISTENERS.clear();
        ENTITY_CARRY_LISTENERS.clear();
        
        final CarryListenerSaveData data = CarryListenerSaveData.get(event.getServer());
        
        //! DO NOT MOVE THIS INTO LAMBDA. See [[IVault#ofAccessLimited]], it is related to [[StackWalker]].
        final var internalRestore = CarryID.__$1NT3RNAL_R3ST0R3$__.tryGet(Optional.of(event));
        data.getEntries().ifPresent(
            listTag ->
            {
                try(final var handle = LOGGER.pushMarker("CARRY_DATA_RECOVER"))
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
                                final CarryID fullID = internalRestore.apply(id, uuid);
                                LOGGER.debug("Restored CarryID: \n{}", fullID);
                                
                                final @Nullable var adapter = CarryRegistryManager.INST.searchFactory(ResourceLocation.parse(id));
                                
                                if(adapter == null)
                                {
                                    LOGGER.error("Entry with CarryID \"{}\" doesn't have a corresponded factory!", fullID);
                                    return;
                                }
                                
                                ((Map<CarryID, IBaseCarryAdapterFactory<?, ?>>) LISTENER_LOOKUP.get(adapter.getType())).put(fullID, adapter);
                                
                                LOGGER.debug("Recovered a {} listener with ID: {}.", adapter.getType().name(), fullID);
                            }
                        );
                    
                    handle.changeMarker("CARRY_ENGINE_STARTED");
                    LOGGER.debug("Listeners recovered. Carry engine, start!");
                }
            }
        );
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
        
        final var carryID = carryCrate.get(CarryCrateRegistries.CARRY_ID.get());
        final var data = carryCrate.get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        assert carryID != null;//! [[DataComponentHolder#has()]] has granted the safety.
        assert data != null;//! `assert` doesn't work in non-debugging environment, it won't bring any extra performance penalty comparing to [[Objects#requireNonNull]].
        
        final var context = new CarriableExtensions.TickingContext(carryCrate, level, entity, data, carryID.uuid, slotId);
        
        final int penaltyRate = data.unionData().penaltyRate;
        final @Nullable var adapter = getCarryAdapter(LISTENER_LOOKUP.get(data.carryType), carryID);
        
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
                adapter.onBreak(
                    level,
                    player.getOnPos(),
                    data.unionData(),
                    level.getGameTime() - data.startTime
                );
                
                OverweightEffect.updateFactorAndEffect(player, data, TriState.FALSE);
                
                carryCrate.remove(CarryCrateRegistries.CARRY_CRATE_DATA.get());
                carryCrate.remove(CarryCrateRegistries.CARRY_ID.get());
            }
            
            if(
                data.carryType.equals(CarryType.BLOCK_ENTITY) &&
                adapter instanceof AbstractBlockEntityCarryAdapter<?> blockEntityCarryAdapter &&
                level.getRandom().nextFloat() < (float) carryCrate.getDamageValue() / carryCrate.getMaxDamage()
            ) carryCrate.set(
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
            
            final var level = context.getLevel();
            final var interactPos = context.getInteractPos();
            final var optionalPlayer = context.getPlayer();
            final var carryCrate = context.getCarryCrate();
            final @Nullable var carryData = context.getCarryData();
            
            if(!level.isClientSide)
                LOGGER.debug(
                    "State of this interaction: Player: {}, Data: {}",
                    optionalPlayer.map(player -> player.getGameProfile().getName()).orElse("N/A"),
                    carryData
                );
            
            final @Nullable CarryType action = switch(context)
            {
                case CarryBlocklikeInteractContext ctx ->
                {
                    if(optionalPlayer.isEmpty() || level.getBlockState(interactPos).is(Blocks.VOID_AIR))
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
                        targetBlockState = null;
                        useOnContext = null;
                        yield null;
                    }
                    
                    useOnContext = ctx.context();
                    
                    yield switch(carryData)
                    {
                        case null ->
                        {
                            targetEntity = null;
                            targetBlockState = level.getBlockState(interactPos);
                            
                            targetBlockEntity = context.getLevel().getBlockEntity(interactPos);
                            final CarryType result = targetBlockEntity != null ? CarryType.BLOCK_ENTITY : CarryType.BLOCK;
                            
                            //! As you can see, once the [[CarryType]] is BLOCK_ENTITY, "targetBlockEntity" won't be null.
                            assert targetBlockEntity != null;
                            yield validateBlocklikeAction(result, targetBlockEntity, targetBlockState);
                        }
                        case CarryData that when that.unionData() instanceof CarryData.CarryEntityDataHolder holder ->
                        {
                            targetEntity = (LivingEntity) holder.type.create(level);
                            targetBlockEntity = null;
                            targetBlockState = null;
                            yield CarryType.ENTITY;
                        }
                        default ->
                        {
                            targetEntity = null;
                            
                            switch(carryData.unionData())
                            {
                                case CarryBlockEntityDataHolder blockEntityDataHolder ->
                                {
                                    IS_INTERACTING_WITH_BE.set(true);
                                    
                                    final var type = blockEntityDataHolder.type;
                                    
                                    targetBlockState = blockEntityDataHolder.state;
                                    //* NOTE: This logic is illegal on Vanilla.
                                    //* It is implemented with the help of [[CarryEngine#IS_INTERACTING_WITH_BE]] and [[CarryBlockEntityValidationPasser#tryPass]].
                                    targetBlockEntity = Objects.requireNonNull(
                                        type.create(interactPos, targetBlockState),
                                        DefinitionUtils.quickFormat(
                                            """
                                            Fatal:
                                            Failed to create blockEntity "{}"'s adapter. This usually means the blockEntity's type registration itself has dataflow issue, or this
                                            method is called at improper time.
                                            
                                            {}
                                            """,
                                            type.toString(),
                                            MetainfoConstants.FEEDBACK_MESSAGE
                                        )
                                    );
                                    
                                    IS_INTERACTING_WITH_BE.set(false);
                                }
                                case CarryData.CarryBlockDataHolder blockDataHolder ->
                                {
                                    targetBlockState = blockDataHolder.state;
                                    targetBlockEntity = null;
                                }
                                default -> throw new IllegalStateException("Impossible branch!\n" + carryData.unionData());
                            }
                            
                            yield carryData.carryType;
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
            
            if(action == null)
                return null;
            
            handle.changeMarker("ACTION_SELECT");
            
            if(!level.isClientSide)
                LOGGER.debug("Current action: {}.", action.name());
            //endregion
            
            //region Process Logics
            if(level.isClientSide)
            {
                handle.changeMarker("CLIENT_HANDLE");
                LOGGER.debug("Current is client side, returning result as \"SUCCESS_NO_ITEM_USED\".");
                return InteractionResult.SUCCESS_NO_ITEM_USED;//! Using SUCCESS, or [[InteractionResult#sidedSuccess]] may lead to [[ItemStack#shrink]].
            }
            
            final var serverLevel = (ServerLevel) level;
            final var serverPlayer = (ServerPlayer) optionalPlayer.get();
            
            handle.changeMarker("CARRY_ID_QUERY");
            final @Nullable var carryID = context.getCarryID();
            LOGGER.debug("Got CarryID: \n{}", Objects.requireNonNullElse(carryID, "N/A"));
            
            final @Nullable var interactResult = CarryOperationExecutor.INST.handle(
                CarryInteractContext.init(
                    action,
                    serverLevel,
                    serverPlayer,
                    interactPos,
                    carryCrate,
                    ((Map<CarryID, IBaseCarryAdapterFactory<?, ?>>) LISTENER_LOOKUP.get(action))::put,
                    LISTENER_LOOKUP.get(action)::remove,
                    useOnContext != null ? state -> new StatedBlockPlaceContext(useOnContext, state) : null,
                    targetBlockState,
                    targetEntity,
                    targetBlockEntity,
                    carryID
                )
            );
            
            return interactResult;
            //endregion
        }
    }
    //endregion
    
    //region API & Helpers
    /**
     * @implNote There's no need to refactor this into <u>{@link IVault}</u>, especially with <u>{@link IVault#ofAccessLimited(Object, Class[])}</u>.<br>
     * That's because <b>mixin class doesn't exist during runtime. Thus, mixin framework takes any Classes that uses mixin class references as illegal access,
     * causing exception on game start.</b><br><br>
     * <i>Also, this hook is just revealing a {@code boolean}, man. What can others do with this?</i>
     */
    @ApiStatus.Internal public boolean isInteracting() { return IS_INTERACTING_WITH_BE.get(); }
    
    private static @Nullable AbstractCarryAdapter<?> getCarryAdapter(
        @NotNull Map<CarryID, ? extends ICarryRegistryView.IBaseCarryAdapterFactory<?, ?>> map,
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
