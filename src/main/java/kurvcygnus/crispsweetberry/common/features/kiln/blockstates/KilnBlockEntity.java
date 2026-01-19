package kurvcygnus.crispsweetberry.common.features.kiln.blockstates;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnBlock;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnConstants;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.CalculationResult;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressCalculator;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressModel;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.ResultType;
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

import static kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressModel.upgradeProgress;

//? TODO: HOLY CRAP, A BUNCH OF BUGS, SHIT
//? 1. Only when after a while of material put inside will the procession start
//? 2. Progress won't stop after content is cleaned
//? 3. Procession output is always the first input result, and x3
//? 4. Can't see any visual change of arrow, or the appearance of widget
//? ABSOLUTELY TERRIBLE QAQ

/**
 * The <b>container part</b> of <b>Kiln Block</b>, which is responsible for <b>containment, sync and logical</b> things.
 * @since CSB Release 1.0
 * @author Kurv
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
        KilnConstants.KILN_INPUT_SLOTS_RANGE.getMin(),
        KilnConstants.KILN_INPUT_SLOTS_RANGE.getMin() + 1,
        KilnConstants.KILN_INPUT_SLOTS_RANGE.getMax()
    };
    
    private static final int[] OUTPUT_SLOTS = {
        KilnConstants.KILN_OUTPUT_SLOTS_RANGE.getMin(),
        KilnConstants.KILN_OUTPUT_SLOTS_RANGE.getMin() + 1,
        KilnConstants.KILN_OUTPUT_SLOTS_RANGE.getMax()
    };
    
    //*:=== Fields
    //*:== Slot Data
    protected NonNullList<ItemStack> containerItems = NonNullList.withSize(KilnConstants.KILN_DEFAULT_SIZE, ItemStack.EMPTY);
    
    //*:== Components
    /**
     * Field <u>{@link KilnProgressModel KilnProgressModel}</u> keeps
     * both real and visual progress of the kiln, and the model itself has some private methods specifically for fixing values.
     */
    public final KilnProgressModel model = new KilnProgressModel();
    private final KilnProgressCalculator progressCalculator = new KilnProgressCalculator();
    
    protected final ContainerData data = new KilnContainerData(this);
    
    protected final KilnRecipe[] recipeCache = { KilnRecipe.noRecipe(), KilnRecipe.noRecipe(), KilnRecipe.noRecipe() };
    
    //*:== Logical Procession Data
    private InputState inputState = InputState.EMPTY;
    private float experience = 0F;
    
    //*:== Logger
    private static final Logger logger = LogUtils.getLogger();
    
    //*:== Enum State Machines
    public enum ProcessionState { WORKING, COOLDOWN }
    public enum InputState { EMPTY, VALID, INVALID }
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
        if(!KilnConstants.KILN_INPUT_SLOTS_RANGE.inRange(slot))
            return false;
        
        return canSmelt(stack);
    }
    
    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction)
        { return direction == Direction.DOWN && KilnConstants.KILN_OUTPUT_SLOTS_RANGE.inRange(index); }
    
    //*:=== Container Basics
    @Override
    protected @NotNull Component getDefaultName() { return Component.translatable("crispsweetberry.container.kiln"); }
    
    @Override
    protected @NotNull NonNullList<ItemStack> getItems() { return this.containerItems; }
    
    @Override
    protected @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory)
        { return new KilnMenu(containerId, inventory, this); }
    
    @Override
    public int getContainerSize() { return KilnConstants.KILN_DEFAULT_SIZE; }
    //endregion
    
    //  region
    //* Block Entity Interaction Basics
    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> items) { this.containerItems = items; }
    
    @Override
    public void setItem(int index, @NotNull ItemStack stack)
    {
        ItemStack oldItemStack = containerItems.get(index);
        final boolean slotChanged = !ItemStack.isSameItemSameComponents(stack, oldItemStack);
        
        stack.limitSize(this.getMaxStackSize(stack));
        this.containerItems.set(index, stack);
        
        if(level != null && !level.isClientSide && slotChanged)
        {
            if(KilnConstants.KILN_INPUT_SLOTS_RANGE.inRange(index))
                updateInputSlotsInfo(stack);
            
            setChanged(level, worldPosition, this.getBlockState());//If the content in the container is changed, the data must get dirtied.
        }
    }
    
    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack)
    {
        if(!KilnConstants.KILN_INPUT_SLOTS_RANGE.inRange(index))
            return false;
        
        return canSmelt(stack);
    }
    
    private void updateInputSlotsInfo(ItemStack stack)
    {
        inputState = InputState.EMPTY;
        
        for(int index = KilnConstants.KILN_INPUT_SLOTS_RANGE.getMin(); KilnConstants.KILN_INPUT_SLOTS_RANGE.inRange(index); index++)
        {
            int slotIndex = KilnConstants.KILN_INPUT_SLOTS_RANGE.getMin() + index;
            ItemStack itemInSlot = containerItems.get(slotIndex);
            
            if(itemInSlot.isEmpty())
                recipeCache[index] = KilnRecipe.noRecipe();
            else if(canSmelt(containerItems.get(index)))
            {
                inputState = InputState.VALID;
                //? TODO: 多态支持
                KilnRecipe recipe = KilnRecipeCacheEvent.getKilnCachedRecipes().
                    getOrDefault(stack.getItem(), KilnRecipe.noRecipeList()).getFirst();
                
                recipeCache[index] = (recipe != null && !KilnRecipe.isEmptyRecipe(recipe)) ? recipe : KilnRecipe.noRecipe();
            }
            else if(isBanned(containerItems.get(index)))
            {
                //* Kiln doesn't support blasting recipes since they require huge heats, which can't afforded by kiln,
                //* and thus, we should tip players about this.
                inputState = InputState.INVALID;
                recipeCache[index] = KilnRecipe.tipRecipe();
                return;
            }
            else
            {
                //* This is vanilla behavior: when something can't be processed in the container, the entire procession will be paused.
                recipeCache[index] = KilnRecipe.noRecipe();
                inputState = InputState.INVALID;
                return;
            }
        }
        
        logger.debug("Try pushing recipes to calculator... Details: Recipes: {}, {}, {}", recipeCache[0], recipeCache[1], recipeCache[2]);
        
        this.progressCalculator.setRecipes(recipeCache);//* Sync recipes here to minimalize performance penalty instead of doing this in serverTick.
    }
    
    /**
     * @apiNote This method is called by <u>{@link KilnOutputSlot#onTake(Player, ItemStack) KilnOutputSlot#onTake()}</u>, not the blockEntity itself.
     */
    public void dropExperience(@NotNull Player player) 
    {
        int exp = Mth.floor(this.experience);
        ExperienceOrb.award((ServerLevel) level, player.position(), exp);
        
        this.experience = 0;
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
        if(!isIgnited || this.inputState == InputState.INVALID)
            return ProcessionState.COOLDOWN;
        
        return ProcessionState.WORKING;
    }
    
    private static boolean processInputItems(KilnBlockEntity blockEntity, Level level)
    {
        ItemStack[] resultStacks = new ItemStack[3];
        
        for(int index = 0; index < KilnConstants.KILN_SLOT_COUNT_FOR_EACH_TYPE; index++)
        {
            final int inputIndex = index + KilnConstants.KILN_INPUT_SLOTS_RANGE.getMin();
            
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
        for(int index = 0; index < KilnConstants.KILN_SLOT_COUNT_FOR_EACH_TYPE; index++)
        {
            final int outputIndex = index + KilnConstants.KILN_OUTPUT_SLOTS_RANGE.getMin();
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
        for(int slotIndex = 0; slotIndex < KilnConstants.KILN_SLOT_COUNT_FOR_EACH_TYPE; slotIndex++)
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
        NonNullList<ItemStack> copy = NonNullList.withSize(KilnConstants.KILN_SLOT_COUNT_FOR_EACH_TYPE, ItemStack.EMPTY);
        
        for(int index = KilnConstants.KILN_SLOT_COUNT_FOR_EACH_TYPE; index < KilnConstants.KILN_DEFAULT_SIZE; index++)
        {
            final int copyIndex = index - KilnConstants.KILN_SLOT_COUNT_FOR_EACH_TYPE;
            
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
        
        for(int index = KilnConstants.KILN_INPUT_SLOTS_RANGE.getMin(); KilnConstants.KILN_INPUT_SLOTS_RANGE.inRange(index); index++)
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
        this.containerItems = NonNullList.withSize(KilnConstants.KILN_DEFAULT_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.containerItems, registries);
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.putDouble("DisplayProgress", this.model.getVisualProgress());
        tag.putDouble("RealProgress", this.model.getRealProgress());
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
    
    public boolean isBanned(@NotNull ItemStack itemstack) { return KilnRecipeCacheEvent.getBannedRecipes().containsKey(itemstack.getItem()); }
    
    public ContainerData getData() { return this.data; }
    //endregion
}
