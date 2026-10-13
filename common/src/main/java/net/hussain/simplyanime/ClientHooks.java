package net.hussain.simplyanime;

import net.hussain.simplyanime.entity.CruelSunEntity;
import net.minecraft.world.entity.LivingEntity;

// the calls shared code makes into the client. A server keeps NONE and they do nothing
public interface ClientHooks {

    ClientHooks NONE = new ClientHooks() {
    };

    default void sunTick(CruelSunEntity sun, LivingEntity owner) {
    }

    default void sunFlightStarted(CruelSunEntity sun) {
    }

    // sun power where the local player stands, below 0 when there is none
    default float localSunPower() {
        return -1.0F;
    }
}
