package net.ghostedhype.simplyanime.client.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.ghostedhype.simplyanime.entity.EnumaElishVisualEntity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class EnumaElishFirstPerson {

    public static boolean apply(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack stack,
                                float partialTick, float equipProgress, float swingProgress) {
        EnumaElishVisualEntity cast = EnumaElishVisualEntity.CLIENT_BY_OWNER.get(player.getId());
        if (cast == null || cast.isRemoved() || !isCastingHand(player, arm, stack)) {
            return false;
        }
        float s = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float t = cast.getPhaseProgress(partialTick);
        float ticks = cast.getPhaseTicks(partialTick);

        // same starting point vanilla uses for a held item
        poseStack.translate(s * 0.56F, -0.52F, -0.72F);

        switch (cast.getPhase()) {
            case EnumaElishVisualEntity.PHASE_CHARGE -> {
                float e = Ease.out(Math.min(1.0F, ticks / 8.0F));
                float shake = Mth.sin(ticks * 2.3F) * (0.004F + 0.012F * t);
                poseStack.translate(-s * 0.12F * e + shake, 0.32F * e, 0.1F * e);
                poseStack.mulPose(Axis.XP.rotationDegrees(28.0F * e));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-s * 12.0F * e));
            }
            case EnumaElishVisualEntity.PHASE_RELEASE -> {
                float e = Ease.out(t);
                thrust(poseStack, s, e, 1.0F - e);
            }
            case EnumaElishVisualEntity.PHASE_BEAM -> {
                thrust(poseStack, s, 1.0F, 0.0F);
                poseStack.translate(Mth.sin(ticks * 3.7F) * 0.012F, Mth.cos(ticks * 4.1F) * 0.012F, 0.0F);
            }
            case EnumaElishVisualEntity.PHASE_FADE -> thrust(poseStack, s, 1.0F - Ease.inOut(t), 0.0F);
            default -> {
                return false;
            }
        }
        return true;
    }

    private static void thrust(PoseStack poseStack, float s, float e, float raised) {
        poseStack.translate(-s * 0.18F * e, 0.06F * e + 0.32F * raised, -0.42F * e + 0.1F * raised);
        poseStack.mulPose(Axis.XP.rotationDegrees(-62.0F * e + 28.0F * raised));
        poseStack.mulPose(Axis.YP.rotationDegrees(s * 8.0F * e));
    }

    // with one in each hand only the main hand moves
    private static boolean isCastingHand(LocalPlayer player, HumanoidArm arm, ItemStack stack) {
        return arm == player.getMainArm() || player.getMainHandItem().getItem() != stack.getItem();
    }
}
