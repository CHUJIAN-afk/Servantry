package first.summoner.common.projectile;

import first.lyra.common.attachmentEntity.AttachmentEntity;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.projectile.Projectile;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.SummonerStreamCodecs;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 眼球激光塔的激光：位置、朝向、粗细、伤害全部由 {@code EyeLaserTurret} 每 tick 驱动，
 * 本类只负责保存碰撞箱、结算命中，并在发射者消失后自行回收。
 * <p>
 * 颜色由渲染器 {@code EyeLaserRenderer} 决定，本类不做渲染相关设置。
 */
public class EyeLaser extends Projectile implements IEntityCollision<EyeLaser> {

    private static final int INVINCIBLE_TICKS = 5;

    private AABB hitbox = new AABB(0, 0, 0, 0, 0, 0);
    /** 发射者，仅服务端 tick() 使用（tick() 仅服务端调用），无需同步 */
    private @Nullable AttachmentEntity source = null;

    public EyeLaser() {
        super(SummonerAttachmentEntityRegister.EYE_LASER);
        setPhysics(false);
        setMaxTickCount(-1);
    }

    /** 绑定发射者：发射者消失（含超出跟随距离）后激光自行回收 */
    public void follow(AttachmentEntity source) {
        this.source = source;
    }

    @Override
    public boolean isAlive() {
        return source != null && !source.isRemove();
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(SummonerStreamCodecs.AABB, this::getHitbox, this::setHitbox);
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
            attack(hitContext.entity(), getDamage(), INVINCIBLE_TICKS);
        }
    }
}
