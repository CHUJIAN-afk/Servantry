package first.servantry.common.servent;

import first.servantry.api.PathNode;
import first.servantry.api.common.attachment.InvincibleData;
import first.servantry.api.entity.ICollideAttack;
import first.servantry.api.register.ServantType;
import first.servantry.api.servant.Servant;
import first.servantry.api.servant.ai.ServantGoalSelector;
import first.servantry.common.servent.goal.EnchantedThrowingKnivesAttackGoal;
import first.servantry.common.servent.goal.EnchantedThrowingKnivesIdleGoal;
import first.servantry.register.ServantRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

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
public class EnchantedThrowingKnives extends Servant implements ICollideAttack {

    // ===================== 渲染状态 =====================

    /** 拖尾计时器 */
    public int trailTimer = 0;

    /** 空闲混合系数（用于平滑过渡到空闲状态） */
    public float idleBlend = 0f;
    public float idleBlendO = 0f;

    /** 攻击状态标记 */
    public boolean attacking = false;

    // ===================== 碰撞攻击状态 =====================

    /** 本轮攻击已命中的目标 */
    public final Set<LivingEntity> hitTargets = new HashSet<>();

    public EnchantedThrowingKnives() {
        super();
    }

    // ===================== AI 目标注册 =====================

    @Override
    public void registerGoals(ServantGoalSelector goalSelector) {
        goalSelector.addGoal(0, new EnchantedThrowingKnivesAttackGoal(this));
        goalSelector.addGoal(1, new EnchantedThrowingKnivesIdleGoal(this));
    }

    // ===================== 属性实现 =====================

    @Override
    public float getDamage() {
        return 0.6f;
    }

    @Override
    public float getKnockback() {
        return 0f;
    }

    @Override
    public AABB getHitbox() {
        return new AABB(-0.2, -0.05, -0.8, 0.2, 0.05, -0.2);
    }

    // ===================== 碰撞攻击 =====================

    @Override
    public void onCollisionAttack(Set<LivingEntity> targets) {
        if (isExecutingPath()) {
            for (LivingEntity target : targets) {
                InvincibleData.servantAttack(target, this, 10, getDamageSource(), getDamage(), InvincibleData.Type.PARTIAL);
            }
        }
    }

    // ===================== Tick 更新 =====================

    @Override
    public void tick() {
        super.tick();
        if (getOwner().level().isClientSide()) {
            // 拖尾计时器更新
            if (attacking) {
                trailTimer = 10;
            } else if (trailTimer > 0) {
                trailTimer--;
            }
            // 空闲混合系数更新
            idleBlendO = idleBlend;
            if (!attacking) {
                idleBlend = Math.min(1.0f, idleBlend + 0.1f);
            } else {
                idleBlend = Math.max(0.0f, idleBlend - 0.25f);
            }
        }
    }

    @Override
    public int getTargetDistance() {
        return 12;
    }

    @Override
    public boolean requireLineOfSight() {
        return !attacking;
    }

    // ===================== 网络同步 =====================

    @Override
    public void writeAdditional(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(attacking);
    }

    @Override
    public void readAdditional(RegistryFriendlyByteBuf buf) {
        this.attacking = buf.readBoolean();
    }

    // ===================== 空闲状态计算 =====================

    /**
     * 计算空闲状态下的插值位置节点。
     * <p>
     * 飞刀围绕玩家旋转悬浮，位置由玩家位置、顺序索引和总数决定。
     * </p>
     *
     * @param owner       玩家所有者
     * @param order       当前飞刀在同类仆从中的顺序
     * @param total       同类仆从总数
     * @param partialTick 部分 tick 插值进度
     * @return 插值后的空闲状态节点
     */
    public PathNode getInterpolatedIdleState(Player owner, int order, int total, float partialTick) {
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

    // ===================== 注册类型 =====================

    @Override
    public ServantType<? extends Servant> getServantType() {
        return ServantRegister.EnchantedThrowingKnives.get();
    }
}
