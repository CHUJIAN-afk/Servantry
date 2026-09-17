package first.summoner.common.minion.sentry;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachmentEntity.AttachmentEntityDamageSource;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.projectile.CustomLaser;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 眼球激光塔哨兵 - 原地不动，有重力，面向目标持续射出激光。
 * <p>
 * 行为：
 * - 有目标时看向目标，以自身视角方向发射 16 格长、不能穿墙的淡红色激光
 * - 激光跟随视角移动，命中造成 10 无敌帧伤害
 * - 无目标时激光消失，看向玩家
 */
public class EyeLaserTurret extends MomentumMinion implements IBlockCollision<EyeLaserTurret> {

    private CustomLaser laser = null;
    private int chargeTicks = 0;

    public EyeLaserTurret() {
        super(SummonerAttachmentEntityRegister.EYE_LASER_TURRET);
    }

    @Override
    public int getSearchDistance() {
        return 16;
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && getPos().distanceToSqr(owner.position()) <= 128 * 128;
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = getTarget();
        if (target != null) {
            lookAtPos(target.getBoundingBox().getCenter());
        } else if (owner.getBoundingBox().getCenter().distanceTo(getPos()) < 2) {
            lookAtPos(owner.getEyePosition());
        }

        if (target != null) {
            updateLaser();
        } else if (laser != null) {
            laser.setRemove();
            laser = null;
        }
    }

    /**
     * 创建或更新激光：以塔的当前视角方向延伸 16 格，射线检测方块截断（不能穿墙）。
     * <p>
     * 充能机制（持续 3 秒 = 60 tick）：
     * - 激光半径从 0.1 逐渐变粗至基础半径的 3 倍
     * - 粒子数量逐渐增多至基础 3 倍
     * - 伤害从 50% 逐渐提升至 300%
     */
    private void updateLaser() {
        Vec3 pos = getPos();
        Vec3 dir = getLookAngle();
        Vec3 end = pos.add(dir.scale(Math.max(16, pos.distanceTo(getTarget().getBoundingBox().getCenter()))));
        HitResult hit = getLevel().clip(new ClipContext(pos, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        Vec3 hitPos = hit.getType() != HitResult.Type.MISS ? hit.getLocation() : end;

        if (laser == null || laser.isRemove()) {
            chargeTicks = Math.max(chargeTicks - 1, 0);
            laser = new CustomLaser();
            laser.setOwner(owner);
            laser.init(new PathNode(pos, getYaw(), getPitch(), 0));
            laser.copyAttributes(this);
            laser.setColor(0xFF6B6B);
            laser.setTickConsumer(l -> {
                if (l.getDamageSource() instanceof AttachmentEntityDamageSource attachmentDamageSource && attachmentDamageSource.getAttachmentEntity().isRemove()) {
                    l.setRemove();
                }
            });
            laser.setHitConsumer((l, hitContexts) -> {
                DamageSource source = getDamageSource();
                                for (var hitContext : hitContexts) {
                        InvincibleData.attack(hitContext.entity())
                                .attacker(l.getUuid())
                                .damageSource(source)
                                .damageAmount(l.getDamage())
                                .invincibleTime(5)
                                .apply();
                    }
            });
            LyraHelper.get(owner).add(laser);
        } else {
            chargeTicks = Math.min(chargeTicks + 1, 60);
        }
        float progress = chargeTicks / 60f;
        float radius = 0.1f + 0.2f * progress;
        int particleCount = 2 + (int) (4 * progress);
        int explosionCount = 6 + (int) (12 * progress);
        // 每 tick 跟随视角更新激光位置、朝向、半径与伤害（50% → 300%）
        laser.setCurrentPathNode(new PathNode(pos, getYaw(), getPitch(), 0));
        laser.setHitbox(pos, hitPos, radius);
        laser.setDamage(getDamage() * (0.5f + 2.5f * progress));
        // 向前喷射红色粒子（效果与藤壶一致，仅颜色不同，数量随充能增长至 3 倍）
        ParticleHelper.create(getLevel())
                .generic(GenericParticleBuilder.create()
                        .centerColor(0xff6b6b)
                        .edgeColor(0xff2a2a)
                        .lifetime(8)
                        .lifetimeRandom(6)
                        .spin(0.15f)
                        .spinRandom(0.1F)
                        .friction(0.55F)
                        .scale(0.03f)
                        .scaleRandom(0.01f)
                )
                .pos(pos)
                .offset(0.15)
                .velocity(dir)
                .count(particleCount)
                .speed(1.2)
                .spread(0.25)
                .emit();
        // 激光末端爆炸效果（与月亮传送门一致，遮挡激光末端渲染，数量随充能增长至 3 倍）
        Vec3 direction = hitPos.offsetRandom(getRandom(), 2).subtract(hitPos).normalize();
        ParticleHelper.create(getLevel())
                .generic(GenericParticleBuilder.create()
                        .centerColor(0xff6b6b)
                        .edgeColor(0xff2a2a)
                        .lifetime(5)
                        .lifetimeRandom(5)
                        .spin(0.1f)
                        .spinRandom(0.05F)
                        .friction(0.75F)
                        .scale(0.045f)
                        .scaleRandom(0.005f)
                )
                .pos(hitPos)
                .offset(0.35)
                .velocity(direction)
                .count(explosionCount)
                .speed(0.35)
                .spread(Math.TAU)
                .emit();
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.4, -0.4, -0.4, 0.4, 0.4, 0.4);
    }

        public void dimensionChange() {
        setRemove();
    }
}
