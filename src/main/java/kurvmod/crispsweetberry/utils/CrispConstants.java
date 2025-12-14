package kurvmod.crispsweetberry.utils;

public final class CrispConstants
{
    private CrispConstants() {}
    
    public static class SoundConstants
    {
        private SoundConstants() {}
        
        public static final float NORMAL_SOUND_VOLUME = 1.0F;
        public static final float LOUD_SOUND_VOLUME = 1.5F;
        public static final float QUIET_SOUND_VOLUME = 0.5F;
        public static final float NORMAL_SOUND_PITCH = 1.0F;
    }
    
    public static class ProjectileConstants
    {
        private ProjectileConstants() {}
        
        public static final float PROJECTILE_SHOOT_Z_POS = 0.0F;
        public static final double X_NO_SPEED = 0.0D;
        public static final double Y_NO_SPEED = 0.0D;
        public static final double Z_NO_SPEED = 0.0D;
    }
    
    @SuppressWarnings("unused")//* Despite some constants are unused, but they are essential for understanding Slot Indexes, so the warnings shall be suppressed.
    public static class SlotConstants
    {
        private SlotConstants() {}
        
        /**
         * This constant exists because <b>the source code of the method {@code quickMoveStack()} originally and oddly uses {@code '<'} and {@code '>='} to check indexes</b>,
         * thus we <b>need this to make constant's name fits the actual context meaning</b>.<br>
         * <i>BTW, it is also used for <b>correct {@code endIdx}</b> in method <b>{@code moveItemStackTo()}</b></i>.
         * @see net.minecraft.world.inventory.AbstractContainerMenu Source
         */
        public static final int CORRECTION_INDEX = 1;
        
        public static final int INPUT_SLOT = 0;
        
        public static final int FUEL_SLOT = 1;
        
        public static final int OUTPUT_SLOT = 2;
        public static final int BACKPACK_SLOT_START_INDEX = 3;
        public static final int BACKPACK_SLOT_END_INDEX = 29;
        public static final int BACKPACK_PLUS_BAR_SLOT_END_INDEX = 38;
        public static final int BAR_SLOT_START_INDEX = 30;
        public static final int BAR_SLOT_END_INDEX = 38;
        
        public static final int INPUT_SLOT_END_INDEX_EXCLUSIVE = INPUT_SLOT + CORRECTION_INDEX;
        public static final int BACKPACK_SLOT_END_INDEX_EXCLUSIVE = BACKPACK_SLOT_END_INDEX + CORRECTION_INDEX;
        public static final int BACKPACK_PLUS_BAR_SLOT_END_INDEX_EXCLUSIVE = BACKPACK_PLUS_BAR_SLOT_END_INDEX + CORRECTION_INDEX;
        public static final int BAR_SLOT_END_INDEX_EXCLUSIVE = BAR_SLOT_END_INDEX + CORRECTION_INDEX;
    }
}
