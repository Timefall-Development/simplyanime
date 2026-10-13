package net.hussain.simplyanime.callout;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.networking.FzzyPayload;
import net.hussain.simplyanime.Constants;
import net.hussain.simplyanime.client.hud.CalloutHud;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record CalloutPayload(Callout callout) implements FzzyPayload {
    public static final ResourceLocation PACKET = Constants.id("callout");

    public CalloutPayload(FriendlyByteBuf buf) {
        this(buf.readEnum(Callout.class));
    }

    public static void register() {
        ConfigApiJava.network().registerS2C(PACKET, CalloutPayload.class, CalloutPayload::new,
                (payload, context) -> context.execute(() -> CalloutHud.INSTANCE.show(payload.callout())));
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buf) {
        buf.writeEnum(callout);
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return PACKET;
    }
}
