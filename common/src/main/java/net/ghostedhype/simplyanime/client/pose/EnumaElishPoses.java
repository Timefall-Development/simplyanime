package net.ghostedhype.simplyanime.client.pose;

import net.ghostedhype.simplyanime.entity.EnumaElishVisualEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class EnumaElishPoses {

    private static final float RAISE_TICKS = 8.0F;

    public static void apply(LivingEntity entity, float animationProgress, HumanoidModel<?> model) {
        EnumaElishVisualEntity cast = EnumaElishVisualEntity.CLIENT_BY_OWNER.get(entity.getId());
        if (cast == null || cast.isRemoved() || cast.level() != entity.level()) {
            return;
        }
        float partialTick = Mth.clamp(animationProgress - entity.tickCount, 0.0F, 1.0F);
        float progress = cast.getPhaseProgress(partialTick);
        float ticks = cast.getPhaseTicks(partialTick);

        boolean left = entity.getMainArm() == HumanoidArm.LEFT;
        float sideSign = left ? -1.0F : 1.0F;
        ModelPart main = left ? model.leftArm : model.rightArm;
        ModelPart off = left ? model.rightArm : model.leftArm;
        ModelPart frontLeg = left ? model.rightLeg : model.leftLeg;
        ModelPart backLeg = left ? model.leftLeg : model.rightLeg;
        float thrust = model.head.xRot - 0.3F;

        switch (cast.getPhase()) {
            case EnumaElishVisualEntity.PHASE_CHARGE -> {
                float eased = Ease.out(Math.min(1.0F, ticks / RAISE_TICKS));
                // the arms start shaking as the charge fills up
                float shake = Mth.sin(ticks * 2.3F) * (0.015F + 0.05F * progress);
                main.xRot = Mth.lerp(eased, main.xRot, EnumaElishVisualEntity.RAISED) + shake;
                main.yRot = Mth.lerp(eased, main.yRot, -sideSign * 0.1F);
                main.zRot = Mth.lerp(eased, main.zRot, sideSign * 0.05F);
                off.xRot = Mth.lerp(eased, off.xRot, EnumaElishVisualEntity.RAISED + 0.15F) - shake;
                off.yRot = Mth.lerp(eased, off.yRot, sideSign * 0.45F);
                off.zRot = Mth.lerp(eased, off.zRot, 0.0F);
                model.rightLeg.zRot = 0.1F * eased;
                model.leftLeg.zRot = -0.1F * eased;
            }
            case EnumaElishVisualEntity.PHASE_RELEASE -> {
                float eased = Ease.out(progress);
                thrustPose(model, main, off, frontLeg, backLeg, sideSign, eased, Mth.lerp(eased, EnumaElishVisualEntity.RAISED, thrust));
            }
            case EnumaElishVisualEntity.PHASE_BEAM -> {
                float recoil = Mth.sin(ticks * 3.1F) * 0.04F;
                thrustPose(model, main, off, frontLeg, backLeg, sideSign, 1.0F, thrust + recoil);
            }
            case EnumaElishVisualEntity.PHASE_FADE -> {
                float k = 1.0F - Ease.inOut(progress);
                thrustPose(model, main, off, frontLeg, backLeg, sideSign, k, Mth.lerp(k, main.xRot, thrust));
            }
            default -> {
                return;
            }
        }
        model.hat.copyFrom(model.head);
    }

    private static void thrustPose(HumanoidModel<?> model, ModelPart main, ModelPart off, ModelPart frontLeg,
                                   ModelPart backLeg, float sideSign, float eased, float mainPitch) {
        InvertedSpearPoses.lean(model, 0.55F * eased);
        main.xRot = mainPitch;
        main.yRot = model.head.yRot * 0.6F * eased;
        main.zRot = 0.0F;
        // shoulder forward so it looks like a lunge, not a swing
        main.z -= 2.0F * eased;
        off.xRot = Mth.lerp(eased, off.xRot, 0.5F);
        off.yRot = 0.0F;
        off.zRot = -sideSign * 0.3F * eased;
        frontLeg.xRot = -0.55F * eased;
        backLeg.xRot = 0.5F * eased;
    }
}
