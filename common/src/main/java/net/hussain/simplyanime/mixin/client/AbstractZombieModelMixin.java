package net.hussain.simplyanime.mixin.client;

import net.hussain.simplyanime.client.pose.EnumaElishPoses;
import net.hussain.simplyanime.client.pose.InvertedSpearPoses;
import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// zombies redo their arms after super.setAngles
@Mixin(AbstractZombieModel.class)
public abstract class AbstractZombieModelMixin<T extends Monster> {

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/Monster;FFFFF)V", at = @At("TAIL"))
    private void simplyanime$poseAbilityArms(T entity, float limbAngle, float limbDistance, float animationProgress,
                                                    float headYaw, float headPitch, CallbackInfo ci) {
        InvertedSpearPoses.apply(entity, animationProgress, (HumanoidModel<?>) (Object) this);
        EnumaElishPoses.apply(entity, animationProgress, (HumanoidModel<?>) (Object) this);
    }
}
