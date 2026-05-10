package thunder.hack.features.modules.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

import static thunder.hack.core.Managers.FRIEND;

public class PenisESP extends Module {

    public PenisESP() {
        super("MathDick", Category.RENDER);
    }

    private final Setting<Boolean> onlyOwn =
            new Setting<>("OnlyOwn", false);

    private final Setting<Float> ballSize =
            new Setting<>("BallSize", 0.12f, 0.05f, 0.5f);

    private final Setting<Float> penisSize =
            new Setting<>("PenisSize", 1.5f, 0.1f, 5.0f);

    private final Setting<Float> friendSize =
            new Setting<>("FriendSize", 2.0f, 0.1f, 5.0f);

    private final Setting<Float> enemySize =
            new Setting<>("EnemySize", 1.0f, 0.1f, 5.0f);

    private final Setting<Integer> gradation =
            new Setting<>("Gradation", 40, 12, 100);

    private final Setting<ColorSetting> penisColor =
            new Setting<>("PenisColor",
                    new ColorSetting(new Color(198, 134, 66, 220)));

    private final Setting<ColorSetting> headColor =
            new Setting<>("HeadColor",
                    new ColorSetting(new Color(220, 110, 120, 230)));

    @Override
    public void onRender2D(DrawContext context) {

        if (mc.world == null || mc.player == null)
            return;

        for (PlayerEntity player : mc.world.getPlayers()) {

            if (onlyOwn.getValue() && player != mc.player)
                continue;

            double size = FRIEND.isFriend(player)
                    ? friendSize.getValue()
                    : (player != mc.player
                    ? enemySize.getValue()
                    : penisSize.getValue());

            Vec3d base = getBase(player);

            float yaw = player.getYaw();
            float pitch = player.getPitch();

            Vec3d dir = Vec3d.fromPolar(pitch, yaw).normalize();

            Vec3d pelvis = base
                    .add(0, player.getHeight() / 2.35, 0)
                    .add(dir.multiply(0.08));

            double separation = ballSize.getValue() * 0.9;

            Vec3d rightVec = getRightVector(dir);

            Vec3d leftBall = pelvis
                    .add(rightVec.multiply(-separation))
                    .add(0, -0.04, 0);

            Vec3d rightBall = pelvis
                    .add(rightVec.multiply(separation))
                    .add(0, -0.04, 0);

            drawBall(
                    ballSize.getValue(),
                    leftBall,
                    penisColor.getValue().getColorObject()
            );

            drawBall(
                    ballSize.getValue(),
                    rightBall,
                    penisColor.getValue().getColorObject()
            );

            drawPenis(player, context.getMatrices(), size, pelvis);
        }
    }

    public Vec3d getBase(Entity entity) {

        double x = entity.prevX +
                ((entity.getX() - entity.prevX)
                        * Render3DEngine.getTickDelta());

        double y = entity.prevY +
                ((entity.getY() - entity.prevY)
                        * Render3DEngine.getTickDelta());

        double z = entity.prevZ +
                ((entity.getZ() - entity.prevZ)
                        * Render3DEngine.getTickDelta());

        return new Vec3d(x, y, z);
    }

    public void drawPenis(
            PlayerEntity player,
            MatrixStack matrices,
            double size,
            Vec3d start
    ) {

        float yaw = player.getYaw();
        float pitch = player.getPitch();

        Vec3d dir = Vec3d.fromPolar(pitch, yaw).normalize();

        double time = System.currentTimeMillis() / 180.0;

        double idleSway = Math.sin(time) * 0.02;

        double speed = player.getVelocity().horizontalLength();

        double bounce =
                Math.sin(System.currentTimeMillis() / 90.0)
                        * speed
                        * 0.12;

        start = start.add(0, idleSway + bounce, 0);

        int segments = 24;

        for (int i = 0; i < segments; i++) {

            float progress = i / (float) segments;

            float nextProgress =
                    (i + 1f) / segments;

            Vec3d p1 =
                    start.add(dir.multiply(size * progress));

            Vec3d p2 =
                    start.add(dir.multiply(size * nextProgress));

            double radius =
                    0.13 - (progress * 0.05);

            drawRing(
                    p1,
                    dir,
                    radius,
                    penisColor.getValue().getColorObject()
            );

            drawRing(
                    p2,
                    dir,
                    radius,
                    penisColor.getValue().getColorObject()
            );

            Render3DEngine.drawLine(
                    p1,
                    p2,
                    penisColor.getValue().getColorObject()
            );
        }

        Vec3d tip =
                start.add(dir.multiply(size));

        drawBall(
                0.14,
                tip,
                headColor.getValue().getColorObject()
        );
    }

    public void drawRing(
            Vec3d center,
            Vec3d dir,
            double radius,
            Color color
    ) {

        Vec3d up = new Vec3d(0, 1, 0);

        Vec3d right =
                dir.crossProduct(up).normalize();

        if (right.lengthSquared() < 0.01)
            right = new Vec3d(1, 0, 0);

        Vec3d forward =
                dir.crossProduct(right).normalize();

        int points = gradation.getValue();

        for (int i = 0; i < points; i++) {

            double angle1 =
                    (Math.PI * 2 * i) / points;

            double angle2 =
                    (Math.PI * 2 * (i + 1)) / points;

            Vec3d v1 = center
                    .add(right.multiply(
                            Math.cos(angle1) * radius))
                    .add(forward.multiply(
                            Math.sin(angle1) * radius));

            Vec3d v2 = center
                    .add(right.multiply(
                            Math.cos(angle2) * radius))
                    .add(forward.multiply(
                            Math.sin(angle2) * radius));

            Render3DEngine.drawLine(v1, v2, color);
        }
    }

    public void drawBall(
            double radius,
            Vec3d pos,
            Color color
    ) {

        int detail = gradation.getValue();

        for (float alpha = 0;
             alpha < Math.PI;
             alpha += (float) Math.PI / detail) {

            for (float beta = 0;
                 beta < Math.PI * 2;
                 beta += (float) Math.PI / detail) {

                double x1 =
                        pos.x +
                                radius
                                        * Math.cos(beta)
                                        * Math.sin(alpha);

                double y1 =
                        pos.y +
                                radius
                                        * Math.sin(beta)
                                        * Math.sin(alpha);

                double z1 =
                        pos.z +
                                radius
                                        * Math.cos(alpha);

                double x2 =
                        pos.x +
                                radius
                                        * Math.cos(beta)
                                        * Math.sin(alpha + Math.PI / detail);

                double y2 =
                        pos.y +
                                radius
                                        * Math.sin(beta)
                                        * Math.sin(alpha + Math.PI / detail);

                double z2 =
                        pos.z +
                                radius
                                        * Math.cos(alpha + Math.PI / detail);

                Render3DEngine.drawLine(
                        new Vec3d(x1, y1, z1),
                        new Vec3d(x2, y2, z2),
                        color
                );
            }
        }
    }

    public Vec3d getRightVector(Vec3d dir) {

        Vec3d up = new Vec3d(0, 1, 0);

        Vec3d right =
                dir.crossProduct(up);

        if (right.lengthSquared() < 0.001)
            right = new Vec3d(1, 0, 0);

        return right.normalize();
    }
}
