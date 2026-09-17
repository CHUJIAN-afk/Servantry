package first.summoner.common.minion.minion;



import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.Minion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.projectile.ShatteredStellarCore;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EtherealStellarCore extends Minion {

    private int shootCooldown = 0;

    public EtherealStellarCore() {
        super(SummonerAttachmentEntityRegister.ETHEREAL_STELLAR_CORE);
    }

    @Override
    public void tick() {
        super.tick();

        if (shootCooldown > 0) {
            shootCooldown--;
        }
        LivingEntity target = getTarget();
        if (getShootCooldown() <= 0 && target != null) {
            shootTarget(target);
        }
        setCurrentPathNode(getInterpolatedIdleState(1f));
    }

    public void shootTarget(LivingEntity target) {
        RandomSource random = getRandom();
        shootCooldown = 18 + random.nextInt(-2, 2);
        int count = random.nextInt(2, 5);
        for (int i = 0; i < count; i++) {
            Vec3 start = getPos();
            Vec3 direction = start.offsetRandom(random, 2f).subtract(start).normalize();
            Vec3 spawnPos = start.add(direction.scale(-0.75));
            ShatteredStellarCore projectile = new ShatteredStellarCore();
            projectile.setOwner(owner);
            projectile.init(new PathNode(spawnPos, direction));
            projectile.copyAttributes(this);
            projectile.setVelocity(direction.scale(0.5f));
            projectile.setChaseTarget(target);
            LyraHelper.get(owner).add(projectile);
            GenericParticleBuilder genericParticleBuilder = GenericParticleBuilder.create()
                             .centerColor(0x2fb2e1)
                             .edgeColor(0x33ccff);
            ParticleHelper.create(getLevel())
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
    public PathNode getRenderNode(float partialTick) {
        return getInterpolatedIdleState(partialTick);
    }

    /**
     * 计算环绕位置，保持当前朝向不变。
     */
    public PathNode getInterpolatedIdleState(float partialTick) {
        Player owner = getOwner() instanceof Player player ? player : null;
        PathNode node = getCurrentPathNode();
        if (owner != null) {
            int total = Math.max(1, getSameSize());
            int order = getOrder();
            float angle = (owner.tickCount + partialTick) * 0.02f + (order * Mth.TWO_PI / total);
            float radius = 1.5f;

            double px = Mth.lerp(partialTick, owner.xo, owner.getX());
            double py = Mth.lerp(partialTick, owner.yo, owner.getY());
            double pz = Mth.lerp(partialTick, owner.zo, owner.getZ());

            Vec3 orbitPos = new Vec3(px, py, pz).add(Math.cos(angle) * radius, owner.getBbHeight() / 2, Math.sin(angle) * radius);
            float cos = (float) Math.cos(angle) * 180;
            node = new PathNode(orbitPos, cos, cos, cos);
        }
        return node;
    }
}
