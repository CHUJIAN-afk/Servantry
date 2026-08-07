package first.servantry.common.servant;

import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.servant.Servant;
import first.servantry.common.projectile.MiniNecroSpirit;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 死魂灵巫术单元仆从 - 待机位置与ScavengerFairy一致，每5秒向目标发射死魂灵射弹。
 * 无goal，主tick内处理攻击逻辑。有俯仰角无翻滚角。
 */
public class NecroSpirit extends Servant {

    private int cooldown = 0;

    public NecroSpirit() {
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

                setCurrentPathNode(currentNode.lerp(attackNode, 0.25f));
                if (cooldown <= 0) {
                    cooldown = 200;
                    fire(target);
                }
            } else {
                setCurrentPathNode(currentNode.lerp(idleNode, 0.25f));
            }
        }
        super.tick();
    }

    /**
     * 向目标发射死魂灵射弹。
     */
    private void fire(LivingEntity target) {
        Vec3 start = getPos();
        Vec3 targetCenter = target.getBoundingBox().getCenter();
        Vec3 direction = targetCenter.subtract(start).normalize();

        MiniNecroSpirit projectile = new MiniNecroSpirit(getDamageSource(), start, direction);
        projectile.setTrackingTarget(target);
        projectile.join(owner);

        // 发射粒子效果
        ParticleHelper.create(owner.level())
                .generic(GenericParticleBuilder.create()
                                 .centerColor(0xff1200)
                                 .edgeColor(0xd40f00)
                                 .lifetime(10)
                                 .lifetimeRandom(20)
                                 .friction(0.75f)
                                 .scale(0.02f)
                                 .scaleRandom(0.01f)
                )
                .pos(start)
                .velocity(direction)
                .count(30)
                .speed(0.5)
                .spread(0.5)
                .emit();
    }

    /**
     * 计算待机位置（与ScavengerFairy一致）。
     * 在玩家右肩后方偏上，有上下浮动。
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
        float rightX = Mth.cos(rad);
        float rightZ = -Mth.sin(rad);
        float bob = Mth.sin((tickCount + partialTick) * 0.16f) * 0.035f;
        Vec3 pos = new Vec3(px, py, pz).add(rightX * 0.35 + backX * 0.12, owner.getBbHeight() + 0.45 + bob, rightZ * 0.35 + backZ * 0.12);
        return new PathNode(pos, playerYaw, 0, 0);
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public AttachmentEntityType<? extends Servant> getType() {
        return ServantryAttachmentEntityRegister.NECRO_SPIRIT.get();
    }
}
