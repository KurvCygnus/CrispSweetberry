//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.core.registry;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.lib.base.datastructure.CrispRanger;
import kurvcygnus.crispsweetberry.lib.core.log.MarkLogger;
import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.*;

/**
 * This is the class that processes all <u>{@link IRegistrant}</u> implementers.
 * @implNote <b>This class won't take any memories after all mods' initialization are completed.
 * Once <u>{@link FMLLoadCompleteEvent}</u> is fired, this class will destroy its only one instance with its fields immediately.</b>
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see IRegistrant
 * @see #register(ModContainer, IEventBus) Usage
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE) public final class CrispRegistrationManager
{
    private static @Nullable CrispRegistrationManager INSTANCE = new CrispRegistrationManager();
    
    private final MarkLogger logger = MarkLogger.withMarkerSuffixes(LogUtils.getLogger(), "REGISTRY_MANAGER");
    private final CrispRanger legalPriorityRange = CrispRanger.closed(1, 999);
    private final Set<ModContainer> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    
    private CrispRegistrationManager() {}
    
    @SubscribeEvent static void destroyInstance(@NotNull FMLLoadCompleteEvent event)
    {
        assert INSTANCE != null;
        INSTANCE.logger.info("Registration phase finished, RegistrationManager destroyed.");
        INSTANCE = null;
    }
    
    /**
     * This method starts the automatic registration of a mod.<br>
     * You should call this method in your mod's entry class's {@code <init>} method(<i>a.k.a Constructor</i>),
     * with <u>{@link CrispRegistrationManager#getInstance() CrispRegistrationManager.getInstance()}</u>{@code .register(ModContainer, IEventBus)}.
     * <hr>
     * <b>This class has a strict lifecycle, any improper, or invalid call will get exception.</b>
     */
    public void register(@NotNull ModContainer modContainer, @NotNull IEventBus eventBus)
    {
        Objects.requireNonNull(modContainer, "Param \"modContainer\" must not be null!");
        Objects.requireNonNull(eventBus, "Param \"eventBus\" must not be null!");
        if(!visited.add(modContainer))
            return;
        
        logger.info("Searching registries for mod: {}...", modContainer.getModId());
        
        final ModFileScanData scanData = modContainer.getModInfo().getOwningFile().getFile().getScanResult();
        
        scanData.getClasses().stream().
            filter(data -> data.interfaces().contains(Type.getType(IRegistrant.class))).
            map(
                data ->
                {
                    try
                    {
                        final Class<?> clazz = Class.forName(data.clazz().getClassName());
                        if(clazz.isEnum() && clazz.getEnumConstants().length == 1)
                        {
                            final IRegistrant registrant = (IRegistrant) clazz.getEnumConstants()[0];
                            
                            FunctionalUtils.throwIf(
                                !legalPriorityRange.inRange(registrant.getPriority().priority()),
                                "Invalid priority: %s".formatted(registrant.getPriority().priority()),
                                IllegalArgumentException::new
                            );
                            return registrant;
                        }
                        logger.warn("Skipped class \"{}\": Not a singleton enum.", clazz.getName());
                    }
                    catch(Exception e) { logger.error("Failed to instantiate registry: {}", data.clazz().getClassName(), e); }
                    return null;
                }
            ).
            filter(Objects::nonNull).
            sorted(Comparator.comparingInt(IRegistrant::getFullPriority)).
            forEach(
                registrant ->
                {
                    registrant.register(eventBus);
                    logger.info(
                        "[{}] Registering {}{}...",
                        modContainer.getModId(),
                        registrant.isFeature() ? "Feature: " : "",
                        registrant.getJob()
                    );
                }
            );
    }
    
    public static @NotNull CrispRegistrationManager getInstance()
    {
        if(INSTANCE == null)
            throw new IllegalStateException("Registration phase is already over!");
        return INSTANCE;
    }
}