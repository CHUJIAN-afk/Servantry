package first.servantry.common.servant.goal.enchantedThrowingKnives;

import first.servantry.api.common.attachment.EntityData;
import first.servantry.api.entity.PathNode;
import first.servantry.api.entity.PlannedPath;
import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.EnchantedThrowingKnives;
import first.servantry.register.AttachmentRegister;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * 附魔飞刀攻击目标。
 * <p>
 * 攻击流程：
 * <ol>
 *   <li>发现目标后生成冲刺路径</li>
 *   <li>沿路径冲刺穿过目标</li>
 *   <li>路径完成后返回空闲状态</li>
 * </ol>
 * </p>
 */
public class EnchantedThrowingKnivesAttackGoal extends ServantGoal<EnchantedThrowingKnives> {

    /**
     * 上一次记录的目标位置，用于位置修正
     */
    private Vec3 lastTargetPos;

    public EnchantedThrowingKnivesAttackGoal(EnchantedThrowingKnives servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isTarget(servant.getTarget());
    }

    /**
     * 判断当前仆从是否可以进入攻击状态。
     * <p>
     * 选择优先级最高的空闲飞刀执行攻击。
     * </p>
     */
    private boolean canTransitionToAttack(Player owner) {
        if (owner == null) return false;
        EntityData data = owner.getData(AttachmentRegister.EntityData);
        int maxOrder = -1;
        EnchantedThrowingKnives chosen = null;
        for (var s : data.getServants()) {
            if (s instanceof EnchantedThrowingKnives knives && !knives.attacking) {
                int order = data.getOrder(s);
                if (order > maxOrder) {
                    maxOrder = order;
                    chosen = knives;
                }
            }
        }
        return chosen == servant;
    }

    @Override
    public boolean canContinueToUse() {
        return servant.isExecutingPath() || (servant.getTarget() != null && servant.isTarget(servant.getTarget()));
    }

    @Override
    public boolean isInterruptable() {
        return !servant.isExecutingPath();
    }

    @Override
    public void start() {
        servant.attacking = true;
        planDashAttack(servant.getTarget());
    }

    @Override
    public void stop() {
        servant.attacking = false;
        lastTargetPos = null;
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();

        // 目标切换检测
        if (servant.isTargetChange() && target != null && servant.isExecutingPath()) {
            // 目标切换时重新规划攻击
            planDashAttack(target);
            return;
        }

        // 位置修正
        applyPositionCorrection(target);

        // 路径完成后返回空闲
        if (!servant.isExecutingPath()) {
            if (target != null && servant.isTarget(target)) {
                // 继续攻击
                planDashAttack(target);
            }
        }
    }

    /**
     * 规划冲刺攻击路径。
     */
    private void planDashAttack(LivingEntity target) {
        if (target == null) return;

        Player owner = servant.getOwner();
        if (owner == null) return;

        // 随机偏移目标位置
        float hw = target.getBbWidth() * 0.4f;
        double offsetX = (owner.getRandom().nextDouble() - 0.5) * 2.0 * hw;
        double offsetY = target.getBbHeight() * (0.2 + owner.getRandom().nextDouble() * 0.6);
        double offsetZ = (owner.getRandom().nextDouble() - 0.5) * 2.0 * hw;

        Vec3 targetPoint = target.position().add(offsetX, offsetY, offsetZ);
        Vec3 start = servant.getPos();
        Vec3 dir = targetPoint.subtract(start);

        if (dir.lengthSqr() < 1e-4) dir = new Vec3(0, -1, 0);
        dir = dir.normalize();

        Vec3 dashEndPos = targetPoint.add(dir);
        Vec3 dashDir = dir;

        Vec3 randomUp = new Vec3(owner.getRandom().nextFloat() - 0.5, owner.getRandom().nextFloat() - 0.5, owner.getRandom().nextFloat() - 0.5).normalize();
        Vec3 dashNormal = dir.cross(randomUp).normalize();
        if (dashNormal.lengthSqr() < 1e-4) dashNormal = new Vec3(0, 1, 0);

        // 生成冲刺路径节点
        int ticks = owner.getRandom().nextInt(6, 10);
        List<PathNode> nodes = new ArrayList<>();
        for (int i = 1; i <= ticks; i++) {
            float t = (float) i / ticks;
            Vec3 p = start.lerp(dashEndPos, t);
            float dashSpinRotations = 1.0f;
            float currentAngle = t * dashSpinRotations * (float) Math.PI * 2f;

            Vector3f rotatedTipF = new Vector3f((float) dashDir.x, (float) dashDir.y, (float) dashDir.z);
            rotatedTipF.rotateAxis(currentAngle, (float) dashNormal.x, (float) dashNormal.y, (float) dashNormal.z);
            Vec3 currentTip = new Vec3(rotatedTipF.x(), rotatedTipF.y(), rotatedTipF.z());

            nodes.add(servant.getEulerNode(p, currentTip, dashNormal));
        }
        servant.setPath(nodes);
        this.lastTargetPos = target.position().add(0, target.getBbHeight() / 2.0, 0);
    }

    /**
     * 位置修正：追踪移动中的目标。
     */
    private void applyPositionCorrection(LivingEntity target) {
        if (target == null || lastTargetPos == null || !servant.isExecutingPath()) return;

        Vec3 currentTargetCenter = target.position().add(0, target.getBbHeight() / 2.0, 0);
        Vec3 offset = currentTargetCenter.subtract(lastTargetPos);

        if (offset.lengthSqr() > 1e-5) {
            PlannedPath path = servant.getCurrentPath();
            if (path != null) {
                List<PathNode> nodes = path.getNodes();
                int startIdx = path.getCurrentIndex();
                int remaining = nodes.size() - startIdx;
                for (int i = 0; i < remaining; i++) {
                    PathNode node = nodes.get(startIdx + i);
                    float weight = (float) (i + 1) / remaining;
                    Vec3 blendedOffset = offset.scale(weight);
                    nodes.set(startIdx + i, new PathNode(node.pos().add(blendedOffset), node.yaw(), node.pitch(), node.roll()));
                }
            }
        }
        lastTargetPos = currentTargetCenter;
    }
}
