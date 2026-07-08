package first.servantry.common.servant.goal.voidEater;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.particle.GenericParticleBuilder;
import first.servantry.common.projectile.CustomLaserProjectile;
import first.servantry.common.projectile.GodFlameProjectile;
import first.servantry.common.servant.VoidEater;
import first.servantry.register.SoundRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 虚空吞噬者攻击目标。
 */
public class VoidEaterAttackGoal extends ServantGoal<VoidEater> {

    private CustomLaserProjectile laser = null;
    private AttackMode mode = AttackMode.GOD_DASH;
    private int combat = 0;
    private Vec3 prepPos = Vec3.ZERO;
    private boolean approaching = true;

    public VoidEaterAttackGoal(VoidEater servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isHead() && servant.isTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        switch (mode) {
            case GOD_DASH -> tickGodDash(targetPos);
            case HOLY_GOD_DASH -> tickHolyGodDash(targetPos);
            case COSMIC_MAELSTROM -> tickCosmicMaelstrom(targetPos);
        }
    }

    private void tickGodDash(Vec3 targetPos) {
        if (approaching) {
            if (prepPos.equals(Vec3.ZERO)) {
                prepPos = servant.getWanderPos(prepPos, targetPos, 8, 0);
            }
            servant.orbitToward(prepPos, 120f, 0.4f);
            if (servant.getPos().distanceTo(prepPos) < 1) {
                approaching = false;
                prepPos = Vec3.ZERO;
            }
        } else {
            servant.orbitToward(targetPos, 150f, 1f, false);
            if (servant.getPos().distanceTo(targetPos) < 2) {
                combat++;
                if (combat >= 6) {
                    nextMode();
                } else {
                    approaching = true;
                }
            }
        }
    }

    private void tickHolyGodDash(Vec3 targetPos) {
        if (approaching) {
            if (prepPos.equals(Vec3.ZERO)) {
                do {
                    prepPos = servant.getWanderPos(prepPos, targetPos, 16, 0);
                } while (prepPos.distanceTo(servant.getPos()) < 8);
            }
            servant.orbitToward(prepPos, 120f, 0.4f);
            if (servant.getPos().distanceTo(prepPos) < 1) {
                approaching = false;
                prepPos = Vec3.ZERO;
            }
        } else {
            Player owner = servant.getOwner();
            if (owner.tickCount % 5 == 0) {
                double dot = servant.getLookAngle().dot(targetPos.subtract(servant.getPos()).normalize());
                if (dot > 0 && Math.acos((float) Math.min(1, dot)) < Mth.DEG_TO_RAD * 30) {
                    Vec3 start = servant.getPos();
                    owner.level().playSound(null, start.x(), start.y(), start.z(), SoundRegister.Laser.get(), owner.getSoundSource());
                    for (int i = 0; i < 9; i++) {
                        Vec3 direction = targetPos.offsetRandom(owner.getRandom(), 2f).subtract(servant.getPos()).normalize();
                        GodFlameProjectile projectile = new GodFlameProjectile(servant.getDamageSource(), start, direction.scale(3));
                        projectile.copyDamageData(servant);
                        projectile.setDamage(servant.getDamage() * 0.5f);
                        projectile.join(owner);
                    }
                }
            }
            servant.orbitToward(targetPos, 120f, 0.25f, false);
            if (servant.getPos().distanceTo(targetPos) < 4) {
                combat++;
                if (combat >= 6) {
                    nextMode();
                } else {
                    approaching = true;
                }
            }
        }
    }

