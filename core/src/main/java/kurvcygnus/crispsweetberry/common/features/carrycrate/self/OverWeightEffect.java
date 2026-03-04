//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.self;

import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class OverWeightEffect extends MobEffect
{
    public static final String OVERWEIGHT_KEY = "effect.overweight";
    public static final ResourceLocation OVERWEIGHT_ID = CrispDefUtils.getModNamespacedLocation(OVERWEIGHT_KEY);
    
    private OverWeightEffect()
    {
        super(
            MobEffectCategory.HARMFUL,
            795548//* Brown uwu
        );
    }
    
    public static @NotNull MobEffect register(@NotNull DeferredRegister<MobEffect> deferredRegister)
    {
        Objects.requireNonNull(
            deferredRegister,
            "This static constructor is only used for registration, provide DeferredRegister<MobEffect>!"
        );
        
        return new OverWeightEffect().addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            OVERWEIGHT_ID,
            -0.25F,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}
