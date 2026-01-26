package kurvcygnus.crispsweetberry.common.misc.items;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

//? TODO: See implNote.

/**
 * This is the collection of all vanilla ores' coins.
 *
 * @author Kurv Cygnus
 * @implNote Among all the coins, <u>{@link CopperCoinItem CopperCoinItem}</u> and <u>{@link DiamondCoinItem DiamondCoinItem}</u>
 * will appear only when CSB detects the nugget variants of these ores exist in other mods.
 * @see kurvcygnus.crispsweetberry.common.misc.events.CoinExperienceEvent Actual Function
 * @since 1.0 Release
 */
public final class CoinCollections
{
    public abstract static class AbstractCoinItem extends Item
    {
        private final int storedExperience;
        
        @SuppressWarnings("unused")//! Only for vanilla CODEC stuff.
        private AbstractCoinItem(@Nullable Properties properties) { this(); }
        
        public AbstractCoinItem() 
        {
            super(new Properties());
            this.storedExperience = initStoredExperience();
        }
        
        protected abstract int initStoredExperience();
        
        public int getStoredExperience() { return storedExperience; }
    }
    
    public static final class CopperCoinItem extends AbstractCoinItem
    {
        @Override
        protected int initStoredExperience() { return 0; }
    }
    
    public static final class IronCoinItem extends AbstractCoinItem
    {
        @Override
        protected int initStoredExperience() { return 0; }
    }
    
    public static final class GoldCoinItem extends AbstractCoinItem
    {
        @Override
        protected int initStoredExperience() { return 0; }
    }
    
    public static final class DiamondCoinItem extends AbstractCoinItem
    {
        @Override
        protected int initStoredExperience() { return 0; }
    }
}
