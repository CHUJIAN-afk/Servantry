package first.servantry.common.servent.goal;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.projectile.StardustProjectile;
import first.servantry.common.servent.StardustCell;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.ParticleRegister;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 星尘细胞攻击Goal。
 * <p>
 * 追踪目标并在射击冷却结束时发射星细胞射弹。
 * </p>
 */
public class StardustCellAttackGoal extends ServantGoal<StardustCell> {

    public StardustCellAttackGoal(StardustCell servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        LivingEntity target = servant.getTarget();
        return target != null && servant.isTarget(target);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = servant.getTarget();
        return target != null && servant.isTarget(target);
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        if (target == null || !target.isAlive()) return;

        Player owner = servant.getOwner();
        int order = owner.getData(AttachmentRegister.EntityData).getOrder(servant);
        Vec3 anchor = servant.getHaloAnchorPos(owner, target, order);

        Vec3 toAnchor = anchor.subtract(servant.getPos());
        double dist = toAnchor.length();
        if (dist > 0.05) {
            double force = Math.min(dist * 0.08, 0.4);
            servant.applyForce(toAnchor.normalize().scale(force));
        }

        // 动态摩擦与限速
        float friction = dist < 1.5 ? 0.55f : 0.85f;
        float maxSpd = (float) Math.min(1.8, dist * 0.8 + 0.05);
        Vec3 vel = servant.getVelocity();
        if (vel.lengthSqr() > maxSpd * maxSpd) vel = vel.normalize().scale(maxSpd);
        servant.setVelocity(vel.scale(friction));

        // 朝向目标
        Vec3 faceDir = target.position().subtract(servant.getPos()).normalize();
        servant.setDesiredRotation((float) Math.toDegrees(Math.atan2(-faceDir.x, faceDir.z)), servant.getDesiredPitch(), servant.getDesiredRoll());

        // 射击冷却
        if (servant.getShootCooldown() <= 0) {
            shootAtTarget(owner, target);
            servant.setShootCooldown(12 + owner.getRandom().nextInt(4));
        }
    }

    private void shootAtTarget(Player owner, LivingEntity target) {
        Vec3 start = servant.getPos();
        // 创建并发射星细胞射弹
        StardustProjectile projectile = new StardustProjectile(owner.getUUID(), servant.getUuid(), start, target);
        projectile.life = 10;
        owner.getData(AttachmentRegister.EntityData).addProjectile(projectile);
        // 后坐力
        Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
        spawnShootParticles((ServerLevel) owner.level(), start, direction);
        servant.applyForce(direction.scale(-0.5));
    }

    public static void spawnShootParticles(ServerLevel level, Vec3 pos, Vec3 direction) {
        RandomSource rand = level.getRandom();
        int particleCount = 2 + rand.nextInt(4);

        for (int i = 0; i < particleCount; i++) {
            double spreadAngle = (rand.nextDouble() - 0.5) * 0.8;
            double rollAngle = rand.nextDouble() * Math.PI * 2.0;
            double speed = 0.15 + rand.nextDouble() * 0.25;

            // 基于前进方向散射
            double vx = direction.x * speed + Math.cos(rollAngle) * spreadAngle * 0.1;
            double vy = direction.y * speed + Math.sin(rollAngle) * spreadAngle * 0.1;
            double vz = direction.z * speed + rand.nextDouble() * 0.1;

            level.sendParticles(
                    ParticleRegister.StardustScatter.get(),
                    pos.x, pos.y, pos.z,
                    0,
                    vx, vy, vz,
                    1.0
            );
        }
    }

    @Override
    public void stop() {
        servant.setTarget(null);
    }
}