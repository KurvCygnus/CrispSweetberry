//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.entity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.AbstractCarryAdapter;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public abstract class AbstractEntityCarryAdapter<E extends LivingEntity> extends AbstractCarryAdapter
{
    /**
     * @apiNote During <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u>, the implementation will create an adapter
     * with <b>{@code null}</b> as adapter's param, since <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u> shouldn't use it.
     * <br><br>
     * In a nutshell, <b>using {@code entity} during <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u>
     * will throw <u>{@link NullPointerException}</u></b>.
     */
    private final @Nullable E entity;
    
    public AbstractEntityCarryAdapter(@Nullable E entity) { this.entity = entity; }
    
    @Override public abstract @Range(from = 0, to = Integer.MAX_VALUE) int getPenaltyRate();
    
    /**
     * @apiNote During <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u>, the implementation will create an adapter
     * with <b>{@code null}</b> as adapter's param, since <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u> shouldn't use it.
     * <br><br>
     * In a nutshell, <b>using {@code #getEntity()} during <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u>
     * will throw <u>{@link NullPointerException}</u>, and you shouldn't use this at most cases.</b>
     */
    protected final @NotNull E getEntity() 
    {
        Objects.requireNonNull(
            entity,
            """
                Assertion failed: Field "blockEntity" happens to be null, this shouldn't be happen, which usually means
                method is called at improper time, with improper param. %s
                """.
                formatted(MiscConstants.FEEDBACK_MESSAGE)
        );
        
        return entity;
    }
}
