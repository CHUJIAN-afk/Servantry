package first.summoner.common.projectile;


import first.lyra.common.attachmentEntity.Ellipse;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.lyra.utils.LyraStreamCodecs;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class Zenith extends Projectile implements IEntityCollision<Zenith> {

    public LivingEntity chaseTarget = null;
    public Vec3 direction = Vec3.ZERO;
    public Vec3 normal = Vec3.ZERO;
    public float curvature = 1;
    public float lastProgress = 0;
    public float progress = 0;

    public Zenith() {
        super(SummonerAttachmentEntityRegister.ZENITH);
        setMaxTickCount(-1);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(LyraStreamCodecs.VEC_3, () -> direction, value -> direction = value);
        fields.field(LyraStreamCodecs.VEC_3, () -> normal, value -> normal = value);
        fields.field(ByteBufCodecs.FLOAT, () -> curvature, value -> curvature = value);
        fields.field(ByteBufCodecs.INT, () -> chaseTarget != null ? chaseTarget.getId() : -1, (level, id) -> chaseTarget = id != -1 && level.getEntity(id) instanceof LivingEntity living ? living : null);
        fields.field(ByteBufCodecs.FLOAT, () -> lastProgress, value -> lastProgress = value);
        fields.field(ByteBufCodecs.FLOAT, () -> progress, value -> progress = value);
    }

    @Override
    public void tick() {
        super.tick();
        updateDirection(chaseTarget, 1);
        setCurrentPathNode(getNode(progress, 1));
        if (progress >= 1) {
            setRemove();
        }
        ParticleHelper.create(getLevel())
                .generic(GenericParticleBuilder.create()
                                 .centerColor(0xffffff)
                                 .edgeColor(0xb7b7b7)
                                 .lifetime(30)
                                 .lifetimeRandom(5)
                                 .spin(3)
                                 .spinRandom(0.1F)
                                 .friction(0.7F)
                                 .scale(0.04f)
                                 .scaleRandom(0.004f)
                )
                .pos(getPos())
                .offset(0.15)
                .velocity(getCurrentVelocity().normalize())
                .count(1)
                .speed(2)
                .spread(0.05)
                .emit();
        lastProgress = progress;
        progress = Math.min(progress + (float) 1 / 14, 1);
    }

    public void updateDirection(LivingEntity chaseTarget, float partialTick) {
        if (chaseTarget != null && chaseTarget.isAlive()) {
            Vec3 center = getPos();
            if (owner != null) {
                center = owner.getPosition(partialTick).add(0, owner.getBbHeight() / 2, 0);
            }
            Vec3 endPos = chaseTarget.getPosition(partialTick).add(0, chaseTarget.getBbHeight() / 2, 0);
            direction = endPos.subtract(center);
        }
    }

    public PathNode getNode(float progress, float partialTick) {
        Vec3 center = getPos();
        if (owner != null) {
            center = owner.getPosition(partialTick).add(0, owner.getBbHeight() / 2, 0);
        }
        Vec3 endPos = center.add(direction);
        Vec3 startPos = center.add(direction.normalize().scale(-1));
        Ellipse ellipse = new Ellipse(endPos, startPos, normal, curvature);
        Vec3 point = ellipse.getPoint(progress);
        Vec3 ellipseCenter = ellipse.getCenter();
        Vec3 tipDir = point.subtract(ellipseCenter).normalize();
        return getEulerNode(point, tipDir, normal);
    }

    @Override
    public void tickPhysics() {
    }

    @Override
    public PathNode getRenderNode(float partialTick) {
        PathNode renderNode = super.getRenderNode(partialTick);
        if (chaseTarget != null && chaseTarget.isAlive()) {
            updateDirection(chaseTarget, partialTick);
        }
        if (!direction.equals(Vec3.ZERO)) {
            return getNode(Mth.lerp(partialTick, lastProgress, progress), partialTick);
        }
        return renderNode;
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.6, -0.1, -1, 0.6, 0.1, 1.5);
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
                for (HitContext hit : hitContexts) {
                LivingEntity living = hit.entity();
                attack(living, getDamage(), 2);
            }
    }
}
