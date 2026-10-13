package net.ghostedhype.simplyanime.entity;

import net.ghostedhype.simplyanime.SimplyAnime;
import net.ghostedhype.simplyanime.ability.AbilityHelper;
import net.ghostedhype.simplyanime.ability.SunPower;
import net.ghostedhype.simplyanime.callout.Callout;
import net.ghostedhype.simplyanime.callout.Callouts;
import net.ghostedhype.simplyanime.config.RhittaConfig;
import net.ghostedhype.simplyanime.item.DivineAxeRhittaItem;
import net.ghostedhype.simplyanime.platform.Platform;
import net.ghostedhype.simplyanime.registry.EntityRegistry;
import net.ghostedhype.simplyanime.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.api.SpellScalingProfile;
import net.sweenus.simplyswords.api.WeaponAbilityContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public class CruelSunEntity extends PhasedEffectEntity {

    public static final int PHASE_CHARGE = 0;
    public static final int PHASE_FLIGHT = 1;
    public static final int PHASE_BLAST = 2;

    // shared with the renderer
    public static final float HEAT_EDGE = 2.0F;          // corona ends here, x orb radius
    public static final float FLIGHT_GROWTH = 0.2F;
    public static final float FLIGHT_GROW_TICKS = 30.0F;
    public static final int IGNITE_TICKS = 20;
    public static final int ERUPT_TICKS = 25;
    public static final int SWELL_TICKS = 55;
    public static final int BLAST_GROW = 10;
    public static final int BLAST_TICKS = 70;
    public static final float LINGER_CUTOFF = 0.6F;      // fireball is too faint to burn past this
    public static final float TRAIL_LENGTH = 3.0F;       // x orb radius
    public static final float TRAIL_WIDTH = 0.9F;        // half width at the sun
    public static final float TRAIL_TIP = 0.15F;
    public static final float FADE_SWELL = 0.08F;
    private static final double HAND_OFFSET = 0.36;      // raised hand, out from the middle of the body

    public static boolean dealing;

    public static final Map<Integer, CruelSunEntity> CLIENT_BY_OWNER = new HashMap<>();
    public static final Set<CruelSunEntity> CLIENT_ACTIVE = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<UUID, CruelSunEntity> CHARGING = new HashMap<>();

    private static final EntityDataAccessor<Float> POWER = SynchedEntityData.defineId(CruelSunEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SUN = SynchedEntityData.defineId(CruelSunEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ORB_BASE = SynchedEntityData.defineId(CruelSunEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BLAST_RADIUS = SynchedEntityData.defineId(CruelSunEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3f> DIRECTION = SynchedEntityData.defineId(CruelSunEntity.class, EntityDataSerializers.VECTOR3);

    @Nullable private UUID ownerUuid;
    @Nullable private UUID targetUuid;
    private ItemStack stack = ItemStack.EMPTY;
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    private Vec3 direction = new Vec3(0, 0, 1);
    private double traveled;
    private final Set<UUID> blasted = new HashSet<>();

    private boolean flightSoundStarted;

    public CruelSunEntity(EntityType<? extends CruelSunEntity> type, Level world) {
        super(type, world);
    }

    public static void clearAll() {
        CHARGING.clear();
    }

    public static boolean blocksDamage(LivingEntity entity, DamageSource source) {
        return RhittaConfig.get().cruelSun.chargeInvulnerable && isCharging(entity)
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    public static boolean isCharging(LivingEntity actor) {
        CruelSunEntity sun = CHARGING.get(actor.getUUID());
        return sun != null && !sun.isRemoved() && sun.getPhase() == PHASE_CHARGE;
    }

    public static boolean start(WeaponAbilityContext context) {
        return start(context.world(), context.actor(), context.target(), context.stack(),
                context.hand() == null ? InteractionHand.MAIN_HAND : context.hand());
    }

    public static boolean start(ServerLevel world, LivingEntity actor, LivingEntity target, ItemStack stack, InteractionHand hand) {
        RhittaConfig.CruelSunSettings cfg = RhittaConfig.get().cruelSun;
        float power = SunPower.sunPower(world, actor);
        float sun = SunPower.fraction(power);

        CruelSunEntity entity = new CruelSunEntity(EntityRegistry.CRUEL_SUN.get(), world);
        entity.ownerUuid = actor.getUUID();
        entity.targetUuid = target == null ? null : target.getUUID();
        entity.stack = stack.copy();
        entity.hand = hand;
        entity.setOwner(actor);
        entity.entityData.set(POWER, power);
        entity.entityData.set(SUN, sun);
        entity.entityData.set(ORB_BASE, cfg.orbRadius * (1.0F + cfg.orbSunScaling * sun));
        entity.entityData.set(BLAST_RADIUS, cfg.blastRadius * (1.0F + cfg.blastSunScaling * sun));
        entity.setPhase(PHASE_CHARGE, cfg.chargeDuration);
        Vec3 at = chargeCenter(actor, 1.0F, entity.getOrbRadius(0.0F));
        entity.setPos(at.x, at.y, at.z);
        world.addFreshEntity(entity);
        CHARGING.put(actor.getUUID(), entity);

        if (cfg.chargeSlowness >= 0) {
            actor.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, cfg.chargeDuration + 2,
                    cfg.chargeSlowness, false, false, false));
        }
        AbilityHelper.playSound(world, actor.position(), SoundRegistry.RHITTA_BUILD_UP.get(), cfg.buildUpVolume, 1.0F);
        if (cfg.nameText && cfg.nameTextLead >= cfg.chargeDuration) {
            Callouts.send(world, actor.position(), Callout.CRUEL_SUN);
        }
        return true;
    }

    public static float orbRadius(float base, int phase, float chargeTicks, float flightTicks) {
        if (phase == PHASE_CHARGE) {
            if (chargeTicks < IGNITE_TICKS) {
                return base * (0.06F + 0.03F * chargeTicks / IGNITE_TICKS);
            }
            float g = Math.min(1.0F, (chargeTicks - IGNITE_TICKS) / ERUPT_TICKS);
            g = 1.0F - (1.0F - g) * (1.0F - g) * (1.0F - g);
            float late = Mth.clamp((chargeTicks - IGNITE_TICKS - ERUPT_TICKS) / SWELL_TICKS, 0.0F, 1.0F);
            return base * (0.09F + 0.76F * g + 0.15F * late);
        }
        return base * (1.0F + FLIGHT_GROWTH * Math.min(1.0F, flightTicks / FLIGHT_GROW_TICKS));
    }

    public static float blastFront(float radius, float ticks) {
        float g = Math.min(1.0F, ticks / BLAST_GROW);
        return radius * (1.0F - (1.0F - g) * (1.0F - g));
    }

    public static float blastFade(float ticks) {
        return Mth.clamp((ticks - BLAST_GROW) / (float) (BLAST_TICKS - BLAST_GROW), 0.0F, 1.0F);
    }

    // over the hand holding Rhitta
    public static Vec3 chargeCenter(LivingEntity owner, float partialTick, float orb) {
        float yaw = Mth.lerp(partialTick, owner.yBodyRotO, owner.yBodyRot) * Mth.DEG_TO_RAD;
        double side = owner.getMainArm() == HumanoidArm.LEFT ? -HAND_OFFSET : HAND_OFFSET;
        return owner.getPosition(partialTick).add(-Mth.cos(yaw) * side, owner.getBbHeight() + 2.2 + orb,
                -Mth.sin(yaw) * side);
    }

    // full size, reached at the end of the build up
    public float getOrbBase() {
        return this.entityData.get(ORB_BASE);
    }

    public float getOrbRadius(float partialTick) {
        int phase = this.getPhase();
        float ticks = this.getPhaseTicks(partialTick);
        return orbRadius(this.entityData.get(ORB_BASE), phase, phase == PHASE_CHARGE ? ticks : 0.0F,
                phase == PHASE_FLIGHT ? ticks : 0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.clientTick();
            return;
        }
        ServerLevel world = (ServerLevel) this.level();
        LivingEntity owner = this.owner(world);
        RhittaConfig.CruelSunSettings cfg = RhittaConfig.get().cruelSun;
        int ticks = (int) this.getPhaseTicks(0.0F);

        switch (this.getPhase()) {
            case PHASE_CHARGE -> this.tickCharge(world, owner, cfg, ticks);
            case PHASE_FLIGHT -> this.tickFlight(world, owner, cfg, ticks);
            case PHASE_BLAST -> this.tickBlast(world, owner, cfg, ticks);
            default -> this.discard();
        }
        if (this.tickCount > 20 * 60) {
            this.discard();
        }
    }

    private void tickCharge(ServerLevel world, LivingEntity owner, RhittaConfig.CruelSunSettings cfg, int ticks) {
        if (owner == null || !owner.isAlive() || !holdingAxe(owner)) {
            // swapped off the axe or died, the sun fizzles
            if (owner != null) {
                owner.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            }
            AbilityHelper.playSound(world, this.position(), SoundEvents.FIRE_EXTINGUISH, 2.0F, 0.6F);
            this.stopSound(world, SoundRegistry.RHITTA_BUILD_UP.get());
            this.discard();
            return;
        }
        float orb = this.getOrbRadius(0.0F);
        Vec3 at = chargeCenter(owner, 1.0F, orb);
        this.setPos(at.x, at.y, at.z);

        if (cfg.nameText && cfg.nameTextLead < cfg.chargeDuration && ticks == Math.max(1, cfg.chargeDuration - cfg.nameTextLead)) {
            Callouts.send(world, owner.position(), Callout.CRUEL_SUN);
        }
        if (ticks == IGNITE_TICKS) {
            AbilityHelper.playSound(world, at, SoundEvents.BLAZE_SHOOT, cfg.buildUpVolume * 0.5F, 0.5F);
        }
        if (ticks >= IGNITE_TICKS && ticks % 3 == 0 && cfg.surroundingsCatchFire) {
            this.setSurroundingsAlight(world, owner, orb);
        }
        if (ticks >= IGNITE_TICKS && ticks % Math.max(1, cfg.heatInterval) == 0) {
            this.heat(world, owner, at, orb * HEAT_EDGE, null, 0.0F, cfg);
        }
        if (ticks >= cfg.chargeDuration) {
            this.launch(world, owner, cfg);
        }
    }

    private void launch(ServerLevel world, LivingEntity owner, RhittaConfig.CruelSunSettings cfg) {
        this.direction = this.aim(world, owner);
        this.entityData.set(DIRECTION, pack(this.direction));
        this.setPhase(PHASE_FLIGHT, (int) Math.ceil(cfg.range / Math.max(0.2, cfg.projectileSpeed)));
        CHARGING.remove(owner.getUUID(), this);
        // don't let the build up audio run into the throw
        this.stopSound(world, SoundRegistry.RHITTA_BUILD_UP.get());
        owner.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        owner.swing(this.hand, true);
        AbilityHelper.playSound(world, owner.position(), SoundRegistry.RHITTA_LAUNCH.get(), cfg.launchVolume, 0.9F);
        AbilityHelper.playSound(world, owner.position(), SoundEvents.BLAZE_SHOOT, cfg.launchVolume * 0.6F, 0.6F);
    }

    private Vec3 aim(ServerLevel world, LivingEntity owner) {
        if (owner instanceof Player) {
            RhittaConfig.CruelSunSettings cfg = RhittaConfig.get().cruelSun;
            Vec3 eye = owner.getEyePosition();
            Vec3 reach = eye.add(owner.getViewVector(1.0F).scale(cfg.range));
            BlockHitResult look = world.clip(new ClipContext(eye, reach, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, owner));
            Vec3 to = (look.getType() == HitResult.Type.BLOCK ? look.getLocation() : reach).subtract(this.position());
            if (to.lengthSqr() > 1.0) {
                return to.normalize();
            }
        }
        if (!(owner instanceof Player) && this.targetUuid != null
                && world.getEntity(this.targetUuid) instanceof LivingEntity target && target.isAlive()) {
            Vec3 to = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(this.position());
            if (to.lengthSqr() > 1.0E-4) {
                return to.normalize();
            }
        }
        return Vec3.directionFromRotation(owner.getXRot(), owner.getYHeadRot()).normalize();
    }

    private void tickFlight(ServerLevel world, LivingEntity owner, RhittaConfig.CruelSunSettings cfg, int ticks) {
        float orb = this.getOrbRadius(0.0F);
        Vec3 from = this.position();
        Vec3 to = from.add(this.direction.scale(cfg.projectileSpeed));

        Vec3 hit = null;
        BlockHitResult block = world.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        if (block.getType() == HitResult.Type.BLOCK) {
            to = block.getLocation().subtract(this.direction.scale(Math.min(orb, 0.5)));
            hit = to;
        }
        Vec3 entityHit = this.firstEntityOnPath(world, owner, from, to, orb);
        if (entityHit != null) {
            hit = entityHit;
            to = entityHit;
        }

        this.traveled += from.distanceTo(to);
        this.setPos(to.x, to.y, to.z);
        if (ticks % Math.max(1, cfg.heatInterval) == 0) {
            this.heat(world, owner, to, orb * HEAT_EDGE, this.direction.scale(-orb * TRAIL_LENGTH), orb, cfg);
        }
        if (hit != null || this.traveled >= cfg.range) {
            this.detonate(world, owner, cfg);
        }
    }

    @Nullable
    private Vec3 firstEntityOnPath(ServerLevel world, LivingEntity owner, Vec3 from, Vec3 to, float orb) {
        Vec3 best = null;
        double bestAlong = Double.MAX_VALUE;
        Vec3 path = to.subtract(from);
        double length = path.length();
        Vec3 dir = length < 1.0E-6 ? this.direction : path.scale(1.0 / length);
        for (Entity entity : world.getEntities(this, new AABB(from, to).inflate(orb + 1.0))) {
            LivingEntity target = rootLiving(entity);
            if (target == null || !canHit(target, owner)) {
                continue;
            }
            AABB box = entity.getBoundingBox();
            double along = Mth.clamp(box.getCenter().subtract(from).dot(dir), 0.0, length);
            Vec3 point = from.add(dir.scale(along));
            if (AbilityHelper.distanceToPoint(box, point) <= orb && along < bestAlong) {
                bestAlong = along;
                best = point;
            }
        }
        return best;
    }

    private void detonate(ServerLevel world, LivingEntity owner, RhittaConfig.CruelSunSettings cfg) {
        this.setPhase(PHASE_BLAST, BLAST_TICKS);
        float sun = this.getSun();
        Vec3 at = this.position();
        float volume = cfg.blastVolume * (0.7F + 0.3F * sun);
        float pitch = 1.05F - 0.25F * sun;
        AbilityHelper.playSound(world, at, SoundRegistry.RHITTA_BLAST.get(), volume, pitch);
        AbilityHelper.playSound(world, at, SoundRegistry.RHITTA_BLAST_ROAR.get(), volume * 0.7F, pitch * 0.9F);
        AbilityHelper.playSound(world, at, Platform.explosionSound(), volume * 0.6F, pitch * 0.7F);
        this.explosionBlocks(world, owner, cfg);
    }

    private void tickBlast(ServerLevel world, LivingEntity owner, RhittaConfig.CruelSunSettings cfg, int ticks) {
        float radius = this.getBlastRadius();
        Vec3 center = this.position();
        if (ticks <= BLAST_GROW) {
            this.blast(world, owner, center, blastFront(radius, ticks), radius, cfg);
        } else if (blastFade(ticks) < LINGER_CUTOFF && ticks % Math.max(1, cfg.heatInterval) == 0) {
            this.heat(world, owner, center, radius * (1.0F + FADE_SWELL * blastFade(ticks)), null, 0.0F, cfg);
        }
        if (ticks >= BLAST_TICKS) {
            this.discard();
        }
    }

    // the big hit, once per target as the edge reaches it
    private void blast(ServerLevel world, LivingEntity owner, Vec3 center, float front, float radius,
                       RhittaConfig.CruelSunSettings cfg) {
        float full = this.scaled(owner, cfg.blastDamageScaling, cfg.blastSpellScaling);
        for (Entity entity : world.getEntities(this, new AABB(center, center).inflate(front + 2.0))) {
            LivingEntity target = rootLiving(entity);
            if (target == null || !canHit(target, owner)) {
                continue;
            }
            double dist = AbilityHelper.distanceToPoint(entity.getBoundingBox(), center);
            if (dist > front || !this.blasted.add(target.getUUID())) {
                continue;
            }
            float falloff = Mth.lerp((float) Mth.clamp(dist / radius, 0.0, 1.0), 1.0F, cfg.edgeDamage);
            if (this.hurt(world, owner, target, full * falloff)) {
                Vec3 push = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(center);
                push = push.lengthSqr() < 1.0E-4 ? new Vec3(0, 1, 0) : push.normalize();
                double strength = cfg.knockback * falloff;
                target.push(push.x * strength, 0.25 * strength + push.y * 0.3 * strength, push.z * strength);
                target.hurtMarked = true;
                Platform.ignite(target, Math.round(cfg.blastBurnSeconds * (0.5F + 0.5F * this.getSun())));
            }
        }
    }

    // heat ticks: corona, trail, lingering fireball
    private void heat(ServerLevel world, LivingEntity owner, Vec3 center, float radius, Vec3 trail,
                      float orb, RhittaConfig.CruelSunSettings cfg) {
        float damage = this.scaled(owner, cfg.heatDamageScaling, cfg.heatSpellScaling);
        AABB area = new AABB(center, center).inflate(radius + 2.0);
        if (trail != null) {
            area = area.minmax(new AABB(center.add(trail), center.add(trail)).inflate(orb * TRAIL_WIDTH + 2.0));
        }
        Set<LivingEntity> struck = new HashSet<>();
        for (Entity entity : world.getEntities(this, area)) {
            LivingEntity target = rootLiving(entity);
            if (target == null || !canHit(target, owner) || struck.contains(target)) {
                continue;
            }
            AABB box = entity.getBoundingBox();
            boolean inside = AbilityHelper.distanceToPoint(box, center) <= radius
                    || (trail != null && inTrail(box, center, trail, orb));
            if (!inside) {
                continue;
            }
            struck.add(target);
            if (this.hurt(world, owner, target, damage) && cfg.heatBurnSeconds > 0) {
                Platform.ignite(target, Math.round(cfg.heatBurnSeconds));
            }
        }
    }

    private float scaled(LivingEntity owner, float damageScaling, float spellScaling) {
        float base = owner == null ? damageScaling
                : SimplySwordsAPI.scaleAbilityDamage(SpellScalingProfile.ARCANE, owner, this.stack, damageScaling, spellScaling);
        return base * this.getPower();
    }

    private boolean hurt(ServerLevel world, LivingEntity owner, LivingEntity target, float amount) {
        if (amount <= 0) {
            return false;
        }
        dealing = true;
        try {
            if (target instanceof EnderDragon dragon) {
                return dragon.hurt(dragon.head, owner == null ? world.damageSources().magic()
                        : SimplySwordsAPI.getWeaponDamageSource(owner), amount);
            }
            if (owner == null) {
                return target.hurt(world.damageSources().magic(), amount);
            }
            return SimplySwordsAPI.applyAbilityMagicDamageThroughIframes(world, owner, this.stack, target, amount,
                    SpellScalingProfile.ARCANE);
        } finally {
            dealing = false;
        }
    }

    // real fire, only with surroundingsCatchFire on
    private void setSurroundingsAlight(ServerLevel world, LivingEntity owner, float orb) {
        if (!world.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }
        double reach = Math.max(3.0, orb * 1.8);
        for (int i = 0; i < 3; i++) {
            double a = this.random.nextDouble() * Mth.TWO_PI;
            double r = 1.5 + this.random.nextDouble() * reach;
            BlockPos.MutableBlockPos pos = BlockPos.containing(owner.getX() + Math.cos(a) * r, owner.getY() + 3,
                    owner.getZ() + Math.sin(a) * r).mutable();
            for (int d = 0; d < 7 && world.getBlockState(pos).isAir(); d++) {
                pos.move(0, -1, 0);
            }
            BlockState state = world.getBlockState(pos);
            if (state.isAir() || !state.isFlammable(world, pos, Direction.UP)) {
                continue;
            }
            if (state.canBeReplaced()) {
                world.setBlockAndUpdate(pos.immutable(), BaseFireBlock.getState(world, pos));
            } else if (world.getBlockState(pos.above()).isAir()) {
                world.setBlockAndUpdate(pos.above(), BaseFireBlock.getState(world, pos.above()));
            }
        }
    }

    private void explosionBlocks(ServerLevel world, LivingEntity owner, RhittaConfig.CruelSunSettings cfg) {
        if ((!cfg.explosionBreaksBlocks && !cfg.explosionStartsFires)
                || !world.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }
        BlockPos center = this.blockPosition();
        if (cfg.explosionBreaksBlocks) {
            int r = (int) Math.min(this.getBlastRadius() * 0.5F, 12.0F);
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
                if (pos.distSqr(center) > r * r) {
                    continue;
                }
                BlockState state = world.getBlockState(pos);
                float hardness = state.getDestroySpeed(world, pos);
                if (!state.isAir() && hardness >= 0 && hardness < 50) {
                    world.destroyBlock(pos.immutable(), true, owner);
                }
            }
        }
        if (cfg.explosionStartsFires) {
            int r = (int) Math.min(this.getBlastRadius() * 0.6F, 14.0F);
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
                if (this.random.nextInt(5) != 0 || pos.distSqr(center) > r * r
                        || !world.getBlockState(pos).isAir()
                        || !world.getBlockState(pos.below()).isSolidRender(world, pos.below())) {
                    continue;
                }
                world.setBlockAndUpdate(pos.immutable(), BaseFireBlock.getState(world, pos));
            }
        }
    }

    // same taper as the drawn trail
    private static boolean inTrail(AABB box, Vec3 center, Vec3 trail, float orb) {
        double length = trail.length();
        if (length < 1.0E-4) {
            return false;
        }
        Vec3 dir = trail.scale(1.0 / length);
        double along = Mth.clamp(box.getCenter().subtract(center).dot(dir), 0.0, length);
        float half = orb * Mth.lerp((float) (along / length), TRAIL_WIDTH, TRAIL_TIP);
        return AbilityHelper.distanceToPoint(box, center.add(dir.scale(along))) <= half;
    }

    @Nullable
    private static LivingEntity rootLiving(Entity entity) {
        Entity root = Platform.partParent(entity);
        return root instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    private static boolean canHit(LivingEntity target, LivingEntity owner) {
        if (owner == null) {
            return true;
        }
        return target != owner && SimplySwordsAPI.isValidAbilityTarget(target, owner);
    }

    private static boolean holdingAxe(LivingEntity entity) {
        return entity.getMainHandItem().getItem() instanceof DivineAxeRhittaItem
                || entity.getOffhandItem().getItem() instanceof DivineAxeRhittaItem;
    }

    private void stopSound(ServerLevel world, SoundEvent sound) {
        ClientboundStopSoundPacket stop = new ClientboundStopSoundPacket(sound.getLocation(), SoundSource.PLAYERS);
        for (ServerPlayer player : world.getPlayers(p -> p.distanceToSqr(this) < 192 * 192)) {
            player.connection.send(stop);
        }
    }

    private void clientTick() {
        LivingEntity owner = this.getOwner();
        int phase = this.getPhase();
        CLIENT_ACTIVE.add(this);
        if (owner != null) {
            boolean posing = phase == PHASE_CHARGE || (phase == PHASE_FLIGHT && this.getPhaseTicks(0.0F) < 10);
            if (posing) {
                CLIENT_BY_OWNER.put(owner.getId(), this);
            } else {
                CLIENT_BY_OWNER.remove(owner.getId(), this);
            }
            if (phase == PHASE_CHARGE) {
                owner.yBodyRotO = owner.yHeadRotO;
                owner.yBodyRot = owner.yHeadRot;
            }
        }
        if (phase == PHASE_FLIGHT && !this.flightSoundStarted) {
            this.flightSoundStarted = true;
            SimplyAnime.client.sunFlightStarted(this);
        }
        SimplyAnime.client.sunTick(this, owner);
        if (this.tickCount % 2 == 0) {
            EffectLights.set(this, this.lightSpots(owner));
        }
    }

    // spots for EffectLights
    private List<Vec3> lightSpots(LivingEntity owner) {
        List<Vec3> spots = new ArrayList<>();
        int phase = this.getPhase();
        float ticks = this.getPhaseTicks(0.0F);
        if (phase == PHASE_CHARGE) {
            if (owner == null || ticks < IGNITE_TICKS) {
                return spots;
            }
            float orb = this.getOrbRadius(0.0F);
            Vec3 center = chargeCenter(owner, 1.0F, orb);
            spots.add(center.add(0, -orb, 0));
            for (int i = 0; i < 4; i++) {
                float a = i * Mth.HALF_PI;
                spots.add(owner.position().add(Mth.cos(a) * orb, 2.0, Mth.sin(a) * orb));
            }
        } else if (phase == PHASE_FLIGHT) {
            spots.add(this.position().add(0, -this.getOrbRadius(0.0F), 0));
            spots.add(this.position());
        } else if (phase == PHASE_BLAST && blastFade(ticks) < 0.7F) {
            float front = blastFront(this.getBlastRadius(), ticks);
            spots.add(this.position().add(0, 2.0, 0));
            for (int i = 0; i < 8; i++) {
                float a = i * Mth.TWO_PI / 8.0F;
                float r = front * (i % 2 == 0 ? 0.5F : 0.95F);
                spots.add(this.position().add(Mth.cos(a) * r, 2.0, Mth.sin(a) * r));
            }
        }
        return spots;
    }

    @Override
    public void onClientRemoval() {
        super.onClientRemoval();
        CLIENT_BY_OWNER.values().removeIf(sun -> sun == this);
        CLIENT_ACTIVE.remove(this);
        EffectLights.clear(this);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!this.level().isClientSide()) {
            CHARGING.values().removeIf(sun -> sun == this);
        }
    }

    @Override
    protected void defineData(DataDefiner definer) {
        super.defineData(definer);
        definer.define(POWER, 1.0F);
        definer.define(SUN, 0.0F);
        definer.define(ORB_BASE, 1.0F);
        definer.define(BLAST_RADIUS, 6.0F);
        definer.define(DIRECTION, new Vector3f(0, 0, 1));
    }

    public float getPower() {
        return this.entityData.get(POWER);
    }

    // 0 at night, 1 at noon
    public float getSun() {
        return this.entityData.get(SUN);
    }

    public float getBlastRadius() {
        return this.entityData.get(BLAST_RADIUS);
    }

    public Vec3 getAim() {
        return unpack(this.entityData.get(DIRECTION));
    }

    @Nullable
    private LivingEntity owner(ServerLevel world) {
        return this.ownerUuid != null && world.getEntity(this.ownerUuid) instanceof LivingEntity living ? living : null;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }
}
