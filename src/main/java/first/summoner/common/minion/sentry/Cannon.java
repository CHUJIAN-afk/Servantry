package first.summoner.common.minion.sentry;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.IBlockCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
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
    public boolean isAlive() {
        return super.isAlive() && getPos().distanceToSqr(owner.position()) <= 128 * 128;
    }

    @Override
    public void tick() {
        super.tick();
        if (!getLevel().isClientSide()) {
            LivingEntity target = getTarget();
            boolean hasTarget = target != null;
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
    }

        public void dimensionChange() {
        setRemove();
    }

    public void fire() {
        Vec3 start = getPos().add(0, 1, -1.5);
        Vec3 velocity = Ballistics.solveVelocity(start, targetPos, 85f, 0.99f, -0.05f);
        if (velocity != null) {
            Corn projectile = new Corn();
            projectile.setOwner(owner);
            projectile.init(new PathNode(start, velocity));
            projectile.copyAttributes(this);
            projectile.setVelocity(velocity);
            LyraHelper.get(level).add(projectile);
            Playable.play(SoundEvents.GENERIC_EXPLODE, level, start, getSoundSource());
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
