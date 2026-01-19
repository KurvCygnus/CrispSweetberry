package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipe;
import kurvcygnus.crispsweetberry.utils.constants.MiscConstants;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;

import static com.mojang.math.Constants.EPSILON;

/**
 * This class makes sure that the balancing effect of kiln is visually acceptable.
 * @since CSB Release 1.0
 * @author Kurv
 * @see KilnBlockEntity#serverTick Usage 
 */
public final class KilnProgressCalculator
{
    private static final double NORMAL_PROGRESS_RATE = 0.005D;
    private static final double STANDARD_PROCESS_FACTOR = 1D;
    private static final int BALANCE_STATE_STANDARD_TICKS = 60;
    
    private KilnRecipe[] recipes = new KilnRecipe[3];
    private double lastProcessFactor = 0D;
    private boolean isBalancing = false;
    
    private boolean hasWarnedRecipeLengthMismatch = false;
    private boolean hasWarnedNullRecipe = false;
    private boolean hasWarnedAbnormalFactor = false;
    
    private static final Logger logger = LogUtils.getLogger();
    
    public KilnProgressCalculator() {}
    
    public void setRecipes(KilnRecipe[] recipes)
    {
        if(this.recipes.length != recipes.length)
        {
            if(!hasWarnedRecipeLengthMismatch)
            {
                logger.warn("Kiln recipes' length do NOT match, check the codes!");
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
                    logger.error("Denied new recipe for its invalidity. Null element start at -> 'Index {}'", index);
                    hasWarnedNullRecipe = true;
                }
                return;
            }
        }
        
        this.recipes = recipes;
    }
    
    @Contract("_, _, _ -> new")
    @CheckReturnValue
    public CalculationResult calculateRates(double currentRealProgress, double currentVisualProgress, KilnBlockEntity.ProcessionState processState)
    {
        final double currentProcessFactor;
        double multipliedFactor = STANDARD_PROCESS_FACTOR;
        double revaluateFactor = 0D;
        boolean canUseAverageReward = true;
        
        for(KilnRecipe recipe: recipes)
        {
            if(KilnRecipe.isEmptyRecipe(recipe) || KilnRecipe.isTipRecipe(recipe))
            {
                lastProcessFactor = STANDARD_PROCESS_FACTOR;
                isBalancing = false;
                return new CalculationResult(
                    currentRealProgress - NORMAL_PROGRESS_RATE,
                    currentVisualProgress - NORMAL_PROGRESS_RATE,
                    KilnRecipe.isEmptyRecipe(recipe) ? ResultType.SKIP : ResultType.BLAST_TIP,
                    ProgressTrend.DECREASE
                );//! This is the case that the container holds inputs that are invalid or unsupported, so we should treat it as COOLDOWN.
            }
            
            //! Explanation: processFactor smaller than STANDARD_PROCESS_FACTOR means it is a recipe that processes faster than normal
            //! process time, multiplying these values in such situation would lead to an imbalance.
            //! Therefore, taking the average value of three small factors is the best solution.
            //! Of course, this method will not be used if any of the values is greater than STANDARD_PROCESS_FACTOR.
            if(recipe.getProcessFactor() < STANDARD_PROCESS_FACTOR)
                revaluateFactor += recipe.getProcessFactor();
            else
                canUseAverageReward = false;
            
            multipliedFactor *= recipe.getProcessFactor();
        }
        
        currentProcessFactor = canUseAverageReward ? revaluateFactor / 3 : multipliedFactor;
        
        if(currentProcessFactor <= 0D)
        {
            if(!hasWarnedAbnormalFactor)
            {
                logger.warn("Calculation error! Returning the values of args as result. Reason: Variable \"currentProgressFactor\" happens to be " +
                    "a non-positive double number, which will cause calculation result abnormal. {}", MiscConstants.FEEDBACK_MESSAGE
                );
                hasWarnedAbnormalFactor = true;
            }
            
            return new CalculationResult(
                currentRealProgress,
                currentVisualProgress,
                ResultType.INVALID,
                ProgressTrend.NEUTRAL
            );
        }
        
        if(Double.compare(currentProcessFactor, lastProcessFactor) != 0)
            isBalancing = true;
        
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
                isBalancing = false;
                return new CalculationResult(
                    targetProgress,
                    targetProgress,
                    ResultType.CONTINUE,
                    ProgressTrend.NEUTRAL
                );
            }
            
            return new CalculationResult(
                targetProgress,//* targetProgress is also balancedRealProgress.
                balancedVisualProgress,
                ResultType.BALANCING,
                balanceDecayRate > EPSILON ? ProgressTrend.INCREASE : ProgressTrend.DECREASE
            );
        }
        
        double realChangeRate = NORMAL_PROGRESS_RATE / currentProcessFactor;
        double visualChangeRate;
        
        if(processState == KilnBlockEntity.ProcessionState.COOLDOWN)
            realChangeRate = -realChangeRate;
        
        visualChangeRate = realChangeRate;
        
        return new CalculationResult(
            realChangeRate + currentRealProgress,
            visualChangeRate + currentVisualProgress,
            processState != KilnBlockEntity.ProcessionState.COOLDOWN ? ResultType.CONTINUE : ResultType.SKIP,
            currentRealProgress > EPSILON ? ProgressTrend.INCREASE : ProgressTrend.DECREASE
        );
    }
}
