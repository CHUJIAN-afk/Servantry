package first.servantry.common.servant;

import first.servantry.api.common.particle.genericParticle.GenericParticleBuilder;
import first.servantry.api.entity.AttachmentEntityType;
import first.servantry.api.entity.IBlockCollision;
import first.servantry.api.servant.MomentumServant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.projectile.MiniStardustCell;
import first.servantry.common.servant.goal.stardustCell.StardustCellAttackGoal;
import first.servantry.common.servant.goal.stardustCell.StardustCellIdleGoal;
import first.servantry.register.ServantryAttachmentEntityRegister;
import first.servantry.utils.ParticleHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

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
public class StardustCell extends MomentumServant implements IBlockCollision<StardustCell> {

    /**
     * 拖尾计时器
     */
    private int trailTimer = 0;

    /**
     * 基础射击冷却
     */
    private int shootCooldown = 0;

    /**
     * 玩家攻击联动冷却
     */
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
    public int getSearchDistance() {
        return 32;
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

    public void shootExtraAtTarget(LivingEntity target){
        Vec3 start = getPos();
        // 创建并发射星细胞射弹
        MiniStardustCell projectile = new MiniStardustCell(getDamageSource(), start);
        projectile.setDamage(getDamage() * 0.67f);
        projectile.setChaseTarget(target);
        projectile.join(owner);
        // 后坐力
        Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
        // 喷射粒子 - 星尘调色
        GenericParticleBuilder genericParticleBuilder = GenericParticleBuilder.create()
                         .centerColor(0x2fb2e1)
                         .edgeColor(0x33ccff);
        ParticleHelper.create(owner.level())
                .generic(genericParticleBuilder
                                 .lifetime(5)
                                 .lifetimeRandom(25)
                                 .spin(0.1f)
                                 .spinRandom(0.5F)
                                 .friction(0.75F)
                                 .scale(0.035f)
                                 .scaleRandom(0.005f)
                )
                .pos(start)
                .velocity(direction)
                .count(5)
                .speed(0.35)
                .spread(0.3)
                .emit();
    }

    public void shootAtTarget(LivingEntity target) {
        Vec3 start = getPos();
        // 创建并发射星细胞射弹
        MiniStardustCell projectile = new MiniStardustCell(getDamageSource(), start);
        projectile.setChaseTarget(target);
        projectile.join(owner);
        // 后坐力
        Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
        applyForce(direction.scale(-0.5));
        // 喷射粒子 - 星尘调色
        GenericParticleBuilder genericParticleBuilder = GenericParticleBuilder.create()
                .centerColor(0x2fb2e1)
                .edgeColor(0x33ccff);
        ParticleHelper.create(owner.level())
                .generic(genericParticleBuilder
                        .lifetime(5)
                        .lifetimeRandom(25)
                        .spin(0.1f)
                        .spinRandom(0.5F)
                        .friction(0.75F)
                        .scale(0.035f)
                        .scaleRandom(0.005f)
                )
                .pos(start)
                .velocity(direction)
                .count(5)
                .speed(0.65)
                .spread(0.5)
                .emit();
    }

    @Override
    public void teleportTo(Vec3 targetPos) {
        setTrailTimer(4);
        super.teleportTo(targetPos);
    }

    @Override
    public AttachmentEntityType<StardustCell> getType() {
        return ServantryAttachmentEntityRegister.StardustCell.get();
    }

    public int getShootCooldown() {
        return shootCooldown;
    }

    public void setShootCooldown(int cooldown) {
        this.shootCooldown = cooldown;
    }

    public int getExtraShootCooldown() {
        return extraShootCooldown;
    }

    public void setExtraShootCooldown(int cooldown) {
        this.extraShootCooldown = cooldown;
    }

    public int getTrailTimer() {
        return trailTimer;
    }

    public void setTrailTimer(int timer) {
        this.trailTimer = timer;
    }

    @Override
    public @NotNull AABB getBlockCollisionBox() {
        return new AABB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
    }

    @Override
    public boolean canCollideWithBlocks() {
        return !isExecutingPath();
    }

    @Override
    public void onBlockCollision(CollisionContext context) {
        setVelocity(IBlockCollision.bounceVelocity(getVelocity(), context, 0.25, 0.001));
    }
}