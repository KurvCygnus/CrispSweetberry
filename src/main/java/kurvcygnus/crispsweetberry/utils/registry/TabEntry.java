package kurvcygnus.crispsweetberry.utils.registry;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public record TabEntry(Supplier<? extends Item> itemSupplier, boolean condition) {}
