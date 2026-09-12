package first.summoner.common.minion.minion;

import first.lyra.api.LyraHelper;
import first.lyra.common.entity.PathNode;
import first.lyra.common.minion.MinionGoalSelector;
import first.lyra.common.minion.MomentumMinion;
import first.lyra.common.particle.genericParticle.GenericParticleBuilder;
import first.summoner.common.minion.minion.goal.ruinSigil.RuinSigilAttackGoal;
import first.summoner.common.minion.minion.goal.ruinSigil.RuinSigilIdleGoal;
import first.summoner.common.projectile.ForbiddenOrb;
import first.summoner.register.SummonerAttachmentEntityRegister;
import first.summoner.utils.ParticleHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 禁戒遗迹仆从 - 待机时在玩家背后竖直圆环环绕，攻击时阻隔在玩家与目标之间。
 * <p>
 * 行为：
 * - 待机：圆环垂直于地面，位于玩家背后，看向玩家视线方向
 * - 攻击：目标距离玩家超过 8 格时移动到玩家与目标之间，否则在待机位置看向目标
 * - 每 1.75~1.92 秒发射一枚禁戒弹，发射时产生后坐力
 * - 玩家召唤数量越多，开火越慢、禁戒弹伤害越高
 * </p>
 */
public class RuinRelic extends MomentumMinion {

    private int cooldown = 0;

    public RuinRelic() {
        super(SummonerAttachmentEntityRegister.RUIN_RELIC);
        setDrag(0.85f);
        setGravity(0);
        setRotationSpeed(0);
    }

