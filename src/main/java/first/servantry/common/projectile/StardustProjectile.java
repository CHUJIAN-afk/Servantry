package first.servantry.common.projectile;

import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.projectile.Projectile;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.servant.Servant;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.MobEffectRegister;
import first.servantry.register.ParticleRegister;
import first.servantry.register.ProjectileRegister;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * 星细胞射弹 - 由星尘细胞仆从发射的追踪射弹。
 * <p>
 * 状态流转：FLYING（飞行追踪） -> ATTACHED（黏贴在目标身上） -> DEAD（死亡移除）。
 * </p>
 * <p>
 * 特性：
 * <ul>
 *   <li>追踪目标直到命中</li>
 *   <li>命中后黏贴在目标碰撞箱内部随机位置</li>
 *   <li>施加细胞寄生效果（可叠加）</li>
 *   <li>目标死亡时爆裂粒子消失</li>
 * </ul>
 * </p>
 */
public class StardustProjectile extends Projectile {

    // ===================== 常量 =====================

    /** 细胞寄生效果持续时间（tick） */
    private static final int PARASITISM_DURATION = 100;

    /** 细胞寄生效果最大等级 */
    private static final int MAX_PARASITISM_LEVEL = 10;

    // ===================== 构造器 =====================

    public StardustProjectile() {
        super();
    }

    public StardustProjectile(UUID ownerUuid, UUID sourceServantUuid, Vec3 startPos, LivingEntity target) {
        super(startPos, target);
        setOwnerUuid(ownerUuid);
        setSourceServantUuid(sourceServantUuid);
    }

    // ===================== Tick行为 =====================

    @Override
    public void tickBehavior(Player owner) {
        switch (getState()) {
            case FLYING -> tickFlying(owner);
            case ATTACHED -> tickAttached(owner);
            case DEAD -> { }
        }

        // 远距离检测：超过最大距离时消失
        if (getPos().distanceToSqr(owner.position()) > getMaxDistance() * getMaxDistance()) {
            spawnScatterParticles((ServerLevel) owner.level(), getPos(), 2, 0.1);
            markForRemoval();
        }
    }

