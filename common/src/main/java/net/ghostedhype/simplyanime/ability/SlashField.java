package net.ghostedhype.simplyanime.ability;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// one Heavenly Slash Field in progress
class SlashField {
    final UUID actorId;
    @Nullable final UUID ownerId;
    final ItemStack stack;
    final InteractionHand hand;
    final long startTime;
    final UUID chainId;
    @Nullable final UUID lockOnId;
    @Nullable final UUID lockTargetId;
    @Nullable UUID payoffTargetId;
    Vec3 flingFrom = Vec3.ZERO;
    Vec3 flingTo = Vec3.ZERO;
    Vec3 retractFrom = Vec3.ZERO;

    SlashField(UUID actorId, @Nullable UUID ownerId, ItemStack stack, InteractionHand hand, long startTime, UUID chainId,
               @Nullable UUID lockOnId, @Nullable UUID lockTargetId) {
        this.actorId = actorId;
        this.ownerId = ownerId;
        this.stack = stack;
        this.hand = hand;
        this.startTime = startTime;
        this.chainId = chainId;
        this.lockOnId = lockOnId;
        this.lockTargetId = lockTargetId;
    }
}
