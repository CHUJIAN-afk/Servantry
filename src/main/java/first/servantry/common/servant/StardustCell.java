package first.servantry.common.servant;

import first.servantry.api.PathNode;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.projectile.StardustProjectile;
import first.servantry.common.servant.goal.stardustCell.StardustCellAttackGoal;
import first.servantry.common.servant.goal.stardustCell.StardustCellIdleGoal;
import first.servantry.register.AttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 星尘细胞仆从 - 追踪敌人并发射星细胞射弹。
 * <p>
 * 特性：
 * <ul>
 *   <li>瞬移攻击：瞬移到目标附近发射射弹</li>
 *   <li>玩家攻击联动：玩家攻击时33%概率额外发射射弹</li>
 *   <li>持续旋转：渲染时恒定速度旋转</li>
 * </ul>
 * </p>
 */
public class StardustCell extends MomentumServant {

    /** 拖尾计时器 */
    private int trailTimer = 0;

    /** 基础射击冷却 */
    private int shootCooldown = 0;

    /** 玩家攻击联动冷却 */
    private int extraShootCooldown = 0;


    public StardustCell() {
        super();
    }

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new StardustCellAttackGoal(this));
        goalSelector.addGoal(1, new StardustCellIdleGoal(this));
    }

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeInt(trailTimer);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        trailTimer = buf.readInt();
    }

    @Override
    public int getTargetDistance() {
        return 24;
    }

    @Override
    public void tick() {
        if (!getOwner().level().isClientSide()) {
            // 冷却衰减
            if (shootCooldown > 0) {
                shootCooldown--;
            }
            if (extraShootCooldown > 0) {
                extraShootCooldown--;
            }
            if (trailTimer > 0) {
                trailTimer--;
            }
            setDesiredRotation(currentPathNode.yaw() + 4, currentPathNode.pitch() + 4, currentPathNode.roll() + 4);
        }
        super.tick();
    }

    public void shootAtTarget(LivingEntity target) {
        Vec3 start = getPos();
        // 创建并发射星细胞射弹
        StardustProjectile projectile = new StardustProjectile(getDamageSource(), start);
        projectile.setChaseTarget(target);
        projectile.setLife(10);
        projectile.join(owner);
        // 后坐力
        Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
        applyForce(direction.scale(-0.5));
        // 喷射粒子 - 星尘调色：青蓝色带随机偏差
        ParticleHelper.create(owner.level())
                .generic(builder -> builder
                        .color(51, 204, 255)
                        .colorRandom(0.2F, 0.2F, 0.0F)
                        .lifetime(5)
                        .lifetimeRandom(25)
                        .friction(0.75F)
                        .spinRandom(0.5F)
                )
                .pos(start)
                .velocity(direction)
                .count(5)
                .speed(0.65)
                .spread(1.0)
                .emit();
    }

    @Override
    public void teleportTo(Vec3 targetPos) {
        if (getTrailTimer() == 0) {
            setTrailTimer(4);
            Vec3 start = getPos();
            float yaw = getYaw(), pitch = getPitch(), roll = getRoll();
            List<PathNode> path = new ArrayList<>();
            int tick = 3;
            for (int i = 0; i <= tick; i++) {
                float t = (float) i / tick;
                Vec3 pos = start.lerp(targetPos, t);
                path.add(new PathNode(pos, yaw, pitch, roll));
            }
            setPath(path);
        }
    }

    @Override
    public float getDamage() { return 6f; }

    @Override
    public float getKnockback() {
        return 0.2f;
    }

    @Override
    public AttachmentEntityType<StardustCell> getType() {
        return AttachmentEntityRegister.StardustCell.get();
    }

    public int getShootCooldown() { return shootCooldown; }
    public void setShootCooldown(int cooldown) { this.shootCooldown = cooldown; }

    public int getExtraShootCooldown() { return extraShootCooldown; }
    public void setExtraShootCooldown(int cooldown) { this.extraShootCooldown = cooldown; }

    public int getTrailTimer() {
        return trailTimer;
    }

    public void setTrailTimer(int timer) { this.trailTimer = timer; }

}