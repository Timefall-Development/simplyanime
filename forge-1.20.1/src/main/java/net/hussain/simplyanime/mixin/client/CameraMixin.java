package net.hussain.simplyanime.mixin.client;

import net.hussain.simplyanime.client.RhittaCinematics;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// third person camera pull back for Cruel Sun, still clips at walls.
// require = 0 so a camera mod replacing this call doesn't crash
@Mixin(Camera.class)
public abstract class CameraMixin {

    @ModifyArg(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(D)D"),
            require = 0)
    private double simplyanime$pullBack(double distance) {
        return distance + RhittaCinematics.cameraPullBack();
    }
}
