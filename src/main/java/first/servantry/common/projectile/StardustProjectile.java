package first.servantry.common.projectile;

import first.servantry.api.projectile.Projectile;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.common.attachment.InvincibleData;
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
 * 继承自 {@link Projectile} 基类，实现追踪目标、命中后黏贴、细胞寄生效果等功能。
 * </p>
 * <p>
 * 状态流转：FLYING（飞行追踪） -> ATTACHED（黏贴在目标身上） -> DEAD（死亡移除）。
 * </p>
 */
public class StardustProjectile extends Projectile {

    // ===================== 特有字段 =====================

    /** 额外发射冷却（用于玩家攻击联动） */
    private int extraShootCooldown = 0;

    /** 细胞寄生效果持续时间（tick） */
    private static final int PARASITISM_DURATION = 100;

    /** 细胞寄生效果最大等级 */
    private static final int MAX_PARASITISM_LEVEL = 10;

    /** 命中时的入射方向（用于确定附着位置） */
    private Vec3 hitDirection = Vec3.ZERO;


    // ===================== 构造器 =====================

    public StardustProjectile() {
        super();
    }

    public StardustProjectile(UUID ownerUuid, UUID sourceServantUuid, Vec3 startPos, LivingEntity target) {
        super(startPos, target);
        setOwnerUuid(ownerUuid);
        setSourceServantUuid(sourceServantUuid);
    }

    // ===================== 行为实现 =====================

    @Override
    public void tickBehavior(Player owner) {
        switch (getState()) {
            case FLYING -> tickFlying(owner);
            case ATTACHED -> tickAttached(owner);
            case DEAD -> { /* 无操作 */ }
        }

        // 远距离检测
        if (getPos().distanceToSqr(owner.position()) > getMaxDistance() * getMaxDistance()) {
            spawnDisappearParticles((ServerLevel) owner.level(), getPos());
            markForRemoval();
        }

        // 冷却衰减
        if (extraShootCooldown > 0) {
            extraShootCooldown--;
        }
    }

    /**
     * 飞行状态逻辑：追踪目标、检测命中。
     */
    private void tickFlying(Player owner) {
        ServerLevel level = (ServerLevel) owner.level();

        // 更新缓存的目标
        if (cachedTarget == null && targetUuid != null) {
            cachedTarget = findEntityByUuid(level, targetUuid);
        }

        LivingEntity target = cachedTarget;

        // 目标消失处理
        if (target == null || !target.isAlive()) {
            spawnDisappearParticles(level, getPos());
            markForRemoval();
            return;
        }

        // 追踪目标
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        Vec3 toTarget = targetCenter.subtract(getPos());
        double dist = toTarget.length();

        // 检测命中
        if (dist < getHitRadius() + target.getBbWidth() / 2.0) {
            onHit(owner, target);
            return;
        }

        // 计算追踪速度
        Vec3 trackingDir = toTarget.normalize();
        double trackingForce = Math.min(dist * 0.1, 0.3);
        applyForce(trackingDir.scale(trackingForce));

        // 更新朝向
        if (getVelocity().lengthSqr() > 0.01) {
            Vec3 velDir = getVelocity().normalize();
            setDesiredRotation(
                    (float) Math.toDegrees(Math.atan2(-velDir.x, velDir.z)),
                    (float) Math.toDegrees(Math.atan2(-velDir.y, Math.sqrt(velDir.x * velDir.x + velDir.z * velDir.z))),
                    getDesiredRoll()
            );
        }

        // 拖尾计时器
        setTrailTimer(getTrailDuration());
    }

    /**
     * 黏贴状态逻辑：跟随目标、检测目标消失。
     */
    private void tickAttached(Player owner) {
        ServerLevel level = (ServerLevel) owner.level();

        // 更新缓存的目标
        if (cachedAttachedTarget == null && attachedTargetUuid != null) {
            cachedAttachedTarget = findEntityByUuid(level, attachedTargetUuid);
        }

        LivingEntity target = cachedAttachedTarget;

        // 目标消失处理（死亡或移除）
        if (target == null || !target.isAlive()) {
            spawnExplosionParticles(level, getPos());
            markForRemoval();
            return;
        }

        // 跟随目标位置
        updateAttachedPosition();
    }

    /**
     * 命中处理：造成伤害、添加药水效果、切换到黏贴状态。
     */
    private void onHit(Player owner, LivingEntity target) {
        // 记录命中方向和角度
        hitDirection = getVelocity().lengthSqr() > 0.01 ? getVelocity().normalize() : Vec3.ZERO;

        // 计算附着位置（基于命中方向）
        Vec3 attachedPos = calculateAttachedPosition(target);

        // 切换到黏贴状态
        attachTo(target, attachedPos);

        // 造成伤害（使用来源仆从的伤害源和伤害值）
        Servant sourceServant = findServantByUuid(owner, getSourceServantUuid());
        if (sourceServant != null) {
            ServantDamageSource damageSource = sourceServant.getDamageSource();
            float damage = sourceServant.getDamage();
            InvincibleData.servantAttack(target, sourceServant, 0, damageSource, damage, InvincibleData.Type.PARTIAL);
        }

        // 添加细胞寄生药水效果
        Holder<MobEffect> effectHolder = MobEffectRegister.CellParasitism;
        MobEffectInstance existing = target.getEffect(effectHolder);
        int newAmplifier = existing == null ? 0 : Math.min(existing.getAmplifier() + 1, MAX_PARASITISM_LEVEL - 1);
        target.addEffect(new MobEffectInstance(effectHolder, PARASITISM_DURATION, newAmplifier));
    }

