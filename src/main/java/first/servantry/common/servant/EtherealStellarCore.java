package first.servantry.common.servant;

import first.lyra.common.entity.AttachmentEntityType;
import first.lyra.common.entity.PathNode;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.servant.Servant;
import first.servantry.common.projectile.ShatteredStellarCore;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EtherealStellarCore extends Servant {

    private int shootCooldown = 0;

    public EtherealStellarCore() {
        super();
    }

    @Override
    public void tick() {
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
        int count = random.nextInt(2, 5);
        for (int i = 0; i < count; i++) {
            Vec3 start = getPos();
            Vec3 direction = start.offsetRandom(random, 2f).subtract(start).normalize();
            ShatteredStellarCore projectile = new ShatteredStellarCore(getDamageSource(), start.add(direction.scale(-0.75)), direction.scale(0.5f));
            projectile.setDamage(getDamage());
            projectile.setChaseTarget(target);
            projectile.join(owner);
            GenericParticleBuilder genericParticleBuilder = GenericParticleBuilder.create()
                             .centerColor(0x2fb2e1)
                             .edgeColor(0x33ccff);
            ParticleHelper.create(owner.level())
                    .generic(genericParticleBuilder
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

    public int getShootCooldown() {
        return shootCooldown;
    }

    @Override
    public int getSearchDistance() {
        return 16;
    }

    @Override
    public AttachmentEntityType<? extends Servant> getType() {
        return ServantryAttachmentEntityRegister.ETHEREAL_STELLAR_CORE.get();
    }

    @Override
    public PathNode getRenderNode(float partialTick) {
        return getInterpolatedIdleState(partialTick);
    }

    /**
     * 计算环绕位置，保持当前朝向不变。
     */
    public PathNode getInterpolatedIdleState(float partialTick) {
        Player owner = getOwner();
        int total = Math.max(1, getSameSizeCache());
        int order = getOrderCache();
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
