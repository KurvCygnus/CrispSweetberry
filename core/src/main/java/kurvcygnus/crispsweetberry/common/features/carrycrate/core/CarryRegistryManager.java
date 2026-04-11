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
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateConstants;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.block.AbstractBlockCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.entity.AbstractEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.entity.AdaptiveAnimalCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.events.CarryAdapterRegisterEvent;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.AbstractCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryType;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import kurvcygnus.crispsweetberry.utils.UIUtils;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import kurvcygnus.crispsweetberry.utils.core.log.MarkLogger;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static kurvcygnus.crispsweetberry.utils.FunctionalUtils.throwIf;

@ApiStatus.Internal @EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public enum CarryRegistryManager implements ICarryRegistry
{
    INST;
    
    //region Fields & Constants
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "CARRY_REGISTRY");
    private static final String ILLEGAL_REGISTER_INFO = "Attempting registration after registry frozen is not allowed!";
    
    private boolean frozen = false;
    
    private static final HashMap<BlockEntityType<?>, ICarryBlockEntityAdapterFactory<?, ?>> BLOCK_ENTITY_REGISTRY = new HashMap<>();
    private static final HashMap<ResourceLocation, ICarryBlockEntityAdapterFactory<?, ?>> RECOVERY_BLOCK_ENTITY_REGISTRY = new HashMap<>();
    
    private static final HashMap<Block, ICarryBlockAdapterFactory<?, ?>> BLOCK_REGISTRY = new HashMap<>();
    private static final HashMap<ResourceLocation, ICarryBlockAdapterFactory<?, ?>> RECOVERY_BLOCK_REGISTRY = new HashMap<>();
    
    private static final HashMap<EntityType<?>, ICarryEntityAdapterFactory<?, ?>> ENTITY_REGISTRY = new HashMap<>();
    private static final HashMap<ResourceLocation, ICarryEntityAdapterFactory<?, ?>> RECOVERY_ENTITY_REGISTRY = new HashMap<>();
    
    private static final HashMap<ResourceLocation, Component> TRANSLATION_REGISTRY = new HashMap<>();
    
    private static final Map<CarryType, HashMap<?, ? extends IBaseCarryAdapterFactory<?, ?>>> REGISTRY_LOOKUP =
        DefinitionUtils.createImmutableEnumMap(
            CarryType.class,
            map ->
            {
                map.put(CarryType.BLOCK_ENTITY, BLOCK_ENTITY_REGISTRY);
                map.put(CarryType.ENTITY, ENTITY_REGISTRY);
                map.put(CarryType.BLOCK, BLOCK_REGISTRY);
            }
        );
    
    private static final Map<CarryType, HashMap<ResourceLocation, ? extends IBaseCarryAdapterFactory<?, ?>>> RECOVER_LOOKUP =
        DefinitionUtils.createImmutableEnumMap(
            CarryType.class,
            map ->
            {
                map.put(CarryType.BLOCK_ENTITY, RECOVERY_BLOCK_ENTITY_REGISTRY);
                map.put(CarryType.ENTITY, RECOVERY_ENTITY_REGISTRY);
                map.put(CarryType.BLOCK, RECOVERY_BLOCK_REGISTRY);
            }
        );
    //endregion
    
    //region Event Lifecycles & Auto Bind Logics
    @SubscribeEvent static void register(@NotNull FMLLoadCompleteEvent event)
    {
        requireNonNull(
            CrispSweetberry.CRISP_BUS,
            "Fatal: ModBus seems to be null! %s".formatted(MetainfoConstants.FEEDBACK_MESSAGE)
        ).
            post(new CarryAdapterRegisterEvent(CarryRegistryManager.INST));
        
        event.enqueueWork(
            () ->
            {
                LOGGER.debug("Registration ended, starting Entity Compat Binding...");
                
                autoEntityBind();
                
                CarryRegistryManager.INST.frozen = true;
                LOGGER.info("Carry adapters' registration completed!");
            }
        );
    }
    
    @SuppressWarnings("unchecked")//! Unsafe casting, with try-catch ;)
    private static void autoEntityBind()
    {
        BuiltInRegistries.ENTITY_TYPE.stream().
            filter(
                entityType ->
                {
                    if(!Objects.equals(entityType.getCategory(), MobCategory.CREATURE))
                        return false;
                    
                    LOGGER.debug("Captured entity \"{}\" as friendly entity.", entityType.getDescriptionId());
                    
                    final AABB aabb = entityType.getSpawnAABB(0D, 0D, 0D);
                    final double entityVolume = aabb.getXsize() * aabb.getYsize() * aabb.getZsize();
                    final boolean isAcceptable = entityVolume <= AdaptiveAnimalCarryAdapter.MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME;
                    
                    LOGGER.debug(
                        "Entity \"{}\" {}.",
                        entityType.getDescriptionId(),
                        isAcceptable ?
                            "accepted" :
                            "rejected. Its volume doesn't meet the condition. Volume: %f".
                                formatted(entityVolume)
                    );
                    
                    return isAcceptable;
                }
            ).
            forEach(
                animalType ->
                {
                    //! Seems hacky? Actually, this is the best reality solution of such a situation.
                    //! [[EntityType]] has a method [[EntityType#getBaseClass()]], which actually turns out to be hard-coded, returning "Entity.class" only.
                    //! So, Animal.class#isAssignableFrom(Class<?>) will not work.
                    //! Then, how about [[EntityTypeTest#forClass(Class<F>)]]?
                    //! That is reliable, but all of its return values are not EntityType, we can't use it.
                    //! What about [[EntityType#create(Level)]]?
                    //! [[Level]] is unaccessible during game initialization, doing that is even more hacky than this.
                    //! Besides, dummy level is a hard stuff, this doesn't worth it.
                    try
                    {
                        final EntityType<? extends Animal> animal = (EntityType<? extends Animal>) animalType;
                        
                        //! Somehow, wandering trader is counted as an animal, despite it is not a animal.
                        if(animal.getDescriptionId().equals("minecraft:wandering_trader"))
                            return;
                        
                        CarryRegistryManager.INST.unsafeRegisterEntity(animal, AdaptiveAnimalCarryAdapter::new);
                    }
                    catch(ClassCastException $) { LOGGER.debug("Entity \"{}\" is not an animal. Skipped.", animalType.getDescriptionId()); }
                }
            );
    }
    //endregion
    
    //region Register Boilerplates
    @Override public <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<E>> void register(
        @NotNull BlockEntityType<E> blockEntityType,
        @NotNull ICarryRegistry.ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory
    )
    {
        throwIf(this.frozen, ILLEGAL_REGISTER_INFO, IllegalStateException::new);
        
        requireNonNull(blockEntityType, "Param \"blockEntityType\" must not be null!");
        requireNonNull(BlockEntityType.getKey(blockEntityType), "Param \"blockEntityType\"'s ResourceLocation must not be null!");
        requireNonNull(carryAdapterBlockEntityFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        registerBlockEntity(blockEntityType, carryAdapterBlockEntityFactory);
    }
    
    @Override public <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>> void registerUniversal(
        @NotNull Set<? extends BlockEntityType<? extends E>> blockEntityTypes,
        @NotNull ICarryRegistry.ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory
    )
    {
        throwIf(this.frozen, ILLEGAL_REGISTER_INFO, IllegalStateException::new);
        
        requireNonNull(blockEntityTypes, "Param \"blockEntityTypes\" must not be null!");
        requireNonNull(carryAdapterBlockEntityFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        for(final BlockEntityType<? extends E> blockEntityType: blockEntityTypes)
        {
            requireNonNull(blockEntityType, "\"blockEntityType\" must not be null!");
            requireNonNull(BlockEntityType.getKey(blockEntityType), "Param \"blockEntityType\"'s ResourceLocation must not be null!");
            
            registerBlockEntity(blockEntityType, carryAdapterBlockEntityFactory);
        }
    }
    
    @Override public <B extends Block, A extends AbstractBlockCarryAdapter<B>>
    void register(@NotNull B block, @NotNull ICarryBlockAdapterFactory<B, A> carryAdapterBlockAdapterFactory)
    {
        throwIf(this.frozen, ILLEGAL_REGISTER_INFO, IllegalStateException::new);
        
        requireNonNull(block, "Param \"block\" must not be null!");
        requireNonNull(BuiltInRegistries.BLOCK.getKey(block), "Param \"block\"'s ResourceLocation must not be null!");
        requireNonNull(carryAdapterBlockAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        registerBlock(block, carryAdapterBlockAdapterFactory);
    }
    
    @Override public <B extends Block, A extends AbstractBlockCarryAdapter<? extends B>>
    void registerUniversal(@NotNull Set<? extends B> blocks, @NotNull ICarryBlockAdapterFactory<B, A> carryAdapterBlockAdapterFactory)
    {
        throwIf(this.frozen, ILLEGAL_REGISTER_INFO, IllegalStateException::new);
        
        requireNonNull(blocks, "Param \"blocks\" must not be null!");
        requireNonNull(carryAdapterBlockAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        for(final B block: blocks)
        {
            requireNonNull(block, "Param \"block\" must not be null!");
            requireNonNull(BuiltInRegistries.BLOCK.getKey(block), "Param \"block\"'s ResourceLocation must not be null!");
            
            registerBlock(block, carryAdapterBlockAdapterFactory);
        }
    }
    
    @Override public <E extends LivingEntity, A extends AbstractEntityCarryAdapter<E>>
    void register(@NotNull EntityType<E> entityType, @NotNull ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory)
    {
        throwIf(this.frozen, ILLEGAL_REGISTER_INFO, IllegalStateException::new);
        
        requireNonNull(entityType, "Param \"entityType\" must not be null!");
        requireNonNull(EntityType.getKey(entityType), "Param \"entityType\"'s ResourceLocation must not be null!");
        requireNonNull(carryEntityAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        registerEntity(entityType, carryEntityAdapterFactory);
    }
    
    @Override public <E extends LivingEntity, A extends AbstractEntityCarryAdapter<? extends E>>
    void registerUniversal(@NotNull Set<? extends EntityType<? extends E>> entityTypes, @NotNull ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory)
    {
        throwIf(this.frozen, ILLEGAL_REGISTER_INFO, IllegalStateException::new);
        
        requireNonNull(entityTypes, "Param \"entityTypes\" must not be null!");
        requireNonNull(carryEntityAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        for(final EntityType<? extends E> entityType: entityTypes)
        {
            requireNonNull(entityType, "Param \"entityType\" must not be null!");
            requireNonNull(EntityType.getKey(entityType), "Param \"entityType\"'s ResourceLocation must not be null!");
            
            registerEntity(entityType, carryEntityAdapterFactory);
        }
    }
    
    @SuppressWarnings("unchecked")//! javac is too stupid to deduce generics, so we choose runtime inspection instead.
    private void unsafeRegisterEntity(
        @NotNull EntityType<? extends LivingEntity> entityType,
        @NotNull ICarryEntityAdapterFactory<? extends LivingEntity, ? extends AbstractEntityCarryAdapter<?>> carryEntityAdapterFactory
    )
    {
        requireNonNull(entityType, "Param \"entityType\" must not be null!");
        requireNonNull(EntityType.getKey(entityType), "Param \"entityType\"'s ResourceLocation must not be null!");
        requireNonNull(carryEntityAdapterFactory, "Param \"carryEntityAdapterFactory\" must not be null!");
        
        if(ENTITY_REGISTRY.containsKey(entityType))
        {
            LOGGER.debug("Entity \"{}\" has already been bounded with an adapter, skipped.", entityType.getDescriptionId());
            return;
        }
        
        final var castedEntity = (EntityType<LivingEntity>) entityType;
        final var castedFactory = (ICarryEntityAdapterFactory<LivingEntity, AbstractEntityCarryAdapter<LivingEntity>>) carryEntityAdapterFactory;
        final ResourceLocation resourceLocation = EntityType.getKey(entityType);
        
        final String translationID = entityType.getDescriptionId();
        LOGGER.debug("Registered entity \"{}\".", translationID);
        
        ENTITY_REGISTRY.put(castedEntity, castedFactory);
        TRANSLATION_REGISTRY.put(resourceLocation, UIUtils.dimmedText(translationID));
        RECOVERY_ENTITY_REGISTRY.put(EntityType.getKey(entityType), castedFactory);
    }
    //endregion
    
    //region Core Registration Logics
    private <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>>
    void registerBlockEntity(@NotNull BlockEntityType<? extends E> blockEntityType, @NotNull ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory)
    {
        validateAdapterData(blockEntityType, carryAdapterBlockEntityFactory);
        
        throwIf(
            BLOCK_ENTITY_REGISTRY.containsKey(blockEntityType),
            "BlockEntity \"%s\" has already been bounded with an adapter!".formatted(blockEntityType.toString()),
            IllegalStateException::new
        );
        
        final ResourceLocation resourceLocation = BlockEntityType.getKey(blockEntityType);
        
        BLOCK_ENTITY_REGISTRY.put(blockEntityType, carryAdapterBlockEntityFactory);
        RECOVERY_BLOCK_ENTITY_REGISTRY.put(resourceLocation, carryAdapterBlockEntityFactory);
        TRANSLATION_REGISTRY.put(resourceLocation, UIUtils.dimmedText(Util.makeDescriptionId("block", resourceLocation)));
        
        LOGGER.debug("Registered blockEntity \"{}\".", blockEntityType.toString());
    }
    
    private <B extends Block, A extends AbstractBlockCarryAdapter<? extends B>>
    void registerBlock(@NotNull B block, @NotNull ICarryBlockAdapterFactory<B, A> carryAdapterBlockAdapterFactory)
    {
        validateAdapterData(block, carryAdapterBlockAdapterFactory);
        
        final String translationID = block.getDescriptionId();
        
        throwIf(
            BLOCK_REGISTRY.containsKey(block),
            "Block \"%s\" has already been bounded with an adapter!".formatted(translationID),
            IllegalStateException::new
        );
        
        final ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(block);
        
        BLOCK_REGISTRY.put(block, carryAdapterBlockAdapterFactory);
        RECOVERY_BLOCK_REGISTRY.put(resourceLocation, carryAdapterBlockAdapterFactory);
        TRANSLATION_REGISTRY.put(resourceLocation, UIUtils.dimmedText(translationID));
        LOGGER.debug("Registered block \"{}\".", block.getDescriptionId());
    }
    
    private <E extends LivingEntity, A extends AbstractEntityCarryAdapter<? extends E>>
    void registerEntity(@NotNull EntityType<? extends E> entityType, @NotNull ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory)
    {
        validateAdapterData(entityType, carryEntityAdapterFactory);
        
        final String translationID = entityType.getDescriptionId();
        
        throwIf(
            ENTITY_REGISTRY.containsKey(entityType),
            "Entity \"%s\" has already been bounded with an adapter!".formatted(translationID),
            IllegalStateException::new
        );
        
        final ResourceLocation resourceLocation = EntityType.getKey(entityType);
        
        ENTITY_REGISTRY.put(entityType, carryEntityAdapterFactory);
        RECOVERY_ENTITY_REGISTRY.put(resourceLocation, carryEntityAdapterFactory);
        TRANSLATION_REGISTRY.put(resourceLocation, UIUtils.dimmedText(translationID));
        LOGGER.debug("Registered entity \"{}\".", entityType.getDescriptionId());
    }
    
    @SuppressWarnings("ConstantValue")//! Defensive check.
    private void validateAdapterData(@NotNull Object obj, @NotNull IBaseCarryAdapterFactory<?, ?> baseCarryAdapterFactory)
    {
        final AbstractCarryAdapter<?> adapter = baseCarryAdapterFactory.create(null);
        
        throwIf(
            adapter.getPenaltyRate() < 0,
            "The penaltyRate of \"%s\"'s adapter should be non-negative! Current is: %d".formatted(obj.toString(), adapter.getPenaltyRate()),
            IllegalStateException::new
        );
        
        if(obj instanceof Block block && adapter instanceof AbstractBlockCarryAdapter<?> blockCarryAdapter)
            throwIf(
                blockCarryAdapter.getAcceptableCount() <= 0,
                "The acceptableCount of \"%s\"'s adapter should be positive! Current is: %d".
                    formatted(
                        block.getDescriptionId(),
                        blockCarryAdapter.getAcceptableCount()
                    ),
                IllegalStateException::new
            );
    }
    //endregion
    
    //region Getters
    @SuppressWarnings("unchecked")//! Danger, but relatively safe as long as the param is matched, mismatch only happens when caller did it by design.
    <F extends IBaseCarryAdapterFactory<?, ?>, K> @NotNull Optional<F> searchFactory(@NotNull CarryType carryType, @NotNull K key)
    {
        requireNonNull(carryType, "Param \"carryType\" must not be null!");
        requireNonNull(key, "Param \"key\" must not be null!");
        
        final Class<?> keyType = carryType.boundClass();
        
        if(!keyType.isAssignableFrom(key.getClass()))
            throw new IllegalArgumentException("Invalid factory creation type! Expected: %s, got: %s".formatted(keyType, key.getClass()));
        
        return Optional.ofNullable((F) REGISTRY_LOOKUP.get(carryType).get(keyType.cast(key)));
    }
    
    @SuppressWarnings("unchecked")//! Safe casting.
    <F extends IBaseCarryAdapterFactory<?, ?>> @NotNull Optional<F> searchFactory(@NotNull ResourceLocation resourceLocation)
    {
        requireNonNull(resourceLocation, "Param \"resourceLocation\" must not be null!");
        
        for(final HashMap<ResourceLocation, ? extends IBaseCarryAdapterFactory<?, ?>> map: RECOVER_LOOKUP.values())
            if(map.containsKey(resourceLocation))
                return Optional.ofNullable((F) map.get(resourceLocation));
        
        return Optional.empty();
    }
    
    public @NotNull Optional<ICarryBlockEntityAdapterFactory<?, ?>> getBlockEntityAdapter(@Nullable BlockEntityType<?> blockEntityType)
        { return Optional.ofNullable(BLOCK_ENTITY_REGISTRY.get(blockEntityType)); }
    
    public @NotNull Optional<ICarryBlockAdapterFactory<?, ?>> getBlockAdapter(@Nullable Block block)
        { return Optional.ofNullable(BLOCK_REGISTRY.get(block)); }
    
    public @NotNull Optional<ICarryEntityAdapterFactory<?, ?>> getEntityAdapter(@Nullable EntityType<?> entityType)
        { return Optional.ofNullable(ENTITY_REGISTRY.get(entityType)); }
    
    public @NotNull Optional<Component> getContentTranslation(@NotNull ResourceLocation resourceLocation)
    {
        requireNonNull(resourceLocation, "Param \"resourceLocation\" must not be null!");
        return Optional.ofNullable(TRANSLATION_REGISTRY.get(resourceLocation));
    }
    
    public @NotNull Optional<Component> getCombinedContentTranslation(@NotNull ResourceLocation resourceLocation)
    {
        requireNonNull(resourceLocation, "Param \"resourceLocation\" must not be null!");
        return getContentTranslation(resourceLocation).map(CarryCrateConstants.UI__CARRY_CRATE__CONTENT_PREFIX.get()::append);
    }
    //endregion
}
