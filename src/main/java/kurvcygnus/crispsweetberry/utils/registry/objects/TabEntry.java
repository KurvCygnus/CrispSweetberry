package kurvcygnus.crispsweetberry.utils.registry.objects;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public record TabEntry(Supplier<? extends Item> itemSupplier, ResourceKey<CreativeModeTab> tab, boolean condition) {}
