package first.servantry.common.sentryServant;

import first.servantry.api.common.sound.Playable;
import first.servantry.api.entity.AttachmentEntity;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.servant.MomentumServant;
import first.servantry.common.projectile.Corn;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.Ballistics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 玉米加农炮哨兵 - 不移动，有重力，35秒冷却，发射抛物线玉米炮精准命中目标。
 */
public class Cannon extends MomentumServant implements IBlockCollision<Cannon> {

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
        setGravity(-0.05f);
        setRotationSpeed(18f);
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
            Corn projectile = new Corn(getDamageSource(), start, velocity);
            projectile.join(owner);
            Playable.play(SoundEvents.GENERIC_EXPLODE, owner.level(), start, owner.getSoundSource());
        }
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(shooting);
        buf.writeBoolean(hasCorn);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        shooting = buf.readBoolean();
        hasCorn = buf.readBoolean();
    }

    @Override
    public AttachmentEntityType<? extends AttachmentEntity> getType() {
        return ServantryAttachmentEntityRegister.CANNON.get();
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
