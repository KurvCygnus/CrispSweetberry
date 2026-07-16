//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.client.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.lib.core.registry.IRegistrant;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

/**
 * The creative tab registry, which defines the <u>{@link ResourceKey resourceKey}</u>, <u>{@link CreativeModeTab}</u> of this mod only.<br>
 * <span style="color: f84b4b"><i>None of any creative mode tab content is included at here.</i></span>
 * @implNote All tab content is registered at runtime, with the help of <u>{@link kurvcygnus.crispsweetberry.utils.core.RegisterToTab @RegisterToTab}</u>,
 * <u>{@link kurvcygnus.crispsweetberry.lib.core.registry.CrispRegistrationManager CrispRegistrationManager}</u>, and the {@code package-private} event at
 * <u>{@link CrispSweetberry entry class}</u>.
 * @since 1.0 Release
 */
public enum CrispCreativeTabs implements IRegistrant<CrispCreativeTabs>
{
    INST;
    
    @Override public void register(@NotNull IRegisterAction registerLogic) { registerLogic.register(CRISP_TAB_REGISTER); }
    
    @Override public boolean isFeature() { return false; }
    
    @Override public @NotNull String getJob() { return "Creative Tab"; }
    
    @Override public @NotNull PriorityPair getPriority() { return ofPriority(PriorityRange.REFERENCE_HOLDER, 1); }
    
    public static final DeferredRegister<CreativeModeTab> CRISP_TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CrispSweetberry.NAMESPACE);
    
    public static final ResourceKey<CreativeModeTab> CRISP_CREATIVE_TAB = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        DefinitionUtils.getModNamespacedLocation("crisp_tab")
    );
    
    @AutoI18n(value = {
        "en_us = Crisp Sweetberry",
        "lol_us = TA2TY FRUT",
        "zh_cn = 澄莓物语"
        },
        key = "tabtitle"
    )
    public static final Holder<CreativeModeTab> CRISP_SWEETBERRY_TAB = CRISP_TAB_REGISTER.register(
        "crisp_tab",
        CreativeModeTab.builder().
            title(Component.translatable("crispsweetberry.creativetab.tabtitle")).
            withTabsBefore(CreativeModeTabs.COMBAT).
            icon(Items.SWEET_BERRIES::getDefaultInstance)::build
    );
}
