//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.config.CrispConfig;
import kurvcygnus.crispsweetberry.lib.core.log.MarkLogger;
import kurvcygnus.crispsweetberry.lib.core.registry.CrispRegistrationManager;
import kurvcygnus.crispsweetberry.lib.core.registry.IRegistrant;
import kurvcygnus.crispsweetberry.lib.core.registry.RegisterToTab;
import kurvcygnus.crispsweetberry.lib.core.registry.TabEntry;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

/**
 * This is the entrance class of the whole project.
 * @see RegisterToTab Our Creative Tabs Item Registration Implementation
 * @see IRegistrant Our Content Registration Implementation
 * @since Always here!
 */
@Mod(CrispSweetberry.NAMESPACE)
public final class CrispSweetberry
{
    public static final String NAMESPACE = "crispsweetberry";
    public static final @NotNull IEventBus CRISP_BUS = Objects.requireNonNull(
        ModLoadingContext.get().getActiveContainer().getEventBus(),
        "Fatal: ModBus seems to be null! \n%s".formatted(MetainfoConstants.FEEDBACK_MESSAGE)
    );
    
    private static final List<String> ANNOTATIONS = List.of(RegisterToTab.class.getName());
    
    public static final Map<ResourceKey<CreativeModeTab>, Set<TabEntry>> TAB_LOOKUP = new HashMap<>();
    
    private static final MarkLogger LOGGER = MarkLogger.withMarkerSuffixes(LogUtils.getLogger(), "MOD_INIT");
    
    public CrispSweetberry(@NotNull IEventBus eventBus, @NotNull ModContainer modContainer)
    {
        LOGGER.info("Collecting annotation info...");
        final ModFileScanData scanData = modContainer.getModInfo().getOwningFile().getFile().getScanResult();
        
        final List<ModFileScanData.AnnotationData> annotationData = scanData.getAnnotations().stream().filter(
            data -> ANNOTATIONS.contains(data.annotationType().getClassName())
        ).toList();
        
        LOGGER.info("Annotation collection completed!");
        LOGGER.info("Start tab entries' pre-collect...");
        
        for(final var data: annotationData)
        {
            try
            {
                final Class<?> clazz = Class.forName(data.clazz().getClassName());
                final Field field = clazz.getDeclaredField(data.memberName());
                
                field.setAccessible(true);
                
                final @Nullable RegisterToTab registerToTab = field.getAnnotation(RegisterToTab.class);
                
                if(registerToTab == null)
                    continue;
                
                final Object value = field.get(null);
                final Supplier<? extends Item> supplier = wrapToSupplier(value);
                
                if(supplier != null)
                    TAB_LOOKUP.computeIfAbsent(registerToTab.tabGroup().toCreativeTab(), ignored -> new HashSet<>()).
                        add(new TabEntry(supplier, registerToTab.tabGroup().toCreativeTab(), registerToTab.registerCondition()));
            }
            catch(Exception e) { LOGGER.error("Failed to pre-cache tab entry. Details: ", e); }
        }
        
        LOGGER.info("Finished the pre-collection of tab entries!");

        modContainer.registerConfig(ModConfig.Type.CLIENT, CrispConfig.SPEC);
        
        LOGGER.info("Initializing Configurations...");
        
        LOGGER.info("Initializing registries...");
        CrispRegistrationManager.getInstance().register(modContainer, eventBus);
        LOGGER.info("CrispSweetberry initialized!");
    }
    
    @SuppressWarnings("unchecked")//! As you can see, this casting is actually reliable.
    private @Nullable Supplier<? extends Item> wrapToSupplier(@Nullable Object value)
    {
        return switch(value)
        {
            case Supplier<?> supplier -> (Supplier<? extends Item>) supplier;
            case Item item -> () -> item;
            case null, default -> null;
        };
    }
}