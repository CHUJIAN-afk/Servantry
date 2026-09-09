package first.summoner.common.minion;

import first.lyra.api.LyraHelper;
import first.lyra.common.attachment.InvincibleData;
import first.lyra.common.attachment.TargetCache;
import first.lyra.common.entity.IEntityCollision;
import first.lyra.common.entity.PathNode;
import first.lyra.common.entity.SyncFieldDispatcher;
import first.lyra.common.minion.Minion;
import first.lyra.common.minion.MinionGoalSelector;
import first.summoner.common.minion.goal.enchantedThrowingKnives.EnchantedThrowingKnivesAttackGoal;
import first.summoner.common.minion.goal.enchantedThrowingKnives.EnchantedThrowingKnivesIdleGoal;
import first.summoner.register.SummonerAttachmentEntityRegister;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 附魔飞刀仆从 - 围绕玩家旋转并冲刺攻击敌人。
 * <p>
 * 特性：
 * <ul>
 *   <li>空闲状态：围绕玩家旋转悬浮</li>
 *   <li>攻击状态：冲刺穿过目标，造成伤害</li>
 *   <li>碰撞攻击：冲刺过程中检测碰撞并攻击</li>
 * </ul>
 * </p>
 */
public class EnchantedThrowingKnives extends Minion implements IEntityCollision<EnchantedThrowingKnives> {

    public boolean attacking = false;

    public float idleBlend = 0f;
    public float idleBlendO = 0f;

    public EnchantedThrowingKnives() {
        super(SummonerAttachmentEntityRegister.ENCHANTED_THROWING_KNIVES);
    }

    @Override
    protected void registerSyncFields(SyncFieldDispatcher fields) {
        super.registerSyncFields(fields);
        fields.field(ByteBufCodecs.FLOAT, () -> idleBlendO, value -> idleBlendO = value);
        fields.field(ByteBufCodecs.FLOAT, () -> idleBlend, value -> idleBlend = value);
    }

    // ===================== AI 目标注册 =====================

    @Override
    public void registerGoals(MinionGoalSelector goalSelector) {
        goalSelector.addGoal(0, new EnchantedThrowingKnivesAttackGoal(this));
        goalSelector.addGoal(1, new EnchantedThrowingKnivesIdleGoal(this));
    }

    @Override
    public @NotNull AABB getHitbox() {
        return new AABB(-0.15, -0.025, -0.25, 0.15, 0.025, 0.25);
    }

    // ===================== 碰撞攻击 =====================

    @Override
    public boolean canCollideAttack() {
        return isTarget(getTarget());
    }

    @Override
    public void onCollisionAttack(List<HitContext> hitContexts) {
        for (HitContext hit : hitContexts) {
            InvincibleData.attack(hit.entity())
                    .attacker(getUuid())
                    .damageSource(getDamageSource())
                    .damageAmount(getDamage())
                    .invincibleTime(2)
                    .apply();
        }
    }

    // ===================== Tick 更新 =====================

    @Override
    public void tick() {
        if (!getOwner().level().isClientSide()) {
            // 空闲混合系数更新
            idleBlendO = idleBlend;
            if (!attacking) {
                idleBlend = Math.min(1.0f, idleBlend + 0.05f);
            } else {
                idleBlend = Math.max(0.0f, idleBlend - 0.1f);
            }
        }
        super.tick();
    }

    @Override
    public LivingEntity searchTarget() {
        int distance = this.getSearchDistance();
        if (distance > 0) {
            LyraHelper helper = LyraHelper.get(this.owner);
            TargetCache targetCache = helper.getTargetCache();
            if (!targetCache.isEmpty()) {
                List<LivingEntity> targets = targetCache.getEntitiesInRadius(this.getPos(), targetCache.getSummonSearchRange(this.getOwner(), (float) distance), (living) -> (attacking || targetCache.isVisibility(this.owner, living)) && this.isTarget(living));
                if (!targets.isEmpty()) {
                    return targetCache.getNewTarget(this, targets, 8, false);
                }
            }
        }
        return null;
    }

    @Override
    public int getSearchDistance() {
        return 32;
    }

    // ===================== 网络同步 =====================

    @Override
    public PathNode getRenderNode(float partialTick) {
        PathNode renderNode = super.getRenderNode(partialTick);
        return renderNode.lerp(getInterpolatedIdleState(partialTick), Mth.lerp(partialTick, idleBlendO, idleBlend));
    }

    // ===================== 空闲状态计算 =====================

    /**
     * 计算空闲状态下的插值位置节点。
     * <p>
     * 飞刀围绕玩家旋转悬浮，位置由玩家位置、顺序索引和总数决定。
     * </p>
     *
     * @param partialTick 部分 tick 插值进度
     * @return 插值后的空闲状态节点
     */
    public PathNode getInterpolatedIdleState(float partialTick) {
        Player owner = getOwner();
        int total = Math.max(1, getSameSizeCache());
        int order = getOrderCache();
        float angle = (owner.tickCount + partialTick) * 0.05f + (order * Mth.TWO_PI / total);
        float radius = 1.2f + (total > 4 ? (total - 4) * 0.025f : 0f);

        double px = Mth.lerp(partialTick, owner.xo, owner.getX());
        double py = Mth.lerp(partialTick, owner.yo, owner.getY());
        double pz = Mth.lerp(partialTick, owner.zo, owner.getZ());

        Vec3 targetPos = new Vec3(px, py, pz).add(Math.cos(angle) * radius, owner.getBbHeight() + 1.2, Math.sin(angle) * radius);
        Vec3 centerAxis = new Vec3(px, py + owner.getBbHeight() + 1.2, pz);

        Vec3 toAxis = centerAxis.subtract(targetPos).normalize();
        if (toAxis.lengthSqr() < 1e-4) toAxis = new Vec3(1, 0, 0);
        Vec3 tipDir = new Vec3(0, -1, 0);

        return getEulerNode(targetPos, tipDir, toAxis);
    }
}
