package first.summoner.common.minion.minion.goal.ruinSigil;

import first.lyra.common.entity.PathNode;
import first.lyra.common.minion.MinionGoal;
import first.summoner.common.minion.minion.RuinRelic;
import net.minecraft.world.entity.LivingEntity;

import java.util.Random;

/**
 * 禁戒遗迹攻击Goal - 阻隔在玩家与目标之间，每 1.75~1.92 秒发射一枚禁戒弹。
 */
public class RuinSigilAttackGoal extends MinionGoal<RuinRelic> {

    private final float speed;

    public RuinSigilAttackGoal(RuinRelic minion) {
        super(minion);
        this.speed = new Random().nextFloat(0.2f, 0.4f);
    }

    @Override
    public boolean canUse() {
        return minion.isTarget(minion.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        // 移动到攻击圆环位置（距离>8格）或待机位置（距离≤8格），看向目标
        PathNode currentPathNode = minion.getCurrentPathNode();
        PathNode targetPathNode = minion.getAttackRingNode(target, 1.0f);
        minion.setCurrentPathNode(currentPathNode.lerp(targetPathNode, speed));
        // 每 1.75~1.92 秒发射一枚禁戒弹（数量越多冷却越长）
        if (minion.getCooldown() <= 0) {
            minion.shootForbiddenOrb(target);
            minion.setCooldown(minion.getAttackCooldown());
        }
    }
}
