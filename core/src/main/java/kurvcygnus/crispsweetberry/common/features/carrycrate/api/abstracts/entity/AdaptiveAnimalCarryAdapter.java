//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.entity;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class AdaptiveAnimalCarryAdapter<E extends Animal> extends AbstractEntityCarryAdapter<E>
{
    /**
     * <u>{@link net.minecraft.world.entity.animal.Cow Cow}</u>'s <u>{@link net.minecraft.world.phys.AABB Bounding Box}</u>.<br>
     * Here we take it as the biggest animal that could be boxed.<br><br>
     * <i>{@code 0.9D} is its width and length, and {@code 1.4D} is its height</i>.
     */
    public static final double MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME = Math.pow(0.9D, 2) * 1.4D;
    private final int penaltyRate;
    
    public AdaptiveAnimalCarryAdapter(@NotNull E entity) 
    {
        super(entity);
        
        final AABB boundingBox = entity.getBoundingBox();
        final double volume = boundingBox.getXsize() * boundingBox.getYsize() * boundingBox.getZsize();
        
        if(volume > MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME)
            throw new IllegalArgumentException(
                "Entity \"%s\" is too big to be boxed into Carry Crate QAQ! Volume: %f, Expected: Smaller than %f".
                    formatted(entity.getName().getString(), volume, MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME)
            );
        
        this.penaltyRate = (int) (DEFAULT_PENALTY_RATE / 
            ((volume == 0D ? MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME : volume) / MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME));
    }
    
    @Override public @Range(from = 0, to = Integer.MAX_VALUE) int getPenaltyRate() { return this.penaltyRate; }
}
