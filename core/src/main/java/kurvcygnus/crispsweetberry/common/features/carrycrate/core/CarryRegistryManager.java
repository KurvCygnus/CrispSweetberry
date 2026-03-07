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
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.block.AbstractBlockCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.entity.AbstractEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.entity.AdaptiveAnimalCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.events.CarryAdapterRegisterEvent;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.AbstractCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@ApiStatus.Internal
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public enum CarryRegistryManager implements ICarryRegistry
{
    INSTANCE;
    
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "CARRY_REGISTRY");
    
    private boolean frozen = false;
    
    private final HashMap<
        BlockEntityType<? extends BlockEntity>,
        ICarryBlockEntityAdapterFactory<? extends BlockEntity, ? extends AbstractBlockEntityCarryAdapter<? extends BlockEntity>>
        >
        blockEntityRegistry = new HashMap<>();
    
    private final HashMap<
        ResourceLocation,
        ICarryBlockEntityAdapterFactory<
            ? extends BlockEntity,
            ? extends AbstractBlockEntityCarryAdapter<? extends BlockEntity>
            >
        >
        recoveryBlockEntityRegistry = new HashMap<>();
    
    private final HashMap<
        Block,
        ICarryBlockAdapterFactory<? extends Block, ? extends AbstractBlockCarryAdapter<? extends Block>>
        >
        blockRegistry = new HashMap<>();
    
    private final HashMap<
        ResourceLocation,
        ICarryBlockAdapterFactory<
            ? extends Block,
            ? extends AbstractBlockCarryAdapter<? extends Block>
            >
        > 
        recoveryBlockRegistry = new HashMap<>();
    
    private final HashMap<
        EntityType<? extends LivingEntity>,
        ICarryEntityAdapterFactory<? extends LivingEntity, ? extends AbstractEntityCarryAdapter<? extends LivingEntity>>
        >
        entityRegistry = new HashMap<>();
    
    private final HashMap<
        ResourceLocation,
        ICarryEntityAdapterFactory<
            ? extends LivingEntity,
            ? extends AbstractEntityCarryAdapter<? extends LivingEntity>
            >
        >
        recoveryEntityRegistry = new HashMap<>();
    
    @SubscribeEvent static void postAdapterRegisterEvent(@NotNull FMLCommonSetupEvent event) 
    {
        event.enqueueWork(
            () ->
            {
                LOGGER.info("Starting carry adapters' registration...");
                NeoForge.EVENT_BUS.post(new CarryAdapterRegisterEvent(CarryRegistryManager.INSTANCE));
            }
        ); 
    }
    
    @SubscribeEvent static void loadComplete(@NotNull FMLLoadCompleteEvent event) 
    {
        event.enqueueWork(
            () -> 
            {
                LOGGER.debug("Registration ended, starting Entity Compat Binding...");
                
                autoEntityBind();
                
                CarryRegistryManager.INSTANCE.frozen = true;
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
                entityType ->
                {
                    //! Seems hacky? Actually, this is the best reality solution of such a situation.
                    //! EntityType has a method #getBaseClass(), which actually turns out to be hrad-coded, returning "Entity.class" only.
                    //! So, Animal.class#isAssignableFrom(Class<?>) will not work.
                    //! Then, how about EntityTypeTest#forExactClass(Class<T>)?
                    //! That is reliable, but it is a static interface method, we can't use it.
                    //! What about EntityType#create(Level)?
                    //! Level is unaccessible during game initialization, doing that is even more hacky than this.
                    //! Besides, dummy level is a hard stuff, this doesn't worth it.
                    try
                    {
                        final EntityType<? extends Animal> animal = (EntityType<? extends Animal>) entityType;
                        
                        CarryRegistryManager.INSTANCE.register(animal, AdaptiveAnimalCarryAdapter::new, false);
                    }
                    catch(ClassCastException $) { LOGGER.debug("Entity \"{}\" is not an animal. Skipped.", entityType.getDescriptionId()); }
                }
            );
    }
    
    @Override public <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<E>> void register(
        @NotNull BlockEntityType<E> blockEntityType,
        @NotNull ICarryRegistry.ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory
    )
    {
        CrispFunctionalUtils.throwIf(
            this.frozen,
            () -> new IllegalStateException("Attempting registration after registry frozen is not allowed!")
        );
        
        requireNonNull(blockEntityType, "Param \"blockEntityType\" must not be null!");
        requireNonNull(BlockEntityType.getKey(blockEntityType), "Param \"blockEntityType\"'s ResourceLocation must not be null!");
        requireNonNull(carryAdapterBlockEntityFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        validateMiscAdapter(blockEntityType, carryAdapterBlockEntityFactory);
        
        if(blockEntityRegistry.containsKey(blockEntityType))
            throw new IllegalStateException("BlockEntity \"%s\" has already been bounded with an adapter!".formatted(blockEntityType.toString()));
        
        blockEntityRegistry.put(blockEntityType, carryAdapterBlockEntityFactory);
        recoveryBlockEntityRegistry.put(BlockEntityType.getKey(blockEntityType), carryAdapterBlockEntityFactory);
        
        LOGGER.debug("Registered blockEntity \"{}\".", blockEntityType.toString());
    }
    
    @Override public <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>> void registerUniversal(
        @NotNull Set<BlockEntityType<? extends E>> blockEntityTypes,
        @NotNull ICarryRegistry.ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory
    )
    {
        CrispFunctionalUtils.throwIf(
            this.frozen,
            () -> new IllegalStateException("Attempting registration after registry frozen is not allowed!")
        );
        
        requireNonNull(blockEntityTypes, "Param \"blockEntityTypes\" must not be null!");
        requireNonNull(carryAdapterBlockEntityFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        for(final BlockEntityType<? extends E> blockEntityType: blockEntityTypes)
        {
            requireNonNull(blockEntityType, "\"blockEntityType\" must not be null!");
            requireNonNull(BlockEntityType.getKey(blockEntityType), "Param \"blockEntityType\"'s ResourceLocation must not be null!");
            
            validateMiscAdapter(blockEntityType, carryAdapterBlockEntityFactory);
            
            if(blockEntityRegistry.containsKey(blockEntityType))
                throw new IllegalStateException("BlockEntity \"%s\" has already been bounded with an adapter!".formatted(blockEntityType.toString()));
            
            blockEntityRegistry.put(blockEntityType, carryAdapterBlockEntityFactory);
            recoveryBlockEntityRegistry.put(BlockEntityType.getKey(blockEntityType), carryAdapterBlockEntityFactory);
            LOGGER.debug("Registered blockEntity \"{}\".", blockEntityType.toString());
        }
    }
    
    @SuppressWarnings("DuplicatedCode")
    @Override public <B extends Block, A extends AbstractBlockCarryAdapter<B>> 
    void register(@NotNull B block, @NotNull ICarryBlockAdapterFactory<B, A> carryAdapterBlockAdapterFactory)
    {
        CrispFunctionalUtils.throwIf(
            this.frozen,
            () -> new IllegalStateException("Attempting registration after registry frozen is not allowed!")
        );
        
        requireNonNull(block, "Param \"block\" must not be null!");
        requireNonNull(BuiltInRegistries.BLOCK.getKey(block), "Param \"block\"'s ResourceLocation must not be null!");
        requireNonNull(carryAdapterBlockAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        //! Two method's generic are not fully matched, and Java's generic is stupid,
        //! we shouldn't pursuit DRY, with readability as cost.
        //! Sometimes, KISS is better.
        validateBlockAdapter(block, carryAdapterBlockAdapterFactory);
        
        if(blockRegistry.containsKey(block))
            throw new IllegalStateException("Block \"%s\" has already been bounded with an adapter!".formatted(block.getDescriptionId()));
        
        blockRegistry.put(block, carryAdapterBlockAdapterFactory);
        recoveryBlockRegistry.put(BuiltInRegistries.BLOCK.getKey(block), carryAdapterBlockAdapterFactory);
        LOGGER.debug("Registered block \"{}\".", block.getDescriptionId());
    }
    
    @SuppressWarnings("DuplicatedCode")
    @Override public <B extends Block, A extends AbstractBlockCarryAdapter<? extends B>> 
    void registerUniversal(@NotNull Set<? extends B> blocks, @NotNull ICarryBlockAdapterFactory<B, A> carryAdapterBlockAdapterFactory)
    {
        CrispFunctionalUtils.throwIf(
            this.frozen,
            () -> new IllegalStateException("Attempting registration after registry frozen is not allowed!")
        );
        
        requireNonNull(blocks, "Param \"blocks\" must not be null!");
        requireNonNull(carryAdapterBlockAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        for(final B block: blocks)
        {
            requireNonNull(block, "Param \"block\" must not be null!");
            requireNonNull(BuiltInRegistries.BLOCK.getKey(block), "Param \"block\"'s ResourceLocation must not be null!");
            
            //! Two method's generic are not fully matched, and Java's generic is stupid,
            //! we shouldn't pursuit DRY, with readability as cost.
            //! Sometimes, KISS is better.
            validateBlockAdapter(block, carryAdapterBlockAdapterFactory);
            
            if(blockRegistry.containsKey(block))
                throw new IllegalStateException("Block \"%s\" has already been bounded with an adapter!".formatted(block.getDescriptionId()));
            
            blockRegistry.put(block, carryAdapterBlockAdapterFactory);
            recoveryBlockRegistry.put(BuiltInRegistries.BLOCK.getKey(block), carryAdapterBlockAdapterFactory);
            LOGGER.debug("Registered block \"{}\".", block.getDescriptionId());
        }
    }
    
    @Override public <E extends LivingEntity, A extends AbstractEntityCarryAdapter<E>>
    void register(@NotNull EntityType<E> entityType, @NotNull ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory) 
        { this.register(entityType, carryEntityAdapterFactory, true); }
    
    private <E extends LivingEntity, A extends AbstractEntityCarryAdapter<E>>
    void register(@NotNull EntityType<E> entityType, @NotNull ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory, boolean doThrow)
    {
        CrispFunctionalUtils.throwIf(
            this.frozen,
            () -> new IllegalStateException("Attempting registration after registry frozen is not allowed!")
        );
        
        requireNonNull(entityType, "Param \"entityType\" must not be null!");
        requireNonNull(EntityType.getKey(entityType), "Param \"entityType\"'s ResourceLocation must not be null!");
        requireNonNull(carryEntityAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        validateMiscAdapter(entityType, carryEntityAdapterFactory);
        
        if(entityRegistry.containsKey(entityType))
        {
            if(doThrow)
                throw new IllegalStateException("Entity \"%s\" has already been bounded with an adapter!".formatted(entityType.getDescriptionId()));
            
            LOGGER.debug("Entity \"{}\" has already been bounded with an adapter, skipped.", entityType.getDescriptionId());
            return;
        }
        
        entityRegistry.put(entityType, carryEntityAdapterFactory);
        recoveryEntityRegistry.put(EntityType.getKey(entityType), carryEntityAdapterFactory);
        LOGGER.debug("Registered entity \"{}\".", entityType.getDescriptionId());
    }
    
    @Override public <E extends LivingEntity, A extends AbstractEntityCarryAdapter<? extends E>> 
    void registerUniversal(@NotNull Set<EntityType<? extends E>> entityTypes, @NotNull ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory)
    {
        CrispFunctionalUtils.throwIf(
            this.frozen,
            () -> new IllegalStateException("Attempting registration after registry frozen is not allowed!")
        );
        
        requireNonNull(entityTypes, "Param \"entityTypes\" must not be null!");
        requireNonNull(carryEntityAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        for(final EntityType<? extends E> entityType: entityTypes)
        {
            requireNonNull(entityType, "Param \"entityType\" must not be null!");
            requireNonNull(EntityType.getKey(entityType), "Param \"entityType\"'s ResourceLocation must not be null!");
            
            validateMiscAdapter(entityType, carryEntityAdapterFactory);
            
            if(entityRegistry.containsKey(entityType))
                throw new IllegalStateException("Entity \"%s\" has already been bounded with an adapter!".formatted(entityType.getDescriptionId()));
            
            entityRegistry.put(entityType, carryEntityAdapterFactory);
            recoveryEntityRegistry.put(EntityType.getKey(entityType), carryEntityAdapterFactory);
            LOGGER.debug("Registered entity \"{}\".", entityType.getDescriptionId());
        }
    }
    
    public @NotNull Optional<ICarryBlockEntityAdapterFactory<? extends BlockEntity, ? extends AbstractBlockEntityCarryAdapter<?>>> 
    getBlockEntityAdapter(@NotNull BlockEntityType<?> blockEntityType) { return Optional.ofNullable(blockEntityRegistry.get(blockEntityType)); }
    
    @NotNull Optional<ICarryBlockEntityAdapterFactory<? extends BlockEntity, ? extends AbstractBlockEntityCarryAdapter<?>>>
    getBlockEntityAdapter(@NotNull ResourceLocation id) { return Optional.ofNullable(recoveryBlockEntityRegistry.get(id)); }
    
    public @NotNull Optional<ICarryBlockAdapterFactory<? extends Block, ? extends AbstractBlockCarryAdapter<?>>>
    getBlockAdapter(@NotNull Block block) { return Optional.ofNullable(blockRegistry.get(block)); }
    
    @NotNull Optional<ICarryBlockAdapterFactory<? extends Block, ? extends AbstractBlockCarryAdapter<?>>>
    getBlockAdapter(@NotNull ResourceLocation id) { return Optional.ofNullable(recoveryBlockRegistry.get(id)); }
    
    public @NotNull Optional<ICarryEntityAdapterFactory<? extends LivingEntity, ? extends AbstractEntityCarryAdapter<?>>>
    getEntityAdapter(@NotNull EntityType<?> entityType) { return Optional.ofNullable(entityRegistry.get(entityType)); }
    
    @NotNull Optional<ICarryEntityAdapterFactory<? extends LivingEntity, ? extends AbstractEntityCarryAdapter<?>>>
    getEntityAdapter(@NotNull ResourceLocation id) { return Optional.ofNullable(recoveryEntityRegistry.get(id)); }
    
    @SuppressWarnings("ConstantValue")//! Defensive check.
    private void validateMiscAdapter(@NotNull Object obj, @NotNull IBaseCarryAdapterFactory<?, ?> baseCarryAdapterFactory)
    {
        final AbstractCarryAdapter adapter = baseCarryAdapterFactory.create(null);
        
        CrispFunctionalUtils.throwIf(
            adapter.getPenaltyRate() < 0,
            () -> new IllegalStateException(
                "The penaltyRate of \"%s\"'s adapter should be non-negative! Current is: %d".
                    formatted(obj.toString(), adapter.getPenaltyRate())
            )
        );
    }
    
    @SuppressWarnings("ConstantValue")//! Defensive check.
    private void validateBlockAdapter(@NotNull Block block, @NotNull ICarryBlockAdapterFactory<?, ?> carryAdapterBlockAdapterFactory)
    {
        final AbstractBlockCarryAdapter<?> adapter = carryAdapterBlockAdapterFactory.create(null);
        
        CrispFunctionalUtils.throwIf(
            adapter.getPenaltyRate() < 0,
            () -> new IllegalStateException(
                "The penaltyRate of \"%s\"'s adapter should be non-negative! Current is: %d".
                    formatted(block.getDescriptionId(), adapter.getPenaltyRate())
            )
        );
        CrispFunctionalUtils.throwIf(
            adapter.getAcceptableCount() <= 0,
            () -> new IllegalStateException(
                "The acceptableCount of \"%s\"'s adapter should be positive! Current is: %d".
                    formatted(block.getDescriptionId(), adapter.getAcceptableCount())
            )
        );
    }
}
