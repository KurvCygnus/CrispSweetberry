package kurvmod.crispsweetberry.items.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TransmogWandItem extends Item
{
    private final Map<Block, Block> TRANSMOG_MAP = Map.of(
        Blocks.STONE, Blocks.DIORITE,
        Blocks.DIORITE, Blocks.STONE
    );
    
    public TransmogWandItem(Properties properties) { super(properties); }
    
    public @NotNull InteractionResult useOn(UseOnContext context)
    {
        Level world = context.getLevel();
        Block interaction =  world.getBlockState(context.getClickedPos()).getBlock();
        
        if(TRANSMOG_MAP.containsKey(interaction))
        {
            if(!world.isClientSide())
            {
                world.setBlockAndUpdate(context.getClickedPos(), TRANSMOG_MAP.get(interaction).defaultBlockState());
                context.getItemInHand().hurtAndBreak(1, ((ServerLevel) world), context.getPlayer(), wand ->
                    context.getPlayer().onEquippedItemBroken(wand, EquipmentSlot.MAINHAND));
                
                world.playSound(null, context.getClickedPos(), SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS);
            }
        }
        
        return InteractionResult.SUCCESS;
    }
}