    private void tickCosmicMaelstrom(Vec3 targetPos) {
        Vec3 pos = servant.getPos();
        if (approaching) {
            if (prepPos.equals(Vec3.ZERO)) {
                prepPos = servant.getWanderPos(prepPos, targetPos, 16, 2);
            }
            servant.orbitToward(prepPos, 120f, 0.4f);
            if (pos.distanceTo(prepPos) < 3) {
                approaching = false;
                combat = 0;
            }
        } else {
            if (laser == null || laser.isRemove()) {
                laser = new CustomLaserProjectile(servant.getDamageSource(), servant.getCurrentPathNode(), 0x6f19d4);
                laser.copyDamageData(servant);
                laser.setDamage(servant.getDamage() * 2);
                Player owner = servant.getOwner();
                laser.join(owner);
                laser.setTickConsumer(laserProjectile -> {
                    Servant servant = null;
                    if (laserProjectile.getDamageSource() instanceof ServantDamageSource servantDamageSource) {
                        servant = servantDamageSource.getServant();
                        if (servant.isRemove()) {
                            laserProjectile.setRemove();
                            return;
                        }
                        if (servant instanceof VoidEater voidEater) {
                            if (voidEater.getGoalSelector().getCurrentGoal() instanceof VoidEaterAttackGoal voidEaterAttackGoal) {
                                if (voidEaterAttackGoal.mode != AttackMode.COSMIC_MAELSTROM) {
                                    laserProjectile.setRemove();
                                    return;
                                }
                            } else {
                                laserProjectile.setRemove();
                                return;
                            }
                        }
                    }
                    if (servant == null) {
                        laserProjectile.setRemove();
                        return;
                    }
                    laserProjectile.setCurrentPathNode(servant.getCurrentPathNode());
                    // 沿当前朝向射线追踪方块，计算碰撞箱
                    float pYaw = laserProjectile.getCurrentPathNode().yaw();
                    float pPitch = laserProjectile.getCurrentPathNode().pitch();
                    Vec3 dir = Vec3.directionFromRotation(pPitch, pYaw);
                    Vec3 hitPos = pos.add(dir.scale(48));
                    laserProjectile.setHitbox(pos, hitPos, 0.4f);
                });
                laser.setHitConsumer((laserProjectile, hitContexts) -> {
                    DamageSource source = laserProjectile.getDamageSource();
                    if (source != null) {
                        for (ICollideAttack.HitContext hitContext : hitContexts) {
                            LivingEntity living = hitContext.entity();
                            InvincibleData.attack(living)
                                    .attacker(laserProjectile.getUuid())
                                    .damageSource(source)
                                    .damageAmount(laserProjectile.getDamage())
                                    .apply();
                            Vec3 hitPoint = living.getBoundingBox().getCenter();
                            Vec3 direction = hitPoint.offsetRandom(owner.getRandom(), 2).subtract(hitPoint).normalize();
                            ParticleHelper.create(owner.level())
                                    .generic(GenericParticleBuilder.create()
                                                     .color(0x841af3)
                                                     .edgeColor(0x6f19d4)
                                                     .colorRandom(0, 0.2F, 0.2F)
                                                     .lifetime(5)
                                                     .lifetimeRandom(5)
                                                     .spin(0.1f)
                                                     .spinRandom(0.05F)
                                                     .friction(0.75F)
                                                     .scale(0.045f)
                                                     .scaleRandom(0.005f)
                                    )
                                    .pos(hitPoint)
                                    .offset(0.35)
                                    .velocity(direction)
                                    .count(2)
                                    .speed(0.65)
                                    .spread(2)
                                    .emit();
                        }
                    }
                });
            }
            ParticleHelper.create(servant.getOwner().level())
                    .generic(GenericParticleBuilder.create()
                                     .color(0x841af3)
                                     .edgeColor(0x6f19d4)
                                     .lifetime(10)
                                     .lifetimeRandom(10)
                                     .spin(0.1f)
                                     .spinRandom(0.05F)
                                     .friction(0.75F)
                                     .scale(0.045f)
                                     .scaleRandom(0.005f)
                    )
                    .pos(servant.getPos())
                    .offset(0.5f)
                    .velocity(servant.getLookAngle())
                    .count(5)
                    .speed(1)
                    .spread(0.5)
                    .emit();
            servant.orbitToward(targetPos, 90f, 0.025f, false);
            combat++;
            if (combat >= 100) {
                nextMode();
            }
        }
    }

    public void nextMode() {
        combat = 0;
        approaching = true;
        prepPos = Vec3.ZERO;
        mode = mode.next();
    }

    public enum AttackMode {
        GOD_DASH, HOLY_GOD_DASH, COSMIC_MAELSTROM;

        public AttackMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }
}
