package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.config.CrispConfig;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.LogicalResult;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.VisualTrend;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipe;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import org.jetbrains.annotations.*;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class makes sure that the balancing effect of kiln is visually acceptable.
 *
 * @author Kurv Cygnus
 * @see KilnBlockEntity#serverTick Usage
 * @since 1.0 Release
 */
@ApiStatus.Internal
public final class KilnProgressCalculator
{
    private static final double NORMAL_PROGRESS_RATE = 0.005D;
    private static final double STANDARD_PROCESS_FACTOR = 1D;
    private static final int BALANCE_STATE_STANDARD_TICKS = 40;
    
    private @NotNull KilnRecipe[] recipes = {KilnRecipe.noRecipe(), KilnRecipe.noRecipe(), KilnRecipe.noRecipe()};
    
    /**
     * We should use <u>{@link Double}</u> instead of primitive type {@code double},
     * when it comes to cases like empty content, putting stuff inside should go to
     * {@code WORKING} variant, not {@code BALANCING}.</u>
     */
    private @Nullable Double lastProcessFactor = null;
    private byte balanceTick = 0;
    private double balanceRate = 0D;
    private VisualTrend balanceTrend = VisualTrend.NORMAL;
    
    /**
     * This field is used for return early in <u>{@link #calculateRates calculateRates()}</u>.<br>
     * It's named "nonWorking" since the standard procession is uncertain, due to BALANCE stuff.<br>
     * It is only reliable when <u>{@link KilnBlockEntity.ProcessionState}</u> is <u>{@link KilnBlockEntity.ProcessionState#COOLDOWN COOLDOWN}</u>.
     */
    private @NotNull LogicalResult nonWorkingLogicalResult = LogicalResult.SKIP;
    
    private boolean hasWarnedRecipeLengthMismatch = false;
    private boolean hasWarnedNullRecipe = false;
    private boolean hasWarnedAbnormalFactor = false;
    
    private static final MarkLogger LOGGER = MarkLogger.marklessLogger(LogUtils.getLogger());
    
    public void setRecipesAndResultType(KilnRecipe @NotNull [] recipes, @NotNull LogicalResult logicalResult)
    {
        try(MarkLogger.MarkerHandle ignored = LOGGER.pushMarker("ABNORMAL_RECIPES"))
        {
            if(this.recipes.length != recipes.length)
            {
                if(!hasWarnedRecipeLengthMismatch || CrispConfig.KILN_BE_CAL_DEBUG.get())
                {
                    LOGGER.warn("Kiln recipes' length do NOT match, go check codes!");
                    
                    hasWarnedRecipeLengthMismatch = true;
                }
                return;
            }
            
            for(int index = 0; index < recipes.length; index++)
            {
                if(recipes[index] == null)
                {
                    if(!hasWarnedNullRecipe || CrispConfig.KILN_BE_CAL_DEBUG.get())
                    {
                        LOGGER.error("Denied new recipe for its invalidity. Null element start at -> 'Index {}'", index);
                        hasWarnedNullRecipe = true;
                    }
                    return;
                }
            }
        }
        
        this.recipes = recipes;
        this.nonWorkingLogicalResult = logicalResult;
    }
    
    public void synchronize(byte balanceTick, double balanceRate)
    {
        this.balanceTick = (byte) Math.clamp(balanceTick, 0, BALANCE_STATE_STANDARD_TICKS);
        this.balanceRate = balanceRate;
    }
    
