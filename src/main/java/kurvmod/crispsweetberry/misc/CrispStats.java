package kurvmod.crispsweetberry.misc;

import kurvmod.crispsweetberry.CrispSweetberry;
import net.minecraft.resources.ResourceLocation;

/**
 * Custom stat ID used by this mod.
 *
 * <p><b>WARNING:</b><br>
 *  Custom stats are <i>lazily created</i> by {@link net.minecraft.stats.Stats#CUSTOM}
 *  when first accessed. Merely defining a {@link net.minecraft.resources.ResourceLocation}
 *  is sufficient.</p>
 *
 * <p><b>Do NOT attempt to register this stat!</b><br>
 *  {@code Registries.CUSTOM_STAT} is a read-only, built-in registry and cannot be
 *  written to by mods.</p>
 */
public final class CrispStats
{
    public static final ResourceLocation INTERACT_WITH_KILN = ResourceLocation.fromNamespaceAndPath(CrispSweetberry.MOD_ID, "interact_with_kiln");
}
