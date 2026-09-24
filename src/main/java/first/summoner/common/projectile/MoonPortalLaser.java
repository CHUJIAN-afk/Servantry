package first.summoner.common.projectile;

import first.lyra.common.attachmentEntity.AttachmentEntity;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.register.SummonerMobEffectRegister;
import first.summoner.utils.ParticleHelper;
import first.summoner.utils.SummonerStreamCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 月亮传送门激光：沿预先铺好的折线飞行 20 tick，沿途射线检测方块截断碰撞箱，命中施加月噬。
 * <p>
 * 颜色由渲染器 {@code MoonPortalLaserRenderer} 决定，本类不做渲染相关设置。
 */
public class MoonPortalLaser extends Projectile implements IEntityCollision<MoonPortalLaser> {

    private static final float RADIUS = 0.15f;

    private AABB hitbox = new AABB(0, 0, 0, 0, 0, 0);
    /** 以下两个字段只在服务端 tick() 中使用（tick() 仅服务端调用），无需同步 */
    private Vec3 origin = Vec3.ZERO;
    private LivingEntity target = null;
    private @Nullable AttachmentEntity source = null;

    public MoonPortalLaser() {
        super(SummonerAttachmentEntityRegister.MOON_PORTAL_LASER);
        setPhysics(false);
        setMaxTickCount(-1);
        setMaxTickCount(20);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(SummonerStreamCodecs.AABB, this::getHitbox, this::setHitbox);
    }

    /** 设定发射原点与追踪目标，需在加入世界前调用 */
    public void aim(Vec3 origin, LivingEntity target) {
        this.origin = origin;
        this.target = target;
    }

    public void setHitbox(AABB hitbox) {
        this.hitbox = hitbox;
    }

    /** 以线段长度作为激光长度，构造朝向 +Z 的局部碰撞箱 */
    public void setHitbox(Vec3 start, Vec3 end, float radius) {
        this.hitbox = new AABB(-radius, -radius, 0, radius, radius, start.distanceTo(end));
    }

    @Override
    public @NotNull AABB getHitbox() {
        return hitbox;
    }

    @Override
    public void tick() {
        super.tick();
        float yaw = getCurrentPathNode().yaw();
        float pitch = getCurrentPathNode().pitch();
        Vec3 dir = Vec3.directionFromRotation(pitch, yaw);
        Vec3 end = origin.add(dir.scale(Math.max(origin.distanceTo(target.getBoundingBox().getCenter()), 32)));
        HitResult hit = getLevel().clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        Vec3 hitPos = hit.getType() != HitResult.Type.MISS ? hit.getLocation() : end;
        setHitbox(origin, hitPos, RADIUS);
        emitParticles(hitPos, 0.25, 10, 0.35);
    }

    public void follow(AttachmentEntity source) {
        this.source = source;
    }

    @Override
    public boolean isAlive() {
        return source != null && !source.isRemove();
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hitContext : hitContexts) {
            LivingEntity living = hitContext.entity();
            if (attack(living, getDamage(), 3)) {
                living.addEffect(new MobEffectInstance(SummonerMobEffectRegister.MoonBite, 60));
            }
            emitParticles(living.getBoundingBox().getCenter(), 0.35, 2, 0.65);
        }
    }

    private void emitParticles(Vec3 pos, double offset, int count, double speed) {
        ParticleHelper.create(getLevel())
                .generic(GenericParticleBuilder.create()
                                 .centerColor(0x00fdd6)
                                 .edgeColor(0x00d4b0)
                                 .lifetime(5)
                                 .lifetimeRandom(5)
                                 .spin(0.1f)
                                 .spinRandom(0.05F)
                                 .friction(0.75F)
                                 .scale(0.045f)
                                 .scaleRandom(0.005f)
                )
                .pos(pos)
                .offset(offset)
                .velocity(pos.offsetRandom(getRandom(), 2).subtract(pos).normalize())
                .count(count)
                .speed(speed)
                .spread(2)
                .emit();
    }
}
