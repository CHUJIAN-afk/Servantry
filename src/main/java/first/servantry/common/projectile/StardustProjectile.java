package first.servantry.common.projectile;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.projectile.AttachingProjectile;
import first.servantry.api.register.ProjectileType;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.register.AttachmentRegister;
import first.servantry.register.MobEffectRegister;
import first.servantry.register.ParticleRegister;
import first.servantry.register.ProjectileRegister;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * 星细胞射弹 - 追踪目标并黏着施加寄生效果。
 */
public class StardustProjectile extends AttachingProjectile {

    private static final int PARASITISM_DURATION = 100;
    private static final int MAX_PARASITISM_LEVEL = 10;

    private LivingEntity target;

    public StardustProjectile() {
        super();
        setDrag(0.95f);
        setMaxSpeed(1.2f);
    }

    public StardustProjectile(DamageSource damageSource, Vec3 startPos) {
        super(startPos, Vec3.ZERO);
        setDamageSource(damageSource);
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            ServerLevel level = (ServerLevel) owner.level();
            if (isAttached()) {
                tickAttached(owner, level);
            } else {
                tickFlying();
            }
        }
        super.tick();
    }

    private void tickFlying() {
        if (life >= 10) {
            if (target != null && target.isAlive()) {
                Vec3 targetCenter = target.getBoundingBox().getCenter();
                double dist = getPos().distanceTo(targetCenter);

                if (dist < 0.5 + target.getBbWidth() / 2.0) {
                    onHit(target);
                    return;
                }

                applyForce(targetCenter.subtract(getPos()).normalize().scale(Math.min(dist * 0.1, 0.3)));
                setTrailTimer(getTrailDuration());
            } else {
                setRemove();
            }
        }
    }

    private void tickAttached(Player owner, ServerLevel level) {
        if (target == null || !target.isAlive()) {
            onTargetDeath(owner, level);
            setRemove();
            return;
        }
        // 更新黏着位置跟随目标
        AABB box = target.getBoundingBox();
        RandomSource random = target.getRandom();
        random.setSeed(getUuid().hashCode());
        attachTo(box.getCenter().offsetRandom(random, (float) box.getSize()));
    }

    private void onHit(LivingEntity target) {
        attachTo(getPos());

        DamageSource source = getDamageSource();
        if (source != null) {
            UUID uuid = null;
            if (source instanceof ServantDamageSource servantDamageSource && servantDamageSource.getServant() instanceof Servant servant) {
                uuid = servant.getUuid();
            }
            InvincibleData.criteriaAttack(target, uuid, 0, source, getDamage(), InvincibleData.Type.PARTIAL);
        }

        MobEffectInstance existing = target.getEffect(MobEffectRegister.CellParasitism);
        int amplifier = existing == null ? 0 : Math.min(existing.getAmplifier() + 1, MAX_PARASITISM_LEVEL - 1);
        target.addEffect(new MobEffectInstance(MobEffectRegister.CellParasitism, PARASITISM_DURATION, amplifier));
    }

    private void onTargetDeath(Player owner, ServerLevel level) {
        if (getDamageSource() instanceof ServantDamageSource source && owner.getData(AttachmentRegister.EntityData).getProjectiles().size() < 500) {
            Servant servant = source.getServant();
            if (servant != null) {
                level.getEntitiesOfClass(LivingEntity.class, new AABB(getPos(), getPos()).inflate(10)).stream()
                        .filter(servant::isTarget)
                        .findAny()
                        .ifPresent(newTarget -> spawnSplit(owner, newTarget, source, level));
            }
        }
    }

    private void spawnSplit(Player owner, LivingEntity target, DamageSource source, ServerLevel level) {
        RandomSource rand = level.getRandom();
        for (int i = 0; i < rand.nextInt(1, 3); i++) {
            StardustProjectile stardustProjectile = new StardustProjectile(source, getPos());
            stardustProjectile.target = target;
            double theta = rand.nextDouble() * Math.PI * 2;
            double phi = rand.nextDouble() * Math.PI * 0.5;
            double speed = 0.15 + rand.nextDouble() * 0.1;
            stardustProjectile.applyForce(new Vec3(Math.sin(phi) * Math.cos(theta), Math.cos(phi), Math.sin(phi) * Math.sin(theta)).scale(speed * 2));
            owner.getData(AttachmentRegister.EntityData).addProjectile(stardustProjectile);
        }
    }

    @Override
    public void omRemove() {
        Level level = owner.level();
        if (!level.isClientSide()) {
            Vec3 pos = getPos();
            int count = owner.getRandom().nextInt(8, 12);
            RandomSource rand = level.getRandom();
            for (int i = 0; i < count; i++) {
                double theta = rand.nextDouble() * Math.PI * 2;
                double phi = rand.nextDouble() * Math.PI;
                double s = 0.5 + rand.nextDouble() * 0.5;
                ((ServerLevel) level).sendParticles(ParticleRegister.StardustScatter.get(), pos.x(), pos.y(), pos.z(), 0, Math.sin(phi) * Math.cos(theta) * s, Math.cos(phi) * s, Math.sin(phi) * Math.sin(theta) * s, 1.0);
            }
        }
    }

    @Override
    public float getDamage() {
        if (damageSource instanceof ServantDamageSource source) {
            Servant servant = source.getServant();
            if (servant != null) {
                return servant.getDamage();
            }
        }
        return 6;
    }

    @Override
    public ProjectileType<? extends AttachingProjectile> getProjectileType() {
        return ProjectileRegister.StardustProjectile.get();
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
    }
}