    @Override
    public void registerGoals(MinionGoalSelector goalSelector) {
        goalSelector.addGoal(0, new RuinSigilAttackGoal(this));
        goalSelector.addGoal(1, new RuinSigilIdleGoal(this));
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public void tick() {
        if (!getLevel().isClientSide() && cooldown > 0) {
            cooldown--;
        }
        super.tick();
    }

    /**
     * 向目标发射禁戒弹，产生后坐力（参考星尘细胞）。
     * <p>
     * 每多一个同类仆从，禁戒弹伤害提升 10%，至多提升 200%。
     * </p>
     */
    public void shootForbiddenOrb(LivingEntity target) {
        Vec3 start = getPos();
        Vec3 direction = target.getBoundingBox().getCenter().subtract(start).normalize();
        ForbiddenOrb orb = new ForbiddenOrb();
        orb.copyDamageSource(this);
        orb.setPos(start);
        orb.setVelocity(direction.scale(4));
        // 数量越多伤害越高：每多一个 +10%，至多 +200%
        int extra = Math.max(0, getSameSize() - 1);
        float multiplier = 1.0f + 0.1f * Math.min(extra, 20);
        orb.setDamage(getDamage() * multiplier);
        if (owner != null) {
            orb.join(owner);
        } else {
            orb.join(getLevel());
        }
        // 后坐力
        applyForce(direction.scale(-0.4));
        // 发射粒子
        ParticleHelper.create(getLevel())
                .generic(GenericParticleBuilder.create()
                        .centerColor(0xffd700)
                        .edgeColor(0xffa500)
                        .lifetime(5)
                        .lifetimeRandom(5)
                        .spin(0.2f)
                        .spinRandom(0.1F)
                        .friction(0.7F)
                        .scale(0.035f)
                        .scaleRandom(0.008f)
                )
                .pos(start)
                .velocity(direction)
                .count(5)
                .speed(0.6)
                .spread(0.4)
                .emit();
    }

    /**
     * 获取攻击冷却：基础 1.75~1.92 秒（35~38 tick），每多一个同类仆从随机上限 +1 tick，最多额外 +20 tick。
     */
    public int getAttackCooldown() {
        int extra = Math.max(0, getSameSize() - 1);
        return 35 + getRandom().nextInt(Math.min(extra, 20) + 4);
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    /**
     * 获取与自身目标一致的同类遗迹数量（分组总数）。
     */
    public int getSameTargetCount() {
        LivingEntity myTarget = getTarget();
        List<RuinRelic> relics = LyraHelper.get(getLevel()).getEntityData().get(SummonerAttachmentEntityRegister.RUIN_RELIC.get());
        int count = 0;
        for (RuinRelic relic : relics) {
            if (relic.getTarget() == myTarget) {
                count++;
            }
        }
        return Math.max(1, count);
    }

    /**
     * 获取自身在"同目标分组"中的次序。
     */
    public int getSameTargetOrder() {
        LivingEntity myTarget = getTarget();
        List<RuinRelic> relics = LyraHelper.get(getLevel()).getEntityData().get(SummonerAttachmentEntityRegister.RUIN_RELIC.get());
        int order = 0;
        for (RuinRelic relic : relics) {
            if (relic == this) {
                return order;
            }
            if (relic.getTarget() == myTarget) {
                order++;
            }
        }
        return 0;
    }

    /**
     * 待机圆环：竖直圆环位于玩家背后，看向玩家视线方向。
     * 只有目标一致的遗迹参与总数与次序；只存在一个时占据轴心。
     */
    public PathNode getIdleRingNode(float partialTick) {
        Player player = getOwner();
        PathNode node = getCurrentPathNode();
        if (player != null) {
            int total = getSameTargetCount();
            int order = getSameTargetOrder();
            // 玩家视线水平方向（圆环法向量）
            float playerYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
            float rad = (float) Math.toRadians(-playerYaw);
            Vec3 n = new Vec3(Mth.sin(rad), 0, Mth.cos(rad));
            // 竖直圆环面内正交基：v 竖直向上，u = n × v（水平垂直于视线）
            Vec3 v = new Vec3(0, 1, 0);
            Vec3 u = n.cross(v).normalize();
            // 圆环中心在玩家背后偏移
            double px = Mth.lerp(partialTick, player.xo, player.getX());
            double py = Mth.lerp(partialTick, player.yo, player.getY());
            double pz = Mth.lerp(partialTick, player.zo, player.getZ());
            Vec3 center = new Vec3(px, py + player.getBbHeight() * 0.5, pz).add(n.scale(-0.8));
            // 每只遗迹按顺序分配角度并随时间旋转；只存在一个时占据轴心
            float angle = (player.tickCount + partialTick) * 0.06f + (order * Mth.TWO_PI / total);
            float radius = total > 1 ? 1.6f : 0f;
            Vec3 pos = center.add(u.scale(Math.cos(angle) * radius)).add(v.scale(Math.sin(angle) * radius));
            node = new PathNode(pos, playerYaw, 0, 0);
        }
        return node;
    }

    /**
     * 攻击圆环：圆环轴心为玩家与目标连线，阻隔在两者之间，看向目标。
     * 仅当目标距离玩家超过 8 格时使用；否则直接在待机位置看向目标。
     * 只有目标一致的遗迹参与总数与次序；只存在一个时占据轴心。
     */
    public PathNode getAttackRingNode(LivingEntity target, float partialTick) {
        Player player = getOwner();
        PathNode node = getCurrentPathNode();
        if (player != null) {
            int total = getSameTargetCount();
            int order = getSameTargetOrder();
            Vec3 playerPos = player.getPosition(partialTick).add(0, player.getBbHeight() * 0.5, 0);
            Vec3 targetPos = target.getPosition(partialTick).add(0, target.getBbHeight() * 0.5, 0);
            double distance = playerPos.distanceTo(targetPos);
            if (distance <= 8) {
                // 目标距离玩家不超过 8 格：在待机位置看向目标
                PathNode idleNode = getIdleRingNode(partialTick);
                Vec3 toTarget = target.getBoundingBox().getCenter().subtract(idleNode.pos());
                float yaw = (float) Math.toDegrees(Math.atan2(-toTarget.x, toTarget.z));
                float pitch = (float) Math.toDegrees(Math.asin(-toTarget.y));
                node = new PathNode(idleNode.pos(), yaw, pitch, 0);
            } else {
                Vec3 axis = targetPos.subtract(playerPos).normalize();
                // 圆环面内正交基（垂直于连线）
                Vec3 v = axis.cross(new Vec3(0, 1, 0)).normalize();
                if (v.lengthSqr() < 1e-5) {
                    v = axis.cross(new Vec3(1, 0, 0)).normalize();
                }
                Vec3 u = axis.cross(v).normalize();
                // 圆环中心在连线上，偏玩家侧 20%
                Vec3 center = playerPos.add(axis.scale(Math.min(4, distance * 0.2)));
                // 每只遗迹按顺序分配角度并随时间旋转；只存在一个时占据轴心
                float angle = (player.tickCount + partialTick) * 0.06f + (order * Mth.TWO_PI / total);
                float radius = total > 1 ? 1.6f : 0f;
                Vec3 pos = center.add(u.scale(Math.cos(angle) * radius)).add(v.scale(Math.sin(angle) * radius));
                // 看向目标
                Vec3 toTarget = target.getBoundingBox().getCenter().subtract(pos);
                float yaw = (float) Math.toDegrees(Math.atan2(-toTarget.x, toTarget.z));
                float pitch = (float) Math.toDegrees(Math.asin(-toTarget.y));
                node = new PathNode(pos, yaw, pitch, 0);
            }
        }
        return node;
    }
}
