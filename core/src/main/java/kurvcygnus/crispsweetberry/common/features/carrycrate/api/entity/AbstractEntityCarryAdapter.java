//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.entity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.AbstractCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableExtensions;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

/**
 * This is the basic of Entity Adapters, which doesn't include any usable logics.
 * @apiNote <span style="color: 95ce6d">At the most cases, these's no need to register your own entity with an adapter.</span><br>
 * For further details, see <u>{@link AdaptiveAnimalCarryAdapter}</u>.
 * @param <E> The entity this adapter takes responsibility of.
 * @author Kurv Cygnus
 * @see AdaptiveAnimalCarryAdapter Utility Adapter
 * @since 1.0 Release
 */
public abstract class AbstractEntityCarryAdapter<E extends LivingEntity> extends AbstractCarryAdapter<CarryData.CarryEntityDataHolder>
{
    /**
     * @apiNote During <u>{@link #carryingTick(CarriableExtensions.TickingContext) #carryingTick(TickingContext)}</u>, the implementation will create an adapter
     * with <b>{@code null}</b> as adapter's param, since <u>{@link #carryingTick(CarriableExtensions.TickingContext) #carryingTick(TickingContext)}</u> shouldn't use it.
     * <br><br>
     * In a nutshell, <b>using {@code entity} during <u>{@link #carryingTick(CarriableExtensions.TickingContext) #carryingTick(TickingContext)}</u>
     * will throw <u>{@link NullPointerException}</u></b>.
     */
    private final @Nullable E entity;
    
    @SuppressWarnings("unchecked")//! Due to the limitation of javac's generic deduction, we have to choose runtime check.
    public AbstractEntityCarryAdapter(@Nullable LivingEntity entity) 
    {
        if(entity == null)
        {
            this.entity = null;
            return;
        }
        
        try { this.entity = (E) entity; }
        catch(ClassCastException ex) 
        {
            throw new IllegalArgumentException(
                "Adapter Type Mismatch! Attempted to bind adapter to entity: %s, but this adapter expects: %s".
                    formatted(entity.getClass().getSimpleName(), this.getClass().getSimpleName())
            );
        }
    }
    
    @Override public abstract @Range(from = NO_PENALTY, to = Integer.MAX_VALUE) int getPenaltyRate();
    
    /**
     * @apiNote During <u>{@link #carryingTick(CarriableExtensions.TickingContext) #carryingTick(TickingContext)}</u>, the implementation will create an adapter
     * with <b>{@code null}</b> as adapter's param, since <u>{@link #carryingTick(CarriableExtensions.TickingContext) #carryingTick(TickingContext)}</u> shouldn't use it.
     * <br><br>
     * In a nutshell, <b>using {@code #getEntity()} during <u>{@link #carryingTick(CarriableExtensions.TickingContext) #carryingTick(TickingContext)}</u>
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
                formatted(MetainfoConstants.FEEDBACK_MESSAGE)
        );
        
        return entity;
    }
}
