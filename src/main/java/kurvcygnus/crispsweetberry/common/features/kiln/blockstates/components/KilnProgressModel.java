package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.VisualTrend;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;

import static kurvcygnus.crispsweetberry.common.features.kiln.data.KilnContainerData.FALSE;
import static kurvcygnus.crispsweetberry.common.features.kiln.data.KilnContainerData.TRUE;

/**
 * This class keeps the synchronization, and connections between blockEntity and menu.
 * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity BlockEntity
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class KilnProgressModel
{
    private double realProgress;
    private double visualProgress;
    private VisualTrend trend;
    private boolean isIgnited;
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public KilnProgressModel()
    {
        this.realProgress = 0D;
        this.visualProgress = 0D;
        this.trend = VisualTrend.TIP;
        this.isIgnited = true;
    }
    
    public void synchronize(double realProgress, double visualProgress, VisualTrend trend, boolean isIgnited)
    {
        this.realProgress = Math.clamp(realProgress, 0D, 1D);
        this.visualProgress = Math.clamp(visualProgress, 0D, 1D);
        this.trend = Objects.requireNonNullElse(trend, VisualTrend.TIP);
        this.isIgnited = isIgnited;
    }
    
    @CheckReturnValue
    public static boolean upgradeProgress(@NotNull KilnProgressModel model)
    {
        if(model.realProgress >= 1D)
        {
            if(model.visualProgress < 1D)
                LOGGER.warn("Kiln Model encountered a abnormal situation. Detail: visualProgress does not match realProgress when procession is " +
                        "already done. visualProgress: {} \n{}",
                    model.visualProgress,
                    MiscConstants.FEEDBACK_MESSAGE
                );
            
            model.realProgress = 0D;
            model.visualProgress = 0D;
            
            return true;
        }
        
        return false;
    }
    
    public double getRealProgress() { return realProgress; }
    
    public double getVisualProgress() { return visualProgress; }
    
    public int getProgressTrendIndex() { return trend.ordinal(); }
    
    public int getIgnitionState() { return isIgnited ? TRUE : FALSE; }
    
    public void setVisualProgress(double visualProgress) { this.visualProgress = visualProgress; }
    
    public void setProgressTrend(VisualTrend trend) { this.trend = trend; }
    
    public void setIgnitionState(int state) { this.isIgnited = state == TRUE; }
}