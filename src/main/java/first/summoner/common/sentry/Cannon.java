package first.summoner.common.sentry;

import first.lyra.common.entity.IBlockCollision;
import first.lyra.common.entity.SyncFieldDispatcher;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.sound.Playable;
import first.summoner.common.projectile.Corn;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.Ballistics;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 玉米加农炮哨兵 - 不移动，有重力，35秒冷却，发射抛物线玉米炮精准命中目标。
 */
public class Cannon extends MomentumMinion implements IBlockCollision<Cannon> {

    /**
     * 冷却计时器，初始700（召唤时视为刚发射过）
     */
    private int cooldown = 200;
    private int shootingTick = 0;
    private boolean hasCorn = true;
    private boolean shooting = false;
    private int renderTick = 0;
    private int lastRenderTick = 0;
    private Vec3 targetPos = null;

    public Cannon() {
        super(SummonerAttachmentEntityRegister.CANNON);
        setGravity(-0.05f);
        setRotationSpeed(18f);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.BOOL, () -> shooting, value -> shooting = value);
        fields.field(ByteBufCodecs.BOOL, this::isHasCorn, value -> hasCorn = value);
    }

    @Override
    public int getSearchDistance() {
        return 64;
    }

    @Override
    public void tick() {
        if (!owner.level().isClientSide()) {
            if (getPos().distanceToSqr(owner.position()) > 128 * 128) {
                setRemove();
            }
            LivingEntity target = getTarget();
            boolean hasTarget = isTarget(target);
            if (shootingTick == 0) {
                if (--cooldown <= 0) {
                    hasCorn = true;
                    if (hasTarget) {
                        shootingTick++;
                        targetPos = target.getBoundingBox().getCenter();
                    }
                }
            } else {
                shootingTick++;
                if (shootingTick == 30) {
                    hasCorn = false;
                    fire();
                }
                if (shootingTick == 50) {
                    cooldown = 100;
                    shootingTick = 0;
                }
            }
            shooting = shootingTick != 0;
        } else {
            if (shooting) {
                lastRenderTick = renderTick;
                renderTick++;
            } else {
                lastRenderTick = 0;
                renderTick = 0;
            }
        }
        super.tick();
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        if (context.bottomSupported()) {
            Vec3 v = getVelocity();
            setVelocity(new Vec3(v.x(), 0, v.z()));
        }
    }

    @Override
    public void dimensionChange() {
        setRemove();
    }

    public void fire() {
        Vec3 start = getPos().add(0, 1, -1.5);
        Vec3 velocity = Ballistics.solveVelocity(start, targetPos, 85f, 0.99f, -0.05f);
        if (velocity != null) {
            Corn projectile = new Corn();
            projectile.setPos(start);
            projectile.copyDamageSource(this);
            projectile.setVelocity(velocity);
            projectile.join(owner);
            Playable.play(SoundEvents.GENERIC_EXPLODE, owner.level(), start, owner.getSoundSource());
        }
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-1, -1, -2, 1, 1.5, 2);
    }

    public float getShootingTick(float partialTick) {
        return Mth.lerp(partialTick, lastRenderTick, renderTick);
    }

    public boolean isHasCorn() {
        return hasCorn;
    }
}
