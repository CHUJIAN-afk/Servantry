package first.summoner.common.minion.minion.goal.voidEater;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.sound.Playable;
import first.summoner.common.minion.minion.VoidEater;
import first.summoner.common.projectile.GodFlame;
import first.summoner.common.projectile.VoidEaterLaser;
import first.summoner.register.SummonerSoundRegister;
import first.summoner.utils.ParticleHelper;
import first.summoner.utils.Utils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 虚空吞噬者攻击目标。
 */
public class VoidEaterAttackGoal extends AttachmentEntityGoal<VoidEater> {

    private VoidEaterLaser laser = null;
    private AttackMode mode = AttackMode.GOD_DASH;
    private int combat = 0;
    private Vec3 prepPos = Vec3.ZERO;
    private boolean approaching = true;

    public VoidEaterAttackGoal(VoidEater minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.isHead() && minion.getTarget() != null;
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
                prepPos = Utils.getWanderPos(minion, prepPos, targetPos, 8, 0);
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
                    prepPos = Utils.getWanderPos(minion, prepPos, targetPos, 16, 0);
                } while (prepPos.distanceTo(minion.getPos()) < 8);
            }
            minion.orbitToward(prepPos, 120f, 0.4f);
            if (minion.getPos().distanceTo(prepPos) < 1) {
                approaching = false;
                prepPos = Vec3.ZERO;
            }
        } else {
            Player owner = minion.getOwner() instanceof Player player ? player : null;
            Level level = minion.getLevel();
            if (minion.getTickCount() % 5 == 0) {
                double dot = minion.getLookAngle().dot(targetPos.subtract(minion.getPos()).normalize());
                if (dot > 0 && Math.acos((float) Math.min(1, dot)) < Mth.DEG_TO_RAD * 30) {
                    Vec3 start = minion.getPos();
                    for (int i = 0; i < 9; i++) {
                        Vec3 direction = targetPos.offsetRandom(level.getRandom(), 2f).subtract(minion.getPos()).normalize();
                        GodFlame projectile = new GodFlame();
                        projectile.setOwner(minion.getOwner());
                        projectile.init(new PathNode(start, direction));
                        projectile.copyAttributes(minion);
                        projectile.setVelocity(direction.scale(3));
                        projectile.setDamage(minion.getDamage());
                        LyraHelper.get(owner).add(projectile);
                    }
                    Playable.play(SummonerSoundRegister.Laser, level, start, minion.getSoundSource());
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
                prepPos = Utils.getWanderPos(minion, prepPos, targetPos, 16, 2);
            }
            minion.orbitToward(prepPos, 120f, 0.4f);
            if (pos.distanceTo(prepPos) < 3) {
                approaching = false;
                combat = 0;
            }
        } else {
            if (laser == null || laser.isRemove()) {
                laser = new VoidEaterLaser();
                laser.setOwner(minion.getOwner());
                laser.init(minion.getCurrentPathNode());
                laser.copyAttributes(minion);
                laser.setDamage(minion.getDamage() * 2);
                laser.follow(minion);
                LyraHelper.get(minion.getOwner()).add(laser);
            }
            // 每 tick 跟随本体：同步朝向，并沿当前朝向延伸 48 格作为碰撞箱
            laser.setCurrentPathNode(minion.getCurrentPathNode());
            Vec3 dir = Vec3.directionFromRotation(minion.getCurrentPathNode().pitch(), minion.getCurrentPathNode().yaw());
            laser.setHitbox(pos, pos.add(dir.scale(48)), 0.4f);
            ParticleHelper.create(minion.getLevel())
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

    /** 目标丢失或切换到其它 Goal 时收起激光 */
    @Override
    public void stop() {
        removeLaser();
    }

    public void nextMode() {
        // 离开宇宙漩涡形态时收起激光
        if (mode == AttackMode.COSMIC_MAELSTROM) {
            removeLaser();
        }
        combat = 0;
        approaching = true;
        prepPos = Vec3.ZERO;
        mode = mode.next();
    }

    private void removeLaser() {
        if (laser != null) {
            laser.setRemove();
            laser = null;
        }
    }

    public enum AttackMode {
        GOD_DASH, HOLY_GOD_DASH, COSMIC_MAELSTROM;

        public AttackMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }
}
