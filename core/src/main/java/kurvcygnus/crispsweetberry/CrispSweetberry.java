//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry;

import kurvcygnus.crispsweetberry.client.init.CrispCreativeTabsRegistryEvent;
import kurvcygnus.crispsweetberry.common.config.CrispConfig;
import kurvcygnus.crispsweetberry.lib.base.lang.ISealableBox;
import kurvcygnus.crispsweetberry.lib.base.lang.IVault;
import kurvcygnus.crispsweetberry.lib.core.log.IMarkLogger;
import kurvcygnus.crispsweetberry.lib.core.registry.CrispRegistrationManager;
import kurvcygnus.crispsweetberry.lib.core.registry.IRegistrant;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import kurvcygnus.crispsweetberry.utils.core.RegisterToTab;
import kurvcygnus.crispsweetberry.utils.core.TabEntry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

/**
 * This is the entrance class of the whole project.
 * @see RegisterToTab Our Creative Tabs Item Registration Implementation
 * @see IRegistrant Our Content Registration Implementation
 * @since Always here!
 */
@Mod(CrispSweetberry.NAMESPACE) @EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public final class CrispSweetberry
{
    public static final String NAMESPACE = "crispsweetberry";
    public static final ISealableBox<IEventBus> CRISP_BUS = ISealableBox.of(
        Objects.requireNonNull(
            ModLoadingContext.get().getActiveContainer().getEventBus(),
            () -> DefinitionUtils.quickFormat("Fatal: ModBus seems to be null! \n{}", MetainfoConstants.FEEDBACK_MESSAGE)
        )
    );
    
    private static final Map<ResourceKey<CreativeModeTab>, Set<TabEntry>> TAB_ENTRIES = new HashMap<>();
    
    /**
     * @implNote This cannot be optimized with <u>{@link ISealableBox}</u>, because <b><u>{@link BuildCreativeModeTabContentsEvent}</u> is
     * multi-rounded and asynchronous, also, as player exited the world, entering the new one, creative tabs will be reloaded</b>, so
     * we can only use <u>{@link IVault}</u>.
     */
    public static final IVault<Map<ResourceKey<CreativeModeTab>, Set<TabEntry>>, BuildCreativeModeTabContentsEvent> TAB_LOOKUP =
        IVault.ofAccessLimited(TAB_ENTRIES, CrispCreativeTabsRegistryEvent.class);
    
    private static final IMarkLogger LOGGER = IMarkLogger.withMarkerSuffixes("MOD_INIT");
    
    @SuppressWarnings("unchecked")//! As you can see, this casting is actually reliable.
    public CrispSweetberry(@NotNull IEventBus eventBus, @NotNull ModContainer modContainer)
    {
        modContainer.registerConfig(ModConfig.Type.CLIENT, CrispConfig.SPEC);
        
        LOGGER.info("Initializing Configurations...");
        
        LOGGER.info("Initializing registries...");
        
        CrispRegistrationManager.registerWithAnnotationDelegate(
            modContainer,
            eventBus,
            RegisterToTab.class,
            (annotation, annotationTarget) ->
            {
                final @Nullable Supplier<? extends Item> supplier = switch(annotationTarget)
                {
                    case Supplier<?> getter -> (Supplier<? extends Item>) getter;
                    case Item item -> () -> item;
                    default -> null;
                };
                
                if(supplier == null)
                    return;
                
                TAB_ENTRIES.computeIfAbsent(annotation.value().toCreativeTab(), FunctionalUtils.supplierToFunction(LinkedHashSet::new)).
                    add(new TabEntry(supplier, annotation.value().toCreativeTab()));
            }
        );
        
        LOGGER.info("CrispSweetberry initialized!");
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST) static void sealModBus(@NotNull FMLLoadCompleteEvent event) { CRISP_BUS.seal(); }
}