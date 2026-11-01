package pl.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import pl.visuals.VisualsConfig.Visual;

public class VisualsRenderer {

    public static void render(WorldRenderContext ctx) {
        Entity t = VisualsMod.target;
        MatrixStack ms = ctx.matrixStack();
        if (t == null || ms == null || ctx.consumers() == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        Entity camEntity = mc.getCameraEntity();
        if (camEntity == null) return;

        float td = mc.getRenderTickCounter().getTickProgress(false);
        Vec3d cam = ctx.camera().getPos();
        Vec3d pos = t.getLerpedPos(td);
        Box box = t.getBoundingBox().offset(pos.subtract(t.getPos()));

        ms.push();
        ms.translate(-cam.x, -cam.y, -cam.z);
        MatrixStack.Entry e = ms.peek();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderLayer.getLines());

        double cx = (box.minX + box.maxX) / 2.0;
        double cz = (box.minZ + box.maxZ) / 2.0;

        // 1) Obramowanie
        if (Visual.BOX.enabled) {
            box(vc, e, box, Visual.BOX.color());
        }

        // 3) Tęczowy box (lekko większy, żeby nie walczył z zwykłym boxem)
        if (Visual.RAINBOW.enabled) {
            float hue = (System.currentTimeMillis() % 3000L) / 3000f;
            int rgb = MathHelper.hsvToRgb(hue, 1f, 1f) | 0xFF000000;
            box(vc, e, box.expand(0.06), rgb);
        }

        // 4) Pierścień pod nogami
        if (Visual.RING.enabled) {
            double r = Math.max(box.getLengthX(), box.getLengthZ()) / 2.0 + 0.3;
            double y = box.minY + 0.02;
            int seg = 48;
            for (int i = 0; i < seg; i++) {
                double a1 = i * 2 * Math.PI / seg;
                double a2 = (i + 1) * 2 * Math.PI / seg;
                line(vc, e,
                        cx + Math.cos(a1) * r, y, cz + Math.sin(a1) * r,
                        cx + Math.cos(a2) * r, y, cz + Math.sin(a2) * r,
                        Visual.RING.color());
            }
        }

        // 5) Tracer - linia od dołu ekranu do środka celu
        if (Visual.TRACER.enabled) {
            Vec3d look = camEntity.getRotationVec(td);
            Vec3d down = camEntity.getRotationVector(camEntity.getPitch(td) + 90f, camEntity.getYaw(td));
            Vec3d start = cam.add(look.multiply(0.7)).add(down.multiply(0.35));
            Vec3d end = new Vec3d(cx, (box.minY + box.maxY) / 2.0, cz);
            line(vc, e, start.x, start.y, start.z, end.x, end.y, end.z, Visual.TRACER.color());
        }

        // 6) Znacznik (odwrócona piramida) nad głową
        if (Visual.MARKER.enabled) {
            double apexY = box.maxY + 0.3;
            double baseY = box.maxY + 0.9;
            double w = 0.25;
            int c = Visual.MARKER.color();
            double[][] corners = {
                    {cx - w, cz - w}, {cx + w, cz - w}, {cx + w, cz + w}, {cx - w, cz + w}
            };
            for (int i = 0; i < 4; i++) {
                double[] a = corners[i];
                double[] b = corners[(i + 1) % 4];
                line(vc, e, cx, apexY, cz, a[0], baseY, a[1], c);
                line(vc, e, a[0], baseY, a[1], b[0], baseY, b[1], c);
            }
        }

        // 7) Mały box na głowie
        if (Visual.HEAD.enabled) {
            double eyeY = pos.y + t.getEyeHeight(t.getPose());
            double h = 0.3;
            box(vc, e, new Box(cx - h, eyeY - h, cz - h, cx + h, eyeY + h, cz + h), Visual.HEAD.color());
        }

        ms.pop();
    }

    private static void box(VertexConsumer vc, MatrixStack.Entry e, Box b, int c) {
        double x1 = b.minX, y1 = b.minY, z1 = b.minZ, x2 = b.maxX, y2 = b.maxY, z2 = b.maxZ;
        // dół
        line(vc, e, x1, y1, z1, x2, y1, z1, c);
        line(vc, e, x2, y1, z1, x2, y1, z2, c);
        line(vc, e, x2, y1, z2, x1, y1, z2, c);
        line(vc, e, x1, y1, z2, x1, y1, z1, c);
        // góra
        line(vc, e, x1, y2, z1, x2, y2, z1, c);
        line(vc, e, x2, y2, z1, x2, y2, z2, c);
        line(vc, e, x2, y2, z2, x1, y2, z2, c);
        line(vc, e, x1, y2, z2, x1, y2, z1, c);
        // pionowe
        line(vc, e, x1, y1, z1, x1, y2, z1, c);
        line(vc, e, x2, y1, z1, x2, y2, z1, c);
        line(vc, e, x2, y1, z2, x2, y2, z2, c);
        line(vc, e, x1, y1, z2, x1, y2, z2, c);
    }

    private static void line(VertexConsumer vc, MatrixStack.Entry e,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2, int argb) {
        float dx = (float) (x2 - x1), dy = (float) (y2 - y1), dz = (float) (z2 - z1);
        float len = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-6f) return;
        dx /= len; dy /= len; dz /= len;
        vc.vertex(e, (float) x1, (float) y1, (float) z1).color(argb).normal(e, dx, dy, dz);
        vc.vertex(e, (float) x2, (float) y2, (float) z2).color(argb).normal(e, dx, dy, dz);
    }
}
