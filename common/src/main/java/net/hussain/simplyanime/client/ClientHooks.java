package net.hussain.simplyanime.client;

import net.hussain.simplyanime.entity.CruelSunEntity;
import net.minecraft.world.entity.LivingEntity;

public interface ClientHooks {

    ClientHooks NONE = new ClientHooks() {
    };

    default void sunTick(CruelSunEntity sun, LivingEntity owner) {
    }

    default void sunFlightStarted(CruelSunEntity sun) {
    }

    default float localSunPower() {
        return -1.0F;
    }
}
