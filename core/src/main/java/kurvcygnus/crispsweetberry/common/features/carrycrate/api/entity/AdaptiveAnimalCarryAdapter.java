//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.entity;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * As its name implied, this is an adapter for animal entities, which features auto compat.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @param <E> Any entity that is inherited from <u>{@link Animal}</u>.
 * @apiNote At most cases, animal entity's compat is not necessary.<br>
 * Before the Carry Registry's access frozen, an automatic compat task will be activated to find, and register 
 * animal entities that meet the <u>{@link #MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME condition}</u>.<br><br>
 * <b>So, for most custom animals, there's no need to write compat manually</b>, unless you have further customization demand, then you should 
 * write it, and the auto compat will ignore it.
 */
public final class AdaptiveAnimalCarryAdapter<E extends Animal> extends AbstractEntityCarryAdapter<E>
{
    /**
     * <u>{@link net.minecraft.world.entity.animal.Cow Cow}</u>'s <u>{@link net.minecraft.world.phys.AABB Bounding Box}</u>.<br>
     * Here we take it as the biggest animal that could be boxed.<br><br>
     * <i>{@code 0.9D} is its width and length, and {@code 1.4D} is its height</i>.
     */
    public static final double MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME = Math.pow(0.9D, 2) * 1.4D;
    private final int penaltyRate;
    
    public AdaptiveAnimalCarryAdapter(@Nullable E entity) 
    {
        super(entity);
        
        if(entity != null)
        {
            final AABB boundingBox = entity.getBoundingBox();
            final double volume = boundingBox.getXsize() * boundingBox.getYsize() * boundingBox.getZsize();
            
            if(volume > MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME)
                throw new IllegalArgumentException(
                    "Entity \"%s\" is too big to be boxed into Carry Crate! Volume: %f, Expected: Smaller than %f".
                        formatted(entity.getName().getString(), volume, MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME)
                );
            
            this.penaltyRate = (int) (DEFAULT_PENALTY_RATE /
                ((volume == 0D ? MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME : volume) / MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME));
        }
        else//! The registry will grantee that this statement will only be used in the implementation.
            this.penaltyRate = -1;
    }
    
    @Override public @Range(from = NO_PENALTY, to = Integer.MAX_VALUE) int getPenaltyRate() { return this.penaltyRate; }
}
