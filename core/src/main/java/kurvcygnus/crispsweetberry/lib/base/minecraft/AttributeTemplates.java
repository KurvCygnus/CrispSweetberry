//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.minecraft;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static kurvcygnus.crispsweetberry.lib.base.minecraft.BuilderSpecMonoid.Instance;
import static kurvcygnus.crispsweetberry.lib.base.minecraft.BuilderSpecMonoid.OF_BLOCK_PROPERTY;

//? TODO: Need MORE templates.

/**
 * A collection of composable pre-defined attributes, using <u>{@link Instance}</u>.
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
public final class AttributeTemplates
{
    private AttributeTemplates() { throw new IllegalAccessError("Class \"AttributeTemplates\" is not meant to be instantized!"); }
    
    public static final class BlockProperties
    {
        private BlockProperties() { throw new IllegalAccessError("Class \"BlockProperties\" is not meant to be instantized!"); }
        
        public static final Instance<BlockBehaviour.Properties> DECORATE_LIKE = OF_BLOCK_PROPERTY.instantize(
            properties -> properties.
                pushReaction(PushReaction.DESTROY).
                instabreak().
                noCollission()
        );
        
        public static final Instance<BlockBehaviour.Properties> FRAGILE_DECORATE = DECORATE_LIKE.compose(
            properties -> properties.
                requiresCorrectToolForDrops().
                noLootTable().
                replaceable()
        );
        
        public static final Instance<BlockBehaviour.Properties> STONE = OF_BLOCK_PROPERTY.instantize(
            properties -> properties.
                requiresCorrectToolForDrops().
                strength(1.5F, 6F).
                mapColor(MapColor.STONE).
                instrument(NoteBlockInstrument.BASEDRUM)
        );
        
        public static final Instance<BlockBehaviour.Properties> DEEPSLATE = STONE.compose(
            properties -> properties.
                strength(3F, 6F).
                sound(SoundType.DEEPSLATE)
        );
        
        public static final Function<MapColor, BlockBehaviour.Properties> MAP_COLOR_EDITABLE_STONE = STONE.makeInputFactory(BlockBehaviour.Properties::mapColor);
        public static final Function<Float, BlockBehaviour.Properties> UNIQUE_STRENGTH_STONE = STONE.makeInputFactory((p, i) -> p.strength(i));
        
        private static final Instance<BlockBehaviour.Properties> PLANK = OF_BLOCK_PROPERTY.instantize(
            properties -> properties.
                instrument(NoteBlockInstrument.BASS).
                strength(2F, 3F).
                sound(SoundType.WOOD).
                ignitedByLava()
        );
        
        public static final Function<MapColor, BlockBehaviour.Properties> STD_PLANK_FACTORY = PLANK.makeInputFactory(BlockBehaviour.Properties::mapColor);
        public static final BiFunction<MapColor, SoundType, BlockBehaviour.Properties> UNIQUE_SOUNDED_PLANK =
            PLANK.makeInputFactory(BlockBehaviour.Properties::mapColor, BlockBehaviour.Properties::sound);
        
        public static final Instance<BlockBehaviour.Properties> SAPLING = OF_BLOCK_PROPERTY.
            instantize(
                properties -> properties.
                    mapColor(MapColor.PLANT).
                    noCollission().
                    randomTicks().
                    instabreak().
                    sound(SoundType.GRASS).
                    pushReaction(PushReaction.DESTROY)
            );
        
        public static final Function<SoundType, BlockBehaviour.Properties> UNIQUE_SOUNDED_SAPLING = SAPLING.makeInputFactory(BlockBehaviour.Properties::sound);
        
        public static final Instance<BlockBehaviour.Properties> BEDROCK_LIKE = STONE.compose(
            properties -> properties.
                strength(-1F, 3600000F).
                noLootTable().
                isValidSpawn(Blocks::never)
        );
        
        private static final Instance<BlockBehaviour.Properties> BASE_FLUID = OF_BLOCK_PROPERTY.instantize(
            properties -> properties.
                replaceable().
                noCollission().
                strength(100F).
                pushReaction(PushReaction.DESTROY).
                noLootTable().
                liquid().
                sound(SoundType.EMPTY)
        );
        
        public static final Function<MapColor, BlockBehaviour.Properties> FLUID_CONFIGURER = BASE_FLUID.makeInputFactory(BlockBehaviour.Properties::mapColor);
        public static final BiFunction<MapColor, ToIntFunction<BlockState>, BlockBehaviour.Properties> LUMINOUS_FLUID =
            BASE_FLUID.makeInputFactory(BlockBehaviour.Properties::mapColor, BlockBehaviour.Properties::lightLevel);
        
        public static final BiFunction<MapColor, MapColor, BlockBehaviour.Properties> LOG = PLANK.compose(properties -> properties.strength(2F)).
            makeInputFactory(
                (top, side, prop) ->
                    prop.mapColor(bs -> bs.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? top : side)
            );
    }
}