package kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync;

import io.netty.buffer.ByteBuf;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record SpyglassPayload(boolean isPressed) implements CustomPacketPayload
{
    public static final Type<SpyglassPayload> TYPE = new Type<>(CrispDefUtils.getModNamespacedLocation("qol/spyglass/packet"));
    
    public static final StreamCodec<ByteBuf, SpyglassPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        SpyglassPayload::isPressed,
        SpyglassPayload::new
    );
    
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}