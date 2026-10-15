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
        float sideSign = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float progress = cast.getPhaseProgress(partialTick);
        float ticks = cast.getPhaseTicks(partialTick);

        // same starting point vanilla uses for a held item
        poseStack.translate(sideSign * 0.56F, -0.52F, -0.72F);

        switch (cast.getPhase()) {
            case EnumaElishVisualEntity.PHASE_CHARGE -> {
                float eased = Ease.out(Math.min(1.0F, ticks / 8.0F));
                float shake = Mth.sin(ticks * 2.3F) * (0.004F + 0.012F * progress);
                poseStack.translate(-sideSign * 0.12F * eased + shake, 0.32F * eased, 0.1F * eased);
                poseStack.mulPose(Axis.XP.rotationDegrees(28.0F * eased));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-sideSign * 12.0F * eased));
            }
            case EnumaElishVisualEntity.PHASE_RELEASE -> {
                float eased = Ease.out(progress);
                thrust(poseStack, sideSign, eased, 1.0F - eased);
            }
            case EnumaElishVisualEntity.PHASE_BEAM -> {
                thrust(poseStack, sideSign, 1.0F, 0.0F);
                poseStack.translate(Mth.sin(ticks * 3.7F) * 0.012F, Mth.cos(ticks * 4.1F) * 0.012F, 0.0F);
            }
            case EnumaElishVisualEntity.PHASE_FADE -> thrust(poseStack, sideSign, 1.0F - Ease.inOut(progress), 0.0F);
            default -> {
                return false;
            }
        }
        return true;
    }

    private static void thrust(PoseStack poseStack, float sideSign, float eased, float raised) {
        poseStack.translate(-sideSign * 0.18F * eased, 0.06F * eased + 0.32F * raised, -0.42F * eased + 0.1F * raised);
        poseStack.mulPose(Axis.XP.rotationDegrees(-62.0F * eased + 28.0F * raised));
        poseStack.mulPose(Axis.YP.rotationDegrees(sideSign * 8.0F * eased));
    }

    // with one in each hand only the main hand moves
    private static boolean isCastingHand(LocalPlayer player, HumanoidArm arm, ItemStack stack) {
        return arm == player.getMainArm() || player.getMainHandItem().getItem() != stack.getItem();
    }
}
