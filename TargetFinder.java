package pl.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class TargetFinder {
    private static final double RANGE = 64.0;

    public static Entity find(MinecraftClient mc, float tickDelta) {
        Entity cam = mc.getCameraEntity();
        if (cam == null || mc.world == null) return null;

        Vec3d start = cam.getCameraPosVec(tickDelta);
        Vec3d look = cam.getRotationVec(tickDelta);
        Vec3d end = start.add(look.multiply(RANGE));
        Box box = cam.getBoundingBox().stretch(look.multiply(RANGE)).expand(1.0);

        EntityHitResult hit = ProjectileUtil.raycast(
                cam, start, end, box,
                e -> e instanceof LivingEntity && e != cam && e.isAlive() && !e.isSpectator(),
                RANGE * RANGE
        );
        if (hit == null) return null;

        // blok między nami a przeciwnikiem = brak celu
        HitResult blockHit = cam.raycast(RANGE, tickDelta, false);
        if (blockHit.getType() != HitResult.Type.MISS
                && blockHit.getPos().squaredDistanceTo(start) < hit.getPos().squaredDistanceTo(start)) {
            return null;
        }
        return hit.getEntity();
    }
}
