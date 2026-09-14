package first.summoner.common.minion.minion.goal.enchantedThrowingKnives;


import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.EnchantedThrowingKnives;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 附魔飞刀攻击目标。
 */
public class EnchantedThrowingKnivesAttackGoal extends AttachmentEntityGoal<EnchantedThrowingKnives> {

    /**
     * 上一次记录的目标位置，用于位置修正
     */
    private Vec3 startPos;
    private Vec3 endPos;
    private float progress = 0;

    public EnchantedThrowingKnivesAttackGoal(EnchantedThrowingKnives minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() != null;
    }

    @Override
    public void start() {
        minion.attacking = true;
        refreshTarget(minion.getTarget());
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        if (minion.isTargetChange() || progress >= 1) {
            refreshTarget(target);
        }
        progress += 0.2f + minion.getRandom().nextFloat() * 0.1f;
        Vec3 currentNormal = minion.getCurrentNormal();
        minion.setCurrentPathNode(
                minion.getEulerNode(
                        startPos.lerp(endPos, progress),
                        target.getBoundingBox().getCenter().subtract(minion.getPos()).normalize(),
                        currentNormal
                ));
    }

    private void refreshTarget(LivingEntity target) {
        startPos = minion.getPos();
        Vec3 center = target.getBoundingBox().getCenter().offsetRandom(minion.getRandom(), 0.5f);
        endPos = center.add(center.subtract(startPos).normalize().scale(2));
        progress = 0;
    }
}
