package first.summoner.common.minion.minion;



import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.Minion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.projectile.MiniChlorophyteCrystal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ChlorophyteCrystal extends Minion {

    private int shootCooldown = 20;
    private int extraShootCooldown = 20;

    public ChlorophyteCrystal() {
        super(SummonerAttachmentEntityRegister.CHLOROPHYTE_CRYSTAL);
    }

    @Override
    public void tick() {
        super.tick();
        if (!getLevel().isClientSide()) {
            LivingEntity target = getTarget();
            if (shootCooldown > 0) {
                shootCooldown--;
            }
            if (extraShootCooldown > 0) {
                extraShootCooldown--;
            }
            if (shootCooldown <= 0 && target != null) {
                shootCooldown = 13;
                shootTarget(target);
            }
            float bob = Mth.sin((tickCount) * 0.12f) * 0.15f;
            Vec3 targetPos = owner.getBoundingBox().getCenter().add(0, owner.getBbHeight() + bob, 0);
            Vec3 lerpXZ = currentPathNode.pos().lerp(targetPos, 0.8f);
            Vec3 lerpY = currentPathNode.pos().lerp(targetPos, 0.15f);
            int yaw = getTarget() != null ? 30 : 15;
            currentPathNode = new PathNode(new Vec3(lerpXZ.x(), lerpY.y(), lerpXZ.z()), currentPathNode.yaw() + yaw, currentPathNode.pitch(), currentPathNode.roll());
        }
    }

    public int getExtraShootCooldown() {
        return extraShootCooldown;
    }

    public void setExtraShootCooldown(int extraShootCooldown) {
        this.extraShootCooldown = extraShootCooldown;
    }

    public void shootTarget(LivingEntity target) {
        net.minecraft.world.level.Level level = getLevel();
        Vec3 direction = target.getBoundingBox().getCenter().subtract(getPos()).normalize();
        Vec3 startPos = getPos().add(direction.scale(0.25));
        MiniChlorophyteCrystal projectile = new MiniChlorophyteCrystal();
        projectile.setOwner(owner);
        projectile.init(new PathNode(startPos, direction));
        projectile.copyAttributes(this);
        projectile.setVelocity(direction.scale(1.5));
        LyraHelper.get(level).add(projectile);
        ParticleHelper.create(level)
                .generic(GenericParticleBuilder.create()
                        .centerColor(0x1bff10)
                        .edgeColor(0x17b70e)
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

    @Override
    public int getSearchDistance() {
        return 16;
    }
}
