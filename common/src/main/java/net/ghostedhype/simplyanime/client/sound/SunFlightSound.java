package net.ghostedhype.simplyanime.client.sound;

import net.ghostedhype.simplyanime.config.RhittaConfig;
import net.ghostedhype.simplyanime.entity.CruelSunEntity;
import net.ghostedhype.simplyanime.registry.SoundRegistry;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

// roar that rides along with the sun while it flies
public class SunFlightSound extends AbstractTickableSoundInstance {

    private final CruelSunEntity sun;

    public SunFlightSound(CruelSunEntity sun) {
        super(SoundRegistry.RHITTA_FLIGHT.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.sun = sun;
        this.looping = true;
        this.delay = 0;
        this.volume = RhittaConfig.get().cruelSun.flightVolume * (0.7F + 0.3F * sun.getSun());
        this.pitch = 1.0F - 0.2F * sun.getSun();
        this.x = sun.getX();
        this.y = sun.getY();
        this.z = sun.getZ();
    }

    @Override
    public void tick() {
        if (this.sun.isRemoved() || this.sun.getPhase() != CruelSunEntity.PHASE_FLIGHT) {
            this.stop();
            return;
        }
        this.x = this.sun.getX();
        this.y = this.sun.getY();
        this.z = this.sun.getZ();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
