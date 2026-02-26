//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.blockstates;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.config.CrispConfig;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnBlock;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnContainerData;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnRecipeCacheEvent;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnRegistries;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.CalculationResult;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressCalculator;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressModel;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.InputState;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.LogicalResult;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.VisualTrend;
import kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnMenu;
import kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnOutputSlot;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipe;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipeManager;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;
import java.util.Optional;

import static kurvcygnus.crispsweetberry.common.features.kiln.KilnConstants.*;
import static kurvcygnus.crispsweetberry.common.features.kiln.KilnContainerData.TRUE;
import static kurvcygnus.crispsweetberry.common.features.kiln.integration.KilnCarriableExtensions.*;

/**
 * The <b>container part</b> of <b>Kiln Block</b>, which is responsible for <b>containment, sync and logical</b> things.
 *
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressModel Progress Model
 * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressCalculator Progress Calculator
 * @see KilnContainerData Data Sync Part
 * @see KilnMenu Menu Part
 * @see KilnRecipeCacheEvent Recipe Initialization
 * @see KilnRecipe Recipe Definition
 * @since 1.0 Release
 */
public sealed class KilnBlockEntity extends BaseContainerBlockEntity 
implements MenuProvider, WorldlyContainer, IKilnCarriableBlockEntityBridge permits KilnDummyBlockEntity
{
    //  region
    //* Constants & Fields
    //*:=== Constants
    private static final int[] INPUT_SLOTS = {
        KILN_INPUT_SLOTS_RANGE.getMin(),
        KILN_INPUT_SLOTS_RANGE.getMin() + 1,
        KILN_INPUT_SLOTS_RANGE.getMax()
    };
    
    private static final int[] OUTPUT_SLOTS = {
        KILN_OUTPUT_SLOTS_RANGE.getMin(),
        KILN_OUTPUT_SLOTS_RANGE.getMin() + 1,
        KILN_OUTPUT_SLOTS_RANGE.getMax()
    };
    
    private static final String NBT_TAG_VISUAL_PROGRESS = "visualProgress";
    private static final String NBT_TAG_REAL_PROGRESS = "realProgress";
    private static final String NBT_TAG_EXPERIENCE = "experience";
    private static final String NBT_TAG_LIT = "lit";
    private static final String NBT_TAG_BALANCE_TICK = "balanceTick";
    private static final String NBT_TAG_BALANCE_RATE = "balanceRate";
    
    public static final KilnRecipe EMPTY_RECIPE = new KilnRecipe(
        Ingredient.EMPTY,
        ItemStack.EMPTY,
        0D,
        0F,
        false
    );
    
    //*:=== Fields
    //*:== Slot Data
    private NonNullList<ItemStack> containerItems = NonNullList.withSize(KILN_DEFAULT_SIZE, ItemStack.EMPTY);
    private final NonNullList<KilnRecipe> recipeCache = NonNullList.withSize(KILN_INPUT_SLOTS_RANGE.size(), EMPTY_RECIPE);
    
    //*:== Components
    /**
     * Field <u>{@link KilnProgressModel KilnProgressModel}</u> keeps
     * both real and visual progress of the kiln, and the model itself has some private methods specifically for fixing value.
     */
    public final KilnProgressModel model = new KilnProgressModel();
    private final KilnProgressCalculator calculator = new KilnProgressCalculator();
    
    private final ContainerData data = new KilnContainerData(this);
    
    //*:== Logical Procession Data
    private InputState inputState = InputState.ALL_EMPTY;
    private float experience = 0F;
    
    public enum ProcessionState { WORKING, COOLDOWN }
    
    public record EmulateResult(boolean canProcess, float exp)
    {
        public static final @NotNull EmulateResult FAILED = new EmulateResult(false, 0F);
        
        public static @NotNull EmulateResult success(@Range(from = 0, to = (long) Float.MAX_VALUE) float exp)
            { return new EmulateResult(true, exp); }
    }
    
    //*:== Logger
    private static final MarkLogger LOGGER = MarkLogger.marklessLogger(LogUtils.getLogger());
    //endregion
    
    //  region
    //* Constructor & Basic Info Declaration
    public KilnBlockEntity(@NotNull BlockPos pos, @NotNull BlockState blockState) { super(KilnRegistries.KILN_BLOCK_ENTITY.get(), pos, blockState); }
    
    //*:=== Hopper Support Essentials
    @Override public int @NotNull [] getSlotsForFace(@NotNull Direction side)
    {
        if(side == Direction.DOWN)
            return OUTPUT_SLOTS;
        
        return INPUT_SLOTS;
    }
    
    @Override public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, Direction dir)
    {
        if(!KILN_INPUT_SLOTS_RANGE.inRange(slot))
            return false;
        
        return canSmelt(stack);
    }
    
    @Override public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction)
        { return direction == Direction.DOWN && KILN_OUTPUT_SLOTS_RANGE.inRange(index); }
    
    //*:=== Container Basics
    @Override protected @NotNull Component getDefaultName() { return CONTAINER__KILN; }
    
    @Override protected @NotNull NonNullList<ItemStack> getItems() { return this.containerItems; }
    
    @Override protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory)
        { return new KilnMenu(containerId, inventory, this); }
    
    @Override public int getContainerSize() { return KILN_DEFAULT_SIZE; }
    //endregion
    
    //  region
    //* Block Entity Interaction Basics
    @Override protected void setItems(@NotNull NonNullList<ItemStack> items) { this.containerItems = items; }
    
    /**
     * @implNote Do not add flags like {@code slotChanged} to make <u>{@link #updateInputSlotsInfo()}</u> event-driven based.
     * Neither <u>{@link ItemStack#isSameItem ItemStack#isSameItem()}</u> nor <u>{@link ItemStack#isSameItemSameComponents ItemStack#isSameItemSameComponents()}</u>
     * can fully cover boundary cases. Mixed other logics with them together mostly also won't settle these issues.
     */
    @Override public void setItem(int index, @NotNull ItemStack stack)
    {
        final ItemStack oldItemStack = containerItems.get(index);
        
        try(MarkLogger.MarkerHandle handle = LOGGER.pushMarker("INPUT_CHECK"))
        {
            configDebug("Before size limitation -> index: {}, old: [name: {}, quantity: {}], new: [name: {}, quantity: {}]",
                index, oldItemStack.getDisplayName(), oldItemStack.getCount(), stack.getDisplayName(), stack.getCount()
            );
            
            stack.limitSize(this.getMaxStackSize(stack));
            this.containerItems.set(index, stack);
            
            if(level != null && !level.isClientSide)
            {
                if(KILN_INPUT_SLOTS_RANGE.inRange(index))
                {
                    handle.changeMarker("UPDATE_INFO");
                    configDebug("index: {} -> at input range -> go update input slots' information", index);
                    updateInputSlotsInfo();
                }
                
                setChanged(level, worldPosition, this.getBlockState());// If the content in the container is changed, the data must markedLogger dirtied.
            }
        }
    }
    
    @Override public boolean canPlaceItem(int index, @NotNull ItemStack stack)
    {
        if(!KILN_INPUT_SLOTS_RANGE.inRange(index))
            return false;
        
        return canSmelt(stack);
    }
    
    private void updateInputSlotsInfo()
    {
        inputState = InputState.ALL_EMPTY;
        
        final LogicalResult nonWorkingLogicalResult;
        
        try(MarkLogger.MarkerHandle handle = LOGGER.pushMarker("INPUT_CHECK"))
        {
            for(int slotIndex = KILN_INPUT_SLOTS_RANGE.getMin(); KILN_INPUT_SLOTS_RANGE.inRange(slotIndex); slotIndex++)
            {
                final int cacheIndex = slotIndex - KILN_INPUT_SLOTS_RANGE.getMin();
                final ItemStack stackInSlot = containerItems.get(slotIndex);
                
                configDebug("slotIndex: {}, cacheIndex: {}, content: {}",
                    slotIndex, cacheIndex, stackInSlot.getDisplayName()
                );
                
                if(stackInSlot.isEmpty())
                    recipeCache.set(slotIndex, EMPTY_RECIPE);
                else if(canSmelt(stackInSlot))
                {
                    if(inputState != InputState.VALID)
                    {
                        handle.changeMarker("INPUT_STATE_CHANGED");
                        configDebug("{} -> VALID (at slot {}, stack: {})",
                            inputState, slotIndex, stackInSlot.getDisplayName()
                        );
                    }
                    
                    inputState = InputState.VALID;
                    final Optional<KilnRecipe> optionalKilnRecipe = getKilnRecipe(stackInSlot);
                    
                    if(optionalKilnRecipe.isPresent())
                    {
                        final KilnRecipe kilnRecipe = optionalKilnRecipe.get();
                        
                        recipeCache.set(cacheIndex, kilnRecipe);
                        
                        if(kilnRecipe.isBanned())
                        {
                            //* Kiln doesn't support blasting recipes since they require huge heats, which can't afforded by kiln,
                            //* and thus, we should tip players about this.
                            inputState = InputState.HAS_TIP;
                            break;
                        }
                    }
                    else
                        recipeCache.set(cacheIndex, EMPTY_RECIPE);
                    
                    handle.changeMarker("CACHE_WRITE");
                    configDebug("write cache[{}] from slot {} -> {}",
                        cacheIndex, slotIndex, optionalKilnRecipe
                    );
                }
                else
                {
                    //* This is vanilla behavior: when something can't be processed in the container, the entire procession will be paused.
                    recipeCache.set(cacheIndex, EMPTY_RECIPE);
                    break;
                }
                
                final KilnRecipe cache = recipeCache.get(cacheIndex);
                
                handle.changeMarker("CACHE_CONFIRM");
                configDebug("new cache at index {}: [Ingredient: {}, Result: {}]",
                    slotIndex, cache.ingredient(), cache.result()
                );
            }
            
            switch(inputState)
            {
                case HAS_TIP -> nonWorkingLogicalResult = LogicalResult.BLAST_TIP;
                case VALID -> nonWorkingLogicalResult = LogicalResult.CONTINUE;
                default -> nonWorkingLogicalResult = LogicalResult.SKIP;
            }
            
            handle.changeMarker("PULL_CACHE");
            configDebug(
                "\"Calculator#setRecipesAndResultType\" called. inputState = {}, recipes = {}, nonWorkingLogicalResult = {}",
                inputState,
                recipeCache.toString(),
                nonWorkingLogicalResult.name()
            );
        }
        
        //* Sync recipes here to minimalize performance penalty instead of doing this in serverTick.
        this.calculator.setRecipesAndResultType(recipeCache, nonWorkingLogicalResult);
    }
    
    /**
     * @apiNote This method is called by <u>{@link KilnOutputSlot#onTake(Player, ItemStack) KilnOutputSlot#onTake()}</u>, not the blockEntity itself.
     */
    public void dropExperience(@NotNull Player player)
    {
        final int exp = Mth.floor(this.experience);
        ExperienceOrb.award((ServerLevel) level, player.position(), exp);
        
        this.experience = 0F;
    }
    //endregion
    
    //  region
    //* Lifecycle & Logics
    public static void serverTick(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull KilnBlockEntity blockEntity)
    {
        final boolean isIgnited = state.getValue(KilnBlock.LIT);
        
        final ProcessionState currentState = blockEntity.deduceProcessState(isIgnited);
        
        final CalculationResult result = blockEntity.calculator.calculateRates(blockEntity.model.getRealProgress(), blockEntity.model.getVisualProgress(), currentState);
        
        final boolean worldDirty = statelessTick(level, pos, blockEntity, result, isIgnited);
        
        if(worldDirty)
            setChanged(level, pos, state);
    }
    
    private static boolean statelessTick(
        @NotNull Level level,
        @NotNull BlockPos pos,
        @NotNull KilnBlockEntity blockEntity,
        @NotNull CalculationResult result,
        boolean isIgnited
    ) { return statelessTick(level, pos, blockEntity, result, isIgnited, 1, false); }
    
    private static boolean statelessTick(
        @NotNull Level level,
        @NotNull BlockPos pos,
        @NotNull KilnBlockEntity blockEntity,
        @NotNull CalculationResult result,
        boolean isIgnited,
        int processRound,
        boolean isStateless
    )
    {
        blockEntity.model.synchronize(result.currentRealProgress(), result.currentVisualProgress(), result.trend(), isIgnited, isStateless);
        
        switch(result.logicalResult())
        {
            case CONTINUE, BALANCING -> {}
            case INVALID ->
            {
                try(MarkLogger.MarkerHandle ignored = LOGGER.pushMarker("UNEXPECTED_RESULT"))
                    { LOGGER.error("Received unexpected result \"{}\"", result.logicalResult().name()); }
            }
            case SKIP -> { return false; }
        }
        
        final boolean isFinishedProcession = blockEntity.model.upgradeProgress(isStateless);
        
        boolean worldDirty = false;
        
        for(int round = 0; round < processRound && isFinishedProcession; round++)
        {
            final EmulateResult processResult = processInputItems(blockEntity, level);
            
            worldDirty |= processResult.canProcess;
            worldDirty |= checkAndShrinkInputItems(blockEntity, level, pos);
            
            if(processResult.canProcess)
                blockEntity.experience += processResult.exp;
        }
        
        return worldDirty;
    }
    
    /**
     * @apiNote Block <u>{@link LogicalResult#BALANCING BALANCING}</u>
     * is strictly considered as a special variation of
     * <u>{@link ProcessionState#WORKING WORKING}</u>, instead of being an independent attachTag,
     * it's deduced, only being used in
     * <u>{@link KilnProgressCalculator#calculateRates Calculation}</u>
     * and returned as a part of <u>{@link CalculationResult CalculationResult}</u>
     * for menu visual change.
     */
    private @NotNull ProcessionState deduceProcessState(boolean isIgnited)
    {
        if(!isIgnited || this.inputState != InputState.VALID)
            return ProcessionState.COOLDOWN;
        
        return ProcessionState.WORKING;
    }
    
    private static @NotNull EmulateResult processInputItems(@NotNull KilnBlockEntity blockEntity, @NotNull Level level)
    {
        final ItemStack[] resultStacks = new ItemStack[KILN_OUTPUT_SLOTS_RANGE.size()];
        
        for(int index = 0; index < KILN_SLOT_COUNT_FOR_EACH_TYPE; index++)
            resultStacks[index] = blockEntity.recipeCache.get(index).getResultItem(level.registryAccess());
        
        //* Since the procession is complex, using emulation-then-apply strategy can sufficiently decrease the quantity of boundary cases.
        final NonNullList<ItemStack> emulatedOutputSlots = copyOutputSlots(blockEntity.containerItems);
        
        final EmulateResult insertResult = insertItemsToSlots(blockEntity, resultStacks, emulatedOutputSlots);
        
        if(!insertResult.canProcess)
            return EmulateResult.FAILED;
        
        //* Emulation succeed, then apply emulated results.
        for(int index = 0; index < KILN_SLOT_COUNT_FOR_EACH_TYPE; index++)
        {
            final int outputIndex = index + KILN_OUTPUT_SLOTS_RANGE.getMin();
            blockEntity.containerItems.set(outputIndex, emulatedOutputSlots.get(index).copy());
        }
        
        return EmulateResult.success(insertResult.exp);
    }
    
    /**
     * @implNote This method emulates the insertion of result items by trying to insert one item into three different slots<i>(9 attempts in most cases)</i>,
     * only when both of three result item insertions are succeed, then the method will return {@code true} to tell
     * <u>{@link #processInputItems processInputItems()}</u> to apply emulated results.
     */
    private static @NotNull EmulateResult insertItemsToSlots(
        @NotNull KilnBlockEntity blockEntity,
        @NotNull ItemStack[] resultStacks,
        @NotNull NonNullList<ItemStack> emulatedOutputSlots
    )
    {
        float exp = 0F;
        
        for(int slotIndex = 0; slotIndex < KILN_SLOT_COUNT_FOR_EACH_TYPE; slotIndex++)
        {
            int invalidAttempts = 0;//! Both result mismatch with slot content and unable to stack to output slots are counted as invalid attempts.
            Integer emptyStackIndex = null;
            final ItemStack resultStack = resultStacks[slotIndex];
            
            for(int attemptSlotIndex = 0; attemptSlotIndex < 3; attemptSlotIndex++)
            {
                final ItemStack emulatedOutputStack = emulatedOutputSlots.get(attemptSlotIndex);
                
                if(!canMerge(resultStack, emulatedOutputStack))
                {
                    invalidAttempts++;
                    
                    if(emptyStackIndex == null && emulatedOutputStack.isEmpty())
                        emptyStackIndex = attemptSlotIndex;//! Only assigns the earliest matched index to emptyStackIndex to make sure the merge
                                                           //! behavior is same as vanilla.
                    
                    if(invalidAttempts >= 3)
                    {
                        if(emptyStackIndex != null)//! If no slot meets the condition, put this result item into the earliest empty slot.
                        {
                            emulatedOutputSlots.set(emptyStackIndex, resultStack);
                            break;
                        }
                        else
                            return EmulateResult.FAILED;
                    }
                    continue;
                }
                
                exp += blockEntity.recipeCache.get(slotIndex).experience();
                emulatedOutputStack.setCount(emulatedOutputStack.getCount() + 1);
            }
        }
        
        return EmulateResult.success(exp);
    }
    
    private static @NotNull NonNullList<ItemStack> copyOutputSlots(NonNullList<ItemStack> outputStacks)
    {
        final NonNullList<ItemStack> copy = NonNullList.withSize(KILN_SLOT_COUNT_FOR_EACH_TYPE, ItemStack.EMPTY);
        
        for(int index = KILN_SLOT_COUNT_FOR_EACH_TYPE; index < KILN_DEFAULT_SIZE; index++)
        {
            final int copyIndex = index - KILN_SLOT_COUNT_FOR_EACH_TYPE;
            
            if(!outputStacks.get(index).equals(copy.get(copyIndex)))
                copy.set(copyIndex, outputStacks.get(index));
        }
        
        return copy;
    }
    
    private static boolean canMerge(@NotNull ItemStack resultStack, @NotNull ItemStack emulatedOutputStack)
    {
        return ItemStack.isSameItemSameComponents(resultStack, emulatedOutputStack) &&
            emulatedOutputStack.getCount() < emulatedOutputStack.getMaxStackSize();
    }
    
    private static boolean checkAndShrinkInputItems(@NotNull KilnBlockEntity blockEntity, @Nullable Level level, @NotNull BlockPos pos)
    {
        if(level == null || level.isClientSide)
            return false;
        
        for(int index = KILN_INPUT_SLOTS_RANGE.getMin(); KILN_INPUT_SLOTS_RANGE.inRange(index); index++)
        {
            final ItemStack stack = blockEntity.containerItems.get(index);
            final ItemStack remainingStack;
            
            if(stack.isEmpty())
                continue;
            
            if(!stack.hasCraftingRemainingItem())
            {
                stack.shrink(1);
                continue;
            }
            
            remainingStack = stack.getCraftingRemainingItem().copy();
            
            if(stack.getCount() == 1)
                blockEntity.containerItems.set(index, remainingStack);
            else
            {   //* Pop out the remaining item near the container.
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainingStack);
                stack.shrink(1);
            }
        }
        
        return true;
    }
    
    @Override public boolean stillValid(@NotNull Player player) { return Container.stillValidBlockEntity(this, player); }
    //endregion
    
    //  region
    //* Data Serialization
    @Override protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.loadAdditional(tag, registries);
        
        //* Reinitialize first, then load contents.
        this.containerItems = NonNullList.withSize(KILN_DEFAULT_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.containerItems, registries);
        
        final double loadedVisualProgress = tag.contains(NBT_TAG_VISUAL_PROGRESS, Tag.TAG_DOUBLE) ? tag.getDouble(NBT_TAG_VISUAL_PROGRESS) : 0D;
        final double loadedRealProgress = tag.contains(NBT_TAG_REAL_PROGRESS, Tag.TAG_DOUBLE) ? tag.getDouble(NBT_TAG_REAL_PROGRESS) : 0D;
        final float loadedExp = tag.contains(NBT_TAG_EXPERIENCE, Tag.TAG_FLOAT) ? tag.getFloat(NBT_TAG_EXPERIENCE) : 0F;
        final byte loadedLitProperty = tag.contains(NBT_TAG_LIT, Tag.TAG_BYTE) ? tag.getByte(NBT_TAG_LIT) : TRUE;
        final byte loadedBalanceTick = tag.contains(NBT_TAG_BALANCE_TICK, Tag.TAG_BYTE) ? tag.getByte(NBT_TAG_BALANCE_TICK) : 0;
        final double loadedBalanceRate = tag.contains(NBT_TAG_BALANCE_RATE, Tag.TAG_DOUBLE) ? tag.getDouble(NBT_TAG_BALANCE_RATE) : 0D;
        
        this.model.synchronize(loadedRealProgress, loadedVisualProgress, VisualTrend.NONE, loadedLitProperty == TRUE);
        this.calculator.synchronize(loadedBalanceTick, loadedBalanceRate);
        
        this.experience = loadedExp;
        this.inputState = InputState.ALL_EMPTY;
        this.updateInputSlotsInfo();
    }
    
    @Override protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.putDouble(NBT_TAG_VISUAL_PROGRESS, this.model.getVisualProgress());
        tag.putDouble(NBT_TAG_REAL_PROGRESS, this.model.getRealProgress());
        tag.putFloat(NBT_TAG_EXPERIENCE, this.experience);
        tag.putByte(NBT_TAG_LIT, (byte) this.model.getIgnitionState());
        tag.putByte(NBT_TAG_BALANCE_TICK, this.calculator.getBalanceTick());
        tag.putDouble(NBT_TAG_BALANCE_RATE, this.calculator.getBalanceRate());
        
        ContainerHelper.saveAllItems(tag, this.containerItems, registries);
    }
    //endregion
    
    //  region
    //*:=== Carry Crate Compat
    @Override @CheckReturnValue public @NotNull KilnBlockEntityContext 
    onCarriedSequence(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ServerPlayer player)
    {
        final boolean isLit = state.getValue(KilnBlock.LIT);
        final double realRate = this.calculator.onCarriedSequence();
        
        return new KilnBlockEntityContext(isLit, realRate);
    }
    
    @Override public void carryTick(@NotNull ServerLevel level, long carryingTime, @NotNull KilnBlockEntityContext context, @NotNull BlockPos pos)
    {
        final AtomicCalculationResult result = this.calculator.statelessCalculate(new CalculationContext(
            carryingTime,
            this.model.getRealProgress(),
            this.model.getVisualProgress(),
            context.realRate(),
            context.isLit() ? ProcessionState.WORKING : ProcessionState.COOLDOWN
        ));
        
        statelessTick(level, pos, this, result.calculationResult(), context.isLit(), result.theoreticalProcessRound(), true);
    }
    //endregion
    
    //  region
    //* Helpers, Getters & Setters
    /**
     * Helper methods to check item validation.
     */
    private boolean canSmelt(@NotNull ItemStack itemstack) 
    {
        final Optional<KilnRecipe> recipe = getKilnRecipe(itemstack);
        
        return recipe.isPresent() && !recipe.get().isBanned();
    }
    
    public @NotNull Optional<KilnRecipe> getKilnRecipe(@NotNull ItemStack stackInSlot)
    {
        final Optional<NonNullList<KilnRecipe>> list = Optional.ofNullable(KilnRecipeManager.INSTANCE.getRecipes().get(stackInSlot.getItem()));
        
        return list.map(List::getFirst);//? TODO: Polymorph support
    }
    
    public @NotNull ContainerData getData() { return this.data; }
    
    public @NotNull NonNullList<ItemStack> getContainerItems() { return this.containerItems; }
    
    private void configDebug(String message, Object @NotNull ... args) { LOGGER.when(CrispConfig.KILN_BE_DEBUG.get()).debug(message, args); }
    //endregion
}
