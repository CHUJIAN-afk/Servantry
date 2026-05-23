package first.servantry.common.servant;

import first.servantry.api.PathNode;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.servant.Servant;
import first.servantry.common.particle.GenericParticleBuilder;
import first.servantry.common.projectile.ShatteredStellarCoreProjectile;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.register.AttachmentRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 永夜之眼 - 环绕玩家旋转的激光仆从。
 * <p>
 * 始终环绕玩家旋转，有目标时维持原轨迹，
 * 通过激光射线对路径上所有敌人造成伤害。
 * </p>
 */
public class EtherealStellarCore extends Servant {

    private int preShootCooldown = 0;
    private int shootCooldown = 0;

    public EtherealStellarCore() {
        super();
    }

    @Override
    public void tick() {
        preShootCooldown = shootCooldown;
        if (!owner.level().isClientSide()) {
            if (shootCooldown > 0) {
                shootCooldown--;
            }
            LivingEntity target = getTarget();
            if (getShootCooldown() <= 0 && isTarget(target)) {
                shootTarget(target);
            }
            currentPathNode = getInterpolatedIdleState(1f);
        }
        super.tick();
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(shootCooldown);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        shootCooldown = buf.readInt();
    }

    public void shootTarget(LivingEntity target) {
        Player owner = getOwner();
        RandomSource random = owner.getRandom();
        shootCooldown = 18 + random.nextInt(-2, 2);
        int count = random.nextInt(2, 4);
        for (int i = 0; i < count; i++) {
            Vec3 start = getPos();
            Vec3 direction = start.offsetRandom(random, 2f).subtract(start).normalize();
            ShatteredStellarCoreProjectile projectile = new ShatteredStellarCoreProjectile(getDamageSource(), start.add(direction.scale(-0.75)), direction.scale(0.5f));
            projectile.setChaseTarget(target);
            projectile.join(owner);
            ParticleHelper.create(owner.level())
                    .generic(GenericParticleBuilder.create()
                            .color(0x7926ff)
                            .edgeColor(0x7125e2)
                            .colorRandom(0.2F, 0.2F, 0.0F)
                            .lifetime(4)
                            .lifetimeRandom(8)
                            .spin(0.1f)
                            .spinRandom(0.5F)
                            .friction(0.75F)
                            .scale(0.035f)
                            .scaleRandom(0.005f)
                    )
                    .pos(start)
                    .velocity(direction)
                    .count(2)
                    .speed(0.65)
                    .spread(0.5)
                    .emit();
        }
    }

    public int getPreShootCooldown() {
        return preShootCooldown;
    }

    public int getShootCooldown() {
        return shootCooldown;
    }

    @Override
    public float getDamage() {
        return 3.0f;
    }

    @Override
    public float getKnockback() {
        return 0.3f;
    }

    @Override
    public int getTargetDistance() {
        return 12;
    }

    @Override
    public AttachmentEntityType<? extends Servant> getType() {
        return AttachmentEntityRegister.EtherealStellarCore.get();
    }

    /**
     * 计算环绕位置，保持当前朝向不变。
     */
    public PathNode getInterpolatedIdleState(float partialTick) {
        Player owner = getOwner();
        int total = Math.max(1, owner.getData(AttachmentRegister.EntityData).getSameSize(this));
        int order = getOrder();
        float angle = (owner.tickCount + partialTick) * 0.02f + (order * Mth.TWO_PI / total);
        float radius = 1.5f;

        double px = Mth.lerp(partialTick, owner.xo, owner.getX());
        double py = Mth.lerp(partialTick, owner.yo, owner.getY());
        double pz = Mth.lerp(partialTick, owner.zo, owner.getZ());

        Vec3 orbitPos = new Vec3(px, py, pz).add(Math.cos(angle) * radius, owner.getBbHeight() / 2, Math.sin(angle) * radius);
        float cos = (float) Math.cos(angle) * 180;
        return new PathNode(orbitPos, cos, cos, cos);
    }
}
