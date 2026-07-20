package first.servantry.common.servant;

import first.servantry.api.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.common.projectile.ElectricLaser;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 激光机枪仆从 - 待机在玩家左肩，每4~6tick向目标发射电能激光射弹。
 * 无goal，主tick内处理攻击逻辑。有俯仰角无翻滚角。
 */
public class LaserMinigun extends Servant {

    private int cooldown = 0;

    public LaserMinigun() {
        super();
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            LivingEntity target = getTarget();
            PathNode idleNode = getInterpolatedIdleState(0);
            PathNode currentNode = getCurrentPathNode();

            cooldown--;
            if (isTarget(target)) {
                Vec3 targetCenter = target.getBoundingBox().getCenter();
                Vec3 direction = targetCenter.subtract(getPos()).normalize();
                float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
                float pitch = (float) Math.toDegrees(Math.asin(-direction.y));
                PathNode attackNode = new PathNode(idleNode.pos(), yaw, pitch, 0);

                setCurrentPathNode(currentNode.lerp(attackNode, 0.5f));

                if (cooldown <= 0) {
                    cooldown = 2;
                    fire(target);
                }
            } else {
                setCurrentPathNode(currentNode.lerp(idleNode, 0.5f));
            }
        }
        super.tick();
    }

    /**
     * 向目标发射电能激光射弹。
     */
    private void fire(LivingEntity target) {
        Vec3 startPos = getPos();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 direction = targetPos.subtract(startPos).normalize();
        RandomSource random = owner.getRandom();

        for (int i = 0; i < 4; i++) {
            Vec3 laserTargetPos = targetPos.add(direction.scale(5)).offsetRandom(random, 3);
            Vec3 normalize = laserTargetPos.subtract(startPos).normalize();

            ElectricLaser laser = new ElectricLaser(getDamageSource(), startPos.add(normalize.scale(random.nextFloat() * 0.5f)), normalize.scale(1.5));
            laser.join(owner);
        }

        ParticleHelper.create(owner.level())
                .generic(GenericParticleBuilder.create()
                                 .centerColor(0x4488ff)
                                 .edgeColor(0x2266dd)
                                 .lifetime(6)
                                 .lifetimeRandom(4)
                                 .spin(0.2f)
                                 .spinRandom(0.2f)
                                 .scale(0.01f)
                                 .scaleRandom(0.006f)
                                 .friction(0.7f)
                )
                .pos(getPos())
                .velocity(direction)
                .offset(0.1)
                .count(10)
                .speed(0.4)
                .spread(0.4)
                .emit();
    }

    /**
     * 计算待机位置（左肩，与ScavengerFairy/NecroSpirit右肩镜像）。
     * 在玩家左肩偏上，有上下浮动。
     */
    public PathNode getInterpolatedIdleState(float partialTick) {
        Player owner = getOwner();
        double px = Mth.lerp(partialTick, owner.xo, owner.getX());
        double py = Mth.lerp(partialTick, owner.yo, owner.getY());
        double pz = Mth.lerp(partialTick, owner.zo, owner.getZ());
        float bodyYaw = Mth.rotLerp(partialTick, owner.yBodyRotO, owner.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, owner.yHeadRotO, owner.yHeadRot);
        float playerYaw = Mth.wrapDegrees(bodyYaw + Mth.wrapDegrees(headYaw - bodyYaw) * 0.5f);
        float rad = (float) Math.toRadians(-playerYaw + 180);
        float backX = Mth.sin(rad);
        float backZ = Mth.cos(rad);
        float leftX = -Mth.cos(rad);
        float leftZ = Mth.sin(rad);
        float bob = Mth.sin((tickCount + partialTick) * 0.16f) * 0.035f;
        Vec3 pos = new Vec3(px, py, pz).add(leftX * 0.35 + backX * 0.12, owner.getBbHeight() + 0.45 + bob, leftZ * 0.35 + backZ * 0.12);
        return new PathNode(pos, playerYaw, 0, 0);
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public AttachmentEntityType<? extends Servant> getType() {
        return ServantryAttachmentEntityRegister.LASER_MINIGUN.get();
    }
}
