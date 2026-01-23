package kurvcygnus.crispsweetberry.common.features.kiln.blockstates;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnBlock;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.CalculationResult;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressCalculator;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressModel;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.InputState;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.ProcessionState;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.ResultType;
import kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnMenu;
import kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnOutputSlot;
import kurvcygnus.crispsweetberry.common.features.kiln.data.KilnContainerData;
import kurvcygnus.crispsweetberry.common.features.kiln.events.KilnRecipeCacheEvent;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipe;
import kurvcygnus.crispsweetberry.common.registries.CrispBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Arrays;

import static kurvcygnus.crispsweetberry.common.features.kiln.KilnConstants.*;
import static kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressModel.upgradeProgress;
import static kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipe.*;

//? TODO: HOLY CRAP, A BUNCH OF BUGS, SHIT
//? KICKED 1. Only when after a while of material put inside will the procession start 
//? 2. Progress won't stop after content is cleaned, and the rate is positive
//? KINDA KICKED, DISPLAY NORMAL, PROGRESS CAL NOT NORMAL 3. Can't see any visual change of arrow, or the appearance of widget
//? 4. On Block Destroy has even no PROCESSION(Simple LOL)
//? KICKED XD 5. Procession speed has no difference! Test with: Raw rabbit, raw chicken, raw beef ~ 3 Stack of sand

//? TODO: Configurable detailed debug logs

/**
 * The <b>container part</b> of <b>Kiln Block</b>, which is responsible for <b>containment, sync and logical</b> things.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressModel Progress Model
 * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressCalculator Progress Calculator
 * @see KilnContainerData Data Sync Part
 * @see KilnMenu Menu Part
 * @see KilnRecipeCacheEvent Recipe Initialization
 * @see KilnRecipe Recipe Definition
 * @implNote The full execution flow of KilnBlockEntity is documented in
 * {@link kurvcygnus.crispsweetberry.common.features.kiln.docs Kiln-Flowmap.md}.
 */
