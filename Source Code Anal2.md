# **AbstractContainerMenu Anal**

>**警告: 以下注释如没有标记为CONFIRMED, 则都是根据实际游戏/代码上下文的推测, 推测不一定准确!**

#### *由于个人码风习惯, Anal里的代码格式, 变量命名和源代码将会不同.*
#### *本文件的注释不会全部注释, 尤其是针对一些基础易懂的方法.*

```java
package net.minecraft.world.inventory;

import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

public abstract class AbstractContainerMenu
{
    //CONFIRMED Logger
    private static final Logger LOGGER = LogUtils.getLogger();
    
    //CONFIRMED 鼠标点击互动和非UI的交互结果
    //CONFIRMED UI外的交互
    public static final int SLOT_CLICKED_OUTSIDE = -999;

    //CONFIRMED 物品的平摊交互(选中物品后的拖动均分)
    public static final int QUICKCRAFT_TYPE_CHARITABLE = 0;

    //CONFIRMED 每经过一个槽位只放置一个物品的交互（拖动时每格只放一个）
    public static final int QUICKCRAFT_TYPE_GREEDY = 1;

    //CONFIRMED 创造模式的中键在其它槽位的复制
    public static final int QUICKCRAFT_TYPE_CLONE = 2;

    //CONFIRMED 表示快速合成/拖动操作的开始（按下鼠标）
    public static final int QUICKCRAFT_HEADER_START = 0;

    //CONFIRMED 表示快速合成/拖动操作的持续（拖动鼠标）
    public static final int QUICKCRAFT_HEADER_CONTINUE = 1;

    //COFIRMED 表示快速合成/拖动操作的结束（释放鼠标）
    public static final int QUICKCRAFT_HEADER_END = 2;//老实说, enum会更直观, 不过用int表示应该是出于性能考虑.

    //COFIRMED 鼠标拿着的物品的物品堆上限
    public static final int CARRIED_SLOT_SIZE = Integer.MAX_VALUE;

    //
    private final NonNullList<ItemStack> lastSlots = NonNullList.create();
    public final NonNullList<Slot> slots = NonNullList.create();
    private final List<DataSlot> dataSlots = Lists.newArrayList();
    private ItemStack carried = ItemStack.EMPTY;
    private final NonNullList<ItemStack> remoteSlots = NonNullList.create();
    private final IntList remoteDataSlots = new IntArrayList();
    private ItemStack remoteCarried = ItemStack.EMPTY;
    private int stateId;
    @Nullable
    private final MenuType<?> menuType;
    public final int containerId;
    private int quickcraftType = -1;
    private int quickcraftStatus;
    private final Set<Slot> quickcraftSlots = Sets.newHashSet();
    private final List<ContainerListener> containerListeners = Lists.newArrayList();
    @Nullable
    private ContainerSynchronizer synchronizer;
    private boolean suppressRemoteUpdates;

    /**
     * COFIRMED 构造方法, 用于eventBus的注册数据传递.
     */
    protected AbstractContainerMenu(@Nullable MenuType<?> menuType, int containerId)
    {
        this.menuType = menuType;
        this.containerId = containerId;
    }

    /**
     * COFIRMED 方法的结果实现方法由于涉及原版代码的参与, 具体实现不明. 但可推测是基于Blockstate与玩家状态的检测.
     */
    protected static boolean stillValid(ContainerLevelAccess access, Player player, Block targetBlock)
    {
        return access.evaluate((p_339522_, p_339523_) -> 
            !p_339522_.getBlockState(p_339523_).is(targetBlock) ? false : player.canInteractWithBlock(p_339523_, 4.0), true
        );
    }

    /**
     * COFIRMED 提供给ServerPlayer类使用的getter方法.
     */
    public MenuType<?> getType() 
    {
        if(this.menuType == null)
            throw new UnsupportedOperationException("Unable to construct this menu by type");
        else
            return this.menuType;
    }

    /**
     * COFIRMED 检测实现menu的容器的容量大小是否正常的方法.
     */
    protected static void checkContainerSize(Container container, int minSize)
    {
        int containerSize = container.getContainerSize();
        if(containerSize < minSize)
            throw new IllegalArgumentException("Container size " + containerSize + " is smaller than expected " + minSize);
    }

    /**
     * COFIRMED 检测实现menu的容器的数据量是否正常的方法.
     */
    protected static void checkContainerDataCount(ContainerData intArray, int minSize) 
    {
        int dataSize = intArray.getCount();
        if(dataSize < minSize)
            throw new IllegalArgumentException("Container data count " + dataSize + " is smaller than expected " + minSize);
    }
    
    /**
     * COFIRMED 临界条件的判定.<br>
     * 1. "-1" -> 当前不明.<br>
     * 2. "-999" -> 请自行查阅SLOT_CLICKED_OUTSIDE.<br>
     * 3. {@code slotIndex < this.slots.size()} -> 显而易见.<br>
     */
    public boolean isValidSlotIndex(int slotIndex) { return slotIndex == -1 || slotIndex == -999 || slotIndex < this.slots.size(); }

    /**
     * Adds an item slot to this container
     * Dev注释翻译: 将一个物品栏槽位添加到这个容器中.
     */
    protected Slot addSlot(Slot slot) 
    {
        //COFIRMED 可见slot的索引机制和数组相同.
        slot.index = this.slots.size();
        this.slots.add(slot);
        this.lastSlots.add(ItemStack.EMPTY);

        //COFIRMED 和服务端同步的List
        this.remoteSlots.add(ItemStack.EMPTY);
        return slot;
    }

    /**
     * 方法不解释, 此处推测dataSlots:<br>
     * dataSlot是有特殊功能的槽位, 如熔炉的燃烧槽位.<br>
     * 对应的, {@code remoteDataSlots} 不做解释.
     */
    protected DataSlot addDataSlot(DataSlot intValue)
    {
        this.dataSlots.add(intValue);
        this.remoteDataSlots.add(0);
        return intValue;
    }

    /**
     * 推测是添加dataSlot组的方法.
     */
    protected void addDataSlots(ContainerData array)
    {
        for(int i = 0; i < array.getCount(); i++)
            this.addDataSlot(DataSlot.forContainer(array, i));
    }

    /**
     * 添加检测dataSlot槽位内容改变的事件监听器的方法.
     */
    public void addSlotListener(ContainerListener listener)
    {
        if(this.containerListeners.contains(listener))
            return;

        this.containerListeners.add(listener);
        this.broadcastChanges();
        
    }

    /**
     * 设置同步器, 并把所有数据推送到服务端的方法.
     */
    public void setSynchronizer(ContainerSynchronizer synchronizer)
    {
        this.synchronizer = synchronizer;
        this.sendAllDataToRemote();
    }

    /**
     * 数据推送的实现方法.
     */
    public void sendAllDataToRemote()
    {
        //同时用于添加slots和dataSlots到remoteSlots的索引变量
        int slotCollectionIndex = 0;

        for(int slotsSize = this.slots.size(); slotCollectionIndex < slotsSize; slotCollectionIndex++)
            this.remoteSlots.set(slotCollectionIndex, this.slots.get(slotCollectionIndex).getItem().copy());

        this.remoteCarried = this.getCarried().copy();
        slotCollectionIndex = 0;

        for(int dataSlotsSize = this.dataSlots.size(); slotCollectionIndex < dataSlotsSize; slotCollectionIndex++)
            this.remoteDataSlots.set(slotCollectionIndex, this.dataSlots.get(slotCollectionIndex).get());

        //理所当然的边界条件检测
        if(this.synchronizer != null)
            this.synchronizer.sendInitialData(this, this.remoteSlots, this.remoteCarried, this.remoteDataSlots.toIntArray());
    }

    /**
     * Remove the given Listener. Method name is for legacy.
     * Dev注释翻译: 移除参数指定的监听器. 该方法的名字出于历史包袱.
     */
    public void removeSlotListener(ContainerListener listener) { this.containerListeners.remove(listener); }

    public NonNullList<ItemStack> getItems()
    {
        NonNullList<ItemStack> nonnulllist = NonNullList.create();

        for(Slot slot: this.slots)
            nonnulllist.add(slot.getItem());

        return nonnulllist;
    }

    /**
     * 把槽位物品的改变广播到服务端进行同步的方法.
     */
    public void broadcastChanges()
    {
        //同步slots组
        for(int slotsIndex = 0; slotsIndex < this.slots.size(); slotsIndex++)
        {
            ItemStack itemstack = this.slots.get(slotsIndex).getItem();
            Supplier<ItemStack> supplier = Suppliers.memoize(itemstack::copy);
            this.triggerSlotListeners(slotsIndex, itemstack, supplier);
            this.synchronizeSlotToRemote(slotsIndex, itemstack, supplier);
        }

        this.synchronizeCarriedToRemote();

        //同步dataSlots组
        for(int dataSlotIndex = 0; dataSlotIndex < this.dataSlots.size(); dataSlotIndex++) {
            DataSlot dataslot = this.dataSlots.get(dataSlotIndex);
            int dataSlotValue = dataslot.get();

            if(dataslot.checkAndClearUpdateFlag())
                this.updateDataSlotListeners(dataSlotIndex, dataSlotValue);

            this.synchronizeDataSlotToRemote(dataSlotIndex, dataSlotValue);
        }
    }

    /**
     * 把所有slots组, dataSlots组的数据的进行同步播报的方法.
     */
    public void broadcastFullState()
    {
        for(int slotIndex = 0; slotIndex < this.slots.size(); slotIndex++)
        {
            ItemStack slotStack = this.slots.get(slotIndex).getItem();
            this.triggerSlotListeners(slotIndex, slotStack, slotStack::copy);
        }

        for(int dataSlotIndex = 0; dataSlotIndex < this.dataSlots.size(); dataSlotIndex++)
        {
            DataSlot dataslot = this.dataSlots.get(dataSlotIndex);
            if(dataslot.checkAndClearUpdateFlag())
                this.updateDataSlotListeners(dataSlotIndex, dataslot.get());
        }

        this.sendAllDataToRemote();
    }

    /**
     * 把DataSlot槽位的数据上传到容器监听器的方法.
     */
    private void updateDataSlotListeners(int dataslotIndex, int value)
    {
        for(ContainerListener containerlistener: this.containerListeners)
            containerlistener.dataChanged(this, dataslotIndex, value);
    }

    private void triggerSlotListeners(int slotIndex, ItemStack stack, Supplier<ItemStack> supplier) 
    {
        ItemStack itemstack = this.lastSlots.get(slotIndex);
        if(!ItemStack.matches(itemstack, stack)) 
        {
            ItemStack itemstack1 = supplier.get();
            this.lastSlots.set(slotIndex, itemstack1);

            for(ContainerListener containerlistener : this.containerListeners)
                containerlistener.slotChanged(this, slotIndex, itemstack1);
        }
    }

    private void synchronizeSlotToRemote(int slotIndex, ItemStack stack, Supplier<ItemStack> supplier) {
        if (!this.suppressRemoteUpdates) {
            ItemStack itemstack = this.remoteSlots.get(slotIndex);
            if (!ItemStack.matches(itemstack, stack)) {
                ItemStack itemstack1 = supplier.get();
                this.remoteSlots.set(slotIndex, itemstack1);
                if (this.synchronizer != null) {
                    this.synchronizer.sendSlotChange(this, slotIndex, itemstack1);
                }
            }
        }
    }

    private void synchronizeDataSlotToRemote(int slotIndex, int value) {
        if (!this.suppressRemoteUpdates) {
            int i = this.remoteDataSlots.getInt(slotIndex);
            if (i != value) {
                this.remoteDataSlots.set(slotIndex, value);
                if (this.synchronizer != null) {
                    this.synchronizer.sendDataChange(this, slotIndex, value);
                }
            }
        }
    }

    private void synchronizeCarriedToRemote() {
        if (!this.suppressRemoteUpdates) {
            if (!ItemStack.matches(this.getCarried(), this.remoteCarried)) {
                this.remoteCarried = this.getCarried().copy();
                if (this.synchronizer != null) {
                    this.synchronizer.sendCarriedChange(this, this.remoteCarried);
                }
            }
        }
    }

    public void setRemoteSlot(int slot, ItemStack stack) {
        this.remoteSlots.set(slot, stack.copy());
    }

    public void setRemoteSlotNoCopy(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.remoteSlots.size()) {
            this.remoteSlots.set(slot, stack);
        } else {
            LOGGER.debug("Incorrect slot index: {} available slots: {}", slot, this.remoteSlots.size());
        }
    }

    public void setRemoteCarried(ItemStack remoteCarried) {
        this.remoteCarried = remoteCarried.copy();
    }

    /**
     * Handles the given Button-click on the server, currently only used by enchanting. Name is for legacy.
     */
    public boolean clickMenuButton(Player player, int id) {
        return false;
    }

    public Slot getSlot(int slotId) {
        return this.slots.get(slotId);
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player inventory and the other inventory(s).
     */
    public abstract ItemStack quickMoveStack(Player player, int index);

    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        try {
            this.doClick(slotId, button, clickType, player);
        } catch (Exception exception) {
            CrashReport crashreport = CrashReport.forThrowable(exception, "Container click");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Click info");
            crashreportcategory.setDetail("Menu Type", () -> this.menuType != null ? BuiltInRegistries.MENU.getKey(this.menuType).toString() : "<no type>");
            crashreportcategory.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
            crashreportcategory.setDetail("Slot Count", this.slots.size());
            crashreportcategory.setDetail("Slot", slotId);
            crashreportcategory.setDetail("Button", button);
            crashreportcategory.setDetail("Type", clickType);
            throw new ReportedException(crashreport);
        }
    }

    //真是一场灾难...
    private void doClick(int slotId, int button, ClickType clickType, Player player) {
        Inventory inventory = player.getInventory();
        if (clickType == ClickType.QUICK_CRAFT) {
            int i = this.quickcraftStatus;
            this.quickcraftStatus = getQuickcraftHeader(button);
            if ((i != 1 || this.quickcraftStatus != 2) && i != this.quickcraftStatus) {
                this.resetQuickCraft();
            } else if (this.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.quickcraftStatus == 0) {
                this.quickcraftType = getQuickcraftType(button);
                if (isValidQuickcraftType(this.quickcraftType, player)) {
                    this.quickcraftStatus = 1;
                    this.quickcraftSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.quickcraftStatus == 1) {
                Slot slot = this.slots.get(slotId);
                ItemStack itemstack = this.getCarried();
                if (canItemQuickReplace(slot, itemstack, true)
                    && slot.mayPlace(itemstack)
                    && (this.quickcraftType == 2 || itemstack.getCount() > this.quickcraftSlots.size())
                    && this.canDragTo(slot)) {
                    this.quickcraftSlots.add(slot);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    if (this.quickcraftSlots.size() == 1) {
                        int i1 = this.quickcraftSlots.iterator().next().index;
                        this.resetQuickCraft();
                        this.doClick(i1, this.quickcraftType, ClickType.PICKUP, player);
                        return;
                    }

                    ItemStack itemstack3 = this.getCarried().copy();
                    if (itemstack3.isEmpty()) {
                        this.resetQuickCraft();
                        return;
                    }

                    int k1 = this.getCarried().getCount();

                    for (Slot slot1 : this.quickcraftSlots) {
                        ItemStack itemstack1 = this.getCarried();
                        if (slot1 != null
                            && canItemQuickReplace(slot1, itemstack1, true)
                            && slot1.mayPlace(itemstack1)
                            && (this.quickcraftType == 2 || itemstack1.getCount() >= this.quickcraftSlots.size())
                            && this.canDragTo(slot1)) {
                            int j = slot1.hasItem() ? slot1.getItem().getCount() : 0;
                            int k = Math.min(itemstack3.getMaxStackSize(), slot1.getMaxStackSize(itemstack3));
                            int l = Math.min(getQuickCraftPlaceCount(this.quickcraftSlots, this.quickcraftType, itemstack3) + j, k);
                            k1 -= l - j;
                            slot1.setByPlayer(itemstack3.copyWithCount(l));
                        }
                    }

                    itemstack3.setCount(k1);
                    this.setCarried(itemstack3);
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.quickcraftStatus != 0) {
            this.resetQuickCraft();
        } else if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (button == 0 || button == 1)) {
            ClickAction clickaction = button == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
            if (slotId == -999) {
                if (!this.getCarried().isEmpty()) {
                    if (clickaction == ClickAction.PRIMARY) {
                        player.drop(this.getCarried(), true);
                        this.setCarried(ItemStack.EMPTY);
                    } else {
                        player.drop(this.getCarried().split(1), true);
                    }
                }
            } else if (clickType == ClickType.QUICK_MOVE) {
                if (slotId < 0) {
                    return;
                }

                Slot slot6 = this.slots.get(slotId);
                if (!slot6.mayPickup(player)) {
                    return;
                }

                ItemStack itemstack8 = this.quickMoveStack(player, slotId);

                while (!itemstack8.isEmpty() && ItemStack.isSameItem(slot6.getItem(), itemstack8)) {
                    itemstack8 = this.quickMoveStack(player, slotId);
                }
            } else {
                if (slotId < 0) {
                    return;
                }

                Slot slot7 = this.slots.get(slotId);
                ItemStack itemstack9 = slot7.getItem();
                ItemStack itemstack10 = this.getCarried();
                player.updateTutorialInventoryAction(itemstack10, slot7.getItem(), clickaction);
                if (!this.tryItemClickBehaviourOverride(player, clickaction, slot7, itemstack9, itemstack10)) {
                    if (itemstack9.isEmpty()) {
                        if (!itemstack10.isEmpty()) {
                            int i3 = clickaction == ClickAction.PRIMARY ? itemstack10.getCount() : 1;
                            this.setCarried(slot7.safeInsert(itemstack10, i3));
                        }
                    } else if (slot7.mayPickup(player)) {
                        if (itemstack10.isEmpty()) {
                            int j3 = clickaction == ClickAction.PRIMARY ? itemstack9.getCount() : (itemstack9.getCount() + 1) / 2;
                            Optional<ItemStack> optional1 = slot7.tryRemove(j3, Integer.MAX_VALUE, player);
                            optional1.ifPresent(p_150421_ -> {
                                this.setCarried(p_150421_);
                                slot7.onTake(player, p_150421_);
                            });
                        } else if (slot7.mayPlace(itemstack10)) {
                            if (ItemStack.isSameItemSameComponents(itemstack9, itemstack10)) {
                                int k3 = clickaction == ClickAction.PRIMARY ? itemstack10.getCount() : 1;
                                this.setCarried(slot7.safeInsert(itemstack10, k3));
                            } else if (itemstack10.getCount() <= slot7.getMaxStackSize(itemstack10)) {
                                this.setCarried(itemstack9);
                                slot7.setByPlayer(itemstack10);
                            }
                        } else if (ItemStack.isSameItemSameComponents(itemstack9, itemstack10)) {
                            Optional<ItemStack> optional = slot7.tryRemove(
                                itemstack9.getCount(), itemstack10.getMaxStackSize() - itemstack10.getCount(), player
                            );
                            optional.ifPresent(p_150428_ -> {
                                itemstack10.grow(p_150428_.getCount());
                                slot7.onTake(player, p_150428_);
                            });
                        }
                    }
                }

                slot7.setChanged();
            }
        } else if (clickType == ClickType.SWAP && (button >= 0 && button < 9 || button == 40)) {
            ItemStack itemstack2 = inventory.getItem(button);
            Slot slot5 = this.slots.get(slotId);
            ItemStack itemstack7 = slot5.getItem();
            if (!itemstack2.isEmpty() || !itemstack7.isEmpty()) {
                if (itemstack2.isEmpty()) {
                    if (slot5.mayPickup(player)) {
                        inventory.setItem(button, itemstack7);
                        slot5.onSwapCraft(itemstack7.getCount());
                        slot5.setByPlayer(ItemStack.EMPTY);
                        slot5.onTake(player, itemstack7);
                    }
                } else if (itemstack7.isEmpty()) {
                    if (slot5.mayPlace(itemstack2)) {
                        int j2 = slot5.getMaxStackSize(itemstack2);
                        if (itemstack2.getCount() > j2) {
                            slot5.setByPlayer(itemstack2.split(j2));
                        } else {
                            inventory.setItem(button, ItemStack.EMPTY);
                            slot5.setByPlayer(itemstack2);
                        }
                    }
                } else if (slot5.mayPickup(player) && slot5.mayPlace(itemstack2)) {
                    int k2 = slot5.getMaxStackSize(itemstack2);
                    if (itemstack2.getCount() > k2) {
                        slot5.setByPlayer(itemstack2.split(k2));
                        slot5.onTake(player, itemstack7);
                        if (!inventory.add(itemstack7)) {
                            player.drop(itemstack7, true);
                        }
                    } else {
                        inventory.setItem(button, itemstack7);
                        slot5.setByPlayer(itemstack2);
                        slot5.onTake(player, itemstack7);
                    }
                }
            }
        } else if (clickType == ClickType.CLONE && player.hasInfiniteMaterials() && this.getCarried().isEmpty() && slotId >= 0) {
            Slot slot4 = this.slots.get(slotId);
            if (slot4.hasItem()) {
                ItemStack itemstack5 = slot4.getItem();
                this.setCarried(itemstack5.copyWithCount(itemstack5.getMaxStackSize()));
            }
        } else if (clickType == ClickType.THROW && this.getCarried().isEmpty() && slotId >= 0) {
            Slot slot3 = this.slots.get(slotId);
            int j1 = button == 0 ? 1 : slot3.getItem().getCount();
            ItemStack itemstack6 = slot3.safeTake(j1, Integer.MAX_VALUE, player);
            player.drop(itemstack6, true);
        } else if (clickType == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot slot2 = this.slots.get(slotId);
            ItemStack itemstack4 = this.getCarried();
            if (!itemstack4.isEmpty() && (!slot2.hasItem() || !slot2.mayPickup(player))) {
                int l1 = button == 0 ? 0 : this.slots.size() - 1;
                int i2 = button == 0 ? 1 : -1;

                for (int l2 = 0; l2 < 2; l2++) {
                    for (int l3 = l1; l3 >= 0 && l3 < this.slots.size() && itemstack4.getCount() < itemstack4.getMaxStackSize(); l3 += i2) {
                        Slot slot8 = this.slots.get(l3);
                        if (slot8.hasItem()
                            && canItemQuickReplace(slot8, itemstack4, true)
                            && slot8.mayPickup(player)
                            && this.canTakeItemForPickAll(itemstack4, slot8)) {
                            ItemStack itemstack11 = slot8.getItem();
                            if (l2 != 0 || itemstack11.getCount() != itemstack11.getMaxStackSize()) {
                                ItemStack itemstack12 = slot8.safeTake(itemstack11.getCount(), itemstack4.getMaxStackSize() - itemstack4.getCount(), player);
                                itemstack4.grow(itemstack12.getCount());
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean tryItemClickBehaviourOverride(Player player, ClickAction action, Slot slot, ItemStack clickedItem, ItemStack carriedItem) {
        // Neo: Fire the ItemStackedOnOtherEvent, and return true if it was cancelled (meaning the event was handled). Returning true will trigger the container to stop processing further logic.
        // The first parameter to onItemStackedOn is the "carried" (under-mouse) item, which is the second ItemStack parameter to this method.
        if (net.neoforged.neoforge.common.CommonHooks.onItemStackedOn(carriedItem, clickedItem, slot, action, player, createCarriedSlotAccess())) {
            return true;
        }

        FeatureFlagSet featureflagset = player.level().enabledFeatures();
        return carriedItem.isItemEnabled(featureflagset) && carriedItem.overrideStackedOnOther(slot, action, player)
            ? true
            : clickedItem.isItemEnabled(featureflagset)
                && clickedItem.overrideOtherStackedOnMe(carriedItem, slot, action, player, this.createCarriedSlotAccess());
    }

    private SlotAccess createCarriedSlotAccess() {
        return new SlotAccess() {
            @Override
            public ItemStack get() {
                return AbstractContainerMenu.this.getCarried();
            }

            @Override
            public boolean set(ItemStack p_150452_) {
                AbstractContainerMenu.this.setCarried(p_150452_);
                return true;
            }
        };
    }

    /**
     * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is null for the initial slot that was double-clicked.
     */
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return true;
    }

    /**
     * Called when the container is closed.
     */
    public void removed(Player player) {
        if (player instanceof ServerPlayer) {
            ItemStack itemstack = this.getCarried();
            if (!itemstack.isEmpty()) {
                if (player.isAlive() && !((ServerPlayer)player).hasDisconnected()) {
                    player.getInventory().placeItemBackInInventory(itemstack);
                } else {
                    player.drop(itemstack, false);
                }

                this.setCarried(ItemStack.EMPTY);
            }
        }
    }

    protected void clearContainer(Player player, Container container) {
        if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
            for (int j = 0; j < container.getContainerSize(); j++) {
                player.drop(container.removeItemNoUpdate(j), false);
            }
        } else {
            for (int i = 0; i < container.getContainerSize(); i++) {
                Inventory inventory = player.getInventory();
                if (inventory.player instanceof ServerPlayer) {
                    inventory.placeItemBackInInventory(container.removeItemNoUpdate(i));
                }
            }
        }
    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    public void slotsChanged(Container container) {
        this.broadcastChanges();
    }

    /**
     * Puts an ItemStack in a slot.
     */
    public void setItem(int slotId, int stateId, ItemStack stack) {
        this.getSlot(slotId).set(stack);
        this.stateId = stateId;
    }

    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
        for (int i = 0; i < items.size(); i++) {
            this.getSlot(i).set(items.get(i));
        }

        this.carried = carried;
        this.stateId = stateId;
    }

    public void setData(int id, int data) {
        this.dataSlots.get(id).set(data);
    }

    /**
     * Determines whether supplied player can use this container
     */
    public abstract boolean stillValid(Player player);

    /**
     * Merges provided ItemStack with the first available one in the container/player inventor between minIndex (included) and maxIndex (excluded). Args : stack, minIndex, maxIndex, negativDirection. [!] the Container implementation do not check if the item is valid for the slot
     */
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while (!stack.isEmpty() && (reverseDirection ? i >= startIndex : i < endIndex)) {
                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                if (!itemstack.isEmpty() && ItemStack.isSameItemSameComponents(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int k = slot.getMaxStackSize(itemstack);
                    if (j <= k) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.setChanged();
                        flag = true;
                    } else if (itemstack.getCount() < k) {
                        stack.shrink(k - itemstack.getCount());
                        itemstack.setCount(k);
                        slot.setChanged();
                        flag = true;
                    }
                }

                if (reverseDirection) {
                    i--;
                } else {
                    i++;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while (reverseDirection ? i >= startIndex : i < endIndex) {
                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
                    int l = slot1.getMaxStackSize(stack);
                    slot1.setByPlayer(stack.split(Math.min(stack.getCount(), l)));
                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (reverseDirection) {
                    i--;
                } else {
                    i++;
                }
            }
        }

        return flag;
    }

    /**
     * Extracts the drag mode. Args : eventButton. Return (0 : evenly split, 1 : one item by slot, 2 : not used ?)
     */
    public static int getQuickcraftType(int eventButton) {
        return eventButton >> 2 & 3;
    }

    /**
     * Args : clickedButton, Returns (0 : start drag, 1 : add slot, 2 : end drag)
     */
    public static int getQuickcraftHeader(int clickedButton) {
        return clickedButton & 3;
    }

    public static int getQuickcraftMask(int quickCraftingHeader, int quickCraftingType) {
        return quickCraftingHeader & 3 | (quickCraftingType & 3) << 2;
    }

    public static boolean isValidQuickcraftType(int dragMode, Player player) {
        if (dragMode == 0) {
            return true;
        } else {
            return dragMode == 1 ? true : dragMode == 2 && player.hasInfiniteMaterials();
        }
    }

    protected void resetQuickCraft() {
        this.quickcraftStatus = 0;
        this.quickcraftSlots.clear();
    }

    /**
     * Checks if it's possible to add the given itemstack to the given slot.
     */
    public static boolean canItemQuickReplace(@Nullable Slot slot, ItemStack stack, boolean stackSizeMatters) {
        boolean flag = slot == null || !slot.hasItem();
        return !flag && ItemStack.isSameItemSameComponents(stack, slot.getItem())
            ? slot.getItem().getCount() + (stackSizeMatters ? 0 : stack.getCount()) <= stack.getMaxStackSize()
            : flag;
    }

    public static int getQuickCraftPlaceCount(Set<Slot> slots, int type, ItemStack stack) {
        return switch (type) {
            case 0 -> Mth.floor((float)stack.getCount() / (float)slots.size());
            case 1 -> 1;
            case 2 -> stack.getMaxStackSize();
            default -> stack.getCount();
        };
    }

    /**
     * Returns {@code true} if the player can "drag-spilt" items into this slot. Returns {@code true} by default. Called to check if the slot can be added to a list of Slots to split the held ItemStack across.
     */
    public boolean canDragTo(Slot slot) {
        return true;
    }

    /**
     * Like the version that takes an inventory. If the given BlockEntity is not an Inventory, 0 is returned instead.
     */
    public static int getRedstoneSignalFromBlockEntity(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof Container ? getRedstoneSignalFromContainer((Container)blockEntity) : 0;
    }

    public static int getRedstoneSignalFromContainer(@Nullable Container container) {
        if (container == null) {
            return 0;
        } else {
            float f = 0.0F;

            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack itemstack = container.getItem(i);
                if (!itemstack.isEmpty()) {
                    f += (float)itemstack.getCount() / (float)container.getMaxStackSize(itemstack);
                }
            }

            f /= (float)container.getContainerSize();
            return Mth.lerpDiscrete(f, 0, 15);
        }
    }

    public void setCarried(ItemStack stack) {
        this.carried = stack;
    }

    public ItemStack getCarried() {
        return this.carried;
    }

    public void suppressRemoteUpdates() {
        this.suppressRemoteUpdates = true;
    }

    public void resumeRemoteUpdates() {
        this.suppressRemoteUpdates = false;
    }

    public void transferState(AbstractContainerMenu menu) {
        Table<Container, Integer, Integer> table = HashBasedTable.create();

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            table.put(slot.container, slot.getContainerSlot(), i);
        }

        for (int j = 0; j < this.slots.size(); j++) {
            Slot slot1 = this.slots.get(j);
            Integer integer = table.get(slot1.container, slot1.getContainerSlot());
            if (integer != null) {
                this.lastSlots.set(j, menu.lastSlots.get(integer));
                this.remoteSlots.set(j, menu.remoteSlots.get(integer));
            }
        }
    }

    public OptionalInt findSlot(Container container, int slotIndex) {
        for (int i = 0; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            if (slot.container == container && slotIndex == slot.getContainerSlot()) {
                return OptionalInt.of(i);
            }
        }

        return OptionalInt.empty();
    }

    public int getStateId() {
        return this.stateId;
    }

    public int incrementStateId() {
        this.stateId = this.stateId + 1 & 32767;
        return this.stateId;
    }
}

```