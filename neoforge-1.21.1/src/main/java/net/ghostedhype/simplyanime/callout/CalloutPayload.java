package net.ghostedhype.simplyanime.callout;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.ghostedhype.simplyanime.Constants;
import net.ghostedhype.simplyanime.client.hud.CalloutHud;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record CalloutPayload(Callout callout) implements CustomPacketPayload {
    public static final ResourceLocation PACKET = Constants.id("callout");
    public static final Type<CalloutPayload> TYPE = new Type<>(PACKET);
    public static final StreamCodec<FriendlyByteBuf, CalloutPayload> CODEC =
            StreamCodec.of((buf, payload) -> buf.writeEnum(payload.callout()), buf -> new CalloutPayload(buf.readEnum(Callout.class)));

    public static void register() {
        ConfigApiJava.network().registerS2C(TYPE, CODEC,
                (payload, context) -> context.execute(() -> CalloutHud.INSTANCE.show(payload.callout())));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
