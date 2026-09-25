package first.summoner.common.minion.minion.goal.ironGolem;

import first.lyra.common.attachmentEntity.AttachmentEntityGoal;
import first.summoner.common.minion.minion.IronGolem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

/**
 * 攻击：贴到目标 2 格内后每 20 tick 挥拳一次，命中后上挑击退并播放音效。
 */
public class IronGolemAttackGoal extends AttachmentEntityGoal<IronGolem> {

    private int cooldown;

    public IronGolemAttackGoal(IronGolem minion) {
        super(minion);
    }

    @Override
    public boolean canUse() {
        return minion.getTarget() != null;
    }

    @Override
    public void tick() {
        LivingEntity target = minion.getTarget();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        minion.lookAtPos(targetPos);
        cooldown--;
        if (minion.getPos().distanceTo(targetPos) < 2) {
            if (cooldown <= 0) {
                cooldown = 20;
                minion.attackTime = minion.getTickCount();
            }
            if (cooldown == 10) {
                attack(target);
            }
        } else {
            if (cooldown <= 0) {
                minion.moveTo(targetPos, 0.06f);
            }
        }
    }

    /**
     * 造成一次近战伤害，并播放攻击动画、音效与上挑击退。
     * <p>
     * 原实现跳过 confluence 的 {@code BaseBoss}：B 侧没有该类型，改用 NeoForge 通用 {@code c:bosses}
     * 标签做同一判定（见迁移报告）。
     */
    private void attack(LivingEntity target) {
        minion.attack(target, minion.getDamage(), 10);
        if (!target.getType().is(Tags.EntityTypes.BOSSES)) {
            double resistance = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.4 * minion.getKnockback() * Math.max(0.0, 1.0 - resistance), 0.0));
            target.hasImpulse = true;
        }
        minion.getLevel().playSound(null, minion.getPos().x, minion.getPos().y, minion.getPos().z, SoundEvents.IRON_GOLEM_ATTACK, minion.getSoundSource(), 1.0F, 1.0F);
    }
}
