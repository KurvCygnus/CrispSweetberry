//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.registry;

import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * This the core of automatic registration.
 * @apiNote Make sure the registry class is an enum, and has only one enumeration named {@code INSTANCE}, it makes automatic registration 
 * works correctly, and help others understand automatic registration quickly.
 * @implNote <h2>Some Q&A which will probably happen:</h2>
 * <ul>
 *     <li>
 *         Q: Is automatic registration with reflection dangerous, or unstable?<br>
 *         A: No. It depends. To put it simple, using {@code private} level constructor won't gonna work, since initialization phase is quite sensitive about 
 *         {@code private} access, which will throw some expr in the end. Using annotation is also same as using {@code private} level constructor.
 *         And directly use {@code public} constructor may lead to abuse, thus, <b>using <u>{@link Enum}</u> is the best choice, because
 *         <a href="https://en.wikipedia.org/wiki/Singleton_pattern"><u>{@code Singleton}</u></a>
 *         mode is based on convention, and <u>{@link Enum}</u> is more mandatory than that</b>. Also, it is stable.</li>
 *     <li>
 *         Q: Will automatic registration lead to lifecycle issues?<br>
 *         A: Yes, <b>but if it happened, mostly is not automatic registration's fault</b>. In fact, non-automatic registration will also encounter such issues
 *         if you didn't notice the order of registries' initialization. Anyway, lifecycle issue can be solved by adjusting <u>{@link #getPriority()}</u>, 
 *         it is usually not a big deal, for our <u>{@link kurvcygnus.crispsweetberry.common.features features package}</u> and 
 *         <u>{@link kurvcygnus.crispsweetberry.common.qol QoL package}</u>, whose are DDD driven, we use <u>{@link net.neoforged.neoforge.common.util.Lazy Lazy}</u>
 *         when necessary.
 *     </li>
 *     <li>
 *         Q: Isn't manually adjusting initialization priorities troublesome?<br>
 *         A: Yeah, you got me. This is the only tricky issue we are deal with. Currently we have no solution about it.<br>
 *         We are considering using <u>{@link Enum}</u> to decide main registration key, and using <u>{@link #getPriority()}</u> 
 *         to confirm the detailed order. However, that another things that will happen when mod becomes much bigger than now, 
 *         currently it is not worth it.
 *     </li>
 * </ul>
 * @since Release 1.0
 * @author Kurv Cygnus
 */
public interface IRegistrant
{
    void register(@NotNull IEventBus bus);
    
    boolean isFeature();
    
    @NotNull String getJob();
    
    /**
     * Used for order sensitive registries.
     *
     * @apiNote Higher number has higher property.
     */
    @NotNull PriorityPair getPriority();
    
    default int getFullPriority() { return getPriority().priorityRange().getPriority() + getPriority().priority; }
    
    record PriorityPair(@NotNull PriorityRange priorityRange, @Range(from = 1, to = 99) int priority) {}
    
    enum PriorityRange
    {
        MISC,
        FEATURE,
        REFERENCE_HOLDER;
        
        private final int priority;
        
        PriorityRange() { this.priority = (this.ordinal() + 1) * 100; }
        
        public int getPriority() { return this.priority; }
    }
}
