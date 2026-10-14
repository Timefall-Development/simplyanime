package net.hussain.simplyanime.client.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.hussain.simplyanime.entity.CruelSunEntity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class RhittaFirstPerson {

    public static boolean apply(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack stack,
                                float partialTick, float equipProgress, float swingProgress) {
        if (arm != player.getMainArm()) {
            return false;
        }
        float s = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;

        // what vanilla does for a held item
        poseStack.translate(s * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
        if (swingProgress > 0) {
            float f = Mth.sin(swingProgress * swingProgress * Mth.PI);
            float g = Mth.sin(Mth.sqrt(swingProgress) * Mth.PI);
            poseStack.mulPose(Axis.YP.rotationDegrees(s * (45.0F + f * -20.0F)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(s * g * -20.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(g * -80.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(s * -45.0F));
        }

        CruelSunEntity cast = CruelSunEntity.CLIENT_BY_OWNER.get(player.getId());
        if (cast != null && !cast.isRemoved()) {
            float ticks = cast.getPhaseTicks(partialTick);
            if (cast.getPhase() == CruelSunEntity.PHASE_CHARGE) {
                float e = Ease.out(Math.min(1.0F, ticks / 8.0F));
                float shake = Mth.sin(ticks * 2.1F) * 0.01F * cast.getPhaseProgress(partialTick);
                poseStack.translate(-s * 0.1F * e + shake, 0.5F * e, 0.15F * e);
                poseStack.mulPose(Axis.XP.rotationDegrees(40.0F * e));
            } else {
                float k = Ease.out(Math.min(1.0F, ticks / 4.0F));
                float back = Mth.clamp((ticks - 5.0F) / 5.0F, 0.0F, 1.0F);
                float raise = (1.0F - k) * (1.0F - back);
                float throwing = k * (1.0F - back);
                poseStack.translate(0, 0.5F * raise - 0.1F * throwing, 0.15F * raise - 0.3F * throwing);
                poseStack.mulPose(Axis.XP.rotationDegrees(40.0F * raise - 55.0F * throwing));
            }
            return true;
        }

        float w = RhittaPoses.weight(player, partialTick);
        if (w > 0.001F) {
            // over the shoulder, slow sway
            float time = player.tickCount + partialTick;
            float sway = Mth.sin(time * 0.07F);
            poseStack.translate(s * 0.08F * w, (0.14F + 0.01F * sway) * w, 0.2F * w);
            poseStack.mulPose(Axis.XP.rotationDegrees((34.0F + 1.5F * sway) * w));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-s * 14.0F * w));
        }
        return true;
    }
}
