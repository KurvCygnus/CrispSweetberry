//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry;

import com.google.errorprone.annotations.DoNotCall;
import kurvcygnus.crispsweetberry.lib.base.lang.ISealableBox;
import kurvcygnus.crispsweetberry.lib.base.util.TextUtils;
import kurvcygnus.crispsweetberry.lib.core.log.IMarkLogger;
import kurvcygnus.crispsweetberry.lib.core.registry.CrispRegistrationManager;
import kurvcygnus.crispsweetberry.lib.core.registry.IRegistrant;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import kurvcygnus.crispsweetberry.utils.core.RegisterToTab;
import kurvcygnus.crispsweetberry.utils.core.TabEntry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
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
            TextUtils.format("Fatal: ModBus seems to be null! \n{}", MetainfoConstants.FEEDBACK_MESSAGE)
        )
    );
    
    @SuppressWarnings("unchecked")
    public CrispSweetberry(@NotNull IEventBus eventBus, @NotNull ModContainer modContainer)
    {
        CrispRegistrationManager.registerWithAnnotationDelegate(
            modContainer,
            eventBus,
            RegisterToTab.class,
            (annotation, annotationTarget) ->
            {
                final @Nullable Supplier<? extends Item> supplier = switch(annotationTarget)
                {
                    //! Valid. [[RegisterToTab]] should only be used on [[Supplier]], [[net.minecraft.core.Holder]] and
                    //! [[DeferredHolder]], and the creative tab registry is also only for [[Item]].
                    case Supplier<?> getter -> (Supplier<? extends Item>) getter;
                    case Item item -> () -> item;
                    default -> null;
                };
                
                if(supplier == null)
                    return;
                
                CrispCreativeTabsRegistryEvent.TAB_ENTRIES.computeIfAbsent(annotation.value().toCreativeTab(), __ -> new LinkedHashSet<>()).
                    add(new TabEntry(supplier, annotation.value().toCreativeTab()));
            }
        );
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST) static void destroyModBus(@NotNull FMLLoadCompleteEvent event) { CRISP_BUS.seal(); }
}

/**
 * The executor of annotation <b><u>{@link RegisterToTab @RegisterToTab}</u></b>.<br>
 * <b>It automatically registers every entry that presents {@link RegisterToTab @RegisterToTab} to designated tabs.</b>
 * @implNote This class should be {@code package-private}, since making <u>{@link CrispCreativeTabsRegistryEvent#TAB_ENTRIES}</u> {@code public} will cause access issues.
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE, value = Dist.CLIENT)
final class CrispCreativeTabsRegistryEvent
{
    private static final IMarkLogger LOGGER = IMarkLogger.withMarkerSuffixes("TAB_REGISTRY");
    
    static final Map<ResourceKey<CreativeModeTab>, SequencedSet<TabEntry>> TAB_ENTRIES = new HashMap<>();
    
    @SubscribeEvent @DoNotCall static void tabRegistryEvent(final @NotNull BuildCreativeModeTabContentsEvent event)
    {
        final @Nullable var entries = TAB_ENTRIES.get(event.getTabKey());
        
        if(entries == null)
            return;
        
        for(final var entry: entries)
        {
            final var item = entry.itemSupplier().get();
            final var tabKey = entry.tab();
            
            //* Yes, using `==` to compare is legal.
            //* Firstly, [[ResourceKey]] didn't implement [[Object#equals]](which is terrible, this also happens on [[EntityType]]).
            //* Secondly, don't forget that most value holders in Minecraft are CONSTANTS,
            //* whose does have a fixed hashcode, thus can be compared with `==`, because `==` is a CPU command, it is slightly faster.
            if(tabKey == event.getTabKey())
            {
                event.accept(item);
                LOGGER.debug("Registered item \"{}\" to tab \"{}\".", item.getDefaultInstance().getDisplayName().getString(), tabKey);
            }
        }
    }
}