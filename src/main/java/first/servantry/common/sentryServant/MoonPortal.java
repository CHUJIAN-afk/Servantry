package first.servantry.common.sentryServant;

import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.common.particle.GenericParticleBuilder;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.entity.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ServantDamageSource;
import first.servantry.common.projectile.CustomLaser;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.register.ServantryMobEffectRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MoonPortal extends Servant {

    private int cooldown = 0;

    public MoonPortal() {
        super();
    }

    @Override
    public void tick() {
        super.tick();
        if (!owner.level().isClientSide()) {
            if (getPos().distanceToSqr(owner.position()) > 128 * 128) {
                setRemove();
            }
            if (--cooldown < 0 && isTarget(getTarget())) {
                cooldown = owner.getRandom().nextInt(9, 11);
                fireLaser(getTarget());
            }
        }
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    private void fireLaser(LivingEntity target) {
        Vec3 pos = getPos();
        Vec3 middlePos = target.getBoundingBox().getCenter();
        Vec3 startPos = middlePos.offsetRandom(target.getRandom(), 3);
        Vec3 endPos = middlePos.add(middlePos.subtract(startPos));

        Vec3 startDirection = pos.subtract(startPos).scale(-1).normalize();
        float startYaw = (float) Math.toDegrees(Math.atan2(-startDirection.x, startDirection.z));
        float startPitch = (float) Math.toDegrees(Math.asin(-startDirection.y));
        PathNode startPathNode = new PathNode(pos, startYaw, startPitch, 0);

        Vec3 middleDirection = pos.subtract(middlePos).scale(-1).normalize();
        float middleYaw = (float) Math.toDegrees(Math.atan2(-middleDirection.x, middleDirection.z));
        float middlePitch = (float) Math.toDegrees(Math.asin(-middleDirection.y));
        PathNode middlePathNode = new PathNode(pos, middleYaw, middlePitch, 0);

        Vec3 endDirection = pos.subtract(endPos).scale(-1).normalize();
        float endYaw = (float) Math.toDegrees(Math.atan2(-endDirection.x, endDirection.z));
        float endPitch = (float) Math.toDegrees(Math.asin(-endDirection.y));
        PathNode endPathNode = new PathNode(pos, endYaw, endPitch, 0);

        List<PathNode> pathNodes = new ArrayList<>(40);
        for (int i = 0; i < 10; i++) {
            float progress = (float) i / 9;
            pathNodes.add(startPathNode.lerp(middlePathNode, progress));
        }
        for (int i = 0; i < 10; i++) {
            float progress = (float) i / 9;
            pathNodes.add(middlePathNode.lerp(endPathNode, progress));
        }

        CustomLaser projectile = new CustomLaser(getDamageSource(), pathNodes.getFirst(), 0x00fdd6);
        projectile.setDamage(getDamage());
        projectile.setKnockback(getKnockback());
        projectile.setArmorPierce(getArmorPierce());
        projectile.setPath(pathNodes);

        projectile.setTickConsumer(laserProjectile -> {
            if (laserProjectile.getLife() >= 20) {
                laserProjectile.setRemove();
                return;
            }
            if (laserProjectile.getDamageSource() instanceof ServantDamageSource servantDamageSource) {
                Servant servant = servantDamageSource.getServant();
                if (servant.isRemove()) {
                    laserProjectile.setRemove();
                    return;
                }
            }
            // 沿当前朝向射线追踪方块，计算碰撞箱
            float pYaw = laserProjectile.getCurrentPathNode().yaw();
            float pPitch = laserProjectile.getCurrentPathNode().pitch();
            Vec3 dir = Vec3.directionFromRotation(pPitch, pYaw);
            Vec3 end = pos.add(dir.scale(32));
            HitResult hit = owner.level().clip(new ClipContext(pos, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
            Vec3 hitPos = hit.getType() != HitResult.Type.MISS ? hit.getLocation() : end;
            laserProjectile.setHitbox(pos, hitPos, 0.15f);
            Vec3 direction = hitPos.offsetRandom(owner.getRandom(), 2).subtract(hitPos).normalize();
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                                     .color(0x00fdd6)
                                     .edgeColor(0x00d4b0)
                                     .colorRandom(0, 0.2F, 0.2F)
                                     .lifetime(5)
                                     .lifetimeRandom(5)
                                     .spin(0.1f)
                                     .spinRandom(0.05F)
                                     .friction(0.75F)
                                     .scale(0.045f)
                                     .scaleRandom(0.005f)
                    )
                    .pos(hitPos)
                    .offset(0.25)
                    .velocity(direction)
                    .count(10)
                    .speed(0.35)
                    .spread(2)
                    .emit();
        });

        projectile.setHitConsumer((laser, hitContexts) -> {
            DamageSource source = getDamageSource();
            if (source != null) {
                for (ICollideAttack.HitContext hitContext : hitContexts) {
                    LivingEntity living = hitContext.entity();
                    InvincibleData.attack(living)
                            .attacker(laser.getUuid())
                            .damageSource(source)
                            .damageAmount(getDamage())
                            .invincibleTime(3)
                            .effect(new MobEffectInstance(ServantryMobEffectRegister.MoonBite, 60))
                            .apply();
                    Vec3 hitPoint = living.getBoundingBox()
                            .getCenter();
                    Vec3 direction = hitPoint.offsetRandom(owner.getRandom(), 2).subtract(hitPoint).normalize();
                    ParticleHelper.create(owner.level())
                            .generic(GenericParticleBuilder.create()
                                             .color(0x00fdd6)
                                             .edgeColor(0x00d4b0)
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
        projectile.join(owner);
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.MoonPortal.get();
    }

    @Override
    public void dimensionChange() {
        setRemove();
    }
}
