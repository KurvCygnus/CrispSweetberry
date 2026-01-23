package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.ProcessionState;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.ProgressTrend;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.ResultType;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipe;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Arrays;

import static com.mojang.math.Constants.EPSILON;
import static kurvcygnus.crispsweetberry.common.features.kiln.KilnConstants.KILN_SLOT_COUNT_FOR_EACH_TYPE;

/**
 * This class makes sure that the balancing effect of kiln is visually acceptable.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see KilnBlockEntity#serverTick Usage 
 */
public final class KilnProgressCalculator
{
    private static final double NORMAL_PROGRESS_RATE = 0.005D;
    private static final double STANDARD_PROCESS_FACTOR = 1D;
    private static final int BALANCE_STATE_STANDARD_TICKS = 60;
    
    private @NotNull KilnRecipe[] recipes = { KilnRecipe.noRecipe(), KilnRecipe.noRecipe(), KilnRecipe.noRecipe() };
    
    /**
     * We should use <u>{@link Double}</u> instead of primitive type {@code double}, 
     * when it comes to cases like empty content, putting stuff inside should go to 
     * {@code WORKING} variant, not {@code BALANCING}.</u>
     */
    private @Nullable Double lastProcessFactor = null;
    private boolean isBalancing = false;
    
    /**
     * This field is used for return early in <u>{@link #calculateRates calculateRates()}</u>.<br>
     * It's named "predicted" since the standard procession is uncertain, due to BALANCE stuff.
     */
    private @NotNull ResultType predictedResultType = ResultType.SKIP;
    
    private boolean hasWarnedRecipeLengthMismatch = false;
    private boolean hasWarnedNullRecipe = false;
    private boolean hasWarnedAbnormalFactor = false;
    
    private static final Logger logger = LogUtils.getLogger();
    
    public KilnProgressCalculator() {}
    
    public void setRecipesAndResultType(KilnRecipe @NotNull [] recipes, @NotNull ResultType resultType)
    {
        if(this.recipes.length != recipes.length)
        {
            if(!hasWarnedRecipeLengthMismatch)
            {
                logger.warn("[ABNORMAL_RECIPES] Kiln recipes' length do NOT match, go check codes!");
                hasWarnedRecipeLengthMismatch = true;
            }
            return;
        }
        
        for(int index = 0; index < recipes.length; index++)
        {
            if(recipes[index] == null)
            {
                if(!hasWarnedNullRecipe)
                {
                    logger.error("[ABNORMAL_RECIPES] Denied new recipe for its invalidity. Null element start at -> 'Index {}'", index);
                    hasWarnedNullRecipe = true;
                }
                return;
            }
        }
        
        this.recipes = recipes;
        this.predictedResultType = resultType;
    }
    
    @Contract("_, _, _ -> new")
    @CheckReturnValue
    public @NotNull CalculationResult calculateRates(double currentRealProgress, double currentVisualProgress, @NotNull ProcessionState processState)
    {
        if(processState != ProcessionState.WORKING)
        {
            lastProcessFactor = null;
            isBalancing = false;
            
            logger.debug("[CAL_END] Current resultType \"{}\" doesn't need any further calculation. Returning decreased progresses as result.",
                predictedResultType.name()
            );
            
            return new CalculationResult(
                currentRealProgress - NORMAL_PROGRESS_RATE,
                currentVisualProgress - NORMAL_PROGRESS_RATE,
                predictedResultType,
                ProgressTrend.DECREASE
            );//! This is the case that the container holds inputs that are invalid or unsupported, so we should treat it as COOLDOWN.
        }
        
        logger.debug("[CAL_CHECK] recipes = {}, lastFactor = {}, isBalancing = {}, processState = {}",
            Arrays.toString(recipes), lastProcessFactor, isBalancing, processState.name());
        
        final double currentProcessFactor;
        double multipliedFactor = STANDARD_PROCESS_FACTOR;
        double revaluateFactor = 0D;
        boolean canUseAverageReward = true;
        
        for(KilnRecipe recipe: recipes)
        {
            logger.debug("[FACTOR_CAL] Factor of recipe({}): {}",
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
                multipliedFactor *= recipe.getProcessFactor();
        }
        
        currentProcessFactor = canUseAverageReward ? revaluateFactor / KILN_SLOT_COUNT_FOR_EACH_TYPE : multipliedFactor;
        
        logger.debug("[STRATEGY_SELECT] strategy = \"{}\"",
            canUseAverageReward ? "Average" : "Multiply");
        
        if(currentProcessFactor <= 0D)
        {
            isBalancing = false;
            lastProcessFactor = STANDARD_PROCESS_FACTOR;
            
            if(!hasWarnedAbnormalFactor)
            {
                logger.warn("[CAL_ERROR] Calculation error! Returning the values of args as result. Reason: Variable \"currentProgressFactor\" happens to be " +
                    "a non-positive double number, which will cause calculation result abnormal. {}", MiscConstants.FEEDBACK_MESSAGE
                );
                hasWarnedAbnormalFactor = true;
            }
            
            logger.debug("[CAL_ERROR] Keeping progress unchanged to prevent unexpected behavior.");
            
            return CalculationResult.unexpectedResult(currentRealProgress, currentVisualProgress);
        }
        
        if(lastProcessFactor != null)
            if(Double.compare(currentProcessFactor, lastProcessFactor) != 0)
            {
                logger.debug("[CAL_BALANCE] ProgressFactor mismatch. Start balancing.");
                isBalancing = true;
            }
        
        this.lastProcessFactor = currentProcessFactor;
        
        if(isBalancing)
        {
            final double targetProgress = currentRealProgress / currentProcessFactor;
            
            //! You may think: Why use BALANCE_STATE_STANDARD_TICKS instead of the halved value of this constant(30), right?
            //! However, the fact is, since both real and visual progresses are moving, slicing rate into 60 pieces
            //! is actually averaged for both real and visual progress, so it's ok.
            final double visualPercentage = 1D / BALANCE_STATE_STANDARD_TICKS;
            final double balanceDecayRate = (targetProgress - currentVisualProgress) * visualPercentage;
            final double balancedVisualProgress = currentVisualProgress + balanceDecayRate;
            
            if(Math.abs(targetProgress - currentVisualProgress) < EPSILON)//! BALANCING completes when visualProgress converges to targetProgress.
            {
                logger.debug("[CAL_BALANCE] Dual Progress are approximately equal, balance ended.");
                
                isBalancing = false;
                return new CalculationResult(
                    targetProgress,
                    targetProgress,
                    ResultType.CONTINUE,
                    ProgressTrend.NEUTRAL
                );
            }
            
            logger.debug("[CAL_BALANCE] RealProgress adjusted to {}, Visual: {}, rate: {}",
                targetProgress, balancedVisualProgress, balanceDecayRate);
            
            return new CalculationResult(
                targetProgress,//* targetProgress is also balancedRealProgress.
                balancedVisualProgress,
                ResultType.BALANCING,
                balanceDecayRate > EPSILON ? ProgressTrend.INCREASE : ProgressTrend.DECREASE
            );
        }
        
        final double realChangeRate = NORMAL_PROGRESS_RATE / currentProcessFactor;
        
        @SuppressWarnings("UnnecessaryLocalVariable")//! JIT will opt this, and it's semantically necessary.
        final double visualChangeRate = realChangeRate;
        
        logger.debug("[CAL_NORMAL] Rate: {}", realChangeRate);
        
        return new CalculationResult(
            realChangeRate + currentRealProgress,
            visualChangeRate + currentVisualProgress,
            ResultType.CONTINUE,
            ProgressTrend.INCREASE
        );
    }
}
