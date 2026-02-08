package kurvcygnus.crispsweetberry.common.features.ttorches;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.TemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.TemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableTorchItem;
import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.DEFAULT_TEMP_TORCH_PARTICLE;

/**
 * This registers everything that relates to throwable torch series.<br>
 * <i>Since the first letter of this series content are all {@code 'T'}, thus both registry and package are called {@code TTorch}.</i>
 * @since 1.0 Release
 */
public enum TTorchRegistries implements IRegistrant
{
    INSTANCE;
    
    @Override
    public void register(@NotNull IEventBus bus) { REGISTRIES.forEach(register ->  register.register(bus)); }
    
    @Override
    public boolean isFeature() { return true; }
    
    @Override
    public @NotNull String getJob() { return "Temporary Torches"; }
    
    @Override
    public int getPriority() { return 6; }
    
    private static final DeferredRegister<Item> THROWABLE_TORCH_REGISTER = DeferredRegister.createItems(CrispSweetberry.NAMESPACE);
    private static final DeferredRegister<Block> TEMPORARY_TORCH_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.NAMESPACE);
    private static final DeferredRegister<EntityType<?>> THROWN_TORCH_REGISTER = DeferredRegister.create(Registries.ENTITY_TYPE, CrispSweetberry.NAMESPACE);
    
    public static final List<DeferredRegister<?>> REGISTRIES = List.of(
        THROWABLE_TORCH_REGISTER,
        TEMPORARY_TORCH_REGISTER,
        THROWN_TORCH_REGISTER
    );
    
    @AutoI18n({
        "en_us -> Thrown Torch",
        "lol_us -> fullee lite stik",
        "zh_cn -> 投掷火把"
    })
    public static final Holder<Block> TEMPORARY_TORCH = TEMPORARY_TORCH_REGISTER.register("temporary_torch", resourceLocation ->
        new TemporaryTorchBlock(DEFAULT_TEMP_TORCH_PARTICLE)
    );
    
    @AutoI18n({
        "en_us -> Thrown Torch",
        "lol_us -> fullee lite stik",
        "zh_cn -> 投掷火把"
    })
    public static final Holder<Block> TEMPORARY_WALL_TORCH = TEMPORARY_TORCH_REGISTER.register("temporary_wall_torch", resourceLocation ->
        new TemporaryWallTorchBlock(DEFAULT_TEMP_TORCH_PARTICLE)
    );
    
    @RegisterToTab
    @AutoI18n({
        "en_us -> Throwable Torch",
        "lol_us -> chuk da lite stik",
        "zh_cn -> 投掷火把"
    })
    public static final Holder<Item> THROWABLE_TORCH = THROWABLE_TORCH_REGISTER.register("throwable_torch", resourceLocation ->
        new ThrowableTorchItem(new Item.Properties())
    );
    
    @AutoI18n({
        "en_us -> Thrown Torch",
        "lol_us -> Spinn' Stik",
        "zh_ch -> 投掷火把"
    })
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownTorchEntity>> THROWN_TORCH = THROWN_TORCH_REGISTER.register("thrown_torch", () ->
        EntityType.Builder.<ThrownTorchEntity>of(ThrownTorchEntity::new, MobCategory.MISC).
            sized(0.25F, 0.25F).
            updateInterval(10).
            noSummon().
            build("thrown_torch")
    );
}
