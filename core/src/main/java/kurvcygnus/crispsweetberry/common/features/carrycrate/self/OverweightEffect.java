//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.self;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import kurvcygnus.crispsweetberry.utils.MathUtils;
import kurvcygnus.crispsweetberry.utils.constants.DummyFunctionalConstants;
import kurvcygnus.crispsweetberry.utils.core.log.MarkLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateConstants.MAX_ACCEPTABLE_FACTOR;
import static kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateConstants.OVERWEIGHT_FACTOR;

/**
 * This is a simple implementation of alternative slowdown effect. Reusing <u>{@link net.minecraft.world.effect.MobEffects#MOVEMENT_SLOWDOWN Slowness}</u> 
 * will have a lot of conflict issues, and doesn't have a unique icon, so we made a new effect.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class OverweightEffect extends MobEffect
{
    public static final String OVERWEIGHT_KEY = "effect.overweight";
    public static final ResourceLocation OVERWEIGHT_ID = DefinitionUtils.getModNamespacedLocation(OVERWEIGHT_KEY);
    public static final Lazy<MobEffectInstance> EFFECT_INST = Lazy.of(
        () -> new MobEffectInstance(CarryCrateRegistries.OVERWEIGHT, MobEffectInstance.INFINITE_DURATION)
    );
    
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "OVERWEIGHT");
    
    private OverweightEffect()
    {
        super(
            MobEffectCategory.HARMFUL,
            FastColor.ARGB32.color(255, 121, 85, 72)//* Brown UwU
        );
    }
    
    @ApiStatus.Internal public static @NotNull MobEffect register(@NotNull DeferredRegister<MobEffect> deferredRegister)
    {
        Objects.requireNonNull(
            deferredRegister,
            "This static constructor is only used for registration, provide DeferredRegister<MobEffect>!"
        );
        
        FunctionalUtils.throwIf(
            !Objects.equals(deferredRegister.getNamespace(), CrispSweetberry.NAMESPACE),
            "External usage is not allowed!",
            IllegalArgumentException::new
        );
        
        return new OverweightEffect().addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            OVERWEIGHT_ID,
            -0.25F,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
    
    //? TODO: Check Factor, and try update when player changes gamemode.
    public static float updateFactorAndEffect(@NotNull Player player, @NotNull CarryData data, @NotNull TriState state)
        { return updateFactorAndEffect(player, data, state, DummyFunctionalConstants.DO_NOTHING_RUN); }
    
    public static float updateFactorAndEffect(@NotNull Player player, @NotNull CarryData data, @NotNull TriState state, @NotNull Runnable unacceptableCallback)
    {
        Objects.requireNonNull(player, "Param \"player\" must not be null!");
        Objects.requireNonNull(data, "Param \"data\" must not be null!");
        Objects.requireNonNull(state, "Param \"state\" must not be null!");
        Objects.requireNonNull(unacceptableCallback, "Param \"unacceptableCallback\" must not be null!");
        
        final float layerFactor = data.unionData() instanceof CarryData.CarryBlockDataHolder holder ?
            (float) 1 / holder.getMaxCarryCount() :
            1F;
        
        final float newFactor = state.isDefault() ?
            0F :
            Math.max(
                0F,
                player.getData(CarryCrateRegistries.CARRY_FACTOR.get()) +
                MathUtils.negativeIf(
                    state.isFalse(),
                    data.causesOverweight() ? 1F : 0.5F
                ) * layerFactor
            );
        
        LOGGER.debug("Evaluated new factor value {}.", newFactor);
        
        final boolean isInteractable = !(player.isCreative() || player.isSpectator());
        
        if(!isInteractable)
        {
            LOGGER.debug("Player \"{}\" can't interact with carry crate, skipped side effects.", player.getName());
            return newFactor;
        }
        
        if(newFactor >= OVERWEIGHT_FACTOR)
        {
            LOGGER.debug("Current factor's value has reached OVERWEIGHT_FACTOR, award player with debuff.");
            player.addEffect(EFFECT_INST.get());
        }
        else
        {
            LOGGER.debug("Current factor's value is smaller than OVERWEIGHT_FACTOR, remove player's debuff.");
            player.removeEffect(EFFECT_INST.get().getEffect());
        }
        
        if(newFactor > MAX_ACCEPTABLE_FACTOR)
        {
            LOGGER.debug("Current factor's value has reached MAX_ACCEPTABLE_FACTOR, execute sequence operation.");
            unacceptableCallback.run();
        }
        
        player.setData(CarryCrateRegistries.CARRY_FACTOR.get(), newFactor);
        return newFactor;
    }
}
