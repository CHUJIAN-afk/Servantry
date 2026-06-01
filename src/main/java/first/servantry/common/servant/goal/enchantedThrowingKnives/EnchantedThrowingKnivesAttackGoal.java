package first.servantry.common.servant.goal.enchantedThrowingKnives;

import first.servantry.api.servant.ai.ServantGoal;
import first.servantry.common.servant.EnchantedThrowingKnives;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 附魔飞刀攻击目标。
 */
public class EnchantedThrowingKnivesAttackGoal extends ServantGoal<EnchantedThrowingKnives> {

    /**
     * 上一次记录的目标位置，用于位置修正
     */
    private Vec3 startPos;
    private Vec3 endPos;
    private float progress = 0;

    public EnchantedThrowingKnivesAttackGoal(EnchantedThrowingKnives servant) {
        super(servant);
    }

    @Override
    public boolean canUse() {
        return servant.isTarget(servant.getTarget());
    }

    @Override
    public void start() {
        servant.attacking = true;
        refreshTarget(servant.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = servant.getTarget();
        if (servant.isTargetChange() || progress >= 1) {
            refreshTarget(target);
        }
        progress += 0.15f + target.getRandom().nextFloat() * 0.05f;
        Vec3 currentNormal = servant.getCurrentNormal();
        servant.setCurrentPathNode(servant.getEulerNode(
                startPos.lerp(endPos, progress),
                target.getBoundingBox().getCenter().subtract(servant.getPos()).normalize(),
                currentNormal
        ));
    }

    private void refreshTarget(LivingEntity target) {
        startPos = servant.getPos();
        Vec3 center = target.getBoundingBox().getCenter().offsetRandom(target.getRandom(), 0.5f);
        endPos = center.add(center.subtract(startPos).normalize().scale(2));
        progress = 0;
    }
}
