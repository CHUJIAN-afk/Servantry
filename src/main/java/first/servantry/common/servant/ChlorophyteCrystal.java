package first.servantry.common.servant;

import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.PathNode;
import first.servantry.api.servant.Servant;
import first.servantry.common.particle.GenericParticleBuilder;
import first.servantry.common.projectile.ChlorophyteCrystalProjectile;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ChlorophyteCrystal extends Servant {

    private int shootCooldown = 20;
    private int extraShootCooldown = 20;

    public ChlorophyteCrystal() {
        super();
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            LivingEntity target = getTarget();
            if (shootCooldown > 0) {
                shootCooldown--;
            }
            if (extraShootCooldown > 0) {
                extraShootCooldown--;
            }
            if (shootCooldown <= 0 && isTarget(target)) {
                shootCooldown = 13;
                shootTarget(target);
            }
            float bob = Mth.sin((owner.tickCount) * 0.12f) * 0.15f;
            Vec3 targetPos = owner.getBoundingBox().getCenter().add(0, owner.getBbHeight() + bob, 0);
            Vec3 lerpXZ = currentPathNode.pos().lerp(targetPos, 0.8f);
            Vec3 lerpY = currentPathNode.pos().lerp(targetPos, 0.15f);
            int yaw = isTarget(getTarget()) ? 30 : 15;
            currentPathNode = new PathNode(new Vec3(lerpXZ.x(), lerpY.y(), lerpXZ.z()), currentPathNode.yaw() + yaw, currentPathNode.pitch(), currentPathNode.roll());
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

    public int getExtraShootCooldown() {
        return extraShootCooldown;
    }

    public void setExtraShootCooldown(int extraShootCooldown) {
        this.extraShootCooldown = extraShootCooldown;
    }

    public void shootTarget(LivingEntity target) {
        Player owner = getOwner();
        Vec3 direction = target.getBoundingBox().getCenter().subtract(getPos()).normalize();
        Vec3 startPos = getPos().add(direction.scale(0.25));
        ChlorophyteCrystalProjectile projectile = new ChlorophyteCrystalProjectile(getDamageSource(), startPos, direction.scale(1.5));
        projectile.join(owner);
        ParticleHelper.create(owner.level())
                .generic(GenericParticleBuilder.create()
                        .color(0x1bff10)
                        .edgeColor(0x17b70e)
                        .colorRandom(0.2F, 0.2F, 0.0F)
                        .lifetime(4)
                        .lifetimeRandom(6)
                        .spin(0.5f)
                        .spinRandom(0.5F)
                        .friction(0.75F)
                        .scale(0.015f)
                        .scaleRandom(0.0025f)
                )
                .pos(startPos)
                .offset(0.1)
                .velocity(direction)
                .count(20)
                .speed(0.5)
                .spread(0.5)
                .emit();
    }

    public PathNode getInterpolatedIdleState(float partialTick) {
        Player owner = getOwner();
        double px = Mth.lerp(partialTick, owner.xo, owner.getX());
        double py = Mth.lerp(partialTick, owner.yo, owner.getY());
        double pz = Mth.lerp(partialTick, owner.zo, owner.getZ());
        float bob = Mth.sin((owner.tickCount + partialTick) * 0.12f) * 0.05f;
        Vec3 targetPos = new Vec3(px, py, pz).add(0, owner.getBbHeight() + 1.05f + bob, 0);
        int i = isTarget(getTarget()) ? 30 : 15;
        return new PathNode(targetPos, (owner.tickCount + partialTick) * i, 0, 0);
    }

    @Override
    public float getDamage() {
        return 10;
    }

    @Override
    public float getKnockback() {
        return 1;
    }

    @Override
    public int getSlotCost() {
        return 0;
    }

    @Override
    public int getTargetDistance() {
        return 12;
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return AttachmentEntityRegister.ChlorophyteCrystal.get();
    }
}
