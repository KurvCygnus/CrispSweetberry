package kurvcygnus.crispsweetberry.common.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableTorchItem;
import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import net.minecraft.core.Holder;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public enum CrispItems implements IRegistrant
{
    INSTANCE;
    @Override
    public void register(@NotNull IEventBus bus) { CRISP_ITEM_REGISTER.register(bus); }
    
    @Override
    public boolean isFeature() { return false; }
    
    @Override
    public @NotNull String getJob() { return "Misc Items"; }
    
    @Override
    public int getPriority() { return 1; }
    
    public static final DeferredRegister<Item> CRISP_ITEM_REGISTER = DeferredRegister.createItems(CrispSweetberry.NAMESPACE);
    
    @RegisterToTab
    @AutoI18n({
        "en_us -> Throwable Torch",
        "lol_us -> chuk da lite stik",
        "zh_cn -> 投掷火把"
    })
    public static final Holder<Item> THROWABLE_TORCH = CRISP_ITEM_REGISTER.register("throwable_torch", resourceLocation ->
        new ThrowableTorchItem(new Item.Properties())
    );
    
    public static final Holder<Item> HONEY_BERRY = CRISP_ITEM_REGISTER.register("crisp_sweetberry", resourceLocation ->
        new Item(
            new Item.Properties().
                food(new FoodProperties.Builder().
                    nutrition(1).
                    saturationModifier(8.0F).
                    build()
                )
        )
    );
    
    @RegisterToTab
    @AutoI18n({
        "en_us -> Carry Crate",
        "lol_us -> hoom",
        "zh_cn -> 搬运箱"
    })
    public static final Holder<Item> CARRY_CRATE = CRISP_ITEM_REGISTER.register("carry_crate", resourceLocation ->
        new BlockItem(CrispBlocks.CARRY_CRATE.value(), new Item.Properties())
    );
}
