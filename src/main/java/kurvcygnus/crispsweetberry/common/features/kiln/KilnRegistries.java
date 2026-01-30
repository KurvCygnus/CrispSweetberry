package kurvcygnus.crispsweetberry.common.features.kiln;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnMenu;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipeSerializer;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipeType;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import kurvcygnus.crispsweetberry.utils.registry.annotations.BanFromTabRegistry;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public final class KilnRegistries
{
    private KilnRegistries() { throw new IllegalAccessError(); }
    
    @BanFromTabRegistry private static final DeferredRegister<Item> KILN_ITEM_REGISTER = DeferredRegister.createItems(CrispSweetberry.MOD_ID);
    @BanFromTabRegistry public static final DeferredRegister<Block> KILN_BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.MOD_ID);
    @BanFromTabRegistry private static final DeferredRegister<BlockEntityType<?>> KILN_BE_REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CrispSweetberry.MOD_ID);
    @BanFromTabRegistry private static final DeferredRegister<RecipeType<?>> KILN_RECIPE_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, CrispSweetberry.MOD_ID);
    @BanFromTabRegistry private static final DeferredRegister<RecipeSerializer<?>> KILN_SERIALIZER_REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, CrispSweetberry.MOD_ID);
    @BanFromTabRegistry private static final DeferredRegister<MenuType<?>> KILN_MENU_REGISTER = DeferredRegister.create(Registries.MENU, CrispSweetberry.MOD_ID);
    
    @BanFromTabRegistry public static final List<DeferredRegister<?>> REGISTRIES = List.of(
        KILN_ITEM_REGISTER,
        KILN_BLOCK_REGISTER,
        KILN_BE_REGISTER,
        KILN_RECIPE_REGISTER,
        KILN_SERIALIZER_REGISTER,
        KILN_MENU_REGISTER
    );
    
    @BanFromTabRegistry public static final Holder<Block> KILN_BLOCK = KILN_BLOCK_REGISTER.register("kiln", resourceLocation -> new KilnBlock());
    
    @RegisterToTab
    public static final Holder<Item> KILN = KILN_ITEM_REGISTER.register("kiln", resourceLocation ->
        new BlockItem(KILN_BLOCK.value(), new Item.Properties())
    );
    
    @SuppressWarnings("ConstantConditions") //* https://docs.neoforged.net/docs/1.21.1/blockentities/ You can find the reason of suppression here.
    @BanFromTabRegistry
    public static final Supplier<BlockEntityType<KilnBlockEntity>> KILN_BLOCK_ENTITY = KILN_BE_REGISTER.register("kiln_block_entity", () ->
        BlockEntityType.Builder.of(KilnBlockEntity::new, KilnRegistries.KILN_BLOCK.value()).
            build(null)//* Build using null; vanilla does some datafixer with the parameter that we don't need.
    );
    
    @BanFromTabRegistry public static final DeferredHolder<RecipeType<?>, KilnRecipeType> KILN_RECIPE_TYPE = KILN_RECIPE_REGISTER.register("kiln", () ->
        KilnRecipeType.INSTANCE
    );
    
    @BanFromTabRegistry 
    public static final DeferredHolder<RecipeSerializer<?>, KilnRecipeSerializer> KILN_SERIALIZER = KILN_SERIALIZER_REGISTER.register("kiln", KilnRecipeSerializer::new);
    
    @BanFromTabRegistry public static final Supplier<MenuType<KilnMenu>> KILN_MENU = KILN_MENU_REGISTER.register("kiln_menu", () ->
        new MenuType<>(KilnMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );
    
    @BanFromTabRegistry public static final ResourceLocation INTERACT_WITH_KILN = CrispDefUtils.getModNamespacedLocation("interact_with_kiln");
}