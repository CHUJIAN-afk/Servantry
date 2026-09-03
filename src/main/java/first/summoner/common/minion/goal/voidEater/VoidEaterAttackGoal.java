package first.summoner.common.minion.goal.voidEater;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.minion.Minion;
import first.lyra.common.minion.MinionDamageSource;
import first.lyra.common.minion.MinionGoal;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.sound.Playable;
import first.summoner.common.minion.VoidEater;
import first.summoner.common.projectile.CustomLaser;
import first.summoner.common.projectile.GodFlame;
import first.summoner.register.SummonerSoundRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 虚空吞噬者攻击目标。
 */
public class VoidEaterAttackGoal extends MinionGoal<VoidEater> {

    private CustomLaser laser = null;
    private AttackMode mode = AttackMode.GOD_DASH;
    private int combat = 0;
    private Vec3 prepPos = Vec3.ZERO;
    private boolean approaching = true;

    public VoidEaterAttackGoal(VoidEater minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.isHead() && minion.isTarget(minion.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
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
                prepPos = minion.getWanderPos(prepPos, targetPos, 8, 0);
            }
            minion.orbitToward(prepPos, 120f, 0.4f);
            if (minion.getPos().distanceTo(prepPos) < 1) {
                approaching = false;
                prepPos = Vec3.ZERO;
            }
        } else {
            minion.orbitToward(targetPos, 150f, 1f, false);
            if (minion.getPos().distanceTo(targetPos) < 2) {
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
                    prepPos = minion.getWanderPos(prepPos, targetPos, 16, 0);
                } while (prepPos.distanceTo(minion.getPos()) < 8);
            }
            minion.orbitToward(prepPos, 120f, 0.4f);
            if (minion.getPos().distanceTo(prepPos) < 1) {
                approaching = false;
                prepPos = Vec3.ZERO;
            }
        } else {
            Player owner = minion.getOwner();
            if (minion.getTickCount() % 5 == 0) {
                double dot = minion.getLookAngle().dot(targetPos.subtract(minion.getPos()).normalize());
                if (dot > 0 && Math.acos((float) Math.min(1, dot)) < Mth.DEG_TO_RAD * 30) {
                    Vec3 start = minion.getPos();
                    Playable.play(SummonerSoundRegister.Laser, owner.level(), start, owner.getSoundSource());
                    for (int i = 0; i < 9; i++) {
                        Vec3 direction = targetPos.offsetRandom(owner.getRandom(), 2f).subtract(minion.getPos()).normalize();
                        GodFlame projectile = new GodFlame(minion.getDamageSource(), start, direction.scale(3));
                        projectile.setDamage(minion.getDamage() * 0.5f);
                        projectile.join(owner);
                    }
                }
            }
            minion.orbitToward(targetPos, 120f, 0.25f, false);
            if (minion.getPos().distanceTo(targetPos) < 4) {
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
        Vec3 pos = minion.getPos();
        if (approaching) {
            if (prepPos.equals(Vec3.ZERO)) {
                prepPos = minion.getWanderPos(prepPos, targetPos, 16, 2);
            }
            minion.orbitToward(prepPos, 120f, 0.4f);
            if (pos.distanceTo(prepPos) < 3) {
                approaching = false;
                combat = 0;
            }
        } else {
            if (laser == null || laser.isRemove()) {
                laser = new CustomLaser(minion.getDamageSource(), minion.getCurrentPathNode(), 0x6f19d4);
                laser.setDamage(minion.getDamage() * 2);
                Player owner = minion.getOwner();
                laser.join(owner);
                laser.setTickConsumer(laserProjectile -> {
                    Minion minion = null;
                    if (laserProjectile.getDamageSource() instanceof MinionDamageSource minionDamageSource) {
                        minion = minionDamageSource.getMinion();
                        if (minion.isRemove()) {
                            laserProjectile.setRemove();
                            return;
                        }
                        if (minion instanceof VoidEater voidEater) {
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
                    if (minion == null) {
                        laserProjectile.setRemove();
                        return;
                    }
                    laserProjectile.setCurrentPathNode(minion.getCurrentPathNode());
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
                        for (IEntityCollision.HitContext hitContext : hitContexts) {
                            LivingEntity living = hitContext.entity();
                            InvincibleData.attack(living)
                                    .attacker(laserProjectile.getUuid())
                                    .damageSource(source)
                                    .damageAmount(laserProjectile.getDamage())
                                    .apply();
                            Vec3 hitPoint = living.getBoundingBox().getCenter();
                            Vec3 direction = hitPoint.offsetRandom(owner.getRandom(), 2).subtract(hitPoint).normalize();
                            GenericParticleBuilder genericParticleBuilder = GenericParticleBuilder.create()
                                             .centerColor(0x841af3)
                                             .edgeColor(0x6f19d4);
                            ParticleHelper.create(owner.level())
                                    .generic(genericParticleBuilder
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
            ParticleHelper.create(minion.getOwner().level())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0x841af3)
                                     .edgeColor(0x6f19d4)
                                     .lifetime(10)
                                     .lifetimeRandom(10)
                                     .spin(0.1f)
                                     .spinRandom(0.05F)
                                     .friction(0.75F)
                                     .scale(0.045f)
                                     .scaleRandom(0.005f)
                    )
                    .pos(minion.getPos())
                    .offset(0.5f)
                    .velocity(minion.getLookAngle())
                    .count(5)
                    .speed(1)
                    .spread(0.5)
                    .emit();
            minion.orbitToward(targetPos, 90f, 0.025f, false);
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