    /**
     * 飞行状态：追踪目标、检测命中。
     */
    private void tickFlying(Player owner) {
        ServerLevel level = (ServerLevel) owner.level();
        LivingEntity target = getCachedTarget(level);

        // 目标消失
        if (target == null || !target.isAlive()) {
            spawnScatterParticles(level, getPos(), 2, 0.1);
            markForRemoval();
            return;
        }

        Vec3 targetCenter = target.getBoundingBox().getCenter();
        double dist = getPos().distanceTo(targetCenter);

        // 命中检测
        if (dist < getHitRadius() + target.getBbWidth() / 2.0) {
            onHit(owner, target);
            return;
        }

        // 追踪：施加指向目标的力
        if (life >= 10) {
            Vec3 trackingDir = targetCenter.subtract(getPos()).normalize();
            double trackingForce = Math.min(dist * 0.1, 0.3);
            applyForce(trackingDir.scale(trackingForce));
            // 更新朝向跟随速度方向
            Vec3 vel = getVelocity();
            if (vel.lengthSqr() > 0.01) {
                Vec3 dir = vel.normalize();
                setDesiredRotation((float) Math.toDegrees(Math.atan2(-dir.x, dir.z)), (float) Math.toDegrees(Math.atan2(-dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z))), getDesiredRoll());
            }
            setTrailTimer(getTrailDuration());
        }
    }

    /**
     * 黏贴状态：跟随目标、检测目标消失。
     * <p>
     * 目标死亡时：如果来源星细胞仆从有其他目标，则产生两个新星细胞向新目标发起攻击。
     * </p>
     */
    private void tickAttached(Player owner) {
        ServerLevel level = (ServerLevel) owner.level();
        LivingEntity target = getCachedAttachedTarget(level);

        // 目标死亡
        if (target == null || !target.isAlive()) {
            // 尝试找到来源仆从并检查是否有新目标
            Servant sourceServant = findServantByUuid(owner, getSourceServantUuid());
            if (sourceServant != null && owner.getData(AttachmentRegister.EntityData).getProjectiles().size() < 500) {
                level.getEntitiesOfClass(LivingEntity.class, new AABB(getPos(), getPos()).inflate(10)).stream()
                        .filter(sourceServant::isTarget)
                        .findAny()
                        .ifPresent(newTarget -> spawnNewProjectiles(owner, newTarget, sourceServant, level));
            }
            // 爆裂粒子消失
            spawnScatterParticles(level, getPos(), owner.getRandom().nextInt(8, 12), 0.5);
            markForRemoval();
            return;
        }

        // 跟随目标位置
        updateAttachedPosition();
    }

    /**
     * 产生新星细胞射弹，在同一位置向四周散射不同方向。
     */
    private void spawnNewProjectiles(Player owner, LivingEntity newTarget, Servant sourceServant, ServerLevel level) {
        Vec3 spawnPos = getPos();
        RandomSource rand = level.getRandom();
        int count = rand.nextInt(1, 3);
        for (int i = 0; i < count; i++) {
            // 在同一位置生成射弹，但给予不同的初始速度方向（散射）
            StardustProjectile newProjectile = new StardustProjectile(owner.getUUID(), sourceServant.getUuid(), spawnPos, newTarget);
            // 给予随机方向的初始速度，实现散射效果
            double theta = rand.nextDouble() * Math.PI * 2;
            double phi = rand.nextDouble() * Math.PI * 0.5; // 主要向上半球散射
            double speed = 0.15 + rand.nextDouble() * 0.1;
            Vec3 scatterDir = new Vec3(Math.sin(phi) * Math.cos(theta) * speed, Math.cos(phi) * speed, Math.sin(phi) * Math.sin(theta) * speed);
            newProjectile.applyForce(scatterDir.scale(2));
            EntityData data = owner.getData(AttachmentRegister.EntityData);
            data.addProjectile(newProjectile);
        }
    }

    // ===================== 命中处理 =====================

    /**
     * 命中时：造成伤害、施加寄生效果、黏贴到目标碰撞箱内随机位置。
     */
    private void onHit(Player owner, LivingEntity target) {
        // 黏贴到目标碰撞箱内随机位置
        Vec3 attachOffset = randomPositionInBoundingBox(target.getBoundingBox(), target.getRandom());
        attachTo(target, attachOffset);

        // 造成伤害
        Servant sourceServant = findServantByUuid(owner, getSourceServantUuid());
        if (sourceServant != null) {
            InvincibleData.servantAttack(target, sourceServant, 0,
                    sourceServant.getDamageSource(), sourceServant.getDamage(), InvincibleData.Type.PARTIAL);
        }

        // 施加细胞寄生效果（可叠加）
        Holder<MobEffect> effectHolder = MobEffectRegister.CellParasitism;
        MobEffectInstance existing = target.getEffect(effectHolder);
        int newAmplifier = existing == null ? 0 : Math.min(existing.getAmplifier() + 1, MAX_PARASITISM_LEVEL - 1);
        target.addEffect(new MobEffectInstance(effectHolder, PARASITISM_DURATION, newAmplifier));
    }

    /**
     * 在碰撞箱内生成随机位置偏移。
     */
    private Vec3 randomPositionInBoundingBox(AABB box, RandomSource rand) {
        double offsetX = (rand.nextDouble() - 0.5) * box.getXsize() * 0.8;
        double offsetY = rand.nextDouble() * box.getYsize() * 0.8;
        double offsetZ = (rand.nextDouble() - 0.5) * box.getZsize() * 0.8;
        return new Vec3(offsetX, offsetY, offsetZ);
    }

    // ===================== 粒子效果 =====================

    /**
     * 散射粒子效果。
     */
    private void spawnScatterParticles(ServerLevel level, Vec3 pos, int count, double speed) {
        RandomSource rand = level.getRandom();
        for (int i = 0; i < count; i++) {
            double theta = rand.nextDouble() * Math.PI * 2;
            double phi = rand.nextDouble() * Math.PI;
            double s = speed + rand.nextDouble() * speed;
            level.sendParticles(ParticleRegister.StardustScatter.get(), pos.x(), pos.y(), pos.z(), 0, Math.sin(phi) * Math.cos(theta) * s, Math.cos(phi) * s, Math.sin(phi) * Math.sin(theta) * s, 1.0);
        }
    }

    // ===================== 辅助方法 =====================

    private LivingEntity getCachedTarget(ServerLevel level) {
        if (cachedTarget == null && targetUuid != null) {
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, AABB.INFINITE)) {
                if (entity.getUUID().equals(targetUuid)) {
                    cachedTarget = entity;
                    break;
                }
            }
        }
        return cachedTarget;
    }

    private LivingEntity getCachedAttachedTarget(ServerLevel level) {
        if (cachedAttachedTarget == null && attachedTargetUuid != null) {
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, AABB.INFINITE)) {
                if (entity.getUUID().equals(attachedTargetUuid)) {
                    cachedAttachedTarget = entity;
                    break;
                }
            }
        }
        return cachedAttachedTarget;
    }

    private Servant findServantByUuid(Player owner, UUID servantUuid) {
        for (Servant servant : owner.getData(AttachmentRegister.EntityData).getServants()) {
            if (servant.getUuid().equals(servantUuid)) return servant;
        }
        return null;
    }

    // ===================== 网络同步 =====================

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) { }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) { }

    // ===================== 属性实现 =====================

    @Override
    public float getDamage() { return 6f; }

    @Override
    public ProjectileType<? extends Projectile> getProjectileType() {
        return ProjectileRegister.StardustProjectile.get();
    }
}