//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln;

import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.VisualTrend;
import net.minecraft.world.inventory.ContainerData;

/**
 * The modified ContainerData for Kiln Block.<br>
 * <b>{@code ContainerData}</b> plays an important role in <b>Client-Server Sync</b>.
 *
 * @author Kurv Cygnus
 * @see net.minecraft.world.inventory.ContainerData Source
 * @see KilnBlockEntity Usage
 * @since 1.0 Release
 */
public final class KilnContainerData implements ContainerData
{
    public static final int VISUAL_PROGRESS_INDEX = 0;
    public static final int PROGRESS_TREND_INDEX = 1;
    public static final int IGNITION_STATE_INDEX = 2;
    
    public static final int TRUE = 0;
    public static final int FALSE = 1;
    
    private final KilnBlockEntity kiln;
    
    public KilnContainerData(KilnBlockEntity kiln) { this.kiln = kiln; }
    
    /**
     * <b>Returns</b> the value of <b>the field specified by the index</b>.
     * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity Variable Source
     */
    @Override
    public int get(int index)
    {
        return (int) switch(index)
        {
            case VISUAL_PROGRESS_INDEX -> kiln.model.getVisualProgress() * 10000;
            case PROGRESS_TREND_INDEX -> kiln.model.getProgressTrendIndex();
            case IGNITION_STATE_INDEX -> kiln.model.getIgnitionState();
            default -> throw new IllegalArgumentException("Illegal value for index: " + index);
        };
    }
    
    /**
     * <b>Sets</b> the value of <b>the field specified by the index</b>.
     * @apiNote For progress cases, make sure the value of {@code value} is 10000 times size of the actual value.
     */
    @Override
    public void set(int index, int value)
    {
        switch(index)
        {
            case VISUAL_PROGRESS_INDEX -> kiln.model.setVisualProgress((double) value / 10000);
            case PROGRESS_TREND_INDEX -> kiln.model.setProgressTrend(VisualTrend.values()[value]);
            case IGNITION_STATE_INDEX -> kiln.model.setIgnitionState(value);
            default -> throw new IllegalArgumentException("Illegal value for index: " + index);
        }
    }
    
    /**
     * Returns the <b>number of fields in this {@code ContainerData}</b>.<br>
     * <i>Hard-coded for performance reasons, probably.</i>
     */
    @Override
    public int getCount() { return 3; }
    
    public static double toStandardProgress(int progress) { return (double) progress / 10000; }
    
    public static boolean toStandardIgnitionState(int ignitionState) { return ignitionState == TRUE; }
}
