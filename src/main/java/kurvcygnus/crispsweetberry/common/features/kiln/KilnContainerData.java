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
    
    public KilnContainerData(KilnBlockEntity blockEntity) { this.kiln = blockEntity; }
    
    /**
     * <b>Returns</b> the value of <b>the field specified by the index</b>.
     *
     * @param p_58431_ The field index<i>(obfuscated vanilla variable, likely <b></i>{@code dataIndex}</b><i>)</i>.
     * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity Variable Source
     */
    @Override
    public int get(int p_58431_)
    {
        return (int) switch(p_58431_)
        {
            case VISUAL_PROGRESS_INDEX -> kiln.model.getVisualProgress() * 10000;
            case PROGRESS_TREND_INDEX -> kiln.model.getProgressTrendIndex();
            case IGNITION_STATE_INDEX -> kiln.model.getIgnitionState();
            default -> throw new IllegalArgumentException("Illegal value for p_58431_: " + p_58431_);
        };
    }
    
    /**
     * <b>Sets</b> the value of <b>the field specified by the index</b>.
     *
     * @param p_58433_ The field index<i>(likely <b></i>{@code dataIndex}<i></b>)</i>
     * @param p_58434_ The value to set
     * @apiNote For progress cases, make sure the value of {@code p_58434_} is 10000 times size of the actual value.
     */
    @Override
    public void set(int p_58433_, int p_58434_)
    {
        switch(p_58433_)
        {
            case VISUAL_PROGRESS_INDEX -> kiln.model.setVisualProgress((double) p_58434_ / 10000);
            case PROGRESS_TREND_INDEX -> kiln.model.setProgressTrend(VisualTrend.toEnum(p_58434_));
            case IGNITION_STATE_INDEX -> kiln.model.setIgnitionState(p_58434_);
            default -> throw new IllegalArgumentException("Illegal value for p_58433_: " + p_58433_);
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
