package first.summoner.common.minion.minion;


import first.lyra.common.attachmentEntity.AttachmentEntityGoalSelector;
import first.lyra.common.attachmentEntity.Ellipse;
import first.lyra.common.attachmentEntity.IEntityCollision;
import first.lyra.common.attachmentEntity.PathNode;
import first.lyra.common.minion.Minion;
import first.summoner.common.minion.minion.goal.bloodBat.BloodBatAttackGoal;
import first.summoner.common.minion.minion.goal.bloodBat.BloodBatIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BloodBat extends Minion implements IEntityCollision<BloodBat> {

    public boolean prep = false;
    public float progress = 0;
    public Vec3 normal = new Vec3(0, 1, 0);
    public boolean hasDamaged = false;
    public final Set<LivingEntity> hitTargets = new HashSet<>();

    public BloodBat() {
        super(SummonerAttachmentEntityRegister.BLOOD_BAT);
    }

    @Override
    public void registerGoals(AttachmentEntityGoalSelector goalSelector) {
        goalSelector.addGoal(0, new BloodBatAttackGoal(this));
        goalSelector.addGoal(1, new BloodBatIdleGoal(this));
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    @Override
    public LivingEntity searchTarget() {
        if (!prep) {
            return null;
        }
        if (getTarget() != null) {
            return getTarget();
        }
        return super.searchTarget();
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.15, -0.15, -0.15, 0.15, 0.15, 0.15);
    }

    @Override
    public boolean canCollideAttack() {
        return getTarget() != null;
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            LivingEntity living = hit.entity();
            if (hitTargets.add(living)) {
                attack(living, getDamage(), 0);
                if (living == getTarget()) {
                    hasDamaged = true;
                }
            }
        }
    }

    public void finishAttack() {
        progress = 0;
        hasDamaged = false;
        hitTargets.clear();
    }

    public boolean hasDamaged() {
        return hasDamaged;
    }

    /**
     * 以待机位置与目标位置为支点计算椭圆轨迹节点（切线为视角方向，无翻滚角）。
     */
    public PathNode getEllipseNode(float ellipseProgress, Vec3 idlePos, Vec3 targetPos) {
        Ellipse ellipse = new Ellipse(targetPos, idlePos, normal, 0.5f);
        Vec3 point = ellipse.getPoint(ellipseProgress);
        Vec3 tangent = ellipse.getTangent(ellipseProgress);
        float yaw = (float) Math.toDegrees(Math.atan2(-tangent.x, tangent.z));
        float pitch = (float) Math.toDegrees(Math.asin(-tangent.y));
        return new PathNode(point, yaw, pitch, 0);
    }

    /**
     * 待机状态：环绕玩家（比附魔飞刀低一段距离），看向玩家看向的方向。
     */
    public PathNode getInterpolatedIdleState(float partialTick) {
        int total = Math.max(1, getSameSize());
        int order = getOrder();
        float angle = (owner.tickCount + partialTick) * 0.05f + (order * Mth.TWO_PI / total);
        float radius = 1.2f;

        double px = Mth.lerp(partialTick, owner.xo, owner.getX());
        double py = Mth.lerp(partialTick, owner.yo, owner.getY());
        double pz = Mth.lerp(partialTick, owner.zo, owner.getZ());

        // 比附魔飞刀低一段距离（飞刀在 +1.2，蝙蝠在 +0.4）
        Vec3 targetPos = new Vec3(px, py, pz).add(Math.cos(angle) * radius, owner.getBbHeight() + 0.4, Math.sin(angle) * radius);
        // 看向玩家看向的方向，无翻滚角
        float playerYaw = Mth.rotLerp(partialTick, owner.yHeadRotO, owner.yHeadRot);
        return new PathNode(targetPos, playerYaw, 0, 0);
    }
}
