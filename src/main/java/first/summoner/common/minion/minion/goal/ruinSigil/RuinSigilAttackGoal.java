package first.summoner.common.minion.minion.goal.ruinSigil;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.lyra.common.attachmentEntity.PathNode;
import first.summoner.common.minion.minion.RuinRelic;
import net.minecraft.world.entity.LivingEntity;

/**
 * 禁戒遗迹攻击Goal - 阻隔在玩家与目标之间，每 1.75~1.92 秒发射一枚禁戒弹。
 */
public class RuinSigilAttackGoal extends AttachmentEntityGoal<RuinRelic> {

    public RuinSigilAttackGoal(RuinRelic minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() != null;
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        // 移动到攻击圆环位置（距离>8格）或待机位置（距离≤8格），看向目标
        PathNode currentPathNode = minion.getCurrentPathNode();
        PathNode targetPathNode = minion.getAttackRingNode(target, 1.0f);
        minion.setCurrentPathNode(new PathNode(currentPathNode.pos().lerp(targetPathNode.pos(), 0.2f), currentPathNode.yaw(), currentPathNode.pitch(), currentPathNode.roll()));
        minion.setDesiredRotation(targetPathNode.yaw(), targetPathNode.pitch(), targetPathNode.roll());
        // 每 1.75~1.92 秒发射一枚禁戒弹（数量越多冷却越长）
        if (minion.getCooldown() <= 0) {
            minion.shootForbiddenOrb(target);
            minion.setCooldown(minion.getAttackCooldown());
        }
    }
}
