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
     * @param <E> The detailed type of the blockEntity.
     * @param <A> The detailed type of the blockEntity's adapter.
     * @apiNote Please notes that the type that adapter supports must be completely same as blockEntityType's, 
     * even the child blockEntities are not allowed. For universal case, use 
     * <u>{@link #registerUniversal(ICarryRegistry.ICarryBlockEntityAdapterFactory, BlockEntityType[])}</u> instead.
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
     *          BaseVanillaBrewingStandAdapter::new,
     *          MyReg.SUPER_BREWING_STAND.get()
     *      );// This works.
     *  }
     * }</pre>
     */
    public <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<E>>
    void register(
        @NotNull BlockEntityType<E> blockEntityType,
        @NotNull ICarryRegistry.ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory
    ) { this.carryRegistry.register(blockEntityType, carryAdapterBlockEntityFactory); }
    
    /**
     * Registers a collection of blockEntity with a base adapter.
     * @param carryAdapterBlockEntityFactory Bounded Adapter's Contractor
     * @param blockEntityTypes Bounded BlockEntities' Types
     * @param <E> The base type of all blockEntities.
     * @param <A> The detailed type of the blockEntity's adapter, it only requires the blockEntityTypes are assignable from 
     *           its bound blockEntity's Type.
     * @implSpec <pre>{@code 
     *  @SubscribeEvent 
     *  static void register(@NotNull CarryAdapterRegisterEvent event)
     *  {
     *      event.registerUniversal(
     *          BaseVanillaFurnaceSeriesAdapter::new,
     *          BlockEntityType.FURNACE,
     *          BlockEntityType.BLAST_FURNACE,
     *          BlockEntityType.SMOKER,
     *          MyReg.IRON_FURNACE.get()
     *      );// Works!
     *  }
     * }</pre>
     */
    @SafeVarargs public final <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>> 
    void registerUniversal(
        @NotNull ICarryRegistry.ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory,
        @NotNull BlockEntityType<? extends E>... blockEntityTypes
    ) { this.carryRegistry.registerUniversal(Set.of(blockEntityTypes), carryAdapterBlockEntityFactory); }
    
    /**
     * Registers a block with its unique adapter.
     * @param block Bounded Block's Type
     * @param carryAdapterBlockAdapterFactory Bounded Adapter's Contractor
     * @param <B> The detailed type of the block.
     * @param <A> The detailed type of the block's adapter.
     * @apiNote Please notes that the type that adapter supports must be completely same as block's,
     * even the child blocks are not allowed. For universal case, use
     * <u>{@link #registerUniversal(ICarryRegistry.ICarryBlockAdapterFactory, Block[])}</u> instead.
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
     *          ProcessBlockCarryAdapter::new,
     *          MyReg.BIG_PROCESS_TABLE.get()
     *      );// This works.
     *  }
     * }</pre>
     */
    public <B extends Block, A extends AbstractBlockCarryAdapter<B>>
    void register(
        @NotNull B block,
        @NotNull ICarryRegistry.ICarryBlockAdapterFactory<B, A> carryAdapterBlockAdapterFactory
    ) { this.carryRegistry.register(block, carryAdapterBlockAdapterFactory); }
    
    /**
     * Registers a collection of block with a base adapter.
     * @param blocks Bounded Blocks' Types
     * @param carryAdapterBlockAdapterFactory Bounded Adapter's Contractor
     * @param <B> The base type of all blocks.
     * @param <A> The detailed type of the block's adapter, it only requires the blocks are assignable from
     *           its bound block's Type.
     * @implSpec <pre>{@code
     *  @SubscribeEvent 
     *  static void register(@NotNull CarryAdapterRegisterEvent event)
     *  {
     *      event.registerUniversal(
     *          LiteBlockEntityCarryAdapter::new,
     *          MyReg.LITE_LOG_BLOCK.get(),
     *          MyReg.LITE_CRAFTING_TABLE.get()
     *      );// Works!
     *  }
     * }</pre>
     */
    @SafeVarargs public final <B extends Block, A extends AbstractBlockCarryAdapter<? extends B>>
    void registerUniversal(
        @NotNull ICarryRegistry.ICarryBlockAdapterFactory<B, A> carryAdapterBlockAdapterFactory,
        @NotNull B... blocks
    ) { this.carryRegistry.registerUniversal(Set.of(blocks), carryAdapterBlockAdapterFactory); }
    
    /**
     * Registers a entity with its unique adapter.
     *
     * @param entityType Bounded Entity's Type
     * @param carryEntityAdapterFactory Bounded Adapter's Contractor
     * @param <E> The detailed type of the entity.
     * @param <A> The detailed type of the entity's adapter.
     * @apiNote Please notes that the type that adapter supports must be completely same as entityType's,
     * even the child entities are not allowed. For universal case, use
     * <u>{@link #registerUniversal(ICarryRegistry.ICarryEntityAdapterFactory, EntityType[])}</u> instead.
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
     *          MyReg.BIG_ANT.get(),
     *          AntCarryAdapter::new
     *      );// Works.
     *  }
     * }</pre>
     */
    public <E extends LivingEntity, A extends AbstractEntityCarryAdapter<E>>
    void register(
        @NotNull EntityType<E> entityType,
        @NotNull ICarryRegistry.ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory
    ) { this.carryRegistry.register(entityType, carryEntityAdapterFactory); }
    
    /**
     * Registers a collection of Entity with a base adapter.
     * @param entityTypes Bounded Entities' Types
     * @param carryEntityAdapterFactory Bounded Adapter's Contractor
     * @param <E> The base type of all entities.
     * @param <A> The detailed type of the entity's adapter, it only requires the entities are assignable from
     *           its bound entity's Type.
     * @implSpec <pre>{@code
     *  @SubscribeEvent 
     *  static void register(@NotNull CarryAdapterRegisterEvent event)
     *  {
     *      event.registerUniversal(
     *          BaseRabbitCarryAdapter::new,
     *          MyReg.BLUE_RABBIT.get(),
     *          MyReg.INVIS_RABBIT.get()
     *      );// Works!
     *  }
     * }</pre>
     */
    @SafeVarargs public final <E extends LivingEntity, A extends AbstractEntityCarryAdapter<? extends E>>
    void registerUniversal(
        @NotNull ICarryRegistry.ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory,
        @NotNull EntityType<? extends E>... entityTypes
    ) { this.carryRegistry.registerUniversal(Set.of(entityTypes), carryEntityAdapterFactory); }
}
