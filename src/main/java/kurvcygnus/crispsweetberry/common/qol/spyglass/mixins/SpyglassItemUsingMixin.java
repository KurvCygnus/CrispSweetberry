package kurvcygnus.crispsweetberry.common.qol.spyglass.mixins;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.qol.spyglass.SpyglassClientRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.entity.LivingEntity.class)
public final class SpyglassItemUsingMixin
{
    @Shadow @Final private static final Logger LOGGER = LogUtils.getLogger();
    
    @Inject(method = {"isUsingItem"}, at = @At("RETURN"), cancellable = true)
    private void spyglass$forceIsUsingItem(@NotNull CallbackInfoReturnable<Boolean> cir)
    {
        final LivingEntity entity = (LivingEntity) (Object) this;
        
        if(!entity.level().isClientSide && SpyglassClientRegistries.SPYGLASS_ZOOM.isDown() && entity.getOffhandItem().is(Items.SPYGLASS))
            cir.setReturnValue(true);
    }
}