public sealed class KilnBlockEntity extends BaseContainerBlockEntity implements MenuProvider, WorldlyContainer permits KilnDummyBlockEntity
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
    
    //*:=== Fields
    //*:== Slot Data
    private NonNullList<ItemStack> containerItems = NonNullList.withSize(KILN_DEFAULT_SIZE, ItemStack.EMPTY);
    
    //*:== Components
    /**
     * Field <u>{@link KilnProgressModel KilnProgressModel}</u> keeps
     * both real and visual progress of the kiln, and the model itself has some private methods specifically for fixing values.
     */
    public final KilnProgressModel model = new KilnProgressModel();
    private final KilnProgressCalculator progressCalculator = new KilnProgressCalculator();
    
    private final ContainerData data = new KilnContainerData(this);
    
    private final KilnRecipe[] recipeCache = { noRecipe(), noRecipe(), noRecipe() };
    
    //*:== Logical Procession Data
    private InputState inputState = InputState.ALL_EMPTY;
    private float experience = 0F;
    
    //*:== Logger
    private static final Logger logger = LogUtils.getLogger();
    //endregion
    
    //  region
    //* Constructor & Basic Info Declaration
    public KilnBlockEntity(BlockPos pos, BlockState blockState) { super(CrispBlockEntities.KILN_BLOCK_ENTITY.get(), pos, blockState); }
    
    //*:=== Hopper Support Essentials
    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) 
    {
        if(side == Direction.DOWN)
            return OUTPUT_SLOTS;
        
        return INPUT_SLOTS;
    }
    
    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, Direction dir)
    {
        if(!KILN_INPUT_SLOTS_RANGE.inRange(slot))
            return false;
        
        return canSmelt(stack);
    }
    
    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction)
        { return direction == Direction.DOWN && KILN_OUTPUT_SLOTS_RANGE.inRange(index); }
    
    //*:=== Container Basics
    @Override
    protected @NotNull Component getDefaultName() { return Component.translatable("crispsweetberry.container.kiln"); }
    
    @Override
    protected @NotNull NonNullList<ItemStack> getItems() { return this.containerItems; }
    
    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory)
        { return new KilnMenu(containerId, inventory, this); }
    
    @Override
    public int getContainerSize() { return KILN_DEFAULT_SIZE; }
    //endregion
    
    //  region
    //* Block Entity Interaction Basics
    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) { this.containerItems = items; }
    
    @Override
    public void setItem(int index, @NotNull ItemStack stack)
    {
        ItemStack oldItemStack = containerItems.get(index);
        final boolean slotChanged = stack.isEmpty() != oldItemStack.isEmpty() ||//! You may wonder: "Why we need to so many methods? Isn't 'isSameItem()' usable?"
            stack.getCount() != oldItemStack.getCount() ||                      //! Answer's no. It behaves odd on cases that an item is being taken.
            !ItemStack.isSameItemSameComponents(stack, oldItemStack);           //! So we need to use these methods to check.
        
        
        logger.debug("[INPUT_CHECK] Changed: {}, Before size limitation -> index: {}, old: [name: {}, quantity: {}], new: [name: {}, quantity: {}]",
            slotChanged, index, oldItemStack.getDisplayName(), oldItemStack.getCount(), stack.getDisplayName(), stack.getCount()
        );
        
        stack.limitSize(this.getMaxStackSize(stack));
        this.containerItems.set(index, stack);
        
        if(level != null && !level.isClientSide && slotChanged)
        {
            if(KILN_INPUT_SLOTS_RANGE.inRange(index))
            {
                logger.debug("[UPDATE_INFO] index: {} -> at input range -> go update input slots' information", index);
                updateInputSlotsInfo();
            }
            
            setChanged(level, worldPosition, this.getBlockState());//If the content in the container is changed, the data must get dirtied.
        }
    }
    
    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack)
    {
        if(!KILN_INPUT_SLOTS_RANGE.inRange(index))
            return false;
        
        return canSmelt(stack);
    }
    
    private void updateInputSlotsInfo()
    {
        inputState = InputState.ALL_EMPTY;
        
        for(int slotIndex = KILN_INPUT_SLOTS_RANGE.getMin(); KILN_INPUT_SLOTS_RANGE.inRange(slotIndex); slotIndex++)
        {
            int cacheIndex = slotIndex - KILN_INPUT_SLOTS_RANGE.getMin();
            ItemStack stackInSlot = containerItems.get(slotIndex);
            
            logger.debug("[INPUT_CHECK] slotIndex: {}, cacheIndex: {}, content: {}",
                slotIndex, cacheIndex, stackInSlot.getDisplayName());
            
            if(stackInSlot.isEmpty())
                recipeCache[cacheIndex] = noRecipe();
            else if(canSmelt(stackInSlot))    
            {
                if(inputState != InputState.VALID)
                    logger.debug("[INPUT_STATE_CHANGED] {} -> VALID (at slot {}, stack: {})",
                        inputState, slotIndex, stackInSlot.getDisplayName());
                
                inputState = InputState.VALID;
                //? TODO: Polymorph support
                KilnRecipe recipe = KilnRecipeCacheEvent.getKilnCachedRecipes().
                    getOrDefault(stackInSlot.getItem(), noRecipeList()).getFirst();
                
                recipeCache[cacheIndex] = (recipe != null && !isEmptyRecipe(recipe)) ? recipe : noRecipe();
                logger.debug("[CACHE_WRITE] write cache[{}] from slot {} -> {}",
                    cacheIndex, slotIndex, recipe);
            }
            else if(isBanned(stackInSlot))
            {
                //* Kiln doesn't support blasting recipes since they require huge heats, which can't afforded by kiln,
                //* and thus, we should tip players about this.
                inputState = InputState.HAS_TIP;
                recipeCache[cacheIndex] = tipRecipe();
                break;
            }
            else
            {
                //* This is vanilla behavior: when something can't be processed in the container, the entire procession will be paused.
                recipeCache[cacheIndex] = noRecipe();
                break;
            }
            
            logger.debug("[CACHE_CONFIRM] new cache at index {}: [Ingredient: {}, Result: {}]",
                slotIndex, recipeCache[cacheIndex].getIngredient(), recipeCache[cacheIndex].getResult());
        }
        
        final ResultType predictedResultType;
        
        switch(inputState)
        {
            case HAS_TIP -> predictedResultType = ResultType.BLAST_TIP;
            case VALID -> predictedResultType = ResultType.CONTINUE;
            default -> predictedResultType = ResultType.SKIP;
        }
        
        logger.debug(
            "[PULL_CACHE] \"Calculator#setRecipesAndResultType\" called. inputState = {}, recipes = {}, predictedResultType = {}",
            inputState,
            Arrays.toString(recipeCache),
            predictedResultType.name()
        );
        
        //* Sync recipes here to minimalize performance penalty instead of doing this in serverTick.
        this.progressCalculator.setRecipesAndResultType(recipeCache, predictedResultType);
    }
    
    /**
     * @apiNote This method is called by <u>{@link KilnOutputSlot#onTake(Player, ItemStack) KilnOutputSlot#onTake()}</u>, not the blockEntity itself.
     */
    public void dropExperience(@NotNull Player player) 
    {
        int exp = Mth.floor(this.experience);
        ExperienceOrb.award((ServerLevel) level, player.position(), exp);
        
        this.experience = 0F;
    }
    //endregion
    
    //  region
    //* Lifecycle & Logics
    public static void serverTick(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull KilnBlockEntity blockEntity)
    {
        final boolean isIgnited = state.getValue(KilnBlock.LIT);
        
        ProcessionState currentState = blockEntity.deduceProcessState(isIgnited);
        
        final CalculationResult result = blockEntity.progressCalculator.
            calculateRates(blockEntity.model.getRealProgress(), blockEntity.model.getVisualProgress(), currentState);
        
        blockEntity.model.synchronize(result.currentRealProgress(), result.currentVisualProgress(), result.resultType(), result.trend(), isIgnited);
        
        if(result.resultType() != ResultType.CONTINUE)
            return;
            
        final boolean isFinishedProcession = upgradeProgress(blockEntity.model);
        
        boolean worldDirty = false;
        
        if(isFinishedProcession)
        {
            worldDirty |= processInputItems(blockEntity, level);
            worldDirty |= checkAndShrinkInputItems(blockEntity, level, pos);
        }
        
        if(worldDirty)
            setChanged(level, pos, state);
    }
    
    /**
     * @apiNote 
     * State <u>{@link ResultType#BALANCING BALANCING}</u>
     * is strictly considered as a special variation of 
     * <u>{@link ProcessionState#WORKING WORKING}</u>, instead of being an independent state,
     * it's deduced, only being used in 
     * <u>{@link KilnProgressCalculator#calculateRates Calculation}</u>
     * and returned as a part of <u>{@link CalculationResult CalculationResult}</u> 
     * for menu visual change.
     */
    public ProcessionState deduceProcessState(boolean isIgnited)
    {
        if(!isIgnited || this.inputState != InputState.VALID)
            return ProcessionState.COOLDOWN;
        
        return ProcessionState.WORKING;
    }
    
    private static boolean processInputItems(KilnBlockEntity blockEntity, Level level)
    {
        ItemStack[] resultStacks = new ItemStack[3];
        
        for(int index = 0; index < KILN_SLOT_COUNT_FOR_EACH_TYPE; index++)
        {
            final int inputIndex = index + KILN_INPUT_SLOTS_RANGE.getMin();
            
            //! Despite inputs are already checked before this logic,
            //! we still need to be defensive.
            if(!blockEntity.canSmelt(blockEntity.containerItems.get(inputIndex)))
                return false;
            
            resultStacks[index] = blockEntity.recipeCache[index].getResultItem(level.registryAccess()).copy();
        }
        
        //* Since the procession is complex, using emulation-then-apply strategy can sufficiently decrease the quantity of boundary cases.
        NonNullList<ItemStack> emulatedOutputSlots = copyOutputSlots(blockEntity.containerItems);
        
        if(!insertItemsToSlots(blockEntity, resultStacks, emulatedOutputSlots))
            return false;
        
        //* Emulation succeed, then apply emulated results.
        for(int index = 0; index < KILN_SLOT_COUNT_FOR_EACH_TYPE; index++)
        {
            final int outputIndex = index + KILN_OUTPUT_SLOTS_RANGE.getMin();
            blockEntity.containerItems.set(outputIndex, emulatedOutputSlots.get(index).copy());
        }
        
        return true;
    }
    
    /**
     * @implNote This method emulates the insertion of result items by trying to insert one item into three different slots<i>(9 attempts in most cases)</i>,
     * only when both of three result item insertions are succeed, then the method will return {@code true} to tell 
     * <u>{@link #processInputItems processInputItems()}</u> to apply emulated results.
     */
    private static boolean insertItemsToSlots(KilnBlockEntity blockEntity, ItemStack[] resultStacks, NonNullList<ItemStack> emulatedOutputSlots)
    {
        for(int slotIndex = 0; slotIndex < KILN_SLOT_COUNT_FOR_EACH_TYPE; slotIndex++)
        {
            int invalidAttempts = 0;//! Both result mismatch with slot content and unable to stack to output slots are counted as invalid attempts.
            int emptyStackIndex = -1;
            final ItemStack resultStack = resultStacks[slotIndex];
            
            for(int attemptSlotIndex = 0; attemptSlotIndex < 3; attemptSlotIndex++)
            {
                final ItemStack emulatedOutputStack = emulatedOutputSlots.get(attemptSlotIndex);
                
                if(!canMerge(resultStack, emulatedOutputStack))
                {
                    invalidAttempts++;
                    
                    if(emptyStackIndex == -1 && emulatedOutputStack.isEmpty())
                        emptyStackIndex = attemptSlotIndex;//! Only assigns the earliest matched index to emptyStackIndex to make sure the merge
                                                           //! behavior is same as vanilla.
                    
                    if(invalidAttempts >= 3)
                    {
                        if(emptyStackIndex != -1)//! If no slot meets the condition, put this result item into the earliest empty slot.
                        {
                            emulatedOutputSlots.set(emptyStackIndex, resultStack);
                            break;
                        }
                        else
                            return false;
                    }
                    continue;
                }
                final float itemExperience = blockEntity.recipeCache[slotIndex].getExperience();
                blockEntity.experience += itemExperience;
                
                emulatedOutputStack.setCount(emulatedOutputStack.getCount() + 1);
            }
        }
        
        return true;
    }
    
    private static NonNullList<ItemStack> copyOutputSlots(NonNullList<ItemStack> outputStacks)
    {
        NonNullList<ItemStack> copy = NonNullList.withSize(KILN_SLOT_COUNT_FOR_EACH_TYPE, ItemStack.EMPTY);
        
        for(int index = KILN_SLOT_COUNT_FOR_EACH_TYPE; index < KILN_DEFAULT_SIZE; index++)
        {
            final int copyIndex = index - KILN_SLOT_COUNT_FOR_EACH_TYPE;
            
            if(!outputStacks.get(index).equals(copy.get(copyIndex)))
                copy.set(copyIndex, outputStacks.get(index));
        }
        
        return copy;
    }
    
    private static boolean canMerge(ItemStack resultStack, ItemStack emulatedOutputStack)
    {
        return ItemStack.isSameItemSameComponents(resultStack, emulatedOutputStack) &&
            emulatedOutputStack.getCount() <= emulatedOutputStack.getMaxStackSize();
    }
    
    private static boolean checkAndShrinkInputItems(KilnBlockEntity blockEntity, Level level, BlockPos pos)
    {
        if(level == null || level.isClientSide)
            return false;
        
        for(int index = KILN_INPUT_SLOTS_RANGE.getMin(); KILN_INPUT_SLOTS_RANGE.inRange(index); index++)
        {
            ItemStack stack = blockEntity.containerItems.get(index);
            ItemStack remainingStack;
            
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
    
    @Override
    public boolean stillValid(@NotNull Player player) { return Container.stillValidBlockEntity(this, player); }
    //endregion
    
    //  region
    //* Data Serialization
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.loadAdditional(tag, registries);
        
        //* Reinitialize first, then load contents.
        this.containerItems = NonNullList.withSize(KILN_DEFAULT_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.containerItems, registries);
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.putDouble("VisualProgress", this.model.getVisualProgress());
        tag.putDouble("RealProgress", this.model.getRealProgress());
        tag.putFloat("Experience", this.experience);
        ContainerHelper.saveAllItems(tag, this.containerItems, registries);
    }
    //endregion
    
    //  region
    //* Helpers, Getters & Setters
    /**
     * Helper methods to check item validation.
     * @see KilnRecipeCacheEvent#getKilnRecipes(ServerStartedEvent)  Source
     */
    public boolean canSmelt(@NotNull ItemStack itemstack) { return KilnRecipeCacheEvent.getKilnCachedRecipes().containsKey(itemstack.getItem()); }
    
    private boolean isBanned(@NotNull ItemStack itemstack) { return KilnRecipeCacheEvent.getBannedRecipes().containsKey(itemstack.getItem()); }
    
    public ContainerData getData() { return this.data; }
    //endregion
}
