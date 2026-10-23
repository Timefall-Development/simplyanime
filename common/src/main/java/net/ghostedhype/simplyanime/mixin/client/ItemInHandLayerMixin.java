package net.ghostedhype.simplyanime.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ghostedhype.simplyanime.client.pose.BladePoses;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemInHandLayer.class, priority = 2000)
public abstract class ItemInHandLayerMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void simplyanime$animatedSword(LivingEntity entity, ItemStack stack, ItemDisplayContext context, HumanoidArm arm,
                                           PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (BladePoses.renderItem(entity, stack, context, arm, poseStack, buffers, light)) {
            ci.cancel();
        }
    }
}
