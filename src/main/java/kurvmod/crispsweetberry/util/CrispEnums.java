package kurvmod.crispsweetberry.util;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class CrispEnums
{
    private CrispEnums() {}
    
    private static final int PROPERTY_INDEX_TO_NEXT_STD = 1;
    private static final int BRIGHTNESS_CONVERSION_CONSTANT = 4;//4 brightness per stage.
    
    /**
     * The <b>enum property</b> that controls the <b>life cycle and brightness</b> of temporary torches.
     */
    public enum LIGHT_STATE implements StringRepresentable
    {
        DARK, DIM, BRIGHT, FULL_BRIGHT;//DARK coordinates burned out, but to put the state name more simple,
        //I deprecated the later name.
        
        /**
         * The <b>formula</b> to <b>convert enum to actual brightness value</b>.<br><br>
         * <b>e.g.</b><br>
         * <b>FULL_BRIGHT</b> ->
         * <b>3</b>(FULL_BRIGHT.ordinal()) <b>× 4</b>(BRIGHTNESS_CONVERSION_CONSTANT) <b> = 12</b>
         */
        public int toBrightness() { return this.ordinal() * BRIGHTNESS_CONVERSION_CONSTANT; }
        
        public LIGHT_STATE getNextState()
        {
            //Do boundary check at the same time.
            return this.ordinal() - PROPERTY_INDEX_TO_NEXT_STD > LIGHT_STATE.DARK.ordinal() ?
                LIGHT_STATE.values()[this.ordinal() - PROPERTY_INDEX_TO_NEXT_STD] : LIGHT_STATE.DARK;
        }
        
        /**
         * The <b>essential method</b> for <b>registering the state names correctly</b>.
         * @return The names of <b>corresponded states</b>.
         */
        @Override
        public @NotNull String getSerializedName() {return this.name().toLowerCase();}
    }
}
