package kurvcygnus.crispsweetberry.common.features.kiln;

import kurvcygnus.crispsweetberry.utils.ui.collects.CrispIntRanger;

/**
 * This class mainly maintains slots contants of Kiln.
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
public final class KilnConstants
{
    private KilnConstants() { throw new IllegalAccessError(); }
    
    public static final int KILN_SLOT_COUNT_FOR_EACH_TYPE = 3;
    public static final int KILN_DEFAULT_SIZE = KILN_SLOT_COUNT_FOR_EACH_TYPE * 2;
    
    public static final int KILN_INPUT_START_INDEX = 0;
    public static final int KILN_INPUT_END_INDEX = 2;
    
    public static final int KILN_OUTPUT_START_INDEX = 3;
    public static final int KILN_OUTPUT_END_INDEX = 5;
    
    private static final int KILN_BACKPACK_START_INDEX = 6;
    private static final int KILN_BACKPACK_END_INDEX = 32;
    
    private static final int KILN_HOTBAR_START_INDEX = 33;
    private static final int KILN_HOTBAR_END_INDEX = 41;
    
    public static final CrispIntRanger KILN_INPUT_SLOTS_RANGE = CrispIntRanger.closed(KILN_INPUT_START_INDEX, KILN_INPUT_END_INDEX);
    public static final CrispIntRanger KILN_OUTPUT_SLOTS_RANGE = CrispIntRanger.closed(KILN_OUTPUT_START_INDEX, KILN_OUTPUT_END_INDEX);
    public static final CrispIntRanger KILN_BACKPACK_SLOTS_RANGE = CrispIntRanger.closed(KILN_BACKPACK_START_INDEX, KILN_BACKPACK_END_INDEX);
    public static final CrispIntRanger KILN_HOTBAR_SLOTS_RANGE = CrispIntRanger.closed(KILN_HOTBAR_START_INDEX, KILN_HOTBAR_END_INDEX);
    public static final CrispIntRanger KILN_INVENTORY_SLOTS_RANGE = CrispIntRanger.closed(KILN_BACKPACK_SLOTS_RANGE.getMin(), KILN_HOTBAR_SLOTS_RANGE.getMax());
}
