package first.summoner.common.minion;

import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.entity.PathNode;
import first.lyra.common.entity.SyncFieldDispatcher;
import first.lyra.common.minion.MinionGoalSelector;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.goal.MomentumMinionIdleGoal;
import first.summoner.common.minion.goal.ufo.UFOAttackGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * UFO仆从 - 悬浮在敌怪上方，发射蓝色激光瞬间命中目标。
 * 无朝向自旋，无俯仰角。
 */
public class UFO extends MomentumMinion {

    private int trailTimer = 0;

    public UFO() {
        super(SummonerAttachmentEntityRegister.UFO);
        setDrag(0.75f);
        setGravity(0);
        setRotationSpeed(30f);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.INT, this::getTrailTimer, this::setTrailTimer);
    }

    @Override
    public void registerGoals(MinionGoalSelector goalSelector) {
        goalSelector.addGoal(0, new UFOAttackGoal(this));
        goalSelector.addGoal(2, new MomentumMinionIdleGoal(this, 6, 0.04f, 48, false));
    }

    @Override
    public void tick() {
        if (!getLevel().isClientSide()) {
            if (trailTimer > 0) {
                trailTimer--;
                if (trailTimer == 0) {
                    setVelocity(getCurrentVelocity().scale(0.25));
                }
            }
            PathNode current = getCurrentPathNode();
            setDesiredRotation(current.yaw() + 16, 0, 0);
        }
        super.tick();
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    /**
     * 无俯仰角：只设置yaw，pitch固定为0。
     */
    @Override
    public void lookAtDirection(Vec3 direction) {
        float targetYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        setDesiredRotation(targetYaw, 0, 0);
    }

    /**
     * 向目标发射激光（瞬间命中，无射弹实体）。
     * 直接对目标造成伤害，并用粒子模拟激光线。
     */
    public void shootLaserAt(LivingEntity target) {
        Vec3 startPos = getPos();
        Vec3 targetPos = target.getBoundingBox().getCenter();

        InvincibleData.attack(target)
                .attacker(getUuid())
                .damageSource(getDamageSource())
                .damageAmount(getDamage())
                .invincibleTime(4)
                .apply();

        // 粒子模拟激光线：从UFO到目标之间均匀生成蓝色粒子
        Level level = getLevel();
        Vec3 direction = targetPos.subtract(startPos);
        double distance = direction.length();
        direction = direction.normalize();
        int particleCount = Math.max(3, (int) (distance / 0.1));
        for (int i = 0; i < particleCount; i++) {
            double t = (double) i / particleCount;
            Vec3 particlePos = startPos.add(direction.scale(distance * t));
            ParticleHelper.create(level)
                    .generic(GenericParticleBuilder.create()
                            .centerColor(0x46f7ff)
                            .edgeColor(0x3dd6dd)
                            .lifetime(3)
                            .lifetimeRandom(2)
                            .scale(0.015f)
                            .scaleRandom(0.005f)
                    )
                    .pos(particlePos)
                    .offset(0.05)
                    .count(5)
                    .emit();
        }
    }

    @Override
    public void teleportTo(Vec3 targetPos) {
        setTrailTimer(6);
        super.teleportTo(targetPos);
    }

    public int getTrailTimer() {
        return trailTimer;
    }

    public void setTrailTimer(int timer) {
        this.trailTimer = timer;
    }

    /**
     * 激光攻击冷却：8 tick（Terraria 0.4秒=24嘀嗒，ceil(24/3)=8）。
     */
    public int getLaserCooldown() {
        RandomSource random = getRandom();
        return 7 + random.nextIntBetweenInclusive(1, 2);
    }
}
