package net.ghostedhype.simplyanime.client.pose;

import net.ghostedhype.simplyanime.ability.RhittaGrowth;
import net.ghostedhype.simplyanime.config.RhittaClientConfig;
import net.ghostedhype.simplyanime.entity.CruelSunEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class RhittaPoses {

    // prev, current pose weight
    private static final Map<LivingEntity, float[]> WEIGHTS = new WeakHashMap<>();

    private static final float SHOULDER_PITCH = 1.65F;

    public static void tick(ClientLevel world) {
        for (Player player : world.players()) {
            float[] w = WEIGHTS.computeIfAbsent(player, p -> new float[2]);
            w[0] = w[1];
            float target = idle(player) ? 1.0F : 0.0F;
            // eases in slowly, gets out of the way fast for a swing
            w[1] = target > w[1] ? Math.min(target, w[1] + 0.1F) : Math.max(target, w[1] - 0.34F);
        }
    }

    private static boolean idle(Player player) {
        return RhittaClientConfig.get().idlePose && RhittaGrowth.holding(player) && !player.swinging
                && !player.isUsingItem() && !player.isSwimming() && !player.isFallFlying() && !player.isPassenger()
                && !player.isSleeping() && !player.isSpectator()
                && (player.getPose() == Pose.STANDING || player.getPose() == Pose.CROUCHING)
                && !CruelSunEntity.CLIENT_BY_OWNER.containsKey(player.getId());
    }

    public static float weight(LivingEntity entity, float partialTick) {
        float[] w = WEIGHTS.get(entity);
        if (w == null) {
            return 0.0F;
        }
        float k = Mth.lerp(partialTick, w[0], w[1]);
        return k * k * (3.0F - 2.0F * k);
    }

    public static void apply(LivingEntity entity, float animationProgress, HumanoidModel<?> model) {
        float partialTick = Mth.clamp(animationProgress - entity.tickCount, 0.0F, 1.0F);
        boolean left = entity.getMainArm() == HumanoidArm.LEFT;
        float s = left ? -1.0F : 1.0F;
        ModelPart main = left ? model.leftArm : model.rightArm;
        ModelPart off = left ? model.rightArm : model.leftArm;

        CruelSunEntity cast = CruelSunEntity.CLIENT_BY_OWNER.get(entity.getId());
        if (cast != null && !cast.isRemoved() && cast.level() == entity.level()) {
            castPose(cast, partialTick, main, off, s);
            model.hat.copyFrom(model.head);
            return;
        }

        float w = weight(entity, partialTick);
        if (w <= 0.001F) {
            return;
        }
        main.xRot = Mth.lerp(w, main.xRot, SHOULDER_PITCH);
        main.yRot = Mth.lerp(w, main.yRot, s * 1.125F);
        main.zRot = Mth.lerp(w, main.zRot, -s * 1.75F);
        // chest out, chin up
        model.body.xRot = Mth.lerp(w, model.body.xRot, model.body.xRot - 0.06F);
        model.head.xRot = Mth.lerp(w, model.head.xRot, model.head.xRot - 0.14F);
        model.hat.copyFrom(model.head);
    }

    private static void castPose(CruelSunEntity cast, float partialTick, ModelPart main, ModelPart off, float s) {
        float ticks = cast.getPhaseTicks(partialTick);
        if (cast.getPhase() == CruelSunEntity.PHASE_CHARGE) {
            // held straight up
            float e = Ease.out(Math.min(1.0F, ticks / 8.0F));
            float shake = Mth.sin(ticks * 2.1F) * 0.03F * cast.getPhaseProgress(partialTick);
            main.xRot = Mth.lerp(e, main.xRot, -3.05F) + shake;
            main.yRot = Mth.lerp(e, main.yRot, -s * 0.05F);
            main.zRot = Mth.lerp(e, main.zRot, s * 0.12F);
            off.xRot = Mth.lerp(e, off.xRot, -2.7F) - shake;
            off.yRot = Mth.lerp(e, off.yRot, s * 0.3F);
            off.zRot = Mth.lerp(e, off.zRot, -s * 0.1F);
        } else {
            // the throw
            float k = Math.min(1.0F, ticks / 4.0F);
            float back = Mth.clamp((ticks - 5.0F) / 5.0F, 0.0F, 1.0F);
            float pitch = Mth.lerp(Ease.out(k), -3.05F, -1.1F);
            main.xRot = Mth.lerp(back, pitch, main.xRot);
            main.yRot = Mth.lerp(back, -s * 0.1F, main.yRot);
            main.zRot = Mth.lerp(back, 0.0F, main.zRot);
            off.xRot = Mth.lerp(back, 0.4F, off.xRot);
            off.zRot = Mth.lerp(back, -s * 0.3F, off.zRot);
        }
    }
}
