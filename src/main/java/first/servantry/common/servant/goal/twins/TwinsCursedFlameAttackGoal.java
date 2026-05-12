package first.servantry.common.servant.goal.twins;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.servant.ai.ServantGoal;
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
            Vec3 motionDir = servant.getVelocity().normalize();
            float targetYaw = (float) Math.toDegrees(Math.atan2(-motionDir.x, motionDir.z));
            float targetPitch = (float) Math.toDegrees(Math.asin(-motionDir.y));
            servant.setDesiredRotation(targetYaw, targetPitch, 0);
            if (servant.getTrailTimer() > 0) {
                ParticleHelper.create(owner.level())
                        .generic(builder -> builder
                                .color(0x8a0801)
                                .lifetime(10)
                                .lifetimeRandom(4)
                                .friction(0.75F)
                                .spin(0.1F)
                                .spinRandom(0.05F)
                        )
                        .pos(servant.getPos())
                        .velocity(servant.getVelocity().scale(-0.1))
                        .count(2)
                        .speed(0.05)
                        .spread(0.5)
                        .emit();
            }
            if (--cooldown <= 0 && targetPos.distanceToSqr(servant.getPos()) < 16 * 16) {
                emit += 10;
                if (emit > 60) {
                    emited = true;
                } else {
                    cooldown = 10;
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
                }
                Vec3 toAnchor = wanderTarget.subtract(servant.getPos());
                double dist = toAnchor.length();
                if (dist > 0.05) {
                    double force = Math.min(dist * 0.08, 0.4);
                    servant.applyForce(toAnchor.normalize().scale(force));
                }
                servant.setDesiredRotation(targetPos);
                InvincibleData.criteriaAttack(target, servant.getUuid(), 2, servant.getDamageSource(), servant.getDamage(), InvincibleData.Type.PARTIAL);
                ParticleHelper.create(owner.level())
                        .generic(builder -> builder
                                .color(0x28ff09)
                                .colorRandom(0.2f, 0f, 0.2f)
                                .lifetime(5)
                                .lifetimeRandom(15)
                                .friction(0.85F)
                                .spin(0.2F)
                                .spinRandom(0.1F)
                        )
                        .pos(servant.getPos())
                        .velocity(targetPos.subtract(servant.getPos()))
                        .count(5)
                        .speed(1)
                        .spread(0.2)
                        .emit();
            } else {
                emited = false;
            }
        }
    }

}