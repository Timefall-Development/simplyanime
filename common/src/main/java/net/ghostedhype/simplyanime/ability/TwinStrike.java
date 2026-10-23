package net.ghostedhype.simplyanime.ability;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// one Twin Heaven Strike in progress
class TwinStrike {
    final UUID actorId;
    @Nullable final UUID ownerId;
    final ItemStack stack;
    final InteractionHand hand;
    final long startTime;
    final UUID stanceId;
    @Nullable UUID targetId;
    final List<UUID> hit = new ArrayList<>();
    Vec3 end = Vec3.ZERO;
    Vec3 direction = Vec3.ZERO;
    long dashStart = -1;
    long arrivedAt = -1;
    long slashStart = -1;
    long recoverUntil = -1;
    boolean secondDone;

    TwinStrike(UUID actorId, @Nullable UUID ownerId, ItemStack stack, InteractionHand hand, long startTime, UUID stanceId,
               @Nullable UUID targetId) {
        this.actorId = actorId;
        this.ownerId = ownerId;
        this.stack = stack;
        this.hand = hand;
        this.startTime = startTime;
        this.stanceId = stanceId;
        this.targetId = targetId;
    }
}
