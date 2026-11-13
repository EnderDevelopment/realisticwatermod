package com.example.waterphysics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

final class WaterReactions {
    private static final Map<ServerLevel, Long> LEVEL_TICKS = new WeakHashMap<>();
    private static final Map<UUID, Long> ENTITY_REACTIONS = new java.util.HashMap<>();

    private WaterReactions() {
    }

    static void tick(ServerLevel level) {
        long gameTime = level.getGameTime();
        if (gameTime % 4 != 0) {
            return;
        }
        LEVEL_TICKS.put(level, gameTime);

        for (Entity entity : level.getAllEntities()) {
            if (!entity.isAlive() || entity.isSpectator()) {
                continue;
            }
            Vec3 velocity = entity.getDeltaMovement();
            double speedSquared = velocity.horizontalDistanceSqr();
            BlockPos feet = entity.blockPosition();
            boolean inWater = entity.isInWater();
            boolean nearWater = inWater || level.getFluidState(feet.below()).is(Fluids.WATER);

            if (nearWater && speedSquared > 0.018 && gameTime % 8 == 0) {
                double x = entity.getX();
                double y = entity.getY() + 0.12;
                double z = entity.getZ();
                level.sendParticles(ParticleTypes.SPLASH, x, y, z,
                        inWater ? 3 : 1, 0.22, 0.08, 0.22, Math.min(0.18, Math.sqrt(speedSquared) * 0.08));
                level.sendParticles(ParticleTypes.BUBBLE, x, y - 0.1, z,
                        inWater ? 1 : 0, 0.12, 0.05, 0.12, 0.025);
            }

            if (inWater && Math.abs(velocity.y) > 0.16) {
                long lastReaction = ENTITY_REACTIONS.getOrDefault(entity.getUUID(), Long.MIN_VALUE / 2);
                if (gameTime - lastReaction >= 12) {
                    level.sendParticles(ParticleTypes.SPLASH, entity.getX(), entity.getY() + 0.35, entity.getZ(),
                            7, 0.3, 0.18, 0.3, Math.min(0.3, Math.abs(velocity.y) * 0.22));
                    ENTITY_REACTIONS.put(entity.getUUID(), gameTime);
                }
            }
        }

        if (ENTITY_REACTIONS.size() > 4096) {
            ENTITY_REACTIONS.entrySet().removeIf(entry -> gameTime - entry.getValue() > 1200);
        }
    }
}