    /**
     * 根据命中方向计算在目标身上的附着位置。
     */
    private Vec3 calculateAttachedPosition(LivingEntity target) {
        AABB box = target.getBoundingBox();
        double width = box.getXsize();
        double height = box.getYsize();
        double depth = box.getZsize();

        // 根据入射方向确定附着面
        double absX = Math.abs(hitDirection.x);
        double absY = Math.abs(hitDirection.y);
        double absZ = Math.abs(hitDirection.z);

        double offsetX, offsetY, offsetZ;

        // 选择入射方向最大的分量作为附着面
        if (absY >= absX && absY >= absZ) {
            // 从上方或下方命中
            offsetX = (target.getRandom().nextDouble() - 0.5) * width * 0.6;
            offsetY = hitDirection.y > 0 ? height * 0.1 : height * 0.9;
            offsetZ = (target.getRandom().nextDouble() - 0.5) * depth * 0.6;
        } else if (absX >= absZ) {
            // 从左侧或右侧命中
            offsetX = hitDirection.x > 0 ? width * 0.1 : width * 0.9;
            offsetY = target.getRandom().nextDouble() * height * 0.8 + height * 0.1;
            offsetZ = (target.getRandom().nextDouble() - 0.5) * depth * 0.6;
        } else {
            // 从前方或后方命中
            offsetX = (target.getRandom().nextDouble() - 0.5) * width * 0.6;
            offsetY = target.getRandom().nextDouble() * height * 0.8 + height * 0.1;
            offsetZ = hitDirection.z > 0 ? depth * 0.1 : depth * 0.9;
        }

        // 转换为相对于目标位置的偏移
        return new Vec3(
                box.minX + offsetX - target.getX(),
                box.minY + offsetY - target.getY(),
                box.minZ + offsetZ - target.getZ()
        );
    }

    // ===================== 粒子效果 =====================

    /**
     * 爆裂粒子效果（目标死亡时）。
     */
    private void spawnExplosionParticles(ServerLevel level, Vec3 pos) {
        RandomSource rand = level.getRandom();
        int particleCount = 8 + rand.nextInt(6);

        for (int i = 0; i < particleCount; i++) {
            double theta = rand.nextDouble() * Math.PI * 2.0;
            double phi = rand.nextDouble() * Math.PI;
            double speed = 0.4 + rand.nextDouble() * 0.5;

            double vx = Math.sin(phi) * Math.cos(theta) * speed;
            double vy = Math.cos(phi) * speed;
            double vz = Math.sin(phi) * Math.sin(theta) * speed;

            level.sendParticles(
                    ParticleRegister.StardustScatter.get(),
                    pos.x, pos.y, pos.z,
                    0,
                    vx, vy, vz,
                    1.0
            );
        }
    }

    /**
     * 消失残留粒子效果（追踪过程中消失）。
     */
    private void spawnDisappearParticles(ServerLevel level, Vec3 pos) {
        RandomSource rand = level.getRandom();
        int particleCount = 2 + rand.nextInt(2);

        for (int i = 0; i < particleCount; i++) {
            double spreadAngle = rand.nextDouble() * Math.PI * 2.0;
            double spreadSpeed = 0.05 + rand.nextDouble() * 0.1;

            double vx = Math.cos(spreadAngle) * spreadSpeed;
            double vy = rand.nextDouble() * 0.05;
            double vz = Math.sin(spreadAngle) * spreadSpeed;

            level.sendParticles(
                    ParticleRegister.StardustScatter.get(),
                    pos.x, pos.y, pos.z,
                    0,
                    vx, vy, vz,
                    1.0
            );
        }
    }

    // ===================== 辅助方法 =====================

    private LivingEntity findEntityByUuid(ServerLevel level, UUID uuid) {
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, AABB.INFINITE)) {
            if (entity.getUUID().equals(uuid)) {
                return entity;
            }
        }
        return null;
    }

    private Servant findServantByUuid(Player owner, UUID servantUuid) {
        for (Servant servant : owner.getData(AttachmentRegister.ServantData).getServants()) {
            if (servant.getUuid().equals(servantUuid)) {
                return servant;
            }
        }
        return null;
    }

    // ===================== 网络同步 =====================

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(extraShootCooldown);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        extraShootCooldown = buf.readInt();
    }

    // ===================== 属性实现 =====================

    @Override
    public float getDamage() {
        return 6f;
    }

    @Override
    public ProjectileType<? extends Projectile> getType() {
        return ProjectileRegister.StardustProjectile.get();
    }

    // ===================== 访问器 =====================

    public int getExtraShootCooldown() {
        return extraShootCooldown;
    }

    public void setExtraShootCooldown(int cooldown) {
        this.extraShootCooldown = cooldown;
    }
}