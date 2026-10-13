package net.hussain.simplyanime.mixin.client;

import net.hussain.simplyanime.client.pose.EnumaElishPoses;
import net.hussain.simplyanime.client.pose.InvertedSpearPoses;
import net.hussain.simplyanime.client.pose.RhittaPoses;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> {

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void simplyanime$poseAbilityArms(T entity, float limbAngle, float limbDistance, float animationProgress,
                                                    float headYaw, float headPitch, CallbackInfo ci) {
        InvertedSpearPoses.apply(entity, animationProgress, (HumanoidModel<?>) (Object) this);
        EnumaElishPoses.apply(entity, animationProgress, (HumanoidModel<?>) (Object) this);
        RhittaPoses.apply(entity, animationProgress, (HumanoidModel<?>) (Object) this);
    }
}
