//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.events;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.block.AbstractBlockCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.entity.AbstractEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A event that makes adapter's registration available.<br>
 * It is fired during the <u>{@link net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent common setup}</u>.
 * @apiNote Before registration compat, it's strongly recommended to see <u>{@link AbstractBlockCarryAdapter}</u> and 
 * <u>{@link AbstractBlockEntityCarryAdapter}</u> first, since they have different constraints from Vanilla's principle.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class CarryAdapterRegisterEvent extends Event
{
    private final ICarryRegistry carryRegistry;
    
    public CarryAdapterRegisterEvent(@NotNull ICarryRegistry carryRegistry) { this.carryRegistry = carryRegistry; }
    
    /**
     * Registers a blockEntity with its unique adapter.
     * @param blockEntityType Bounded BlockEntity's Type
     * @param carryAdapterBlockEntityFactory Bounded Adapter's Contractor
     * @apiNote Please notes that the type that adapter supports must be completely same as blockEntityType's, 
     * even the child blockEntities are not allowed. For universal case, use 
     * <u>{@link #registerUniversal(Set, ICarryRegistry.ICarryBlockEntityAdapterFactory)}</u> instead.
     * @implSpec <pre>{@code
     *  @SubscribeEvent 
     *  static void register(@NotNull CarryAdapterRegisterEvent event)
     *  {
     *      event.register(
     *          BlockEntityType.BREWING_STAND,
     *          BaseVanillaBrewingStandAdapter::new
     *      );// OK
     *      
     *      event.register(
     *          MyReg.SUPER_BREWING_STAND,
     *          BaseVanillaBrewingStandAdapter::new
     *      );// This won't work.
     *      
     *      event.registerUniversal(
     *          Set.of(MyReg.SUPER_BREWING_STAND.get()),
     *          BaseVanillaBrewingStandAdapter::new
     *      );// This works.
     *  }
     * }</pre>
     */
    @SuppressWarnings("unchecked")//! javac is too stupid to deduce generics, so we choose runtime inspection instead.
    public void register(
        @NotNull BlockEntityType<? extends BlockEntity> blockEntityType,
        @NotNull ICarryRegistry.ICarryBlockEntityAdapterFactory<? extends BlockEntity, ? extends AbstractBlockEntityCarryAdapter<?>> carryAdapterBlockEntityFactory
    )
    {
        final var castedFactory = (ICarryRegistry.ICarryBlockEntityAdapterFactory<BlockEntity, AbstractBlockEntityCarryAdapter<BlockEntity>>) 
            carryAdapterBlockEntityFactory;
        final var castedType = (BlockEntityType<BlockEntity>) blockEntityType;
        
        this.carryRegistry.register(castedType, castedFactory);
    }
    
    /**
     * Registers a collection of blockEntity with a base adapter.
     * @param carryAdapterBlockEntityFactory Bounded Adapter's Contractor
     * @param blockEntityTypes Bounded BlockEntities' Types
     * @implSpec <pre>{@code 
     *  @SubscribeEvent 
     *  static void register(@NotNull CarryAdapterRegisterEvent event)
     *  {
     *      event.registerUniversal(
     *          Set.of(
     *              BlockEntityType.FURNACE,
     *              BlockEntityType.BLAST_FURNACE,
     *              BlockEntityType.SMOKER,
     *              MyReg.IRON_FURNACE.get()
     *          ),
     *          BaseVanillaFurnaceSeriesAdapter::new
     *      );// Works!
     *  }
     * }</pre>
     */
    @SuppressWarnings("unchecked")//! javac is too stupid to deduce generics, so we choose runtime inspection instead.
    public void registerUniversal(
        @NotNull Set<? extends BlockEntityType<? extends BlockEntity>> blockEntityTypes,
        @NotNull ICarryRegistry.ICarryBlockEntityAdapterFactory<? extends BlockEntity, ? extends AbstractBlockEntityCarryAdapter<?>> carryAdapterBlockEntityFactory
    ) 
    {
        final var castedFactory = (ICarryRegistry.ICarryBlockEntityAdapterFactory<BlockEntity, AbstractBlockEntityCarryAdapter<BlockEntity>>)
            carryAdapterBlockEntityFactory;
        final var castedTypes = (Set<BlockEntityType<BlockEntity>>) blockEntityTypes;
        
        this.carryRegistry.registerUniversal(castedTypes, castedFactory);
    }
    
    /**
     * Registers a block with its unique adapter.
     * @param block Bounded Block's Type
     * @param carryAdapterBlockAdapterFactory Bounded Adapter's Contractor
     * @apiNote Please notes that the type that adapter supports must be completely same as block's,
     * even the child blocks are not allowed. For universal case, use
     * <u>{@link #registerUniversal(Set, ICarryRegistry.ICarryBlockAdapterFactory)}</u> instead.
     * @implSpec <pre>{@code
     *  @SubscribeEvent 
     *  static void register(@NotNull CarryAdapterRegisterEvent event)
     *  {
     *      event.register(
     *          MyReg.PROCESS_TABLE.get(),
     *          ProcessBlockCarryAdapter::new
     *      );// Works.
     *
     *      event.register(
     *          MyReg.BIG_PROCESS_TABLE.get(),
     *          ProcessBlockCarryAdapter::new
     *      );// This won't work.
     *
     *      event.registerUniversal(
     *          Set.of(MyReg.BIG_PROCESS_TABLE.get()),
     *          ProcessBlockCarryAdapter::new
     *      );// This works.
     *  }
     * }</pre>
     */
    public void register(
        @NotNull Block block,
        @NotNull ICarryRegistry.ICarryBlockAdapterFactory<? extends Block, ? extends AbstractBlockCarryAdapter<?>> carryAdapterBlockAdapterFactory
    ) 
    {
        @SuppressWarnings("unchecked")//! javac is too stupid to deduce generics, so we choose runtime inspection instead.
        final var castedFactory = (ICarryRegistry.ICarryBlockAdapterFactory<Block, AbstractBlockCarryAdapter<Block>>) carryAdapterBlockAdapterFactory;
        
        this.carryRegistry.register(block, castedFactory);
    }
    
    /**
     * Registers a collection of block with a base adapter.
     *
     * @param blocks                          Bounded Blocks' Types
     * @param carryAdapterBlockAdapterFactory Bounded Adapter's Contractor
     *                                        its bound block's Type.
     * @implSpec <pre>{@code
     *  @SubscribeEvent
     *  static void register(@NotNull CarryAdapterRegisterEvent event)
     *  {
     *      event.registerUniversal(
     *          Set.of(
     *              MyReg.LITE_LOG_BLOCK.get(),
     *              MyReg.LITE_CRAFTING_TABLE.get()
     *          ),
     *          LiteBlockEntityCarryAdapter::new
     *      );// Works!
     *  }
     * }</pre>
     */
    @SuppressWarnings("unchecked")//! javac is too stupid to deduce generics, so we choose runtime inspection instead.
    public void registerUniversal(
        @NotNull Set<? extends Block> blocks,
        @NotNull ICarryRegistry.ICarryBlockAdapterFactory<? extends Block, ? extends AbstractBlockCarryAdapter<?>> carryAdapterBlockAdapterFactory
    )
    {
        final var castedBlocks = (Set<Block>) blocks;
        final var castedFactory = (ICarryRegistry.ICarryBlockAdapterFactory<Block, AbstractBlockCarryAdapter<Block>>) carryAdapterBlockAdapterFactory;
        
        this.carryRegistry.registerUniversal(castedBlocks, castedFactory);
    }
    
    /**
     * Registers a entity with its unique adapter.
     *
     * @param entityType Bounded Entity's Type
     * @param carryEntityAdapterFactory Bounded Adapter's Contractor
     * @apiNote Please notes that the type that adapter supports must be completely same as entityType's,
     * even the child entities are not allowed. For universal case, use
     * <u>{@link #registerUniversal(Set, ICarryRegistry.ICarryEntityAdapterFactory)}</u> instead.
     * @implSpec <pre>{@code
     *  @SubscribeEvent 
     *  static void register(@NotNull CarryAdapterRegisterEvent event)
     *  {
     *      event.register(
     *          MyReg.ANT.get(),
     *          AntCarryAdapter::new
     *      );// Works.
     *      
     *      event.register(
     *          MyReg.BIG_ANT.get(),
     *          AntCarryAdapter::new
     *      );// This won't work.
     *      
     *      event.registerUniversal(
     *          Set.of(MyReg.BIG_ANT.get()),
     *          AntCarryAdapter::new
     *      );// Works.
     *  }
     * }</pre>
     */
    @SuppressWarnings("unchecked")//! javac is too stupid to deduce generics, so we choose runtime inspection instead.
    public void register(
        @NotNull EntityType<? extends LivingEntity> entityType,
        @NotNull ICarryRegistry.ICarryEntityAdapterFactory<? extends LivingEntity, ? extends AbstractEntityCarryAdapter<?>> carryEntityAdapterFactory
    ) 
    {
        final var castedEntity = (EntityType<LivingEntity>) entityType;
        final var castedFactory = (ICarryRegistry.ICarryEntityAdapterFactory<LivingEntity, AbstractEntityCarryAdapter<LivingEntity>>) carryEntityAdapterFactory;
        
        this.carryRegistry.register(castedEntity, castedFactory);
    }
    
    /**
     * Registers a collection of Entity with a base adapter.
     *
     * @param entityTypes               Bounded Entities' Types
     * @param carryEntityAdapterFactory Bounded Adapter's Contractor
     *                                  its bound entity's Type.
     * @implSpec <pre>{@code
     *  @SubscribeEvent
     *  static void register(@NotNull CarryAdapterRegisterEvent event)
     *  {
     *      event.registerUniversal(
     *          Set.of(
     *              MyReg.BLUE_RABBIT.get(),
     *              MyReg.INVIS_RABBIT.get()
     *          ),
     *          BaseRabbitCarryAdapter::new
     *      );// Works!
     *  }
     * }</pre>
     */
    @SuppressWarnings("unchecked")//! javac is too stupid to deduce generics, so we choose runtime inspection instead.
    public void registerUniversal(
        @NotNull Set<? extends EntityType<? extends LivingEntity>> entityTypes,
        @NotNull ICarryRegistry.ICarryEntityAdapterFactory<? extends LivingEntity, ? extends AbstractEntityCarryAdapter<?>> carryEntityAdapterFactory
    )
    {
        final var castedEntities = (Set<EntityType<LivingEntity>>) entityTypes;
        final var castedFactory = (ICarryRegistry.ICarryEntityAdapterFactory<LivingEntity, AbstractEntityCarryAdapter<LivingEntity>>) carryEntityAdapterFactory;
        
        this.carryRegistry.registerUniversal(castedEntities, castedFactory);
    }
}
