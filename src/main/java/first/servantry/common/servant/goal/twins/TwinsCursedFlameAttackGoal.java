package first.servantry.common.servant.goal.twins;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.particle.GenericParticleBuilder;
import first.servantry.common.projectile.DemonFlameProjectile;
import first.servantry.common.servant.Twins;
import first.servantry.utils.ParticleHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 魔焰眼冲刺攻击Goal。
 * <p>
 * 咒焰眼（Spazmatism）的攻击行为：
 * <ul>
 *   <li>冷却期间：看向目标并逐渐加速靠近</li>
 *   <li>冲刺时：施加巨大冲量冲向目标身后</li>
 *   <li>冷却时长：固定14tick</li>
 *   <li>冲刺条件：距离目标小于16格</li>
 * </ul>
 * </p>
 */
public class TwinsCursedFlameAttackGoal extends ServantGoal<Twins> {

    private Vec3 wanderTarget = Vec3.ZERO;
    private int cooldown;
    private int emit = 0;
    private boolean emited = false;

    public TwinsCursedFlameAttackGoal(Twins twins) {
        super(twins);
    }

    @Override
    public boolean canUse() {
        return !servant.isLaserEye() && servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 toTarget = targetPos.subtract(servant.getPos());
        Player owner = servant.getOwner();
        if (!emited) {
            servant.lookAtDirection(servant.getVelocity().normalize());
            if (--cooldown <= 0) {
                emit += 5;
                if (emit > 40) {
                    emited = true;
                } else {
                    cooldown = 8 + owner.getRandom().nextInt(-2, 2);
                    Vec3 direction = toTarget.offsetRandom(owner.getRandom(), (float) target.getBoundingBox().getSize()).normalize();
                    servant.applyForce(direction.scale(2));
                    servant.setTrailTimer(10);
                }
            }
        } else {
            if (--emit > 0) {
                // 计算环绕位置
                if (wanderTarget.equals(Vec3.ZERO) || owner.getRandom().nextDouble() < 0.025 || targetPos.distanceToSqr(servant.getPos()) > 6 * 6) {
                    wanderTarget = targetPos.offsetRandom(target.getRandom(), (float) target.getBoundingBox().getSize() * 6);
                    wanderTarget.add(0, target.getBoundingBox().getSize() * 12, 0);
                    double height = target.position().y() + target.getBoundingBox().getYsize() / 2;
                    while (wanderTarget.y() < height) {
                        wanderTarget = wanderTarget.add(0, 1, 0);
                    }
                }
                Vec3 toAnchor = wanderTarget.subtract(servant.getPos());
                double dist = toAnchor.length();
                if (dist > 0.05) {
                    double force = Math.min(dist * 0.08, 0.4);
                    servant.applyForce(toAnchor.normalize().scale(force));
                }
                servant.lookAtPos(targetPos);
                Vec3 direction = targetPos.subtract(servant.getPos()).normalize();
                DemonFlameProjectile demonFlameProjectile = new DemonFlameProjectile(servant.getDamageSource(), servant.getPos().add(direction.scale(-1)), direction);
                demonFlameProjectile.join(owner);
                // 喷射粒子 - 诅咒焰调色，高速小角度散射
                ParticleHelper.create(owner.level())
                        .generic(GenericParticleBuilder.create()
                                .color(0x24d509)
                                .edgeColor(0x1FF109)
                                .colorRandom(0.2f, 0f, 0.2f)
                                .lifetime(5)
                                .lifetimeRandom(15)
                                .spin(0.4f)
                                .spinRandom(0.1F)
                                .friction(0.85F)
                                .scale(0.035f)
                                .scaleRandom(0.005f)
                        )
                        .pos(servant.getPos())
                        .offset(0.025)
                        .velocity(targetPos.subtract(servant.getPos()))
                        .count(4)
                        .speed(1)
                        .spread(0.25)
                        .emit();
            } else {
                emited = false;
            }
        }
    }

}