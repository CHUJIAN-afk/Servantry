package first.summoner.common.minion.minion;


import first.lyra.api.LyraHelper;
import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.attachmentEntity.SyncFieldDispatcher;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.minion.goal.stardustCell.StardustCellAttackGoal;
import first.summoner.common.minion.minion.goal.stardustCell.StardustCellIdleGoal;
import first.summoner.common.projectile.MiniStardustCell;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

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
public class StardustCell extends MomentumMinion {

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

    private final float recoil;

    public StardustCell() {
        super(SummonerAttachmentEntityRegister.STARDUST_CELL);
        this.recoil = new Random().nextBoolean() ? 0.325f : 0.33f;
        setDrag(0.97f);
        setGravity(0);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.INT, this::getTrailTimer, this::setTrailTimer);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new StardustCellAttackGoal(this));
        goalSelector.addGoal(1, new StardustCellIdleGoal(this));
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public void tick() {
        super.tick();
        if (!getLevel().isClientSide()) {
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
            //setDesiredRotation(currentPathNode.yaw() + 4, currentPathNode.pitch() + 4, currentPathNode.roll() + 4);
        }
    }

    public void shootExtraAtTarget(LivingEntity target){
        Vec3 start = getPos();
        // 创建并发射星细胞射弹
        Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
        MiniStardustCell projectile = new MiniStardustCell();
        projectile.setOwner(owner);
        projectile.init(new PathNode(start, direction));
        projectile.copyAttributes(this);
        projectile.setVelocity(direction);
        projectile.setDamage(getDamage() * 0.67f);
        projectile.setChaseTarget(target);
        LyraHelper.get(level).add(projectile);
        // 喷射粒子 - 星尘调色
        GenericParticleBuilder genericParticleBuilder = GenericParticleBuilder.create()
                         .centerColor(0x2fb2e1)
                         .edgeColor(0x33ccff);
        ParticleHelper.create(getLevel())
                .generic(genericParticleBuilder
                                 .lifetime(5)
                                 .lifetimeRandom(25)
                                 .spin(0.1f)
                                 .spinRandom(0.5F)
                                 .friction(0.75F)
                                 .scale(0.015f)
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
        Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
        MiniStardustCell projectile = new MiniStardustCell();
        projectile.setOwner(owner);
        projectile.init(new PathNode(start, direction));
        projectile.copyAttributes(this);
        projectile.setVelocity(direction);
        projectile.setChaseTarget(target);
        LyraHelper.get(level).add(projectile);
        // 后坐力
        addVelocity(direction.scale(-recoil));
        // 喷射粒子 - 星尘调色
        GenericParticleBuilder genericParticleBuilder = GenericParticleBuilder.create()
                .centerColor(0x2fb2e1)
                .edgeColor(0x33ccff);
        ParticleHelper.create(getLevel())
                .generic(genericParticleBuilder
                        .lifetime(5)
                        .lifetimeRandom(25)
                        .spin(0.1f)
                        .spinRandom(0.5F)
                        .friction(0.75F)
                        .scale(0.015f)
                        .scaleRandom(0.005f)
                )
                .pos(start)
                .velocity(direction)
                .count(5)
                .speed(0.65)
                .spread(0.5)
                .emit();
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
}
