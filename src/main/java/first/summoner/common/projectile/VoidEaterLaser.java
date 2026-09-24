package first.summoner.common.projectile;

import first.lyra.common.attachmentEntity.AttachmentEntity;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import first.summoner.utils.SummonerStreamCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 虚空吞噬者「宇宙漩涡」形态的激光：位置与碰撞箱由 {@code VoidEaterAttackGoal} 每 tick 驱动，
 * 本类只负责保存碰撞箱、结算命中，并在本体消失后自行回收。
 * <p>
 * 颜色由渲染器 {@code VoidEaterLaserRenderer} 决定，本类不做渲染相关设置。
 */
public class VoidEaterLaser extends Projectile implements IEntityCollision<VoidEaterLaser> {

    private AABB hitbox = new AABB(0, 0, 0, 0, 0, 0);
    /** 发射者，仅服务端 tick() 使用（tick() 仅服务端调用），无需同步 */
    private @Nullable AttachmentEntity source = null;

    public VoidEaterLaser() {
        super(SummonerAttachmentEntityRegister.VOID_EATER_LASER);
        setPhysics(false);
        setMaxTickCount(-1);
    }

    /** 绑定发射者：本体消失（含超出跟随距离）后激光自行回收 */
    public void follow(AttachmentEntity source) {
        this.source = source;
    }

    @Override
    public void tick() {
        super.tick();
        if (source == null || source.isRemove() || !source.isAlive()) {
            setRemove();
        }
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher dispatcher) {
        super.registerSyncFields(dispatcher);
        dispatcher.field(SummonerStreamCodecs.AABB, this::getHitbox, this::setHitbox);
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
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hitContext : hitContexts) {
            LivingEntity living = hitContext.entity();
            attack(living, getDamage(), 0);
            Vec3 hitPoint = living.getBoundingBox().getCenter();
            ParticleHelper.create(getLevel())
                    .generic(GenericParticleBuilder.create()
                                     .centerColor(0x841af3)
                                     .edgeColor(0x6f19d4)
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
                    .velocity(hitPoint.offsetRandom(getRandom(), 2).subtract(hitPoint).normalize())
                    .count(2)
                    .speed(0.65)
                    .spread(2)
                    .emit();
        }
    }
}