    @Contract("_, _, _ -> new")
    @CheckReturnValue
    public @NotNull CalculationResult calculateRates(double currentRealProgress, double currentVisualProgress, @NotNull KilnBlockEntity.ProcessionState processState)
    {
        try(MarkLogger.MarkerHandle handle = LOGGER.pushMarker("CALCULATION"))
        {
            if(processState != KilnBlockEntity.ProcessionState.WORKING)
            {
                this.lastProcessFactor = null;
                this.balanceTick = 0;
                this.balanceRate = 0D;
                
                //* Defensive measures are taken at KilnProgressModel#synchronize().
                final double decreasedRealProgress = currentRealProgress - NORMAL_PROGRESS_RATE;
                final double decreasedVisualProgress = currentVisualProgress - NORMAL_PROGRESS_RATE;
                
                handle.changeMarker("CAL_END");
                configDebug("Current logicalResult \"{}\" doesn't need any further calculation. Progress values: R: {}, V: {}",
                    nonWorkingLogicalResult.name(), decreasedRealProgress, decreasedVisualProgress
                );
                
                return new CalculationResult(
                    decreasedRealProgress,
                    decreasedVisualProgress,
                    nonWorkingLogicalResult,
                    VisualTrend.NORMAL
                );//! This is the case that the container holds inputs that are invalid or unsupported, so we should treat it as COOLDOWN.
            }
            
            final byte remainingTicks = (byte) (BALANCE_STATE_STANDARD_TICKS - this.balanceTick);
            
            handle.changeMarker("CAL_CHECK");
            configDebug("recipes = {}, lastFactor = {}, processState = {}{}",
                Arrays.toString(this.recipes), this.lastProcessFactor, processState.name(),
                this.balanceTick > 0 ? ", %d balance tick%s remain%s".formatted
                    (remainingTicks, remainingTicks == 1 ? "s" : "", remainingTicks == 1 ? "" : "s") : ""
            );
            
            final double currentProcessFactor;
            double multipliedFactor = STANDARD_PROCESS_FACTOR;
            double revaluateFactor = 0D;
            boolean canUseAverageReward = true;
            byte nonEmptyCount = 0;
            
            handle.changeMarker("FACTOR_CAL");
            for(final KilnRecipe recipe: recipes)
            {
                configDebug("Factor of recipe({}): {}",
                    recipe, recipe.getProcessFactor()
                );
                
                //! Explanation: processFactor smaller than STANDARD_PROCESS_FACTOR means it is a recipe that processes faster than normal
                //! process time, multiplying these values in such situation would lead to an imbalance.
                //! Therefore, taking the average value of three small factors is the best solution.
                //! Of course, this method will not be used if any of the values is greater than STANDARD_PROCESS_FACTOR.
                if(recipe.getProcessFactor() <= STANDARD_PROCESS_FACTOR)
                    revaluateFactor += recipe.getProcessFactor();
                else
                    canUseAverageReward = false;
                
                if(!KilnRecipe.isEmptyRecipe(recipe))
                {
                    nonEmptyCount++;
                    multipliedFactor *= recipe.getProcessFactor();
                }
            }
            
            currentProcessFactor = canUseAverageReward ? (revaluateFactor / nonEmptyCount) : multipliedFactor;
            
            handle.changeMarker("STRATEGY_SELECT");
            configDebug("strategy = \"{}\", totalFactor = {}{}",
                canUseAverageReward ? "Average" : "Multiply", currentProcessFactor,
                canUseAverageReward ? ", non-empty recipes: %d".formatted(nonEmptyCount) : ""
            );
            
            if(currentProcessFactor <= 0D)
            {
                this.balanceTick = 0;
                this.balanceRate = 0D;
                this.lastProcessFactor = STANDARD_PROCESS_FACTOR;
                
                handle.changeMarker("CAL_ERROR");
                if(!hasWarnedAbnormalFactor || CrispConfig.KILN_BE_CAL_DEBUG.get())
                {
                    LOGGER.warn("Calculation error! Returning the values of args as result. Reason: Variable \"currentProgressFactor\" happens to be " +
                        "a non-positive double number, which will cause calculation result abnormal. {}", MiscConstants.FEEDBACK_MESSAGE
                    );
                    hasWarnedAbnormalFactor = true;
                }
                
                configDebug("Keeping progress unchanged to prevent unexpected behavior.");
                
                return CalculationResult.unexpectedResult(currentRealProgress, currentVisualProgress);
            }
            
            final boolean shouldBalance;
            
            if(this.lastProcessFactor != null)
                shouldBalance = Double.compare(this.lastProcessFactor, currentProcessFactor) != 0 && currentRealProgress > 0D;
            else
                shouldBalance = false;
            
            if(shouldBalance)
                this.balanceTrend = currentProcessFactor > this.lastProcessFactor ? VisualTrend.BALANCE : VisualTrend.BURST;
            
            if(Objects.requireNonNullElse(this.lastProcessFactor, -1D) != currentProcessFactor)
            {
                handle.changeMarker("CAL_DATA_INFO");
                configDebug("Factors: C: {}, L: {}", currentProcessFactor, Objects.requireNonNullElse(this.lastProcessFactor, "N/A"));
            }
            
            this.lastProcessFactor = currentProcessFactor;
            
            final double realChangeRate = NORMAL_PROGRESS_RATE / currentProcessFactor;
            
            if(shouldBalance)
            {
                handle.changeMarker("CAL_BALANCE");
                configDebug("ProgressFactor mismatch. Start calculate balance factors.");
                
                currentRealProgress = currentRealProgress * (this.lastProcessFactor / currentProcessFactor);
                this.balanceRate = (currentRealProgress + realChangeRate * BALANCE_STATE_STANDARD_TICKS - currentVisualProgress) / BALANCE_STATE_STANDARD_TICKS;
                currentVisualProgress += this.balanceRate;
                
                this.balanceTick = BALANCE_STATE_STANDARD_TICKS - 1;//* The calculation tick also counts as a tick of whole balance state.
                
                handle.changeMarker("CAL_BALANCE_END");
                configDebug("Balance calculation ended.");
                
                return new CalculationResult(
                    currentRealProgress,
                    currentVisualProgress,
                    LogicalResult.BALANCING,
                    this.balanceTrend
                );
            }
            
            if(this.balanceTick > 0)
            {
                handle.changeMarker("CAL_BALANCE");
                configDebug("balanceTick({}) is bigger than 0. Continue to calculate visualProgress.", balanceTick);
                
                this.balanceTick--;
                
                configDebug("Current VisualTrend: {}", this.balanceTrend);
                
                return new CalculationResult(
                    currentRealProgress + realChangeRate,
                    currentVisualProgress + this.balanceRate,
                    LogicalResult.BALANCING,
                    this.balanceTrend
                );
            }
            
            @SuppressWarnings("UnnecessaryLocalVariable")//! JIT will opt this, and it's semantically necessary.
            final double visualChangeRate = realChangeRate;
            
            final double newRealProgress = currentRealProgress + realChangeRate;
            final double newVisualProgress = currentVisualProgress + visualChangeRate;
            
            if(Math.abs(newRealProgress - newVisualProgress) > 0.02D)
            {
                handle.changeMarker("CAL_NORMAL_ERROR");
                LOGGER.when(CrispConfig.KILN_BE_CAL_DEBUG.get()).error(
                    "ProgressPair doesn't match. R: {}, V: {}",
                    newRealProgress, newVisualProgress
                );
            }
            
            handle.changeMarker("CAL_NORMAL");
            configDebug("Rate: {}, progressPairValue: R: {}, V: {}",
                realChangeRate, newRealProgress, newVisualProgress
            );
            
            return new CalculationResult(
                newRealProgress,
                newVisualProgress,
                LogicalResult.CONTINUE,
                VisualTrend.NORMAL
            );
        }
    }
    
    public byte getBalanceTick() { return balanceTick; }
    
    public double getBalanceRate() { return balanceRate; }
    
    private void configDebug(String message, Object @NotNull ... args) { LOGGER.when(CrispConfig.KILN_BE_CAL_DEBUG.get()).debug(message, args); }
}
