package net.hussain.simplyanime.entity;

import net.hussain.simplyanime.config.ClientConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

// light blocks in the air near an effect so it lights the ground. client world only, the server never sees them
public class EffectLights {

    private static final Map<Entity, Set<BlockPos>> LIGHTS = new WeakHashMap<>();
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    public static void set(Entity owner, List<Vec3> spots) {
        Level world = owner.level();
        if (!world.isClientSide()) {
            return;
        }
        if (!ClientConfig.get().dynamicLight || spots.isEmpty()) {
            clear(owner);
            return;
        }
        Set<BlockPos> wanted = new HashSet<>();
        for (Vec3 spot : spots) {
            wanted.add(BlockPos.containing(spot));
        }
        Set<BlockPos> placed = LIGHTS.computeIfAbsent(owner, e -> new HashSet<>());
        Iterator<BlockPos> old = placed.iterator();
        while (old.hasNext()) {
            BlockPos pos = old.next();
            if (!wanted.contains(pos)) {
                remove(world, pos);
                old.remove();
            }
        }
        BlockState light = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15);
        for (BlockPos pos : wanted) {
            if (!placed.contains(pos) && world.getBlockState(pos).isAir()) {
                world.setBlock(pos, light, FLAGS);
                placed.add(pos);
            }
        }
    }

    public static void clear(Entity owner) {
        Set<BlockPos> placed = LIGHTS.remove(owner);
        if (placed != null) {
            for (BlockPos pos : placed) {
                remove(owner.level(), pos);
            }
        }
    }

    private static void remove(Level world, BlockPos pos) {
        if (world.getBlockState(pos).is(Blocks.LIGHT)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), FLAGS);
        }
    }
}
